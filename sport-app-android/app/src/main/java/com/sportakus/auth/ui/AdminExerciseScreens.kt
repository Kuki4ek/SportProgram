package com.sportakus.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

@Composable
fun AdminExerciseListScreen(
    exercises: List<WorkoutExercise>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<WorkoutExercise?>(null) }
    val filtered = exercises.filter { it.title.contains(query, ignoreCase = true) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            AdminRoundButton(Icons.Filled.ArrowBack, "Назад", onBack)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                AdminText("Упражнения", 20, FontWeight.Bold)
                AdminText("Каталог упражнений", 10, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AdminRoundButton(Icons.Filled.MoreVert, "Дополнительно") { }
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 32.dp)) {
            AdminSearchField(query, { query = it }, "Поиск упражнений")
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminFilterPill("Фильтры") { }
                AdminFilterPill("Сортировка") { }
                AdminFilterPill("Время") { }
                AdminFilterPill("Коины") { }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AdminText("${filtered.size} упражнений", 13, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Surface(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).clickable(onClick = onAdd),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(54.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(31.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        AdminText("Добавить", 17, FontWeight.Bold)
                        AdminText("Новое упражнение", 12, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(12.dp))
            filtered.forEach { exercise ->
                AdminExerciseCard(
                    exercise = exercise,
                    onEdit = { onEdit(exercise.id) },
                    onToggle = { onToggleVisibility(exercise.id) },
                    onDelete = { deleteTarget = exercise }
                )
                Spacer(Modifier.height(11.dp))
            }
            if (filtered.isEmpty()) {
                Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
                    AdminText("Упражнения не найдены", 14, modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    deleteTarget?.let { exercise ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { AdminText("Удалить упражнение?", 19, FontWeight.Bold) },
            text = {
                Column {
                    AdminText(exercise.title, 15, FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    AdminText("Упражнение будет удалено из локального списка предпросмотра. В рабочей версии удаление должно выполняться на сервере.", 13, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    AdminText("Вместо удаления можно скрыть упражнение.", 12, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                TextButton(onClick = { onDelete(exercise.id); deleteTarget = null }) {
                    AdminText("Удалить", 14, color = MaterialTheme.colorScheme.error, weight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (exercise.published) onToggleVisibility(exercise.id)
                    deleteTarget = null
                }) {
                    AdminText(
                        if (exercise.published) "Скрыть вместо удаления" else "Оставить скрытым",
                        14,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun AdminExerciseCard(
    exercise: WorkoutExercise,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        Modifier.fillMaxWidth(),
        color = if (exercise.published) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .6f))
    ) {
        Column(Modifier.padding(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(94.dp).clip(RoundedCornerShape(13.dp)).background(Color.Red))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Surface(color = if (exercise.published) Color(0xFF4B6B00) else Color(0xFF7B8174), shape = RoundedCornerShape(7.dp)) {
                        AdminText(if (exercise.published) "Опубликовано" else "Скрыто", 10, color = Color.White, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    AdminText(exercise.title, 16, FontWeight.Bold, maxLines = 2)
                    Spacer(Modifier.height(3.dp))
                    AdminText("✦ ${exercise.coins}", 13, color = Color(0xFF00AFC6), weight = FontWeight.SemiBold)
                    AdminText(if (exercise.durationMinutes > 0) "◷ ${exercise.durationMinutes} минут" else "◷ Время не указано", 12, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1.2f).height(38.dp), contentPadding = PaddingValues(horizontal = 5.dp), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    AdminText("Изменить", 11, weight = FontWeight.SemiBold)
                }
                OutlinedButton(onClick = onToggle, modifier = Modifier.weight(1f).height(38.dp), contentPadding = PaddingValues(horizontal = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Icon(if (exercise.published) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(3.dp))
                    AdminText(if (exercise.published) "Скрыть" else "Показать", 11, weight = FontWeight.SemiBold)
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(.85f).height(38.dp), contentPadding = PaddingValues(horizontal = 4.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(3.dp))
                    AdminText("Удалить", 11, weight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AdminExerciseEditorScreen(
    initial: WorkoutExercise?,
    onCancel: () -> Unit,
    onSave: (WorkoutExercise) -> Unit
) {
    var title by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var shortDescription by remember(initial?.id) { mutableStateOf(initial?.shortDescription.orEmpty()) }
    var duration by remember(initial?.id) { mutableStateOf(initial?.durationMinutes?.toString().orEmpty()) }
    var distance by remember(initial?.id) { mutableStateOf(initial?.distance?.takeIf { it != "—" }.orEmpty()) }
    var calories by remember(initial?.id) { mutableStateOf(initial?.calories?.toString().orEmpty()) }
    var coins by remember(initial?.id) { mutableStateOf(initial?.coins?.toString().orEmpty()) }
    var description by remember(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var contraindications by remember(initial?.id) { mutableStateOf(initial?.contraindications.orEmpty()) }
    var tips by remember(initial?.id) { mutableStateOf(initial?.tips.orEmpty()) }
    var steps by remember(initial?.id) { mutableStateOf(initial?.steps.orEmpty()) }
    var published by remember(initial?.id) { mutableStateOf(initial?.published ?: false) }
    var textExpanded by remember(initial?.id) { mutableStateOf(false) }
    var stepsExpanded by remember(initial?.id) { mutableStateOf(false) }
    var validation by remember { mutableStateOf<String?>(null) }
    val isNew = initial == null

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().imePadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            AdminRoundButton(Icons.Filled.ArrowBack, "Назад", onCancel)
            AdminText(if (isNew) "Добавить упражнение" else "Редактировать упражнение", 18, FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
            Surface(
                Modifier.fillMaxWidth().height(90.dp).clickable { },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                    Column(Modifier.padding(start = 12.dp)) {
                        AdminText(if (isNew) "Добавить обложку *" else "Изображения упражнения", 14, FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        AdminText("JPG, PNG · до 10 МБ · пока локальный макет", 11, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            AdminInputField("Название *", title, { title = it }, "Введите название")
            Spacer(Modifier.height(9.dp))
            AdminInputField("Короткое описание", shortDescription, { shortDescription = it }, "Небольшое описание", minLines = 2)
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminInputField("Время, мин *", duration, { duration = it.filter(Char::isDigit) }, "—", Modifier.weight(1f), numeric = true)
                AdminInputField("Дистанция", distance, { distance = it }, "— км", Modifier.weight(1f))
                AdminInputField("Ккал *", calories, { calories = it.filter(Char::isDigit) }, "—", Modifier.weight(1f), numeric = true)
            }
            Spacer(Modifier.height(9.dp))
            AdminInputField("Коины *", coins, { coins = it.filter(Char::isDigit) }, "Укажите награду", numeric = true)
            Spacer(Modifier.height(12.dp))
            AdminExpandableSection(
                title = "Тексты упражнения",
                subtitle = "Описание, противопоказания и рекомендации",
                expanded = textExpanded,
                onToggle = { textExpanded = !textExpanded }
            ) {
                AdminInputField("Полное описание", description, { description = it }, "Введите описание", minLines = 4)
                Spacer(Modifier.height(8.dp))
                AdminInputField("Противопоказания", contraindications, { contraindications = it }, "Разделяйте пункты точкой с запятой", minLines = 3)
                Spacer(Modifier.height(8.dp))
                AdminInputField("Рекомендации", tips, { tips = it }, "Каждый совет с новой строки", minLines = 3)
            }
            Spacer(Modifier.height(9.dp))
            AdminExpandableSection(
                title = "Инструкция упражнения",
                subtitle = "Шаги выполнения · один шаг на строку в формате Название|Описание",
                expanded = stepsExpanded,
                onToggle = { stepsExpanded = !stepsExpanded }
            ) {
                AdminInputField("Шаги выполнения", steps, { steps = it }, "Разминка|5 минут ходьбы", minLines = 5)
            }
            Spacer(Modifier.height(10.dp))
            Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        AdminText("Видимость", 15, FontWeight.SemiBold)
                        AdminText(if (published) "Опубликовано" else "Скрыто", 12, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = published, onCheckedChange = { published = it })
                }
            }
            Spacer(Modifier.height(8.dp))
            AdminText("* Обязательные поля. Для нового упражнения по умолчанию установлено состояние «Скрыто».", 11, color = MaterialTheme.colorScheme.onSurfaceVariant)
            validation?.let { AdminText(it, 12, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
            Spacer(Modifier.height(16.dp))
        }
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(.85f).height(52.dp), shape = RoundedCornerShape(13.dp)) { AdminText("Отмена", 14, FontWeight.SemiBold) }
            Button(
                onClick = {
                    val minutes = duration.toIntOrNull()
                    val calorieValue = calories.toIntOrNull()
                    val coinValue = coins.toIntOrNull()
                    validation = when {
                        title.isBlank() -> "Укажите название упражнения."
                        minutes == null || minutes <= 0 -> "Укажите корректное время."
                        calorieValue == null || calorieValue < 0 -> "Укажите корректное количество калорий."
                        coinValue == null || coinValue < 0 -> "Укажите количество СпортКоинов."
                        else -> null
                    }
                    if (validation == null) {
                        onSave(
                            WorkoutExercise(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                title = title.trim(),
                                shortDescription = shortDescription.trim(),
                                description = description.trim().ifBlank { shortDescription.trim() },
                                durationMinutes = minutes!!,
                                distance = distance.trim().ifBlank { "—" },
                                calories = calorieValue!!,
                                coins = coinValue!!,
                                contraindications = contraindications.trim(),
                                tips = tips.trim(),
                                steps = steps.trim(),
                                published = published,
                                category = initial?.category ?: "Другое",
                                intensity = initial?.intensity.orEmpty()
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1.5f).height(52.dp),
                shape = RoundedCornerShape(13.dp)
            ) { AdminText(if (isNew) "Создать" else "Сохранить", 15, FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }
        }
    }
}

@Composable
private fun AdminExpandableSection(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(13.dp)) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onToggle), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    AdminText(title, 15, FontWeight.SemiBold)
                    AdminText(subtitle, 11, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun AdminInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    numeric: Boolean = false
) {
    Column(modifier) {
        AdminText(label, 12, FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth().heightIn(min = if (minLines > 1) 86.dp else 51.dp)
                .clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(11.dp)).padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = if (minLines > 1) Alignment.Top else Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) AdminText(placeholder, 13, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = minLines == 1,
                    maxLines = if (minLines == 1) 1 else 6,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                )
            }
            if (numeric) {
                Icon(Icons.Filled.Numbers, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AdminSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(9.dp))
        Box(Modifier.weight(1f)) {
            if (value.isBlank()) AdminText(placeholder, 14, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BasicTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface))
        }
        if (value.isNotBlank()) Icon(Icons.Filled.Close, null, modifier = Modifier.clickable { onValueChange("") }, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AdminFilterPill(text: String, onClick: () -> Unit) {
    Surface(Modifier.clip(RoundedCornerShape(100.dp)).clickable(onClick = onClick), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(100.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        AdminText(text, 11, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}

@Composable
private fun AdminRoundButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun AdminText(
    text: String,
    size: Int,
    weight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(text, modifier = modifier, fontSize = size.sp, fontWeight = weight, color = color, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}
