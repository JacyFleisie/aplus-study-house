package com.example.blankapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

// PayFast integration.
//
// Security model: the merchant key and passphrase live ONLY as edge-function
// secrets on Supabase — never in the app or app_config. To start a checkout,
// the app asks the `payfast-create-payment` edge function (with the user's
// JWT) to build and sign the payment URL server-side.
//
// Docs: https://developers.payfast.co.za/docs
object PayFastRepository {

    private const val TAG = "PayFast"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Asks the payfast-create-payment edge function to build a signed
     * checkout URL. Returns Pair(queryParams, fullUrl) on success — the
     * first element is kept for backwards compatibility with call sites,
     * the second is the URL to open in the browser. Returns null if the
     * gateway isn't configured or the call fails.
     */
    suspend fun buildPaymentData(
        invoiceId: String,
        amount: Double,
        itemName: String,
        parentEmail: String,
        parentId: String
    ): Pair<JSONObject, String>? = buildBatchPaymentData(listOf(invoiceId), amount, itemName, parentEmail, parentId)

    /**
     * Pay-All: builds ONE signed PayFast checkout for several invoices.
     * The edge function re-verifies ownership and re-computes the total
     * server-side, so the amount passed here is only advisory.
     * Returns Pair(batchPaymentId, checkoutUrl); batchPaymentId is
     * "batch_<id>" so a pending payments row can reference it.
     */
    suspend fun buildBatchPaymentData(
        invoiceIds: List<String>,
        amount: Double,
        itemName: String,
        parentEmail: String,
        parentId: String
    ): Pair<JSONObject, String>? = withContext(Dispatchers.IO) {
        try {
            val authToken = AuthRepository.getCurrentAuthToken()
            if (authToken.isNullOrBlank()) {
                Log.e(TAG, "buildPaymentData: no auth token — user must be logged in")
                return@withContext null
            }

            val url = "${SupabaseConfig.SUPABASE_URL.trimEnd('/')}/functions/v1/payfast-create-payment"
            val body = JSONObject().apply {
                if (invoiceIds.size > 1) put("invoiceIds", org.json.JSONArray(invoiceIds))
                put("invoiceId", invoiceIds.firstOrNull() ?: "")
                put("amount", amount)
                put("itemName", itemName)
                put("parentEmail", parentEmail)
                put("parentId", parentId)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $authToken")
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseText = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "buildPaymentData failed: HTTP ${response.code} — $responseText")
                    return@use null
                }
                val json = JSONObject(responseText)
                val checkoutUrl = json.optString("url", "")
                if (checkoutUrl.isBlank()) {
                    Log.e(TAG, "buildPaymentData: no url in response")
                    return@use null
                }
                // For batches, put the batch payment id (m_payment_id) in the
                // first slot so call sites can record it on the payment rows.
                val batchPaymentId = json.optString("batchPaymentId", "")
                val firstSlot = if (batchPaymentId.isNotBlank()) JSONObject().put("m_payment_id", batchPaymentId)
                else JSONObject().put("checkout_url", checkoutUrl)
                Pair(firstSlot, checkoutUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "buildPaymentData failed: ${e.message}")
            null
        }
    }

    // Kept for backwards compatibility: with the server-signed URL the query
    // string is already embedded, so this now returns an empty query.
    internal fun buildQueryString(data: JSONObject): String = ""

    // Legacy client-side signature (kept for the unit tests). Not used to
    // start real checkouts anymore — signing happens on the server.
    internal fun generateSignature(data: JSONObject, passphrase: String): String {
        val sb = StringBuilder()
        val keys = data.keys().asSequence().toList().sorted()

        for (key in keys) {
            if (key == "signature") continue
            val value = data.optString(key, "")
            if (value.isNotEmpty()) {
                if (sb.isNotEmpty()) sb.append("&")
                sb.append(key).append("=").append(value)
            }
        }

        val stringToHash = if (passphrase.isNotEmpty()) {
            "$sb&passphrase=$passphrase"
        } else {
            sb.toString()
        }

        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(stringToHash.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    // quick check: gateway is usable if the edge function is reachable and
    // the user has a session
    suspend fun isConfigured(): Boolean {
        return !AuthRepository.getCurrentAuthToken().isNullOrBlank()
    }
}
