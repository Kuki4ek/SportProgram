package com.sportakus.auth.ui

import com.sportakus.auth.ui.theme.sportakusTextColor

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import org.json.JSONObject
import org.json.JSONArray
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.sportakus.auth.data.api.EmailConfirmationApiResult
import com.sportakus.auth.data.api.RegisterRequest
import com.sportakus.auth.data.api.LoginApiRequest
import com.sportakus.auth.data.api.LoginApiResult
import com.sportakus.auth.data.api.PasswordRecoveryApiResult
import com.sportakus.auth.data.api.AuthActionApiResult
import com.sportakus.auth.data.api.UserProfile
import com.sportakus.auth.data.api.CurrentUserApiResult
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
    SERVER_HELLO,
    MAIN_APP,
    WORKOUT_DETAIL,
    ADMIN_LIST,
    ADMIN_EDITOR
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
    var currentUserName by rememberSaveable { mutableStateOf(preferences.getString("profile_display_name", "") ?: "") }
    var currentProfile by remember { mutableStateOf(UserProfile()) }
    var activeWorkoutTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var activeWorkoutSeconds by rememberSaveable { mutableStateOf(0) }
    var currentAccountKey by rememberSaveable {
        mutableStateOf(preferences.getString("active_account_identity", "") ?: "")
    }
    var isGuestSession by rememberSaveable { mutableStateOf(false) }
    val exerciseCatalog = remember(preferences) {
        mutableStateListOf<WorkoutExercise>().apply {
            val savedCatalogV3 = preferences.getString("local_exercise_catalog_v3_json", null)
            val savedCatalogV2 = preferences.getString("local_exercise_catalog_v2_json", null)
            val restored = (savedCatalogV3 ?: savedCatalogV2)?.let(::decodeWorkoutExercises)
                ?.map { exercise ->
                    // Normalize only the four built-in demo records; retain admin-created entries.
                    if (exercise.id.startsWith("demo-")) {
                        exercise.copy(durationMinutes = 0, distance = "—", calories = 0, coins = 0)
                    } else exercise
                }
            addAll(restored?.takeIf { it.isNotEmpty() } ?: demoWorkoutExercises())
        }
    }
    val persistExerciseCatalog: () -> Unit = {
        preferences.edit()
            .putString("local_exercise_catalog_v3_json", encodeWorkoutExercises(exerciseCatalog))
            .apply()
    }
    var selectedExerciseId by rememberSaveable { mutableStateOf("") }
    var editingExerciseId by rememberSaveable { mutableStateOf<String?>(null) }

    // Test server address is editable in the app and persisted across restarts.
    val defaultServerUrl = "http://10.0.2.2:5000"
    var serverBaseUrl by remember {
        mutableStateOf(preferences.getString("api_base_url", defaultServerUrl) ?: defaultServerUrl)
    }
    var serverUrlDraft by rememberSaveable {
        mutableStateOf(preferences.getString("api_base_url", defaultServerUrl) ?: defaultServerUrl)
    }
    var registrationSubmitting by remember { mutableStateOf(false) }
    var registrationCodeRequesting by remember { mutableStateOf(false) }
    var loginSubmitting by remember { mutableStateOf(false) }
    var serverRegistrationFieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    // Shared UI message state must be declared before callbacks that capture it.
    var notice by remember { mutableStateOf<String?>(null) }

    val loadCurrentProfile: () -> Unit = {
        val accessToken = TokenStore.getAccessToken(context)
        val refreshToken = TokenStore.getRefreshToken(context)
        if (!accessToken.isNullOrBlank()) {
            coroutineScope.launch {
                when (val result = withContext(Dispatchers.IO) {
                    AuthApi.getCurrentUserName(serverBaseUrl, accessToken, refreshToken)
                }) {
                    is CurrentUserApiResult.Success -> {
                        currentProfile = result.profile
                        currentUserName = result.profile.displayName
                        if (!result.renewedAccessToken.isNullOrBlank()) {
                            TokenStore.saveTokens(context, result.renewedAccessToken, result.renewedRefreshToken ?: refreshToken)
                        }
                        preferences.edit()
                            .putString(profileDisplayNamePreferenceKey(currentAccountKey.ifBlank { "guest" }), currentUserName)
                            .putString("profile_display_name", currentUserName)
                            .apply()
                    }
                    is CurrentUserApiResult.Failure -> {
                        if (!result.renewedAccessToken.isNullOrBlank()) {
                            TokenStore.saveTokens(context, result.renewedAccessToken, result.renewedRefreshToken ?: refreshToken)
                        }
                        // Do not manufacture profile data on API failure. Unknown fields stay blank/zero.
                        currentProfile = currentProfile.copy(
                            login = currentProfile.login.ifBlank { currentUserName }
                        )
                        notice = result.message
                        if (result.httpStatus == 401) {
                            TokenStore.clear(context)
                            destination = AuthDestination.LOGIN
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(activeWorkoutTitle) {
        if (activeWorkoutTitle.isNullOrBlank()) {
            activeWorkoutSeconds = 0
        } else {
            while (true) {
                delay(1_000)
                activeWorkoutSeconds += 1
            }
        }
    }

    // V2 always uses the light palette; legacy dark-theme preferences are ignored.
    val activeDarkTheme = false

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
    var recoveryCodeToken by rememberSaveable { mutableStateOf("") }
    var recoveryResetToken by rememberSaveable { mutableStateOf("") }
    var recoverySubmitting by rememberSaveable { mutableStateOf(false) }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    // Questionnaire answers are retained locally while the user moves between steps.
    // New accounts start with unanswered questionnaire fields; no sample personal data is preselected.
    var questionnaireReasons by rememberSaveable { mutableStateOf("") }
    var questionnaireHeight by rememberSaveable { mutableStateOf("") }
    var questionnaireWeight by rememberSaveable { mutableStateOf("") }
    var questionnaireGender by rememberSaveable { mutableStateOf("") }
    var birthDay by rememberSaveable { mutableStateOf("") }
    var birthMonth by rememberSaveable { mutableStateOf("") }
    var birthYear by rememberSaveable { mutableStateOf("") }
    var questionnaireIntensity by rememberSaveable { mutableStateOf("") }
    var questionnaireRegularity by rememberSaveable { mutableStateOf("") }
    var questionnaireVolume by rememberSaveable { mutableStateOf("") }
    var questionnaireDays by rememberSaveable { mutableStateOf("") }
    var questionnaireFormats by rememberSaveable { mutableStateOf("") }
    var questionnaireGoals by rememberSaveable { mutableStateOf("") }
    var questionnaireMedical by rememberSaveable { mutableStateOf("") }

    // Start a fresh questionnaire for a newly registered account. Completion is
    // tracked per account, not globally on the device.
    val resetQuestionnaireAnswers: () -> Unit = {
        questionnaireReasons = ""
        questionnaireHeight = ""
        questionnaireWeight = ""
        questionnaireGender = ""
        birthDay = ""
        birthMonth = ""
        birthYear = ""
        questionnaireIntensity = ""
        questionnaireRegularity = ""
        questionnaireVolume = ""
        questionnaireDays = ""
        questionnaireFormats = ""
        questionnaireGoals = ""
        questionnaireMedical = ""
    }

    // Kept as a no-op for compatibility with existing screen signatures.
    val toggleTheme: () -> Unit = {}
    val completeOnboarding: () -> Unit = {
        val accountIdentity = currentAccountKey.ifBlank { "guest" }
        val answerSuffix = accountPreferenceSuffix(accountIdentity)
        preferences.edit()
            .putBoolean(questionnaireCompletedPreferenceKey(accountIdentity), true)
            // Kept for compatibility with older local builds; routing uses the scoped key above.
            .putBoolean("questionnaire_completed", true)
            .putString("questionnaire_${answerSuffix}_reasons", questionnaireReasons)
            .putString("questionnaire_${answerSuffix}_height", questionnaireHeight)
            .putString("questionnaire_${answerSuffix}_weight", questionnaireWeight)
            .putString("questionnaire_${answerSuffix}_gender", questionnaireGender)
            .putString("questionnaire_${answerSuffix}_birth_date", "$birthDay|$birthMonth|$birthYear")
            .putString("questionnaire_${answerSuffix}_intensity", questionnaireIntensity)
            .putString("questionnaire_${answerSuffix}_regularity", questionnaireRegularity)
            .putString("questionnaire_${answerSuffix}_volume", questionnaireVolume)
            .putString("questionnaire_${answerSuffix}_days", questionnaireDays)
            .putString("questionnaire_${answerSuffix}_formats", questionnaireFormats)
            .putString("questionnaire_${answerSuffix}_goals", questionnaireGoals)
            .putString("questionnaire_${answerSuffix}_medical", questionnaireMedical)
            .putString(profileDisplayNamePreferenceKey(accountIdentity), currentUserName)
            .putString("profile_display_name", currentUserName)
            .apply()
        destination = AuthDestination.MAIN_APP
    }

    val navigateBack: () -> Unit = {
        destination = when (destination) {
            AuthDestination.SPLASH -> AuthDestination.SPLASH
            AuthDestination.SAFETY_NOTE -> AuthDestination.SPLASH
            AuthDestination.INTRO -> AuthDestination.SAFETY_NOTE
            AuthDestination.QUESTION_REASON -> AuthDestination.AUTH_WELCOME
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
            AuthDestination.REGISTRATION_VERIFY_CODE -> AuthDestination.REGISTER
            AuthDestination.RECOVERY -> AuthDestination.LOGIN
            AuthDestination.RECOVERY_VERIFY_CODE -> AuthDestination.RECOVERY
            AuthDestination.RESET_PASSWORD -> AuthDestination.RECOVERY_VERIFY_CODE
            AuthDestination.SERVER_SETTINGS -> serverSettingsReturn
            AuthDestination.GUEST_HOME -> AuthDestination.AUTH_WELCOME
            AuthDestination.SERVER_HELLO -> AuthDestination.AUTH_WELCOME
            AuthDestination.MAIN_APP -> AuthDestination.AUTH_WELCOME
            AuthDestination.WORKOUT_DETAIL -> AuthDestination.MAIN_APP
            AuthDestination.ADMIN_LIST -> AuthDestination.MAIN_APP
            AuthDestination.ADMIN_EDITOR -> AuthDestination.ADMIN_LIST
        }
    }

    val submitRegistration: () -> Unit = {
        registrationAttempted = true
        val loginError = registrationUsernameError(registrationLogin)
        val emailError = registrationEmailError(registrationEmail)
        val phoneError = registrationPhoneError(registrationPhone)
        val passwordError = when {
            registrationPassword.isBlank() -> "Пароль обязателен"
            registrationPassword.length < 8 -> "Пароль должен содержать не менее 8 символов"
            else -> null
        }
        val locallyValid = loginError == null && emailError == null && phoneError == null && passwordError == null

        // Step 1: validate locally and ask the server to send an email code.
        // Account creation is intentionally deferred until the user submits that code.
        if (locallyValid && !registrationSubmitting) {
            serverRegistrationFieldErrors = emptyMap()
            notice = null
            registrationSubmitting = true
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    AuthApi.requestEmailConfirmation(
                        baseUrl = serverBaseUrl,
                        login = registrationLogin,
                        email = registrationEmail
                    )
                }
                registrationSubmitting = false
                when (result) {
                    is EmailConfirmationApiResult.Success -> {
                        registrationVerificationCode = ""
                        registrationResendKey += 1
                        registrationContactMethod = "Почта"
                        registrationContact = registrationEmail
                        destination = AuthDestination.REGISTRATION_VERIFY_CODE
                        notice = null
                    }
                    is EmailConfirmationApiResult.Failure -> {
                        serverRegistrationFieldErrors = mapRegistrationFieldErrors(result.fieldErrors)
                        notice = result.message
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
                            credentials = LoginApiRequest(
                                identifier = if (!loginIdentifier.contains('@') && loginIdentifier.all { it.isDigit() || it in "+ ()-" }) {
                                    formatPhoneForRegistrationApi(loginIdentifier) ?: loginIdentifier
                                } else loginIdentifier,
                                password = loginPassword
                            )
                        )
                    }
                    loginSubmitting = false
                    when (result) {
                        is LoginApiResult.Success -> {
                            TokenStore.saveTokens(context, result.jwt, result.refreshToken)
                            isGuestSession = false
                            val tokenIdentity = jwtAccountIdentity(result.jwt, loginIdentifier)
                            currentAccountKey = preferences.getString(accountAliasPreferenceKey(loginIdentifier), null)
                                ?: tokenIdentity
                            preferences.edit().putString("active_account_identity", currentAccountKey).apply()
                            currentUserName = loginIdentifier.substringBefore('@').ifBlank { "Спортсмен" }
                            currentProfile = UserProfile(
                                login = if (loginIdentifier.contains('@')) "" else loginIdentifier,
                                email = loginIdentifier.takeIf { it.contains('@') }.orEmpty()
                            )
                            loadCurrentProfile()
                            val questionnaireDone = preferences.getBoolean(
                                questionnaireCompletedPreferenceKey(currentAccountKey), false
                            )
                            destination = if (questionnaireDone) {
                                AuthDestination.MAIN_APP
                            } else {
                                resetQuestionnaireAnswers()
                                AuthDestination.QUESTION_REASON
                            }
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
            // Every app launch begins with the persisted theme selection.
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

    SportakusTheme(darkTheme = activeDarkTheme) {
        val view = LocalView.current
        val colors = MaterialTheme.colorScheme
        androidx.compose.runtime.SideEffect {
            view.context.findActivity()?.window?.let { window ->
                val barColor = if (destination == AuthDestination.SPLASH) android.graphics.Color.rgb(121, 181, 5) else colors.background.toArgb()
                window.statusBarColor = barColor
                window.navigationBarColor = barColor
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = destination != AuthDestination.SPLASH
                    isAppearanceLightNavigationBars = destination != AuthDestination.SPLASH
                }
            }
        }

        // Recreate screen-local remembered components when route or theme changes.
        // Form data lives in AuthApp state, so credentials and questionnaire answers persist.
        key(destination, activeDarkTheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colors.background,
                contentColor = sportakusTextColor()
            ) {
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
                            onGuest = {
                                isGuestSession = true
                                currentAccountKey = "guest"
                                currentUserName = "Гость"
                                currentProfile = UserProfile()
                                activeWorkoutTitle = null
                                activeWorkoutSeconds = 0
                                destination = AuthDestination.MAIN_APP
                            },
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
                                    registrationPassword.length < 8 -> "Пароль должен содержать не менее 8 символов"
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
                                    notice = if (registrationContactMethod == "Почта") "Введите корректный адрес почты." else "Введите корректный номер с международным кодом страны."
                                } else {
                                    registrationVerificationCode = ""
                                    registrationResendKey += 1
                                    destination = AuthDestination.REGISTRATION_VERIFY_CODE
                                }
                            }
                        )

                        AuthDestination.REGISTRATION_VERIFY_CODE -> CodeConfirmationScreen(
                            title = "Подтверждение почты",
                            description = "Мы отправили шестизначный код подтверждения на\n${registrationEmail}",
                            code = registrationVerificationCode,
                            onCodeChange = { registrationVerificationCode = it },
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            resendResetKey = registrationResendKey,
                            isSubmitting = registrationSubmitting,
                            isResending = registrationCodeRequesting,
                            onResend = {
                                if (!registrationCodeRequesting && !registrationSubmitting) {
                                    registrationCodeRequesting = true
                                    coroutineScope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            AuthApi.requestEmailConfirmation(
                                                baseUrl = serverBaseUrl,
                                                login = registrationLogin,
                                                email = registrationEmail
                                            )
                                        }
                                        registrationCodeRequesting = false
                                        when (result) {
                                            is EmailConfirmationApiResult.Success -> {
                                                registrationVerificationCode = ""
                                                registrationResendKey += 1
                                                notice = null
                                            }
                                            is EmailConfirmationApiResult.Failure -> {
                                                notice = result.message
                                            }
                                        }
                                    }
                                }
                            },
                            onBack = {
                                registrationVerificationCode = ""
                                destination = AuthDestination.REGISTER
                            },
                            onConfirm = {
                                when {
                                    registrationVerificationCode.length != 6 -> notice = "Введите все 6 цифр кода из письма."
                                    registrationSubmitting || registrationCodeRequesting -> Unit
                                    else -> {
                                        notice = null
                                        registrationSubmitting = true
                                        coroutineScope.launch {
                                            val result = withContext(Dispatchers.IO) {
                                                AuthApi.register(
                                                    baseUrl = serverBaseUrl,
                                                    registration = RegisterRequest(
                                                        login = registrationLogin,
                                                        email = registrationEmail,
                                                        password = registrationPassword,
                                                        emailCode = registrationVerificationCode,
                                                        phone = registrationPhone.takeIf { it.isNotBlank() }
                                                            ?.let { formatPhoneForRegistrationApi(it) }
                                                    )
                                                )
                                            }
                                            registrationSubmitting = false
                                            when (result) {
                                                is RegisterApiResult.Success -> {
                                                    TokenStore.saveTokens(context, result.jwt, result.refreshToken)
                                                    isGuestSession = false
                                                    currentAccountKey = jwtAccountIdentity(result.jwt, registrationEmail)
                                                    currentUserName = registrationLogin.trim().ifBlank { "Спортсмен" }
                                                    currentProfile = UserProfile(
                                                        login = registrationLogin.trim(),
                                                        email = registrationEmail.trim(),
                                                        phone = registrationPhone.takeIf { it.isNotBlank() }?.let { formatPhoneForRegistrationApi(it).orEmpty() }.orEmpty()
                                                    )
                                                    loadCurrentProfile()
                                                    preferences.edit()
                                                        .putString("active_account_identity", currentAccountKey)
                                                        .putString(profileDisplayNamePreferenceKey(currentAccountKey), currentUserName)
                                                        .putString(accountAliasPreferenceKey(registrationLogin), currentAccountKey)
                                                        .putString(accountAliasPreferenceKey(registrationEmail), currentAccountKey)
                                                        .putString(accountAliasPreferenceKey(registrationPhone), currentAccountKey)
                                                        .putString("profile_display_name", currentUserName)
                                                        .apply()
                                                    val questionnaireDone = preferences.getBoolean(
                                                        questionnaireCompletedPreferenceKey(currentAccountKey), false
                                                    )
                                                    destination = if (questionnaireDone) {
                                                        AuthDestination.MAIN_APP
                                                    } else {
                                                        resetQuestionnaireAnswers()
                                                        AuthDestination.QUESTION_REASON
                                                    }
                                                    notice = null
                                                }
                                                is RegisterApiResult.Failure -> {
                                                    val mappedErrors = mapRegistrationFieldErrors(result.fieldErrors)
                                                    serverRegistrationFieldErrors = mappedErrors
                                                    val details = result.fieldErrors.values.flatten().distinct().joinToString("\n")
                                                    notice = if (details.isBlank()) result.message else "${result.message}\n$details"
                                                    if (result.accountCreated) {
                                                        // Do not submit registration a second time: the backend may already have created the account.
                                                        loginIdentifier = registrationEmail
                                                        loginPassword = registrationPassword
                                                        destination = AuthDestination.LOGIN
                                                    } else if (mappedErrors.keys.any { it !in setOf("code", "emailcode") }) {
                                                        destination = AuthDestination.REGISTER
                                                    }
                                                }
                                            }
                                        }
                                    }
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
                                val kind = when (recoveryMethod) {
                                    "Почта" -> "email"
                                    "Телефон" -> "phone"
                                    else -> "login"
                                }
                                val locallyValid = when (kind) {
                                    "email" -> registrationEmailError(recoveryContact) == null
                                    "phone" -> recoveryContact.isNotBlank() && isCompleteInternationalPhone(recoveryContact)
                                    else -> recoveryContact.isNotBlank()
                                }
                                if (!locallyValid) {
                                    notice = when (kind) {
                                        "email" -> "Введите корректный адрес почты."
                                        "phone" -> "Введите корректный номер с международным кодом страны."
                                        else -> "Введите имя пользователя."
                                    }
                                } else if (!recoverySubmitting) {
                                    recoverySubmitting = true
                                    notice = null
                                    coroutineScope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            AuthApi.requestPasswordReset(
                                                baseUrl = serverBaseUrl,
                                                contactKind = kind,
                                                identifier = if (kind == "phone") {
                                                    formatPhoneForRegistrationApi(recoveryContact) ?: recoveryContact
                                                } else recoveryContact.trim()
                                            )
                                        }
                                        recoverySubmitting = false
                                        when (result) {
                                            is PasswordRecoveryApiResult.Success -> {
                                                if (result.codeToken.isNullOrBlank()) {
                                                    notice = "Сервер отправил ответ без codeToken. Проверь контракт forgot-password/request."
                                                } else {
                                                    recoveryCodeToken = result.codeToken
                                                    recoveryResetToken = ""
                                                    recoveryVerificationCode = ""
                                                    recoveryResendKey += 1
                                                    destination = AuthDestination.RECOVERY_VERIFY_CODE
                                                }
                                            }
                                            is PasswordRecoveryApiResult.Failure -> notice = result.message
                                        }
                                    }
                                }
                            }
                        )

                        AuthDestination.RECOVERY_VERIFY_CODE -> CodeConfirmationScreen(
                            title = "Восстановление пароля",
                            description = "Введите код подтверждения, отправленный на\n${if (recoveryMethod == "Телефон") formatInternationalPhone(recoveryContact) else recoveryContact}",
                            code = recoveryVerificationCode,
                            onCodeChange = { recoveryVerificationCode = it.filter(Char::isDigit).take(8) },
                            darkTheme = activeDarkTheme,
                            onToggleTheme = toggleTheme,
                            resendResetKey = recoveryResendKey,
                            isSubmitting = recoverySubmitting,
                            onResend = {
                                if (!recoverySubmitting) {
                                    recoverySubmitting = true
                                    coroutineScope.launch {
                                        val kind = when (recoveryMethod) {
                                            "Почта" -> "email"
                                            "Телефон" -> "phone"
                                            else -> "login"
                                        }
                                        val resendResult = withContext(Dispatchers.IO) {
                                            AuthApi.requestPasswordReset(
                                                baseUrl = serverBaseUrl,
                                                contactKind = kind,
                                                identifier = if (kind == "phone") formatPhoneForRegistrationApi(recoveryContact) ?: recoveryContact else recoveryContact.trim()
                                            )
                                        }
                                        recoverySubmitting = false
                                        when (resendResult) {
                                            is PasswordRecoveryApiResult.Success -> {
                                                if (!resendResult.codeToken.isNullOrBlank()) {
                                                    recoveryCodeToken = resendResult.codeToken
                                                    recoveryVerificationCode = ""
                                                    recoveryResendKey += 1
                                                    notice = null
                                                } else notice = "Сервер не вернул codeToken для повторной отправки."
                                            }
                                            is PasswordRecoveryApiResult.Failure -> notice = resendResult.message
                                        }
                                    }
                                }
                            },
                            onBack = { destination = AuthDestination.RECOVERY },
                            onConfirm = {
                                if (recoveryVerificationCode.length != 6) {
                                    notice = "Введите все 6 цифр кода подтверждения."
                                } else if (!recoverySubmitting) {
                                    recoverySubmitting = true
                                    coroutineScope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            AuthApi.verifyPasswordResetCode(
                                                baseUrl = serverBaseUrl,
                                                contactKind = when (recoveryMethod) {
                                                    "Почта" -> "email"
                                                    "Телефон" -> "phone"
                                                    else -> "login"
                                                },
                                                identifier = if (recoveryMethod == "Телефон") {
                                                    formatPhoneForRegistrationApi(recoveryContact) ?: recoveryContact
                                                } else recoveryContact.trim(),
                                                codeToken = recoveryCodeToken,
                                                emailCode = recoveryVerificationCode
                                            )
                                        }
                                        recoverySubmitting = false
                                        when (result) {
                                            is PasswordRecoveryApiResult.Success -> {
                                                recoveryResetToken = result.resetToken.orEmpty()
                                                destination = AuthDestination.RESET_PASSWORD
                                                notice = null
                                            }
                                            is PasswordRecoveryApiResult.Failure -> notice = result.message
                                        }
                                    }
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
                                    recoverySubmitting -> Unit
                                    else -> {
                                        recoverySubmitting = true
                                        notice = null
                                        coroutineScope.launch {
                                            val result = withContext(Dispatchers.IO) {
                                                AuthApi.resetPassword(
                                                    baseUrl = serverBaseUrl,
                                                    contactKind = when (recoveryMethod) {
                                                        "Почта" -> "email"
                                                        "Телефон" -> "phone"
                                                        else -> "login"
                                                    },
                                                    identifier = if (recoveryMethod == "Телефон") {
                                                        formatPhoneForRegistrationApi(recoveryContact) ?: recoveryContact
                                                    } else recoveryContact.trim(),
                                                    resetPasswordToken = recoveryResetToken,
                                                    newPassword = newPassword
                                                )
                                            }
                                            recoverySubmitting = false
                                            when (result) {
                                                is AuthActionApiResult.Success -> {
                                                    destination = AuthDestination.LOGIN
                                                    loginPassword = ""
                                                    newPassword = ""
                                                    confirmPassword = ""
                                                    recoveryCodeToken = ""
                                                    recoveryResetToken = ""
                                                    recoveryVerificationCode = ""
                                                    notice = "Пароль успешно изменён. Теперь войдите с новым паролем."
                                                }
                                                is AuthActionApiResult.Failure -> notice = result.message
                                            }
                                        }
                                    }
                                }
                            }
                        )

                        AuthDestination.SERVER_SETTINGS -> ServerSettingsScreen(
                            darkTheme = activeDarkTheme,
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

                        AuthDestination.MAIN_APP -> MainAppScreen(
                            username = currentUserName,
                            profile = currentProfile,
                            exercises = exerciseCatalog.toList(),
                            activeWorkoutTitle = activeWorkoutTitle,
                            activeWorkoutSeconds = activeWorkoutSeconds,
                            onFinishWorkout = {
                                activeWorkoutTitle = null
                                activeWorkoutSeconds = 0
                            },
                            isAdmin = com.sportakus.auth.BuildConfig.DEBUG || jwtHasAdminRole(TokenStore.getJwt(context)),
                            onOpenExercise = { id ->
                                selectedExerciseId = id
                                destination = AuthDestination.WORKOUT_DETAIL
                            },
                            onOpenAdmin = { destination = AuthDestination.ADMIN_LIST },
                            onLogout = {
                                val accessToken = TokenStore.getAccessToken(context)
                                val refreshToken = TokenStore.getRefreshToken(context)
                                coroutineScope.launch {
                                    if (!accessToken.isNullOrBlank()) {
                                        withContext(Dispatchers.IO) {
                                            AuthApi.logout(serverBaseUrl, accessToken, refreshToken)
                                        }
                                    }
                                    TokenStore.clear(context)
                                    isGuestSession = false
                                    currentAccountKey = ""
                                    currentProfile = UserProfile()
                                    currentUserName = ""
                                    activeWorkoutTitle = null
                                    activeWorkoutSeconds = 0
                                    preferences.edit().remove("active_account_identity").apply()
                                    destination = AuthDestination.AUTH_WELCOME
                                }
                            },
                            onProfileSave = { updatedProfile, onCompleted ->
                                val accessToken = TokenStore.getAccessToken(context)
                                if (accessToken.isNullOrBlank()) {
                                    notice = "Для сохранения профиля необходимо войти в аккаунт."
                                    onCompleted(false)
                                } else {
                                    coroutineScope.launch {
                                        when (val result = withContext(Dispatchers.IO) {
                                            AuthApi.updateCurrentUserProfile(serverBaseUrl, accessToken, updatedProfile, TokenStore.getRefreshToken(context))
                                        }) {
                                            is AuthActionApiResult.Success -> {
                                                if (!result.renewedAccessToken.isNullOrBlank()) {
                                                    TokenStore.saveTokens(context, result.renewedAccessToken, result.renewedRefreshToken ?: TokenStore.getRefreshToken(context))
                                                }
                                                currentProfile = updatedProfile
                                                currentUserName = updatedProfile.displayName
                                                preferences.edit()
                                                    .putString(profileDisplayNamePreferenceKey(currentAccountKey.ifBlank { "guest" }), currentUserName)
                                                    .putString("profile_display_name", currentUserName)
                                                    .apply()
                                                onCompleted(true)
                                            }
                                            is AuthActionApiResult.Failure -> {
                                                if (!result.renewedAccessToken.isNullOrBlank()) {
                                                    TokenStore.saveTokens(context, result.renewedAccessToken, result.renewedRefreshToken ?: TokenStore.getRefreshToken(context))
                                                }
                                                if (result.httpStatus == 401) {
                                                    TokenStore.clear(context)
                                                    destination = AuthDestination.LOGIN
                                                }
                                                notice = result.message
                                                onCompleted(false)
                                            }
                                        }
                                    }
                                }
                            }
                        )

                        AuthDestination.WORKOUT_DETAIL -> {
                            val exercise = exerciseCatalog.firstOrNull { it.id == selectedExerciseId }
                            if (exercise == null) {
                                destination = AuthDestination.MAIN_APP
                            } else {
                                WorkoutDetailScreen(
                                    exercise = exercise,
                                    onBack = { destination = AuthDestination.MAIN_APP },
                                    onStart = {
                                        activeWorkoutTitle = exercise.title
                                        activeWorkoutSeconds = 0
                                        destination = AuthDestination.MAIN_APP
                                    }
                                )
                            }
                        }

                        AuthDestination.ADMIN_LIST -> AdminExerciseListScreen(
                            exercises = exerciseCatalog.toList(),
                            onBack = { destination = AuthDestination.MAIN_APP },
                            onAdd = { editingExerciseId = null; destination = AuthDestination.ADMIN_EDITOR },
                            onEdit = { id -> editingExerciseId = id; destination = AuthDestination.ADMIN_EDITOR },
                            onToggleVisibility = { id ->
                                val index = exerciseCatalog.indexOfFirst { it.id == id }
                                if (index >= 0) {
                                    exerciseCatalog[index] = exerciseCatalog[index].copy(published = !exerciseCatalog[index].published)
                                    persistExerciseCatalog()
                                }
                            },
                            onDelete = { id ->
                                exerciseCatalog.removeAll { it.id == id }
                                persistExerciseCatalog()
                            }
                        )

                        AuthDestination.ADMIN_EDITOR -> AdminExerciseEditorScreen(
                            initial = editingExerciseId?.let { id -> exerciseCatalog.firstOrNull { it.id == id } },
                            onCancel = { destination = AuthDestination.ADMIN_LIST },
                            onSave = { updated ->
                                val index = exerciseCatalog.indexOfFirst { it.id == updated.id }
                                if (index >= 0) exerciseCatalog[index] = updated else exerciseCatalog.add(0, updated)
                                persistExerciseCatalog()
                                destination = AuthDestination.ADMIN_LIST
                                notice = if (editingExerciseId == null) "Упражнение добавлено в локальный список предпросмотра." else "Изменения сохранены в локальном списке предпросмотра."
                            }
                        )

                        AuthDestination.SERVER_HELLO -> ServerHelloScreen(
                            baseUrl = serverBaseUrl,
                            onBackToAuth = { destination = AuthDestination.AUTH_WELCOME }
                        )
                }

                notice?.let { message ->
                    AlertDialog(
                        onDismissRequest = { notice = null },
                        title = { DarkText("Спортакус", color = sportakusTextColor()) },
                        text = { DarkText(message, color = sportakusTextColor()) },
                        confirmButton = {
                            TextButton(onClick = { notice = null }) { DarkText("Понятно", color = sportakusTextColor()) }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = sportakusTextColor(),
                        textContentColor = sportakusTextColor()
                    )
                }
                }
            }
        }
    }
}

