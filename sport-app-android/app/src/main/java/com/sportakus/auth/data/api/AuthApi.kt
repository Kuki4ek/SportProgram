package com.sportakus.auth.data.api

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/** Request model matching the current C# POST /api/auth/register contract. */
data class RegisterRequest(
    val login: String,
    val email: String,
    val password: String,
    val phone: String? = null
)

sealed class RegisterApiResult {
    data class Success(val jwt: String, val httpStatus: Int) : RegisterApiResult()
    data class Failure(
        val httpStatus: Int?,
        val message: String,
        val fieldErrors: Map<String, List<String>> = emptyMap()
    ) : RegisterApiResult()
}

sealed class CurrentUserApiResult {
    data class Success(val displayName: String, val httpStatus: Int) : CurrentUserApiResult()
    data class Failure(val httpStatus: Int?, val message: String) : CurrentUserApiResult()
}

/** Identifier can be a username, an email address, or a phone number. */
data class LoginApiRequest(
    val identifier: String,
    val password: String
)

sealed class LoginApiResult {
    data class Success(val jwt: String, val httpStatus: Int) : LoginApiResult()
    data class Failure(
        val httpStatus: Int?,
        val message: String,
        val fieldErrors: Map<String, List<String>> = emptyMap()
    ) : LoginApiResult()
}

