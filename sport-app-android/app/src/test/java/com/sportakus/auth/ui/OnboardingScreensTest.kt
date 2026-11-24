package com.sportakus.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.sportakus.auth.BuildConfig
import com.sportakus.auth.ui.theme.SportakusTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * UI-тесты онбординг-экранов.
 *
 * Выбор в опросниках кодируется строкой «опция1|опция2»
 * (см. QuestionnaireOptionsScreen.selectedEncoded), поэтому тесты
 * проверяют именно строковое кодирование в onSelectionChange.
 *
 * Требуемые зависимости — см. FOR_LEAD_DEVELOPER.md.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OnboardingScreensTest {

    /**
     * createComposeRule() запускает ComponentActivity из statement правила —
     * ещё ДО @Before, — поэтому проверку варианта нельзя делать в @Before:
     * в release Activity не зарегистрирована в манифесте (ui-test-manifest
     * подключён как debugImplementation), и каждый тест упадёт при старте.
     * В release классу подставляется правило-пропуск вместо Compose-правила.
     */
    @get:Rule
    val composeRuleHolder: TestRule =
        if (BuildConfig.DEBUG) createComposeRule() else DebugVariantSkipRule()

    /** Тесты работают с этим свойством; в release они не выполняются. */
    val composeRule: ComposeContentTestRule
        get() = composeRuleHolder as ComposeContentTestRule

    // ------------------------------------------------------------------
    // SafetyNoteScreen
    // ------------------------------------------------------------------

    @Test
    fun safetyNoteScreenShowsMemoAndAcknowledges() {
        var acknowledged = false

        composeRule.setContent {
            SportakusTheme {
                SafetyNoteScreen(
                    darkTheme = false,
                    onAcknowledge = { acknowledged = true }
                )
            }
        }

        composeRule.onNodeWithText("Памятка").assertExists()
        composeRule.onNodeWithText("Запомнил").performClick()
        composeRule.runOnIdle { assertTrue(acknowledged) }
    }

    // ------------------------------------------------------------------
    // IntroScreen
    // ------------------------------------------------------------------

    @Test
    fun introScreenButtonsTriggerCallbacks() {
        var started = false
        var skipped = false

        composeRule.setContent {
            SportakusTheme {
                IntroScreen(
                    onStart = { started = true },
                    onSkip = { skipped = true }
                )
            }
        }

        composeRule.onNodeWithText("Мы помогаем отслеживать физическую активность и выполнять игровые задания.").assertExists()
        composeRule.onNodeWithText("Пропустить").performClick()
        composeRule.onNodeWithText("Начнем").performClick()
        composeRule.runOnIdle {
            assertTrue("Кнопка «Пропустить» должна вызывать onSkip", skipped)
            assertTrue("Кнопка «Начнем» должна вызывать onStart", started)
        }
    }

    // ------------------------------------------------------------------
    // QuestionnaireOptionsScreen
    // ------------------------------------------------------------------

    @Test
    fun questionnaireMultiSelectionRespectsMaxSelections() {
        var selected by mutableStateOf("Похудеть")
        var nextClicked = false

        composeRule.setContent {
            SportakusTheme {
                QuestionnaireOptionsScreen(
                    question = "Что для тебя спорт?",
                    options = listOf("Похудеть", "Накачаться", "Быть в форме"),
                    selectedEncoded = selected,
                    onSelectionChange = { selected = it },
                    progress = 1,
                    onBack = {},
                    onNext = { nextClicked = true },
                    multiple = true,
                    maxSelections = 2
                )
            }
        }

        composeRule.onNodeWithText("Что для тебя спорт?").assertExists()
        composeRule.onNodeWithText("Давай узнаем друг друга получше").assertExists()

        // Вторая опция добавляется к выбранной через «|».
        composeRule.onNodeWithText("Накачаться").performClick()
        composeRule.runOnIdle { assertEquals("Похудеть|Накачаться", selected) }

        // Лимит достигнут (maxSelections = 2): третий выбор игнорируется.
        composeRule.onNodeWithText("Быть в форме").performClick()
        composeRule.runOnIdle { assertEquals("Похудеть|Накачаться", selected) }

        // Повторный клик по выбранной опции снимает её.
        composeRule.onNodeWithText("Накачаться").performClick()
        composeRule.runOnIdle { assertEquals("Похудеть", selected) }

        composeRule.onNodeWithText("Следующее").performClick()
        composeRule.runOnIdle { assertTrue(nextClicked) }
    }

    @Test
    fun questionnaireSingleSelectionReplacesValue() {
        var selected by mutableStateOf("")

        composeRule.setContent {
            SportakusTheme {
                QuestionnaireOptionsScreen(
                    question = "Что для тебя спорт?",
                    options = listOf("Похудеть", "Накачаться"),
                    selectedEncoded = selected,
                    onSelectionChange = { selected = it },
                    progress = 1,
                    onBack = {},
                    onNext = {},
                    multiple = false
                )
            }
        }

        composeRule.onNodeWithText("Похудеть").performClick()
        composeRule.runOnIdle { assertEquals("Похудеть", selected) }

        // Одиночный выбор заменяет прежнее значение, а не добавляет.
        composeRule.onNodeWithText("Накачаться").performClick()
        composeRule.runOnIdle { assertEquals("Накачаться", selected) }
    }

    // ------------------------------------------------------------------
    // BodyMetricsScreen
    // ------------------------------------------------------------------

    @Test
    fun bodyMetricsScreenShowsCurrentValuesAndForwardsNext() {
        var nextClicked = false

        composeRule.setContent {
            SportakusTheme {
                BodyMetricsScreen(
                    height = "175",
                    onHeightChange = {},
                    weight = "70",
                    onWeightChange = {},
                    onBack = {},
                    onNext = { nextClicked = true }
                )
            }
        }

        composeRule.onNodeWithText("Расскажите о себе").assertExists()
        composeRule.onNodeWithText("Рост").assertExists()
        composeRule.onNodeWithText("175 см").assertExists()
        composeRule.onNodeWithText("Вес").assertExists()
        composeRule.onNodeWithText("70 кг").assertExists()
        composeRule.onNodeWithText("Выберите поле, затем прокрутите шкалу и нажмите «ОК».").assertExists()

        composeRule.onNodeWithText("Следующее").performClick()
        composeRule.runOnIdle { assertTrue(nextClicked) }
    }

    @Test
    fun bodyMetricsScreenShowsPlaceholdersWhenBlank() {
        composeRule.setContent {
            SportakusTheme {
                BodyMetricsScreen(
                    height = "",
                    onHeightChange = {},
                    weight = "",
                    onWeightChange = {},
                    onBack = {},
                    onNext = {}
                )
            }
        }

        composeRule.onNodeWithText("Выберите рост").assertExists()
        composeRule.onNodeWithText("Выберите вес").assertExists()
    }

    // ------------------------------------------------------------------
    // BirthDateScreen
    // ------------------------------------------------------------------

    @Test
    fun birthDateScreenGenderSelectionFlow() {
        var gender by mutableStateOf("")
        var nextClicked = false

        composeRule.setContent {
            SportakusTheme {
                BirthDateScreen(
                    day = "15",
                    month = "Март",
                    year = "2000",
                    gender = gender,
                    onGenderChange = { gender = it },
                    onDateChange = { _, _, _ -> },
                    onBack = {},
                    onNext = { nextClicked = true }
                )
            }
        }

        composeRule.onNodeWithText("День рождения").assertExists()
        composeRule.onNodeWithText("15 Март 2000").assertExists()
        composeRule.onNodeWithText("Выберите пол").assertExists()

        // Раскрываем список пола и выбираем «Женщина».
        composeRule.onNodeWithText("Выберите пол").performClick()
        composeRule.onNodeWithText("Мужчина").assertExists()
        composeRule.onNodeWithText("Женщина").assertExists()
        composeRule.onNodeWithText("Не указывать").assertExists()

        composeRule.onNodeWithText("Женщина").performClick()
        composeRule.runOnIdle { assertEquals("Женщина", gender) }

        // После выбора список сворачивается, значение показывается в поле.
        composeRule.onNodeWithText("Женщина").assertExists()
        composeRule.onNodeWithText("Мужчина").assertDoesNotExist()

        composeRule.onNodeWithText("Следующее").performClick()
        composeRule.runOnIdle { assertTrue(nextClicked) }
    }

    @Test
    fun birthDateScreenOpensDateWheel() {
        composeRule.setContent {
            SportakusTheme {
                BirthDateScreen(
                    day = "15",
                    month = "Март",
                    year = "2000",
                    gender = "",
                    onGenderChange = {},
                    onDateChange = { _, _, _ -> },
                    onBack = {},
                    onNext = {}
                )
            }
        }

        composeRule.onNodeWithText("День рождения").performClick()
        composeRule.onNodeWithText("Прокрутите колонки и остановите нужные значения по центру.").assertExists()
        composeRule.onNodeWithText("День").assertExists()
        composeRule.onNodeWithText("Месяц").assertExists()
        composeRule.onNodeWithText("Год").assertExists()
        composeRule.onNodeWithText("ОК").assertExists()
    }

    // ------------------------------------------------------------------
    // DemoGuestHome
    // ------------------------------------------------------------------

    @Test
    fun demoGuestHomeShowsTasksAndReturnsToAuth() {
        var backToAuth = false

        composeRule.setContent {
            SportakusTheme {
                DemoGuestHome(onBackToAuth = { backToAuth = true })
            }
        }

        composeRule.onNodeWithText("Твой спорт — твоя игра.").assertExists()
        composeRule.onNodeWithText("Задания на сегодня").assertExists()
        composeRule.onNodeWithText("Бег").assertExists()
        composeRule.onNodeWithText("Планка").assertExists()
        composeRule.onNodeWithText("Прогулка").assertExists()

        // Клик по семантическому действию: кнопка ниже вьюпорта Robolectric-окна
        // не получает синтезированное касание из performClick().
        composeRule.onNodeWithText("Вернуться к авторизации").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.runOnIdle { assertTrue(backToAuth) }
    }
}
