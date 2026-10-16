package com.tisto.githubusers.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.tisto.githubusers.compose.di.FakeUserRepository
import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.model.UserDetail
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import javax.inject.Inject

// Searching goes through the IME action, which skips the 400 ms debounce. Compose idles with the
// UI, but the ViewModel works on its own coroutines, hence waitUntil for what the fake delivers.
@HiltAndroidTest
class SearchToDetailFlowTest {

    private val hilt = HiltAndroidRule(this)
    private val compose = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(hilt).around(compose)

    @Inject
    lateinit var repository: FakeUserRepository

    @Before
    fun setUp() {
        hilt.inject()
        repository.searchResults = listOf(User(1, "octocat", AVATAR, "User"), User(2, "octokit", AVATAR, "User"))
        repository.cacheDetail(
            UserDetail(
                id = 1, login = "octocat", avatarUrl = AVATAR, htmlUrl = "https://github.com/octocat",
                type = "User", name = "The Octocat", bio = "Mascot of GitHub", company = null,
                location = null, blog = null, twitterUsername = null, email = null,
                followers = 1500, following = 12, publicRepos = 345, publicGists = 0,
                createdAt = "2011-01-25T18:44:36Z",
            ),
        )
    }

    @Test
    fun searchOpenDetailGoBackAndSeeRecentlyViewed() {
        compose.onNode(hasSetTextAction()).performTextInput("octo")
        compose.onNode(hasSetTextAction()).performImeAction()
        compose.waitForText("octocat")
        compose.onNodeWithText("2 users").assertIsDisplayed()

        compose.onNodeWithText("octocat").performClick()
        compose.waitForText("The Octocat")
        compose.onNodeWithText("@octocat").assertIsDisplayed()
        compose.onNodeWithText("Mascot of GitHub").assertIsDisplayed()

        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        compose.waitForText("2 users")

        compose.onNodeWithContentDescription(string(R.string.clear_search)).performClick()
        compose.waitForText(string(R.string.recently_viewed))
        compose.onNodeWithText("octocat").assertIsDisplayed()
    }

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.waitForText(text: String) =
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    private companion object {
        const val AVATAR = "https://avatars.invalid/u.png"
    }
}
