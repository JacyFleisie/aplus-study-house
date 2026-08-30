package com.example.blankapp.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the user's login session to SharedPreferences so the app can
 * auto-login on next launch ("Remember Me").
 *
 * Stores the email (for pre-filling) and the Supabase refresh token (for
 * silently re-authenticating without asking for credentials again).
 */
object SessionStore {
    private const val PREFS = "aplus_session"
    private const val KEY_EMAIL = "remembered_email"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_REMEMBER_ME = "remember_me"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ROLE = "user_role"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(context: Context, email: String, refreshToken: String, user: MockUser) {
        prefs(context).edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putBoolean(KEY_REMEMBER_ME, true)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.fullName)
            .putString(KEY_USER_ROLE, user.role.name)
            .apply()
    }

    fun getRememberedEmail(context: Context): String? =
        prefs(context).getString(KEY_EMAIL, null)

    fun getRefreshToken(context: Context): String? =
        prefs(context).getString(KEY_REFRESH_TOKEN, null)

    fun isRememberMeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REMEMBER_ME, false)

    /** Returns cached user profile if remember-me is enabled, else null. */
    fun getCachedUser(context: Context): MockUser? {
        val p = prefs(context)
        if (!p.getBoolean(KEY_REMEMBER_ME, false)) return null
        val id = p.getString(KEY_USER_ID, null) ?: return null
        val name = p.getString(KEY_USER_NAME, "") ?: ""
        val roleStr = p.getString(KEY_USER_ROLE, "PARENT") ?: "PARENT"
        return MockUser(
            id = id,
            fullName = name,
            email = p.getString(KEY_EMAIL, "") ?: "",
            phone = "",
            password = "",
            role = if (roleStr == "ADMIN") UserRole.ADMIN else UserRole.PARENT
        )
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
