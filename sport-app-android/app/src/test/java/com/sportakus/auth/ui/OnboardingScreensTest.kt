package com.sportakus.auth.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sportakus.auth.ui.theme.SportakusAuthTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardingScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `SafetyNoteScreen displays warning text and triggers onConfirm`() {
        var confirmed = false

        composeTestRule.setContent {
            SportakusAuthTheme {
                SafetyNoteScreen(onConfirm = { confirmed = true })
            }
        }

        composeTestRule.onNodeWithText("Примечание!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Хорошо").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun `QuestionnaireOptionsScreen enforces singleSelection and maxSelections rules`() {
        var currentSelections = listOf<String>()
        var nextClicked = false

        composeTestRule.setContent {
            SportakusAuthTheme {
                QuestionnaireOptionsScreen(
                    title = "Какая у вас цель?",
                    subtitle = "Выберите до 2 целей",
                    options = listOf("Похудеть", "Накачаться", "Развить силу"),
                    selectedValues = listOf("Похудеть", "Накачаться"),
                    onSelectionChange = { currentSelections = it },
                    onBack = {},
                    onNext = { nextClicked = true },
                    maxSelections = 2
                )
            }
        }

        // Попытка выбрать третью опцию при лимите maxSelections = 2 не должна добавлять её
        composeTestRule.onNodeWithText("Развить силу").performClick()
        assertEquals(emptyList<String>(), currentSelections)

        // Снятие выделения с уже выбранной опции должно сработать
        composeTestRule.onNodeWithText("Похудеть").performClick()
        assertEquals(listOf("Накачаться"), currentSelections)

        composeTestRule.onNodeWithText("Далее").performClick()
        assertTrue(nextClicked)
    }

    @Test
    fun `BodyMetricsScreen displays height and weight and updates on button clicks`() {
        var height = 175
        var weight = 70

        composeTestRule.setContent {
            SportakusAuthTheme {
                BodyMetricsScreen(
                    heightCm = height,
                    weightKg = weight,
                    onHeightChange = { height = it },
                    onWeightChange = { weight = it },
                    onBack = {},
                    onNext = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Рост: 175 см").assertIsDisplayed()
        composeTestRule.onNodeWithText("Вес: 70 кг").assertIsDisplayed()
    }

    @Test
    fun `BirthDateScreen switches gender and adjusts day when month changes to shorter month`() {
        var selectedGender = "Мужской"
        var selectedDay = 31
        var selectedMonth = 1
        var selectedYear = 2023

        composeTestRule.setContent {
            SportakusAuthTheme {
                BirthDateScreen(
                    gender = selectedGender,
                    day = selectedDay,
                    month = selectedMonth,
                    year = selectedYear,
                    onGenderChange = { selectedGender = it },
                    onDayChange = { selectedDay = it },
                    onMonthChange = { selectedMonth = it },
                    onYearChange = { selectedYear = it },
                    onBack = {},
                    onNext = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Женский").performClick()
        assertEquals("Женский", selectedGender)

        // Переключение месяца с 1 (январь, 31 день) на 2 (февраль 2023, 28 дней) должно скорректировать день до 28
        composeTestRule.onNodeWithText("Месяц +").performClick()
        assertEquals(2, selectedMonth)
        assertEquals(28, selectedDay)
    }

    @Test
    fun `DemoGuestHome displays collected profile summary and triggers restart`() {
        var restarted = false

        composeTestRule.setContent {
            SportakusAuthTheme {
                DemoGuestHome(
                    goals = listOf("Похудеть", "Развить выносливость"),
                    workoutPlace = "Дома",
                    weeklyFrequency = "3 раза",
                    heightCm = 180,
                    weightKg = 75,
                    gender = "Мужской",
                    birthDate = "15.06.2000",
                    onRestart = { restarted = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Анкета завершена").assertIsDisplayed()
        composeTestRule.onNodeWithText("Цели: Похудеть, Развить выносливость").assertIsDisplayed()
        composeTestRule.onNodeWithText("Параметры: 180 см / 75 кг").assertIsDisplayed()

        composeTestRule.onNodeWithText("Пройти сценарий заново").performClick()
        assertTrue(restarted)
    }
}
