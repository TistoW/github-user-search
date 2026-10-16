package com.tisto.githubusers.compose.ui.users

import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.model.User

enum class UsersSection { RecentlyViewed, Results }

data class UsersUiState(
    val query: String = "",
    val section: UsersSection = UsersSection.RecentlyViewed,
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    // Stays set after a failed search so the screen can tell "failed" from "no matches".
    val error: DataError? = null,
    val errorHandled: Boolean = false,
) {
    val errorToShow: DataError? get() = error.takeUnless { errorHandled }
}
