package com.tisto.githubusers.compose.ui.common

import android.content.res.Resources
import com.tisto.githubusers.compose.R
import com.tisto.githubusers.domain.model.DataError
import kotlin.math.ceil

// Offline wording depends on whether the screen still has results to show.
fun Resources.errorMessage(error: DataError, hasCachedResults: Boolean): String = when (error) {
    is DataError.Network ->
        getString(if (hasCachedResults) R.string.error_offline_cached else R.string.error_offline)
    is DataError.RateLimited -> {
        val minutes = error.resetAtEpochSeconds
            ?.let { ceil((it - System.currentTimeMillis() / 1000) / 60.0).toInt() }
            ?: 0
        if (minutes > 1) {
            getQuantityString(R.plurals.error_rate_limited_minutes, minutes, minutes)
        } else {
            getString(R.string.error_rate_limited)
        }
    }
    else -> getString(R.string.error_generic)
}
