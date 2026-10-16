package com.tisto.githubusers.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDetailDto(
    val id: Long,
    val login: String,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "html_url") val htmlUrl: String,
    val type: String,
    val name: String?,
    val bio: String?,
    val company: String?,
    val location: String?,
    val blog: String?,
    @Json(name = "twitter_username") val twitterUsername: String?,
    val email: String?,
    val followers: Int,
    val following: Int,
    @Json(name = "public_repos") val publicRepos: Int,
    @Json(name = "public_gists") val publicGists: Int,
    @Json(name = "created_at") val createdAt: String?,
)
