package com.tisto.githubusers.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tisto.githubusers.compose.ui.detail.UserDetailRoute
import com.tisto.githubusers.compose.ui.users.UsersScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data object UsersRoute

// The avatar url travels with the route so the detail header shows the picture before the profile loads.
@Serializable
data class DetailRoute(val login: String, val avatarUrl: String)

@Composable
fun GitHubUsersNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = UsersRoute) {
        composable<UsersRoute> {
            UsersScreenRoute(onUserClick = { navController.navigate(DetailRoute(it.login, it.avatarUrl)) })
        }
        composable<DetailRoute> { entry ->
            val route = entry.toRoute<DetailRoute>()
            UserDetailRoute(
                login = route.login,
                avatarUrl = route.avatarUrl,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
