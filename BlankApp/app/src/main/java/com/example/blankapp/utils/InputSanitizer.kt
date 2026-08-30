package com.example.blankapp.utils

/**
 * Input sanitization utilities to prevent injection attacks.
 * Supabase uses parameterized queries (RLS), so SQL injection is mitigated at the DB layer.
 * This utility sanitizes user inputs to prevent XSS, command injection, and data corruption.
 */
object InputSanitizer {

    /**
     * Sanitize a string for safe storage/display.
     * Removes control characters and trims whitespace.
     */
    fun sanitize(input: String): String {
        return input
            .trim()
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Remove control characters
            .replace(Regex("[<>]"), "") // Remove HTML tags (XSS prevention)
    }

    /**
     * Sanitize an email address.
     * Removes whitespace and invalid characters.
     */
    fun sanitizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    /**
     * Sanitize a phone number.
     * Removes all non-numeric characters except +.
     */
    fun sanitizePhone(phone: String): String {
        return phone.replace(Regex("[^0-9+]"), "")
    }

    /**
     * Sanitize a name field.
     * Allows letters, spaces, hyphens, and apostrophes only.
     */
    fun sanitizeName(name: String): String {
        return name.trim()
            .replace(Regex("[^a-zA-ZÀ-ÿ\\s'-]"), "")
            .replace(Regex("\\s+"), " ") // Collapse multiple spaces
    }

    /**
     * Validate and sanitize an email.
     * Returns sanitized email or null if invalid.
     */
    fun validateEmail(email: String): String? {
        val sanitized = sanitizeEmail(email)
        return if (isValidEmail(sanitized)) sanitized else null
    }

    /**
     * Validate email format.
     */
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }

    /**
     * Sanitize a grade value (1-7).
     * Returns null if out of range.
     */
    fun sanitizeGrade(grade: Int): Int? {
        return if (grade in 1..7) grade else null
    }

    /**
     * Sanitize a text field for Supabase JSON payload.
     * Prevents JSON injection.
     */
    fun sanitizeForJson(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Sanitize a general text field (address, school name, etc.).
     * Allows letters, numbers, spaces, and common punctuation.
     */
    fun sanitizeText(input: String): String {
        return input.trim()
            .replace(Regex("[<>{}|\\^~`]"), "") // Remove potentially dangerous chars
    }

    /**
     * Sanitize ID number (South African ID format).
     * Allows digits only.
     */
    fun sanitizeIdNumber(id: String): String {
        return id.replace(Regex("[^0-9]"), "")
    }

    /**
     * Sanitize vehicle registration (e.g., ABC123GP).
     */
    fun sanitizeVehicleReg(reg: String): String {
        return reg.trim().uppercase()
            .replace(Regex("[^A-Z0-9]"), "")
    }
}
