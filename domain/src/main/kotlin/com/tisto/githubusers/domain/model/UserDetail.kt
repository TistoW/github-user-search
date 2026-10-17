package com.tisto.githubusers.domain.model

data class UserDetail(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val type: String,
    val name: String?,
    val bio: String?,
    val company: String?,
    val location: String?,
    val blog: String?,
    val twitterUsername: String?,
    val email: String?,
    // Counts and createdAt stay null for a user only known from a search result.
    val followers: Int?,
    val following: Int?,
    val publicRepos: Int?,
    val publicGists: Int?,
    // ISO-8601 as GitHub sends it. java.time needs desugaring below API 26, so parsing is left to the UI.
    val createdAt: String?,
) {
    val isOrganization: Boolean get() = type.equals("Organization", ignoreCase = true)
}
