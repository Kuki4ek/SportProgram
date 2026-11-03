package com.sportakus.auth.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.google.i18n.phonenumbers.AsYouTypeFormatter
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

/** Stores digits only. A leading Russian trunk prefix 8 is normalized to country code 7. */
internal fun normalizeInternationalPhoneDigits(value: String): String {
    val digits = value.filter(Char::isDigit).take(15)
    return if (digits.startsWith("8") && digits.length <= 11) "7${digits.drop(1)}" else digits
}

private fun canonicalDigits(value: String): String {
    val digits = normalizeInternationalPhoneDigits(value)
    return when {
        digits.length == 10 && digits.startsWith("9") -> "7$digits"
        else -> digits
    }
}

/** Formats a complete or partial international number. Russian numbers get the exact +7 mask. */
internal fun formatInternationalPhone(value: String): String {
    val digits = normalizeInternationalPhoneDigits(value)
    if (digits.isEmpty()) return ""
    if (isRussianDigits(digits)) return formatRussianPhone(digits).first

    val formatter = phoneUtil.getAsYouTypeFormatter("US")
    var result = formatter.inputDigit('+')
    digits.forEach { digit -> result = formatter.inputDigit(digit) }
    return result
}

/** Returns a canonical E.164 value such as +79999999999. */
internal fun formatInternationalPhoneForApi(value: String): String? {
    val digits = canonicalDigits(value)
    if (digits.isEmpty()) return null
    val parsed = parseInternationalPhone(digits) ?: return null
    if (!phoneUtil.isPossibleNumber(parsed)) return null
    return phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
}

/** Returns the exact backend representation for a Russian +7 number. */
internal fun formatPhoneForRegistrationApi(value: String): String? {
    val e164 = formatInternationalPhoneForApi(value) ?: return null
    if (e164.startsWith("+7") && e164.length == 12) {
        val national = e164.substring(2)
        return "+7 (${national.substring(0, 3)}) ${national.substring(3, 6)}-${national.substring(6, 8)}-${national.substring(8, 10)}"
    }
    return e164
}

internal fun isCompleteInternationalPhone(value: String): Boolean {
    val digits = canonicalDigits(value)
    if (digits.isEmpty()) return false
    val parsed = parseInternationalPhone(digits) ?: return false
    return phoneUtil.isValidNumber(parsed)
}

private fun parseInternationalPhone(digits: String) = try {
    phoneUtil.parse("+$digits", "US")
} catch (_: NumberParseException) {
    null
}

private fun isRussianDigits(digits: String): Boolean =
    (digits.startsWith("7") && digits.length <= 11) ||
        (digits.startsWith("9") && digits.length <= 10)

/**
 * Returns the formatted Russian value and an offset map for the original digit-only input.
 * The user may type either 10 national digits or the leading 7 plus 10 digits.
 */
private fun formatRussianPhone(digits: String): Pair<String, IntArray> {
    val hasCountryCode = digits.startsWith("7")
    val national = if (hasCountryCode) digits.drop(1) else digits
    val positions = IntArray(digits.length + 1)
    val output = StringBuilder("+7")

    if (hasCountryCode) {
        positions[0] = 0
        positions[1] = output.length
    } else {
        positions[0] = output.length + if (national.isNotEmpty()) 2 else 0
    }

    national.forEachIndexed { index, char ->
        when (index) {
            0 -> output.append(" (")
            3 -> output.append(") ")
            6, 8 -> output.append('-')
        }
        output.append(char)
        val sourceDigitsEntered = if (hasCountryCode) index + 2 else index + 1
        positions[sourceDigitsEntered] = output.length
    }
    return output.toString() to positions
}

/** Shows punctuation while keeping only digits in BasicTextField state. */
internal object InternationalPhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val sourceDigitPositions = text.text.indices.filter { text.text[it].isDigit() }
        val digits = sourceDigitPositions.joinToString(separator = "") { text.text[it].toString() }.take(15)

        if (digits.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)

        if (isRussianDigits(digits)) {
            val (formatted, sourceOffsets) = formatRussianPhone(digits)
            val mapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val safeOffset = offset.coerceIn(0, text.text.length)
                    val entered = sourceDigitPositions.count { it < safeOffset }.coerceIn(0, digits.length)
                    return sourceOffsets[entered].coerceIn(0, formatted.length)
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val safeOffset = offset.coerceIn(0, formatted.length)
                    val formattedDigitsBefore = formatted.substring(0, safeOffset).count(Char::isDigit)
                    val sourceDigitsBefore = if (digits.startsWith("7")) {
                        formattedDigitsBefore.coerceIn(0, digits.length)
                    } else {
                        (formattedDigitsBefore - 1).coerceIn(0, digits.length)
                    }
                    return if (sourceDigitsBefore == 0) 0
                    else sourceDigitPositions.getOrNull(sourceDigitsBefore - 1)?.plus(1) ?: text.text.length
                }
            }
            return TransformedText(AnnotatedString(formatted), mapping)
        }

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
                val digitsBeforeOffset = sourceDigitPositions.count { it < safeOffset }.coerceIn(0, digits.length)
                return originalToTransformedByDigitCount[digitsBeforeOffset].coerceIn(0, transformedLength)
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
