package com.tisto.githubusers.compose.ui.detail

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.tisto.githubusers.compose.R
import com.tisto.githubusers.compose.ui.common.compactCount
import com.tisto.githubusers.compose.ui.common.formatJoinedDate
import com.tisto.githubusers.compose.ui.common.toWebUrl
import com.tisto.githubusers.compose.ui.common.withAvatarSize
import com.tisto.githubusers.compose.ui.theme.Dimens
import com.tisto.githubusers.compose.ui.theme.GitHubUsersTheme
import com.tisto.githubusers.compose.ui.theme.Spacing
import com.tisto.githubusers.compose.ui.theme.StatTextStyle
import com.tisto.githubusers.domain.model.UserDetail

private val CardShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    state: UserDetailUiState,
    login: String,
    fallbackAvatarUrl: String,
    isPulling: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onPullToRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = login, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Normal)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.navigationBarsPadding())
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            RefreshBar(visible = state.isRefreshing && !isPulling)
            if (state.notFound) {
                NotFound(onBack = onBack)
            } else {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing && isPulling,
                    onRefresh = onPullToRefresh,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    DetailContent(login = login, fallbackAvatarUrl = fallbackAvatarUrl, user = state.user)
                }
            }
        }
    }
}

// The slot keeps its height when idle so the content does not jump.
@Composable
private fun RefreshBar(visible: Boolean) {
    if (visible) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    } else {
        Spacer(Modifier.fillMaxWidth().height(4.dp))
    }
}

@Composable
private fun DetailContent(login: String, fallbackAvatarUrl: String, user: UserDetail?) {
    val gutter = if (LocalConfiguration.current.screenWidthDp >= 600) 120.dp else Dimens.detailGutter
    val uriHandler = LocalUriHandler.current
    // A mail client or browser may be missing, which should not crash the screen.
    val open: (String) -> Unit = { runCatching { uriHandler.openUri(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = gutter)
            .padding(top = Spacing.m, bottom = Spacing.xl)
            .navigationBarsPadding(),
    ) {
        Header(login = login, avatarUrl = user?.avatarUrl ?: fallbackAvatarUrl, user = user)
        if (user == null) return@Column

        user.bio?.takeIf { it.isNotBlank() }?.let { bio ->
            Text(
                text = bio.trim(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.l),
            )
        }

        // A user known only from a search result has no counts yet.
        val followers = user.followers
        val following = user.following
        val repos = user.publicRepos
        if (followers != null && following != null && repos != null) {
            StatsCard(followers = followers, following = following, repos = repos)
        }

        Column(modifier = Modifier.padding(top = Spacing.m)) {
            InfoRow(R.drawable.ic_company, user.company)
            InfoRow(R.drawable.ic_location, user.location)
            InfoRow(R.drawable.ic_link, user.blog, onClick = user.blog?.let { { open(it.toWebUrl()) } })
            InfoRow(
                R.drawable.ic_alternate_email,
                user.twitterUsername?.let { "@$it" },
                onClick = user.twitterUsername?.let { { open("https://x.com/$it") } },
            )
            InfoRow(R.drawable.ic_mail, user.email, onClick = user.email?.let { { open("mailto:$it") } })
            InfoRow(
                R.drawable.ic_calendar,
                formatJoinedDate(user.createdAt)?.let { stringResource(R.string.detail_joined, it) },
            )
        }

        Button(
            onClick = { open(user.htmlUrl) },
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.l),
        ) {
            Icon(painterResource(R.drawable.ic_open_in_new), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.s))
            Text(stringResource(R.string.view_on_github), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun Header(login: String, avatarUrl: String, user: UserDetail?) {
    val avatarPx = with(LocalDensity.current) { Dimens.avatarDetail.roundToPx() * 2 }
    // Without a display name the login is the title, so the @handle line would only repeat it.
    val name = user?.name?.takeIf { it.isNotBlank() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        GlideImage(
            model = avatarUrl.withAvatarSize(avatarPx),
            contentDescription = stringResource(R.string.avatar_description, login),
            loading = placeholder(R.drawable.avatar_placeholder),
            failure = placeholder(R.drawable.avatar_placeholder),
            modifier = Modifier.size(Dimens.avatarDetail).clip(CircleShape),
        )
        Column(modifier = Modifier.weight(1f).padding(start = Spacing.l)) {
            Text(
                text = name ?: login,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (name != null) {
                Text(
                    text = stringResource(R.string.handle, login),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
            if (user?.type.equals("Organization", ignoreCase = true)) {
                OrganizationChip(modifier = Modifier.padding(top = Spacing.s))
            }
        }
    }
}

@Composable
private fun OrganizationChip(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.user_type_organization),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = Spacing.m, vertical = Spacing.xs),
    )
}

@Composable
private fun StatsCard(followers: Int, following: Int, repos: Int) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(Dimens.dividerHeight, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.l),
    ) {
        Row(modifier = Modifier.padding(start = Spacing.xs, end = Spacing.xs, top = 15.dp, bottom = Spacing.m)) {
            Stat(compactCount(followers), R.string.stat_followers, R.drawable.ic_people)
            Stat(compactCount(following), R.string.stat_following, R.drawable.ic_person)
            Stat(compactCount(repos), R.string.stat_repos, R.drawable.ic_repo)
        }
    }
}

@Composable
private fun RowScope.Stat(value: String, label: Int, icon: Int) {
    Column(modifier = Modifier.weight(1f).padding(horizontal = Spacing.m)) {
        Text(
            text = value,
            style = StatTextStyle,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Spacing.xs)) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(start = Spacing.xs),
            )
        }
    }
}

