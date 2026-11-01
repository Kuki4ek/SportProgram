package com.sportakus.auth.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.sportakus.auth.ui.theme.SportakusAuthTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `LoginScreen displays inputs, error banner, and triggers callbacks`() {
        var enteredIdentifier = ""
        var enteredPassword = ""
        var loginClicked = false
        var forgotPasswordClicked = false
        var createAccountClicked = false

        composeTestRule.setContent {
            SportakusAuthTheme {
                LoginScreen(
                    identifier = enteredIdentifier,
                    password = enteredPassword,
                    isLoading = false,
                    errorMessage = "Неверный логин или пароль.",
                    onIdentifierChange = { enteredIdentifier = it },
                    onPasswordChange = { enteredPassword = it },
                    onBack = {},
                    onForgotPasswordClick = { forgotPasswordClicked = true },
                    onLoginClick = { loginClicked = true },
                    onCreateAccountClick = { createAccountClicked = true },
                    onServerSettingsClick = {}
                )
            }
        }

        // Проверяем отображение заголовка и ошибки
        composeTestRule.onNodeWithText("С возвращением!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Неверный логин или пароль.").assertIsDisplayed()

        // Проверяем клики по кнопкам
        composeTestRule.onNodeWithText("Забыли пароль?").performClick()
        assertTrue(forgotPasswordClicked)

        composeTestRule.onNodeWithText("Войти").performClick()
        assertTrue(loginClicked)

        composeTestRule.onNodeWithText("Создать аккаунт").performClick()
        assertTrue(createAccountClicked)
    }

    @Test
    fun `LoginScreen disables button and shows loading label when isLoading is true`() {
        composeTestRule.setContent {
            SportakusAuthTheme {
                LoginScreen(
                    identifier = "athlete",
                    password = "password123",
                    isLoading = true,
                    errorMessage = null,
                    onIdentifierChange = {},
                    onPasswordChange = {},
                    onBack = {},
                    onForgotPasswordClick = {},
                    onLoginClick = {},
                    onCreateAccountClick = {},
                    onServerSettingsClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Входим…").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun `RegistrationScreen shows password length hint and field validation errors`() {
        composeTestRule.setContent {
            SportakusAuthTheme {
                RegistrationScreen(
                    username = "",
                    email = "bad-email",
                    phone = "",
                    password = "short",
                    usernameError = "Имя пользователя обязательно",
                    emailError = "Почта должна быть вида example@example.ru",
                    phoneError = null,
                    passwordError = null,
                    generalError = "Проверьте введённые данные.",
                    isSubmitting = false,
                    onUsernameChange = {},
                    onEmailChange = {},
                    onPhoneChange = {},
                    onPasswordChange = {},
                    onBack = {},
                    onRegisterClick = {},
                    onAlreadyHaveAccountClick = {},
                    onServerSettingsClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Имя пользователя обязательно").assertIsDisplayed()
        composeTestRule.onNodeWithText("Почта должна быть вида example@example.ru").assertIsDisplayed()
        composeTestRule.onNodeWithText("Минимум 8 символов").assertIsDisplayed()
        composeTestRule.onNodeWithText("Проверьте введённые данные.").assertIsDisplayed()
    }

    @Test
    fun `ResetPasswordScreen shows valid password length message when 8 or more chars`() {
        var saveClicked = false

        composeTestRule.setContent {
            SportakusAuthTheme {
                ResetPasswordScreen(
                    newPassword = "StrongPassword123",
                    confirmPassword = "StrongPassword123",
                    onNewPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onBack = {},
                    onSavePasswordClick = { saveClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Длина пароля подходит").assertIsDisplayed()
        composeTestRule.onNodeWithText("Сохранить новый пароль").assertIsEnabled().performClick()
        assertTrue(saveClicked)
    }

    @Test
    fun `ServerSettingsScreen displays current URL and triggers save and reset callbacks`() {
        var savedClicked = false
        var resetClicked = false
        var currentUrl = "http://10.0.2.2:5062"

        composeTestRule.setContent {
            SportakusAuthTheme {
                ServerSettingsScreen(
                    serverBaseUrl = currentUrl,
                    errorMessage = null,
                    savedMessage = "Адрес сервера сохранён",
                    onServerBaseUrlChange = { currentUrl = it },
                    onResetDefault = { resetClicked = true },
                    onSave = { savedClicked = true },
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Адрес сервера сохранён").assertIsDisplayed()
        composeTestRule.onNodeWithText("Сбросить по умолчанию").performClick()
        assertTrue(resetClicked)

        composeTestRule.onNodeWithText("Сохранить").performClick()
        assertTrue(savedClicked)
    }
}
