package com.tisto.githubusers.compose.ui.detail

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tisto.githubusers.compose.R
import com.tisto.githubusers.compose.ui.common.errorMessage

@Composable
fun UserDetailRoute(
    login: String,
    avatarUrl: String,
    onBack: () -> Unit,
    viewModel: UserDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    val retryLabel = stringResource(R.string.retry)

    // True while the refresh was started by a pull, so the pull spinner and the top bar never show together.
    var pulling by remember { mutableStateOf(false) }
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) pulling = false
    }

    // Keyed on the error itself: a new refresh clears it, which also dismisses the stale snackbar.
    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = resources.errorMessage(error, hasCachedResults = state.user != null),
            actionLabel = retryLabel,
            duration = SnackbarDuration.Long,
        )
        viewModel.onErrorShown()
        if (result == SnackbarResult.ActionPerformed) viewModel.refresh()
    }

    UserDetailScreen(
        state = state,
        login = login,
        fallbackAvatarUrl = avatarUrl,
        isPulling = pulling,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onPullToRefresh = {
            pulling = true
            viewModel.refresh()
        },
    )
}
