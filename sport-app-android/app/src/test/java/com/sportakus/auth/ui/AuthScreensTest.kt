package com.sportakus.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import com.sportakus.auth.ui.theme.SportakusTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * UI-тесты авторизационных экранов.
 *
 * Экраны принимают всё состояние через параметры (state hoisting),
 * поэтому каждый тест подставляет собственные флаги/состояния
 * и проверяет, что клики и ввод доходят до колбэков.
 *
 * Требуемые зависимости (см. FOR_LEAD_DEVELOPER.md):
 * - org.robolectric:robolectric
 * - androidx.test.ext:junit + androidx.test:core-ktx (createComposeRule)
 * - androidx.compose.ui:ui-test-junit4 + debugImplementation ui-test-manifest
 * - testOptions.unitTests.isIncludeAndroidResources = true
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ------------------------------------------------------------------
    // LoginScreen
    // ------------------------------------------------------------------

    @Test
    fun loginScreenShowsCoreLabels() {
        composeRule.setContent {
            SportakusTheme {
                LoginScreen(
                    identifier = "",
                    onIdentifierChange = {},
                    password = "",
                    onPasswordChange = {},
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onForgotPassword = {},
                    onRegister = {},
                    onLogin = {},
                    onSocialAuth = {}
                )
            }
        }

        composeRule.onNodeWithText("Пройти авторизацию в Спортакус").assertExists()
        composeRule.onNodeWithText("Логин, почта или телефон").assertExists()
        composeRule.onNodeWithText("Пароль").assertExists()
        composeRule.onNodeWithText("Забыли пароль?").assertExists()
        composeRule.onNodeWithText("Или войти через").assertExists()
        composeRule.onNodeWithText("Войти через Google").assertExists()
        composeRule.onNodeWithText("Войти через Яндекс").assertExists()
        composeRule.onNodeWithText("Войти через почту").assertExists()
        composeRule.onNodeWithText("Нет аккаунта?").assertExists()
        composeRule.onNodeWithText("Регистрация").assertExists()
        // Текст «Войти» показывают и заголовок, и кнопка входа.
        composeRule.onAllNodesWithText("Войти").assertCountEquals(2)
    }

    @Test
    fun loginScreenClickHandlersInvokeCallbacks() {
        var loginClicked = false
        var forgotPasswordClicked = false
        var registerClicked = false
        val socialProviders = mutableListOf<String>()

        composeRule.setContent {
            SportakusTheme {
                LoginScreen(
                    identifier = "",
                    onIdentifierChange = {},
                    password = "",
                    onPasswordChange = {},
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onForgotPassword = { forgotPasswordClicked = true },
                    onRegister = { registerClicked = true },
                    onLogin = { loginClicked = true },
                    onSocialAuth = { socialProviders.add(it) }
                )
            }
        }

        // Кнопка входа — единственный КЛИКАБЕЛЬНЫЙ узел с текстом «Войти»
        // (заголовок «Войти» не обрабатывает клики).
        // Клик выполняется через семантическое действие, а не performClick():
        // окно Robolectric по умолчанию маленькое (320x480), и кнопки ниже
        // вьюпорта не получают синтезированное касание.
        val loginButton = composeRule.onAllNodes(hasText("Войти") and hasClickAction())
        loginButton.assertCountEquals(1)
        loginButton.onFirst().performSemanticsAction(SemanticsActions.OnClick)

        composeRule.onNodeWithText("Забыли пароль?").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("Регистрация").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("Войти через Google").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("Войти через Яндекс").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("Войти через почту").performSemanticsAction(SemanticsActions.OnClick)

        composeRule.runOnIdle {
            assertTrue("Кнопка «Войти» должна вызывать onLogin", loginClicked)
            assertTrue("Ссылка «Забыли пароль?» должна вызывать onForgotPassword", forgotPasswordClicked)
            assertTrue("Ссылка «Регистрация» должна вызывать onRegister", registerClicked)
            assertEquals(
                "Соцкнопки должны передавать провайдера в onSocialAuth",
                listOf("Google", "Яндекс", "почту"),
                socialProviders
            )
        }
    }

    @Test
    fun loginScreenSubmittingDisablesLoginButton() {
        var loginClicked = false

        composeRule.setContent {
            SportakusTheme {
                LoginScreen(
                    identifier = "",
                    onIdentifierChange = {},
                    password = "",
                    onPasswordChange = {},
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onForgotPassword = {},
                    onRegister = {},
                    onLogin = { loginClicked = true },
                    onSocialAuth = {},
                    isSubmitting = true
                )
            }
        }

        // Во время отправки кнопка меняет текст и становится неактивной.
        composeRule.onNodeWithText("Входим...").assertExists().assertIsNotEnabled()
        composeRule.runOnIdle { assertFalse(loginClicked) }
    }

    @Test
    fun loginScreenForwardsTypingToStateHoisting() {
        var identifier by mutableStateOf("")
        var password by mutableStateOf("")

        composeRule.setContent {
            SportakusTheme {
                LoginScreen(
                    identifier = identifier,
                    onIdentifierChange = { identifier = it },
                    password = password,
                    onPasswordChange = { password = it },
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onForgotPassword = {},
                    onRegister = {},
                    onLogin = {},
                    onSocialAuth = {}
                )
            }
        }

        // Поля ввода появляются в порядке отрисовки: [0] — логин, [1] — пароль.
        val textFields = composeRule.onAllNodes(hasSetTextAction())
        textFields[0].performTextInput("user@example.com")
        textFields[1].performTextInput("abcd1234")

        composeRule.runOnIdle {
            assertEquals("Ввод логина должен попадать в onIdentifierChange", "user@example.com", identifier)
            assertEquals("Ввод пароля должен попадать в onPasswordChange", "abcd1234", password)
        }

        // Под полем пароля появляется подсказка о подходящей длине.
        composeRule.onNodeWithText("Длина пароля подходит").assertExists()
    }

    @Test
    fun loginScreenShowsMinLengthHintForShortPassword() {
        var password by mutableStateOf("")

        composeRule.setContent {
            SportakusTheme {
                LoginScreen(
                    identifier = "",
                    onIdentifierChange = {},
                    password = password,
                    onPasswordChange = { password = it },
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onForgotPassword = {},
                    onRegister = {},
                    onLogin = {},
                    onSocialAuth = {}
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("abc")
        composeRule.onNodeWithText("Минимум 8 символов").assertExists()
    }

    // ------------------------------------------------------------------
    // RegistrationScreen
    // ------------------------------------------------------------------

    @Test
    fun registrationScreenShowsLabels() {
        composeRule.setContent {
            SportakusTheme {
                RegistrationScreen(
                    username = "",
                    onUsernameChange = {},
                    email = "",
                    onEmailChange = {},
                    phone = "",
                    onPhoneChange = {},
                    password = "",
                    onPasswordChange = {},
                    usernameError = null,
                    emailError = null,
                    phoneError = null,
                    passwordError = null,
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onLogin = {},
                    onForgotPassword = {},
                    onRegister = {},
                    onSocialAuth = {}
                )
            }
        }

        composeRule.onNodeWithText("Логин").assertExists()
        composeRule.onNodeWithText("Почта").assertExists()
        composeRule.onNodeWithText("Телефон (необязательно)").assertExists()
        composeRule.onNodeWithText("Пароль").assertExists()
        composeRule.onNodeWithText("Забыли пароль?").assertExists()
        composeRule.onNodeWithText("Или зарегистрироваться через").assertExists()
        composeRule.onNodeWithText("Регистрация через Google").assertExists()
        composeRule.onNodeWithText("Регистрация через Яндекс").assertExists()
        composeRule.onNodeWithText("Уже есть аккаунт?").assertExists()
        composeRule.onNodeWithText("Войти").assertExists()
        // Текст «Регистрация» показывают и заголовок, и кнопка отправки.
        composeRule.onAllNodesWithText("Регистрация").assertCountEquals(2)
    }

    @Test
    fun registrationScreenShowsValidationErrors() {
        composeRule.setContent {
            SportakusTheme {
                RegistrationScreen(
                    username = "",
                    onUsernameChange = {},
                    email = "",
                    onEmailChange = {},
                    phone = "",
                    onPhoneChange = {},
                    password = "",
                    onPasswordChange = {},
                    usernameError = "Логин обязателен",
                    emailError = "Почта обязательна",
                    phoneError = "Введите корректный номер с международным кодом страны.",
                    passwordError = "Пароль не соответствует требованиям.",
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onLogin = {},
                    onForgotPassword = {},
                    onRegister = {},
                    onSocialAuth = {}
                )
            }
        }

        composeRule.onNodeWithText("Логин обязателен").assertExists()
        composeRule.onNodeWithText("Почта обязательна").assertExists()
        composeRule.onNodeWithText("Введите корректный номер с международным кодом страны.").assertExists()
        composeRule.onNodeWithText("Пароль не соответствует требованиям.").assertExists()
    }

    @Test
    fun registrationScreenClickHandlersInvokeCallbacks() {
        var forgotPasswordClicked = false
        var loginClicked = false
        var registerClicked = false
        var socialProvider: String? = null

        composeRule.setContent {
            SportakusTheme {
                RegistrationScreen(
                    username = "",
                    onUsernameChange = {},
                    email = "",
                    onEmailChange = {},
                    phone = "",
                    onPhoneChange = {},
                    password = "",
                    onPasswordChange = {},
                    usernameError = null,
                    emailError = null,
                    phoneError = null,
                    passwordError = null,
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onLogin = { loginClicked = true },
                    onForgotPassword = { forgotPasswordClicked = true },
                    onRegister = { registerClicked = true },
                    onSocialAuth = { socialProvider = it }
                )
            }
        }

        // Кнопка — единственный кликабельный узел с текстом «Регистрация».
        // Клик по семантическому действию: кнопки/ссылки ниже вьюпорта
        // Robolectric-окна не получают синтезированное касание из performClick().
        val registerButton = composeRule.onAllNodes(hasText("Регистрация") and hasClickAction())
        registerButton.assertCountEquals(1)
        registerButton.onFirst().performSemanticsAction(SemanticsActions.OnClick)

        composeRule.onNodeWithText("Забыли пароль?").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("Войти").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("Регистрация через Google").performSemanticsAction(SemanticsActions.OnClick)

        composeRule.runOnIdle {
            assertTrue("Кнопка «Регистрация» должна вызывать onRegister", registerClicked)
            assertTrue("Ссылка «Забыли пароль?» должна вызывать onForgotPassword", forgotPasswordClicked)
            assertTrue("Ссылка «Войти» в футере должна вызывать onLogin", loginClicked)
            assertEquals("Соцкнопка должна передавать провайдера в onSocialAuth", "Google", socialProvider)
        }
    }

    @Test
    fun registrationScreenSubmittingDisablesRegisterButton() {
        var registerClicked = false

        composeRule.setContent {
            SportakusTheme {
                RegistrationScreen(
                    username = "",
                    onUsernameChange = {},
                    email = "",
                    onEmailChange = {},
                    phone = "",
                    onPhoneChange = {},
                    password = "",
                    onPasswordChange = {},
                    usernameError = null,
                    emailError = null,
                    phoneError = null,
                    passwordError = null,
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onLogin = {},
                    onForgotPassword = {},
                    onRegister = { registerClicked = true },
                    onSocialAuth = {},
                    isSubmitting = true
                )
            }
        }

        composeRule.onNodeWithText("Отправка...").assertExists().assertIsNotEnabled()
        composeRule.runOnIdle { assertFalse(registerClicked) }
    }

    // ------------------------------------------------------------------
    // ResetPasswordScreen
    // ------------------------------------------------------------------

    @Test
    fun resetPasswordScreenShowsLabels() {
        composeRule.setContent {
            SportakusTheme {
                ResetPasswordScreen(
                    password = "",
                    onPasswordChange = {},
                    confirmPassword = "",
                    onConfirmPasswordChange = {},
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onReset = {}
                )
            }
        }

        composeRule.onNodeWithText("Придумайте пароль").assertExists()
        composeRule.onNodeWithText("Используйте от 8 символов, заглавные и строчные латинские буквы, цифру и спецсимвол.").assertExists()
        composeRule.onNodeWithText("Повторите пароль").assertExists()
        composeRule.onNodeWithText("Сохранить пароль").assertExists()
        // Текст «Новый пароль» показывают и заголовок, и плейсхолдер поля.
        composeRule.onAllNodesWithText("Новый пароль").assertCountEquals(2)
    }

    @Test
    fun resetPasswordScreenShowsValidationErrorAndInvokesReset() {
        var resetClicked = false

        composeRule.setContent {
            SportakusTheme {
                ResetPasswordScreen(
                    password = "",
                    onPasswordChange = {},
                    confirmPassword = "",
                    onConfirmPasswordChange = {},
                    darkTheme = false,
                    onToggleTheme = {},
                    onBack = {},
                    onReset = { resetClicked = true },
                    passwordValidationError = "Пароли не совпадают."
                )
            }
        }

        composeRule.onNodeWithText("Пароли не совпадают.").assertExists()
        composeRule.onNodeWithText("Сохранить пароль").performClick()
        composeRule.runOnIdle { assertTrue(resetClicked) }
    }

    // ------------------------------------------------------------------
    // ServerSettingsScreen
    // ------------------------------------------------------------------

    @Test
    fun serverSettingsScreenShowsLabels() {
        composeRule.setContent {
            SportakusTheme {
                ServerSettingsScreen(
                    darkTheme = false,
                    baseUrl = "",
                    onBaseUrlChange = {},
                    onBack = {},
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText("Адрес тестового сервера").assertExists()
        composeRule.onNodeWithText("Базовый адрес API").assertExists()
        composeRule.onNodeWithText("http://10.0.2.2:5000").assertExists()
        composeRule.onNodeWithText("Сохранить адрес").assertExists()
        composeRule.onNodeWithText("Отмена").assertExists()
    }

    @Test
    fun serverSettingsScreenClickHandlersInvokeCallbacks() {
        var saved = false
        var back = false

        composeRule.setContent {
            SportakusTheme {
                ServerSettingsScreen(
                    darkTheme = false,
                    baseUrl = "http://10.0.2.2:5000",
                    onBaseUrlChange = {},
                    onBack = { back = true },
                    onSave = { saved = true }
                )
            }
        }

        composeRule.onNodeWithText("Сохранить адрес").performClick()
        composeRule.onNodeWithText("Отмена").performClick()
        composeRule.runOnIdle {
            assertTrue("Кнопка «Сохранить адрес» должна вызывать onSave", saved)
            assertTrue("Кнопка «Отмена» должна вызывать onBack", back)
        }
    }

    @Test
    fun serverSettingsScreenForwardsTypingToHostedState() {
        var baseUrl by mutableStateOf("")

        composeRule.setContent {
            SportakusTheme {
                ServerSettingsScreen(
                    darkTheme = false,
                    baseUrl = baseUrl,
                    onBaseUrlChange = { baseUrl = it },
                    onBack = {},
                    onSave = {}
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction()).onFirst()
            .performTextInput("http://localhost:5000")
        composeRule.runOnIdle {
            assertEquals("Ввод адреса должен попадать в onBaseUrlChange", "http://localhost:5000", baseUrl)
        }
    }
}
