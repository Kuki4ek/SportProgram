package com.sportakus.auth.data.api

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/** Request contract for POST /api/auth/register. */
data class RegisterRequest(
    val login: String,
    val email: String,
    val password: String,
    val emailCode: String,
    val phone: String? = null
)

sealed class RegisterApiResult {
    data class Success(
        /** Kept as `jwt` for compatibility with existing UI/account-key code. */
        val jwt: String,
        val refreshToken: String? = null,
        val httpStatus: Int
    ) : RegisterApiResult()
    data class Failure(
        val httpStatus: Int?,
        val message: String,
        val fieldErrors: Map<String, List<String>> = emptyMap()
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
    data class Failure(val httpStatus: Int?, val message: String) : CurrentUserApiResult()
}

data class LoginApiRequest(val identifier: String, val password: String)

sealed class LoginApiResult {
    data class Success(
        val jwt: String,
        val refreshToken: String? = null,
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
    data class Failure(val httpStatus: Int?, val message: String) : AuthActionApiResult()
}

sealed class RefreshTokenApiResult {
    data class Success(val accessToken: String, val refreshToken: String?, val httpStatus: Int) : RefreshTokenApiResult()
    data class Failure(val httpStatus: Int?, val message: String) : RefreshTokenApiResult()
}

/**
 * Endpoints are centralized here so API route changes do not require editing UI code.
 * Registration routes are taken from the 02.11 backend update. Password-reset, refresh,
 * and logout route names follow the integration contract currently available to the client;
 * confirm these three route names with the backend if its branch uses different names.
 */
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

/** Lightweight HTTP client based on HttpURLConnection and org.json. */
object AuthApi {
    private data class HttpResult(
        val status: Int,
        val body: String,
        val authorizationHeader: String? = null
    ) {
        fun json(): JSONObject? = body.takeIf { it.isNotBlank() }
            ?.let { runCatching { JSONObject(it) }.getOrNull() }
    }

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

    private fun accessToken(result: HttpResult): String? {
        val json = result.json()
        val bodyToken = listOf("accessToken", "access_token", "jwt", "JWT", "token")
            .firstNotNullOfOrNull { key -> json?.optString(key)?.trim()?.takeIf { it.isNotEmpty() && it != "null" } }
        val header = result.authorizationHeader?.trim().orEmpty()
        val headerToken = if (header.startsWith("Bearer ", ignoreCase = true)) {
            header.substringAfter(' ').trim().takeIf { it.isNotEmpty() }
        } else null
        return bodyToken ?: headerToken
    }

    private fun refreshToken(result: HttpResult): String? {
        val json = result.json()
        return listOf("refreshToken", "refresh_token")
            .firstNotNullOfOrNull { key -> json?.optString(key)?.trim()?.takeIf { it.isNotEmpty() && it != "null" } }
    }

