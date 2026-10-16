package com.tisto.githubusers.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `adds a bearer header when a token is set`() {
        callWith(token = "ghp_secret")

        assertEquals("Bearer ghp_secret", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `sends no Authorization header for a blank token`() {
        callWith(token = "   ")

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `sends no Authorization header for an empty token`() {
        callWith(token = "")

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    private fun callWith(token: String) {
        server.enqueue(MockResponse().setBody("{}"))
        val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor(token)).build()
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
    }
}
