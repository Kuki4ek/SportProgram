package com.sportakus.auth.ui

import com.sportakus.auth.ui.theme.sportakusTextColor

import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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

/** Theme-aware text primitive used by the app's screens. */
@Composable
internal fun DarkText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
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
    val foreground = color ?: sportakusTextColor()
    val fixedStyle = style.copy(
        color = foreground,
        fontSize = if (fontSize == TextUnit.Unspecified) style.fontSize else fontSize,
        fontStyle = fontStyle ?: style.fontStyle,
        fontWeight = fontWeight ?: style.fontWeight,
        fontFamily = fontFamily ?: style.fontFamily,
        letterSpacing = if (letterSpacing == TextUnit.Unspecified) style.letterSpacing else letterSpacing,
        textDecoration = textDecoration ?: style.textDecoration,
        textAlign = textAlign ?: style.textAlign,
        lineHeight = if (lineHeight == TextUnit.Unspecified) style.lineHeight else lineHeight
    )

    // BasicText can merge LocalTextStyle with the supplied style. Provide the
    // same explicit foreground locally as well, so a parent Surface, selected
    // tile, dropdown, or Button cannot reintroduce a stale white content color.
    CompositionLocalProvider(
        LocalContentColor provides foreground,
        LocalTextStyle provides fixedStyle
    ) {
        BasicText(
            text = text,
            modifier = modifier,
            style = fixedStyle,
            onTextLayout = onTextLayout,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines
        )
    }
}
