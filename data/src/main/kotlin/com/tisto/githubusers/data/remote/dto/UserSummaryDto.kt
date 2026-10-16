package com.tisto.githubusers.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserSummaryDto(
    val id: Long,
    val login: String,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "html_url") val htmlUrl: String,
    val type: String,
)
