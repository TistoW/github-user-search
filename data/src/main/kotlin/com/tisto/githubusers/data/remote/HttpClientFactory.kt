package com.tisto.githubusers.data.remote

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 20L

// Chucker is a real interceptor in debug and an empty one from library-no-op in release.
fun createOkHttpClient(context: Context, githubToken: String): OkHttpClient {
    val chucker = ChuckerInterceptor.Builder(context)
        .collector(
            ChuckerCollector(
                context = context,
                showNotification = true,
                retentionPeriod = RetentionManager.Period.ONE_HOUR,
            ),
        )
        .alwaysReadResponseBody(true)
        .build()

    return OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(githubToken))
        .addInterceptor(chucker)
        .build()
}