    private fun serverMessage(result: HttpResult, fallback: String): String {
        val json = result.json()
        return json?.optString("detail")?.takeIf { it.isNotBlank() }
            ?: json?.optString("message")?.takeIf { it.isNotBlank() }
            ?: json?.optString("title")?.takeIf { it.isNotBlank() }
            ?: when (result.status) {
                401 -> "Сессия недействительна. Войдите в приложение снова."
                404 -> "Запрашиваемый endpoint не найден (HTTP 404). Проверь маршрут на C# API."
                429 -> "Слишком много запросов. Подождите и попробуйте ещё раз."
                in 500..599 -> "Ошибка сервера (${result.status}). Проверь журнал C# API."
                else -> "$fallback (HTTP ${result.status})."
            }
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

    private fun networkErrorMessage(error: Exception): String = when (error) {
        is UnknownHostException -> "Не удалось найти сервер. Проверь адрес API и подключение к сети."
        is SocketTimeoutException -> "Сервер не ответил вовремя. Попробуй ещё раз."
        is java.net.ConnectException -> "Не удалось подключиться к серверу. Проверь порт, адрес и настройки брандмауэра."
        is IllegalArgumentException -> error.message ?: "Проверь адрес сервера."
        else -> "Ошибка запроса: ${error.localizedMessage ?: error.javaClass.simpleName}"
    }

    /** POST /api/auth/email-confirm — отправка кода для подтверждения почты. */
    fun requestEmailConfirmation(baseUrl: String, login: String, email: String): EmailConfirmationApiResult = try {
        require(login.isNotBlank()) { "Введите имя пользователя." }
        require(email.isNotBlank()) { "Введите адрес электронной почты." }
        val result = request(
            baseUrl,
            AuthEndpoints.EMAIL_CONFIRM,
            payload = JSONObject().put("login", login.trim()).put("email", email.trim())
        )
        if (result.status in 200..299) EmailConfirmationApiResult.Success(result.status)
        else EmailConfirmationApiResult.Failure(
            result.status,
            serverMessage(result, "Сервер не смог отправить код подтверждения"),
            parseFieldErrors(result.json()?.optJSONObject("errors"))
        )
    } catch (error: Exception) {
        EmailConfirmationApiResult.Failure(null, networkErrorMessage(error))
    }

    /** POST /api/auth/register. The confirmation field is `emailCode` (not `code`). */
    fun register(baseUrl: String, registration: RegisterRequest): RegisterApiResult = try {
        val payload = JSONObject().apply {
            put("login", registration.login.trim())
            put("email", registration.email.trim())
            put("password", registration.password)
            put("emailCode", registration.emailCode.trim())
            registration.phone?.trim()?.takeIf { it.isNotEmpty() }?.let { put("phone", it) }
        }
        val result = request(baseUrl, AuthEndpoints.REGISTER, payload = payload)
        if (result.status in 200..299) {
            val token = accessToken(result)
            if (token.isNullOrBlank()) {
                RegisterApiResult.Failure(result.status, "Регистрация прошла, но сервер не вернул accessToken.")
            } else {
                RegisterApiResult.Success(token, refreshToken(result), result.status)
            }
        } else {
            RegisterApiResult.Failure(
                result.status,
                serverMessage(result, "Сервер отклонил регистрацию"),
                parseFieldErrors(result.json()?.optJSONObject("errors"))
            )
        }
    } catch (error: Exception) {
        RegisterApiResult.Failure(null, networkErrorMessage(error))
    }

    /** POST /api/auth/login. Exactly one identifier property is included in the body. */
    fun login(baseUrl: String, credentials: LoginApiRequest): LoginApiResult = try {
        val identifier = credentials.identifier.trim()
        require(identifier.isNotBlank()) { "Введите логин, почту или телефон." }
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
        if (result.status in 200..299) {
            val token = accessToken(result)
            if (token.isNullOrBlank()) LoginApiResult.Failure(result.status, "Сервер не вернул accessToken.")
            else LoginApiResult.Success(token, refreshToken(result), result.status)
        } else {
            LoginApiResult.Failure(
                result.status,
                serverMessage(result, "Не удалось войти"),
                parseFieldErrors(result.json()?.optJSONObject("errors"))
            )
        }
    } catch (error: Exception) {
        LoginApiResult.Failure(null, networkErrorMessage(error))
    }

    /**
     * Password recovery request. contactKind must be `nickname`, `email`, or `phone`.
     * The backend returns `code_token`; it is kept only in memory until verification.
     */
    fun requestPasswordReset(baseUrl: String, contactKind: String, identifier: String): PasswordRecoveryApiResult = try {
        require(identifier.isNotBlank()) { "Введите данные аккаунта." }
        require(contactKind in setOf("nickname", "email", "phone")) { "Неизвестный тип контакта." }
        val result = request(
            baseUrl,
            AuthEndpoints.REQUEST_PASSWORD_RESET,
            payload = JSONObject().put(contactKind, identifier.trim())
        )
        if (result.status in 200..299) {
            val json = result.json()
            val codeToken = listOf("code_token", "codeToken")
                .firstNotNullOfOrNull { key -> json?.optString(key)?.takeIf { it.isNotBlank() && it != "null" } }
            PasswordRecoveryApiResult.Success(result.status, codeToken = codeToken)
        } else PasswordRecoveryApiResult.Failure(result.status, serverMessage(result, "Не удалось отправить код восстановления"))
    } catch (error: Exception) {
        PasswordRecoveryApiResult.Failure(null, networkErrorMessage(error))
    }

    /** POST forgot-password/code with the exact code_token + verify_code fields. */
    fun verifyPasswordResetCode(baseUrl: String, codeToken: String, verifyCode: String): PasswordRecoveryApiResult = try {
        require(codeToken.isNotBlank()) { "Сервер не вернул code_token. Запросите код повторно." }
        require(verifyCode.isNotBlank()) { "Введите код подтверждения." }
        val result = request(
            baseUrl,
            AuthEndpoints.VERIFY_PASSWORD_RESET_CODE,
            payload = JSONObject().put("code_token", codeToken).put("verify_code", verifyCode.trim())
        )
        if (result.status in 200..299) {
            val json = result.json()
            val resetToken = listOf("reset_token", "resetToken", "password_reset_token", "passwordResetToken")
                .firstNotNullOfOrNull { key -> json?.optString(key)?.takeIf { it.isNotBlank() && it != "null" } }
            PasswordRecoveryApiResult.Success(result.status, codeToken = codeToken, resetToken = resetToken)
        } else PasswordRecoveryApiResult.Failure(result.status, serverMessage(result, "Код подтверждения не принят"))
    } catch (error: Exception) {
        PasswordRecoveryApiResult.Failure(null, networkErrorMessage(error))
    }

    /** POST forgot-password/reset; validated code data stays only in memory. */
    fun resetPassword(
        baseUrl: String,
        codeToken: String,
        verifyCode: String,
        newPassword: String,
        resetToken: String? = null
    ): AuthActionApiResult = try {
        require(codeToken.isNotBlank()) { "code_token не найден. Запросите код восстановления повторно." }
        require(verifyCode.isNotBlank()) { "Код подтверждения не найден. Пройдите проверку ещё раз." }
        require(newPassword.length >= 8) { "Пароль должен содержать не менее 8 символов." }
        val payload = JSONObject()
            .put("code_token", codeToken)
            .put("verify_code", verifyCode.trim())
            .put("new_password", newPassword)
        resetToken?.takeIf { it.isNotBlank() }?.let { payload.put("reset_token", it) }
        val result = request(
            baseUrl,
            AuthEndpoints.RESET_PASSWORD,
            payload = payload
        )
        if (result.status in 200..299) AuthActionApiResult.Success(result.status)
        else AuthActionApiResult.Failure(result.status, serverMessage(result, "Не удалось изменить пароль"))
    } catch (error: Exception) {
        AuthActionApiResult.Failure(null, networkErrorMessage(error))
    }

    /** POST /api/auth/refresh using the long-lived refresh token. */
    fun refreshAccessToken(baseUrl: String, refreshTokenValue: String): RefreshTokenApiResult = try {
        require(refreshTokenValue.isNotBlank()) { "Refresh-токен не найден." }
        val result = request(
            baseUrl,
            AuthEndpoints.REFRESH,
            payload = JSONObject().put("refreshToken", refreshTokenValue)
        )
        if (result.status in 200..299) {
            val access = accessToken(result)
            if (access.isNullOrBlank()) RefreshTokenApiResult.Failure(result.status, "Сервер не вернул новый accessToken.")
            else RefreshTokenApiResult.Success(access, refreshToken(result) ?: refreshTokenValue, result.status)
        } else RefreshTokenApiResult.Failure(result.status, serverMessage(result, "Не удалось обновить сессию"))
    } catch (error: Exception) {
        RefreshTokenApiResult.Failure(null, networkErrorMessage(error))
    }

    /** POST /api/auth/logout with bearer access token. */
    fun logout(baseUrl: String, accessToken: String, refreshToken: String?): AuthActionApiResult = try {
        val payload = JSONObject().apply {
            refreshToken?.takeIf { it.isNotBlank() }?.let { put("refreshToken", it) }
        }
        val result = request(
            baseUrl,
            AuthEndpoints.LOGOUT,
            payload = payload,
            bearerToken = accessToken
        )
        if (result.status in 200..299 || result.status == 401) AuthActionApiResult.Success(result.status)
        else AuthActionApiResult.Failure(result.status, serverMessage(result, "Не удалось завершить сессию на сервере"))
    } catch (error: Exception) {
        AuthActionApiResult.Failure(null, networkErrorMessage(error))
    }

    /** GET /api/users/me. If access token expired, try refresh once and repeat the request. */
    fun getCurrentUserName(
        baseUrl: String,
        jwt: String,
        storedRefreshToken: String? = null
    ): CurrentUserApiResult {
        return try {
            var activeAccessToken = jwt
            var renewedAccess: String? = null
            var renewedRefresh: String? = null
            var result = request(baseUrl, AuthEndpoints.CURRENT_USER, method = "GET", bearerToken = activeAccessToken)
            if (result.status == 401 && !storedRefreshToken.isNullOrBlank()) {
                when (val refreshed = refreshAccessToken(baseUrl, storedRefreshToken)) {
                    is RefreshTokenApiResult.Success -> {
                        activeAccessToken = refreshed.accessToken
                        renewedAccess = refreshed.accessToken
                        renewedRefresh = refreshed.refreshToken
                        result = request(baseUrl, AuthEndpoints.CURRENT_USER, method = "GET", bearerToken = activeAccessToken)
                    }
                    is RefreshTokenApiResult.Failure -> return CurrentUserApiResult.Failure(
                        refreshed.httpStatus,
                        "Сессия истекла. ${refreshed.message}"
                    )
                }
            }
            if (result.status !in 200..299) {
                CurrentUserApiResult.Failure(result.status, serverMessage(result, "Не удалось получить профиль"))
            } else {
                val json = result.json()
                if (json == null) {
                    CurrentUserApiResult.Failure(result.status, "Сервер вернул пустой или некорректный JSON профиля.")
                } else {
                    val profile = parseUserProfile(json)
                    CurrentUserApiResult.Success(profile, result.status, renewedAccess, renewedRefresh)
                }
            }
        } catch (error: Exception) {
            CurrentUserApiResult.Failure(null, networkErrorMessage(error))
        }
    }

    /**
     * PUT /api/users/me. The endpoint is kept separate from the UI so the DTO can be
     * aligned with the backend contract without changing Compose screens.
     */
    fun updateCurrentUserProfile(
        baseUrl: String,
        accessTokenValue: String,
        profile: UserProfile,
        storedRefreshToken: String? = null
    ): AuthActionApiResult {
        return try {
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
            var activeAccessToken = accessTokenValue
            var renewedAccessToken: String? = null
            var renewedRefreshToken: String? = null
            var result = request(
                baseUrl,
                AuthEndpoints.UPDATE_CURRENT_USER,
                method = "PUT",
                payload = payload,
                bearerToken = activeAccessToken
            )
            if (result.status == 401 && !storedRefreshToken.isNullOrBlank()) {
                when (val refreshed = refreshAccessToken(baseUrl, storedRefreshToken)) {
                    is RefreshTokenApiResult.Success -> {
                        activeAccessToken = refreshed.accessToken
                        renewedAccessToken = refreshed.accessToken
                        renewedRefreshToken = refreshed.refreshToken
                        result = request(
                            baseUrl,
                            AuthEndpoints.UPDATE_CURRENT_USER,
                            method = "PUT",
                            payload = payload,
                            bearerToken = activeAccessToken
                        )
                    }
                    is RefreshTokenApiResult.Failure -> return AuthActionApiResult.Failure(
                        refreshed.httpStatus,
                        "Сессия истекла. ${refreshed.message}"
                    )
                }
            }
            if (result.status in 200..299) {
                AuthActionApiResult.Success(result.status, renewedAccessToken, renewedRefreshToken)
            } else {
                AuthActionApiResult.Failure(result.status, serverMessage(result, "Не удалось сохранить профиль"))
            }
        } catch (error: Exception) {
            AuthActionApiResult.Failure(null, networkErrorMessage(error))
        }
    }
    private fun parseUserProfile(root: JSONObject?): UserProfile {
        if (root == null) return UserProfile()
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