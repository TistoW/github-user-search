package com.tisto.githubusers.compose.ui.users

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tisto.githubusers.compose.R
import com.tisto.githubusers.compose.ui.common.errorMessage
import com.tisto.githubusers.domain.model.User

@Composable
fun UsersScreenRoute(
    onUserClick: (User) -> Unit,
    viewModel: UsersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val queryText by viewModel.queryText.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    val retryLabel = stringResource(R.string.retry)

    // Keyed on the error itself: a new search clears it, which also dismisses the stale snackbar.
    LaunchedEffect(state.errorToShow) {
        val error = state.errorToShow ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = resources.errorMessage(error, hasCachedResults = state.users.isNotEmpty(), isSearch = true),
            actionLabel = retryLabel,
            duration = SnackbarDuration.Long,
        )
        viewModel.onErrorShown()
        if (result == SnackbarResult.ActionPerformed) viewModel.submit()
    }

    UsersScreen(
        state = state,
        queryText = queryText,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::onQueryChanged,
        onSearch = viewModel::submit,
        onUserClick = onUserClick,
    )
}
