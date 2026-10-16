package com.tisto.githubusers.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchResultDaoTest : DaoTestBase() {

    private val userDao get() = db.userDao()
    private val searchDao get() = db.searchResultDao()

    private fun results(query: String, vararg ids: Long) =
        ids.mapIndexed { position, id -> SearchResultEntity(query, id, position) }

    private suspend fun ids(query: String) = searchDao.observeResults(query).first().map { it.id }

    @Test
    fun `results come back in the order they were stored`() = runTest {
        userDao.upsertSummaries(listOf(summary(1), summary(2), summary(3)))

        searchDao.replaceResults("octo", results("octo", 3, 1, 2))

        assertEquals(listOf(3L, 1L, 2L), ids("octo"))
    }

    @Test
    fun `replacing removes stale rows for that query only`() = runTest {
        userDao.upsertSummaries(listOf(summary(1), summary(2), summary(3)))
        searchDao.replaceResults("octo", results("octo", 1, 2))
        searchDao.replaceResults("kotlin", results("kotlin", 2, 3))

        searchDao.replaceResults("octo", results("octo", 3))

        assertEquals(listOf(3L), ids("octo"))
        assertEquals(listOf(2L, 3L), ids("kotlin"))
    }

    @Test
    fun `replacing with nothing clears the query`() = runTest {
        userDao.upsertSummaries(listOf(summary(1)))
        searchDao.replaceResults("octo", results("octo", 1))

        searchDao.replaceResults("octo", emptyList())

        assertEquals(emptyList<Long>(), ids("octo"))
    }

    @Test
    fun `unknown query has no results`() = runTest {
        assertEquals(emptyList<Long>(), ids("nothing"))
    }
}
