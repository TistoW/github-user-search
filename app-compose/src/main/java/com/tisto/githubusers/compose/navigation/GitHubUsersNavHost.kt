package com.tisto.githubusers.compose.navigation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tisto.githubusers.compose.ui.theme.GitHubUsersTheme
import com.tisto.githubusers.compose.ui.users.UsersScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data object UsersRoute

@Serializable
data class DetailRoute(val login: String)

@Composable
fun GitHubUsersNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = UsersRoute) {
        composable<UsersRoute> {
            UsersScreenRoute(onUserClick = { navController.navigate(DetailRoute(it.login)) })
        }
        composable<DetailRoute> { entry ->
            Placeholder(text = entry.toRoute<DetailRoute>().login)
        }
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlaceholderPreview() {
    GitHubUsersTheme { Placeholder(text = "Users") }
}
