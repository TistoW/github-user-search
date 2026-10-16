package com.tisto.githubusers.compose.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private fun style(size: Int, lineHeight: Int, medium: Boolean) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = if (medium) FontWeight.Medium else FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.em,
)

// Mirrors the TextAppearance.App.* styles in app/src/main/res/values/type.xml.
val AppTypography = Typography(
    headlineSmall = style(size = 24, lineHeight = 32, medium = true),
    titleLarge = style(size = 22, lineHeight = 28, medium = true),
    titleMedium = style(size = 16, lineHeight = 24, medium = true),
    bodyLarge = style(size = 16, lineHeight = 24, medium = false),
    bodyMedium = style(size = 14, lineHeight = 20, medium = false),
    labelLarge = style(size = 14, lineHeight = 20, medium = true),
    labelMedium = style(size = 12, lineHeight = 16, medium = false),
)

// TextAppearance.App.Stat has no Material 3 slot of its own.
val StatTextStyle: TextStyle = style(size = 20, lineHeight = 28, medium = true)