private fun accountPreferenceSuffix(identity: String): String {
    val normalized = identity.trim().lowercase().ifBlank { "guest" }
    return Base64.encodeToString(
        normalized.toByteArray(Charsets.UTF_8),
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    )
}

private fun questionnaireCompletedPreferenceKey(identity: String): String =
    "questionnaire_completed_${accountPreferenceSuffix(identity)}"

private fun profileDisplayNamePreferenceKey(identity: String): String =
    "profile_display_name_${accountPreferenceSuffix(identity)}"

private fun accountAliasPreferenceKey(identifier: String): String =
    "account_identity_alias_${accountPreferenceSuffix(identifier)}"

/** Prefer a stable server identity claim; fall back to the login/email entered by the user. */
private fun jwtAccountIdentity(token: String?, fallback: String): String {
    if (!token.isNullOrBlank()) {
        try {
            val parts = token.split('.')
            if (parts.size >= 2) {
                val payloadBytes = Base64.decode(
                    parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                )
                val payload = JSONObject(String(payloadBytes, Charsets.UTF_8))
                val allKeys = mutableListOf<String>()
                val iterator = payload.keys()
                while (iterator.hasNext()) allKeys += iterator.next()
                val preferred = listOf("email", "sub", "nameidentifier", "preferred_username", "unique_name")
                for (claim in preferred) {
                    val key = allKeys.firstOrNull {
                        it.equals(claim, ignoreCase = true) || it.endsWith("/$claim", ignoreCase = true)
                    } ?: if (claim == "nameidentifier") allKeys.firstOrNull {
                        it.contains("nameidentifier", ignoreCase = true)
                    } else null
                    if (key != null) {
                        val value = payload.optString(key).trim()
                        if (value.isNotBlank() && !value.equals("null", ignoreCase = true)) {
                            return value.lowercase()
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Invalid or opaque token: use the user-entered identifier below.
        }
    }
    return fallback.trim().lowercase().ifBlank { "guest" }
}

private fun encodeWorkoutExercises(exercises: List<WorkoutExercise>): String {
    val json = JSONArray()
    exercises.forEach { exercise ->
        json.put(
            JSONObject()
                .put("id", exercise.id)
                .put("title", exercise.title)
                .put("shortDescription", exercise.shortDescription)
                .put("description", exercise.description)
                .put("durationMinutes", exercise.durationMinutes)
                .put("distance", exercise.distance)
                .put("calories", exercise.calories)
                .put("coins", exercise.coins)
                .put("contraindications", exercise.contraindications)
                .put("tips", exercise.tips)
                .put("steps", exercise.steps)
                .put("published", exercise.published)
                .put("intensity", exercise.intensity)
                .put("category", exercise.category)
        )
    }
    return json.toString()
}

private fun decodeWorkoutExercises(raw: String): List<WorkoutExercise>? = try {
    val json = JSONArray(raw)
    buildList {
        for (index in 0 until json.length()) {
            val item = json.optJSONObject(index) ?: continue
            add(
                WorkoutExercise(
                    id = item.optString("id").ifBlank { "local-$index" },
                    title = item.optString("title"),
                    shortDescription = item.optString("shortDescription"),
                    description = item.optString("description"),
                    durationMinutes = item.optInt("durationMinutes", 0),
                    distance = item.optString("distance", "—"),
                    calories = item.optInt("calories", 0),
                    coins = item.optInt("coins", 0),
                    contraindications = item.optString("contraindications"),
                    tips = item.optString("tips"),
                    steps = item.optString("steps"),
                    published = item.optBoolean("published", false),
                    intensity = item.optString("intensity", ""),
                    category = item.optString("category", "Другое")
                )
            )
        }
    }
} catch (_: Exception) {
    null
}

private fun mapRegistrationFieldErrors(errors: Map<String, List<String>>): Map<String, String> =
    errors.entries.associate { (key, messages) ->
        val normalizedKey = when (key.lowercase()) {
            "username", "login" -> "login"
            "email", "mail" -> "email"
            "password" -> "password"
            "phone", "phonenumber", "phone_number" -> "phone"
            "code", "emailcode", "email_code", "confirmcode", "confirmationcode", "verificationcode" -> "code"
            else -> key.lowercase()
        }
        normalizedKey to messages.joinToString("\n")
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
    if (value.isBlank()) return null
    return if (isCompleteInternationalPhone(value)) null else "Введите корректный номер с международным кодом страны."
}

/** Read standard ASP.NET/JWT role claim variants. UI visibility is not an authorization boundary. */
private fun jwtHasAdminRole(token: String?): Boolean {
    if (token.isNullOrBlank()) return false
    return try {
        val parts = token.split('.')
        if (parts.size < 2) return false
        val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val payload = JSONObject(String(payloadBytes, Charsets.UTF_8))
        val keys = payload.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val normalized = key.lowercase()
            val value = payload.opt(key)
            if (normalized.contains("role") && valueContainsAdmin(value)) return true
            if ((normalized == "isadmin" || normalized == "is_admin") && value == true) return true
        }
        false
    } catch (_: Exception) {
        false
    }
}

private fun valueContainsAdmin(value: Any?): Boolean = when (value) {
    is String -> value.split(',', ';', ' ').any { it.equals("admin", ignoreCase = true) }
    is org.json.JSONArray -> (0 until value.length()).any { valueContainsAdmin(value.opt(it)) }
    is JSONObject -> {
        val keys = value.keys()
        var found = false
        while (keys.hasNext() && !found) {
            val key = keys.next()
            if (key.contains("role", ignoreCase = true) || key.equals("name", ignoreCase = true)) {
                found = valueContainsAdmin(value.opt(key))
            }
        }
        found
    }
    else -> false
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
