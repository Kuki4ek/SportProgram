package com.sportakus.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sportakus.auth.R
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    onSocialAuth: (String) -> Unit
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 18.dp, bottom = 14.dp)
        ) {
            Text(
                text = "Войти",
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                color = authTitleColor(darkTheme)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Пройти авторизацию в Спортакус",
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(18.dp))
        AuthTextField(
            value = identifier,
            onValueChange = onIdentifierChange,
            placeholder = "Телефон, почта или логин",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
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
            Text(
                text = "Забыли пароль?",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onForgotPassword)
                    .padding(vertical = 8.dp, horizontal = 2.dp)
            )
        }
        PrimaryAuthButton(text = "Войти", onClick = onLogin)
        Spacer(Modifier.height(24.dp))
        OrDivider("Или войти через")
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
            onClick = onRegister
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun RegistrationScreen(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    onSocialAuth: (String) -> Unit
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
            value = name,
            onValueChange = onNameChange,
            placeholder = "Твоё имя",
            leadingIcon = Icons.Outlined.PersonOutline,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Почта",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Пароль",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            validatePasswordLength = true,
            imeAction = ImeAction.Done,
            onImeDone = onRegister
        )
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Забыли пароль?",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onForgotPassword)
                    .padding(vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        PrimaryAuthButton(text = "Регистрация", onClick = onRegister)
        Spacer(Modifier.height(28.dp))
        OrDivider("Или зарегистрироваться через")
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
        Text(
            text = "Подтверждение",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 27.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = authTitleColor(darkTheme)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (method == "Почта") {
                "Мы отправим на данную почту код\nподтверждения."
            } else {
                "Мы отправим на данный номер код\nподтверждения."
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        MethodSelector(selected = method, onSelect = onMethodChange)
        Spacer(Modifier.height(32.dp))
        AuthTextField(
            value = contact,
            onValueChange = onContactChange,
            placeholder = if (method == "Почта") "Почта" else "Номер телефона",
            leadingIcon = if (method == "Почта") Icons.Outlined.Email else Icons.Outlined.PhoneAndroid,
            keyboardType = if (method == "Почта") KeyboardType.Email else KeyboardType.Phone,
            imeAction = ImeAction.Done,
            onImeDone = onSendCode
        )
        Spacer(Modifier.height(68.dp))
        PrimaryAuthButton(text = "Отправить код", onClick = onSendCode)
    }
}

@Composable
fun VerificationScreen(
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
        AuthTopBar("Подтверждение", darkTheme, onBack, onToggleTheme)
        Spacer(Modifier.height(34.dp))
        Text(
            text = "Введите код",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = authTitleColor(darkTheme)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Введите код подтверждения,\nкоторый мы отправили вам.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(38.dp))
        AuthTextField(
            value = code,
            onValueChange = onCodeChange,
            placeholder = "Код подтверждения",
            leadingIcon = Icons.Outlined.VpnKey,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
            onImeDone = onVerify
        )
        Spacer(Modifier.height(28.dp))
        PrimaryAuthButton(text = "Подтвердить код", onClick = onVerify)
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
        Text(
            text = "Придумайте пароль",
            fontSize = 27.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = authTitleColor(darkTheme)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Используйте не менее 8 символов.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        AuthTextField(
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
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "Повторите пароль",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeDone = onReset
        )
        Spacer(Modifier.height(28.dp))
        PrimaryAuthButton(text = "Сохранить пароль", onClick = onReset)
    }
}

/** High-contrast screen heading: black in light theme, near-white in dark theme. */
private fun authTitleColor(darkTheme: Boolean): Color =
    if (darkTheme) Color(0xFFF2F5EE) else Color(0xFF000000)

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
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(23.dp)
            )
        }
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = authTitleColor(darkTheme)
            )
        }
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(44.dp)
        ) {
            Icon(
                imageVector = if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = if (darkTheme) "Включить светлую тему" else "Включить тёмную тему",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    onImeDone: (() -> Unit)? = null,
    validatePasswordLength: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val passwordLengthInvalid = validatePasswordLength && value.isNotEmpty() && value.length < 8

    Column(modifier = modifier.fillMaxWidth()) {
        varPasswordField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            modifier = Modifier.fillMaxWidth(),
            keyboardType = keyboardType,
            imeAction = imeAction,
            isPassword = isPassword,
            onImeDone = onImeDone,
            interactionSource = interactionSource,
            focused = focused,
            passwordLengthInvalid = passwordLengthInvalid
        )

        if (validatePasswordLength && value.isNotEmpty()) {
            Text(
                text = if (passwordLengthInvalid) "Минимум 8 символов" else "Длина пароля подходит",
                modifier = Modifier.padding(start = 4.dp, top = 5.dp),
                color = if (passwordLengthInvalid) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
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
    modifier: Modifier,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean,
    onImeDone: (() -> Unit)?,
    interactionSource: MutableInteractionSource,
    focused: Boolean,
    passwordLengthInvalid: Boolean
) {
    var passwordVisible by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(false)
    }
    val colors = MaterialTheme.colorScheme
    val fieldShape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(fieldShape)
            .background(colors.surfaceVariant)
            .border(
                width = if (passwordLengthInvalid || focused) 1.2.dp else 0.dp,
                color = when {
                    passwordLengthInvalid -> colors.error
                    focused -> colors.primary
                    else -> colors.surfaceVariant
                },
                shape = fieldShape
            )
            .padding(start = 15.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(21.dp)
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = colors.onSurfaceVariant,
                    fontSize = 16.sp,
                    maxLines = 1
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                interactionSource = interactionSource,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = colors.onSurface,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isPassword && keyboardType == KeyboardType.Text) KeyboardType.Password else keyboardType,
                    imeAction = imeAction
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { onImeDone?.invoke() }
                ),
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                }
            )
        }
        if (isPassword) {
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PrimaryAuthButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OrDivider(text: String) {
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
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 13.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val labelColor = if (darkTheme) Color(0xFFF2F5EE) else Color(0xFF000000)
    val firstText = if (registration) "Регистрация через Госуслуги" else "Войти через Госуслуги"
    val secondText = if (registration) "Регистрация через ВКонтакте" else "Войти через ВКонтакте"

    SocialAuthButton(
        text = firstText,
        logoRes = R.drawable.gosuslugi_logo,
        labelColor = labelColor,
        onClick = { onProviderClick("Госуслуги") }
    )
    Spacer(Modifier.height(12.dp))
    SocialAuthButton(
        text = secondText,
        logoRes = R.drawable.vk_logo,
        labelColor = labelColor,
        onClick = { onProviderClick("ВКонтакте") }
    )
}

@Composable
private fun SocialAuthButton(
    text: String,
    logoRes: Int,
    labelColor: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = text,
            modifier = Modifier.size(34.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            maxLines = 1
        )
    }
}

@Composable
private fun AuthFooter(
    firstPart: String,
    action: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = firstPart,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = action,
            color = MaterialTheme.colorScheme.primary,
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
private fun MethodSelector(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Почта", "Телефон").forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun Modifier.statusAndNavigationBarsPadding(): Modifier =
    this.statusBarsPadding().navigationBarsPadding()
