package com.tisto.githubusers.data.remote

import com.squareup.moshi.Moshi
import com.tisto.githubusers.domain.model.DataError
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class GitHubApiTest {

    private val server = MockWebServer()
    private lateinit var api: GitHubApi

    @Before
    fun setUp() {
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()
            .create(GitHubApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `parses a search response and ignores unknown fields`() = runTest {
        server.enqueue(jsonResponse("search_users_octocat.json"))

        val response = api.searchUsers("octocat")

        assertEquals(2, response.totalCount)
        assertEquals(false, response.incompleteResults)
        assertEquals("octocat", response.items[0].login)
        assertEquals(583231L, response.items[0].id)
        assertEquals("https://avatars.githubusercontent.com/u/583231?v=4", response.items[0].avatarUrl)
        assertEquals("Organization", response.items[1].type)
    }

    @Test
    fun `parses a user detail and keeps null fields null`() = runTest {
        server.enqueue(jsonResponse("user_octocat.json"))

        val user = api.getUser("octocat")

        assertEquals("The Octocat", user.name)
        assertEquals("San Francisco", user.location)
        assertEquals(20000, user.followers)
        assertEquals(8, user.publicRepos)
        assertEquals("2011-01-25T18:44:36Z", user.createdAt)
        assertNull(user.bio)
        assertNull(user.email)
        assertNull(user.twitterUsername)
    }

    @Test
    fun `search sends query, paging and GitHub headers`() = runTest {
        server.enqueue(jsonResponse("search_users_octocat.json"))

        api.searchUsers("octocat")

        val request = server.takeRequest()
        assertEquals("/search/users?q=octocat&per_page=30&page=1", request.path)
        assertEquals("application/vnd.github+json", request.getHeader("Accept"))
        assertEquals("2022-11-28", request.getHeader("X-GitHub-Api-Version"))
    }

    @Test
    fun `user detail requests the login path`() = runTest {
        server.enqueue(jsonResponse("user_octocat.json"))

        api.getUser("octocat")

        assertEquals("/users/octocat", server.takeRequest().path)
    }

    @Test
    fun `403 with an exhausted quota maps to RateLimited with the reset time`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(403)
                .setHeader("X-RateLimit-Remaining", "0")
                .setHeader("X-RateLimit-Reset", "1700000000")
                .setBody("{}"),
        )

        val error = apiCall { api.searchUsers("octocat") }.exceptionOrNull()

        assertTrue(error is DataError.RateLimited)
        assertEquals(1_700_000_000L, (error as DataError.RateLimited).resetAtEpochSeconds)
    }

    @Test
    fun `403 with quota left is not a rate limit`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(403).setHeader("X-RateLimit-Remaining", "12").setBody("{}"),
        )

        assertTrue(apiCall { api.getUser("octocat") }.exceptionOrNull() is DataError.Unknown)
    }

    @Test
    fun `429 maps to RateLimited`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429).setBody("{}"))

        val error = apiCall { api.searchUsers("octocat") }.exceptionOrNull()

        assertTrue(error is DataError.RateLimited)
        assertNull((error as DataError.RateLimited).resetAtEpochSeconds)
    }

    @Test
    fun `404 maps to NotFound`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("{}"))

        assertTrue(apiCall { api.getUser("ghost") }.exceptionOrNull() is DataError.NotFound)
    }

    @Test
    fun `500 maps to Unknown`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        assertTrue(apiCall { api.getUser("octocat") }.exceptionOrNull() is DataError.Unknown)
    }

    @Test
    fun `dropped connection maps to Network`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertTrue(apiCall { api.getUser("octocat") }.exceptionOrNull() is DataError.Network)
    }

    private fun jsonResponse(resource: String): MockResponse {
        val body = javaClass.classLoader!!.getResource(resource)!!.readText()
        return MockResponse().setHeader("Content-Type", "application/json").setBody(body)
    }
}
