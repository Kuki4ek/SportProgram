package com.sportakus.auth.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

private val DarkForeground = Color(0xFFF2F5EE)

/** All static labels use a light foreground for the fixed dark-only app. */
@Suppress("UNUSED_PARAMETER")
@Composable
internal fun DarkText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = DarkForeground,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val fixedStyle = style.copy(
        color = DarkForeground,
        fontSize = if (fontSize == TextUnit.Unspecified) style.fontSize else fontSize,
        fontStyle = fontStyle ?: style.fontStyle,
        fontWeight = fontWeight ?: style.fontWeight,
        fontFamily = fontFamily ?: style.fontFamily,
        letterSpacing = if (letterSpacing == TextUnit.Unspecified) style.letterSpacing else letterSpacing,
        textDecoration = textDecoration ?: style.textDecoration,
        textAlign = textAlign ?: style.textAlign,
        lineHeight = if (lineHeight == TextUnit.Unspecified) style.lineHeight else lineHeight
    )
    BasicText(
        text = text,
        modifier = modifier,
        style = fixedStyle.copy(color = DarkForeground),
        onTextLayout = onTextLayout,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines
    )
}
