package com.sportakus.auth.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthValidationTest {

    // --- normalizeBaseUrl ---

    @Test
    fun `normalizeBaseUrl trims whitespace and trailing slashes for valid http and https urls`() {
        assertEquals("http://10.0.2.2:5062", normalizeBaseUrl("  http://10.0.2.2:5062///  "))
        assertEquals("https://api.sportakus.ru", normalizeBaseUrl("https://api.sportakus.ru/"))
    }

    @Test
    fun `normalizeBaseUrl rejects non-http protocols or missing scheme`() {
        assertNull(normalizeBaseUrl("ftp://10.0.2.2:5062"))
        assertNull(normalizeBaseUrl("10.0.2.2:5062"))
        assertNull(normalizeBaseUrl(""))
    }

    @Test
    fun `normalizeBaseUrl rejects urls with missing host or extra path segments`() {
        assertNull(normalizeBaseUrl("http://"))
        assertNull(normalizeBaseUrl("http://10.0.2.2:5062/api/v1"))
    }

    // --- registrationUsernameError ---

    @Test
    fun `registrationUsernameError returns error for empty or blank username and null for valid`() {
        assertEquals("Имя пользователя обязательно", registrationUsernameError(""))
        assertEquals("Имя пользователя обязательно", registrationUsernameError("   "))
        assertNull(registrationUsernameError("sport_athlete"))
    }

    // --- registrationEmailError ---

    @Test
    fun `registrationEmailError validates blank and malformed emails`() {
        assertEquals("Почта обязательна", registrationEmailError(""))
        assertEquals("Почта обязательна", registrationEmailError("   "))
        assertEquals("Почта должна быть вида example@example.ru", registrationEmailError("plainaddress"))
        assertEquals("Почта должна быть вида example@example.ru", registrationEmailError("user@.ru"))
        assertEquals("Почта должна быть вида example@example.ru", registrationEmailError("user@domain."))
        assertEquals("Почта должна быть вида example@example.ru", registrationEmailError("domain.user@com"))
    }

    @Test
    fun `registrationEmailError returns null for valid email format`() {
        assertNull(registrationEmailError("athlete@sportakus.ru"))
        assertNull(registrationEmailError("first.last@sub.domain.com"))
    }

    // --- registrationPhoneError ---

    @Test
    fun `registrationPhoneError allows empty phone and valid formatted phone`() {
        assertNull(registrationPhoneError(""))
        assertNull(registrationPhoneError("+7 (999) 123-45-67"))
        assertNull(registrationPhoneError("+375 (29) 123-45-67".replace("(29)", "(029)")))
    }

    @Test
    fun `registrationPhoneError rejects unformatted or incomplete phone numbers`() {
        val expectedError = "Телефон должен быть вида +X (XXX) XXX-XX-XX"
        assertEquals(expectedError, registrationPhoneError("89991234567"))
        assertEquals(expectedError, registrationPhoneError("+7 999 123 45 67"))
        assertEquals(expectedError, registrationPhoneError("+7 (999) 123-45"))
    }
}
