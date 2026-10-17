package com.tisto.githubusers.data.repository

import com.squareup.moshi.Moshi
import com.tisto.githubusers.data.local.DaoTestBase
import com.tisto.githubusers.data.remote.GitHubApi
import com.tisto.githubusers.domain.model.DataError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class UserRepositoryImplTest : DaoTestBase() {

    private val server = MockWebServer()
    private var clock = 1_000L
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()
            .create(GitHubApi::class.java)
        repository = UserRepositoryImpl(api, db) { clock }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search persists results in order and the observe flow emits them`() = runTest {
        server.enqueue(json(search(2, 1, 3)))

        assertTrue(repository.searchUsers("octo").isSuccess)

        assertEquals(listOf(2L, 1L, 3L), repository.observeSearchResults("octo").first().map { it.id })
    }

    @Test
    fun `a new search for the same query replaces the old order`() = runTest {
        server.enqueue(json(search(1, 2)))
        server.enqueue(json(search(2, 3)))

        repository.searchUsers("octo")
        repository.searchUsers("octo")

        assertEquals(listOf(2L, 3L), repository.observeSearchResults("octo").first().map { it.id })
    }

    @Test
    fun `a network failure returns Network and keeps the cached results`() = runTest {
        server.enqueue(json(search(1, 2)))
        repository.searchUsers("octo")
        server.shutdown()

        val result = repository.searchUsers("octo")

        assertTrue(result.exceptionOrNull() is DataError.Network)
        assertEquals(listOf(1L, 2L), repository.observeSearchResults("octo").first().map { it.id })
    }

    @Test
    fun `a rate limit returns RateLimited and keeps the cached results`() = runTest {
        server.enqueue(json(search(1)))
        repository.searchUsers("octo")
        server.enqueue(
            MockResponse().setResponseCode(403)
                .setHeader("X-RateLimit-Remaining", "0")
                .setHeader("X-RateLimit-Reset", "1700000000"),
        )

        val error = repository.searchUsers("octo").exceptionOrNull()

        assertEquals(1700000000L, (error as DataError.RateLimited).resetAtEpochSeconds)
        assertEquals(listOf(1L), repository.observeSearchResults("octo").first().map { it.id })
    }

    @Test
    fun `queries are trimmed and lowercased into one cache entry`() = runTest {
        server.enqueue(json(search(1)))

        repository.searchUsers("  Octo ")

        assertEquals("octo", server.takeRequest().requestUrl!!.queryParameter("q"))
        assertEquals(listOf(1L), repository.observeSearchResults("OCTO").first().map { it.id })
        assertEquals(listOf(1L), repository.observeSearchResults("octo ").first().map { it.id })
    }

    @Test
    fun `refreshUser stores the detail and keeps last_viewed_at`() = runTest {
        server.enqueue(json(search(583231)))
        repository.searchUsers("octocat")
        repository.markViewed("user583231")
        server.enqueue(json(resource("user_octocat.json")))

        clock = 5_000L
        assertTrue(repository.refreshUser("octocat").isSuccess)

        val detail = repository.observeUser("octocat").first()!!
        assertEquals("The Octocat", detail.name)
        assertEquals(20000, detail.followers)
        assertEquals(listOf(583231L), repository.observeRecentlyViewed().first().map { it.id })
        assertEquals(5_000L, db.userDao().observeUser("octocat").first()!!.detailFetchedAt)
    }

    @Test
    fun `refreshUser reports NotFound and leaves the cache alone`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val error = repository.refreshUser("ghost").exceptionOrNull()

        assertTrue(error is DataError.NotFound)
        assertNull(repository.observeUser("ghost").first())
    }

    @Test
    fun `markViewed puts a user into recently viewed, newest first`() = runTest {
        server.enqueue(json(search(1, 2)))
        repository.searchUsers("octo")
        assertTrue(repository.observeRecentlyViewed().first().isEmpty())

        clock = 10L
        repository.markViewed("user1")
        clock = 20L
        repository.markViewed("user2")

        assertEquals(listOf(2L, 1L), repository.observeRecentlyViewed().first().map { it.id })
    }

    @Test
    fun `a database failure while saving search results returns Unknown instead of throwing`() = runTest {
        server.enqueue(json(search(1)))
        db.openHelper.writableDatabase.execSQL("DROP TABLE users")

        val error = repository.searchUsers("octo").exceptionOrNull()

        assertTrue("got $error", error is DataError.Unknown)
    }

    @Test
    fun `a database failure while saving a profile returns Unknown instead of throwing`() = runTest {
        server.enqueue(json(resource("user_octocat.json")))
        db.openHelper.writableDatabase.execSQL("DROP TABLE users")

        val error = repository.refreshUser("octocat").exceptionOrNull()

        assertTrue("got $error", error is DataError.Unknown)
    }

    private fun search(vararg ids: Long): String {
        val items = ids.joinToString(",") {
            """{"login":"user$it","id":$it,"avatar_url":"https://avatars/$it","html_url":"https://github.com/user$it","type":"User"}"""
        }
        return """{"total_count":${ids.size},"incomplete_results":false,"items":[$items]}"""
    }

    private fun resource(name: String) = javaClass.classLoader!!.getResource(name)!!.readText()

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)
}
