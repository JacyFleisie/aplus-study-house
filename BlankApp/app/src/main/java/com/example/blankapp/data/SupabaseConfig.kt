package com.example.blankapp.data

import com.example.blankapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Supabase Configuration for A+ Study House
 *
 * Uses OkHttp for REST API calls instead of the Supabase Kotlin SDK
 * to avoid version conflicts with Kotlin 1.9.x
 *
 * Credentials are loaded from supabase.properties via BuildConfig.
 * To configure:
 * 1. Go to https://supabase.com/dashboard
 * 2. Select your project
 * 3. Go to Settings > API
 * 4. Copy the "Project URL" and "anon public" key
 * 5. Add them to supabase.properties (gitignored)
 * 6. Run the SQL migration in supabase/migrations/001_initial_schema.sql
 */
object SupabaseConfig {

    // ============================================
    // SUPABASE CREDENTIALS (from BuildConfig)
    // ============================================
    val SUPABASE_URL: String = BuildConfig.SUPABASE_URL
    val SUPABASE_ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY
    // ============================================
    // ⚠️ DO NOT ADD SECRET KEY TO CLIENT CODE
    // ============================================

    /**
     * Test-only override. When true, [isConfigured] reports false so the data layer
     * uses mock data instead of hitting Supabase. Unit tests set this in @Before so
     * they run deterministically regardless of whether supabase.properties exists.
     */
    @Volatile
    var forceMockMode: Boolean = false

    val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Check if Supabase is configured
     */
    fun isConfigured(): Boolean {
        if (forceMockMode) return false
        return SUPABASE_URL.isNotBlank() &&
               SUPABASE_ANON_KEY.isNotBlank() &&
               SUPABASE_URL.startsWith("https://")
    }

    /**
     * Make an authenticated GET request to Supabase
     */
    suspend fun supabaseGet(
        table: String,
        query: String = "",
        authToken: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/$table${if (query.isNotEmpty()) "?$query" else ""}"
            val builder = Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${authToken ?: SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")

            val response = httpClient.newCall(builder.build()).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Make an authenticated POST request to Supabase
     */
    suspend fun supabasePost(
        table: String,
        body: String,
        authToken: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/$table"
            val requestBody = body.toRequestBody(JSON_MEDIA_TYPE)
            val builder = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${authToken ?: SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")

            val response = httpClient.newCall(builder.build()).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Make an authenticated PATCH request to Supabase
     */
    suspend fun supabasePatch(
        table: String,
        body: String,
        query: String = "",
        authToken: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/$table${if (query.isNotEmpty()) "?$query" else ""}"
            val requestBody = body.toRequestBody(JSON_MEDIA_TYPE)
            val builder = Request.Builder()
                .url(url)
                .patch(requestBody)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${authToken ?: SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")

            val response = httpClient.newCall(builder.build()).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Call Supabase Auth API
     */
    suspend fun supabaseAuth(
        endpoint: String,
        body: String
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/auth/v1/$endpoint"
            val requestBody = body.toRequestBody(JSON_MEDIA_TYPE)
            val builder = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")

            val response = httpClient.newCall(builder.build()).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                JSONObject(responseBody)
            } else {
                // Try to parse error message
                if (responseBody != null) {
                    try {
                        val errorJson = JSONObject(responseBody)
                        val msg = errorJson.optString("msg", errorJson.optString("message", "Unknown error"))
                        val errorObj = JSONObject()
                        errorObj.put("error", msg)
                        errorObj
                    } catch (_: Exception) {
                        null
                    }
                } else null
            }
        } catch (e: Exception) {
            val errorObj = JSONObject()
            errorObj.put("error", e.message ?: "Network error")
            errorObj
        }
    }

    /**
     * Upload a file to a Supabase Storage bucket.
     * @param bucket   e.g. "proof-of-payment"
     * @param path     object path, e.g. "{parentId}/{applicationId}.png"
     * @param bytes    raw file bytes
     * @param contentType e.g. "image/png", "application/pdf"
     * @param authToken user JWT (so RLS scoping to auth.uid() works)
     * @return the public/storage path (Key) on success, or null on failure
     */
    suspend fun supabaseStorageUpload(
        bucket: String,
        path: String,
        bytes: ByteArray,
        contentType: String,
        authToken: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/storage/v1/object/$bucket/$path"
            val mediaType = contentType.toMediaType()
            val requestBody = bytes.toRequestBody(mediaType)
            val builder = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${authToken ?: SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", contentType)
                .addHeader("x-upsert", "true")

            val response = httpClient.newCall(builder.build()).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                json.optString("Key").ifBlank { path }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
