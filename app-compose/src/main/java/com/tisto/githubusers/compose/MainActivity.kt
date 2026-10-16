package com.tisto.githubusers.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tisto.githubusers.compose.navigation.GitHubUsersNavHost
import com.tisto.githubusers.compose.ui.theme.GitHubUsersTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            GitHubUsersTheme {
                GitHubUsersNavHost()
            }
        }
    }
}
