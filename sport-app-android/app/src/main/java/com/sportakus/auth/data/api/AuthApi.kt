package com.sportakus.auth.data.api

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/** Exact payload for POST /api/auth/register. */
data class RegisterRequest(
    val login: String,
    val email: String,
    val password: String,
    val emailCode: String,
    val phone: String? = null
)

sealed class RegisterApiResult {
    data class Success(
        /** JWT obtained by the follow-up POST /api/auth/refresh. */
        val jwt: String,
        /** Rotated refresh token returned by /api/auth/refresh. */
        val refreshToken: String,
        val httpStatus: Int
    ) : RegisterApiResult()

    data class Failure(
        val httpStatus: Int?,
        val message: String,
        val fieldErrors: Map<String, List<String>> = emptyMap(),
        /** True means /register returned 2xx and the account may already exist. */
        val accountCreated: Boolean = false
    ) : RegisterApiResult()
}

sealed class EmailConfirmationApiResult {
    data class Success(val httpStatus: Int) : EmailConfirmationApiResult()
    data class Failure(
        val httpStatus: Int?,
        val message: String,
        val fieldErrors: Map<String, List<String>> = emptyMap()
    ) : EmailConfirmationApiResult()
}

data class UserProfile(
    val login: String = "",
    val email: String = "",
    val phone: String = "",
    val fullName: String = "",
    val birthDate: String = "",
    val height: String = "",
    val weight: String = "",
    val gender: String = "",
    val totalWorkouts: Int = 0,
    val sportCoins: Int = 0,
    val totalMinutes: Int = 0,
    val favoriteCount: Int = 0,
    val activityValues: List<Int> = emptyList()
) {
    val displayName: String
        get() = fullName.ifBlank { login.ifBlank { email.substringBefore('@') } }.ifBlank { "Спортсмен" }
}

sealed class CurrentUserApiResult {
    data class Success(
        val profile: UserProfile,
        val httpStatus: Int,
        val renewedAccessToken: String? = null,
        val renewedRefreshToken: String? = null
    ) : CurrentUserApiResult() {
        val displayName: String get() = profile.displayName
    }

    data class Failure(
        val httpStatus: Int?,
        val message: String,
        val renewedAccessToken: String? = null,
        val renewedRefreshToken: String? = null
    ) : CurrentUserApiResult()
}

data class LoginApiRequest(val identifier: String, val password: String)

sealed class LoginApiResult {
    data class Success(
        /** JWT obtained by the follow-up POST /api/auth/refresh. */
        val jwt: String,
        val refreshToken: String,
        val httpStatus: Int
    ) : LoginApiResult()

    data class Failure(
        val httpStatus: Int?,
        val message: String,
        val fieldErrors: Map<String, List<String>> = emptyMap()
    ) : LoginApiResult()
}

sealed class PasswordRecoveryApiResult {
    data class Success(
        val httpStatus: Int,
        val codeToken: String? = null,
        val resetToken: String? = null
    ) : PasswordRecoveryApiResult()

    data class Failure(val httpStatus: Int?, val message: String) : PasswordRecoveryApiResult()
}

sealed class AuthActionApiResult {
    data class Success(
        val httpStatus: Int,
        val renewedAccessToken: String? = null,
        val renewedRefreshToken: String? = null
    ) : AuthActionApiResult()

    data class Failure(
        val httpStatus: Int?,
        val message: String,
        val renewedAccessToken: String? = null,
        val renewedRefreshToken: String? = null
    ) : AuthActionApiResult()
}

sealed class RefreshTokenApiResult {
    data class Success(val accessToken: String, val refreshToken: String, val httpStatus: Int) : RefreshTokenApiResult()
    data class Failure(val httpStatus: Int?, val message: String) : RefreshTokenApiResult()
}

/** API routes explicitly named in the backend specification dated 02.11. */
object AuthEndpoints {
    const val EMAIL_CONFIRM = "/api/auth/email-confirm"
    const val REGISTER = "/api/auth/register"
    const val LOGIN = "/api/auth/login"
    const val REQUEST_PASSWORD_RESET = "/api/auth/forgot-password/request"
    const val VERIFY_PASSWORD_RESET_CODE = "/api/auth/forgot-password/code"
    const val RESET_PASSWORD = "/api/auth/forgot-password/reset"
    const val REFRESH = "/api/auth/refresh"
    const val LOGOUT = "/api/auth/logout"
    const val CURRENT_USER = "/api/users/me"
    const val UPDATE_CURRENT_USER = "/api/users/me"
}

