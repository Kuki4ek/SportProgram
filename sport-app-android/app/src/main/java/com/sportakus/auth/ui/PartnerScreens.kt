package com.sportakus.auth.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportakus.auth.data.api.Partner
import com.sportakus.auth.data.api.PartnerUpsertRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun PartnersListScreen(
    partners: List<Partner>,
    isLoading: Boolean,
    message: String?,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpen: (Int) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Partner) -> Unit,
    onDelete: (Partner) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<Partner?>(null) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Назад") }
            Text("Партнёры", modifier = Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onRefresh) { Text("Обновить") }
            if (isAdmin) {
                TextButton(onClick = onAdd) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF79B505))
                    Spacer(Modifier.width(4.dp))
                    Text("Добавить", color = Color(0xFF496900), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = Color(0xFF79B505))
        }
        if (!message.isNullOrBlank()) {
            Text(message, Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        if (!isLoading && partners.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().weight(1f).padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Business, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp))
                Spacer(Modifier.height(10.dp))
                Text("Партнёров пока нет", fontWeight = FontWeight.SemiBold)
                Text("Список загружается с сервера", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                TextButton(onClick = onRefresh) { Text("Повторить") }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(partners, key = { it.id }) { partner ->
                    PartnerCard(
                        partner = partner,
                        isAdmin = isAdmin,
                        onClick = { onOpen(partner.id) },
                        onEdit = { onEdit(partner) },
                        onDelete = { deleteTarget = partner }
                    )
                }
            }
        }
    }
    deleteTarget?.let { partner ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Удалить партнёра?") },
            text = { Text("${partner.name} будет удалён из списка партнёров.") },
            confirmButton = {
                TextButton(onClick = { onDelete(partner); deleteTarget = null }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun PartnerCard(
    partner: Partner,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            PartnerLogo(partner.logo, Modifier.size(64.dp))
            Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
                Text(partner.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (partner.contactName.isNotBlank()) Text(partner.contactName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(partner.contactEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(partner.contactPhone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isAdmin) {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Изменить партнёра") }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Удалить партнёра", tint = MaterialTheme.colorScheme.error) }
            } else {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PartnerDetailsScreen(partner: Partner, isAdmin: Boolean, onBack: () -> Unit, onEdit: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Назад") }
            Text("Партнёр", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        PartnerLogo(partner.logo, Modifier.size(112.dp).align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(18.dp))
        Text(partner.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        if (partner.contactName.isNotBlank()) PartnerInfo("Контактное лицо", partner.contactName)
        PartnerInfo("Электронная почта", partner.contactEmail)
        PartnerInfo("Телефон", partner.contactPhone)
        if (isAdmin) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF79B505))) {
                Text("Редактировать", color = Color.White)
            }
        }
    }
}

@Composable
private fun PartnerInfo(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(value.ifBlank { "Не указано" }, fontSize = 15.sp)
        HorizontalDividerCompat()
    }
}

@Composable
private fun HorizontalDividerCompat() {
    Divider(Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun PartnerEditorScreen(
    initial: Partner?,
    onCancel: () -> Unit,
    onSave: (PartnerUpsertRequest) -> Unit,
    saving: Boolean,
    errorMessage: String?
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var logo by remember(initial?.id) { mutableStateOf(initial?.logo.orEmpty()) }
    var contactName by remember(initial?.id) { mutableStateOf(initial?.contactName.orEmpty()) }
    var contactEmail by remember(initial?.id) { mutableStateOf(initial?.contactEmail.orEmpty()) }
    val initialPhoneDigits = remember(initial?.contactPhone) { normalizeInternationalPhoneDigits(initial?.contactPhone.orEmpty()) }
    var contactPhoneDigits by remember(initial?.id) { mutableStateOf(initialPhoneDigits) }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel) { Icon(Icons.Filled.ArrowBack, contentDescription = "Отмена") }
            Text(if (initial == null) "Новый партнёр" else "Изменить партнёра", fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PartnerLogo(logo, Modifier.size(84.dp))
            PartnerInput("Название *", name, { name = it })
            PartnerInput("URL логотипа *", logo, { logo = it }, keyboardType = KeyboardType.Uri)
            PartnerInput("Контактное лицо", contactName, { contactName = it })
            PartnerInput("Контактная почта *", contactEmail, { contactEmail = it }, keyboardType = KeyboardType.Email)
            PartnerPhoneInput(contactPhoneDigits) { contactPhoneDigits = normalizeInternationalPhoneDigits(it) }
            val error = localError ?: errorMessage
            if (!error.isNullOrBlank()) Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !saving) { Text("Отмена") }
            Button(
                onClick = {
                    localError = when {
                        name.isBlank() -> "Введите название партнёра."
                        logo.isBlank() -> "Укажите URL логотипа."
                        !contactEmail.contains('@') -> "Введите корректную контактную почту."
                        !Regex("^\\+7 \\(\\d{3}\\) \\d{3}-\\d{2}-\\d{2}$").matches(formatPhoneForRegistrationApi(contactPhoneDigits).orEmpty()) -> "Телефон должен быть в формате +7 (999) 999-99-99."
                        else -> null
                    }
                    if (localError == null) onSave(PartnerUpsertRequest(name.trim(), logo.trim(), contactName.trim(), contactEmail.trim(), formatPhoneForRegistrationApi(contactPhoneDigits).orEmpty()))
                },
                modifier = Modifier.weight(1f),
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF79B505))
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Сохранить", color = Color.White)
            }
        }
    }
}

@Composable
private fun PartnerInput(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
private fun PartnerPhoneInput(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Контактный телефон *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        visualTransformation = InternationalPhoneVisualTransformation
    )
}

@Composable
private fun PartnerLogo(url: String, modifier: Modifier = Modifier) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(url) {
        bitmap = if (url.startsWith("http://", true) || url.startsWith("https://", true)) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 5_000
                    connection.readTimeout = 5_000
                    connection.instanceFollowRedirects = true
                    try { connection.inputStream.use { stream -> BitmapFactory.decodeStream(stream) } }
                    finally { connection.disconnect() }
                }.getOrNull()
            }
        } else null
    }
    if (bitmap != null) {
        Image(bitmap!!.asImageBitmap(), contentDescription = "Логотип партнёра", modifier = modifier.clip(RoundedCornerShape(14.dp)))
    } else {
        Box(modifier.clip(RoundedCornerShape(14.dp)).background(Color(0xFFF1F4E9)).border(1.dp, Color(0xFFE1E8D4), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Business, null, tint = Color(0xFF79B505), modifier = Modifier.size(32.dp))
        }
    }
}
