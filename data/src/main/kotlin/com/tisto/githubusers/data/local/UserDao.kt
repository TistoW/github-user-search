package com.tisto.githubusers.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao {

    // REPLACE would delete and re-insert the row: it would wipe the cached detail and, through the
    // cascade, the search_results pointing at it. So both upserts are insert-ignore plus a partial update.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertIgnoring(users: List<UserEntity>)

    @Update(entity = UserEntity::class)
    abstract suspend fun updateSummaries(users: List<UserSummaryUpdate>)

    @Update(entity = UserEntity::class)
    abstract suspend fun updateDetail(user: UserDetailUpdate)

    @Transaction
    open suspend fun upsertSummaries(users: List<UserEntity>) {
        insertIgnoring(users)
        updateSummaries(users.map { UserSummaryUpdate(it.id, it.login, it.avatarUrl, it.htmlUrl, it.type) })
    }

    @Transaction
    open suspend fun upsertDetail(user: UserEntity) {
        insertIgnoring(listOf(user))
        updateDetail(
            UserDetailUpdate(
                id = user.id,
                login = user.login,
                avatarUrl = user.avatarUrl,
                htmlUrl = user.htmlUrl,
                type = user.type,
                name = user.name,
                bio = user.bio,
                company = user.company,
                location = user.location,
                blog = user.blog,
                twitterUsername = user.twitterUsername,
                email = user.email,
                followers = user.followers,
                following = user.following,
                publicRepos = user.publicRepos,
                publicGists = user.publicGists,
                createdAt = user.createdAt,
                detailFetchedAt = user.detailFetchedAt,
            ),
        )
    }

    @Query("SELECT * FROM users WHERE login = :login COLLATE NOCASE LIMIT 1")
    abstract fun observeUser(login: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE last_viewed_at IS NOT NULL ORDER BY last_viewed_at DESC LIMIT :limit")
    abstract fun observeRecentlyViewed(limit: Int): Flow<List<UserEntity>>

    @Query("UPDATE users SET last_viewed_at = :timestamp WHERE login = :login COLLATE NOCASE")
    abstract suspend fun markViewed(login: String, timestamp: Long)
}
