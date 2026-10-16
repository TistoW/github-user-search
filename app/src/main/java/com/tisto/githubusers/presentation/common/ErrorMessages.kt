package com.tisto.githubusers.presentation.common

import android.content.Context
import com.tisto.githubusers.R
import com.tisto.githubusers.domain.model.DataError
import kotlin.math.ceil

fun Context.errorMessage(error: DataError): String = when (error) {
    is DataError.Network -> getString(R.string.error_offline)
    is DataError.RateLimited -> {
        val minutes = error.resetAtEpochSeconds
            ?.let { ceil((it - System.currentTimeMillis() / 1000) / 60.0).toInt() }
            ?: 0
        if (minutes > 1) {
            resources.getQuantityString(R.plurals.error_rate_limited_minutes, minutes, minutes)
        } else {
            getString(R.string.error_rate_limited)
        }
    }
    else -> getString(R.string.error_generic)
}
