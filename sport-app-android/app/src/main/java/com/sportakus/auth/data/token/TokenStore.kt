package com.sportakus.auth.data.token

import android.content.Context

/**
 * Simple persistent JWT storage for the team's test build.
 * For production, replace with encrypted storage backed by Android Keystore.
 */
object TokenStore {
    private const val PREFS_NAME = "sportakus_auth_tokens"
    private const val JWT_KEY = "jwt"

    fun saveJwt(context: Context, jwt: String) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(JWT_KEY, jwt)
            .apply()
    }

    fun getJwt(context: Context): String? = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(JWT_KEY, null)

    /** Use this value as the Authorization header for protected requests. */
    fun authorizationHeader(context: Context): String? = getJwt(context)?.let { "Bearer $it" }

    fun clear(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(JWT_KEY)
            .apply()
    }
}
