package com.tisto.githubusers.data.repository

import androidx.room.withTransaction
import com.tisto.githubusers.data.local.GitHubDatabase
import com.tisto.githubusers.data.local.SearchResultEntity
import com.tisto.githubusers.data.mapper.toEntity
import com.tisto.githubusers.data.mapper.toUser
import com.tisto.githubusers.data.mapper.toUserDetail
import com.tisto.githubusers.data.remote.GitHubApi
import com.tisto.githubusers.data.remote.apiCall
import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.model.UserDetail
import com.tisto.githubusers.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Retrofit and Room suspend functions are main-safe, so no dispatcher is switched here.
class UserRepositoryImpl(
    private val api: GitHubApi,
    private val database: GitHubDatabase,
    private val now: () -> Long = System::currentTimeMillis,
) : UserRepository {

    private val userDao = database.userDao()
    private val searchResultDao = database.searchResultDao()

    override fun observeSearchResults(query: String): Flow<List<User>> =
        searchResultDao.observeResults(query.cacheKey()).map { rows -> rows.map { it.toUser() } }

    // A failed call leaves the cached results for this query untouched.
    override suspend fun searchUsers(query: String): Result<Unit> {
        val key = query.cacheKey()
        return apiCall { api.searchUsers(key) }.map { response ->
            database.withTransaction {
                userDao.upsertSummaries(response.items.map { it.toEntity() })
                searchResultDao.replaceResults(
                    key,
                    response.items.mapIndexed { index, dto -> SearchResultEntity(key, dto.id, index) },
                )
            }
        }
    }

    override fun observeUser(login: String): Flow<UserDetail?> =
        userDao.observeUser(login).map { it?.toUserDetail() }

    override suspend fun refreshUser(login: String): Result<Unit> =
        apiCall { api.getUser(login) }.map { userDao.upsertDetail(it.toEntity(now())) }

    override fun observeRecentlyViewed(): Flow<List<User>> =
        userDao.observeRecentlyViewed(RECENTLY_VIEWED_LIMIT).map { rows -> rows.map { it.toUser() } }

    override suspend fun markViewed(login: String) = userDao.markViewed(login, now())

    private fun String.cacheKey() = trim().lowercase()

    private companion object {
        const val RECENTLY_VIEWED_LIMIT = 20
    }
}
