package com.example.blankapp.data

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Lightweight crash reporter for A+ Study House.
 *
 * Features:
 * - Catches all uncaught exceptions
 * - Logs crash details to local files
 * - Stores device info for context
 * - Provides crash log viewer for debugging
 * - Can be upgraded to Firebase Crashlytics later
 *
 * Usage:
 *   CrashReporter.init(context)  // In Application.onCreate()
 *   // Crashes are automatically captured
 *   // View logs: CrashReporter.getCrashLogs(context)
 */
object CrashReporter {

    private const val CRASH_DIR = "crash_reports"
    private const val MAX_CRASH_FILES = 50 // Keep last 50 crashes
    private var isInitialized = false
    private var appContext: Context? = null

    // ============================================
    // INITIALIZATION
    // ============================================

    /**
     * Initialize the crash reporter. Call this in Application.onCreate().
     */
    fun init(context: Context) {
        if (isInitialized) return

        appContext = context.applicationContext

        // Set up uncaught exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                logCrash(thread, throwable)
            } catch (e: Exception) {
                // Don't crash while handling a crash
            }

            // Pass to default handler (shows system crash dialog)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Log app start
        logEvent("APP_START", "Application started")

        isInitialized = true
    }

    // ============================================
    // CRASH LOGGING
    // ============================================

    /**
     * Log a crash to file
     */
    private fun logCrash(thread: Thread, throwable: Throwable) {
        val crashDir = getCrashDir() ?: return
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val crashFile = File(crashDir, "crash_$timestamp.txt")

        val stackTrace = StringWriter()
        throwable.printStackTrace(PrintWriter(stackTrace))

        val deviceInfo = buildDeviceInfo()

        val report = buildString {
            appendLine("========================================")
            appendLine("CRASH REPORT")
            appendLine("========================================")
            appendLine("Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())}")
            appendLine("Thread: ${thread.name} (id=${thread.id})")
            appendLine("Exception: ${throwable.javaClass.name}")
            appendLine("Message: ${throwable.message ?: "No message"}")
            appendLine()
            appendLine("--- DEVICE INFO ---")
            appendLine(deviceInfo)
            appendLine()
            appendLine("--- STACK TRACE ---")
            appendLine(stackTrace.toString())

            // Include cause chain
            var cause = throwable.cause
            var depth = 0
            while (cause != null && depth < 5) {
                appendLine()
                appendLine("--- CAUSED BY (depth ${depth + 1}) ---")
                val causeTrace = StringWriter()
                cause.printStackTrace(PrintWriter(causeTrace))
                appendLine(causeTrace.toString())
                cause = cause.cause
                depth++
            }
        }

        try {
            crashFile.writeText(report)
            cleanupOldCrashFiles(crashDir)
        } catch (e: Exception) {
            // Silently fail
        }
    }

    /**
     * Log a non-fatal event (for debugging)
     */
    fun logEvent(tag: String, message: String, details: String? = null) {
        val crashDir = getCrashDir() ?: return
        val logFile = File(crashDir, "events.log")

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val entry = buildString {
            appendLine("[$timestamp] $tag: $message")
            if (details != null) {
                appendLine("  Details: $details")
            }
        }

        try {
            logFile.appendText(entry)
        } catch (e: Exception) {
            // Silently fail
        }
    }

    /**
     * Log a caught exception (non-fatal)
     */
    fun logException(throwable: Throwable, tag: String = "Exception", message: String = "") {
        val crashDir = getCrashDir() ?: return
        val logFile = File(crashDir, "exceptions.log")

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val stackTrace = StringWriter()
        throwable.printStackTrace(PrintWriter(stackTrace))

        val entry = buildString {
            appendLine("[$timestamp] $tag: $message")
            appendLine("  Type: ${throwable.javaClass.name}")
            appendLine("  Message: ${throwable.message ?: "No message"}")
            appendLine("  Stack: ${stackTrace.toString().take(500)}")
            appendLine()
        }

        try {
            logFile.appendText(entry)
        } catch (e: Exception) {
            // Silently fail
        }
    }

    // ============================================
    // CRASH LOG RETRIEVAL
    // ============================================

    /**
     * Get all crash reports
     */
    fun getCrashLogs(context: Context): List<CrashReport> {
        val crashDir = File(context.filesDir, CRASH_DIR)
        if (!crashDir.exists()) return emptyList()

        return crashDir.listFiles()
            ?.filter { it.name.startsWith("crash_") && it.extension == "txt" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                val content = file.readText()
                val timestamp = file.name
                    .removePrefix("crash_")
                    .removeSuffix(".txt")
                    .replace("_", " ")

                CrashReport(
                    id = file.name,
                    timestamp = timestamp,
                    summary = extractSummary(content),
                    fullReport = content,
                    fileSize = formatFileSize(file.length())
                )
            } ?: emptyList()
    }

    /**
     * Get event logs
     */
    fun getEventLogs(context: Context): String {
        val logFile = File(File(context.filesDir, CRASH_DIR), "events.log")
        return if (logFile.exists()) logFile.readText() else "No events logged"
    }

    /**
     * Get exception logs
     */
    fun getExceptionLogs(context: Context): String {
        val logFile = File(File(context.filesDir, CRASH_DIR), "exceptions.log")
        return if (logFile.exists()) logFile.readText() else "No exceptions logged"
    }

    /**
     * Get crash report count
     */
    fun getCrashCount(context: Context): Int {
        val crashDir = File(context.filesDir, CRASH_DIR)
        return crashDir.listFiles()?.count { it.name.startsWith("crash_") } ?: 0
    }

    /**
     * Clear all crash logs
     */
    fun clearCrashLogs(context: Context) {
        val crashDir = File(context.filesDir, CRASH_DIR)
        crashDir.deleteRecursively()
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================

    private fun getCrashDir(): File? {
        val context = appContext ?: return null
        val dir = File(context.filesDir, CRASH_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun cleanupOldCrashFiles(crashDir: File) {
        val files = crashDir.listFiles()
            ?.filter { it.name.startsWith("crash_") }
            ?.sortedByDescending { it.lastModified() }

        files?.drop(MAX_CRASH_FILES)?.forEach { it.delete() }
    }

    private fun extractSummary(report: String): String {
        val lines = report.lines()
        val exceptionLine = lines.find { it.startsWith("Exception:") } ?: "Unknown exception"
        val messageLine = lines.find { it.startsWith("Message:") } ?: ""
        return "$exceptionLine ${messageLine.removePrefix("Message:")}".trim()
    }

    private fun buildDeviceInfo(): String {
        return buildString {
            appendLine("App Version: 1.0")
            appendLine("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Device ID: ${Build.DEVICE}")
            appendLine("Build: ${Build.DISPLAY}")
            appendLine("Board: ${Build.BOARD}")
            appendLine("Hardware: ${Build.HARDWARE}")
            appendLine("Available Memory: ${Runtime.getRuntime().freeMemory() / 1024 / 1024}MB")
            appendLine("Max Memory: ${Runtime.getRuntime().maxMemory() / 1024 / 1024}MB")
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            else -> "${bytes / 1024 / 1024}MB"
        }
    }
}

/**
 * Data class for crash reports
 */
data class CrashReport(
    val id: String,
    val timestamp: String,
    val summary: String,
    val fullReport: String,
    val fileSize: String
)
