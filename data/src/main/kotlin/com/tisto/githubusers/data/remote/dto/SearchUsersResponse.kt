package com.tisto.githubusers.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchUsersResponse(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "incomplete_results") val incompleteResults: Boolean,
    val items: List<UserSummaryDto>,
)
