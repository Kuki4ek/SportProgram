package com.sportakus.auth.data.token

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TokenStoreTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        TokenStore.clear(context)
    }

    @Test
    fun `getJwt returns null when no token is saved`() {
        assertNull(TokenStore.getJwt(context))
        assertNull(TokenStore.authorizationHeader(context))
    }

    @Test
    fun `saveJwt stores token and getJwt retrieves it`() {
        val sampleJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.sample"
        TokenStore.saveJwt(context, sampleJwt)

        assertEquals(sampleJwt, TokenStore.getJwt(context))
    }

    @Test
    fun `authorizationHeader formats stored token with Bearer prefix`() {
        val sampleJwt = "eyJhbGciOiJIUzI1NiJ9.auth"
        TokenStore.saveJwt(context, sampleJwt)

        assertEquals("Bearer $sampleJwt", TokenStore.authorizationHeader(context))
    }

    @Test
    fun `authorizationHeader returns null when stored token is blank`() {
        TokenStore.saveJwt(context, "   ")

        assertNull(TokenStore.authorizationHeader(context))
    }

    @Test
    fun `clear removes stored token from preferences`() {
        TokenStore.saveJwt(context, "token_to_remove")
        assertEquals("token_to_remove", TokenStore.getJwt(context))

        TokenStore.clear(context)

        assertNull(TokenStore.getJwt(context))
        assertNull(TokenStore.authorizationHeader(context))
    }
}
