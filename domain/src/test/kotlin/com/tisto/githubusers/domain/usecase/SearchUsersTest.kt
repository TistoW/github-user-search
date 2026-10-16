package com.tisto.githubusers.domain.usecase

import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchUsersTest {
    private val repository = mockk<UserRepository>()
    private val searchUsers = SearchUsers(repository)

    @Test
    fun `trims the query before searching`() = runTest {
        coEvery { repository.searchUsers("octocat") } returns Result.success(Unit)

        searchUsers("  octocat \n")

        coVerify(exactly = 1) { repository.searchUsers("octocat") }
    }

    @Test
    fun `blank query succeeds without touching the repository`() = runTest {
        val result = searchUsers("   ")

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { repository.searchUsers(any()) }
    }

    @Test
    fun `passes repository failure through`() = runTest {
        val error = DataError.RateLimited(resetAtEpochSeconds = 1_700_000_000)
        coEvery { repository.searchUsers("octocat") } returns Result.failure(error)

        val result = searchUsers("octocat")

        assertTrue(result.exceptionOrNull() === error)
    }
}
