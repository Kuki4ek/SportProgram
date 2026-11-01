package com.sportakus.auth.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingHelpersTest {

    // --- daysInMonth ---

    @Test
    fun `daysInMonth returns 31 for standard 31-day months`() {
        val monthsWith31Days = listOf(1, 3, 5, 7, 8, 10, 12)
        for (month in monthsWith31Days) {
            assertEquals("Month $month should have 31 days", 31, daysInMonth(2025, month))
        }
    }

    @Test
    fun `daysInMonth returns 30 for 30-day months`() {
        val monthsWith30Days = listOf(4, 6, 9, 11)
        for (month in monthsWith30Days) {
            assertEquals("Month $month should have 30 days", 30, daysInMonth(2025, month))
        }
    }

    @Test
    fun `daysInMonth handles February in leap and non-leap years accurately`() {
        // Обычный невисокосный год
        assertEquals(28, daysInMonth(2023, 2))
        // Делится на 4, не делится на 100 -> високосный
        assertEquals(29, daysInMonth(2024, 2))
        // Делится на 100, но не на 400 -> невисокосный
        assertEquals(28, daysInMonth(1900, 2))
        // Делится на 400 -> високосный
        assertEquals(29, daysInMonth(2000, 2))
    }

    // --- emojiForOption ---

    @Test
    fun `emojiForOption returns specific emojis for known onboarding options`() {
        assertEquals("📉", emojiForOption("Похудеть"))
        assertEquals("💪", emojiForOption("Накачаться"))
        assertEquals("🏢", emojiForOption("В тренажёрном зале"))
        assertEquals("🚫", emojiForOption("Без инвентаря"))
        assertEquals("🦴", emojiForOption("Болезни суставов (артрит, артроз)"))
    }

    @Test
    fun `emojiForOption returns default check emoji for unknown option`() {
        assertEquals("✅", emojiForOption("Индивидуальная цель"))
    }
}
