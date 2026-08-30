package com.example.blankapp.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the Supabase refresh token + email so a parent can be auto-logged-in
 * on next app launch ("Remember Me").
 *
 * Stores a refresh token (not the password) — on restore we exchange it for a
 * fresh access token via Supabase. If the refresh fails (expired/revoked) the
 * stored entry is cleared and the parent sees the login screen.
 */
object RememberMeStore {

    private const val PREFS = "aplus_remember_me"
    private const val KEY_EMAIL = "email"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_REMEMBER = "remember"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(context: Context, email: String, refreshToken: String) {
        prefs(context).edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putBoolean(KEY_REMEMBER, true)
            .apply()
    }

    fun getRefreshToken(context: Context): String? =
        prefs(context).getString(KEY_REFRESH_TOKEN, null)

    fun getEmail(context: Context): String? =
        prefs(context).getString(KEY_EMAIL, null)

    fun isRemembered(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REMEMBER, false) &&
            prefs(context).getString(KEY_REFRESH_TOKEN, null) != null

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
