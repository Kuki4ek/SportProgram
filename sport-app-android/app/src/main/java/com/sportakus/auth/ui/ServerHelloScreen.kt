package com.sportakus.auth.ui

import com.sportakus.auth.ui.theme.sportakusTextColor
import com.sportakus.auth.ui.theme.sportakusSecondaryTextColor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportakus.auth.data.api.AuthApi
import com.sportakus.auth.data.api.CurrentUserApiResult
import com.sportakus.auth.data.token.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val HelloGreen = Color(0xFF7DBA00)

/**
 * Small integration test screen shown after successful registration.
 * It calls GET /api/users/me with Authorization: Bearer <jwt> and displays
 * the name returned by the server. It does not display the JWT itself.
 */
@Composable
fun ServerHelloScreen(
    baseUrl: String,
    onBackToAuth: () -> Unit
) {
    val context = LocalContext.current
    var displayName by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var retryCounter by remember { mutableStateOf(0) }

    LaunchedEffect(baseUrl, retryCounter) {
        loading = true
        displayName = null
        errorMessage = null
        val jwt = TokenStore.getAccessToken(context)
        val refreshToken = TokenStore.getRefreshToken(context)
        if (jwt.isNullOrBlank()) {
            errorMessage = "JWT не найден на устройстве. Сначала выполни вход или регистрацию."
            loading = false
        } else {
            val result = withContext(Dispatchers.IO) {
                AuthApi.getCurrentUserName(baseUrl = baseUrl, jwt = jwt, storedRefreshToken = refreshToken)
            }
            when (result) {
                is CurrentUserApiResult.Success -> {
                    if (!result.renewedAccessToken.isNullOrBlank()) {
                        TokenStore.saveTokens(
                            context,
                            result.renewedAccessToken,
                            result.renewedRefreshToken ?: refreshToken
                        )
                    }
                    displayName = result.displayName
                }
                is CurrentUserApiResult.Failure -> {
                    errorMessage = result.message
                    if (result.httpStatus == 401) {
                        TokenStore.clear(context)
                        onBackToAuth()
                    }
                }
            }
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DarkText(
            text = "Тест авторизации",
            color = sportakusTextColor(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(26.dp))

        when {
            loading -> {
                CircularProgressIndicator(color = HelloGreen)
                Spacer(Modifier.height(16.dp))
                DarkText(
                    text = "Получаем профиль пользователя с сервера…",
                    color = sportakusSecondaryTextColor(),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
            displayName != null -> {
                DarkText(
                    text = "Привет, $displayName",
                    color = sportakusTextColor(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                DarkText(
                    text = "Имя получено из GET /api/users/me с JWT в заголовке Authorization: Bearer.",
                    color = sportakusSecondaryTextColor(),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                DarkText(
                    text = "JWT получен и сохранён.",
                    color = sportakusTextColor(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                DarkText(
                    text = "Не удалось получить имя пользователя с сервера.",
                    color = sportakusSecondaryTextColor(),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                DarkText(
                    text = errorMessage.orEmpty(),
                    color = sportakusTextColor(),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { retryCounter++ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HelloGreen,
                        contentColor = Color.Black
                    )
                ) {
                    DarkText("Повторить запрос", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        OutlinedButton(
            onClick = onBackToAuth,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = sportakusTextColor())
        ) {
            DarkText("Вернуться к авторизации", color = sportakusTextColor(), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
