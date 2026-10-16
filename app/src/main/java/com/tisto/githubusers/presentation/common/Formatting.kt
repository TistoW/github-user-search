package com.tisto.githubusers.presentation.common

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// 950 -> "950", 1234 -> "1.2k", 20000 -> "20k", 1500000 -> "1.5M". Truncates, so 999_999 never shows as "1000k".
fun compactCount(value: Int): String = when {
    value < 1_000 -> value.toString()
    value < 1_000_000 -> scaled(value, 1_000, "k")
    else -> scaled(value, 1_000_000, "M")
}

private fun scaled(value: Int, unit: Int, suffix: String): String {
    val tenths = value / (unit / 10)
    val whole = tenths / 10
    // Above 10 the decimal adds noise.
    return if (tenths % 10 == 0 || whole >= 10) "$whole$suffix" else "$whole.${tenths % 10}$suffix"
}

// "2011-01-25T18:44:36Z" -> "Jan 2011". java.time would need desugaring below API 26.
fun formatJoinedDate(iso: String?, locale: Locale = Locale.getDefault()): String? {
    if (iso == null) return null
    val utc = TimeZone.getTimeZone("UTC")
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = utc }
    val date = runCatching { parser.parse(iso) }.getOrNull() ?: return null
    return SimpleDateFormat("MMM yyyy", locale).apply { timeZone = utc }.format(date)
}

// GitHub blog fields are free text, often without a scheme.
fun String.toWebUrl(): String = trim().let { if (it.startsWith("http://") || it.startsWith("https://")) it else "https://$it" }

// GitHub serves avatars at any size through s=, so ask for what the view actually shows.
fun String.withAvatarSize(px: Int): String = if (contains('?')) "$this&s=$px" else "$this?s=$px"
