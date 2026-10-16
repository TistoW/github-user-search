package com.tisto.githubusers.compose.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.tisto.githubusers.compose.R
import com.tisto.githubusers.compose.ui.detail.UserDetailScreen
import com.tisto.githubusers.compose.ui.detail.UserDetailUiState
import com.tisto.githubusers.compose.ui.theme.GitHubUsersTheme
import com.tisto.githubusers.compose.ui.users.UsersScreen
import com.tisto.githubusers.compose.ui.users.UsersSection
import com.tisto.githubusers.compose.ui.users.UsersUiState
import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.model.UserDetail
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// The screens are stateless, so plain UiState objects drive them: no Hilt, no repository.
class ScreensTest {

    @get:Rule
    val compose = createComposeRule()

    private val octocat = User(1, "octocat", AVATAR, "User")
    private val octokit = User(2, "octokit", AVATAR, "User")

    private fun string(id: Int, vararg args: Any) =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    private fun showUsers(
        state: UsersUiState,
        queryText: String = state.query,
        onQueryChange: (String) -> Unit = {},
    ) = compose.setContent {
        GitHubUsersTheme {
            UsersScreen(
                state = state,
                queryText = queryText,
                snackbarHostState = SnackbarHostState(),
                onQueryChange = onQueryChange,
                onSearch = {},
                onUserClick = {},
            )
        }
    }

    private fun showDetail(state: UserDetailUiState, onBack: () -> Unit = {}) = compose.setContent {
        GitHubUsersTheme {
            UserDetailScreen(
                state = state,
                login = "octocat",
                fallbackAvatarUrl = AVATAR,
                isPulling = false,
                snackbarHostState = SnackbarHostState(),
                onBack = onBack,
                onPullToRefresh = {},
            )
        }
    }

    @Test
    fun startStateAsksForASearch() {
        showUsers(UsersUiState())

        compose.onNodeWithText(string(R.string.empty_start)).assertIsDisplayed()
    }

    @Test
    fun resultsShowLoginsAndCount() {
        showUsers(UsersUiState(query = "octo", section = UsersSection.Results, users = listOf(octocat, octokit)))

        compose.onNodeWithText("octocat").assertIsDisplayed()
        compose.onNodeWithText("octokit").assertIsDisplayed()
        compose.onNodeWithText("2 users").assertIsDisplayed()
    }

    @Test
    fun recentlyViewedShowsItsHeader() {
        showUsers(UsersUiState(users = listOf(octocat)))

        compose.onNodeWithText(string(R.string.recently_viewed)).assertIsDisplayed()
        compose.onNodeWithText("octocat").assertIsDisplayed()
    }

    @Test
    fun failedSearchWithoutCacheShowsTheOfflineEmptyState() {
        showUsers(UsersUiState(query = "octo", section = UsersSection.Results, error = DataError.Network()))

        compose.onNodeWithText(string(R.string.empty_not_loaded)).assertIsDisplayed()
    }

    @Test
    fun clearButtonEmptiesTheQuery() {
        var query: String? = null
        showUsers(UsersUiState(query = "octo", section = UsersSection.Results), onQueryChange = { query = it })

        compose.onNodeWithContentDescription(string(R.string.clear_search)).performClick()

        assertEquals("", query)
    }

    @Test
    fun detailShowsProfileStatsAndInfoRows() {
        showDetail(UserDetailUiState(user = profile))

        compose.onNodeWithText("The Octocat").assertIsDisplayed()
        compose.onNodeWithText("@octocat").assertIsDisplayed()
        compose.onNodeWithText("Mascot of GitHub").assertIsDisplayed()
        compose.onNodeWithText("1.5k").assertIsDisplayed()
        compose.onNodeWithText("12").assertIsDisplayed()
        compose.onNodeWithText("345").assertIsDisplayed()
        compose.onNodeWithText("@github").assertIsDisplayed()
        compose.onNodeWithText("San Francisco").assertIsDisplayed()
        compose.onNodeWithText("https://github.blog").assertIsDisplayed()
        compose.onNode(hasText("Joined", substring = true)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.view_on_github)).assertIsDisplayed()
    }

    @Test
    fun detailHidesRowsWithoutValues() {
        showDetail(
            UserDetailUiState(
                user = profile.copy(
                    bio = null, company = null, location = null, blog = null, createdAt = null,
                    followers = null, following = null, publicRepos = null,
                ),
            ),
        )

        compose.onNodeWithText("The Octocat").assertIsDisplayed()
        compose.onNodeWithText("@github").assertDoesNotExist()
        compose.onNodeWithText("San Francisco").assertDoesNotExist()
        compose.onNodeWithText("https://github.blog").assertDoesNotExist()
        compose.onNodeWithText(string(R.string.stat_followers)).assertDoesNotExist()
        compose.onNode(hasText("Joined", substring = true)).assertDoesNotExist()
    }

    @Test
    fun notFoundOffersGoBack() {
        var wentBack = false
        showDetail(UserDetailUiState(notFound = true), onBack = { wentBack = true })

        compose.onNodeWithText(string(R.string.not_found_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.go_back)).performClick()

        assertEquals(true, wentBack)
    }

    private val profile = UserDetail(
        id = 1, login = "octocat", avatarUrl = AVATAR, htmlUrl = "https://github.com/octocat",
        type = "User", name = "The Octocat", bio = "Mascot of GitHub", company = "@github",
        location = "San Francisco", blog = "https://github.blog", twitterUsername = null, email = null,
        followers = 1500, following = 12, publicRepos = 345, publicGists = 0,
        createdAt = "2011-01-25T18:44:36Z",
    )

    private companion object {
        const val AVATAR = "https://avatars.invalid/u.png"
    }
}
