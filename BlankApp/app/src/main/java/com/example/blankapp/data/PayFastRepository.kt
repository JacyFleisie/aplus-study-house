package com.example.blankapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

// PayFast integration — added this when we moved off manual EFT only
// Docs: https://developers.payfast.co.za/docs
object PayFastRepository {

    private const val TAG = "PayFast"

    // endpoints — sandbox for testing, prod for live
    private const val SANDBOX_URL = "https://sandbox.payfast.co.za/eng/process"
    private const val PRODUCTION_URL = "https://www.payfast.co.za/eng/process"
    private const val SANDBOX_ITN_URL = "https://sandbox.payfast.co.za/eng/query/validate"
    private const val PRODUCTION_ITN_URL = "https://www.payfast.co.za/eng/query/validate"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // config comes from app_config table — set these in Supabase dashboard
    private suspend fun getConfig(key: String): String? = withContext(Dispatchers.IO) {
        try {
            SupabaseRepository.getAppConfig(key)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun merchantId(): String? = getConfig("payfast_merchant_id")
    private suspend fun merchantKey(): String? = getConfig("payfast_merchant_key")
    private suspend fun passphrase(): String? = getConfig("payfast_passphrase")
    private suspend fun isProduction(): Boolean = getConfig("payfast_mode") == "production"

    // builds the form data + signature, returns null if merchant not configured yet
    suspend fun buildPaymentData(
        invoiceId: String,
        amount: Double,
        itemName: String,
        parentEmail: String,
        parentId: String
    ): Pair<JSONObject, String>? = withContext(Dispatchers.IO) {
        try {
            val mid = merchantId() ?: return@withContext null
            val mkey = merchantKey() ?: return@withContext null
            val pass = passphrase() ?: return@withContext null

            val data = JSONObject().apply {
                put("merchant_id", mid)
                put("merchant_key", mkey)
                put("return_url", "https://aplusstudyhouse.co.za/payment/success")
                put("cancel_url", "https://aplusstudyhouse.co.za/payment/cancel")
                put("notify_url", "https://aplusstudyhouse.co.za/api/payfast/itn")
                put("name_first", "")
                put("name_last", "")
                put("email_address", parentEmail)
                put("m_payment_id", invoiceId)
                put("amount", String.format("%.2f", amount))
                put("item_name", itemName)
                put("item_description", "Invoice #$invoiceId")
                put("custom_str1", parentId)
                put("custom_str2", invoiceId)
            }

            val signature = generateSignature(data, pass)
            data.put("signature", signature)

            Pair(data, if (isProduction()) PRODUCTION_URL else SANDBOX_URL)
        } catch (e: Exception) {
            Log.e(TAG, "buildPaymentData failed: ${e.message}")
            null
        }
    }

    // query string for the payment URL — PayFast expects sorted params
    internal fun buildQueryString(data: JSONObject): String {
        val parts = mutableListOf<String>()
        val keys = data.keys().asSequence().toList().sorted()
        for (key in keys) {
            val value = data.optString(key, "")
            if (value.isNotEmpty()) {
                parts.add("$key=$value")
            }
        }
        return parts.joinToString("&")
    }

    // MD5 signature — PayFast spec: sorted params + passphrase, then MD5
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

    // ITN callback — verifies signature and checks if payment completed
    // TODO: this is called from the edge function, not directly from app
    suspend fun verifyItn(postData: Map<String, String>): Boolean = withContext(Dispatchers.IO) {
        try {
            val pass = passphrase() ?: return@withContext false

            val sb = StringBuilder()
            val sortedKeys = postData.keys.sorted()
            for (key in sortedKeys) {
                if (key == "signature") continue
                val value = postData[key] ?: ""
                if (value.isNotEmpty()) {
                    if (sb.isNotEmpty()) sb.append("&")
                    sb.append(key).append("=").append(value)
                }
            }

            val stringToHash = if (pass.isNotEmpty()) {
                "$sb&passphrase=$pass"
            } else {
                sb.toString()
            }

            val md = MessageDigest.getInstance("MD5")
            val expectedSig = md.digest(stringToHash.toByteArray()).joinToString("") { "%02x".format(it) }
            val receivedSig = postData["signature"] ?: ""

            if (expectedSig != receivedSig) {
                Log.e(TAG, "ITN signature mismatch: expected=$expectedSig received=$receivedSig")
                return@withContext false
            }

            val paymentStatus = postData["payment_status"] ?: ""
            paymentStatus == "COMPLETE"
        } catch (e: Exception) {
            Log.e(TAG, "ITN verification failed: ${e.message}")
            false
        }
    }

    suspend fun getPaymentUrl(): String {
        return if (isProduction()) PRODUCTION_URL else SANDBOX_URL
    }

    // quick check if merchant credentials are set
    suspend fun isConfigured(): Boolean {
        return !merchantId().isNullOrBlank() && !merchantKey().isNullOrBlank()
    }
}
