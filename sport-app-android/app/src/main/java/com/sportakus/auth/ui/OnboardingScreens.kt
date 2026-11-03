package com.sportakus.auth.ui

import com.sportakus.auth.ui.theme.sportakusTextColor
import com.sportakus.auth.ui.theme.sportakusSecondaryTextColor
import com.sportakus.auth.ui.theme.sportakusSelectedOptionBackground
import com.sportakus.auth.ui.theme.sportakusSelectedOptionTextColor
import com.sportakus.auth.ui.theme.sportakusInputTextColor
import com.sportakus.auth.ui.theme.sportakusInputBackground

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportakus.auth.R
import com.sportakus.auth.ui.theme.SportakusActionGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs


private val SportakusBrand = Color(0xFF507A12)

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SportakusActionGreen)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Replace res/drawable-nodpi/spk_logo.png with the designer's logo.
            // The Kotlin reference (R.drawable.spk_logo) can stay unchanged.
            Image(
                painter = painterResource(id = R.drawable.spk_logo),
                contentDescription = "Логотип СПК",
                modifier = Modifier.size(96.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(14.dp))
            DarkText("Добро пожаловать в", color = MaterialTheme.colorScheme.onPrimary, fontSize = 17.sp)
            DarkText("Спортакус", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        }
    }
}

@Composable
fun SafetyNoteScreen(darkTheme: Boolean, onAcknowledge: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))
        DarkText("Памятка", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = sportakusTextColor())
        Spacer(Modifier.height(28.dp))
        SafetyParagraph("Приложение не является медицинским обследованием и не определяет, безопасны ли физические нагрузки при конкретных заболеваниях.")
        Spacer(Modifier.height(24.dp))
        SafetyParagraph("Если медицинский специалист назначил вам ограничения, необходимо следовать его рекомендациям.")
        Spacer(Modifier.height(24.dp))
        SafetyParagraph("Не выполняйте задания через боль, выраженную усталость или ухудшение самочувствия. Вы можете остановить задание в любой момент без штрафа.")
        Spacer(Modifier.weight(1f))
        PrimaryGreenButton("Запомнил", onClick = onAcknowledge)
    }
}

@Composable
private fun SafetyParagraph(text: String) {
    DarkText(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        color = sportakusTextColor(),
        textAlign = TextAlign.Center,
        fontSize = 16.sp,
        lineHeight = 23.sp
    )
}

