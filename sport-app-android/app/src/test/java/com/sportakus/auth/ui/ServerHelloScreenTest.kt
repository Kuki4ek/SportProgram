package com.sportakus.auth.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.sportakus.auth.data.token.TokenStore
import com.sportakus.auth.ui.theme.SportakusAuthTheme
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ServerHelloScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var server: MockWebServer
    private lateinit var context: Context

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        context = ApplicationProvider.getApplicationContext()
        TokenStore.clear(context)
    }

    @After
    fun tearDown() {
        TokenStore.clear(context)
        server.shutdown()
    }

    @Test
    fun `ServerHelloScreen shows error when JWT is missing in TokenStore`() {
        composeTestRule.setContent {
            SportakusAuthTheme {
                ServerHelloScreen(
                    serverBaseUrl = server.url("/").toString(),
                    fallbackUsername = "",
                    onBack = {},
                    onServerSettingsClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("JWT токен не найден. Сначала выполните вход или регистрацию.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Повторить запрос").assertIsDisplayed()
    }

    @Test
    fun `ServerHelloScreen displays greeting from server when JWT is valid`() {
        TokenStore.saveJwt(context, "valid.jwt.token")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"nickname": "SportMaster"}""")
        )

        var continueClicked = false

        composeTestRule.setContent {
            SportakusAuthTheme {
                ServerHelloScreen(
                    serverBaseUrl = server.url("/").toString(),
                    fallbackUsername = "FallbackUser",
                    onBack = { continueClicked = true },
                    onServerSettingsClick = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasText("Привет, SportMaster"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Привет, SportMaster").assertIsDisplayed()
        composeTestRule.onNodeWithText("Продолжить").performClick()
        assertTrue(continueClicked)
    }
}
