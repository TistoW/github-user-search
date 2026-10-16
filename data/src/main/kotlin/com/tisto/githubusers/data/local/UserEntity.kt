package com.tisto.githubusers.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index("login")])
data class UserEntity(
    @PrimaryKey val id: Long,
    val login: String,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String,
    @ColumnInfo(name = "html_url") val htmlUrl: String,
    val type: String,
    val name: String? = null,
    val bio: String? = null,
    val company: String? = null,
    val location: String? = null,
    val blog: String? = null,
    @ColumnInfo(name = "twitter_username") val twitterUsername: String? = null,
    val email: String? = null,
    val followers: Int? = null,
    val following: Int? = null,
    @ColumnInfo(name = "public_repos") val publicRepos: Int? = null,
    @ColumnInfo(name = "public_gists") val publicGists: Int? = null,
    @ColumnInfo(name = "created_at") val createdAt: String? = null,
    @ColumnInfo(name = "detail_fetched_at") val detailFetchedAt: Long? = null,
    @ColumnInfo(name = "last_viewed_at") val lastViewedAt: Long? = null,
)

/** The columns a search result can know about. Used to update a row without touching the rest. */
data class UserSummaryUpdate(
    val id: Long,
    val login: String,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String,
    @ColumnInfo(name = "html_url") val htmlUrl: String,
    val type: String,
)

/** Everything except last_viewed_at, which belongs to the user's browsing history, not to the API. */
data class UserDetailUpdate(
    val id: Long,
    val login: String,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String,
    @ColumnInfo(name = "html_url") val htmlUrl: String,
    val type: String,
    val name: String?,
    val bio: String?,
    val company: String?,
    val location: String?,
    val blog: String?,
    @ColumnInfo(name = "twitter_username") val twitterUsername: String?,
    val email: String?,
    val followers: Int?,
    val following: Int?,
    @ColumnInfo(name = "public_repos") val publicRepos: Int?,
    @ColumnInfo(name = "public_gists") val publicGists: Int?,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "detail_fetched_at") val detailFetchedAt: Long?,
)
