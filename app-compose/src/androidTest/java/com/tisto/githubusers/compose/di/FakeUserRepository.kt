package com.tisto.githubusers.compose.di

import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.model.UserDetail
import com.tisto.githubusers.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory stand-in for the offline-first repository. Like the real one, a successful
 * search writes into the "cache" that the observe flows read from.
 */
@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {

    /** What the next successful search returns, for any query. */
    var searchResults: List<User> = emptyList()
    var searchError: DataError? = null
    var refreshError: DataError? = null

    private val cachedSearches = mutableMapOf<String, MutableStateFlow<List<User>>>()
    private val cachedDetails = mutableMapOf<String, MutableStateFlow<UserDetail?>>()
    private val recentlyViewed = MutableStateFlow<List<User>>(emptyList())

    fun cacheDetail(detail: UserDetail) {
        detailFlow(detail.login).value = detail
    }

    override fun observeSearchResults(query: String): Flow<List<User>> = searchFlow(query)

    override suspend fun searchUsers(query: String): Result<Unit> {
        searchError?.let { return Result.failure(it) }
        searchFlow(query).value = searchResults
        return Result.success(Unit)
    }

    override fun observeUser(login: String): Flow<UserDetail?> = detailFlow(login)

    override suspend fun refreshUser(login: String): Result<Unit> =
        refreshError?.let { Result.failure(it) } ?: Result.success(Unit)

    override fun observeRecentlyViewed(): Flow<List<User>> = recentlyViewed

    override suspend fun markViewed(login: String) {
        val user = searchResults.firstOrNull { it.login == login } ?: return
        recentlyViewed.value = listOf(user) + recentlyViewed.value.filterNot { it.login == login }
    }

    private fun searchFlow(query: String): MutableStateFlow<List<User>> =
        cachedSearches.getOrPut(query) { MutableStateFlow(emptyList()) }

    private fun detailFlow(login: String): MutableStateFlow<UserDetail?> =
        cachedDetails.getOrPut(login) { MutableStateFlow(null) }
}