/**
 * HTTP client and DTO mapping for the C# backend. Authentication contract:
 * register/login -> refreshToken -> POST /refresh -> jwt + rotated refreshToken.
 */
object AuthApi {
    private data class HttpResult(
        val status: Int,
        val body: String,
        val authorizationHeader: String? = null
    ) {
        fun json(): JSONObject? = body.takeIf { it.isNotBlank() }
            ?.let { runCatching { JSONObject(it) }.getOrNull() }
    }

    private data class AuthorizedHttpResult(
        val response: HttpResult,
        val renewedAccessToken: String? = null,
        val renewedRefreshToken: String? = null,
        val refreshError: String? = null
    )

    private val refreshLock = Any()
    @Volatile private var lastRefreshSourceToken: String? = null
    @Volatile private var lastRefreshResult: RefreshTokenApiResult.Success? = null
    @Volatile private var lastRefreshAtMillis: Long = 0L

    private fun request(
        baseUrl: String,
        path: String,
        method: String = "POST",
        payload: JSONObject? = null,
        bearerToken: String? = null
    ): HttpResult {
        val normalized = baseUrl.trim().trimEnd('/')
        require(
            normalized.startsWith("http://", ignoreCase = true) ||
                normalized.startsWith("https://", ignoreCase = true)
        ) { "Адрес сервера должен начинаться с http:// или https://." }

        val connection = URL("$normalized$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 12_000
            connection.readTimeout = 15_000
            connection.useCaches = false
            connection.setRequestProperty("Accept", "application/json")
            if (payload != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            if (!bearerToken.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $bearerToken")
            }
            if (payload != null) {
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val responseStream = if (status >= 400) connection.errorStream else connection.inputStream
            val body = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            return HttpResult(status, body, connection.getHeaderField("Authorization"))
        } finally {
            connection.disconnect()
        }
    }

    /** Searches root and common API envelopes (data/result/tokens/auth/user) case-insensitively. */
    private fun findString(root: JSONObject?, acceptedNames: Set<String>): String? {
        if (root == null) return null
        fun visit(obj: JSONObject, depth: Int): String? {
            if (depth > 5) return null
            val keys = obj.keys().asSequence().toList()
            for (accepted in acceptedNames) {
                val key = keys.firstOrNull { it.equals(accepted, ignoreCase = true) } ?: continue
                val value = obj.opt(key)
                if (value is String && value.isNotBlank() && !value.equals("null", true)) return value.trim()
            }
            for (key in keys) {
                when (val value = obj.opt(key)) {
                    is JSONObject -> visit(value, depth + 1)?.let { return it }
                    is JSONArray -> for (index in 0 until value.length()) {
                        val child = value.opt(index)
                        if (child is JSONObject) visit(child, depth + 1)?.let { return it }
                    }
                }
            }
            return null
        }
        return visit(root, 0)
    }

    private fun accessToken(result: HttpResult): String? {
        val bodyToken = findString(
            result.json(),
            linkedSetOf("jwt", "accessToken", "access_token", "token")
        )
        val header = result.authorizationHeader?.trim().orEmpty()
        val headerToken = if (header.startsWith("Bearer ", ignoreCase = true)) {
            header.substringAfter(' ').trim().takeIf { it.isNotEmpty() }
        } else null
        return bodyToken ?: headerToken
    }

    private fun refreshToken(result: HttpResult): String? = findString(
        result.json(),
        setOf("refreshToken", "refresh_token")
    )

    private fun errorFromJson(result: HttpResult): String? {
        val json = result.json() ?: return null
        return findString(json, setOf("detail", "message", "title", "error", "error_status"))
            ?.takeIf { it.isNotBlank() }
    }

    private fun serverMessage(result: HttpResult, fallback: String): String =
        errorFromJson(result) ?: when (result.status) {
            400 -> "$fallback. Проверь переданные данные (HTTP 400)."
            401 -> "Сессия недействительна. Необходимо войти в приложение снова."
            404 -> "Запрашиваемый endpoint не найден (HTTP 404). Проверь маршрут C# API."
            409 -> "$fallback: такая запись уже существует (HTTP 409)."
            422 -> "$fallback: сервер отклонил формат данных (HTTP 422)."
            429 -> "Слишком много запросов. Подожди и попробуй ещё раз."
            in 500..599 -> "Ошибка сервера (${result.status}). Проверь журнал C# API."
            else -> "$fallback (HTTP ${result.status})."
        }

    private fun parseFieldErrors(errors: JSONObject?): Map<String, List<String>> {
        if (errors == null) return emptyMap()
        val parsed = linkedMapOf<String, List<String>>()
        val iterator = errors.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val value = errors.opt(key)
            val messages = when (value) {
                is JSONArray -> buildList {
                    for (index in 0 until value.length()) {
                        value.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                    }
                }
                is String -> listOf(value).filter { it.isNotBlank() }
                else -> emptyList()
            }
            if (messages.isNotEmpty()) parsed[key.lowercase()] = messages
        }
        return parsed
    }

    private fun fieldErrors(result: HttpResult): Map<String, List<String>> {
        val json = result.json() ?: return emptyMap()
        return parseFieldErrors(json.optJSONObject("errors") ?: json.optJSONObject("fieldErrors")
            ?: json.optJSONObject("field_errors") ?: json.optJSONObject("data")?.optJSONObject("errors"))
    }

    private fun networkErrorMessage(error: Exception): String = when (error) {
        is UnknownHostException -> "Не удалось найти сервер. Проверь адрес API и подключение к сети."
        is SocketTimeoutException -> "Сервер не ответил вовремя. Попробуй ещё раз."
        is java.net.ConnectException -> "Не удалось подключиться к серверу. Проверь порт, адрес и настройки брандмауэра."
        is IllegalArgumentException -> error.message ?: "Проверь адрес сервера."
        else -> "Ошибка запроса: ${error.localizedMessage ?: error.javaClass.simpleName}"
    }

    private fun recoveryIdentityKey(value: String): String = when (value.lowercase()) {
        "login", "nickname", "username" -> "login"
        "email", "почта" -> "email"
        "phone", "телефон" -> "phone"
        else -> throw IllegalArgumentException("Для восстановления укажи login, email или phone.")
    }

    /** POST /api/auth/email-confirm. */
    fun requestEmailConfirmation(baseUrl: String, login: String, email: String): EmailConfirmationApiResult = try {
        require(login.isNotBlank()) { "Введите имя пользователя." }
        require(email.isNotBlank()) { "Введите адрес электронной почты." }
        val result = request(
            baseUrl,
            AuthEndpoints.EMAIL_CONFIRM,
            payload = JSONObject().put("login", login.trim()).put("email", email.trim())
        )
        if (result.status in 200..299) EmailConfirmationApiResult.Success(result.status)
        else EmailConfirmationApiResult.Failure(result.status, serverMessage(result, "Сервер не смог отправить код подтверждения"), fieldErrors(result))
    } catch (error: Exception) {
        EmailConfirmationApiResult.Failure(null, networkErrorMessage(error))
    }

    /**
     * POST /register returns refreshToken only. The client immediately exchanges it at
     * POST /refresh and only reports success after a JWT is received.
     */
    fun register(baseUrl: String, registration: RegisterRequest): RegisterApiResult = try {
        require(registration.login.isNotBlank()) { "Введите логин." }
        require(registration.email.isNotBlank()) { "Введите почту." }
        require(registration.password.length >= 8) { "Пароль должен содержать не менее 8 символов." }
        require(registration.emailCode.isNotBlank()) { "Введите код подтверждения почты." }
        val payload = JSONObject().apply {
            put("login", registration.login.trim())
            put("email", registration.email.trim())
            put("emailCode", registration.emailCode.trim())
            put("password", registration.password)
            registration.phone?.trim()?.takeIf { it.isNotEmpty() }?.let { put("phone", it) }
        }
        val result = request(baseUrl, AuthEndpoints.REGISTER, payload = payload)
        if (result.status !in 200..299) {
            RegisterApiResult.Failure(result.status, serverMessage(result, "Сервер отклонил регистрацию"), fieldErrors(result))
        } else {
            val receivedRefresh = refreshToken(result)
            if (receivedRefresh.isNullOrBlank()) {
                RegisterApiResult.Failure(
                    result.status,
                    "Сервер создал аккаунт, но ответ /api/auth/register не содержит refreshToken. Нужен refreshToken в успешном ответе backend.",
                    accountCreated = true
                )
            } else {
                when (val refreshed = refreshAccessToken(baseUrl, receivedRefresh)) {
                    is RefreshTokenApiResult.Success -> RegisterApiResult.Success(
                        refreshed.accessToken,
                        refreshed.refreshToken,
                        result.status
                    )
                    is RefreshTokenApiResult.Failure -> RegisterApiResult.Failure(
                        refreshed.httpStatus,
                        "Аккаунт создан, но получить JWT через /api/auth/refresh не удалось. ${refreshed.message} Попробуй войти позже; повторно регистрироваться не нужно.",
                        accountCreated = true
                    )
                }
            }
        }
    } catch (error: Exception) {
        RegisterApiResult.Failure(null, networkErrorMessage(error))
    }

    /**
     * POST /login accepts exactly one identifier property and returns refreshToken.
     * The following refresh exchange returns the JWT needed by protected endpoints.
     */
    fun login(baseUrl: String, credentials: LoginApiRequest): LoginApiResult = try {
        val identifier = credentials.identifier.trim()
        require(identifier.isNotBlank()) { "Введите логин, почту или телефон." }
        require(credentials.password.length >= 8) { "Пароль должен содержать не менее 8 символов." }
        val key = when {
            identifier.contains('@') -> "email"
            identifier.startsWith("+") ||
                (identifier.all { it.isDigit() || it in " ()-" } && identifier.count(Char::isDigit) >= 10) -> "phone"
            else -> "login"
        }
        val result = request(
            baseUrl,
            AuthEndpoints.LOGIN,
            payload = JSONObject().put(key, identifier).put("password", credentials.password)
        )
        if (result.status !in 200..299) {
            LoginApiResult.Failure(result.status, serverMessage(result, "Не удалось войти"), fieldErrors(result))
        } else {
            val receivedRefresh = refreshToken(result)
            if (receivedRefresh.isNullOrBlank()) {
                LoginApiResult.Failure(
                    result.status,
                    "Сервер подтвердил вход, но ответ /api/auth/login не содержит refreshToken. Проверь контракт backend."
                )
            } else {
                when (val refreshed = refreshAccessToken(baseUrl, receivedRefresh)) {
                    is RefreshTokenApiResult.Success -> LoginApiResult.Success(
                        refreshed.accessToken,
                        refreshed.refreshToken,
                        result.status
                    )
                    is RefreshTokenApiResult.Failure -> LoginApiResult.Failure(
                        refreshed.httpStatus,
                        "Вход принят, но получить JWT через /api/auth/refresh не удалось. ${refreshed.message}"
                    )
                }
            }
        }
    } catch (error: Exception) {
        LoginApiResult.Failure(null, networkErrorMessage(error))
    }

    /** POST /forgot-password/request: exactly one of login/email/phone; response: codeToken. */
    fun requestPasswordReset(baseUrl: String, contactKind: String, identifier: String): PasswordRecoveryApiResult = try {
        require(identifier.isNotBlank()) { "Введите данные аккаунта." }
        val key = recoveryIdentityKey(contactKind)
        val result = request(
            baseUrl,
            AuthEndpoints.REQUEST_PASSWORD_RESET,
            payload = JSONObject().put(key, identifier.trim())
        )
        if (result.status in 200..299) {
            val codeToken = findString(result.json(), setOf("codeToken"))
            if (codeToken.isNullOrBlank()) {
                PasswordRecoveryApiResult.Failure(result.status, "Сервер отправил ответ без codeToken.")
            } else PasswordRecoveryApiResult.Success(result.status, codeToken = codeToken)
        } else PasswordRecoveryApiResult.Failure(result.status, serverMessage(result, "Не удалось отправить код восстановления"))
    } catch (error: Exception) {
        PasswordRecoveryApiResult.Failure(null, networkErrorMessage(error))
    }

    /** POST /forgot-password/code with identity + codeToken + emailCode. */
    fun verifyPasswordResetCode(
        baseUrl: String,
        contactKind: String,
        identifier: String,
        codeToken: String,
        emailCode: String
    ): PasswordRecoveryApiResult = try {
        require(identifier.isNotBlank()) { "Не найден логин, почта или телефон для восстановления." }
        require(codeToken.isNotBlank()) { "Сервер не вернул codeToken. Запроси код повторно." }
        require(emailCode.isNotBlank()) { "Введите код подтверждения." }
        val key = recoveryIdentityKey(contactKind)
        val result = request(
            baseUrl,
            AuthEndpoints.VERIFY_PASSWORD_RESET_CODE,
            payload = JSONObject()
                .put(key, identifier.trim())
                .put("codeToken", codeToken)
                .put("emailCode", emailCode.trim())
        )
        if (result.status in 200..299) {
            val resetToken = findString(result.json(), setOf("resetPasswordToken"))
            if (resetToken.isNullOrBlank()) {
                PasswordRecoveryApiResult.Failure(result.status, "Сервер не вернул resetPasswordToken после проверки кода.")
            } else PasswordRecoveryApiResult.Success(result.status, codeToken = codeToken, resetToken = resetToken)
        } else PasswordRecoveryApiResult.Failure(result.status, serverMessage(result, "Код подтверждения не принят"))
    } catch (error: Exception) {
        PasswordRecoveryApiResult.Failure(null, networkErrorMessage(error))
    }

    /** POST /forgot-password/reset with identity + resetPasswordToken + newPassword. */
    fun resetPassword(
        baseUrl: String,
        contactKind: String,
        identifier: String,
        resetPasswordToken: String,
        newPassword: String
    ): AuthActionApiResult = try {
        require(identifier.isNotBlank()) { "Не найден логин, почта или телефон для восстановления." }
        require(resetPasswordToken.isNotBlank()) { "Токен сброса пароля не найден. Проверь код ещё раз." }
        require(newPassword.length >= 8) { "Пароль должен содержать не менее 8 символов." }
        val key = recoveryIdentityKey(contactKind)
        val payload = JSONObject()
            .put(key, identifier.trim())
            .put("resetPasswordToken", resetPasswordToken)
            .put("newPassword", newPassword)
        val result = request(baseUrl, AuthEndpoints.RESET_PASSWORD, payload = payload)
        if (result.status in 200..299) AuthActionApiResult.Success(result.status)
        else AuthActionApiResult.Failure(result.status, serverMessage(result, "Не удалось изменить пароль"))
    } catch (error: Exception) {
        AuthActionApiResult.Failure(null, networkErrorMessage(error))
    }

    /**
     * POST /api/auth/refresh. Request: { refreshToken }. The documented response is
     * { jwt, refreshToken }; the old refresh token is retained only if the server omits rotation.
     */
    fun refreshAccessToken(baseUrl: String, refreshTokenValue: String): RefreshTokenApiResult = try {
        require(refreshTokenValue.isNotBlank()) { "Refresh-токен не найден." }
        val result = request(
            baseUrl,
            AuthEndpoints.REFRESH,
            payload = JSONObject().put("refreshToken", refreshTokenValue)
        )
        if (result.status in 200..299) {
            val jwt = accessToken(result)
            val rotatedRefresh = refreshToken(result)
            if (jwt.isNullOrBlank() || rotatedRefresh.isNullOrBlank()) {
                val missing = buildList {
                    if (jwt.isNullOrBlank()) add("jwt")
                    if (rotatedRefresh.isNullOrBlank()) add("refreshToken")
                }.joinToString(" и ")
                RefreshTokenApiResult.Failure(
                    result.status,
                    "POST /api/auth/refresh вернул успешный статус, но в JSON отсутствует поле $missing. По контракту backend должен возвращать jwt и новый refreshToken."
                )
            } else {
                RefreshTokenApiResult.Success(jwt, rotatedRefresh, result.status)
            }
        } else RefreshTokenApiResult.Failure(result.status, serverMessage(result, "Не удалось обновить сессию"))
    } catch (error: Exception) {
        RefreshTokenApiResult.Failure(null, networkErrorMessage(error))
    }


    /** Reuses a just-rotated token if several requests receive 401 concurrently. */
    private fun refreshTokenSafely(baseUrl: String, oldRefreshToken: String): RefreshTokenApiResult = synchronized(refreshLock) {
        val cached = lastRefreshResult
        val cacheIsFresh = System.currentTimeMillis() - lastRefreshAtMillis < 30_000L
        if (lastRefreshSourceToken == oldRefreshToken && cached != null && cacheIsFresh) {
            return@synchronized cached
        }
        when (val refreshed = refreshAccessToken(baseUrl, oldRefreshToken)) {
            is RefreshTokenApiResult.Success -> {
                lastRefreshSourceToken = oldRefreshToken
                lastRefreshResult = refreshed
                lastRefreshAtMillis = System.currentTimeMillis()
                refreshed
            }
            is RefreshTokenApiResult.Failure -> {
                lastRefreshSourceToken = null
                lastRefreshResult = null
                lastRefreshAtMillis = 0L
                refreshed
            }
        }
    }

    /** Sends an authenticated request; on 401 refreshes once and retries the original request. */
    private fun requestAuthorized(
        baseUrl: String,
        path: String,
        method: String,
        payload: JSONObject?,
        accessTokenValue: String,
        storedRefreshToken: String?
    ): AuthorizedHttpResult {
        var response = request(baseUrl, path, method, payload, accessTokenValue)
        if (response.status != 401 || storedRefreshToken.isNullOrBlank()) {
            return AuthorizedHttpResult(response)
        }

        when (val refreshed = refreshTokenSafely(baseUrl, storedRefreshToken)) {
            is RefreshTokenApiResult.Success -> {
                // Logout must send the rotated refresh token on its retried request.
                val retryPayload = if (path == AuthEndpoints.LOGOUT) {
                    JSONObject().put("refreshToken", refreshed.refreshToken)
                } else payload
                response = request(baseUrl, path, method, retryPayload, refreshed.accessToken)
                return AuthorizedHttpResult(
                    response,
                    renewedAccessToken = refreshed.accessToken,
                    renewedRefreshToken = refreshed.refreshToken
                )
            }
            is RefreshTokenApiResult.Failure -> return AuthorizedHttpResult(
                response.copy(status = refreshed.httpStatus ?: 503),
                refreshError = "Не удалось обновить сессию после HTTP 401. ${refreshed.message}"
            )
        }
    }

    /** POST /api/auth/logout with JWT in Authorization and refreshToken in JSON. */
    fun logout(baseUrl: String, accessToken: String, refreshToken: String?): AuthActionApiResult = try {
        require(!refreshToken.isNullOrBlank()) { "Refresh-токен не найден. Выполни вход заново." }
        val payload = JSONObject().put("refreshToken", refreshToken)
        val result = requestAuthorized(
            baseUrl,
            AuthEndpoints.LOGOUT,
            method = "POST",
            payload = payload,
            accessTokenValue = accessToken,
            storedRefreshToken = refreshToken
        )
        if (result.response.status in 200..299) {
            AuthActionApiResult.Success(result.response.status, result.renewedAccessToken, result.renewedRefreshToken)
        } else {
            AuthActionApiResult.Failure(
                result.response.status,
                result.refreshError ?: serverMessage(result.response, "Не удалось завершить сессию на сервере"),
                result.renewedAccessToken,
                result.renewedRefreshToken
            )
        }
    } catch (error: Exception) {
        AuthActionApiResult.Failure(null, networkErrorMessage(error))
    }

    /** GET /api/users/me with automatic refresh + one retry after HTTP 401. */
    fun getCurrentUserName(
        baseUrl: String,
        jwt: String,
        storedRefreshToken: String? = null
    ): CurrentUserApiResult = try {
        val result = requestAuthorized(
            baseUrl,
            AuthEndpoints.CURRENT_USER,
            method = "GET",
            payload = null,
            accessTokenValue = jwt,
            storedRefreshToken = storedRefreshToken
        )
        if (result.response.status !in 200..299) {
            CurrentUserApiResult.Failure(
                result.response.status,
                result.refreshError ?: serverMessage(result.response, "Не удалось получить профиль"),
                result.renewedAccessToken,
                result.renewedRefreshToken
            )
        } else {
            val json = result.response.json()
            if (json == null) CurrentUserApiResult.Failure(result.response.status, "Сервер вернул пустой или некорректный JSON профиля.", result.renewedAccessToken, result.renewedRefreshToken)
            else CurrentUserApiResult.Success(parseUserProfile(json), result.response.status, result.renewedAccessToken, result.renewedRefreshToken)
        }
    } catch (error: Exception) {
        CurrentUserApiResult.Failure(null, networkErrorMessage(error))
    }

    /** PUT /api/users/me. This user-profile route/DTO is outside the 02.11 auth specification. */
    fun updateCurrentUserProfile(
        baseUrl: String,
        accessTokenValue: String,
        profile: UserProfile,
        storedRefreshToken: String? = null
    ): AuthActionApiResult = try {
        require(accessTokenValue.isNotBlank()) { "Сначала войдите в приложение." }
        val payload = JSONObject().apply {
            put("login", profile.login)
            put("email", profile.email)
            put("phone", profile.phone)
            put("fullName", profile.fullName)
            put("birthDate", profile.birthDate)
            put("height", profile.height)
            put("weight", profile.weight)
            put("gender", profile.gender)
        }
        val result = requestAuthorized(
            baseUrl,
            AuthEndpoints.UPDATE_CURRENT_USER,
            method = "PUT",
            payload = payload,
            accessTokenValue = accessTokenValue,
            storedRefreshToken = storedRefreshToken
        )
        if (result.response.status in 200..299) {
            AuthActionApiResult.Success(result.response.status, result.renewedAccessToken, result.renewedRefreshToken)
        } else {
            AuthActionApiResult.Failure(
                result.response.status,
                result.refreshError ?: serverMessage(result.response, "Не удалось сохранить профиль"),
                result.renewedAccessToken,
                result.renewedRefreshToken
            )
        }
    } catch (error: Exception) {
        AuthActionApiResult.Failure(null, networkErrorMessage(error))
    }

    private fun parseUserProfile(root: JSONObject): UserProfile {
        val data = root.optJSONObject("data") ?: root
        val user = data.optJSONObject("user") ?: root.optJSONObject("user") ?: data
        val profile = user.optJSONObject("profile") ?: data.optJSONObject("profile")
        val stats = user.optJSONObject("stats") ?: profile?.optJSONObject("stats") ?: data.optJSONObject("stats") ?: root.optJSONObject("stats")
            ?: user.optJSONObject("statistics") ?: profile?.optJSONObject("statistics") ?: data.optJSONObject("statistics") ?: root.optJSONObject("statistics")
        val sources = listOfNotNull(user, profile, data, root, stats).distinct()

        fun string(vararg names: String): String {
            for (source in sources) for (name in names) {
                val value = source.opt(name) ?: continue
                val text = when (value) {
                    is String -> value.trim()
                    is Number, is Boolean -> value.toString()
                    else -> ""
                }
                if (text.isNotBlank() && !text.equals("null", ignoreCase = true)) return text
            }
            return ""
        }

        fun integer(vararg names: String): Int {
            for (source in sources) for (name in names) {
                val value = source.opt(name) ?: continue
                val parsed = when (value) {
                    is Number -> value.toInt()
                    is String -> value.replace(" ", "").toIntOrNull()
                    else -> null
                }
                if (parsed != null) return parsed.coerceAtLeast(0)
            }
            return 0
        }

        val activityArray = listOf("activityValues", "activity_values", "weeklyActivity", "weekly_activity", "activityHistory", "activity_history")
            .firstNotNullOfOrNull { key -> sources.firstNotNullOfOrNull { it.optJSONArray(key) } }
        val activity = activityArray?.let { array ->
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.opt(index)
                    val value = when (item) {
                        is Number -> item.toInt()
                        is JSONObject -> item.optInt("minutes", item.optInt("count", item.optInt("value", 0)))
                        else -> 0
                    }
                    add(value.coerceAtLeast(0))
                }
            }
        }.orEmpty()

        return UserProfile(
            login = string("login", "nickname", "username", "userName"),
            email = string("email", "mail"),
            phone = string("phone", "phoneNumber", "phone_number"),
            fullName = string("fullName", "full_name", "name", "displayName", "display_name"),
            birthDate = string("birthDate", "birth_date", "dateOfBirth", "date_of_birth"),
            height = string("height", "heightCm", "height_cm"),
            weight = string("weight", "weightKg", "weight_kg"),
            gender = string("gender", "sex"),
            totalWorkouts = integer("totalWorkouts", "total_workouts", "workoutsCount", "workouts_count", "completedWorkouts", "completed_workouts", "trainingCount", "training_count", "totalTrainingCount", "total_training_count", "completedSessions", "completed_sessions"),
            sportCoins = integer("sportCoins", "sport_coins", "coins", "balance"),
            totalMinutes = integer("totalMinutes", "total_minutes", "trainingMinutes", "training_minutes", "minutesTrained", "minutes_trained", "totalTrainingMinutes", "total_training_minutes"),
            favoriteCount = integer("favoriteCount", "favorite_count", "favoritesCount", "favorites_count", "favoriteWorkoutsCount", "favorite_workouts_count"),
            activityValues = activity
        )
    }
}
