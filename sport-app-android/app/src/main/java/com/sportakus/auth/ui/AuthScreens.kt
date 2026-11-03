package com.sportakus.auth.ui

import com.sportakus.auth.ui.theme.sportakusTextColor
import com.sportakus.auth.ui.theme.sportakusSecondaryTextColor
import com.sportakus.auth.ui.theme.sportakusInputBackground
import com.sportakus.auth.ui.theme.sportakusInputTextColor
import com.sportakus.auth.ui.theme.sportakusPrimaryActionTextColor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportakus.auth.ui.theme.SportakusActionGreen
import com.sportakus.auth.R


// Always read foreground colors from the active MaterialTheme rather than a
// separately passed Boolean. This keeps every text element synchronized with the
// actual color scheme when the user switches themes.
@Composable
private fun authForeground(): Color = sportakusTextColor()

@Composable
private fun authSecondaryForeground(): Color = sportakusSecondaryTextColor()

@Composable
private fun authButtonForeground(): Color = sportakusPrimaryActionTextColor()

@Composable
fun LoginScreen(
    identifier: String,
    onIdentifierChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onSocialAuth: (String) -> Unit,
    isSubmitting: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusAndNavigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        AuthTopBar(
            title = null,
            darkTheme = darkTheme,
            onBack = onBack,
            onToggleTheme = onToggleTheme
        )

        // Keep the title on the screen background: it should not sit on a field-like panel.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 14.dp)
        ) {
            DarkText(
                text = "Войти",
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                color = authForeground()
            )
            Spacer(Modifier.height(8.dp))
            DarkText(
                text = "Пройти авторизацию в Спортакус",
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = authSecondaryForeground()
            )
        }

        Spacer(Modifier.height(18.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = identifier,
            onValueChange = onIdentifierChange,
            placeholder = "Логин, почта или телефон",
            leadingIcon = Icons.Outlined.PersonOutline,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            autoFormatPhone = true
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Пароль",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            validatePasswordLength = true,
            imeAction = ImeAction.Done,
            onImeDone = onLogin
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            DarkText(
                text = "Забыли пароль?",
                color = sportakusTextColor(),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onForgotPassword)
                    .padding(vertical = 8.dp, horizontal = 2.dp)
            )
        }
        PrimaryAuthButton(
            text = if (isSubmitting) "Входим..." else "Войти",
            darkTheme = darkTheme,
            onClick = onLogin,
            enabled = !isSubmitting
        )
        Spacer(Modifier.height(24.dp))
        OrDivider("Или войти через", darkTheme = darkTheme)
        Spacer(Modifier.height(16.dp))
        SocialAuthButtons(
            registration = false,
            darkTheme = darkTheme,
            onProviderClick = onSocialAuth
        )
        Spacer(Modifier.height(62.dp))
        AuthFooter(
            firstPart = "Нет аккаунта?",
            action = "Регистрация",
            darkTheme = darkTheme,
            onClick = onRegister
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun RegistrationScreen(
    username: String,
    onUsernameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    usernameError: String?,
    emailError: String?,
    phoneError: String?,
    passwordError: String?,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    onSocialAuth: (String) -> Unit,
    isSubmitting: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusAndNavigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        AuthTopBar(
            title = "Регистрация",
            darkTheme = darkTheme,
            onBack = onBack,
            onToggleTheme = onToggleTheme
        )
        Spacer(Modifier.height(20.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = username,
            onValueChange = onUsernameChange,
            placeholder = "Твоё имя",
            leadingIcon = Icons.Outlined.PersonOutline,
            validationError = usernameError,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Почта",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            validationError = emailError,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = "Телефон (необязательно)",
            leadingIcon = Icons.Outlined.PhoneAndroid,
            keyboardType = KeyboardType.Phone,
            validationError = phoneError,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Пароль",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            validatePasswordLength = true,
            minimumPasswordLength = 8,
            validationError = passwordError,
            imeAction = ImeAction.Done,
            onImeDone = onRegister
        )
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            DarkText(
                text = "Забыли пароль?",
                color = sportakusTextColor(),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onForgotPassword)
                    .padding(vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        PrimaryAuthButton(
            text = if (isSubmitting) "Отправка..." else "Регистрация",
            darkTheme = darkTheme,
            onClick = onRegister,
            enabled = !isSubmitting
        )
        Spacer(Modifier.height(28.dp))
        OrDivider("Или зарегистрироваться через", darkTheme = darkTheme)
        Spacer(Modifier.height(16.dp))
        SocialAuthButtons(
            registration = true,
            darkTheme = darkTheme,
            onProviderClick = onSocialAuth
        )
        Spacer(Modifier.height(68.dp))
        AuthFooter(
            firstPart = "Уже есть аккаунт?",
            action = "Войти",
            darkTheme = darkTheme,
            onClick = onLogin
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun RecoveryScreen(
    method: String,
    onMethodChange: (String) -> Unit,
    contact: String,
    onContactChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onSendCode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusAndNavigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        AuthTopBar(null, darkTheme, onBack, onToggleTheme)
        Spacer(Modifier.height(28.dp))
        DarkText(
            text = "Восстановление пароля",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 27.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = sportakusTextColor())
        Spacer(Modifier.height(10.dp))
        DarkText(
            text = when (method) {
                "Почта" -> "Укажи почту, связанную с аккаунтом.\nНа неё будет отправлен код для сброса пароля."
                "Телефон" -> "Укажи номер телефона, связанный с аккаунтом.\nНа него будет отправлен код для сброса пароля."
                else -> "Укажи имя пользователя (nickname), связанное с аккаунтом.\nНа почту или телефон аккаунта будет отправлен код."
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = sportakusTextColor())
        Spacer(Modifier.height(32.dp))
        MethodSelector(selected = method, darkTheme = darkTheme, onSelect = onMethodChange)
        Spacer(Modifier.height(32.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = contact,
            onValueChange = onContactChange,
            placeholder = when (method) {
                "Почта" -> "Почта"
                "Телефон" -> "Номер телефона"
                else -> "Имя пользователя"
            },
            leadingIcon = when (method) {
                "Почта" -> Icons.Outlined.Email
                "Телефон" -> Icons.Outlined.PhoneAndroid
                else -> Icons.Outlined.PersonOutline
            },
            keyboardType = when (method) {
                "Почта" -> KeyboardType.Email
                "Телефон" -> KeyboardType.Phone
                else -> KeyboardType.Text
            },
            imeAction = ImeAction.Done,
            onImeDone = onSendCode
        )
        Spacer(Modifier.height(68.dp))
        PrimaryAuthButton(text = "Восстановить пароль", darkTheme = darkTheme, onClick = onSendCode)
    }
}

@Composable
fun VerificationScreen(
    title: String,
    description: String,
    buttonText: String,
    code: String,
    onCodeChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onVerify: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusAndNavigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        AuthTopBar(null, darkTheme, onBack, onToggleTheme)
        Spacer(Modifier.height(34.dp))
        DarkText(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 27.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = sportakusTextColor())
        Spacer(Modifier.height(10.dp))
        DarkText(
            text = description,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = sportakusTextColor())
        Spacer(Modifier.height(38.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = code,
            onValueChange = onCodeChange,
            placeholder = "Код подтверждения",
            leadingIcon = Icons.Outlined.VpnKey,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
            onImeDone = onVerify
        )
        Spacer(Modifier.height(28.dp))
        PrimaryAuthButton(text = buttonText, darkTheme = darkTheme, onClick = onVerify)
    }
}

@Composable
fun ResetPasswordScreen(
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusAndNavigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        AuthTopBar("Новый пароль", darkTheme, onBack, onToggleTheme)
        Spacer(Modifier.height(28.dp))
        DarkText(
            text = "Придумайте пароль",
            fontSize = 27.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = sportakusTextColor())
        Spacer(Modifier.height(8.dp))
        DarkText(
            text = "Используйте не менее 8 символов.",
            fontSize = 16.sp,
            color = sportakusTextColor())
        Spacer(Modifier.height(24.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Новый пароль",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            validatePasswordLength = true,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            darkTheme = darkTheme,
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "Повторите пароль",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeDone = onReset
        )
        Spacer(Modifier.height(28.dp))
        PrimaryAuthButton(text = "Сохранить пароль", darkTheme = darkTheme, onClick = onReset)
    }
}

/**
 * Use the current screen's explicit theme flag for foreground text.
 * This application uses one light theme; all visible labels use DarkText.
 */

@Composable
private fun AuthTopBar(
    title: String?,
    darkTheme: Boolean,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Назад",
                tint = authForeground(),
                modifier = Modifier.size(23.dp)
            )
        }
        if (!title.isNullOrBlank()) {
            DarkText(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = sportakusTextColor())
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    onImeDone: (() -> Unit)? = null,
    validatePasswordLength: Boolean = false,
    minimumPasswordLength: Int = 8,
    validationError: String? = null,
    autoFormatPhone: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val passwordLengthInvalid = validatePasswordLength && value.isNotEmpty() && value.length < minimumPasswordLength

    Column(modifier = modifier.fillMaxWidth()) {
        varPasswordField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            darkTheme = darkTheme,
            modifier = Modifier.fillMaxWidth(),
            keyboardType = keyboardType,
            imeAction = imeAction,
            isPassword = isPassword,
            onImeDone = onImeDone,
            interactionSource = interactionSource,
            focused = focused,
            passwordLengthInvalid = passwordLengthInvalid || validationError != null,
            autoFormatPhone = autoFormatPhone
        )

        if (validationError != null) {
            DarkText(
                text = validationError,
                modifier = Modifier.padding(start = 4.dp, top = 5.dp),
                color = sportakusTextColor(),
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
        } else if (validatePasswordLength && value.isNotEmpty()) {
            DarkText(
                text = if (passwordLengthInvalid) "Минимум $minimumPasswordLength символов" else "Длина пароля подходит",
                modifier = Modifier.padding(start = 4.dp, top = 5.dp),
                color = sportakusTextColor(),
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
        }
    }
}

// All fields share the same focus, cursor, and password-visibility styling.
@Composable
private fun varPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    darkTheme: Boolean,
    modifier: Modifier,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean,
    onImeDone: (() -> Unit)?,
    interactionSource: MutableInteractionSource,
    focused: Boolean,
    passwordLengthInvalid: Boolean,
    autoFormatPhone: Boolean = false
) {
    var passwordVisible by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(false)
    }
    val colors = MaterialTheme.colorScheme
    val valueLooksLikePhone = value.isNotBlank() && value.all { it.isDigit() || it in "+ ()-" }
    val isPhoneField = keyboardType == KeyboardType.Phone || (autoFormatPhone && valueLooksLikePhone)
    val fieldValue = if (isPhoneField) normalizeInternationalPhoneDigits(value) else value
    val fieldForeground = sportakusInputTextColor()
    val fieldMutedForeground = sportakusSecondaryTextColor()
    val fieldShape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(fieldShape)
            .background(sportakusInputBackground())
            .border(
                width = if (passwordLengthInvalid || focused) 1.2.dp else 0.dp,
                color = when {
                    passwordLengthInvalid -> colors.error
                    focused -> colors.primary
                    else -> sportakusInputBackground()
                },
                shape = fieldShape
            )
            .padding(start = 15.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = fieldMutedForeground,
            modifier = Modifier.size(21.dp)
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (fieldValue.isEmpty() && (!isPhoneField || !focused)) {
                DarkText(
                    text = placeholder,
                    color = fieldForeground,
                    fontSize = 16.sp,
                    maxLines = 1
                )
            }
            CompositionLocalProvider(
                LocalContentColor provides fieldForeground,
                LocalTextStyle provides MaterialTheme.typography.bodyLarge.copy(
                    color = fieldForeground,
                    fontSize = 16.sp
                )
            ) {
                BasicTextField(
                    value = fieldValue,
                    onValueChange = { editedValue ->
                        val editedLooksLikePhone = editedValue.isNotBlank() && editedValue.all { it.isDigit() || it in "+ ()-" }
                        val usePhoneMask = keyboardType == KeyboardType.Phone || (autoFormatPhone && editedLooksLikePhone)
                        onValueChange(if (usePhoneMask) normalizeInternationalPhoneDigits(editedValue) else editedValue)
                    },
                    modifier = Modifier.fillMaxWidth().height(26.dp),
                    singleLine = true,
                    interactionSource = interactionSource,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = fieldForeground,
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.None
                    ),
                    cursorBrush = SolidColor(fieldForeground),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isPassword && keyboardType == KeyboardType.Text) KeyboardType.Password else keyboardType,
                        imeAction = imeAction
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { onImeDone?.invoke() }
                    ),
                    visualTransformation = when {
                        isPassword && !passwordVisible -> PasswordVisualTransformation()
                        isPhoneField && (focused || fieldValue.isNotBlank()) -> InternationalPhoneVisualTransformation
                        else -> VisualTransformation.None
                    }
                )
            }
        }
        if (isPassword) {
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                    tint = fieldMutedForeground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PrimaryAuthButton(
    text: String,
    darkTheme: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SportakusActionGreen,
            contentColor = Color.White,
            disabledContentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        DarkText(text = text, color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OrDivider(text: String, darkTheme: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        DarkText(
            text = text,
            modifier = Modifier.padding(horizontal = 13.dp),
            fontSize = 13.sp,
            color = sportakusTextColor(),
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

@Composable
private fun SocialAuthButtons(
    registration: Boolean,
    darkTheme: Boolean,
    onProviderClick: (String) -> Unit
) {
    val labelColor = sportakusTextColor()
    Column {
        SocialAuthButton(
            text = if (registration) "Регистрация через Google" else "Войти через Google",
            logoRes = R.drawable.google_logo,
            glyph = null,
            glyphColor = Color(0xFF4285F4),
            labelColor = labelColor,
            onClick = { onProviderClick("Google") }
        )
        Spacer(Modifier.height(10.dp))
        SocialAuthButton(
            text = if (registration) "Регистрация через Яндекс" else "Войти через Яндекс",
            logoRes = R.drawable.yandex_logo,
            glyph = null,
            glyphColor = Color(0xFFFF3333),
            labelColor = labelColor,
            onClick = { onProviderClick("Яндекс") }
        )
        if (!registration) {
            Spacer(Modifier.height(10.dp))
            SocialAuthButton(
                text = "Войти через почту",
                logoRes = null,
                glyph = "✉",
                glyphColor = sportakusSecondaryTextColor(),
                labelColor = labelColor,
                onClick = { onProviderClick("почту") }
            )
        }
    }
}

@Composable
private fun SocialAuthButton(
    text: String,
    logoRes: Int?,
    glyph: String?,
    glyphColor: Color,
    labelColor: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = sportakusTextColor(),
            disabledContentColor = sportakusTextColor()
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
    ) {
        Surface(
            modifier = Modifier.size(30.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    logoRes != null -> Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = text,
                        modifier = Modifier.size(22.dp),
                        contentScale = ContentScale.Fit
                    )
                    glyph == "✉" -> Icon(
                        Icons.Outlined.Email,
                        contentDescription = null,
                        tint = glyphColor,
                        modifier = Modifier.size(18.dp)
                    )
                    else -> DarkText(
                        glyph.orEmpty(),
                        color = sportakusTextColor(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        DarkText(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = sportakusTextColor(),
            maxLines = 1
        )
    }
}

@Composable
private fun AuthFooter(
    firstPart: String,
    action: String,
    darkTheme: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        DarkText(
            text = firstPart,
            color = sportakusTextColor(),
            fontSize = 14.sp
        )
        Spacer(Modifier.width(4.dp))
        DarkText(
            text = action,
            color = SportakusActionGreen,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp, horizontal = 2.dp)
        )
    }
}

@Composable
private fun MethodSelector(selected: String, darkTheme: Boolean, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Почта", "Телефон", "Логин").forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                DarkText(
                    text = option,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    // Keep the selector label color fixed regardless of selection state.
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else sportakusTextColor()
                )
            }
        }
    }
}

@Composable
fun ServerSettingsScreen(
    darkTheme: Boolean,
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusAndNavigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        AuthTopBar(
            title = "Адрес тестового сервера",
            darkTheme = darkTheme,
            onBack = onBack,
            onToggleTheme = {}
        )
        Spacer(Modifier.height(26.dp))
        DarkText(
            text = "Базовый адрес API",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = sportakusTextColor()
        )
        Spacer(Modifier.height(10.dp))
        DarkText(
            text = "Укажи базовый адрес сервера без пути endpoint. Адрес сохраняется на устройстве и используется для входа, регистрации и проверки профиля.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = sportakusTextColor()
        )
        Spacer(Modifier.height(22.dp))
        AuthTextField(
            value = baseUrl,
            onValueChange = onBaseUrlChange,
            placeholder = "http://10.0.2.2:5000",
            leadingIcon = Icons.Outlined.VpnKey,
            darkTheme = darkTheme,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done
        )
        Spacer(Modifier.height(16.dp))
        DarkText(
            text = "Эмулятор Android: обычно http://10.0.2.2:5000. Если ASP.NET Core использует другой порт, впиши порт из launchSettings.json.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = sportakusSecondaryTextColor()
        )
        Spacer(Modifier.height(10.dp))
        DarkText(
            text = "Реальный телефон: укажи адрес, доступный с телефона. Это может быть IP компьютера в одной Wi-Fi сети или публичный HTTPS-адрес/туннель, если вы в разных сетях.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = sportakusSecondaryTextColor()
        )
        Spacer(Modifier.height(28.dp))
        PrimaryAuthButton(text = "Сохранить адрес", darkTheme = darkTheme, onClick = onSave)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = sportakusTextColor()
            )
        ) {
            DarkText("Отмена", color = sportakusTextColor(), fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun Modifier.statusAndNavigationBarsPadding(): Modifier =
    this.statusBarsPadding().navigationBarsPadding()
