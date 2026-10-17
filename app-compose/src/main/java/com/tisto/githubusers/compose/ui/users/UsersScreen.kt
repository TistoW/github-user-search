package com.tisto.githubusers.compose.ui.users

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.tisto.githubusers.compose.R
import com.tisto.githubusers.compose.ui.common.withAvatarSize
import com.tisto.githubusers.compose.ui.theme.Dimens
import com.tisto.githubusers.compose.ui.theme.GitHubUsersTheme
import com.tisto.githubusers.compose.ui.theme.Spacing
import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.model.User

@Composable
fun UsersScreen(
    state: UsersUiState,
    queryText: String,
    snackbarHostState: SnackbarHostState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onUserClick: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // The keyboard shrinks the content so the empty state stays visible.
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
            Text(
                text = stringResource(R.string.title_users),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = Spacing.l).padding(top = 20.dp),
            )
            SearchField(
                text = queryText,
                onTextChange = onQueryChange,
                onSearch = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.l)
                    .padding(top = Spacing.m, bottom = Spacing.s),
            )
            LoadingBar(isLoading = state.isLoading)
            UsersContent(state = state, onUserClick = onUserClick, modifier = Modifier.weight(1f))
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).imePadding().navigationBarsPadding(),
        )
    }
}

@Composable
private fun SearchField(
    text: String,
    onTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier,
        singleLine = true,
        shape = CircleShape,
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = { Text(stringResource(R.string.search_hint), color = muted) },
        leadingIcon = {
            Icon(painterResource(R.drawable.ic_search), contentDescription = null, tint = muted)
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { onTextChange("") }) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.clear_search),
                        tint = muted,
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboard?.hide()
                focusManager.clearFocus()
                onSearch()
            },
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
        ),
    )
}

// The slot keeps its height when idle so the list does not jump when a search starts.
@Composable
private fun LoadingBar(isLoading: Boolean) {
    if (isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    } else {
        Spacer(Modifier.fillMaxWidth().height(8.dp))
    }
}

@Composable
private fun UsersContent(
    state: UsersUiState,
    onUserClick: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    val recent = state.section == UsersSection.RecentlyViewed
    Column(modifier = modifier.fillMaxWidth()) {
        if (state.users.isNotEmpty()) {
            if (recent) {
                RecentHeader()
            } else {
                Text(
                    text = pluralStringResource(R.plurals.results_count, state.users.size, state.users.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = Spacing.l)
                        .padding(top = Spacing.m),
                )
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            UserList(users = state.users, onUserClick = onUserClick)
            if (state.users.isEmpty() && !state.isLoading) {
                EmptyState(
                    state = state,
                    recent = recent,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun RecentHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = Spacing.l)
            .padding(top = Spacing.m, bottom = Spacing.xs),
    ) {
        Icon(
            painterResource(R.drawable.ic_history),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.recently_viewed),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Spacing.s),
        )
    }
}

@Composable
private fun UserList(users: List<User>, onUserClick: (User) -> Unit) {
    // With the keyboard open the parent already pads for it, so only the bare navigation bar is added here.
    val bottomInset = WindowInsets.navigationBars.exclude(WindowInsets.ime).asPaddingValues()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomInset.calculateBottomPadding()),
    ) {
        items(items = users, key = { it.id }) { user ->
            Column {
                UserRow(user = user, onClick = { onUserClick(user) })
                if (user != users.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Dimens.dividerInset, end = Spacing.l),
                        thickness = Dimens.dividerHeight,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun UserRow(user: User, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val avatarPx = with(LocalDensity.current) { Dimens.avatarList.roundToPx() * 2 }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.l),
    ) {
        GlideImage(
            model = user.avatarUrl.withAvatarSize(avatarPx),
            contentDescription = stringResource(R.string.avatar_description, user.login),
            loading = placeholder(R.drawable.avatar_placeholder),
            failure = placeholder(R.drawable.avatar_placeholder),
            modifier = Modifier.size(Dimens.avatarList).clip(CircleShape),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.weight(1f).padding(start = Spacing.m),
        ) {
            Text(
                text = user.login,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    if (user.type.equals("Organization", ignoreCase = true)) R.string.user_type_organization
                    else R.string.user_type_user,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(Dimens.iconSize),
        )
    }
}

@Composable
private fun EmptyState(state: UsersUiState, recent: Boolean, modifier: Modifier = Modifier) {
    @DrawableRes val icon: Int
    val text: String
    when {
        recent -> {
            icon = R.drawable.ic_search
            text = stringResource(R.string.empty_start)
        }
        state.error is DataError.RateLimited -> {
            icon = R.drawable.ic_error
            text = stringResource(R.string.empty_rate_limited)
        }
        state.error != null -> {
            icon = R.drawable.ic_cloud_off
            text = stringResource(R.string.empty_not_loaded)
        }
        else -> {
            icon = R.drawable.ic_people
            text = stringResource(R.string.empty_no_matches, state.query)
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.xxl),
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(Dimens.iconSizeLarge),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.m),
        )
    }
}

private val previewUsers = listOf(
    User(1, "octocat", "", "User"),
    User(2, "github", "", "Organization"),
    User(3, "defunkt", "", "User"),
)

@Composable
private fun UsersPreview(state: UsersUiState, query: String) {
    GitHubUsersTheme {
        UsersScreen(
            state = state,
            queryText = query,
            snackbarHostState = SnackbarHostState(),
            onQueryChange = {},
            onSearch = {},
            onUserClick = {},
        )
    }
}

@Preview(name = "Results light", showBackground = true)
@Preview(name = "Results dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ResultsPreview() = UsersPreview(
    UsersUiState(query = "octo", section = UsersSection.Results, users = previewUsers, isLoading = true),
    query = "octo",
)

@Preview(name = "Recent light", showBackground = true)
@Preview(name = "Recent dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun RecentPreview() = UsersPreview(UsersUiState(users = previewUsers), query = "")

@Preview(name = "Empty light", showBackground = true)
@Preview(name = "Empty dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EmptyPreview() = UsersPreview(UsersUiState(), query = "")

@Preview(name = "Offline light", showBackground = true)
@Preview(name = "Offline dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OfflinePreview() = UsersPreview(
    UsersUiState(query = "octo", section = UsersSection.Results, error = DataError.Network()),
    query = "octo",
)
