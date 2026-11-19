package com.sportakus.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import com.sportakus.auth.data.api.UserProfile
import com.sportakus.auth.data.api.ProfileUpdateRequest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Temporary frontend model. Replace its in-memory source with the workout API when the DB endpoints are ready. */
data class WorkoutExercise(
    val id: String,
    val title: String,
    val shortDescription: String,
    val description: String,
    val durationMinutes: Int,
    val distance: String,
    val calories: Int,
    val coins: Int,
    val contraindications: String,
    val tips: String,
    val steps: String,
    val published: Boolean = true,
    val intensity: String = "",
    val category: String = "Кардио"
)

fun demoWorkoutExercises(): List<WorkoutExercise> = listOf(
    WorkoutExercise(
        id = "demo-cardio-run",
        title = "Бег в комфортном темпе",
        shortDescription = "Пример кардиотренировки для развития выносливости",
        description = "Пример программы для категории «Кардио». Темп и продолжительность можно адаптировать под подготовку.",
        durationMinutes = 0,
        distance = "—",
        calories = 0,
        coins = 0,
        contraindications = "Острая боль; ограничения, назначенные врачом",
        tips = """Начните с разминки
Сохраняйте комфортный темп
Завершите занятие спокойной ходьбой""".trimIndent(),
        steps = """Разминка|Лёгкая ходьба и динамические упражнения
Основная часть|Бег в комфортном темпе или чередование бега и ходьбы
Заминка|Спокойная ходьба и мягкая растяжка""".trimIndent(),
        category = "Кардио"
    ),
    WorkoutExercise(
        id = "demo-strength-pushups",
        title = "Отжимания",
        shortDescription = "Пример силовой тренировки с собственным весом",
        description = "Пример программы для категории «Сила». Начните с подходящего варианта отжиманий и не работайте через боль.",
        durationMinutes = 0,
        distance = "—",
        calories = 0,
        coins = 0,
        contraindications = "Боль в запястьях или плечах; ограничения врача",
        tips = """Держите корпус ровно
Не опускайте голову
Выберите удобную опору""".trimIndent(),
        steps = """Подготовка|Примите устойчивое положение
Выполнение|Плавно согните руки и вернитесь в исходное положение
Завершение|Отдохните и повторите по самочувствию""".trimIndent(),
        category = "Сила"
    ),
    WorkoutExercise(
        id = "demo-pilates-mobility",
        title = "Основы пилатеса",
        shortDescription = "Контроль движений и укрепление мышц корпуса",
        description = "Пример спокойной программы для категории «Пилатес» с акцентом на дыхание и контролируемые движения.",
        durationMinutes = 0,
        distance = "—",
        calories = 0,
        coins = 0,
        contraindications = "Острая боль; ограничения, назначенные врачом",
        tips = """Двигайтесь плавно
Не задерживайте дыхание
Соблюдайте комфортную амплитуду""".trimIndent(),
        steps = """Подготовка|Подготовьте коврик
Основная часть|Выполняйте простые контролируемые движения
Завершение|Сделайте несколько спокойных вдохов""".trimIndent(),
        category = "Пилатес"
    ),
    WorkoutExercise(
        id = "demo-recovery-stretch",
        title = "Мягкая растяжка",
        shortDescription = "Небольшой комплекс для восстановления и подвижности",
        description = "Пример занятия для категории «Восстановление». Выполняйте движения без боли и резких усилий.",
        durationMinutes = 0,
        distance = "—",
        calories = 0,
        coins = 0,
        contraindications = "Острая боль; ограничения, назначенные врачом",
        tips = """Не пружиньте в растяжке
Дышите спокойно
Не выходите за комфортную амплитуду""".trimIndent(),
        steps = """Подготовка|Выберите спокойное место
Растяжка|Мягко проработайте основные группы мышц
Завершение|Вернитесь в нейтральное положение""".trimIndent(),
        category = "Восстановление"
    )
)

private enum class MainTab { HOME, SEARCH, ACTIVITY, PROFILE }

