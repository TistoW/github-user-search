package com.tisto.githubusers.compose.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class FormattingTest {

    @Test
    fun `counts under a thousand stay as they are`() {
        assertEquals("0", compactCount(0))
        assertEquals("999", compactCount(999))
    }

    @Test
    fun `thousands get one decimal until ten thousand`() {
        assertEquals("1k", compactCount(1_000))
        assertEquals("1.2k", compactCount(1_234))
        assertEquals("9.9k", compactCount(9_999))
        assertEquals("20k", compactCount(20_000))
        assertEquals("999k", compactCount(999_999))
    }

    @Test
    fun `millions use the M suffix`() {
        assertEquals("1M", compactCount(1_000_000))
        assertEquals("1.5M", compactCount(1_500_000))
    }

    @Test
    fun `the joined date is month and year in UTC`() {
        assertEquals("Jan 2011", formatJoinedDate("2011-01-25T18:44:36Z", Locale.US))
        // 23:30 UTC on the 31st must not roll into the next month in a positive-offset timezone.
        assertEquals("Mar 2011", formatJoinedDate("2011-03-31T23:30:00Z", Locale.US))
    }

    @Test
    fun `a missing or malformed date gives null`() {
        assertNull(formatJoinedDate(null))
        assertNull(formatJoinedDate("not a date"))
    }

    @Test
    fun `web urls get a scheme only when they lack one`() {
        assertEquals("https://github.blog", "github.blog".toWebUrl())
        assertEquals("http://example.com", "http://example.com".toWebUrl())
        assertEquals("https://example.com", " https://example.com ".toWebUrl())
    }

    @Test
    fun `avatar size is appended to the right query separator`() {
        assertEquals("https://a/u/1?v=4&s=88", "https://a/u/1?v=4".withAvatarSize(88))
        assertEquals("https://a/u/1?s=88", "https://a/u/1".withAvatarSize(88))
    }
}
