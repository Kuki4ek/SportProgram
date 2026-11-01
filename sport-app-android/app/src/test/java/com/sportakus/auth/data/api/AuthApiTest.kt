package com.sportakus.auth.data.api

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthApiTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // --- Блок 1: Валидация URL и входных данных до отправки сети ---

    @Test
    fun `register with invalid baseUrl returns failure without network request`() = runTest {
        val result = AuthApi.register(
            baseUrl = "ftp://example.com",
            request = RegisterRequest("user1", "user1@example.ru", "password123")
        )
        assertTrue(result is RegisterApiResult.Failure)
        val failure = result as RegisterApiResult.Failure
        assertEquals("Адрес сервера должен начинаться с http:// или https://", failure.message)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `login with invalid baseUrl returns failure without network request`() = runTest {
        val result = AuthApi.login(
            baseUrl = "invalid-url",
            request = LoginApiRequest("user1", "password123")
        )
        assertTrue(result is LoginApiResult.Failure)
        val failure = result as LoginApiResult.Failure
        assertEquals("Адрес сервера должен начинаться с http:// или https://", failure.message)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `login with blank identifier returns failure immediately`() = runTest {
        val baseUrl = server.url("/").toString()
        val result = AuthApi.login(
            baseUrl = baseUrl,
            request = LoginApiRequest("   ", "password123")
        )
        assertTrue(result is LoginApiResult.Failure)
        val failure = result as LoginApiResult.Failure
        assertEquals("Укажите логин, почту или телефон.", failure.message)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `getCurrentUserName with invalid baseUrl returns failure`() = runTest {
        val result = AuthApi.getCurrentUserName(
            baseUrl = "ws://localhost:8080",
            jwt = "some.jwt.token"
        )
        assertTrue(result is CurrentUserApiResult.Failure)
        val failure = result as CurrentUserApiResult.Failure
        assertEquals("Адрес сервера должен начинаться с http:// или https://", failure.message)
    }

    @Test
    fun `getCurrentUserName with blank jwt returns failure`() = runTest {
        val baseUrl = server.url("/").toString()
        val result = AuthApi.getCurrentUserName(
            baseUrl = baseUrl,
            jwt = "   "
        )
        assertTrue(result is CurrentUserApiResult.Failure)
        val failure = result as CurrentUserApiResult.Failure
        assertEquals("Токен не найден. Выполните вход или регистрацию.", failure.message)
        assertEquals(0, server.requestCount)
    }

    // --- Блок 2: AuthApi.register ---

    @Test
    fun `register success returns jwt and sends expected json body`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"jwt": "eyJhbGciOiJIUzI1NiJ9.reg"}""")
        )
        val baseUrl = server.url("/").toString().trimEnd('/') + "///"

        val result = AuthApi.register(
            baseUrl = baseUrl,
            request = RegisterRequest(
                login = "sport_user",
                email = "sport@example.ru",
                password = "Password123!",
                phone = "+7 (999) 123-45-67"
            )
        )

        assertTrue(result is RegisterApiResult.Success)
        assertEquals("eyJhbGciOiJIUzI1NiJ9.reg", (result as RegisterApiResult.Success).jwt)

        val recorded = server.takeRequest()
        assertEquals("/api/auth/register", recorded.path)
        assertEquals("POST", recorded.method)
        assertEquals("application/json; charset=utf-8", recorded.getHeader("Content-Type"))

        val bodyJson = JSONObject(recorded.body.readUtf8())
        assertEquals("sport_user", bodyJson.getString("login"))
        assertEquals("sport@example.ru", bodyJson.getString("email"))
        assertEquals("Password123!", bodyJson.getString("password"))
        assertEquals("+7 (999) 123-45-67", bodyJson.getString("phone"))
    }

    @Test
    fun `register omits phone field when phone is null or blank`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody("""{"jwt": "token_no_phone"}""")
        )

        val result = AuthApi.register(
            baseUrl = server.url("/").toString(),
            request = RegisterRequest(
                login = "user_no_phone",
                email = "np@example.ru",
                password = "Password123!",
                phone = "   "
            )
        )

        assertTrue(result is RegisterApiResult.Success)
        val recorded = server.takeRequest()
        val bodyJson = JSONObject(recorded.body.readUtf8())
        assertTrue(!bodyJson.has("phone"))
    }

    @Test
    fun `register 200 without jwt returns failure`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"jwt": ""}""")
        )

        val result = AuthApi.register(
            baseUrl = server.url("/").toString(),
            request = RegisterRequest("u", "u@example.ru", "password123")
        )

        assertTrue(result is RegisterApiResult.Failure)
        assertEquals("Сервер не вернул JWT токен.", (result as RegisterApiResult.Failure).message)
    }

    @Test
    fun `register 400 with field errors parses errors in lowercase keys`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(
                    """
                    {
                        "title": "One or more validation errors occurred.",
                        "errors": {
                            "Login": ["Логин уже занят"],
                            "Email": ["Некорректный формат почты", "Почта уже используется"]
                        }
                    }
                    """.trimIndent()
                )
        )

        val result = AuthApi.register(
            baseUrl = server.url("/").toString(),
            request = RegisterRequest("taken", "bad@example.ru", "password123")
        )

        assertTrue(result is RegisterApiResult.Failure)
        val failure = result as RegisterApiResult.Failure
        assertEquals("Проверьте введённые данные.", failure.message)
        assertEquals(listOf("Логин уже занят"), failure.fieldErrors["login"])
        assertEquals(
            listOf("Некорректный формат почты", "Почта уже используется"),
            failure.fieldErrors["email"]
        )
    }

    @Test
    fun `register 401 and 500 return appropriate localized messages`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))
        val res401 = AuthApi.register(
            baseUrl = server.url("/").toString(),
            request = RegisterRequest("u", "u@example.ru", "password123")
        )
        assertTrue(res401 is RegisterApiResult.Failure)
        assertEquals("Ошибка авторизации (401).", (res401 as RegisterApiResult.Failure).message)

        server.enqueue(MockResponse().setResponseCode(503).setBody("{}"))
        val res503 = AuthApi.register(
            baseUrl = server.url("/").toString(),
            request = RegisterRequest("u", "u@example.ru", "password123")
        )
        assertTrue(res503 is RegisterApiResult.Failure)
        assertEquals("Внутренняя ошибка сервера (503).", (res503 as RegisterApiResult.Failure).message)
    }

    // --- Блок 3: AuthApi.login и определение типа идентификатора ---

    @Test
    fun `login sends email field when identifier contains at sign`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"jwt": "jwt_email"}"""))

        val result = AuthApi.login(
            baseUrl = server.url("/").toString(),
            request = LoginApiRequest("  athlete@sport.ru  ", "secret123")
        )

        assertTrue(result is LoginApiResult.Success)
        assertEquals("jwt_email", (result as LoginApiResult.Success).jwt)

        val recorded = server.takeRequest()
        assertEquals("/api/auth/login", recorded.path)
        val body = JSONObject(recorded.body.readUtf8())
        assertEquals("athlete@sport.ru", body.getString("email"))
        assertTrue(!body.has("login"))
        assertTrue(!body.has("phone"))
    }

    @Test
    fun `login sends phone field when identifier starts with plus or has 10 digits`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"jwt": "jwt_phone_1"}"""))
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"jwt": "jwt_phone_2"}"""))

        AuthApi.login(
            baseUrl = server.url("/").toString(),
            request = LoginApiRequest("+7 (999) 111-22-33", "secret123")
        )
        val body1 = JSONObject(server.takeRequest().body.readUtf8())
        assertEquals("+7 (999) 111-22-33", body1.getString("phone"))
        assertTrue(!body1.has("login"))

        AuthApi.login(
            baseUrl = server.url("/").toString(),
            request = LoginApiRequest("8 (999) 111-22-33", "secret123")
        )
        val body2 = JSONObject(server.takeRequest().body.readUtf8())
        assertEquals("8 (999) 111-22-33", body2.getString("phone"))
        assertTrue(!body2.has("login"))
    }

    @Test
    fun `login sends login field when identifier is plain username`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"jwt": "jwt_login"}"""))

        val result = AuthApi.login(
            baseUrl = server.url("/").toString(),
            request = LoginApiRequest("champion_99", "secret123")
        )

        assertTrue(result is LoginApiResult.Success)
        val body = JSONObject(server.takeRequest().body.readUtf8())
        assertEquals("champion_99", body.getString("login"))
        assertTrue(!body.has("email"))
        assertTrue(!body.has("phone"))
    }

    @Test
    fun `login 401 returns invalid credentials message`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"title": "Unauthorized"}"""))

        val result = AuthApi.login(
            baseUrl = server.url("/").toString(),
            request = LoginApiRequest("champion_99", "wrong_pass")
        )

        assertTrue(result is LoginApiResult.Failure)
        assertEquals("Неверный логин или пароль.", (result as LoginApiResult.Failure).message)
    }

    // --- Блок 4: AuthApi.getCurrentUserName ---

    @Test
    fun `getCurrentUserName sends Bearer header and parses direct or nested user name`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"nickname": "IronMan"}""")
        )
        val resDirect = AuthApi.getCurrentUserName(server.url("/").toString(), "my.jwt.token")
        assertTrue(resDirect is CurrentUserApiResult.Success)
        assertEquals("IronMan", (resDirect as CurrentUserApiResult.Success).userName)

        val req = server.takeRequest()
        assertEquals("/api/users/me", req.path)
        assertEquals("GET", req.method)
        assertEquals("Bearer my.jwt.token", req.getHeader("Authorization"))

        // Проверка вложенного объекта user.displayName
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"user": {"displayName": "NestedHero"}}""")
        )
        val resNested = AuthApi.getCurrentUserName(server.url("/").toString(), "my.jwt.token")
        assertTrue(resNested is CurrentUserApiResult.Success)
        assertEquals("NestedHero", (resNested as CurrentUserApiResult.Success).userName)
    }

    @Test
    fun `getCurrentUserName parses plain text response when body is not json`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("PlainTextAthlete")
        )

        val result = AuthApi.getCurrentUserName(server.url("/").toString(), "my.jwt.token")
        assertTrue(result is CurrentUserApiResult.Success)
        assertEquals("PlainTextAthlete", (result as CurrentUserApiResult.Success).userName)
    }

    @Test
    fun `getCurrentUserName handles 401 and 404 error statuses`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody(""))
        val res401 = AuthApi.getCurrentUserName(server.url("/").toString(), "expired.jwt")
        assertTrue(res401 is CurrentUserApiResult.Failure)
        assertEquals("Сессия недействительна или токен истёк (401).", (res401 as CurrentUserApiResult.Failure).message)

        server.enqueue(MockResponse().setResponseCode(404).setBody(""))
        val res404 = AuthApi.getCurrentUserName(server.url("/").toString(), "valid.jwt")
        assertTrue(res404 is CurrentUserApiResult.Failure)
        assertEquals("Эндпоинт /api/users/me не найден на сервере (404).", (res404 as CurrentUserApiResult.Failure).message)
    }

    // --- Блок 5: parseFieldErrors ---

    @Test
    fun `parseFieldErrors ignores empty arrays and blank messages`() {
        val json = JSONObject(
            """
            {
                "Password": ["Слишком короткий пароль", "   "],
                "EmptyField": []
            }
            """.trimIndent()
        )
        val parsed = AuthApi.parseFieldErrors(json)
        assertEquals(1, parsed.size)
        assertEquals(listOf("Слишком короткий пароль"), parsed["password"])
    }
}