@Composable
fun MainAppScreen(
    username: String,
    profile: UserProfile,
    exercises: List<WorkoutExercise>,
    isAdmin: Boolean,
    roleLabel: String,
    activeWorkoutTitle: String?,
    activeWorkoutSeconds: Int,
    activeWorkoutPaused: Boolean,
    onToggleWorkoutPause: () -> Unit,
    onFinishWorkout: () -> Unit,
    onOpenExercise: (String) -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenPartners: () -> Unit,
    onAddPartner: () -> Unit,
    onLogout: () -> Unit,
    onRequestEmailCode: (String, (Boolean, String) -> Unit) -> Unit,
    onProfileSave: (ProfileUpdateRequest, (Boolean, String) -> Unit) -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var profileEditorOpen by rememberSaveable { mutableStateOf(false) }
    var quickStartOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(activeWorkoutTitle) {
        if (!activeWorkoutTitle.isNullOrBlank()) tab = MainTab.HOME
    }
    val context = LocalContext.current
    val searchPreferences = remember(context) {
        context.getSharedPreferences("sportakus_search_history", android.content.Context.MODE_PRIVATE)
    }
    var recentSearches by remember {
        mutableStateOf(searchPreferences.getString("recent_searches_v2", "")?.lines()?.filter { it.isNotBlank() }.orEmpty())
    }
    val recordSearch: (String) -> Unit = { raw ->
        val term = raw.trim()
        if (term.isNotBlank()) {
            val updated = (listOf(term) + recentSearches.filterNot { it.equals(term, ignoreCase = true) }).take(8)
            recentSearches = updated
            searchPreferences.edit().putString("recent_searches_v2", updated.joinToString("\n") { it.replace("\n", " ").replace("\r", " ") }).apply()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        when (tab) {
            MainTab.HOME -> HomeDashboardScreen(
                username = username,
                profile = profile,
                activeWorkoutTitle = activeWorkoutTitle,
                activeWorkoutSeconds = activeWorkoutSeconds,
                activeWorkoutPaused = activeWorkoutPaused,
                onToggleWorkoutPause = onToggleWorkoutPause,
                onFinishWorkout = onFinishWorkout,
                exercises = exercises.filter { it.published },
                onSeeAll = { tab = MainTab.SEARCH },
                onOpenExercise = onOpenExercise,
                onProfile = { tab = MainTab.PROFILE }
            )
            MainTab.SEARCH -> SearchCatalogScreen(
                exercises = exercises.filter { it.published },
                recentSearches = recentSearches,
                onSearchSubmitted = recordSearch,
                onOpenExercise = onOpenExercise,
                onBack = { tab = MainTab.HOME }
            )
            MainTab.ACTIVITY -> ActivityOverviewScreen(profile = profile)
            MainTab.PROFILE -> if (profileEditorOpen) {
                ProfileEditScreen(
                    profile = profile,
                    onBack = { profileEditorOpen = false },
                    onRequestEmailCode = onRequestEmailCode,
                    onSave = { update, completed ->
                        onProfileSave(update) { saved, message ->
                            if (saved) profileEditorOpen = false
                            completed(saved, message)
                        }
                    }
                )
            } else {
                ProfileOverviewScreen(
                    profile = profile,
                    isAdmin = isAdmin,
                    roleLabel = roleLabel,
                    onBack = { tab = MainTab.HOME },
                    onOpenAdmin = onOpenAdmin,
                    onOpenPartners = onOpenPartners,
                    onAddPartner = onAddPartner,
                    onLogout = onLogout,
                    onEditProfile = { profileEditorOpen = true }
                )
            }
        }

        // The dock floats above scrollable content, just like the supplied design.
        MainBottomBar(
            selected = tab,
            onSelect = { tab = it },
            onQuickStart = { quickStartOpen = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (quickStartOpen) {
        QuickStartWorkoutDialog(
            exercises = exercises.filter { it.published },
            onDismiss = { quickStartOpen = false },
            onSelectExercise = { id ->
                quickStartOpen = false
                onOpenExercise(id)
            }
        )
    }
}

@Composable
private fun HomeDashboardScreen(
    username: String,
    profile: UserProfile,
    activeWorkoutTitle: String?,
    activeWorkoutSeconds: Int,
    activeWorkoutPaused: Boolean,
    onToggleWorkoutPause: () -> Unit,
    onFinishWorkout: () -> Unit,
    exercises: List<WorkoutExercise>,
    onSeeAll: () -> Unit,
    onOpenExercise: (String) -> Unit,
    onProfile: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 112.dp)
    ) {
        val hasActiveWorkout = !activeWorkoutTitle.isNullOrBlank()
        Box(Modifier.fillMaxWidth()) {
            Box(
                Modifier.fillMaxWidth()
                    .height(if (hasActiveWorkout) 214.dp else 145.dp)
                    .clip(RoundedCornerShape(bottomStart = 38.dp, bottomEnd = 38.dp))
                    .background(Color.White)
            )
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                Spacer(Modifier.height(53.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppText("Привет,", 19, FontWeight.SemiBold)
                            Spacer(Modifier.width(5.dp))
                            AppText(username.ifBlank { "Спортсмен" }, 19, FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        AppText("Твой спорт — твоя игра", 12, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(
                        Modifier.size(54.dp).clip(CircleShape).background(Color.Red).clickable(onClick = onProfile)
                    )
                }
                if (hasActiveWorkout) {
                    Spacer(Modifier.height(17.dp))
                    Surface(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF496900),
                        border = BorderStroke(1.dp, Color(0xFFD8E4B8)),
                        shadowElevation = 5.dp
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleWorkoutPause,
                                modifier = Modifier.size(48.dp)
                                    .border(3.dp, Color(0xFF00C4D9), CircleShape)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF254400))
                            ) {
                                Icon(
                                    if (activeWorkoutPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                    contentDescription = if (activeWorkoutPaused) "Продолжить тренировку" else "Приостановить тренировку",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                                AppText("В процессе", 12, color = Color(0xFF00C4D9), weight = FontWeight.Medium)
                                AppText(activeWorkoutTitle.orEmpty(), 17, FontWeight.SemiBold, color = Color.White, maxLines = 1)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                AppText(formatElapsedTime(activeWorkoutSeconds), 22, FontWeight.Bold, color = Color(0xFFFFD21F))
                                if (activeWorkoutPaused) AppText("Пауза", 10, FontWeight.Medium, color = Color.White.copy(alpha = .85f))
                            }
                            Spacer(Modifier.width(5.dp))
                            IconButton(onClick = onFinishWorkout, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Завершить тренировку", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(24.dp))
        AppText("Недавние действия", 19, FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (profile.activityValues.isEmpty()) {
            EmptyActivityChart("Данные активности появятся после завершённых тренировок.")
        } else {
            ActivityLineChart(Modifier.fillMaxWidth().height(150.dp), profile.activityValues)
        }
        Spacer(Modifier.height(14.dp))
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(Modifier.padding(vertical = 14.dp, horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                StatText(profile.totalWorkouts.toString(), "тренировок")
                AppText("•", 12, color = MaterialTheme.colorScheme.onSurfaceVariant)
                StatText(profile.sportCoins.toString(), "СпортКоинов", Color(0xFF18B9D4))
                AppText("•", 12, color = MaterialTheme.colorScheme.onSurfaceVariant)
                StatText(profile.totalMinutes.toString(), "минут")
            }
        }
        Spacer(Modifier.height(26.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppText("Программы тренировок", 18, FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onSeeAll) { AppText("Посмотреть все", 13, color = MaterialTheme.colorScheme.primary, weight = FontWeight.Medium) }
        }
        Spacer(Modifier.height(5.dp))
        if (exercises.isEmpty()) {
            EmptyCatalogPlaceholder("Каталог тренировок пока пуст.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                val featured = exercises
                featured.forEachIndexed { index, exercise ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.width(42.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Canvas(Modifier.fillMaxSize()) {
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                drawLine(
                                    color = Color(0xFF8B927E),
                                    start = Offset(centerX, if (index == 0) centerY else 0f),
                                    end = Offset(centerX, if (index == featured.lastIndex) centerY else size.height),
                                    strokeWidth = 1.2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                                )
                            }
                            Box(
                                Modifier.size(24.dp)
                                    .background(MaterialTheme.colorScheme.background, CircleShape)
                                    .border(3.dp, Color(0xFFFFB900), CircleShape)
                            )
                        }
                        FeaturedWorkoutCard(
                            exercise, Modifier.weight(1f).height(100.dp)
                        ) { onOpenExercise(exercise.id) }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        }
    }
}

private fun formatElapsedTime(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 60, safe % 60)
}

@Composable
private fun EmptyActivityChart(message: String) {
    Surface(
        Modifier.fillMaxWidth().height(150.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            AppText(message, 12, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(20.dp), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ActivityLineChart(modifier: Modifier = Modifier, values: List<Int>) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        listOf(0.15f, 0.5f, 0.85f).forEach { fraction ->
            drawLine(
                color = Color(0xFFE6EBDD),
                start = Offset(0f, h * fraction),
                end = Offset(w, h * fraction),
                strokeWidth = 1.dp.toPx()
            )
        }
        if (values.isEmpty()) return@Canvas
        val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
        val points = values.mapIndexed { index, value ->
            val x = if (values.size == 1) w / 2f else w * index / (values.size - 1).toFloat()
            val y = h - (value.toFloat() / maxValue.coerceAtLeast(1)) * h * 0.82f - h * 0.08f
            Offset(x, y)
        }
        if (points.size == 1) {
            drawCircle(Color(0xFF79B900), radius = 5.dp.toPx(), center = points.first())
        } else {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (index in 0 until points.lastIndex) {
                    val previous = points[(index - 1).coerceAtLeast(0)]
                    val current = points[index]
                    val next = points[index + 1]
                    val following = points[(index + 2).coerceAtMost(points.lastIndex)]
                    val control1 = Offset(
                        current.x + (next.x - previous.x) / 6f,
                        current.y + (next.y - previous.y) / 6f
                    )
                    val control2 = Offset(
                        next.x - (following.x - current.x) / 6f,
                        next.y - (following.y - current.y) / 6f
                    )
                    cubicTo(control1.x, control1.y, control2.x, control2.y, next.x, next.y)
                }
            }
            drawPath(path, color = Color(0xFF79B900), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            val latest = points.last()
            drawLine(
                color = Color(0xFF79B900).copy(alpha = .55f),
                start = Offset(latest.x, latest.y + 7.dp.toPx()),
                end = Offset(latest.x, h),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
            )
            drawCircle(Color.White, radius = 10.dp.toPx(), center = latest)
            drawCircle(Color(0xFF79B900), radius = 7.dp.toPx(), center = latest)
        }
    }
}

@Composable
private fun StatText(value: String, label: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AppText(value, 14, FontWeight.Bold, color = valueColor)
        AppText(label, 10, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FeaturedWorkoutCard(
    exercise: WorkoutExercise,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = listOf(Color(0xFFFF1010), Color(0xFF85343C))
    Box(
        modifier.clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(colors))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            AppText(exercise.title, 18, FontWeight.Bold, color = Color.White, maxLines = 1)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText("✦ ${exercise.coins}", 12, FontWeight.SemiBold, color = Color(0xFF32E4E4))
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Filled.Schedule, null, tint = Color.White.copy(alpha = .9f), modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                AppText(if (exercise.durationMinutes > 0) "${exercise.durationMinutes} мин" else "Время не указано", 12, color = Color.White)
            }
        }
    }
}

@Composable
private fun SearchCatalogScreen(
    exercises: List<WorkoutExercise>,
    recentSearches: List<String>,
    onSearchSubmitted: (String) -> Unit,
    onOpenExercise: (String) -> Unit,
    onBack: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("По умолчанию") }
    var gridView by rememberSaveable { mutableStateOf(false) }
    var showAllRecent by rememberSaveable { mutableStateOf(false) }

    val matching = exercises.filter { exercise ->
        query.isBlank() ||
            exercise.title.contains(query, ignoreCase = true) ||
            exercise.shortDescription.contains(query, ignoreCase = true) ||
            exercise.description.contains(query, ignoreCase = true)
    }
    val results = when (selectedFilter) {
        "Время" -> matching.sortedWith(compareBy<WorkoutExercise>({ it.durationMinutes <= 0 }, { it.durationMinutes }))
        "Интенсивность" -> matching.sortedWith(compareBy<WorkoutExercise>({ it.intensity.isBlank() }, { it.intensity.lowercase() }))
        "Коины" -> matching.sortedByDescending { it.coins }
        else -> matching
    }

    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 112.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onSurface)
            }
            AppSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Бег, отжимания...",
                onSearch = { onSearchSubmitted(query) },
                modifier = Modifier.weight(1f)
            )
        }

        // View mode is available immediately on opening the search tab, even before typing.
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppText(
                if (query.isBlank()) "Тренировки" else "${results.size} результатов",
                14,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { gridView = true }, modifier = Modifier.size(38.dp)) {
                Icon(
                    Icons.Filled.GridView,
                    contentDescription = "Плиткой по две",
                    tint = if (gridView) Color(0xFF79B505) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(25.dp)
                )
            }
            IconButton(onClick = { gridView = false }, modifier = Modifier.size(38.dp)) {
                Icon(
                    Icons.Filled.ViewAgenda,
                    contentDescription = "Списком",
                    tint = if (!gridView) Color(0xFF79B505) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        if (query.isBlank()) {
            if (recentSearches.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppText("Недавний поиск", 17, FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showAllRecent = !showAllRecent }) {
                        AppText(if (showAllRecent) "Свернуть" else "Посмотреть все", 13, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(8.dp))
                (if (showAllRecent) recentSearches else recentSearches.take(3)).chunked(3).forEach { terms ->
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                        terms.forEach { term ->
                            Surface(
                                modifier = Modifier.weight(1f).clickable {
                                    query = term
                                    onSearchSubmitted(term)
                                },
                                shape = RoundedCornerShape(100.dp),
                                color = Color(0xFFE2E9D9)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    AppText(term, 12, maxLines = 1)
                                }
                            }
                        }
                        repeat(3 - terms.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(7.dp))
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Время", "Интенсивность", "Коины").forEach { filter ->
                    SmallChoiceChip(filter, selected = selectedFilter == filter) {
                        selectedFilter = if (selectedFilter == filter) "По умолчанию" else filter
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        if (results.isEmpty()) {
            EmptyCatalogPlaceholder(
                if (query.isBlank()) "Тренировки появятся, когда будут доступны в каталоге."
                else "По этому запросу ничего не найдено."
            )
        } else if (gridView) {
            results.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { exercise ->
                        GridWorkoutCard(
                            exercise = exercise,
                            modifier = Modifier.weight(1f).aspectRatio(0.82f),
                            onClick = { onOpenExercise(exercise.id) }
                        )
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }
        } else {
            results.forEach { exercise ->
                CatalogWorkoutRow(exercise) { onOpenExercise(exercise.id) }
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun CatalogWorkoutRow(exercise: WorkoutExercise, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(140.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.2.dp, Color(0xFF496900))
    ) {
        Row(Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            // The red block is a deliberate visual placeholder; exercise imagery is not fabricated.
            Box(
                Modifier.size(104.dp).clip(RoundedCornerShape(13.dp)).background(Color.Red)
            )
            Column(Modifier.weight(1f).fillMaxHeight().padding(start = 12.dp, top = 7.dp, bottom = 3.dp)) {
                AppText(exercise.title, 16, FontWeight.Bold, maxLines = 2)
                Spacer(Modifier.height(5.dp))
                AppText("✦ ${exercise.coins}", 14, FontWeight.Medium, color = Color(0xFF00BBD4))
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Timer, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    AppText(
                        if (exercise.durationMinutes > 0) "${exercise.durationMinutes} минут" else "Время не указано",
                        12,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Outlined.PlayCircleOutline, contentDescription = "Открыть тренировку", tint = Color(0xFF79B505), modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun GridWorkoutCard(exercise: WorkoutExercise, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFFF1010), Color(0xFF211522))))
            .border(1.dp, Color(0xFF496900), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = Color.White.copy(alpha = .97f),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            AppText("✦ ${exercise.coins}", 13, FontWeight.SemiBold, color = Color(0xFF00BBD4), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
        }
        Column(Modifier.align(Alignment.BottomStart).padding(start = 7.dp, bottom = 5.dp)) {
            AppText(exercise.title, 15, FontWeight.Bold, color = Color.White, maxLines = 2)
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timer, null, tint = Color(0xFFB3B6AA), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                AppText(
                    if (exercise.durationMinutes > 0) "${exercise.durationMinutes} минут" else "Время не указано",
                    11,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ActivityOverviewScreen(profile: UserProfile) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp).padding(bottom = 112.dp)) {
        AppText("Моя активность", 22, FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        AppText("Статистика из профиля пользователя", 14, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        if (profile.activityValues.isEmpty()) {
            EmptyActivityChart("Пока нет данных активности.")
        } else {
            ActivityLineChart(Modifier.fillMaxWidth().height(180.dp), profile.activityValues)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(profile.totalWorkouts.toString(), "Тренировок", Modifier.weight(1f))
            StatCard(profile.sportCoins.toString(), "СпортКоинов", Modifier.weight(1f))
            StatCard(profile.totalMinutes.toString(), "Минут", Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        AppText("Последние тренировки", 18, FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        EmptyCatalogPlaceholder("История завершённых тренировок пока отсутствует.")
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AppText(value, 16, FontWeight.Bold)
            AppText(label, 10, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ProfileOverviewScreen(
    profile: UserProfile,
    isAdmin: Boolean,
    roleLabel: String,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenPartners: () -> Unit,
    onAddPartner: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 112.dp)) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onSurface)
            }
            AppText("Профиль", 21, FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            IconButton(onClick = onLogout, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Filled.Logout, contentDescription = "Выйти", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(106.dp)) {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(Color.Red))
                    Box(
                        Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp)
                            .size(28.dp).clip(CircleShape).background(Color(0xFF00BBD4)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppText("♕", 19, FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(Modifier.height(8.dp))
                AppText(profile.displayName, 18, FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAdmin) Color(0xFFE8F2D8) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    AppText(
                        when {
                            isAdmin -> "Администратор · ADMIN"
                            roleLabel.isNotBlank() -> "Роль · $roleLabel"
                            else -> "Роль не указана"
                        },
                        11,
                        FontWeight.SemiBold,
                        color = if (isAdmin) Color(0xFF496900) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { }) {
                    AppText("Изменить фотографию", 14, FontWeight.SemiBold, color = Color(0xFF496900))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStat(Icons.Filled.DirectionsRun, profile.totalWorkouts.toString(), Color(0xFF496900))
            ProfileStat(Icons.Filled.Favorite, profile.favoriteCount.toString(), Color(0xFFEF3340))
            ProfileStat(Icons.Filled.AutoAwesome, profile.sportCoins.toString(), Color(0xFF00BBD4))
        }
        Spacer(Modifier.height(22.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(horizontal = 15.dp)) {
                ProfileMenuRow(Icons.Outlined.PersonOutline, "Мой профиль", onEditProfile)
                ProfileMenuRow(Icons.Filled.Business, "Партнёры", onOpenPartners)
                ProfileMenuRow(Icons.Outlined.FavoriteBorder, "Моё избранное") { }
                ProfileMenuRow(Icons.Filled.FitnessCenter, "Настройка тренировки") { }
                ProfileMenuRow(Icons.Filled.Settings, "Общие настройки") { }
                ProfileMenuRow(Icons.Filled.Language, "Язык") { }
                ProfileMenuRow(Icons.Filled.Link, "Интеграции") { }
            }
        }
        if (isAdmin) {
            Spacer(Modifier.height(14.dp))
            Surface(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).clickable(onClick = onOpenAdmin),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(15.dp)
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        AppText("Управление упражнениями", 15, FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        AppText("Добавление, редактирование и удаление", 11, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.height(10.dp))
            Surface(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).clickable(onClick = onAddPartner),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color(0xFF79B505)),
                shape = RoundedCornerShape(15.dp)
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, null, tint = Color(0xFF79B505))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        AppText("Добавить партнёра", 15, FontWeight.SemiBold)
                        AppText("Создать новую карточку партнёра", 11, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFF7F8F3)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(29.dp))
        }
        Spacer(Modifier.height(7.dp))
        AppText(value, 19, FontWeight.Bold)
    }
}

@Composable
private fun ProfileEditScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onRequestEmailCode: (String, (Boolean, String) -> Unit) -> Unit,
    onSave: (ProfileUpdateRequest, (Boolean, String) -> Unit) -> Unit
) {
    var newLogin by rememberSaveable(profile.login) { mutableStateOf(profile.login) }
    var newNickname by rememberSaveable(profile.nickname) { mutableStateOf(profile.nickname) }
    var newEmail by rememberSaveable(profile.email) { mutableStateOf(profile.email) }
    val originalPhoneDigits = remember(profile.phone) { normalizeInternationalPhoneDigits(profile.phone) }
    var newPhoneDigits by rememberSaveable(profile.phone) { mutableStateOf(originalPhoneDigits) }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var passwordChangeExpanded by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val originalWeight = profile.weight.toDoubleOrNull()?.toInt()?.toString() ?: profile.weight.trim()
    val originalHeight = profile.height.toDoubleOrNull()?.toInt()?.toString() ?: profile.height.trim()
    var weight by rememberSaveable(profile.weight) { mutableStateOf(originalWeight) }
    var height by rememberSaveable(profile.height) { mutableStateOf(originalHeight) }
    var emailCode by rememberSaveable { mutableStateOf("") }
    var requestedEmail by rememberSaveable { mutableStateOf("") }
    var requestInProgress by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val emailChanged = newEmail.trim() != profile.email.trim()
    val loginChanged = newLogin.trim() != profile.login.trim()
    val nicknameChanged = newNickname.trim() != profile.nickname.trim()
    val phoneChanged = newPhoneDigits != originalPhoneDigits
    val weightChanged = weight.trim() != originalWeight
    val heightChanged = height.trim() != originalHeight

    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(bottom = 112.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onSurface)
            }
            AppText("Редактируемый профиль", 18, FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            IconButton(
                enabled = !saving,
                onClick = {
                    val loginPattern = Regex("^[A-Za-z_][A-Za-z0-9_]{0,49}$")
                    val phonePattern = Regex("^\\+7 \\(\\d{3}\\) \\d{3}-\\d{2}-\\d{2}$")
                    val formattedPhoneForApi = formatPhoneForRegistrationApi(newPhoneDigits)
                    val passwordSpecial = "!\"#\$%&'()*+,-./:;<=>?@[]^{|}~"
                    val passwordValid = !passwordChangeExpanded || newPassword.isBlank() || (
                        newPassword.length >= 8 &&
                            newPassword.all { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it in passwordSpecial } &&
                            newPassword.any { it in 'A'..'Z' } &&
                            newPassword.any { it in 'a'..'z' } &&
                            newPassword.any { it.isDigit() } &&
                            newPassword.any { it in passwordSpecial }
                        )
                    val weightValue = weight.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                    val heightValue = height.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                    val validation = when {
                        loginChanged && newLogin.isNotBlank() && !loginPattern.matches(newLogin.trim()) -> "Логин должен начинаться с латинской буквы или _, содержать только A–Z, a–z, 0–9, _ и быть не длиннее 50 символов."
                        emailChanged && newEmail.isNotBlank() && !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(newEmail.trim()) -> "Введите корректный адрес электронной почты."
                        emailChanged && newEmail.isNotBlank() && requestedEmail != newEmail.trim() -> "Сначала запросите код подтверждения для новой почты."
                        emailChanged && newEmail.isNotBlank() && emailCode.isBlank() -> "Введите код из письма для подтверждения новой почты."
                        phoneChanged && newPhoneDigits.isNotBlank() && (formattedPhoneForApi == null || !phonePattern.matches(formattedPhoneForApi)) -> "Телефон должен быть в формате +7 (999) 999-99-99."
                        !passwordValid -> "Новый пароль должен содержать не менее 8 символов, заглавную и строчную буквы, цифру и специальный символ."
                        weightChanged && weight.isNotBlank() && (weightValue == null || weightValue !in 0..700) -> "Вес должен быть целым числом от 0 до 700."
                        heightChanged && height.isNotBlank() && (heightValue == null || heightValue !in 0..300) -> "Рост должен быть целым числом от 0 до 300."
                        else -> null
                    }
                    if (validation != null) {
                        feedback = validation
                    } else {
                        saving = true
                        feedback = null
                        val update = ProfileUpdateRequest(
                            newLogin = newLogin.trim().takeIf { loginChanged && it.isNotEmpty() },
                            newNickname = newNickname.trim().takeIf { nicknameChanged && it.isNotEmpty() },
                            newEmail = newEmail.trim().takeIf { emailChanged && it.isNotEmpty() },
                            emailCode = emailCode.trim().takeIf { emailChanged && it.isNotEmpty() },
                            newPhone = formattedPhoneForApi.takeIf { phoneChanged && newPhoneDigits.isNotEmpty() },
                            newPassword = newPassword.takeIf { passwordChangeExpanded && it.isNotEmpty() },
                            weight = weightValue.takeIf { weightChanged },
                            height = heightValue.takeIf { heightChanged }
                        )
                        onSave(update) { saved, message ->
                            saving = false
                            feedback = message
                            if (saved) onBack()
                        }
                    }
                },
                modifier = Modifier.size(42.dp)
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Filled.Check, contentDescription = "Сохранить", tint = Color(0xFF79B505))
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(112.dp).clip(CircleShape).background(Color.Red))
                TextButton(onClick = { }) { AppText("Изменить фотографию", 14, FontWeight.SemiBold, color = Color(0xFF496900)) }
            }
        }
        Spacer(Modifier.height(12.dp))
        ProfileEditField("Логин", newLogin, { newLogin = it })
        ProfileEditField("Никнейм", newNickname, { newNickname = it })
        ProfileEditField("Электронная почта", newEmail, { newEmail = it }, keyboardType = KeyboardType.Email)
        if (emailChanged) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    enabled = !requestInProgress && newEmail.isNotBlank(),
                    onClick = {
                        requestInProgress = true
                        feedback = null
                        onRequestEmailCode(newEmail.trim()) { success, message ->
                            requestInProgress = false
                            if (success) {
                                requestedEmail = newEmail.trim()
                                emailCode = ""
                                feedback = "Код отправлен на ${newEmail.trim()}. Он действует 15 минут."
                            } else feedback = message
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF79B505))
                ) {
                    if (requestInProgress) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Отправить код", color = Color.White)
                }
            }
            if (requestedEmail == newEmail.trim()) {
                ProfileEditField("Код из письма", emailCode, { emailCode = it.filter(Char::isDigit).take(8) }, keyboardType = KeyboardType.Number)
            }
        }
        ProfilePhoneEditField(
            value = newPhoneDigits,
            onValueChange = { newPhoneDigits = normalizeInternationalPhoneDigits(it) }
        )
        if (!passwordChangeExpanded) {
            OutlinedButton(
                onClick = { passwordChangeExpanded = true; newPassword = ""; passwordVisible = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                AppText("Сменить пароль", 14, FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
        } else {
            ProfilePasswordField(
                value = newPassword,
                onValueChange = { newPassword = it },
                visible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                onCancel = {
                    passwordChangeExpanded = false
                    passwordVisible = false
                    newPassword = ""
                }
            )
        }
        ProfileEditField("Рост", height, { height = it.filter(Char::isDigit) }, suffix = "см", keyboardType = KeyboardType.Number)
        ProfileEditField("Вес", weight, { weight = it.filter(Char::isDigit) }, suffix = "кг", keyboardType = KeyboardType.Number)
        ProfileReadOnlyField("День рождения", profile.birthDate)
        ProfileReadOnlyField("Пол", profile.gender)
        if (!feedback.isNullOrBlank()) {
            Text(feedback!!, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), color = if (feedback!!.startsWith("Код отправлен")) Color(0xFF496900) else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileReadOnlyField(label: String, value: String, suffix: String = "") {
    ProfileEditRow(label = label) {
        val displayValue = if (value.isBlank()) "Не указано" else value
        AppText(
            if (suffix.isNotBlank() && value.isNotBlank()) "$displayValue $suffix" else displayValue,
            13,
            color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 2
        )
    }
}

@Composable
private fun ProfileEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    ProfileEditRow(label = label) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = VisualTransformation.None,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End)
            )
            if (suffix.isNotBlank()) {
                AppText(" $suffix", 13, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfilePhoneEditField(value: String, onValueChange: (String) -> Unit) {
    ProfileEditRow(label = "Телефон") {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = InternationalPhoneVisualTransformation,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End
            )
        )
    }
}

@Composable
private fun ProfilePasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    onCancel: () -> Unit
) {
    ProfileEditRow(label = "Новый пароль") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = if (visible) KeyboardType.Text else KeyboardType.Password),
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End)
            )
            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Скрыть пароль" else "Показать пароль",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Отменить смену пароля", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileEditRow(label: String, valueContent: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 57.dp).padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(label, 14, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Box(Modifier.weight(1.25f), contentAlignment = Alignment.CenterEnd) { valueContent() }
        }
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        AppText(title, 15, FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f).padding(start = 13.dp))
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Divider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun MainBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onQuickStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dockSurface = MaterialTheme.colorScheme.surface
    Box(
        modifier.fillMaxWidth()
            .navigationBarsPadding()
            .height(91.dp)
            .padding(horizontal = 22.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(40.dp),
            color = dockSurface.copy(alpha = .98f),
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, Color(0xFFF0F2EB))
        ) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BottomBarItem(Icons.Outlined.Home, "Главная", selected == MainTab.HOME) { onSelect(MainTab.HOME) }
                BottomBarItem(Icons.Outlined.Search, "Поиск", selected == MainTab.SEARCH) { onSelect(MainTab.SEARCH) }
                Spacer(Modifier.width(62.dp))
                BottomBarItem(Icons.Outlined.Description, "Активность", selected == MainTab.ACTIVITY) { onSelect(MainTab.ACTIVITY) }
                BottomBarItem(Icons.Outlined.PersonOutline, "Профиль", selected == MainTab.PROFILE) { onSelect(MainTab.PROFILE) }
            }
        }
        Box(
            Modifier.align(Alignment.TopCenter)
                .size(70.dp)
                .shadow(10.dp, CircleShape)
                .border(3.dp, dockSurface, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF79B505))
                .clickable(onClick = onQuickStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.FlashOn, contentDescription = "Быстрый старт", tint = Color.White, modifier = Modifier.size(35.dp))
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) Color(0xFF79B505) else Color(0xFF687461),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun QuickStartWorkoutDialog(
    exercises: List<WorkoutExercise>,
    onDismiss: () -> Unit,
    onSelectExercise: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText("Начать тренировку", 19, FontWeight.Bold) },
        text = {
            if (exercises.isEmpty()) {
                AppText("Доступных тренировок пока нет.", 14, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                    exercises.forEach { exercise ->
                        TextButton(onClick = { onSelectExercise(exercise.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                AppText(exercise.title, 15, FontWeight.SemiBold)
                                AppText(
                                    if (exercise.durationMinutes > 0) "${exercise.durationMinutes} минут · ✦ ${exercise.coins}" else "✦ ${exercise.coins}",
                                    12,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { AppText("Закрыть", 14, color = Color(0xFF79B505)) }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun WorkoutDetailScreen(exercise: WorkoutExercise, onBack: () -> Unit, onStart: () -> Unit) {
    var activeSection by rememberSaveable(exercise.id) { mutableStateOf("Описание") }
    var isFavorite by rememberSaveable(exercise.id) { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(
                Modifier.fillMaxWidth().height(318.dp)
                    .clip(RoundedCornerShape(bottomStart = 44.dp, bottomEnd = 44.dp))
                    .background(Color(0xFF496900))
            ) {
                Box(Modifier.align(Alignment.Center).offset(y = (-10).dp).size(190.dp).clip(CircleShape).background(Color(0xFF3E6500)), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(142.dp).clip(CircleShape).background(Color(0xFF5B8500)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.DirectionsRun, null, tint = Color.White, modifier = Modifier.size(72.dp))
                    }
                }
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(Icons.Filled.ArrowBack, "Назад", onBack)
                    Spacer(Modifier.weight(1f))
                    CircleIconButton(Icons.Filled.Share, "Поделиться") { }
                    Spacer(Modifier.width(8.dp))
                    CircleIconButton(Icons.Filled.MoreVert, "Ещё") { }
                }
                Column(Modifier.align(Alignment.BottomStart).padding(start = 24.dp, end = 20.dp, bottom = 22.dp)) {
                    AppText("✦ ${exercise.coins}", 24, FontWeight.Bold, color = Color(0xFF00C4E0))
                    AppText(exercise.title, 25, FontWeight.Bold, color = Color.White, maxLines = 2)
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailMetric(
                    if (exercise.durationMinutes > 0) "${exercise.durationMinutes} мин" else "—",
                    "Время", Icons.Filled.Timer, Modifier.weight(1f)
                )
                DetailMetric(exercise.distance, "Дистанция", Icons.Filled.Place, Modifier.weight(1f))
                DetailMetric(if (exercise.calories > 0) "${exercise.calories} ккал" else "—", "Калории", Icons.Filled.LocalFireDepartment, Modifier.weight(1f))
            }

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Описание", "Инструкция", "Противопоказания").forEach { section ->
                    DetailSectionChip(section, activeSection == section) { activeSection = section }
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp)) {
                when (activeSection) {
                    "Описание" -> {
                        AppText(exercise.description, 15, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(18.dp))
                        RewardPanel(exercise.coins)
                        Spacer(Modifier.height(20.dp))
                        AppText("Инструкция", 19, FontWeight.Bold)
                        exercise.steps.lines().filter { it.isNotBlank() }.forEachIndexed { index, step ->
                            DetailStep(index + 1, step.substringBefore('|'), step.substringAfter('|', ""))
                        }
                        Spacer(Modifier.height(18.dp))
                        AppText("Советы", 19, FontWeight.Bold)
                        exercise.tips.lines().filter { it.isNotBlank() }.forEach { tip ->
                            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF496900), modifier = Modifier.size(19.dp))
                                AppText(tip, 14, modifier = Modifier.padding(start = 9.dp))
                            }
                        }
                    }
                    "Инструкция" -> {
                        exercise.steps.lines().filter { it.isNotBlank() }.forEachIndexed { index, step ->
                            DetailStep(index + 1, step.substringBefore('|'), step.substringAfter('|', ""))
                        }
                    }
                    else -> {
                        AppText("Перед началом тренировки учитывайте индивидуальные ограничения.", 14, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        exercise.contraindications.split(';').filter { it.isNotBlank() }.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color(0xFFFFE0D8),
                                modifier = Modifier.padding(end = 6.dp, bottom = 8.dp)
                            ) {
                                AppText(item.trim(), 13, color = Color(0xFF9C2E1D), modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
                            }
                        }
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                .navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(58.dp).clickable { isFavorite = !isFavorite },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Убрать из избранного" else "В избранное",
                        tint = if (isFavorite) Color(0xFFEF3340) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.weight(1f).height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF79B505), contentColor = Color(0xFF17210D))
            ) {
                AppText("Начать тренировку", 16, FontWeight.Bold, color = Color(0xFF17210D))
            }
        }
    }
}

@Composable
private fun DetailSectionChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(100.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(100.dp),
        color = if (selected) Color(0xFF496900) else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        AppText(text, 13, FontWeight.Medium, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp))
    }
}

@Composable
private fun DetailStep(number: Int, title: String, description: String) {
    Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFEEF5D9)), contentAlignment = Alignment.Center) {
            AppText(number.toString(), 14, FontWeight.Bold, color = Color(0xFF496900))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp, top = 2.dp)) {
            AppText(title, 16, FontWeight.SemiBold)
            AppText(description, 14, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailMetric(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 10.dp), horizontalAlignment = Alignment.Start) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFEAF3DA)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color(0xFF496900), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            AppText(value, 15, FontWeight.Bold)
            AppText(label, 12, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RewardPanel(coins: Int) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.FlashOn, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
            Column(Modifier.padding(start = 10.dp)) {
                AppText("Награда за тренировку", 11, color = MaterialTheme.colorScheme.onPrimaryContainer)
                AppText("$coins СпортКоинов", 17, FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun CircleIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface.copy(alpha = .9f)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AppSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(17.dp)).background(MaterialTheme.colorScheme.background)
            .border(1.2.dp, Color(0xFF496900), RoundedCornerShape(17.dp)).padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (query.isBlank()) AppText(placeholder, 14, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotBlank()) {
            IconButton(onClick = onSearch, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Filled.Search, contentDescription = "Искать", tint = MaterialTheme.colorScheme.primary)
            }
            Icon(Icons.Filled.Close, contentDescription = "Очистить", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onQueryChange("") })
        }
    }
}

@Composable
private fun SmallChoiceChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(100.dp)).clickable(onClick = onClick),
        color = if (selected) Color(0xFF496900) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(100.dp),
        border = BorderStroke(1.dp, if (selected) Color(0xFF496900) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        AppText(text, 12, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp))
    }
}

@Composable
private fun EmptyCatalogPlaceholder(message: String) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary)
            AppText(message, 12, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 10.dp))
        }
    }
}

@Composable
private fun AppText(
    text: String,
    size: Int = 14,
    weight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontWeight = weight,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign
    )
}