@Composable
fun IntroScreen(onStart: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1.1f).height(44.dp).clickable(onClick = onSkip),
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DarkText("Пропустить", color = MaterialTheme.colorScheme.primary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
            IntroProgressBars(active = 1, modifier = Modifier.weight(1.45f))
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(24.dp))
            IntroTaskCard(
                title = "Бег",
                description = "Маленькое описание",
                reward = "+ 10 ✦",
                height = 96.dp,
                horizontalInset = 24.dp
            )
            Spacer(Modifier.height(10.dp))
            IntroTaskCard(
                title = "Планка",
                description = "Маленькое описание",
                reward = "+33 ✦",
                height = 148.dp,
                horizontalInset = 0.dp
            )
            Spacer(Modifier.height(10.dp))
            IntroTaskCard(
                title = "Планка",
                description = "Маленькое описание",
                reward = "+33 ✦",
                height = 96.dp,
                horizontalInset = 24.dp
            )
            Spacer(Modifier.height(24.dp))
            DarkText(
                text = "Твой спорт — твоя\nигра.",
                fontSize = 29.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = sportakusTextColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            DarkText(
                text = "Мы помогаем отслеживать физическую активность и выполнять игровые задания.",
                fontSize = 16.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
                color = sportakusTextColor(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
        PrimaryGreenButton("Начнем", onClick = onStart)
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun IntroProgressBars(active: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (index == active - 1) SportakusBrand else MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}

@Composable
private fun IntroTaskCard(
    title: String,
    description: String,
    reward: String,
    height: androidx.compose.ui.unit.Dp,
    horizontalInset: androidx.compose.ui.unit.Dp
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalInset),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 5.dp,
        tonalElevation = 1.dp
    ) {
        Box(Modifier.fillMaxWidth().height(height).padding(horizontal = 20.dp, vertical = 15.dp)) {
            Column(Modifier.align(Alignment.BottomStart).padding(end = 108.dp)) {
                DarkText(title, color = sportakusTextColor(), fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                DarkText(description, color = sportakusTextColor(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Box(
                Modifier.align(Alignment.BottomEnd).clip(RoundedCornerShape(100.dp)).background(SportakusBrand)
                    .padding(horizontal = 15.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                DarkText(reward, color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionnaireOptionsScreen(
    question: String,
    description: String? = null,
    options: List<String>,
    selectedEncoded: String,
    onSelectionChange: (String) -> Unit,
    progress: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: (() -> Unit)? = null,
    chips: Boolean = false,
    multiple: Boolean = false,
    maxSelections: Int = Int.MAX_VALUE,
    categoryLabel: String? = null,
    secondCategoryLabel: String? = null,
    secondOptions: List<String> = emptyList(),
    secondSelectedEncoded: String = "",
    onSecondSelectionChange: ((String) -> Unit)? = null,
    screenTitle: String? = null,
    collapsibleOptions: Boolean = false,
    showCollapsedSelectionSummary: Boolean = true
) {
    var secondExpanded by rememberSaveable { mutableStateOf(false) }
    var optionsExpanded by rememberSaveable(question, collapsibleOptions) { mutableStateOf(!collapsibleOptions) }
    QuestionnaireShell(progress = progress, onBack = onBack, onNext = onNext, onSkip = onSkip) {
        if (screenTitle == null) {
            DarkText(
                question,
                color = sportakusTextColor(),
                fontSize = 22.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (description != null) {
                Spacer(Modifier.height(8.dp))
                DarkText(description, color = sportakusTextColor(), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        } else {
            DarkText(
                screenTitle,
                color = sportakusTextColor(),
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            val support = question.ifBlank { description.orEmpty() }
            if (support.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                DarkText(support, color = sportakusTextColor(), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            if (description != null && question.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                DarkText(description, color = sportakusTextColor(), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        val selected = selectedEncoded.split("|").filter { it.isNotBlank() }.toSet()
        if (categoryLabel != null) {
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().then(
                    if (collapsibleOptions) Modifier.clickable { optionsExpanded = !optionsExpanded } else Modifier
                ),
                color = MaterialTheme.colorScheme.surface,
                contentColor = sportakusTextColor(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (collapsibleOptions && optionsExpanded) SportakusBrand else MaterialTheme.colorScheme.outline)
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (collapsibleOptions) {
                        Column(Modifier.weight(1f)) {
                            DarkText(categoryLabel, color = sportakusTextColor(), fontSize = 12.sp)
                            if (showCollapsedSelectionSummary) {
                                DarkText(
                                    selected.firstOrNull() ?: "Выберите вариант",
                                    color = sportakusTextColor(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2
                                )
                            }
                        }
                        DarkText(if (optionsExpanded) "⌃" else "⌄", color = sportakusTextColor(), fontSize = 18.sp)
                    } else {
                        DarkText(categoryLabel, color = sportakusTextColor(), fontSize = 14.sp, modifier = Modifier.weight(1f))
                        DarkText("⌃", color = sportakusTextColor(), fontSize = 18.sp)
                    }
                }
            }
        }
        if (!collapsibleOptions || optionsExpanded) {
        Spacer(Modifier.height(8.dp))
        if (chips) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val active = option in selected
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (active) sportakusSelectedOptionBackground() else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (active) SportakusBrand else MaterialTheme.colorScheme.outline, RoundedCornerShape(9.dp))
                            .clickable {
                                val next = if (active) selected - option else if (selected.size < maxSelections) selected + option else selected
                                onSelectionChange(next.joinToString("|"))
                            }
                            .padding(horizontal = 11.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            DarkText(emojiForOption(option), color = if (active) sportakusSelectedOptionTextColor() else sportakusTextColor(), fontSize = 18.sp, maxLines = 1)
                            DarkText(option, color = if (active) sportakusSelectedOptionTextColor() else sportakusTextColor(), fontSize = 13.sp, lineHeight = 17.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal, maxLines = 2)
                        }
                    }
                }
            }
        } else if (multiple && maxSelections > 1) {
            options.forEach { option ->
                val active = option in selected
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (active) sportakusSelectedOptionBackground() else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (active) SportakusBrand else MaterialTheme.colorScheme.outline, RoundedCornerShape(9.dp))
                        .clickable {
                            val next = if (active) selected - option else if (selected.size < maxSelections) selected + option else selected
                            onSelectionChange(next.joinToString("|"))
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    DarkText(option, color = if (active) sportakusSelectedOptionTextColor() else sportakusTextColor(), fontSize = 14.sp, lineHeight = 19.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column {
                    options.forEachIndexed { index, option ->
                        val active = option in selected
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val next = if (multiple) {
                                    if (active) selected - option else if (selected.size < maxSelections) selected + option else selected
                                } else setOf(option)
                                onSelectionChange(next.joinToString("|"))
                            }.background(if (active && multiple) sportakusSelectedOptionBackground() else Color.Transparent)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DarkText(option, modifier = Modifier.weight(1f), color = if (active && multiple) sportakusSelectedOptionTextColor() else sportakusTextColor(), fontSize = 14.sp, lineHeight = 19.sp, fontWeight = if (active && multiple) FontWeight.SemiBold else FontWeight.Normal)
                            Box(
                                modifier = Modifier.size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background)
                                    .border(1.5.dp, if (active) SportakusBrand else MaterialTheme.colorScheme.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (active) Box(Modifier.size(8.dp).clip(CircleShape).background(SportakusBrand))
                            }
                        }
                        if (index < options.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    }
                }
            }
        }

        }

        if (secondCategoryLabel != null && secondOptions.isNotEmpty() && onSecondSelectionChange != null) {
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { secondExpanded = !secondExpanded },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        DarkText(secondCategoryLabel, color = sportakusTextColor(), fontSize = 12.sp)
                        DarkText(
                            secondSelectedEncoded.takeIf { it.isNotBlank() } ?: "Выберите количество",
                            color = sportakusTextColor(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                    }
                    DarkText(if (secondExpanded) "⌃" else "⌄", color = sportakusTextColor(), fontSize = 18.sp)
                }
            }
            if (secondExpanded) {
                Spacer(Modifier.height(6.dp))
                val secondSelected = secondSelectedEncoded.split("|").filter { it.isNotBlank() }.toSet()
                Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                    Column {
                        secondOptions.forEachIndexed { index, option ->
                            val active = option in secondSelected
                            Row(Modifier.fillMaxWidth().clickable {
                                onSecondSelectionChange(if (active) "" else option)
                            }.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                DarkText(option, Modifier.weight(1f), color = sportakusTextColor(), fontSize = 14.sp)
                                Box(Modifier.size(16.dp).clip(CircleShape)
                                    .background(if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background)
                                    .border(1.dp, if (active) SportakusBrand else MaterialTheme.colorScheme.background, CircleShape), contentAlignment = Alignment.Center) {
                                    if (active) Box(Modifier.size(8.dp).clip(CircleShape).background(SportakusBrand))
                                }
                            }
                            if (index < secondOptions.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        }
                    }
                }
            }
        }
    }
}

private fun emojiForOption(option: String): String = when (option) {
    "Фитнес" -> "🏋️"
    "Мотивация" -> "🏅"
    "Дисциплина" -> "⏱️"
    "Общение" -> "🧑‍🤝‍🧑"
    "Здоровье" -> "💚"
    "Самостоятельность" -> "💪"
    "Антистресс" -> "💢"
    "Цель" -> "📌"
    "ЗОЖ" -> "🏃"
    "Результат" -> "💡"
    "Геймификация" -> "📈"
    "Рейтинг" -> "🧭"
    else -> ""
}

@Composable
fun BodyMetricsScreen(
    height: String,
    onHeightChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    progress: Int = 3,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var activeMeasure by rememberSaveable { mutableStateOf("height") }
    var pickerExpanded by rememberSaveable { mutableStateOf(false) }
    // Midpoints are used only to centre the wheel before the user chooses a value;
    // the corresponding profile fields remain blank until an actual selection is made.
    val selectedHeight = (height.toIntOrNull() ?: 165).coerceIn(100, 230)
    val selectedWeight = (weight.toIntOrNull() ?: 115).coerceIn(30, 200)
    val isHeight = activeMeasure == "height"
    val values = if (isHeight) (100..230).toList() else (30..200).toList()
    val selectedValue = if (isHeight) selectedHeight else selectedWeight

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        QuestionnaireHeader(progress = progress, onBack = onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DarkText("Расскажите о себе", color = sportakusTextColor(), fontSize = 23.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            DarkText(
                "Мы помогаем отслеживать физическую активность и выполнять игровые задания. Ответы на вопросы помогут настроить задания с учётом вашего опыта, предпочтений и желаемой нагрузки.",
                color = sportakusTextColor(), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            MeasureSelectionField(
                label = "Рост",
                value = height.takeIf { it.isNotBlank() }?.let { "$it см" } ?: "Выберите рост",
                selected = isHeight,
                onClick = { activeMeasure = "height"; pickerExpanded = true }
            )
            Spacer(Modifier.height(12.dp))
            MeasureSelectionField(
                label = "Вес",
                value = weight.takeIf { it.isNotBlank() }?.let { "$it кг" } ?: "Выберите вес",
                selected = !isHeight,
                onClick = { activeMeasure = "weight"; pickerExpanded = true }
            )
            Spacer(Modifier.height(12.dp))
            DarkText("Выберите поле, затем прокрутите шкалу и нажмите «ОК».", color = sportakusTextColor(), fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
        }

        if (pickerExpanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { pickerExpanded = false },
                            contentAlignment = Alignment.Center
                        ) { DarkText("×", color = sportakusTextColor(), fontSize = 22.sp) }
                        Spacer(Modifier.weight(1f))
                        DarkText(if (isHeight) "Выберите рост" else "Выберите вес", color = sportakusTextColor(), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { pickerExpanded = false }) {
                            DarkText("ОК", color = sportakusTextColor(), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    DarkText(if (isHeight) "Рост, см" else "Вес, кг", color = sportakusTextColor(), fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    key(activeMeasure) {
                        ScrollableWheelColumn(
                            values = values.map(Int::toString),
                            selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0),
                            itemSuffix = if (isHeight) "см" else "кг",
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            onSelect = { value ->
                                if (isHeight) onHeightChange(value) else onWeightChange(value)
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding().padding(bottom = 4.dp).height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SportakusActionGreen, contentColor = Color.White, disabledContentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            DarkText("Следующее", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MeasureSelectionField(
    label: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, if (selected) SportakusBrand else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            DarkText(label, color = sportakusTextColor(), fontSize = 12.sp)
            Spacer(Modifier.height(3.dp))
            DarkText(value, color = sportakusTextColor(), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        DarkText("⌄", color = sportakusTextColor(), fontSize = 20.sp)
    }
}

@Composable
fun BirthDateScreen(
    day: String,
    month: String,
    year: String,
    gender: String,
    onGenderChange: (String) -> Unit,
    onDateChange: (String, String, String) -> Unit,
    progress: Int = 4,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val months = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
    var genderExpanded by rememberSaveable { mutableStateOf(false) }
    var dateExpanded by rememberSaveable { mutableStateOf(false) }
    val calendar = java.util.Calendar.getInstance()
    val currentYear = calendar.get(java.util.Calendar.YEAR)
    val selectedMonthIndex = months.indexOf(month).takeIf { it >= 0 } ?: calendar.get(java.util.Calendar.MONTH)
    val selectedYear = year.toIntOrNull()?.coerceIn(1940, currentYear) ?: currentYear
    val dayCount = daysInMonth(selectedYear, selectedMonthIndex + 1)
    val selectedDay = day.toIntOrNull()?.coerceIn(1, dayCount)
        ?: calendar.get(java.util.Calendar.DAY_OF_MONTH).coerceAtMost(dayCount)
    val selectedMonth = months[selectedMonthIndex]
    val years = (1940..currentYear).toList()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        QuestionnaireHeader(progress = progress, onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp)) {
            DarkText("Расскажите о себе", color = sportakusTextColor(), fontSize = 23.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            DarkText(
                "Ответы на вопросы помогут настроить задания с учётом вашего опыта, предпочтений и желаемой нагрузки.",
                color = sportakusTextColor(), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Surface(
                Modifier.fillMaxWidth().clickable { genderExpanded = !genderExpanded },
                color = MaterialTheme.colorScheme.surface,
                contentColor = sportakusTextColor(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (genderExpanded) SportakusBrand else MaterialTheme.colorScheme.outline)
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        DarkText("Пол", color = sportakusTextColor(), fontSize = 12.sp)
                        DarkText(gender.ifBlank { "Выберите пол" }, color = if (gender.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else sportakusTextColor(), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    DarkText(if (genderExpanded) "⌃" else "⌄", color = sportakusTextColor(), fontSize = 18.sp)
                }
            }
            if (genderExpanded) {
                Spacer(Modifier.height(5.dp))
                listOf("Мужчина", "Женщина", "Не указывать").forEach { option ->
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (gender == option) sportakusSelectedOptionBackground() else MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { onGenderChange(option); genderExpanded = false }
                            .padding(12.dp)
                    ) { DarkText(option, color = sportakusTextColor(), fontSize = 14.sp) }
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(
                Modifier.fillMaxWidth().clickable { dateExpanded = !dateExpanded },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (dateExpanded) SportakusBrand else MaterialTheme.colorScheme.outline)
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        DarkText("День рождения", color = sportakusTextColor(), fontSize = 12.sp)
                        val dateLabel = if (day.isNotBlank() && month.isNotBlank() && year.isNotBlank()) "$selectedDay $selectedMonth $selectedYear" else "Выберите дату рождения"
                        DarkText(dateLabel, color = if (day.isBlank() || month.isBlank() || year.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else sportakusTextColor(), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    DarkText(if (dateExpanded) "⌃" else "⌄", color = sportakusTextColor(), fontSize = 18.sp)
                }
            }
            if (dateExpanded) {
                Spacer(Modifier.height(10.dp))
                DarkText("Прокрутите колонки и остановите нужные значения по центру.", color = sportakusTextColor(), fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    DarkText("День", color = sportakusTextColor(), fontSize = 12.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    DarkText("Месяц", color = sportakusTextColor(), fontSize = 12.sp, modifier = Modifier.weight(1.4f), textAlign = TextAlign.Center)
                    DarkText("Год", color = sportakusTextColor(), fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScrollableWheelColumn(
                        modifier = Modifier.weight(0.8f).height(180.dp),
                        values = (1..dayCount).map(Int::toString),
                        selectedIndex = selectedDay - 1,
                        onSelect = { onDateChange(it, selectedMonth, selectedYear.toString()) }
                    )
                    ScrollableWheelColumn(
                        modifier = Modifier.weight(1.4f).height(180.dp),
                        values = months,
                        selectedIndex = selectedMonthIndex,
                        onSelect = { newMonth ->
                            val monthIndex = months.indexOf(newMonth) + 1
                            val adjustedDay = selectedDay.coerceAtMost(daysInMonth(selectedYear, monthIndex))
                            onDateChange(adjustedDay.toString(), newMonth, selectedYear.toString())
                        }
                    )
                    ScrollableWheelColumn(
                        modifier = Modifier.weight(1f).height(180.dp),
                        values = years.map(Int::toString),
                        selectedIndex = selectedYear - 1940,
                        onSelect = { newYearText ->
                            val newYear = newYearText.toIntOrNull() ?: selectedYear
                            val adjustedDay = selectedDay.coerceAtMost(daysInMonth(newYear, selectedMonthIndex + 1))
                            onDateChange(adjustedDay.toString(), selectedMonth, newYear.toString())
                        }
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { dateExpanded = false }) {
                        DarkText("ОК", color = sportakusTextColor(), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding().padding(bottom = 4.dp).height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SportakusActionGreen,
                contentColor = Color.White,
                disabledContentColor = Color.White
            )
        ) {
            DarkText("Следующее", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScrollableWheelColumn(
    modifier: Modifier = Modifier,
    values: List<String>,
    selectedIndex: Int,
    itemSuffix: String? = null,
    onSelect: (String) -> Unit
) {
    if (values.isEmpty()) return

    val safeIndex = selectedIndex.coerceIn(0, values.lastIndex)
    val itemHeight = 36.dp
    val wheelInk = sportakusInputTextColor()
    val wheelSelectedFill = sportakusSelectedOptionBackground()
    // Five 36 dp rows fit exactly into the 180 dp viewport. Two spacer rows
    // at both ends let even the first and last values sit in the center.
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = safeIndex)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val latestValues by rememberUpdatedState(values)
    val latestOnSelect by rememberUpdatedState(onSelect)
    var programmaticScroll by remember(listState) { mutableStateOf(false) }
    var lastReportedIndex by remember(listState) { mutableStateOf(safeIndex) }

    val centeredSlotIndex by remember(listState) {
        derivedStateOf {
            val layout = listState.layoutInfo
            val center = layout.viewportSize.height / 2
            layout.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - center)
            }?.index
        }
    }

    // A value change coming from another column (for example, changing the
    // month from a 31-day month to February) is synchronization, not user input.
    // Scroll it into place without publishing a second, stale selection.
    LaunchedEffect(safeIndex, values.size) {
        lastReportedIndex = safeIndex
        val expectedCenterSlot = safeIndex + 2
        if (centeredSlotIndex != expectedCenterSlot) {
            programmaticScroll = true
            try {
                listState.scrollToItem(safeIndex)
            } finally {
                programmaticScroll = false
            }
        }
    }

    // Only a real drag/fling in this particular wheel may publish a value.
    // Parent-driven corrections are ignored; row taps publish explicitly below.
    LaunchedEffect(listState, values.size) {
        var userScrollStarted = false
        snapshotFlow {
            Triple(listState.isScrollInProgress, centeredSlotIndex, programmaticScroll)
        }
            .distinctUntilChanged()
            .collect { (scrolling, slotIndex, isProgrammatic) ->
                if (scrolling && !isProgrammatic) {
                    userScrollStarted = true
                }
                if (!scrolling) {
                    if (userScrollStarted && !isProgrammatic && slotIndex != null) {
                        val valueIndex = slotIndex - 2
                        val currentValues = latestValues
                        if (valueIndex in currentValues.indices && valueIndex != lastReportedIndex) {
                            lastReportedIndex = valueIndex
                            latestOnSelect(currentValues[valueIndex])
                        }
                    }
                    userScrollStarted = false
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        userScrollEnabled = values.size > 1
    ) {
        items(count = values.size + 4, key = { it }) { slotIndex ->
            val valueIndex = slotIndex - 2
            if (valueIndex !in values.indices) {
                Box(Modifier.fillMaxWidth().height(itemHeight))
            } else {
                val value = values[valueIndex]
                val selected = slotIndex == centeredSlotIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selected) wheelSelectedFill else Color.Transparent)
                        .clickable {
                            scope.launch {
                                val currentValues = latestValues
                                if (valueIndex !in currentValues.indices) return@launch
                                programmaticScroll = true
                                try {
                                    // The tapped value's adapter index is valueIndex + 2;
                                    // placing firstVisibleItem at valueIndex centers it.
                                    listState.animateScrollToItem(valueIndex)
                                } finally {
                                    programmaticScroll = false
                                }
                                lastReportedIndex = valueIndex
                                latestOnSelect(currentValues[valueIndex])
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    DarkText(
                        text = if (itemSuffix == null) value else "$value $itemSuffix",
                        color = wheelInk,
                        fontSize = if (selected) 15.sp else 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun AuthWelcomeScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onRegisterEmail: () -> Unit,
    onLogin: () -> Unit,
    onGuest: () -> Unit,
    onProvider: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") serverBaseUrl: String,
    onServerSettings: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()
    ) {
        val logoSize = minOf(maxWidth - 48.dp, 350.dp).coerceAtLeast(180.dp)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(logoSize),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(logoSize * 0.075f, SportakusActionGreen)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.spk_logo),
                        contentDescription = "Логотип СПК",
                        modifier = Modifier.size(logoSize * 0.84f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            DarkText("Спортакус", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = sportakusTextColor())
            Spacer(Modifier.height(5.dp))
            DarkText("Двигайся и зарабатывай", fontSize = 16.sp, color = sportakusTextColor())
            Spacer(Modifier.height(16.dp))
            WelcomeProviderButton("Регистрация через Google", onClick = { onProvider("Google") })
            Spacer(Modifier.height(8.dp))
            WelcomeProviderButton("Регистрация через Яндекс", onClick = { onProvider("Яндекс") })
            Spacer(Modifier.height(8.dp))
            WelcomeProviderButton("Регистрация через почту", onClick = onRegisterEmail)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onGuest,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SportakusActionGreen, contentColor = Color.White, disabledContentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                DarkText("Войти как Гость", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DarkText("Уже есть аккаунт?", color = sportakusTextColor(), fontSize = 14.sp)
                TextButton(onClick = onLogin) { DarkText("Войти", color = SportakusActionGreen, fontWeight = FontWeight.SemiBold) }
            }
            if (com.sportakus.auth.BuildConfig.DEBUG) {
                TextButton(
                    onClick = onServerSettings,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    DarkText("Адрес сервера", color = sportakusSecondaryTextColor(), fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun WelcomeProviderButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SportakusActionGreen,
            contentColor = Color.White,
            disabledContentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        DarkText(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ContactConfirmationScreen(
    method: String,
    onMethodChange: (String) -> Unit,
    contact: String,
    onContactChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    title: String = "Подтверждение",
    description: String? = null,
    onBack: () -> Unit,
    onSendCode: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) { Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = sportakusTextColor()) }
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(15.dp))
        DarkText(title, modifier = Modifier.fillMaxWidth(), color = sportakusTextColor(), fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        DarkText(description ?: "Мы отправим на указанную почту или телефон код подтверждения.", modifier = Modifier.fillMaxWidth(), color = sportakusTextColor(), fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)) {
            listOf("Почта", "Телефон").forEach { option ->
                val active = option == method
                Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent).clickable { onMethodChange(option) }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    DarkText(option, color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else sportakusTextColor(), fontSize = 14.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        SportInputField(
            value = contact,
            onValueChange = onContactChange,
            placeholder = if (method == "Почта") "Почта" else "Номер телефона",
            keyboardType = if (method == "Почта") KeyboardType.Email else KeyboardType.Phone,
            leading = if (method == "Почта") Icons.Filled.MailOutline else Icons.Filled.PhoneAndroid
        )
        Spacer(Modifier.height(56.dp))
        PrimaryGreenButton("Отправить код", onClick = onSendCode)
    }
}

@Composable
fun CodeConfirmationScreen(
    title: String,
    description: String,
    code: String,
    onCodeChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    resendResetKey: Int,
    onResend: () -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    isSubmitting: Boolean = false,
    isResending: Boolean = false
) {
    var secondsLeft by rememberSaveable(title) { mutableStateOf(40) }
    LaunchedEffect(title, resendResetKey) {
        secondsLeft = 40
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) { Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = sportakusTextColor()) }
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(34.dp))
        DarkText(title, color = sportakusTextColor(), fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        DarkText(description, color = sportakusTextColor(), fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(34.dp))
        BasicTextField(
            value = code,
            onValueChange = { onCodeChange(it.filter(Char::isDigit).take(6)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            // The actual code is drawn in six visible boxes below; keep the input layer invisible.
            textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(6) { index ->
                            Surface(
                                modifier = Modifier.weight(1f).height(58.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (index == code.length) SportakusBrand else MaterialTheme.colorScheme.outline)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    DarkText(code.getOrNull(index)?.toString() ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = sportakusTextColor())
                                }
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(58.dp)) { innerTextField() }
                }
            }
        )
        Spacer(Modifier.height(18.dp))
        DarkText("Код истекает через 00:${secondsLeft.toString().padStart(2, '0')}", color = sportakusTextColor(), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            DarkText("Не пришёл код? ", color = sportakusTextColor(), fontSize = 13.sp)
            TextButton(onClick = onResend, enabled = secondsLeft == 0 && !isResending && !isSubmitting) {
                DarkText(
                    if (isResending) "Отправка..." else if (secondsLeft == 0) "Повторить" else "Повторить позже",
                    color = sportakusTextColor(),
                    fontSize = 13.sp
                )
            }
        }
        Spacer(Modifier.height(45.dp))
        PrimaryGreenButton(
            text = if (isSubmitting) {
                if (title.contains("регистрац", ignoreCase = true)) "Создание аккаунта..." else "Проверка кода..."
            } else "Подтвердить",
            onClick = onConfirm,
            enabled = !isSubmitting && !isResending
        )
    }
}

@Composable
fun DemoGuestHome(onBackToAuth: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(22.dp)) {
        DarkText("Спортакус", modifier = Modifier.fillMaxWidth(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = sportakusTextColor())
        DarkText("Твой спорт — твоя игра.", color = sportakusTextColor(), fontSize = 15.sp)
        Spacer(Modifier.height(24.dp))
        DarkText("Задания на сегодня", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = sportakusTextColor())
        Spacer(Modifier.height(12.dp))
        listOf("Бег" to "+10 ✦", "Планка" to "+33 ✦", "Прогулка" to "+20 ✦").forEach { item ->
            Surface(Modifier.fillMaxWidth().padding(vertical = 6.dp), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = SportakusBrand, modifier = Modifier.size(30.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        DarkText(item.first, color = sportakusTextColor(), fontWeight = FontWeight.SemiBold)
                        DarkText("Маленькое описание", color = sportakusTextColor(), fontSize = 12.sp)
                    }
                    DarkText(item.second, color = sportakusTextColor(), fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        PrimaryGreenButton("Вернуться к авторизации", onClick = onBackToAuth)
    }
}

@Composable
private fun QuestionnaireHeader(progress: Int, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth().height(34.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = sportakusTextColor(), modifier = Modifier.size(27.dp))
            }
            Spacer(Modifier.width(7.dp))
            DarkText("Давай узнаем друг друга получше", color = sportakusTextColor(), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth().padding(start = 55.dp, end = 52.dp), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            repeat(5) { index ->
                Box(
                    Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(100.dp))
                        .background(if (index == progress.coerceIn(1, 5) - 1) SportakusBrand else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}

@Composable
private fun QuestionnaireShell(
    progress: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
        QuestionnaireHeader(progress = progress, onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(top = 23.dp, bottom = 14.dp), content = content)
        if (onSkip != null) {
            Button(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SportakusActionGreen, contentColor = Color.White, disabledContentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) { DarkText("Пропустить", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SportakusActionGreen, contentColor = Color.White, disabledContentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) { DarkText("Следующее", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SportInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    leading: androidx.compose.ui.graphics.vector.ImageVector? = null,
    highlighted: Boolean = false
) {
    // Explicit ink color avoids inherited theme/text-style colors hiding typed characters.
    val isPhoneField = keyboardType == KeyboardType.Phone
    val fieldValue = if (isPhoneField) normalizeInternationalPhoneDigits(value) else value
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val inputInk = sportakusInputTextColor()
    val inputSurface = sportakusInputBackground()
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(inputSurface).border(1.dp, if (highlighted) SportakusBrand else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)).padding(horizontal = 13.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Icon(leading, contentDescription = null, tint = sportakusSecondaryTextColor(), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Box(Modifier.weight(1f)) {
            if (fieldValue.isEmpty() && (!isPhoneField || !focused)) DarkText(placeholder, color = inputInk, fontSize = 15.sp)
            CompositionLocalProvider(
                LocalContentColor provides inputInk,
                LocalTextStyle provides MaterialTheme.typography.bodyLarge.copy(
                    color = inputInk,
                    fontSize = 16.sp
                )
            ) {
                BasicTextField(
                    value = fieldValue,
                    onValueChange = { editedValue ->
                        onValueChange(if (isPhoneField) normalizeInternationalPhoneDigits(editedValue) else editedValue)
                    },
                    singleLine = true,
                    interactionSource = interactionSource,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    textStyle = TextStyle(color = inputInk, fontSize = 16.sp),
                    cursorBrush = SolidColor(inputInk),
                    visualTransformation = if (isPhoneField && (focused || fieldValue.isNotBlank())) InternationalPhoneVisualTransformation else VisualTransformation.None,
                    modifier = Modifier.fillMaxWidth().height(26.dp)
                )
            }
        }
    }
}


@Composable
private fun PrimaryGreenButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SportakusActionGreen, contentColor = Color.White, disabledContentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) { DarkText(text, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
}
