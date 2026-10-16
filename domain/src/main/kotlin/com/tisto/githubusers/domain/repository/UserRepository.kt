package com.tisto.githubusers.domain.repository

import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.model.UserDetail
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first: the observe functions read the local cache only, the suspend
 * functions fetch from the network into the cache. A failed Result carries a
 * [com.tisto.githubusers.domain.model.DataError].
 */
interface UserRepository {
    fun observeSearchResults(query: String): Flow<List<User>>
    suspend fun searchUsers(query: String): Result<Unit>

    fun observeUser(login: String): Flow<UserDetail?>
    suspend fun refreshUser(login: String): Result<Unit>

    fun observeRecentlyViewed(): Flow<List<User>>
    suspend fun markViewed(login: String)
}
