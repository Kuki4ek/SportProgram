package com.sportakus.auth.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import com.sportakus.auth.ui.theme.SportakusTheme
import com.sportakus.auth.data.api.AuthApi
import com.sportakus.auth.data.api.RegisterApiResult
import com.sportakus.auth.data.api.RegisterRequest
import com.sportakus.auth.data.api.LoginApiRequest
import com.sportakus.auth.data.api.LoginApiResult
import com.sportakus.auth.data.token.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private enum class AuthDestination {
    SPLASH,
    SAFETY_NOTE,
    INTRO,
    QUESTION_REASON,
    QUESTION_BODY,
    QUESTION_BIRTH,
    QUESTION_INTENSITY,
    QUESTION_REGULARITY,
    QUESTION_VOLUME,
    QUESTION_FORMAT,
    QUESTION_GOALS,
    QUESTION_MEDICAL,
    AUTH_WELCOME,
    LOGIN,
    REGISTER,
    REGISTER_CONTACT,
    REGISTRATION_VERIFY_CODE,
    RECOVERY,
    RECOVERY_VERIFY_CODE,
    RESET_PASSWORD,
    SERVER_SETTINGS,
    GUEST_HOME,
    SERVER_HELLO
}

@Composable
fun AuthApp() {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("sportakus_preferences", Context.MODE_PRIVATE)
    }
    // Always show the branded splash. On the first launch the safety note follows;
    // afterwards the app opens the authentication landing screen. A first-time guest
    // is sent to the intro cards and questionnaire.
    var destination by remember { mutableStateOf(AuthDestination.SPLASH) }
    var serverSettingsReturn by remember { mutableStateOf(AuthDestination.AUTH_WELCOME) }

    // Test server address is editable in the app and persisted across restarts.
    val defaultServerUrl = "http://10.0.2.2:5000"
    var serverBaseUrl by remember {
        mutableStateOf(preferences.getString("api_base_url", defaultServerUrl) ?: defaultServerUrl)
    }
    var serverUrlDraft by rememberSaveable {
        mutableStateOf(preferences.getString("api_base_url", defaultServerUrl) ?: defaultServerUrl)
    }
    var registrationSubmitting by remember { mutableStateOf(false) }
    var loginSubmitting by remember { mutableStateOf(false) }
    var serverRegistrationFieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // This edition is intentionally dark-only; no theme switch is exposed.
    val activeDarkTheme = true

    // Authentication form state.
    var loginIdentifier by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var registrationLogin by rememberSaveable { mutableStateOf("") }
    var registrationEmail by rememberSaveable { mutableStateOf("") }
    var registrationPhone by rememberSaveable { mutableStateOf("") }
    var registrationPassword by rememberSaveable { mutableStateOf("") }
    var registrationAttempted by rememberSaveable { mutableStateOf(false) }
    var registrationLoginTouched by rememberSaveable { mutableStateOf(false) }
    var registrationEmailTouched by rememberSaveable { mutableStateOf(false) }
    var registrationPhoneTouched by rememberSaveable { mutableStateOf(false) }
    var registrationPasswordTouched by rememberSaveable { mutableStateOf(false) }
    var registrationContactMethod by rememberSaveable { mutableStateOf("Почта") }
    var registrationContact by rememberSaveable { mutableStateOf("") }
    var registrationVerificationCode by rememberSaveable { mutableStateOf("") }
    var registrationResendKey by rememberSaveable { mutableStateOf(0) }
    var recoveryMethod by rememberSaveable { mutableStateOf("Почта") }
    var recoveryContact by rememberSaveable { mutableStateOf("") }
    var recoveryVerificationCode by rememberSaveable { mutableStateOf("") }
    var recoveryResendKey by rememberSaveable { mutableStateOf(0) }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }

    // Questionnaire answers are retained locally while the user moves between steps.
    var questionnaireReasons by rememberSaveable { mutableStateOf("Фитнес|Общение|Самостоятельность|Цель") }
    var questionnaireHeight by rememberSaveable { mutableStateOf("170") }
    var questionnaireWeight by rememberSaveable { mutableStateOf("65") }
    var questionnaireGender by rememberSaveable { mutableStateOf("Мужчина") }
    var birthDay by rememberSaveable { mutableStateOf("29") }
    var birthMonth by rememberSaveable { mutableStateOf("Март") }
    var birthYear by rememberSaveable { mutableStateOf("2003") }
    var questionnaireIntensity by rememberSaveable { mutableStateOf("Практически не занимался(-ась)") }
    var questionnaireRegularity by rememberSaveable { mutableStateOf("Не занимался(-ась) регулярно") }
    var questionnaireVolume by rememberSaveable { mutableStateOf("До 5 минут") }
    var questionnaireDays by rememberSaveable { mutableStateOf("3 дня в неделю") }
    var questionnaireFormats by rememberSaveable { mutableStateOf("Без прыжков|Самостоятельно выбирать тип") }
    var questionnaireGoals by rememberSaveable { mutableStateOf("Больше двигаться|Поддерживать регулярность занятий") }
    var questionnaireMedical by rememberSaveable { mutableStateOf("Нет известных мне ограничений") }

    // Kept as a no-op while the theme switch is intentionally disabled.
    val toggleTheme: () -> Unit = {}
    val completeOnboarding: () -> Unit = {
        preferences.edit().putBoolean("onboarding_completed", true).apply()
        destination = AuthDestination.GUEST_HOME
    }

    val navigateBack: () -> Unit = {
        destination = when (destination) {
            AuthDestination.SPLASH -> AuthDestination.SPLASH
            AuthDestination.SAFETY_NOTE -> AuthDestination.SPLASH
            AuthDestination.INTRO -> AuthDestination.SAFETY_NOTE
            AuthDestination.QUESTION_REASON -> AuthDestination.INTRO
            AuthDestination.QUESTION_BODY -> AuthDestination.QUESTION_REASON
            AuthDestination.QUESTION_BIRTH -> AuthDestination.QUESTION_BODY
            AuthDestination.QUESTION_INTENSITY -> AuthDestination.QUESTION_BIRTH
            AuthDestination.QUESTION_REGULARITY -> AuthDestination.QUESTION_INTENSITY
            AuthDestination.QUESTION_VOLUME -> AuthDestination.QUESTION_REGULARITY
            AuthDestination.QUESTION_FORMAT -> AuthDestination.QUESTION_VOLUME
            AuthDestination.QUESTION_GOALS -> AuthDestination.QUESTION_FORMAT
            AuthDestination.QUESTION_MEDICAL -> AuthDestination.QUESTION_GOALS
            AuthDestination.AUTH_WELCOME -> AuthDestination.SPLASH
            AuthDestination.LOGIN -> AuthDestination.AUTH_WELCOME
            AuthDestination.REGISTER -> AuthDestination.AUTH_WELCOME
            AuthDestination.REGISTER_CONTACT -> AuthDestination.REGISTER
            AuthDestination.REGISTRATION_VERIFY_CODE -> AuthDestination.REGISTER_CONTACT
            AuthDestination.RECOVERY -> AuthDestination.LOGIN
            AuthDestination.RECOVERY_VERIFY_CODE -> AuthDestination.RECOVERY
            AuthDestination.RESET_PASSWORD -> AuthDestination.RECOVERY_VERIFY_CODE
            AuthDestination.SERVER_SETTINGS -> serverSettingsReturn
            AuthDestination.GUEST_HOME -> AuthDestination.AUTH_WELCOME
            AuthDestination.SERVER_HELLO -> AuthDestination.AUTH_WELCOME
        }
    }

    val submitRegistration: () -> Unit = {
        registrationAttempted = true
        val loginError = registrationUsernameError(registrationLogin)
        val emailError = registrationEmailError(registrationEmail)
        val phoneError = registrationPhoneError(registrationPhone)
        val passwordError = when {
            registrationPassword.isBlank() -> "Пароль обязателен"
            registrationPassword.length < 8 -> "Длина пароля должна быть не менее 8 символов"
            else -> null
        }
        val locallyValid = loginError == null && emailError == null && phoneError == null && passwordError == null

        if (locallyValid && !registrationSubmitting) {
            serverRegistrationFieldErrors = emptyMap()
            registrationSubmitting = true
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    AuthApi.register(
                        baseUrl = serverBaseUrl,
                        request = RegisterRequest(
                            login = registrationLogin,
                            email = registrationEmail,
                            password = registrationPassword,
                            phone = registrationPhone.takeIf { it.isNotBlank() }
                        )
                    )
                }
                registrationSubmitting = false
                when (result) {
                    is RegisterApiResult.Success -> {
                        TokenStore.saveJwt(context, result.jwt)
                        destination = AuthDestination.SERVER_HELLO
                        notice = null
                    }
                    is RegisterApiResult.Failure -> {
                        val mapped = linkedMapOf<String, String>()
                        result.fieldErrors.forEach { (key, messages) ->
                            val normalizedKey = when (key.lowercase()) {
                                "username", "login" -> "login"
                                "email" -> "email"
                                "password" -> "password"
                                "phone" -> "phone"
                                else -> key.lowercase()
                            }
                            mapped[normalizedKey] = messages.joinToString("\n")
                        }
                        serverRegistrationFieldErrors = mapped
                        if (mapped.isEmpty()) notice = result.message
                        else notice = null
                    }
                }
            }
        }
    }

    val submitLogin: () -> Unit = {
        when {
            loginIdentifier.isBlank() -> notice = "Введите логин, почту или телефон."
            loginPassword.isBlank() -> notice = "Введите пароль."
            loginPassword.length < 8 -> notice = "Пароль должен содержать не менее 8 символов."
            loginSubmitting -> Unit
            else -> {
                notice = null
                loginSubmitting = true
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        AuthApi.login(
                            baseUrl = serverBaseUrl,
                            request = LoginApiRequest(
                                identifier = loginIdentifier,
                                password = loginPassword
                            )
                        )
                    }
                    loginSubmitting = false
                    when (result) {
                        is LoginApiResult.Success -> {
                            TokenStore.saveJwt(context, result.jwt)
                            destination = AuthDestination.SERVER_HELLO
                            notice = null
                        }
                        is LoginApiResult.Failure -> {
                            val serverDetails = result.fieldErrors.values.flatten()
                                .distinct()
                                .joinToString("\n")
                            notice = if (serverDetails.isBlank()) result.message
                            else "${result.message}\n$serverDetails"
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(destination) {
        if (destination == AuthDestination.SPLASH) {
            // Every app launch begins in the fixed dark theme.
            delay(1200)
            if (destination == AuthDestination.SPLASH) {
                destination = if (preferences.getBoolean("safety_note_seen", false)) {
                    AuthDestination.AUTH_WELCOME
                } else {
                    AuthDestination.SAFETY_NOTE
                }
            }
        }
    }

    SportakusTheme {
        val view = LocalView.current
        val colors = MaterialTheme.colorScheme
        androidx.compose.runtime.SideEffect {
            view.context.findActivity()?.window?.let { window ->
                val barColor = if (destination == AuthDestination.SPLASH) android.graphics.Color.rgb(125, 186, 0) else colors.background.toArgb()
                window.statusBarColor = barColor
                window.navigationBarColor = barColor
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !activeDarkTheme && destination != AuthDestination.SPLASH
                    isAppearanceLightNavigationBars = !activeDarkTheme && destination != AuthDestination.SPLASH
                }
            }
        }

        Surface(modifier = Modifier.fillMaxSize(), color = colors.background, contentColor = Color(0xFFF2F5EE)) {
            Box(Modifier.fillMaxSize().imePadding()) {
                when (destination) {
                        AuthDestination.SPLASH -> SplashScreen()

                        AuthDestination.SAFETY_NOTE -> SafetyNoteScreen(darkTheme = activeDarkTheme) {
                            preferences.edit().putBoolean("safety_note_seen", true).apply()
                            destination = AuthDestination.AUTH_WELCOME
                        }

                        AuthDestination.INTRO -> IntroScreen(
                            onStart = { destination = AuthDestination.QUESTION_REASON },
                            onSkip = completeOnboarding
                        )

                        AuthDestination.QUESTION_REASON -> QuestionnaireOptionsScreen(
                            question = "Какова основная причина, по которой вы решили присоединиться к Спортакус?",
                            options = listOf("Фитнес", "Мотивация", "Дисциплина", "Общение", "Здоровье", "Самостоятельность", "Антистресс", "Цель", "ЗОЖ", "Результат", "Геймификация", "Рейтинг"),
                            selectedEncoded = questionnaireReasons,
                            onSelectionChange = { questionnaireReasons = it },
                            progress = 1,
                            onBack = navigateBack,
                            onNext = { destination = AuthDestination.QUESTION_BODY },
                            chips = true,
                            multiple = true
                        )

                        AuthDestination.QUESTION_BODY -> BodyMetricsScreen(
                            height = questionnaireHeight,
                            onHeightChange = { questionnaireHeight = it },
                            weight = questionnaireWeight,
                            onWeightChange = { questionnaireWeight = it },
                            progress = 2,
                            onBack = navigateBack,
                            onNext = { destination = AuthDestination.QUESTION_BIRTH }
                        )

                        AuthDestination.QUESTION_BIRTH -> BirthDateScreen(
                            day = birthDay,
                            month = birthMonth,
                            year = birthYear,
                            gender = questionnaireGender,
                            onGenderChange = { questionnaireGender = it },
                            onDateChange = { d, m, y -> birthDay = d; birthMonth = m; birthYear = y },
                            progress = 2,
                            onBack = navigateBack,
                            onNext = { destination = AuthDestination.QUESTION_INTENSITY }
                        )

                        AuthDestination.QUESTION_INTENSITY -> QuestionnaireOptionsScreen(
                            question = "Как часто вы занимались физической активностью в течение последнего месяца?",
                            categoryLabel = "Интенсивность",
                            screenTitle = "Расскажите о себе",
                            options = listOf("Практически не занимался(-ась)", "Несколько раз за месяц", "Примерно 1–2 раза в неделю", "Примерно 3–4 раза в неделю", "5 раз в неделю или чаще"),
                            selectedEncoded = questionnaireIntensity,
                            onSelectionChange = { questionnaireIntensity = it.substringBefore('|') },
                            progress = 3,
                            onBack = navigateBack,
                            onNext = { destination = AuthDestination.QUESTION_REGULARITY },
                            collapsibleOptions = true
                        )

                        AuthDestination.QUESTION_REGULARITY -> QuestionnaireOptionsScreen(
                            question = "Насколько регулярно вы занимались раньше?",
                            categoryLabel = "Регулярность",
                            screenTitle = "Расскажите о себе",
                            options = listOf("Не занимался(-ась) регулярно", "Был опыт занятий, но сейчас сделал(-а) перерыв", "Занимаюсь время от времени", "Занимаюсь регулярно на протяжении длительного времени"),
                            selectedEncoded = questionnaireRegularity,
                            onSelectionChange = { questionnaireRegularity = it.substringBefore('|') },
                            progress = 3,
                            onBack = navigateBack,
                            onNext = { destination = AuthDestination.QUESTION_VOLUME },
                            collapsibleOptions = true
                        )

                        AuthDestination.QUESTION_VOLUME -> QuestionnaireOptionsScreen(
                            question = "Какой объём отдельного задания вам комфортно начать выполнять и сколько дней в неделю вы хотели бы выделять на задания?",
                            categoryLabel = "Объём",
                            screenTitle = "Расскажите о себе",
                            options = listOf("До 5 минут", "5–10 минут", "Более 20 минут", "Пока не знаю — хочу начать с самого простого варианта"),
                            selectedEncoded = questionnaireVolume,
                            onSelectionChange = { questionnaireVolume = it.substringBefore('|') },
                            progress = 3,
                            onBack = navigateBack,
                            onNext = { destination = AuthDestination.QUESTION_FORMAT },
                            secondCategoryLabel = "Количество в неделю",
                            secondOptions = listOf("1 день в неделю", "2 дня в неделю", "3 дня в неделю", "4 дня в неделю", "5 дней в неделю", "6–7 дней в неделю"),
                            secondSelectedEncoded = questionnaireDays,
                            onSecondSelectionChange = { questionnaireDays = it },
                            collapsibleOptions = true
                        )

                        AuthDestination.QUESTION_FORMAT -> QuestionnaireOptionsScreen(
                            question = "Какие ограничения формата заданий вы хотите установить?",
                            categoryLabel = "Ограничения",
                            screenTitle = "Расскажите о себе",
                            options = listOf("Без прыжков", "Без бега", "Без спортивного оборудования", "Самостоятельно выбирать тип", "Заниматься на улице", "Заниматься дома", "Другое"),
                            selectedEncoded = questionnaireFormats,
                            onSelectionChange = { questionnaireFormats = it },
                            progress = 4,
                            onBack = navigateBack,
                            onNext = { destination = AuthDestination.QUESTION_GOALS },
                            onSkip = { destination = AuthDestination.QUESTION_GOALS },
                            chips = true,
                            multiple = true,
                            collapsibleOptions = true,
                            showCollapsedSelectionSummary = false
                        )

                        AuthDestination.QUESTION_GOALS -> QuestionnaireOptionsScreen(
                            question = "Какую цель вы хотите поставить перед собой?",
                            description = "Можно выбрать до двух вариантов",
                            options = listOf("Больше двигаться", "Сформировать регулярную привычку", "Чаще делать активные перерывы", "Поддерживать регулярность занятий", "Получать СпортКоины ✦", "Отслеживать собственный прогресс", "Другая цель"),
                            selectedEncoded = questionnaireGoals,
                            onSelectionChange = { questionnaireGoals = it },
                            progress = 4,
                            onBack = navigateBack,
                            onNext = { destination = AuthDestination.QUESTION_MEDICAL },
                            onSkip = { destination = AuthDestination.QUESTION_MEDICAL },
                            multiple = true,
                            maxSelections = 2
                        )

                        AuthDestination.QUESTION_MEDICAL -> QuestionnaireOptionsScreen(
                            question = "Есть ли у вас ограничения на физическую активность, которые установил медицинский специалист?",
                            categoryLabel = "Ограничения",
                            screenTitle = "Расскажите о себе",
                            options = listOf("Нет известных мне ограничений", "Да, ограничения есть", "Не уверен(-а)", "Не хочу отвечать на этот вопрос"),
                            selectedEncoded = questionnaireMedical,
                            onSelectionChange = { questionnaireMedical = it.substringBefore('|') },
                            progress = 5,
                            onBack = navigateBack,
                            onNext = completeOnboarding,
                            collapsibleOptions = true
                        )

                        AuthDestination.AUTH_WELCOME -> AuthWelcomeScreen(
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            onRegisterEmail = {
                                registrationAttempted = false
                                serverRegistrationFieldErrors = emptyMap()
                                destination = AuthDestination.REGISTER
                            },
                            onLogin = { destination = AuthDestination.LOGIN },
                            onGuest = { destination = AuthDestination.INTRO },
                            onProvider = { provider -> notice = "Регистрация через $provider пока не подключена к серверу." },
                            serverBaseUrl = serverBaseUrl,
                            onServerSettings = {
                                serverSettingsReturn = AuthDestination.AUTH_WELCOME
                                serverUrlDraft = serverBaseUrl
                                destination = AuthDestination.SERVER_SETTINGS
                            }
                        )

                        AuthDestination.LOGIN -> LoginScreen(
                            identifier = loginIdentifier,
                            onIdentifierChange = { loginIdentifier = it },
                            password = loginPassword,
                            onPasswordChange = { loginPassword = it },
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            onBack = { destination = AuthDestination.AUTH_WELCOME },
                            onForgotPassword = { destination = AuthDestination.RECOVERY },
                            onRegister = { destination = AuthDestination.REGISTER },
                            onLogin = submitLogin,
                            isSubmitting = loginSubmitting,
                            onSocialAuth = { provider ->
                                notice = "Вход через $provider пока не подключён к серверу."
                            }
                        )

                        AuthDestination.REGISTER -> RegistrationScreen(
                            username = registrationLogin,
                            onUsernameChange = {
                                registrationLogin = it
                                registrationLoginTouched = true
                                serverRegistrationFieldErrors = serverRegistrationFieldErrors - "login"
                            },
                            email = registrationEmail,
                            onEmailChange = {
                                registrationEmail = it
                                registrationEmailTouched = true
                                serverRegistrationFieldErrors = serverRegistrationFieldErrors - "email"
                            },
                            phone = registrationPhone,
                            onPhoneChange = {
                                registrationPhone = it
                                registrationPhoneTouched = true
                                serverRegistrationFieldErrors = serverRegistrationFieldErrors - "phone"
                            },
                            password = registrationPassword,
                            onPasswordChange = {
                                registrationPassword = it
                                registrationPasswordTouched = true
                                serverRegistrationFieldErrors = serverRegistrationFieldErrors - "password"
                            },
                            usernameError = serverRegistrationFieldErrors["login"]
                                ?: if (registrationLoginTouched || registrationAttempted) registrationUsernameError(registrationLogin) else null,
                            emailError = serverRegistrationFieldErrors["email"]
                                ?: if (registrationEmailTouched || registrationAttempted) registrationEmailError(registrationEmail) else null,
                            phoneError = serverRegistrationFieldErrors["phone"]
                                ?: if (registrationPhoneTouched || registrationAttempted) registrationPhoneError(registrationPhone) else null,
                            passwordError = serverRegistrationFieldErrors["password"] ?: if (registrationPasswordTouched || registrationAttempted) {
                                when {
                                    registrationPassword.isBlank() -> "Пароль обязателен"
                                    registrationPassword.length < 8 -> "Длина пароля должна быть не менее 8 символов"
                                    else -> null
                                }
                            } else null,
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            onBack = { destination = AuthDestination.AUTH_WELCOME },
                            onLogin = { destination = AuthDestination.LOGIN },
                            onForgotPassword = { destination = AuthDestination.RECOVERY },
                            onRegister = submitRegistration,
                            onSocialAuth = { provider -> notice = "Регистрация через $provider пока не подключена к серверу." },
                            isSubmitting = registrationSubmitting
                        )

                        AuthDestination.REGISTER_CONTACT -> ContactConfirmationScreen(
                            method = registrationContactMethod,
                            onMethodChange = { method ->
                                registrationContactMethod = method
                                registrationContact = if (method == "Почта") registrationEmail else registrationPhone
                            },
                            contact = registrationContact,
                            onContactChange = {
                                registrationContact = it
                                if (registrationContactMethod == "Почта") registrationEmail = it else registrationPhone = it
                            },
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            onBack = { destination = AuthDestination.REGISTER },
                            onSendCode = {
                                val valid = if (registrationContactMethod == "Почта") registrationEmailError(registrationContact) == null else registrationPhoneError(registrationContact) == null && registrationContact.isNotBlank()
                                if (!valid) {
                                    notice = if (registrationContactMethod == "Почта") "Введите корректный адрес почты." else "Введите телефон в формате +X (XXX) XXX-XX-XX."
                                } else {
                                    registrationVerificationCode = ""
                                    registrationResendKey += 1
                                    destination = AuthDestination.REGISTRATION_VERIFY_CODE
                                }
                            }
                        )

                        AuthDestination.REGISTRATION_VERIFY_CODE -> CodeConfirmationScreen(
                            title = "Подтвердите аккаунт",
                            description = "Пожалуйста, введите код, который был отправлен на\n${registrationContact.ifBlank { registrationEmail }}",
                            code = registrationVerificationCode,
                            onCodeChange = { registrationVerificationCode = it },
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            resendResetKey = registrationResendKey,
                            onResend = { registrationResendKey += 1; registrationVerificationCode = "" },
                            onBack = { destination = AuthDestination.REGISTER_CONTACT },
                            onConfirm = {
                                if (registrationVerificationCode.length < 4) notice = "Введите код подтверждения."
                                else {
                                    loginIdentifier = registrationContact
                                    loginPassword = ""
                                    destination = AuthDestination.LOGIN
                                    notice = "Экран регистрации пройден в демонстрационном режиме. Серверная проверка кода пока не подключена."
                                }
                            }
                        )

                        AuthDestination.RECOVERY -> RecoveryScreen(
                            method = recoveryMethod,
                            onMethodChange = { recoveryMethod = it; recoveryContact = "" },
                            contact = recoveryContact,
                            onContactChange = { recoveryContact = it },
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            onBack = { destination = AuthDestination.LOGIN },
                            onSendCode = {
                                val valid = if (recoveryMethod == "Почта") registrationEmailError(recoveryContact) == null else recoveryContact.count(Char::isDigit) >= 7
                                if (!valid) notice = if (recoveryMethod == "Почта") "Введите корректный адрес почты." else "Введите корректный номер телефона."
                                else {
                                    recoveryVerificationCode = ""
                                    recoveryResendKey += 1
                                    destination = AuthDestination.RECOVERY_VERIFY_CODE
                                }
                            }
                        )

                        AuthDestination.RECOVERY_VERIFY_CODE -> CodeConfirmationScreen(
                            title = "Восстановление пароля",
                            description = "Введите код подтверждения, отправленный на\n$recoveryContact",
                            code = recoveryVerificationCode,
                            onCodeChange = { recoveryVerificationCode = it },
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            resendResetKey = recoveryResendKey,
                            onResend = { recoveryResendKey += 1; recoveryVerificationCode = "" },
                            onBack = { destination = AuthDestination.RECOVERY },
                            onConfirm = {
                                if (recoveryVerificationCode.length < 4) notice = "Введите код подтверждения."
                                else {
                                    destination = AuthDestination.RESET_PASSWORD
                                    notice = "Проверка кода пока демонстрационная: подключение к API восстановления пароля ещё не настроено."
                                }
                            }
                        )

                        AuthDestination.RESET_PASSWORD -> ResetPasswordScreen(
                            password = newPassword,
                            onPasswordChange = { newPassword = it },
                            confirmPassword = confirmPassword,
                            onConfirmPasswordChange = { confirmPassword = it },
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            onBack = { destination = AuthDestination.RECOVERY_VERIFY_CODE },
                            onReset = {
                                when {
                                    newPassword.isBlank() -> notice = "Введите новый пароль."
                                    newPassword.length < 8 -> Unit
                                    newPassword != confirmPassword -> notice = "Пароли не совпадают."
                                    else -> {
                                        destination = AuthDestination.LOGIN
                                        loginPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        notice = "Форма готова, но запрос сброса пароля ещё не подключён к C# API."
                                    }
                                }
                            }
                        )

                        AuthDestination.SERVER_SETTINGS -> ServerSettingsScreen(
                            baseUrl = serverUrlDraft,
                            onBaseUrlChange = { serverUrlDraft = it },
                            onBack = { destination = serverSettingsReturn },
                            onSave = {
                                val normalized = normalizeBaseUrl(serverUrlDraft)
                                if (normalized == null) {
                                    notice = "Введи полный адрес с http:// или https://, например http://10.0.2.2:5000."
                                } else {
                                    serverBaseUrl = normalized
                                    serverUrlDraft = normalized
                                    preferences.edit().putString("api_base_url", normalized).apply()
                                    destination = serverSettingsReturn
                                    notice = "Адрес сервера сохранён: $normalized"
                                }
                            }
                        )

                        AuthDestination.GUEST_HOME -> DemoGuestHome(
                            onBackToAuth = { destination = AuthDestination.AUTH_WELCOME }
                        )

                        AuthDestination.SERVER_HELLO -> ServerHelloScreen(
                            baseUrl = serverBaseUrl,
                            onBackToAuth = { destination = AuthDestination.AUTH_WELCOME }
                        )
                }

                notice?.let { message ->
                    AlertDialog(
                        onDismissRequest = { notice = null },
                        title = { DarkText("Спортакус", color = Color(0xFFF2F5EE)) },
                        text = { DarkText(message, color = Color(0xFFF2F5EE)) },
                        confirmButton = {
                            TextButton(onClick = { notice = null }) { DarkText("Понятно", color = Color(0xFFF2F5EE)) }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = Color(0xFFF2F5EE),
                        textContentColor = Color(0xFFF2F5EE)
                    )
                }
            }
        }
    }
}

private fun normalizeBaseUrl(raw: String): String? {
    val value = raw.trim().trimEnd('/')
    if (!value.startsWith("http://", ignoreCase = true) && !value.startsWith("https://", ignoreCase = true)) return null
    val uri = android.net.Uri.parse(value)
    if (uri.host.isNullOrBlank()) return null
    if (uri.encodedPath.orEmpty().isNotEmpty() && uri.encodedPath != "/") return null
    return value
}

/** Client-side checks mirror the rules described by the C# registration endpoint. */
private fun registrationUsernameError(value: String): String? =
    if (value.isBlank()) "Имя пользователя обязательно" else null

private fun registrationEmailError(value: String): String? {
    if (value.isBlank()) return "Почта обязательна"
    val atIndex = value.lastIndexOf('@')
    val dotIndex = value.lastIndexOf('.')
    val valid = value.contains('@') && value.contains('.') && atIndex >= 0 && dotIndex >= 0 &&
        atIndex <= dotIndex && dotIndex - atIndex >= 2 && value.length - dotIndex >= 2
    return if (valid) null else "Почта должна быть вида example@example.ru"
}

private fun registrationPhoneError(value: String): String? {
    if (value.isEmpty()) return null
    val pattern = Regex("^\\+\\d{1,4} \\(\\d{3}\\) \\d{3}-\\d{2}-\\d{2}$")
    return if (pattern.matches(value)) null else "Телефон должен быть вида +X (XXX) XXX-XX-XX"
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