@Composable
private fun InfoRow(icon: Int, text: String?, onClick: (() -> Unit)? = null) {
    if (text.isNullOrBlank()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.minTouchTarget)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (onClick != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(start = Spacing.m, top = Spacing.s, bottom = Spacing.s),
        )
    }
}

@Composable
private fun NotFound(onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxl),
    ) {
        Icon(
            painterResource(R.drawable.ic_error),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(Dimens.iconSizeLarge),
        )
        Text(
            text = stringResource(R.string.not_found_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.m),
        )
        FilledTonalButton(
            onClick = onBack,
            modifier = Modifier.padding(top = Spacing.l).heightIn(min = Dimens.minTouchTarget),
        ) {
            Text(stringResource(R.string.go_back))
        }
    }
}

private val fullProfile = UserDetail(
    id = 1,
    login = "octocat",
    avatarUrl = "",
    htmlUrl = "https://github.com/octocat",
    type = "User",
    name = "The Octocat",
    bio = "Building things on GitHub, one pull request at a time.",
    company = "@github",
    location = "San Francisco",
    blog = "github.blog",
    twitterUsername = "github",
    email = "octocat@github.com",
    followers = 20_000,
    following = 9,
    publicRepos = 8,
    publicGists = 8,
    createdAt = "2011-01-25T18:44:36Z",
)

private val minimalProfile = UserDetail(
    id = 2,
    login = "ghost",
    avatarUrl = "",
    htmlUrl = "https://github.com/ghost",
    type = "User",
    name = null,
    bio = null,
    company = null,
    location = null,
    blog = null,
    twitterUsername = null,
    email = null,
    followers = null,
    following = null,
    publicRepos = null,
    publicGists = null,
    createdAt = null,
)

@Composable
private fun DetailPreview(user: UserDetail?, notFound: Boolean = false) {
    GitHubUsersTheme {
        UserDetailScreen(
            state = UserDetailUiState(user = user, notFound = notFound),
            login = user?.login ?: "ghost",
            fallbackAvatarUrl = "",
            isPulling = false,
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onPullToRefresh = {},
        )
    }
}

@Preview(name = "Full light", showBackground = true)
@Preview(name = "Full dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FullProfilePreview() = DetailPreview(fullProfile)

@Preview(name = "Minimal light", showBackground = true)
@Preview(name = "Minimal dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MinimalProfilePreview() = DetailPreview(minimalProfile)

@Preview(name = "Not found light", showBackground = true)
@Preview(name = "Not found dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun NotFoundPreview() = DetailPreview(user = null, notFound = true)
