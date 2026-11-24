package com.sportakus.auth.data.token

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TokenStoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        TokenStore.clear(context)
    }

    @After
    fun tearDown() {
        TokenStore.clear(context)
    }

    @Test
    fun `saveJwt сохраняет и возвращает токен`() {
        TokenStore.saveJwt(context, "jwt-123")

        assertEquals("jwt-123", TokenStore.getJwt(context))
        assertEquals("jwt-123", TokenStore.getAccessToken(context))
    }

    @Test
    fun `clear удаляет access и refresh токены`() {
        TokenStore.saveTokens(context, "access-1", "refresh-1")

        TokenStore.clear(context)

        assertNull(TokenStore.getAccessToken(context))
        assertNull(TokenStore.getRefreshToken(context))
        assertNull(TokenStore.getJwt(context))
    }

    @Test
    fun `authorizationHeader добавляет префикс Bearer`() {
        TokenStore.saveTokens(context, "access-token", null)

        assertEquals("Bearer access-token", TokenStore.authorizationHeader(context))
    }

    @Test
    fun `authorizationHeader отсутствует без сохранённого токена`() {
        assertNull(TokenStore.authorizationHeader(context))
    }

    @Test
    fun `saveTokens сохраняет refresh и удаляет его при null`() {
        TokenStore.saveTokens(context, "a1", "r1")
        assertEquals("r1", TokenStore.getRefreshToken(context))

        TokenStore.saveTokens(context, "a2", null)
        assertNull(TokenStore.getRefreshToken(context))
        assertEquals("a2", TokenStore.getAccessToken(context))
    }

    @Test
    fun `getAccessToken мигрирует токен из устаревшего ключа jwt`() {
        // Старые версии приложения хранили единый JWT в ключе "jwt".
        context.getSharedPreferences("sportakus_auth_tokens", Context.MODE_PRIVATE)
            .edit()
            .putString("jwt", "legacy-jwt")
            .commit()

        assertEquals("legacy-jwt", TokenStore.getAccessToken(context))

        TokenStore.clear(context)
        assertNull(TokenStore.getAccessToken(context))
    }

    @Test
    fun `blank-токен сохраняется как есть`() {
        // Документирует ФАКТИЧЕСКОЕ поведение: пустая строка не отбрасывается при
        // сохранении, и authorizationHeader возвращает "Bearer" с пустым значением.
        // Вопрос дизайна (отклонять ли blank при сохранении) вынесен в FOR_LEAD_DEVELOPER.md.
        TokenStore.saveTokens(context, "   ", null)

        assertEquals("   ", TokenStore.getAccessToken(context))
        assertEquals("Bearer " + "   ", TokenStore.authorizationHeader(context))
    }
}
