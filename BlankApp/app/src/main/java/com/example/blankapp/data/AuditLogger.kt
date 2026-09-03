package com.example.blankapp.data

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AuditLogger {
    private const val TAG = "AUDIT"
    private const val FILE_NAME = "audit_log.txt"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun log(context: Context, event: String, detail: String = "") {
        val ts = dateFormat.format(Date())
        val line = "$ts | $event | $detail\n"
        logcat(event, detail, ts)
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) file.createNewFile()
            file.appendText(line)
        } catch (_: Exception) {
            // Audit write must never crash the app
        }
    }

    fun log(event: String, detail: String = "") {
        val ts = dateFormat.format(Date())
        logcat(event, detail, ts)
    }

    private fun logcat(event: String, detail: String, ts: String) {
        Log.i(TAG, "$ts | $event | $detail")
    }
}
