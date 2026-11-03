package com.sportakus.auth.data.token

import android.content.Context

/** Persistent token storage for the app's authenticated session. */
object TokenStore {
    private const val PREFS_NAME = "sportakus_auth_tokens"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"

    fun saveTokens(context: Context, accessToken: String, refreshToken: String?) {
        val editor = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
        if (refreshToken.isNullOrBlank()) editor.remove(REFRESH_TOKEN_KEY)
        else editor.putString(REFRESH_TOKEN_KEY, refreshToken)
        editor.apply()
    }

    fun getAccessToken(context: Context): String? = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(ACCESS_TOKEN_KEY, null)
        // Migrate tokens created by versions that stored a single JWT.
        ?: context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("jwt", null)

    fun getRefreshToken(context: Context): String? = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(REFRESH_TOKEN_KEY, null)

    /** Backward-compatible alias used by UI code which decodes account claims. */
    fun getJwt(context: Context): String? = getAccessToken(context)

    fun saveJwt(context: Context, jwt: String) = saveTokens(context, jwt, null)

    fun authorizationHeader(context: Context): String? = getAccessToken(context)?.let { "Bearer $it" }

    fun clear(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .remove("jwt")
            .apply()
    }
}
