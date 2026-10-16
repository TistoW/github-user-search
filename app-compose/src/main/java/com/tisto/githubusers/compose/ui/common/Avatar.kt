package com.tisto.githubusers.compose.ui.common

// GitHub serves avatars at any size through s=, so ask for what the view actually shows.
fun String.withAvatarSize(px: Int): String = if (contains('?')) "$this&s=$px" else "$this?s=$px"