/** Small HTTP client for the current registration endpoint; no additional JSON library is required. */
object AuthApi {
    fun register(baseUrl: String, request: RegisterRequest): RegisterApiResult {
        var connection: HttpURLConnection? = null
        return try {
            val normalized = baseUrl.trim().trimEnd('/')
            require(normalized.startsWith("http://", ignoreCase = true) || normalized.startsWith("https://", ignoreCase = true)) {
                "Адрес сервера должен начинаться с http:// или https://."
            }

            val payload = JSONObject().apply {
                put("login", request.login.trim())
                put("email", request.email.trim())
                put("password", request.password)
                request.phone?.trim()?.takeIf { it.isNotEmpty() }?.let { put("phone", it) }
            }

            connection = (URL("$normalized/api/auth/register").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12_000
                readTimeout = 15_000
                doOutput = true
                useCaches = false
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            connection!!.outputStream.use { stream ->
                stream.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val status = connection!!.responseCode
            val responseStream = if (status >= 400) connection!!.errorStream else connection!!.inputStream
            val responseBody = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val responseJson = responseBody.takeIf { it.isNotBlank() }?.let {
                runCatching { JSONObject(it) }.getOrNull()
            }

            if (status in 200..299) {
                val jwt = responseJson?.optString("jwt")?.trim().orEmpty()
                if (jwt.isNotBlank() && jwt != "null") {
                    RegisterApiResult.Success(jwt = jwt, httpStatus = status)
                } else {
                    RegisterApiResult.Failure(
                        status,
                        "Сервер ответил успешно, но в ответе отсутствует поле jwt. Проверь контракт API."
                    )
                }
            } else {
                val fieldErrors = parseFieldErrors(responseJson?.optJSONObject("errors"))
                val message = when (status) {
                    400 -> "Сервер отклонил регистрацию. Проверь поля, отмеченные сообщениями об ошибках."
                    401 -> "Сервер вернул 401 Unauthorized. Уточни у разработчика, доступна ли регистрация без авторизации."
                    in 500..599 -> "Ошибка сервера ($status). Посмотри журнал C# API."
                    else -> responseJson?.optString("title")?.takeIf { it.isNotBlank() }
                        ?: "Сервер вернул ошибку HTTP $status."
                }
                RegisterApiResult.Failure(status, message, fieldErrors)
            }
        } catch (e: UnknownHostException) {
            RegisterApiResult.Failure(null, "Не удалось найти сервер. Проверь адрес API и подключение к сети.")
        } catch (e: SocketTimeoutException) {
            RegisterApiResult.Failure(null, "Сервер не ответил вовремя. Проверь, что C# API запущен.")
        } catch (e: java.net.ConnectException) {
            RegisterApiResult.Failure(null, "Не удалось подключиться к серверу. Проверь порт, адрес и настройки брандмауэра.")
        } catch (e: IllegalArgumentException) {
            RegisterApiResult.Failure(null, e.message ?: "Проверь адрес сервера.")
        } catch (e: Exception) {
            RegisterApiResult.Failure(null, "Не удалось выполнить запрос: ${e.localizedMessage ?: e.javaClass.simpleName}")
        } finally {
            connection?.disconnect()
        }
    }


    /**
     * Login using the final identifier fields. The last API note accidentally repeats
     * /api/auth/register under the Login heading; the previously supplied contract used
     * /api/auth/login, so this client uses /api/auth/login.
     * Exactly one of login/email/phone is sent based on the identifier entered by the user.
     */
    fun login(baseUrl: String, request: LoginApiRequest): LoginApiResult {
        var connection: HttpURLConnection? = null
        return try {
            val normalized = baseUrl.trim().trimEnd('/')
            require(normalized.startsWith("http://", ignoreCase = true) || normalized.startsWith("https://", ignoreCase = true)) {
                "Адрес сервера должен начинаться с http:// или https://."
            }
            val identifier = request.identifier.trim()
            require(identifier.isNotBlank()) { "Введите логин, почту или телефон." }

            val fieldName = when {
                identifier.contains('@') -> "email"
                identifier.startsWith("+") || (identifier.all { it.isDigit() || it in " ()-" } && identifier.count { it.isDigit() } >= 10) -> "phone"
                else -> "login"
            }
            val payload = JSONObject().apply {
                put(fieldName, identifier)
                put("password", request.password)
            }

            connection = (URL("$normalized/api/auth/login").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12_000
                readTimeout = 15_000
                doOutput = true
                useCaches = false
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            connection!!.outputStream.use { stream ->
                stream.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val status = connection!!.responseCode
            val responseStream = if (status >= 400) connection!!.errorStream else connection!!.inputStream
            val responseBody = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val responseJson = responseBody.takeIf { it.isNotBlank() }?.let {
                runCatching { JSONObject(it) }.getOrNull()
            }

            if (status in 200..299) {
                val jwt = responseJson?.optString("jwt")?.trim().orEmpty()
                if (jwt.isNotBlank() && jwt != "null") {
                    LoginApiResult.Success(jwt, status)
                } else {
                    LoginApiResult.Failure(status, "Сервер ответил успешно, но в JSON отсутствует поле jwt.")
                }
            } else {
                val fieldErrors = parseFieldErrors(responseJson?.optJSONObject("errors"))
                val message = when (status) {
                    400 -> "Сервер отклонил вход. Проверь логин, почту или телефон и пароль."
                    401 -> "Неверный логин, почта, телефон или пароль (401 Unauthorized)."
                    in 500..599 -> "Ошибка сервера ($status). Посмотри журнал C# API."
                    else -> responseJson?.optString("title")?.takeIf { it.isNotBlank() }
                        ?: "Сервер вернул ошибку HTTP $status при входе."
                }
                LoginApiResult.Failure(status, message, fieldErrors)
            }
        } catch (e: UnknownHostException) {
            LoginApiResult.Failure(null, "Не удалось найти сервер. Проверь адрес API и подключение к сети.")
        } catch (e: SocketTimeoutException) {
            LoginApiResult.Failure(null, "Сервер не ответил вовремя. Проверь, что C# API запущен.")
        } catch (e: java.net.ConnectException) {
            LoginApiResult.Failure(null, "Не удалось подключиться к серверу. Проверь порт, адрес и настройки брандмауэра.")
        } catch (e: IllegalArgumentException) {
            LoginApiResult.Failure(null, e.message ?: "Проверь адрес сервера.")
        } catch (e: Exception) {
            LoginApiResult.Failure(null, "Не удалось выполнить запрос: ${e.localizedMessage ?: e.javaClass.simpleName}")
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Request the current authenticated profile through GET /api/users/me.
     * The final contract returns a `login` field; alternative display-name keys are
     * accepted only for compatibility with earlier drafts.
     */
    fun getCurrentUserName(baseUrl: String, jwt: String): CurrentUserApiResult {
        var connection: HttpURLConnection? = null
        return try {
            val normalized = baseUrl.trim().trimEnd('/')
            require(normalized.startsWith("http://", ignoreCase = true) || normalized.startsWith("https://", ignoreCase = true)) {
                "Адрес сервера должен начинаться с http:// или https://."
            }
            require(jwt.isNotBlank()) { "JWT не найден. Сначала зарегистрируй пользователя." }

            connection = (URL("$normalized/api/users/me").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 15_000
                useCaches = false
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $jwt")
            }

            val status = connection!!.responseCode
            val responseStream = if (status >= 400) connection!!.errorStream else connection!!.inputStream
            val body = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty().trim()
            val json = body.takeIf { it.isNotBlank() }?.let { runCatching { JSONObject(it) }.getOrNull() }

            if (status in 200..299) {
                val rootUser = json?.optJSONObject("user")
                val candidates = listOfNotNull(
                    json?.optString("nickname"),
                    json?.optString("login"),
                    json?.optString("username"),
                    json?.optString("name"),
                    json?.optString("displayName"),
                    json?.optString("display_name"),
                    (json?.opt("user") as? String),
                    rootUser?.optString("nickname"),
                    rootUser?.optString("login"),
                    rootUser?.optString("username"),
                    rootUser?.optString("name")
                ).map { it.trim() }.firstOrNull { it.isNotBlank() && it != "null" }
                val displayName = candidates ?: body.takeIf {
                    it.isNotBlank() && json == null && !it.startsWith("{") && !it.startsWith("[")
                }?.trim('"')?.trim()

                if (!displayName.isNullOrBlank()) {
                    CurrentUserApiResult.Success(displayName, status)
                } else {
                    CurrentUserApiResult.Failure(
                        status,
                        "GET /api/users/me прошёл успешно, но в JSON не найдено поле login (или другое поле имени пользователя)."
                    )
                }
            } else {
                val message = when (status) {
                    401 -> "Сервер вернул 401 Unauthorized. Проверь, что JWT принят и endpoint /api/users/me защищён правильным способом."
                    404 -> "Endpoint GET /api/users/me не найден. Попроси backend-разработчика подтвердить тестовый endpoint пользователя."
                    in 500..599 -> "Ошибка сервера ($status) при получении пользователя. Посмотри журнал C# API."
                    else -> json?.optString("title")?.takeIf { it.isNotBlank() }
                        ?: "Сервер вернул ошибку HTTP $status при получении пользователя."
                }
                CurrentUserApiResult.Failure(status, message)
            }
        } catch (e: UnknownHostException) {
            CurrentUserApiResult.Failure(null, "Не удалось найти сервер. Проверь адрес API и подключение к сети.")
        } catch (e: SocketTimeoutException) {
            CurrentUserApiResult.Failure(null, "Сервер не ответил вовремя при получении пользователя.")
        } catch (e: java.net.ConnectException) {
            CurrentUserApiResult.Failure(null, "Не удалось подключиться к серверу. Проверь порт, адрес и настройки брандмауэра.")
        } catch (e: IllegalArgumentException) {
            CurrentUserApiResult.Failure(null, e.message ?: "Проверь адрес сервера.")
        } catch (e: Exception) {
            CurrentUserApiResult.Failure(null, "Не удалось получить пользователя: ${e.localizedMessage ?: e.javaClass.simpleName}")
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseFieldErrors(errors: JSONObject?): Map<String, List<String>> {
        if (errors == null) return emptyMap()
        val parsed = linkedMapOf<String, List<String>>()
        val iterator = errors.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val value = errors.optJSONArray(key)
            val messages = if (value != null) {
                buildList {
                    for (index in 0 until value.length()) {
                        val message = value.optString(index)
                        if (message.isNotBlank()) add(message)
                    }
                }
            } else {
                emptyList()
            }
            if (messages.isNotEmpty()) parsed[key.lowercase()] = messages
        }
        return parsed
    }
}
