package com.tisto.githubusers.data.local

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserDaoTest : DaoTestBase() {

    private val userDao get() = db.userDao()
    private val searchDao get() = db.searchResultDao()

    private fun detail(id: Long) = summary(id).copy(
        name = "The Octocat",
        bio = "Mascot",
        followers = 20000,
        detailFetchedAt = 1_000L,
    )

    @Test
    fun `summary upsert keeps the cached detail and refreshes the summary columns`() = runTest {
        userDao.upsertDetail(detail(1))

        userDao.upsertSummaries(listOf(summary(1).copy(avatarUrl = "https://avatars/new")))

        val user = userDao.observeUser("user1").first()!!
        assertEquals("https://avatars/new", user.avatarUrl)
        assertEquals("Mascot", user.bio)
        assertEquals(20000, user.followers)
        assertEquals(1_000L, user.detailFetchedAt)
    }

    @Test
    fun `summary upsert inserts users it has not seen`() = runTest {
        userDao.upsertSummaries(listOf(summary(1), summary(2)))

        assertEquals(1L, userDao.observeUser("user1").first()?.id)
        assertNull(userDao.observeUser("user1").first()?.detailFetchedAt)
        assertEquals(2L, userDao.observeUser("user2").first()?.id)
    }

    @Test
    fun `detail upsert keeps last viewed time and search results`() = runTest {
        userDao.upsertSummaries(listOf(summary(1)))
        searchDao.replaceResults("octo", listOf(SearchResultEntity("octo", 1, 0)))
        userDao.markViewed("user1", 500L)

        userDao.upsertDetail(detail(1))

        val user = userDao.observeUser("user1").first()!!
        assertEquals(500L, user.lastViewedAt)
        assertEquals("Mascot", user.bio)
        assertEquals(listOf(1L), searchDao.observeResults("octo").first().map { it.id })
    }

    @Test
    fun `login lookup ignores case`() = runTest {
        userDao.upsertSummaries(listOf(summary(1, login = "OctoCat")))

        assertEquals(1L, userDao.observeUser("octocat").first()?.id)
    }

    @Test
    fun `recently viewed is newest first, limited, and skips unviewed users`() = runTest {
        userDao.upsertSummaries(listOf(summary(1), summary(2), summary(3), summary(4)))
        userDao.markViewed("user1", 100L)
        userDao.markViewed("user3", 300L)
        userDao.markViewed("user2", 200L)

        assertEquals(listOf(3L, 2L, 1L), userDao.observeRecentlyViewed(10).first().map { it.id })
        assertEquals(listOf(3L, 2L), userDao.observeRecentlyViewed(2).first().map { it.id })
    }

    @Test
    fun `observeUser emits again when the row changes`() = runTest {
        userDao.upsertSummaries(listOf(summary(1)))

        userDao.observeUser("user1").test {
            assertNull(awaitItem()?.bio)
            userDao.upsertDetail(detail(1))
            assertEquals("Mascot", awaitItem()?.bio)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
