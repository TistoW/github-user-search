package com.tisto.githubusers.compose.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Mirrors app/src/main/res/values/dimens.xml.
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object Dimens {
    val minTouchTarget = 48.dp
    val iconSize = 24.dp
    val iconSizeLarge = 48.dp
    val avatarList = 44.dp
    val avatarDetail = 96.dp
    val dividerHeight = 1.dp
    val dividerInset = 72.dp
    val detailGutter = 16.dp
}

private val CornerRadius = 8.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(CornerRadius),
    small = RoundedCornerShape(CornerRadius),
    medium = RoundedCornerShape(CornerRadius),
    large = RoundedCornerShape(CornerRadius),
    extraLarge = RoundedCornerShape(CornerRadius),
)
