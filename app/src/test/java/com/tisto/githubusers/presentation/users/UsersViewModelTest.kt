package com.tisto.githubusers.presentation.users

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.repository.UserRepository
import com.tisto.githubusers.domain.model.UserDetail
import com.tisto.githubusers.domain.usecase.ObserveRecentlyViewed
import com.tisto.githubusers.domain.usecase.ObserveSearchResults
import com.tisto.githubusers.domain.usecase.SearchUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsersViewModelTest {

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

    private fun createViewModel(saved: Map<String, Any?> = emptyMap()) = UsersViewModel(
        SavedStateHandle(saved),
        ObserveSearchResults(repository),
        ObserveRecentlyViewed(repository),
        SearchUsers(repository),
    ).also { it.uiState.observeForever { } }

    @Test
    fun `rapid typing produces a single search after the debounce`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        "octo".indices.forEach { viewModel.onQueryChanged("octo".substring(0, it + 1)); advanceTimeBy(100) }
        advanceTimeBy(299)
        runCurrent()
        assertTrue(repository.searches.isEmpty())

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("octo"), repository.searches)
    }

    @Test
    fun `a blank query shows recently viewed and never searches`() = runTest(dispatcher) {
        repository.recent.value = listOf(user(1))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("   ")
        advanceUntilIdle()

        val state = viewModel.uiState.value!!
        assertEquals(UsersSection.RecentlyViewed, state.section)
        assertEquals(listOf(user(1)), state.users)
        assertTrue(repository.searches.isEmpty())
    }

    @Test
    fun `loading is true while the search runs and false after`() = runTest(dispatcher) {
        repository.searchDelayMs = 1_000
        val viewModel = createViewModel()
        viewModel.onQueryChanged("octo")

        advanceTimeBy(401)
        assertTrue(viewModel.uiState.value!!.isLoading)

        advanceTimeBy(1_000)
        assertEquals(false, viewModel.uiState.value!!.isLoading)
    }

    @Test
    fun `a failed search keeps the cached users and reports the error once`() = runTest(dispatcher) {
        repository.results("octo").value = listOf(user(1), user(2))
        repository.searchResult = Result.failure(DataError.Network())
        val viewModel = createViewModel()

        viewModel.onQueryChanged("octo")
        advanceUntilIdle()

        val state = viewModel.uiState.value!!
        assertEquals(listOf(user(1), user(2)), state.users)
        assertTrue(state.errorToShow is DataError.Network)

        viewModel.onErrorShown()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value!!.errorToShow)
        assertNotNull(viewModel.uiState.value!!.error)
    }

    @Test
    fun `submit searches immediately and the debounce does not repeat it`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onQueryChanged("octo")
        viewModel.submit()
        runCurrent()
        assertEquals(listOf("octo"), repository.searches)

        advanceUntilIdle()
        assertEquals(listOf("octo"), repository.searches)
    }

    @Test
    fun `retry searches the same query again and clears the error`() = runTest(dispatcher) {
        repository.searchResult = Result.failure(DataError.Network())
        val viewModel = createViewModel()
        viewModel.onQueryChanged("octo")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value!!.error)

        repository.searchResult = Result.success(Unit)
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf("octo", "octo"), repository.searches)
        assertNull(viewModel.uiState.value!!.error)
    }

    @Test
    fun `the query is restored from the saved state`() = runTest(dispatcher) {
        val viewModel = createViewModel(mapOf("query" to "octo"))
        assertEquals("octo", viewModel.currentQuery)

        advanceUntilIdle()

        assertEquals(UsersSection.Results, viewModel.uiState.value!!.section)
        assertEquals(listOf("octo"), repository.searches)
    }

    private fun user(id: Long) = User(id, "user$id", "https://avatars/$id", "User")

    private class FakeUserRepository : UserRepository {
        val recent = MutableStateFlow<List<User>>(emptyList())
        val searches = mutableListOf<String>()
        var searchResult: Result<Unit> = Result.success(Unit)
        var searchDelayMs = 0L
        private val cached = mutableMapOf<String, MutableStateFlow<List<User>>>()

        fun results(query: String) = cached.getOrPut(query) { MutableStateFlow(emptyList()) }

        override fun observeSearchResults(query: String): Flow<List<User>> = results(query)

        override suspend fun searchUsers(query: String): Result<Unit> {
            searches += query
            delay(searchDelayMs)
            return searchResult
        }

        override fun observeUser(login: String): Flow<UserDetail?> = MutableStateFlow(null)
        override suspend fun refreshUser(login: String): Result<Unit> = Result.success(Unit)
        override fun observeRecentlyViewed(): Flow<List<User>> = recent
        override suspend fun markViewed(login: String) = Unit
    }
}
