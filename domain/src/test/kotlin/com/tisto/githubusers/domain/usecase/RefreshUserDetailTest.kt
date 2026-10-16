package com.tisto.githubusers.domain.usecase

import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshUserDetailTest {
    private val repository = mockk<UserRepository>()
    private val refreshUserDetail = RefreshUserDetail(repository)

    @Test
    fun `returns the repository result for the given login`() = runTest {
        val error = DataError.NotFound()
        coEvery { repository.refreshUser("ghost") } returns Result.failure(error)

        val result = refreshUserDetail("ghost")

        assertTrue(result.exceptionOrNull() === error)
    }
}
