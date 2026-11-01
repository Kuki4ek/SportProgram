package com.sportakus.auth.ui

import com.google.i18n.phonenumbers.AsYouTypeFormatter
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

/**
 * Stores only the digits typed by the user. The plus sign and punctuation are visual
 * decorations and never become part of the editable value. E.164 allows up to 15 digits.
 */
internal fun normalizeInternationalPhoneDigits(value: String): String =
    value.filter(Char::isDigit).take(15)

/**
 * Formats a complete or partial international number for display.
 * The input must include the country calling code as digits; '+' is supplied automatically.
 * libphonenumber selects the correct formatting rules after recognizing the calling code.
 */
internal fun formatInternationalPhone(value: String): String {
    val digits = normalizeInternationalPhoneDigits(value)
    if (digits.isEmpty()) return ""

    val formatter = phoneUtil.getAsYouTypeFormatter("US")
    var result = formatter.inputDigit('+')
    digits.forEach { digit -> result = formatter.inputDigit(digit) }
    return result
}

/**
 * Returns a canonical E.164 value such as +358401234567, or null if the number cannot be
 * parsed as an international number of a possible length.
 */
internal fun formatInternationalPhoneForApi(value: String): String? {
    val digits = normalizeInternationalPhoneDigits(value)
    if (digits.isEmpty()) return null

    val parsed = parseInternationalPhone(digits) ?: return null
    if (!phoneUtil.isPossibleNumber(parsed)) return null
    return phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
}

/**
 * Formats the optional phone value for the current register API contract.
 * Russian +7 numbers use the exact requested form: +7 (999) 999-99-99.
 * Other valid international numbers retain their international display format.
 */
internal fun formatPhoneForRegistrationApi(value: String): String? {
    val e164 = formatInternationalPhoneForApi(value) ?: return null
    if (e164.startsWith("+7") && e164.length == 12) {
        val national = e164.substring(2)
        return "+7 (${national.substring(0, 3)}) ${national.substring(3, 6)}-${national.substring(6, 8)}-${national.substring(8, 10)}"
    }
    return formatInternationalPhone(value)
}

/** Full validation for registration/recovery, including the country calling code. */
internal fun isCompleteInternationalPhone(value: String): Boolean {
    val digits = normalizeInternationalPhoneDigits(value)
    if (digits.isEmpty()) return false
    val parsed = parseInternationalPhone(digits) ?: return false
    return phoneUtil.isValidNumber(parsed)
}

private fun parseInternationalPhone(digits: String) = try {
    // The number is prefixed with '+', so the default region is only a parsing fallback;
    // it does not force the number to use the US country calling code.
    phoneUtil.parse("+$digits", "US")
} catch (_: NumberParseException) {
    null
}

/**
 * Shows '+' and country-aware punctuation while keeping only digits in BasicTextField state.
 * The source/display offset map keeps cursor movement and deletion usable while formatting.
 */
internal object InternationalPhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val sourceDigitPositions = text.text.indices.filter { text.text[it].isDigit() }
        val digits = sourceDigitPositions.joinToString(separator = "") { text.text[it].toString() }.take(15)

        val formatter: AsYouTypeFormatter = phoneUtil.getAsYouTypeFormatter("US")
        var output = formatter.inputDigit('+')
        val originalToTransformedByDigitCount = IntArray(digits.length + 1)
        originalToTransformedByDigitCount[0] = output.length
        digits.forEachIndexed { index, digit ->
            output = formatter.inputDigit(digit)
            originalToTransformedByDigitCount[index + 1] = output.length
        }

        val transformedLength = output.length
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, text.text.length)
                val digitsBeforeOffset = sourceDigitPositions.count { it < safeOffset }
                    .coerceIn(0, digits.length)
                return originalToTransformedByDigitCount[digitsBeforeOffset]
                    .coerceIn(0, transformedLength)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, transformedLength)
                val digitsBeforeOffset = output.substring(0, safeOffset).count(Char::isDigit)
                return digitsBeforeOffset.coerceIn(0, text.text.length)
            }
        }

        return TransformedText(AnnotatedString(output), offsetMapping)
    }
}
