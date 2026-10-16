package com.tisto.githubusers.domain.usecase

import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveSearchResultsTest {
    private val repository = mockk<UserRepository>()
    private val observeSearchResults = ObserveSearchResults(repository)

    @Test
    fun `observes the cache with the trimmed query`() = runTest {
        val users = listOf(User(1, "octocat", "https://avatar", "User"))
        every { repository.observeSearchResults("octocat") } returns flowOf(users)

        assertEquals(users, observeSearchResults(" octocat ").first())
    }

    @Test
    fun `blank query emits an empty list and skips the repository`() = runTest {
        assertEquals(emptyList<User>(), observeSearchResults("  ").first())
        verify(exactly = 0) { repository.observeSearchResults(any()) }
    }
}
