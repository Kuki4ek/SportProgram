package com.sportakus.auth.data.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Юнит-тесты сетевого слоя [AuthApi] поверх MockWebServer.
 *
 * Контракт аутентификации (по коду AuthApi):
 *  - POST /api/auth/register {login, email, emailCode, password, phone?} -> {refreshToken};
 *  - POST /api/auth/login {email|phone|login, password} -> {refreshToken};
 *  - затем клиент сам обменивает refreshToken через POST /api/auth/refresh -> {jwt, refreshToken}.
 *
 * AuthApi использует блокирующий java.net.HttpURLConnection, поэтому все проверки
 * синхронные и не требуют kotlinx-coroutines-test.
 */
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

    private fun baseUrl(): String = server.url("/").toString().trimEnd('/')

    private fun enqueue(status: Int, body: String? = null) {
        val response = MockResponse().setResponseCode(status)
        if (body != null) response.setBody(body)
        server.enqueue(response)
    }

    private fun registration(
        login: String = "sportuser",
        email: String = "sportuser@example.com",
        password: String = "StrongPass1!",
        emailCode: String = "123456",
        phone: String? = null
    ): RegisterRequest = RegisterRequest(
        login = login,
        email = email,
        password = password,
        emailCode = emailCode,
        phone = phone
    )

    // ------------------------------------------------------------------
    // Проверки адреса сервера и обязательных полей (сеть не используется).
    // ------------------------------------------------------------------

    @Test
    fun `register отклоняет не-http адрес без запроса в сеть`() {
        val result = AuthApi.register("ftp://example.com", registration())

        val failure = result as? RegisterApiResult.Failure
        checkNotNull(failure) { "Ожидался RegisterApiResult.Failure, получено: $result" }
        assertNull(failure.httpStatus)
        assertEquals("Адрес сервера должен начинаться с http:// или https://.", failure.message)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `login отклоняет не-http адрес без запроса в сеть`() {
        val result = AuthApi.login("ftp://example.com", LoginApiRequest("sportuser", "StrongPass1!"))

        val failure = result as? LoginApiResult.Failure
        checkNotNull(failure) { "Ожидался LoginApiResult.Failure, получено: $result" }
        assertNull(failure.httpStatus)
        assertEquals("Адрес сервера должен начинаться с http:// или https://.", failure.message)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `login отклоняет пустой идентификатор без запроса в сеть`() {
        val result = AuthApi.login(baseUrl(), LoginApiRequest("   ", "StrongPass1!"))

        val failure = result as? LoginApiResult.Failure
        checkNotNull(failure)
        assertNull(failure.httpStatus)
        assertEquals("Введите логин, почту или телефон.", failure.message)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `register проверяет обязательные поля до запроса в сеть`() {
        val blankLogin = AuthApi.register(baseUrl(), registration(login = "  ")) as? RegisterApiResult.Failure
        val blankEmail = AuthApi.register(baseUrl(), registration(email = "")) as? RegisterApiResult.Failure
        val shortPassword = AuthApi.register(baseUrl(), registration(password = "1234567")) as? RegisterApiResult.Failure
        val blankEmailCode = AuthApi.register(baseUrl(), registration(emailCode = " ")) as? RegisterApiResult.Failure

        checkNotNull(blankLogin)
        checkNotNull(blankEmail)
        checkNotNull(shortPassword)
        checkNotNull(blankEmailCode)

        assertEquals("Введите логин.", blankLogin.message)
        assertEquals("Введите почту.", blankEmail.message)
        assertEquals("Пароль должен содержать не менее 8 символов.", shortPassword.message)
        assertEquals("Введите код подтверждения почты.", blankEmailCode.message)
        assertEquals(0, server.requestCount)
    }

    // ------------------------------------------------------------------
    // POST /api/auth/register
    // ------------------------------------------------------------------

    @Test
    fun `register обменивает refreshToken на jwt через refresh`() {
        enqueue(200, """{"refreshToken":"reg-refresh-1"}""")
        enqueue(200, """{"jwt":"jwt-from-refresh","refreshToken":"rotated-refresh-1"}""")

        val result = AuthApi.register(baseUrl(), registration())

        val success = result as? RegisterApiResult.Success
        checkNotNull(success) { "Ожидался RegisterApiResult.Success, получено: $result" }
        // jwt приходит из ответа /api/auth/refresh, а не напрямую из /register.
        assertEquals("jwt-from-refresh", success.jwt)
        assertEquals("rotated-refresh-1", success.refreshToken)
        assertEquals(200, success.httpStatus)

        assertEquals(2, server.requestCount)
        val registerCall = server.takeRequest()
        val refreshCall = server.takeRequest()
        assertEquals("/api/auth/register", registerCall.path)
        assertEquals("POST", registerCall.method)
        val registerBody = JSONObject(registerCall.body.readUtf8())
        assertEquals("sportuser", registerBody.getString("login"))
        assertEquals("sportuser@example.com", registerBody.getString("email"))
        assertEquals("123456", registerBody.getString("emailCode"))
        assertEquals("StrongPass1!", registerBody.getString("password"))
        assertFalse("phone не должен попадать в payload, когда он null", registerBody.has("phone"))

        assertEquals("/api/auth/refresh", refreshCall.path)
        assertEquals(
            "reg-refresh-1",
            JSONObject(refreshCall.body.readUtf8()).getString("refreshToken")
        )
    }

    @Test
    fun `register опускает blank-телефон в payload`() {
        enqueue(200, """{"refreshToken":"reg-refresh-2"}""")
        enqueue(200, """{"jwt":"jwt-2","refreshToken":"rotated-2"}""")

        val result = AuthApi.register(baseUrl(), registration(phone = "   "))

        assertTrue(result is RegisterApiResult.Success)
        val registerBody = JSONObject(server.takeRequest().body.readUtf8())
        assertFalse(registerBody.has("phone"))
    }

    @Test
    fun `register отправляет телефон в нормализованном виде`() {
        enqueue(200, """{"refreshToken":"reg-refresh-3"}""")
        enqueue(200, """{"jwt":"jwt-3","refreshToken":"rotated-3"}""")

        val result = AuthApi.register(baseUrl(), registration(phone = " +79991234567 "))

        assertTrue(result is RegisterApiResult.Success)
        val registerBody = JSONObject(server.takeRequest().body.readUtf8())
        assertEquals("+79991234567", registerBody.getString("phone"))
    }

    @Test
    fun `register без refreshToken в ответе помечает аккаунт созданным`() {
        enqueue(200, """{"status":"ok"}""")

        val result = AuthApi.register(baseUrl(), registration())

        val failure = result as? RegisterApiResult.Failure
        checkNotNull(failure)
        assertEquals(200, failure.httpStatus)
        assertEquals(true, failure.accountCreated)
        assertEquals(
            "Сервер создал аккаунт, но ответ /api/auth/register не содержит refreshToken. " +
                "Нужен refreshToken в успешном ответе backend.",
            failure.message
        )
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `register при ошибке refresh сохраняет признак созданного аккаунта`() {
        enqueue(200, """{"refreshToken":"reg-refresh-4"}""")
        enqueue(500)

        val result = AuthApi.register(baseUrl(), registration())

        val failure = result as? RegisterApiResult.Failure
        checkNotNull(failure)
        assertEquals(500, failure.httpStatus)
        assertEquals(true, failure.accountCreated)
        assertEquals(
            "Аккаунт создан, но получить JWT через /api/auth/refresh не удалось. " +
                "Ошибка сервера (500). Проверь журнал C# API. " +
                "Попробуй войти позже; повторно регистрироваться не нужно.",
            failure.message
        )
    }

    @Test
    fun `register склеивает сообщения fieldErrors из ответа 400`() {
        enqueue(
            400,
            """{"errors":{"Login":["Логин уже занят"],"Email":["Некорректный формат почты","Почта уже используется"]}}"""
        )

        val result = AuthApi.register(baseUrl(), registration())

        val failure = result as? RegisterApiResult.Failure
        checkNotNull(failure)
        assertEquals(400, failure.httpStatus)
        assertEquals(
            "Логин уже занят\nНекорректный формат почты\nПочта уже используется",
            failure.message
        )
        // Ключи fieldErrors сохраняются как есть, без приведения к нижнему регистру.
        assertEquals(listOf("Логин уже занят"), failure.fieldErrors["Login"])
        assertEquals(
            listOf("Некорректный формат почты", "Почта уже используется"),
            failure.fieldErrors["Email"]
        )
    }

    @Test
    fun `register транслирует 401 в сообщение о недействительной сессии`() {
        enqueue(401)

        val result = AuthApi.register(baseUrl(), registration())

        val failure = result as? RegisterApiResult.Failure
        checkNotNull(failure)
        assertEquals(401, failure.httpStatus)
        assertEquals("Сессия недействительна. Необходимо войти в приложение снова.", failure.message)
    }

    @Test
    fun `register транслирует 503 в сообщение об ошибке сервера`() {
        enqueue(503)

        val result = AuthApi.register(baseUrl(), registration())

        val failure = result as? RegisterApiResult.Failure
        checkNotNull(failure)
        assertEquals(503, failure.httpStatus)
        assertEquals("Ошибка сервера (503). Проверь журнал C# API.", failure.message)
    }

    // ------------------------------------------------------------------
    // POST /api/auth/login
    // ------------------------------------------------------------------

    @Test
    fun `login отправляет почтовый идентификатор ключом email`() {
        enqueue(200, """{"refreshToken":"login-refresh-1"}""")
        enqueue(200, """{"jwt":"login-jwt-1","refreshToken":"login-rotated-1"}""")

        val result = AuthApi.login(baseUrl(), LoginApiRequest("  User@Example.com  ", "StrongPass1!"))

        val success = result as? LoginApiResult.Success
        checkNotNull(success) { "Ожидался LoginApiResult.Success, получено: $result" }
        assertEquals("login-jwt-1", success.jwt)
        assertEquals("login-rotated-1", success.refreshToken)

        val loginCall = server.takeRequest()
        assertEquals("/api/auth/login", loginCall.path)
        val loginBody = JSONObject(loginCall.body.readUtf8())
        assertEquals("User@Example.com", loginBody.getString("email"))
        assertEquals("StrongPass1!", loginBody.getString("password"))
        assertFalse(loginBody.has("login"))
        assertFalse(loginBody.has("phone"))
    }

    @Test
    fun `login отправляет идентификатор с плюсом ключом phone`() {
        enqueue(200, """{"refreshToken":"login-refresh-2"}""")
        enqueue(200, """{"jwt":"login-jwt-2","refreshToken":"login-rotated-2"}""")

        val result = AuthApi.login(baseUrl(), LoginApiRequest("+7 999 123-45-67", "StrongPass1!"))

        assertTrue(result is LoginApiResult.Success)
        val loginBody = JSONObject(server.takeRequest().body.readUtf8())
        // Идентификатор trim-ится, но не переформатируется на уровне API.
        assertEquals("+7 999 123-45-67", loginBody.getString("phone"))
        assertFalse(loginBody.has("login"))
        assertFalse(loginBody.has("email"))
    }

    @Test
    fun `login отправляет длинный цифровой идентификатор ключом phone`() {
        enqueue(200, """{"refreshToken":"login-refresh-3"}""")
        enqueue(200, """{"jwt":"login-jwt-3","refreshToken":"login-rotated-3"}""")

        val result = AuthApi.login(baseUrl(), LoginApiRequest("89991234567", "StrongPass1!"))

        assertTrue(result is LoginApiResult.Success)
        val loginBody = JSONObject(server.takeRequest().body.readUtf8())
        assertEquals("89991234567", loginBody.getString("phone"))
    }

    @Test
    fun `login отправляет обычный логин ключом login`() {
        enqueue(200, """{"refreshToken":"login-refresh-4"}""")
        enqueue(200, """{"jwt":"login-jwt-4","refreshToken":"login-rotated-4"}""")

        val result = AuthApi.login(baseUrl(), LoginApiRequest("ironman", "StrongPass1!"))

        assertTrue(result is LoginApiResult.Success)
        val loginBody = JSONObject(server.takeRequest().body.readUtf8())
        assertEquals("ironman", loginBody.getString("login"))
        assertFalse(loginBody.has("email"))
        assertFalse(loginBody.has("phone"))
    }

    @Test
    fun `login без refreshToken в ответе сообщает о нарушении контракта`() {
        enqueue(200, """{"status":"ok"}""")

        val result = AuthApi.login(baseUrl(), LoginApiRequest("ironman", "StrongPass1!"))

        val failure = result as? LoginApiResult.Failure
        checkNotNull(failure)
        assertEquals(200, failure.httpStatus)
        assertEquals(
            "Сервер подтвердил вход, но ответ /api/auth/login не содержит refreshToken. Проверь контракт backend.",
            failure.message
        )
    }

    @Test
    fun `login транслирует 401 в сообщение о недействительной сессии`() {
        enqueue(401)

        val result = AuthApi.login(baseUrl(), LoginApiRequest("ironman", "StrongPass1!"))

        val failure = result as? LoginApiResult.Failure
        checkNotNull(failure)
        assertEquals(401, failure.httpStatus)
        assertEquals("Сессия недействительна. Необходимо войти в приложение снова.", failure.message)
    }

    // ------------------------------------------------------------------
    // POST /api/auth/refresh
    // ------------------------------------------------------------------

    @Test
    fun `refreshAccessToken требует jwt и refreshToken в ответе`() {
        enqueue(200, """{"jwt":"only-jwt"}""")

        val result = AuthApi.refreshAccessToken(baseUrl(), "some-refresh-token")

        val failure = result as? RefreshTokenApiResult.Failure
        checkNotNull(failure)
        assertEquals(200, failure.httpStatus)
        assertEquals(
            "POST /api/auth/refresh вернул успешный статус, но в JSON отсутствует поле refreshToken. " +
                "По контракту backend должен возвращать jwt и новый refreshToken.",
            failure.message
        )
    }

    @Test
    fun `refreshAccessToken разбирает обновлённые токены`() {
        enqueue(200, """{"jwt":"access-1","refreshToken":"refresh-2"}""")

        val result = AuthApi.refreshAccessToken(baseUrl(), "refresh-1")

        val success = result as? RefreshTokenApiResult.Success
        checkNotNull(success)
        assertEquals("access-1", success.accessToken)
        assertEquals("refresh-2", success.refreshToken)
        assertEquals(200, success.httpStatus)
    }

    // ------------------------------------------------------------------
    // GET /api/users/me
    // ------------------------------------------------------------------

    @Test
    fun `getCurrentUserName отклоняет не-http адрес без запроса в сеть`() {
        val result = AuthApi.getCurrentUserName("ws://example.com", "jwt")

        val failure = result as? CurrentUserApiResult.Failure
        checkNotNull(failure)
        assertNull(failure.httpStatus)
        assertEquals("Адрес сервера должен начинаться с http:// или https://.", failure.message)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `getCurrentUserName читает профиль с Bearer jwt`() {
        enqueue(200, """{"login":"iron","nickname":"IronMan"}""")

        val result = AuthApi.getCurrentUserName(baseUrl(), "profile-jwt", null)

        val success = result as? CurrentUserApiResult.Success
        checkNotNull(success) { "Ожидался CurrentUserApiResult.Success, получено: $result" }
        assertEquals("IronMan", success.displayName)
        assertEquals("iron", success.profile.login)
        assertEquals(200, success.httpStatus)

        val request = server.takeRequest()
        assertEquals("/api/users/me", request.path)
        assertEquals("GET", request.method)
        assertEquals("Bearer profile-jwt", request.getHeader("Authorization"))
    }

    @Test
    fun `getCurrentUserName подставляет Спортсмен для неизвестных полей профиля`() {
        // displayName не входит в алиасы парсера (nickname/nickName/nick_name, login, email),
        // поэтому вложенный user.displayName игнорируется. Тест фиксирует фактическое
        // поведение; вопрос расширенных алиасов вынесен в FOR_LEAD_DEVELOPER.md.
        enqueue(200, """{"user":{"displayName":"NestedHero"}}""")

        val result = AuthApi.getCurrentUserName(baseUrl(), "profile-jwt", null)

        val success = result as? CurrentUserApiResult.Success
        checkNotNull(success)
        assertEquals("Спортсмен", success.displayName)
    }

    @Test
    fun `getCurrentUserName сообщает о некорректном JSON профиля`() {
        enqueue(200, "Not JSON at all")

        val result = AuthApi.getCurrentUserName(baseUrl(), "profile-jwt", null)

        val failure = result as? CurrentUserApiResult.Failure
        checkNotNull(failure)
        assertEquals(200, failure.httpStatus)
        assertEquals("Сервер вернул пустой или некорректный JSON профиля.", failure.message)
    }

    @Test
    fun `getCurrentUserName транслирует 401 в сообщение о недействительной сессии`() {
        enqueue(401)

        val result = AuthApi.getCurrentUserName(baseUrl(), "profile-jwt", null)

        val failure = result as? CurrentUserApiResult.Failure
        checkNotNull(failure)
        assertEquals(401, failure.httpStatus)
        assertEquals("Сессия недействительна. Необходимо войти в приложение снова.", failure.message)
    }

    @Test
    fun `getCurrentUserName транслирует 404 в сообщение о недоступном endpoint`() {
        enqueue(404)

        val result = AuthApi.getCurrentUserName(baseUrl(), "profile-jwt", null)

        val failure = result as? CurrentUserApiResult.Failure
        checkNotNull(failure)
        assertEquals(404, failure.httpStatus)
        assertEquals("Запрашиваемая запись или endpoint не найдены (HTTP 404).", failure.message)
    }

    @Test
    fun `getCurrentUserName отправляет запрос даже с пустым jwt`() {
        // Документирует фактическое поведение: пустой jwt не проверяется, запрос уходит
        // без заголовка Authorization (request() пропускает blank bearer-токен).
        // Вопрос guard-проверки вынесен в FOR_LEAD_DEVELOPER.md.
        enqueue(401)

        val result = AuthApi.getCurrentUserName(baseUrl(), "   ", null)

        val failure = result as? CurrentUserApiResult.Failure
        checkNotNull(failure)
        assertEquals(401, failure.httpStatus)
        assertEquals(1, server.requestCount)
        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `getCurrentUserName обновляет сессию после 401 и повторяет запрос`() {
        enqueue(401) // первый GET /api/users/me
        enqueue(200, """{"jwt":"renewed-jwt","refreshToken":"renewed-refresh"}""") // POST /api/auth/refresh
        enqueue(200, """{"nickname":"IronMan"}""") // повторный GET /api/users/me

        val result = AuthApi.getCurrentUserName(baseUrl(), "stale-jwt", "stored-refresh")

        val success = result as? CurrentUserApiResult.Success
        checkNotNull(success) { "Ожидался CurrentUserApiResult.Success, получено: $result" }
        assertEquals("IronMan", success.displayName)
        assertEquals("renewed-jwt", success.renewedAccessToken)
        assertEquals("renewed-refresh", success.renewedRefreshToken)

        assertEquals(3, server.requestCount)
        val firstCall = server.takeRequest()
        val refreshCall = server.takeRequest()
        val retryCall = server.takeRequest()
        assertEquals("/api/users/me", firstCall.path)
        assertEquals("Bearer stale-jwt", firstCall.getHeader("Authorization"))
        assertEquals("/api/auth/refresh", refreshCall.path)
        assertEquals(
            "stored-refresh",
            JSONObject(refreshCall.body.readUtf8()).getString("refreshToken")
        )
        assertEquals("/api/users/me", retryCall.path)
        assertEquals("Bearer renewed-jwt", retryCall.getHeader("Authorization"))
    }
}
