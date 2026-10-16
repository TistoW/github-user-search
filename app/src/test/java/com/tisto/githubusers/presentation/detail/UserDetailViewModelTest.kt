package com.tisto.githubusers.presentation.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.model.UserDetail
import com.tisto.githubusers.domain.repository.UserRepository
import com.tisto.githubusers.domain.usecase.GetUserDetail
import com.tisto.githubusers.domain.usecase.MarkUserViewed
import com.tisto.githubusers.domain.usecase.RefreshUserDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailViewModelTest {

    @get:Rule
    val instantTaskExecutor = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeUserRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = UserDetailViewModel(
        SavedStateHandle(mapOf("login" to "octocat")),
        GetUserDetail(repository),
        RefreshUserDetail(repository),
        MarkUserViewed(repository),
    ).also { it.uiState.observeForever { } }

    @Test
    fun `opening the screen marks the user viewed and refreshes`() = runTest(dispatcher) {
        createViewModel()
        advanceUntilIdle()

        assertEquals(listOf("octocat"), repository.viewed)
        assertEquals(listOf("octocat"), repository.refreshed)
    }

    @Test
    fun `a failed refresh keeps the cached user and reports the error once`() = runTest(dispatcher) {
        repository.user.value = detail()
        repository.refreshResult = Result.failure(DataError.Network())
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value!!
        assertEquals("octocat", state.user?.login)
        assertTrue(state.error is DataError.Network)
        assertFalse(state.notFound)

        viewModel.onErrorShown()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value!!.error)
        assertEquals("octocat", viewModel.uiState.value!!.user?.login)
    }

    @Test
    fun `NotFound switches to the not found state without an error`() = runTest(dispatcher) {
        repository.refreshResult = Result.failure(DataError.NotFound())
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value!!
        assertTrue(state.notFound)
        assertNull(state.error)
    }

    @Test
    fun `refresh toggles isRefreshing and ignores a second call while running`() = runTest(dispatcher) {
        repository.refreshDelayMs = 1_000
        val viewModel = createViewModel()
        runCurrent()
        assertTrue(viewModel.uiState.value!!.isRefreshing)

        advanceTimeBy(1_001)
        assertFalse(viewModel.uiState.value!!.isRefreshing)

        viewModel.refresh()
        viewModel.refresh()
        runCurrent()
        assertTrue(viewModel.uiState.value!!.isRefreshing)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value!!.isRefreshing)
        assertEquals(2, repository.refreshed.size)
    }

    private fun detail() = UserDetail(
        id = 1, login = "octocat", avatarUrl = "https://avatars/1", htmlUrl = "https://github.com/octocat",
        type = "User", name = "The Octocat", bio = null, company = null, location = null, blog = null,
        twitterUsername = null, email = null, followers = 10, following = 2, publicRepos = 8,
        publicGists = 1, createdAt = "2011-01-25T18:44:36Z",
    )

    private class FakeUserRepository : UserRepository {
        val user = MutableStateFlow<UserDetail?>(null)
        val viewed = mutableListOf<String>()
        val refreshed = mutableListOf<String>()
        var refreshResult: Result<Unit> = Result.success(Unit)
        var refreshDelayMs = 0L

        override fun observeUser(login: String): Flow<UserDetail?> = user

        override suspend fun refreshUser(login: String): Result<Unit> {
            refreshed += login
            delay(refreshDelayMs)
            return refreshResult
        }

        override suspend fun markViewed(login: String) {
            viewed += login
        }

        override fun observeSearchResults(query: String): Flow<List<User>> = emptyFlow()
        override suspend fun searchUsers(query: String): Result<Unit> = Result.success(Unit)
        override fun observeRecentlyViewed(): Flow<List<User>> = emptyFlow()
    }
}
