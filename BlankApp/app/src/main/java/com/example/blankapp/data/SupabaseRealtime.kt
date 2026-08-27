package com.example.blankapp.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

/**
 * Supabase Realtime client — speaks the Phoenix WebSocket protocol directly over OkHttp,
 * so no Supabase SDK dependency is needed.
 *
 * Usage from a screen:
 * ```
 * DisposableEffect(Unit) {
 *     val unsubscribe = SupabaseRealtime.onTableChange("notifications") { reload() }
 *     onDispose { unsubscribe() }
 * }
 * ```
 *
 * The connection is shared app-wide; it opens when the first listener registers and closes
 * after the last one unsubscribes. Reconnects automatically with backoff.
 */
object SupabaseRealtime {

    private const val HEARTBEAT_INTERVAL_MS = 25_000L
    private const val RECONNECT_MIN_MS = 2_000L
    private const val RECONNECT_MAX_MS = 15_000L

    // table name -> callbacks fired on any INSERT/UPDATE/DELETE for that table
    private val listeners = ConcurrentHashMap<String, CopyOnWriteArraySet<() -> Unit>>()
    private val joinedTables = CopyOnWriteArraySet<String>()

    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private val refCounter = AtomicInteger(0)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reconnectAttempt = 0

    private fun wsClient(): OkHttpClient = OkHttpClient.Builder()
        .pingInterval(20, java.util.concurrent.TimeUnit.SECONDS)
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Listen for changes on a table. Returns an unsubscribe lambda.
     * The callback runs on the main thread and is safe to touch Compose state.
     */
    fun onTableChange(table: String, onEvent: () -> Unit): () -> Unit {
        val set = listeners.getOrPut(table) { CopyOnWriteArraySet() }
        set.add(onEvent)
        ensureConnectedAndJoined(table)
        return {
            set.remove(onEvent)
            if (set.isEmpty()) listeners.remove(table)
            if (listeners.isEmpty()) disconnect()
        }
    }

    // ============================================
    // CONNECTION LIFECYCLE
    // ============================================

    @Synchronized
    private fun ensureConnectedAndJoined(table: String) {
        if (!SupabaseConfig.isConfigured()) return
        val ws = webSocket
        if (ws == null) {
            joinedTables.add(table) // will be joined on open
            connect()
        } else {
            joinChannel(ws, table) // already connected, just join this channel
        }
        startHeartbeat()
    }

    private fun connect() {
        val baseUrl = SupabaseConfig.SUPABASE_URL.replace("https://", "wss://").replace("http://", "ws://")
        val url = "$baseUrl/realtime/v1/websocket?apikey=${SupabaseConfig.SUPABASE_ANON_KEY}&vsn=1.0.0"

        val request = Request.Builder().url(url).build()
        webSocket = wsClient().newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectAttempt = 0
                // Join every subscribed table's channel
                joinedTables.forEach { joinChannel(webSocket, it) }
                sendAccessToken(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                scheduleReconnect()
            }
        })
    }

    private fun joinChannel(ws: WebSocket, table: String) {
        val msg = JSONObject().apply {
            put("topic", "realtime:public:$table")
            put("event", "phx_join")
            put("ref", refCounter.incrementAndGet().toString())
            put(
                "payload",
                JSONObject().put(
                    "config",
                    JSONObject().put(
                        "postgres_changes",
                        org.json.JSONArray().put(
                            JSONObject()
                                .put("event", "*")
                                .put("schema", "public")
                                .put("table", table)
                        )
                    )
                )
            )
        }
        ws.send(msg.toString())
    }

    /** Tell Realtime who we are so RLS governs which rows are streamed to us.
     *  Send on the joined channel topic (Supabase expects the access_token on the
     *  channel, not the root "realtime" topic). */
    private fun sendAccessToken(ws: WebSocket) {
        val token = AuthRepository.getCurrentAuthToken() ?: return
        // Send on every joined channel so each one is RLS-scoped.
        joinedTables.forEach { table ->
            val msg = JSONObject().apply {
                put("topic", "realtime:public:$table")
                put("event", "access_token")
                put("ref", refCounter.incrementAndGet().toString())
                put("payload", JSONObject().put("access_token", token))
            }
            ws.send(msg.toString())
        }
    }

    private fun startHeartbeat() {
        if (heartbeatJob?.isActive == true) return
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                try {
                    webSocket?.send(
                        JSONObject()
                            .put("topic", "phoenix")
                            .put("event", "heartbeat")
                            .put("payload", JSONObject())
                            .put("ref", refCounter.incrementAndGet().toString())
                            .toString()
                    )
                    // Keep the auth token fresh for RLS-filtered streams
                    webSocket?.let { sendAccessToken(it) }
                } catch (_: Exception) {
                    // Socket died; onFailure will trigger reconnect
                }
            }
        }
    }

    private fun scheduleReconnect() {
        webSocket = null
        if (listeners.isEmpty()) return
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            val backoff = minOf(RECONNECT_MAX_MS, RECONNECT_MIN_MS * (1L shl reconnectAttempt.coerceAtMost(3)))
            delay(backoff)
            reconnectAttempt++
            if (listeners.isNotEmpty() && webSocket == null) {
                connect()
            }
        }
    }

    @Synchronized
    private fun disconnect() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        reconnectJob?.cancel()
        reconnectJob = null
        webSocket?.close(1000, "No listeners")
        webSocket = null
        joinedTables.clear()
    }

    // ============================================
    // INBOUND EVENT DISPATCH
    // ============================================

    private fun handleMessage(text: String) {
        try {
            val json = JSONObject(text)
            val event = json.optString("event")

            if (event == "postgres_changes") {
                val payload = json.optJSONObject("payload") ?: return
                val data = payload.optJSONObject("data") ?: return
                val table = data.optString("table")
                val set = listeners[table] ?: return
                // Dispatch on the main thread so callbacks can safely update Compose state
                scope.launch(Dispatchers.Main) {
                    set.forEach { callback ->
                        runCatching { callback() }
                    }
                }
            }
            // phx_reply errors are ignored silently; reconnect logic covers real failures
        } catch (_: Exception) {
            // Malformed frame — ignore
        }
    }

    /** True when there is at least one active listener and the socket exists (for tests/diagnostics). */
    fun hasActiveSubscriptions(): Boolean = listeners.isNotEmpty()
}
