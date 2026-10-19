package com.sportakus.auth.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.sportakus.auth.ui.theme.SportakusTheme

private enum class AuthDestination {
    LOGIN, REGISTER, RECOVERY, VERIFY_CODE, RESET_PASSWORD
}

@Composable
fun AuthApp() {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("sportakus_preferences", Context.MODE_PRIVATE)
    }
    var darkTheme by remember { mutableStateOf(preferences.getBoolean("dark_theme", false)) }
    var destination by rememberSaveable { mutableStateOf(AuthDestination.LOGIN) }

    var loginIdentifier by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var registrationPassword by rememberSaveable { mutableStateOf("") }
    var recoveryMethod by rememberSaveable { mutableStateOf("Почта") }
    var recoveryContact by rememberSaveable { mutableStateOf("") }
    var verificationCode by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }

    val toggleTheme: () -> Unit = { darkTheme = !darkTheme }
    val navigateBack: () -> Unit = {
        destination = when (destination) {
            AuthDestination.LOGIN -> AuthDestination.LOGIN
            AuthDestination.REGISTER -> AuthDestination.LOGIN
            AuthDestination.RECOVERY -> AuthDestination.LOGIN
            AuthDestination.VERIFY_CODE -> AuthDestination.RECOVERY
            AuthDestination.RESET_PASSWORD -> AuthDestination.VERIFY_CODE
        }
    }

    LaunchedEffect(darkTheme) {
        preferences.edit().putBoolean("dark_theme", darkTheme).apply()
    }

    SportakusTheme(darkTheme = darkTheme) {
        val view = LocalView.current
        val colors = MaterialTheme.colorScheme
        SideEffect {
            view.context.findActivity()?.window?.let { window ->
                window.statusBarColor = colors.background.toArgb()
                window.navigationBarColor = colors.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .imePadding()
        ) {
            // Recreate the active screen when theme changes so all theme-dependent
            // colors and any remembered UI values are refreshed immediately.
            key(darkTheme, destination) {
                when (destination) {
                AuthDestination.LOGIN -> LoginScreen(
                    identifier = loginIdentifier,
                    onIdentifierChange = { loginIdentifier = it },
                    password = loginPassword,
                    onPasswordChange = { loginPassword = it },
                    darkTheme = darkTheme,
                    onToggleTheme = toggleTheme,
                    onBack = { notice = "Вы на экране входа." },
                    onForgotPassword = { destination = AuthDestination.RECOVERY },
                    onRegister = { destination = AuthDestination.REGISTER },
                    onLogin = {
                        when {
                            loginIdentifier.isBlank() -> notice = "Введите телефон, почту или логин."
                            loginPassword.isBlank() -> notice = "Введите пароль."
                            loginPassword.length < 8 -> Unit // Длина подсвечивается прямо под полем пароля.
                            else -> notice = "Интерфейс готов, но подключение к C# API ещё не настроено. Серверный вход не выполнялся."
                        }
                    },
                    onSocialAuth = { provider ->
                        notice = "Авторизация через $provider пока не подключена к серверу."
                    }
                )

                AuthDestination.REGISTER -> RegistrationScreen(
                    name = name,
                    onNameChange = { name = it },
                    email = email,
                    onEmailChange = { email = it },
                    password = registrationPassword,
                    onPasswordChange = { registrationPassword = it },
                    darkTheme = darkTheme,
                    onToggleTheme = toggleTheme,
                    onBack = navigateBack,
                    onLogin = { destination = AuthDestination.LOGIN },
                    onForgotPassword = { destination = AuthDestination.RECOVERY },
                    onRegister = {
                        when {
                            name.isBlank() -> notice = "Введите своё имя."
                            email.isBlank() || !email.contains('@') -> notice = "Введите корректную почту."
                            registrationPassword.isBlank() -> notice = "Введите пароль."
                            registrationPassword.length < 8 -> Unit // Подсказка показывается прямо под полем пароля.
                            else -> notice = "Форма регистрации готова, но запрос к C# API ещё не подключён."
                        }
                    },
                    onSocialAuth = { provider ->
                        notice = "Регистрация через $provider пока не подключена к серверу."
                    }
                )

                AuthDestination.RECOVERY -> RecoveryScreen(
                    method = recoveryMethod,
                    onMethodChange = {
                        recoveryMethod = it
                        recoveryContact = ""
                    },
                    contact = recoveryContact,
                    onContactChange = { recoveryContact = it },
                    darkTheme = darkTheme,
                    onToggleTheme = toggleTheme,
                    onBack = navigateBack,
                    onSendCode = {
                        val valid = if (recoveryMethod == "Почта") {
                            recoveryContact.contains('@') && recoveryContact.substringAfter('@').contains('.')
                        } else {
                            recoveryContact.count(Char::isDigit) >= 7
                        }
                        if (!valid) {
                            notice = if (recoveryMethod == "Почта") "Введите корректный адрес почты." else "Введите корректный номер телефона."
                        } else {
                            destination = AuthDestination.VERIFY_CODE
                            notice = "Демонстрационный переход: код пока не отправлен, потому что API не подключён."
                        }
                    }
                )

                AuthDestination.VERIFY_CODE -> VerificationScreen(
                    code = verificationCode,
                    onCodeChange = { verificationCode = it.filter(Char::isDigit).take(8) },
                    darkTheme = darkTheme,
                    onToggleTheme = toggleTheme,
                    onBack = navigateBack,
                    onVerify = {
                        if (verificationCode.length < 4) {
                            notice = "Введите код подтверждения."
                        } else {
                            destination = AuthDestination.RESET_PASSWORD
                            notice = "Демонстрационный переход: код не проверялся сервером."
                        }
                    }
                )

                AuthDestination.RESET_PASSWORD -> ResetPasswordScreen(
                    password = newPassword,
                    onPasswordChange = { newPassword = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    darkTheme = darkTheme,
                    onToggleTheme = toggleTheme,
                    onBack = navigateBack,
                    onReset = {
                        when {
                            newPassword.isBlank() -> notice = "Введите новый пароль."
                            newPassword.length < 8 -> Unit // Подсказка показывается прямо под полем пароля.
                            newPassword != confirmPassword -> notice = "Пароли не совпадают."
                            else -> notice = "Форма готова, но запрос сброса пароля ещё не подключён к C# API."
                        }
                    }
                )
                }
            }
        }

        notice?.let { message ->
            AlertDialog(
                onDismissRequest = { notice = null },
                title = { Text("Спортакус") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { notice = null }) {
                        Text("Понятно")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
