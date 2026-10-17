package com.tisto.githubusers.domain.model

data class User(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val type: String,
) {
    val isOrganization: Boolean get() = type.equals("Organization", ignoreCase = true)
}
