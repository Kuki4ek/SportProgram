package com.sportakus.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportakus.auth.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs


private val SportakusBrand = Color(0xFF507A12)
private val QuestionnaireInk = Color(0xFFF2F5EE)
private val QuestionnaireMutedInk = Color(0xFFF2F5EE)

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SportakusBrand)
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
            DarkText("Добро пожаловать в", color = Color(0xFFF2F5EE), fontSize = 17.sp)
            DarkText("Спортакус", color = Color(0xFFF2F5EE), fontWeight = FontWeight.Bold, fontSize = 26.sp)
        }
    }
}

@Composable
fun SafetyNoteScreen(darkTheme: Boolean, onAcknowledge: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101510))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))
        DarkText("Памятка", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF2F5EE))
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
        color = Color(0xFFF2F5EE),
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
            .background(Color(0xFF101510))
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
                border = BorderStroke(1.dp, Color(0xFF45523D))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DarkText("Пропустить", color = Color(0xFFF2F5EE), fontSize = 15.sp, fontWeight = FontWeight.Medium)
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
                color = Color(0xFFF2F5EE),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            DarkText(
                text = "Мы помогаем отслеживать физическую активность и выполнять игровые задания.",
                fontSize = 16.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFFF2F5EE),
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
                    .background(if (index == active - 1) SportakusBrand else Color(0xFF3E4937))
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
        color = Color(0xFF1B2219),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 5.dp,
        tonalElevation = 1.dp
    ) {
        Box(Modifier.fillMaxWidth().height(height).padding(horizontal = 20.dp, vertical = 15.dp)) {
            Column(Modifier.align(Alignment.BottomStart).padding(end = 108.dp)) {
                DarkText(title, color = Color(0xFFF2F5EE), fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                DarkText(description, color = Color(0xFFF2F5EE), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Box(
                Modifier.align(Alignment.BottomEnd).clip(RoundedCornerShape(100.dp)).background(SportakusBrand)
                    .padding(horizontal = 15.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                DarkText(reward, color = Color(0xFFF2F5EE), fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
                color = Color(0xFFF2F5EE),
                fontSize = 22.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (description != null) {
                Spacer(Modifier.height(8.dp))
                DarkText(description, color = Color(0xFFF2F5EE), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        } else {
            DarkText(
                screenTitle,
                color = Color(0xFFF2F5EE),
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            val support = question.ifBlank { description.orEmpty() }
            if (support.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                DarkText(support, color = Color(0xFFF2F5EE), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            if (description != null && question.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                DarkText(description, color = Color(0xFFF2F5EE), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        val selected = selectedEncoded.split("|").filter { it.isNotBlank() }.toSet()
        if (categoryLabel != null) {
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().then(
                    if (collapsibleOptions) Modifier.clickable { optionsExpanded = !optionsExpanded } else Modifier
                ),
                color = Color(0xFF1B2219),
                contentColor = QuestionnaireInk,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (collapsibleOptions && optionsExpanded) SportakusBrand else Color(0xFF45523D))
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (collapsibleOptions) {
                        Column(Modifier.weight(1f)) {
                            DarkText(categoryLabel, color = Color(0xFFF2F5EE), fontSize = 12.sp)
                            if (showCollapsedSelectionSummary) {
                                DarkText(
                                    selected.firstOrNull() ?: "Выберите вариант",
                                    color = Color(0xFFF2F5EE),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2
                                )
                            }
                        }
                        DarkText(if (optionsExpanded) "⌃" else "⌄", color = Color(0xFFF2F5EE), fontSize = 18.sp)
                    } else {
                        DarkText(categoryLabel, color = Color(0xFFF2F5EE), fontSize = 14.sp, modifier = Modifier.weight(1f))
                        DarkText("⌃", color = Color(0xFFF2F5EE), fontSize = 18.sp)
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
                            .background(if (active) SportakusBrand else Color(0xFF252E22))
                            .border(1.dp, if (active) SportakusBrand else Color(0xFF45523D), RoundedCornerShape(9.dp))
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
                            DarkText(emojiForOption(option), color = Color(0xFFF2F5EE), fontSize = 18.sp, maxLines = 1)
                            DarkText(option, color = Color(0xFFF2F5EE), fontSize = 13.sp, lineHeight = 17.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal, maxLines = 2)
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
                        .background(if (active) SportakusBrand else Color(0xFF252E22))
                        .border(1.dp, if (active) SportakusBrand else Color(0xFF45523D), RoundedCornerShape(9.dp))
                        .clickable {
                            val next = if (active) selected - option else if (selected.size < maxSelections) selected + option else selected
                            onSelectionChange(next.joinToString("|"))
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    DarkText(option, color = Color(0xFFF2F5EE), fontSize = 14.sp, lineHeight = 19.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1B2219),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF45523D))
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
                            }.background(if (active && multiple) SportakusBrand else Color.Transparent)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DarkText(option, modifier = Modifier.weight(1f), color = Color(0xFFF2F5EE), fontSize = 14.sp, lineHeight = 19.sp, fontWeight = if (active && multiple) FontWeight.SemiBold else FontWeight.Normal)
                            Box(
                                modifier = Modifier.size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (active) Color(0xFF1B2219) else Color(0xFF101510))
                                    .border(1.5.dp, if (active) SportakusBrand else Color(0xFF101510), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (active) Box(Modifier.size(8.dp).clip(CircleShape).background(SportakusBrand))
                            }
                        }
                        if (index < options.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF303A2D)))
                    }
                }
            }
        }

        }

        if (secondCategoryLabel != null && secondOptions.isNotEmpty() && onSecondSelectionChange != null) {
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { secondExpanded = !secondExpanded },
                color = Color(0xFF1B2219),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF45523D))
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        DarkText(secondCategoryLabel, color = Color(0xFFF2F5EE), fontSize = 12.sp)
                        DarkText(
                            secondSelectedEncoded.takeIf { it.isNotBlank() } ?: "Выберите количество",
                            color = Color(0xFFF2F5EE),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                    }
                    DarkText(if (secondExpanded) "⌃" else "⌄", color = Color(0xFFF2F5EE), fontSize = 18.sp)
                }
            }
            if (secondExpanded) {
                Spacer(Modifier.height(6.dp))
                val secondSelected = secondSelectedEncoded.split("|").filter { it.isNotBlank() }.toSet()
                Surface(Modifier.fillMaxWidth(), color = Color(0xFF1B2219), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFF45523D))) {
                    Column {
                        secondOptions.forEachIndexed { index, option ->
                            val active = option in secondSelected
                            Row(Modifier.fillMaxWidth().clickable {
                                onSecondSelectionChange(if (active) "" else option)
                            }.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                DarkText(option, Modifier.weight(1f), color = Color(0xFFF2F5EE), fontSize = 14.sp)
                                Box(Modifier.size(16.dp).clip(CircleShape)
                                    .background(if (active) Color(0xFF1B2219) else Color(0xFF101510))
                                    .border(1.dp, if (active) SportakusBrand else Color(0xFF101510), CircleShape), contentAlignment = Alignment.Center) {
                                    if (active) Box(Modifier.size(8.dp).clip(CircleShape).background(SportakusBrand))
                                }
                            }
                            if (index < secondOptions.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF303A2D)))
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
    var activeMeasure by rememberSaveable { mutableStateOf("weight") }
    var pickerExpanded by rememberSaveable { mutableStateOf(true) }
    val selectedHeight = (height.toIntOrNull() ?: 170).coerceIn(100, 230)
    val selectedWeight = (weight.toIntOrNull() ?: 65).coerceIn(30, 200)
    val isHeight = activeMeasure == "height"
    val values = if (isHeight) (100..230).toList() else (30..200).toList()
    val selectedValue = if (isHeight) selectedHeight else selectedWeight

    Column(Modifier.fillMaxSize().background(Color(0xFF101510)).statusBarsPadding()) {
        QuestionnaireHeader(progress = progress, onBack = onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DarkText("Расскажите о себе", color = Color(0xFFF2F5EE), fontSize = 23.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            DarkText(
                "Мы помогаем отслеживать физическую активность и выполнять игровые задания. Ответы на вопросы помогут настроить задания с учётом вашего опыта, предпочтений и желаемой нагрузки.",
                color = Color(0xFFF2F5EE), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center
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
            DarkText("Выберите поле, затем прокрутите шкалу и нажмите «ОК».", color = Color(0xFFF2F5EE), fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
        }

        if (pickerExpanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1B2219),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF252E22)).clickable { pickerExpanded = false },
                            contentAlignment = Alignment.Center
                        ) { DarkText("×", color = Color(0xFFF2F5EE), fontSize = 22.sp) }
                        Spacer(Modifier.weight(1f))
                        DarkText(if (isHeight) "Выберите рост" else "Выберите вес", color = Color(0xFFF2F5EE), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { pickerExpanded = false }) {
                            DarkText("ОК", color = Color(0xFFF2F5EE), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    DarkText(if (isHeight) "Рост, см" else "Вес, кг", color = Color(0xFFF2F5EE), fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    key(activeMeasure) {
                        ScrollableWheelColumn(
                            values = values.map(Int::toString),
                            selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0),
                            itemSuffix = if (isHeight) "см" else "кг",
                            modifier = Modifier.fillMaxWidth().height(150.dp),
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
            colors = ButtonDefaults.buttonColors(containerColor = SportakusBrand, contentColor = Color(0xFFF2F5EE), disabledContentColor = Color(0xFFF2F5EE)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            DarkText("Следующее", color = Color(0xFFF2F5EE), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
            .background(Color(0xFF1B2219))
            .border(1.5.dp, if (selected) SportakusBrand else Color(0xFF45523D), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            DarkText(label, color = Color(0xFFF2F5EE), fontSize = 12.sp)
            Spacer(Modifier.height(3.dp))
            DarkText(value, color = Color(0xFFF2F5EE), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        DarkText("⌄", color = Color(0xFFF2F5EE), fontSize = 20.sp)
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
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val selectedMonthIndex = (months.indexOf(month).takeIf { it >= 0 } ?: 2)
    val selectedYear = year.toIntOrNull()?.coerceIn(1940, currentYear) ?: 2003
    val dayCount = daysInMonth(selectedYear, selectedMonthIndex + 1)
    val selectedDay = day.toIntOrNull()?.coerceIn(1, dayCount) ?: 29.coerceAtMost(dayCount)
    val selectedMonth = months[selectedMonthIndex]
    val years = (1940..currentYear).toList()

    Column(Modifier.fillMaxSize().background(Color(0xFF101510)).statusBarsPadding()) {
        QuestionnaireHeader(progress = progress, onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp)) {
            DarkText("Расскажите о себе", color = Color(0xFFF2F5EE), fontSize = 23.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            DarkText(
                "Ответы на вопросы помогут настроить задания с учётом вашего опыта, предпочтений и желаемой нагрузки.",
                color = Color(0xFFF2F5EE), fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Surface(
                Modifier.fillMaxWidth().clickable { genderExpanded = !genderExpanded },
                color = Color(0xFF1B2219),
                contentColor = QuestionnaireInk,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (genderExpanded) SportakusBrand else Color(0xFF45523D))
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        DarkText("Пол", color = Color(0xFFF2F5EE), fontSize = 12.sp)
                        DarkText(gender, color = Color(0xFFF2F5EE), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    DarkText(if (genderExpanded) "⌃" else "⌄", color = Color(0xFFF2F5EE), fontSize = 18.sp)
                }
            }
            if (genderExpanded) {
                Spacer(Modifier.height(5.dp))
                listOf("Мужчина", "Женщина", "Не указывать").forEach { option ->
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (gender == option) Color(0xFF36551D) else Color(0xFF1B2219))
                            .border(1.dp, Color(0xFF45523D), RoundedCornerShape(8.dp))
                            .clickable { onGenderChange(option); genderExpanded = false }
                            .padding(12.dp)
                    ) { DarkText(option, color = Color(0xFFF2F5EE), fontSize = 14.sp) }
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(
                Modifier.fillMaxWidth().clickable { dateExpanded = !dateExpanded },
                color = Color(0xFF1B2219),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (dateExpanded) SportakusBrand else Color(0xFF45523D))
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        DarkText("День рождения", color = Color(0xFFF2F5EE), fontSize = 12.sp)
                        DarkText("$selectedDay $selectedMonth $selectedYear", color = Color(0xFFF2F5EE), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    DarkText(if (dateExpanded) "⌃" else "⌄", color = Color(0xFFF2F5EE), fontSize = 18.sp)
                }
            }
            if (dateExpanded) {
                Spacer(Modifier.height(10.dp))
                DarkText("Прокрутите колонки и остановите нужные значения по центру.", color = Color(0xFFF2F5EE), fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    DarkText("День", color = Color(0xFFF2F5EE), fontSize = 12.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    DarkText("Месяц", color = Color(0xFFF2F5EE), fontSize = 12.sp, modifier = Modifier.weight(1.4f), textAlign = TextAlign.Center)
                    DarkText("Год", color = Color(0xFFF2F5EE), fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    key(dayCount) {
                        ScrollableWheelColumn(
                            modifier = Modifier.weight(0.8f).height(154.dp),
                            values = (1..dayCount).map(Int::toString),
                            selectedIndex = selectedDay - 1,
                            onSelect = { onDateChange(it, selectedMonth, selectedYear.toString()) }
                        )
                    }
                    ScrollableWheelColumn(
                        modifier = Modifier.weight(1.4f).height(154.dp),
                        values = months,
                        selectedIndex = selectedMonthIndex,
                        onSelect = { newMonth ->
                            val monthIndex = months.indexOf(newMonth) + 1
                            val adjustedDay = selectedDay.coerceAtMost(daysInMonth(selectedYear, monthIndex))
                            onDateChange(adjustedDay.toString(), newMonth, selectedYear.toString())
                        }
                    )
                    ScrollableWheelColumn(
                        modifier = Modifier.weight(1f).height(154.dp),
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
                        DarkText("ОК", color = Color(0xFFF2F5EE), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding().padding(bottom = 4.dp).height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SportakusBrand,
                contentColor = Color(0xFFF2F5EE),
                disabledContentColor = Color(0xFFF2F5EE)
            )
        ) {
            DarkText("Следующее", color = Color(0xFFF2F5EE), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
    val safeIndex = selectedIndex.coerceIn(0, values.lastIndex.coerceAtLeast(0))
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = safeIndex)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val centeredSlotIndex by remember(listState) {
        derivedStateOf {
            val layout = listState.layoutInfo
            val center = layout.viewportSize.height / 2
            layout.visibleItemsInfo.minByOrNull { abs((it.offset + it.size / 2) - center) }?.index
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to centeredSlotIndex }
            .distinctUntilChanged()
            .collect { state: Pair<Boolean, Int?> ->
                val scrolling = state.first
                val slotIndex = state.second
                if (!scrolling && slotIndex != null) {
                    val valueIndex = slotIndex - 2
                    if (valueIndex in values.indices) onSelect(values[valueIndex])
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        userScrollEnabled = values.isNotEmpty()
    ) {
        items(count = values.size + 4, key = { it }) { slotIndex ->
            val valueIndex = slotIndex - 2
            if (valueIndex !in values.indices) {
                Box(Modifier.fillMaxWidth().height(38.dp))
            } else {
                val value = values[valueIndex]
                val selected = slotIndex == centeredSlotIndex
                Box(
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selected) Color(0xFF36551D) else Color.Transparent)
                        .clickable {
                            onSelect(value)
                            scope.launch { listState.animateScrollToItem(valueIndex) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    DarkText(
                        text = if (itemSuffix == null) value else "$value $itemSuffix",
                        color = Color(0xFFF2F5EE),
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
    serverBaseUrl: String,
    onServerSettings: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color(0xFF101510)).statusBarsPadding().navigationBarsPadding()
    ) {
        val logoSize = minOf(maxWidth - 24.dp, 250.dp).coerceAtLeast(180.dp)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onServerSettings) {
                    DarkText("Адрес сервера", color = Color(0xFFF2F5EE), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            DarkText("API: $serverBaseUrl", color = Color(0xFFB8C5AE), fontSize = 10.sp, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Surface(
                modifier = Modifier.size(logoSize),
                shape = CircleShape,
                color = Color(0xFF101510),
                border = BorderStroke(logoSize * 0.075f, SportakusBrand)
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
            DarkText("Спортакус", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF2F5EE))
            Spacer(Modifier.height(5.dp))
            DarkText("Двигайся и зарабатывай", fontSize = 16.sp, color = Color(0xFFF2F5EE))
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
                colors = ButtonDefaults.buttonColors(containerColor = SportakusBrand, contentColor = Color(0xFFF2F5EE), disabledContentColor = Color(0xFFF2F5EE)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                DarkText("Войти как Гость", color = Color(0xFFF2F5EE), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DarkText("Уже есть аккаунт?", color = Color(0xFFF2F5EE), fontSize = 14.sp)
                TextButton(onClick = onLogin) { DarkText("Войти", color = Color(0xFFF2F5EE), fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun WelcomeProviderButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SportakusBrand,
            contentColor = Color(0xFFF2F5EE),
            disabledContentColor = Color(0xFFF2F5EE)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        DarkText(
            text = text,
            color = Color(0xFFF2F5EE),
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
    Column(Modifier.fillMaxSize().background(Color(0xFF101510)).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) { Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = QuestionnaireInk) }
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(15.dp))
        DarkText(title, modifier = Modifier.fillMaxWidth(), color = Color(0xFFF2F5EE), fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        DarkText(description ?: "Мы отправим на указанную почту или телефон код подтверждения.", modifier = Modifier.fillMaxWidth(), color = Color(0xFFF2F5EE), fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color(0xFF252E22)).padding(4.dp)) {
            listOf("Почта", "Телефон").forEach { option ->
                val active = option == method
                Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (active) SportakusBrand else Color.Transparent).clickable { onMethodChange(option) }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    DarkText(option, color = Color(0xFFF2F5EE), fontSize = 14.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
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
    onConfirm: () -> Unit
) {
    var secondsLeft by rememberSaveable(title) { mutableStateOf(40) }
    LaunchedEffect(title, resendResetKey) {
        secondsLeft = 40
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF101510)).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) { Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = QuestionnaireInk) }
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(34.dp))
        DarkText(title, color = Color(0xFFF2F5EE), fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        DarkText(description, color = Color(0xFFF2F5EE), fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
                                color = Color(0xFF1B2219),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (index == code.length) SportakusBrand else Color(0xFF45523D))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    DarkText(code.getOrNull(index)?.toString() ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF2F5EE))
                                }
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(58.dp)) { innerTextField() }
                }
            }
        )
        Spacer(Modifier.height(18.dp))
        DarkText("Код истекает через 00:${secondsLeft.toString().padStart(2, '0')}", color = Color(0xFFF2F5EE), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            DarkText("Не пришёл код? ", color = Color(0xFFF2F5EE), fontSize = 13.sp)
            TextButton(onClick = onResend, enabled = secondsLeft == 0) {
                DarkText(if (secondsLeft == 0) "Повторить" else "Повторить позже", color = Color(0xFFF2F5EE), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(45.dp))
        PrimaryGreenButton("Подтвердить", onClick = onConfirm)
    }
}

@Composable
fun DemoGuestHome(onBackToAuth: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFF101510)).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(22.dp)) {
        DarkText("Спортакус", modifier = Modifier.fillMaxWidth(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF2F5EE))
        DarkText("Твой спорт — твоя игра.", color = Color(0xFFF2F5EE), fontSize = 15.sp)
        Spacer(Modifier.height(24.dp))
        DarkText("Задания на сегодня", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF2F5EE))
        Spacer(Modifier.height(12.dp))
        listOf("Бег" to "+10 ✦", "Планка" to "+33 ✦", "Прогулка" to "+20 ✦").forEach { item ->
            Surface(Modifier.fillMaxWidth().padding(vertical = 6.dp), color = Color(0xFF1B2219), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color(0xFF45523D))) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = SportakusBrand, modifier = Modifier.size(30.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        DarkText(item.first, color = Color(0xFFF2F5EE), fontWeight = FontWeight.SemiBold)
                        DarkText("Маленькое описание", color = Color(0xFFF2F5EE), fontSize = 12.sp)
                    }
                    DarkText(item.second, color = Color(0xFFF2F5EE), fontWeight = FontWeight.Bold)
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
                Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = Color(0xFFF2F5EE), modifier = Modifier.size(27.dp))
            }
            Spacer(Modifier.width(7.dp))
            DarkText("Давай узнаем друг друга получше", color = Color(0xFFF2F5EE), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth().padding(start = 55.dp, end = 52.dp), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            repeat(5) { index ->
                Box(
                    Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(100.dp))
                        .background(if (index == progress.coerceIn(1, 5) - 1) SportakusBrand else Color(0xFF3E4937))
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
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF101510)).statusBarsPadding().navigationBarsPadding()) {
        QuestionnaireHeader(progress = progress, onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(top = 23.dp, bottom = 14.dp), content = content)
        if (onSkip != null) {
            Button(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SportakusBrand, contentColor = Color(0xFFF2F5EE), disabledContentColor = Color(0xFFF2F5EE)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) { DarkText("Пропустить", color = Color(0xFFF2F5EE), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SportakusBrand, contentColor = Color(0xFFF2F5EE), disabledContentColor = Color(0xFFF2F5EE)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) { DarkText("Следующее", color = Color(0xFFF2F5EE), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
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
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1B2219)).border(1.dp, if (highlighted) SportakusBrand else Color(0xFF45523D), RoundedCornerShape(12.dp)).padding(horizontal = 13.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Icon(leading, contentDescription = null, tint = QuestionnaireMutedInk, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) DarkText(placeholder, color = Color(0xFFF2F5EE), fontSize = 15.sp)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(color = Color(0xFFF2F5EE), fontSize = 16.sp),
                cursorBrush = SolidColor(SportakusBrand),
                modifier = Modifier.fillMaxWidth().height(26.dp)
            )
        }
    }
}


@Composable
private fun PrimaryGreenButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SportakusBrand, contentColor = Color(0xFFF2F5EE), disabledContentColor = Color(0xFFF2F5EE)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) { DarkText(text, color = Color(0xFFF2F5EE), fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
}
