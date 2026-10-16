package com.tisto.githubusers.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (token.isBlank()) return chain.proceed(request)
        return chain.proceed(
            request.newBuilder().header("Authorization", "Bearer ${token.trim()}").build(),
        )
    }
}
