package com.tisto.githubusers.compose.ui.common

import android.content.res.Resources
import com.tisto.githubusers.compose.R
import com.tisto.githubusers.domain.model.DataError
import kotlin.math.ceil

// Offline wording depends on whether the screen still has results to show. GitHub has separate
// limits for search and for everything else, so the rate-limit wording depends on the call.
fun Resources.errorMessage(error: DataError, hasCachedResults: Boolean, isSearch: Boolean): String = when (error) {
    is DataError.Network ->
        getString(if (hasCachedResults) R.string.error_offline_cached else R.string.error_offline)
    is DataError.RateLimited -> {
        val minutes = error.resetAtEpochSeconds
            ?.let { ceil((it - System.currentTimeMillis() / 1000) / 60.0).toInt() }
            ?: 0
        val plural = if (isSearch) R.plurals.error_rate_limited_minutes else R.plurals.error_api_rate_limited_minutes
        when {
            minutes > 1 -> getQuantityString(plural, minutes, minutes)
            isSearch -> getString(R.string.error_rate_limited)
            else -> getString(R.string.error_api_rate_limited)
        }
    }
    else -> getString(R.string.error_generic)
}
