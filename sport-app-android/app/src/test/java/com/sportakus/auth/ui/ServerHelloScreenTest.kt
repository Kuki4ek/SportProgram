package com.sportakus.auth.ui

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.sportakus.auth.BuildConfig
import com.sportakus.auth.data.token.TokenStore
import com.sportakus.auth.ui.theme.SportakusTheme
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * UI-тесты экрана проверки авторизации (ServerHelloScreen).
 *
 * Экран читает JWT/refreshToken из TokenStore и обращается к серверу
 * через AuthApi.getCurrentUserName, поэтому тесты используют MockWebServer:
 * реальный backend в юнит-тестах недоступен.
 *
 * Требуемые зависимости — см. FOR_LEAD_DEVELOPER.md.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ServerHelloScreenTest {

    companion object {
        /**
         * Compose-UI-тестам нужен ComponentActivity из ui-test-manifest
         * (debugImplementation). AndroidComposeTestRule запускает Activity
         * из statement правила — ещё до @Before, — поэтому в release-варианте
         * класс пропускается целиком на уровне ClassRule: он выполняется
         * раньше правил уровня метода, и Activity не пытается запуститься.
         */
        @ClassRule
        @JvmField
        val debugVariantGuard: TestRule =
            if (BuildConfig.DEBUG) ExternalResource() else DebugVariantSkipRule()
    }

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var server: MockWebServer
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        TokenStore.clear(context)
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
        TokenStore.clear(context)
    }

    private fun waitForNodeWithText(text: String, timeoutMillis: Long = 10_000L) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setContentServerHello(onBackToAuth: () -> Unit) {
        composeRule.setContent {
            SportakusTheme {
                ServerHelloScreen(
                    baseUrl = server.url("/").toString(),
                    onBackToAuth = onBackToAuth
                )
            }
        }
    }

    @Test
    fun showsSavedTokenErrorWhenNoJwtStored() {
        setContentServerHello(onBackToAuth = {})

        waitForNodeWithText("JWT не найден на устройстве. Сначала выполни вход или регистрацию.")
        composeRule.onNodeWithText("Повторить запрос").assertExists()
        composeRule.onNodeWithText("Вернуться к авторизации").assertExists()

        // Без сохранённого JWT экран не должен обращаться к серверу.
        assertEquals(0, server.requestCount)
    }

    @Test
    fun loadsDisplayNameFromServerProfile() {
        TokenStore.saveTokens(context, "valid-jwt", "valid-refresh")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"nickname":"SportMaster"}""")
        )
        var backToAuth = false
        setContentServerHello(onBackToAuth = { backToAuth = true })

        waitForNodeWithText("Привет, SportMaster")
        composeRule.onNodeWithText("Имя получено из GET /api/users/me с JWT в заголовке Authorization: Bearer.").assertExists()

        composeRule.onNodeWithText("Вернуться к авторизации").performClick()
        composeRule.runOnIdle { assertTrue(backToAuth) }

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/users/me", request.path)
        assertEquals("Bearer valid-jwt", request.getHeader("Authorization"))
    }

    @Test
    fun clearsTokensAndReturnsToAuthOn401() {
        TokenStore.saveTokens(context, "stale-jwt", null)
        // Пустое тело ответа: текст ошибки берётся из статуса HTTP 401.
        server.enqueue(MockResponse().setResponseCode(401))
        var backToAuth = false
        setContentServerHello(onBackToAuth = { backToAuth = true })

        waitForNodeWithText("Сессия недействительна. Необходимо войти в приложение снова.")
        composeRule.onNodeWithText("Не удалось получить имя пользователя с сервера.").assertExists()

        // При 401 экран очищает хранилище и возвращает к авторизации.
        composeRule.waitUntil(10_000L) { backToAuth }
        composeRule.runOnIdle {
            assertNull("При 401 access-токен должен удаляться из хранилища", TokenStore.getAccessToken(context))
            assertNull("При 401 refresh-токен должен удаляться из хранилища", TokenStore.getRefreshToken(context))
        }

        // Без сохранённого refresh-токена повторный запрос не выполняется.
        assertEquals(1, server.requestCount)
    }
}
