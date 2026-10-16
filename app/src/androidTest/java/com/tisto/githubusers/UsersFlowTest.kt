package com.tisto.githubusers

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.tisto.githubusers.di.FakeUserRepository
import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.model.UserDetail
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

// The search goes through the IME action, which skips the 400 ms debounce, and the fake answers
// immediately, so Espresso's main-thread idling is all the synchronisation these tests need.
@HiltAndroidTest
class UsersFlowTest {

    @get:Rule
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var repository: FakeUserRepository

    private lateinit var scenario: ActivityScenario<MainActivity>

    private val octocat = User(1, "octocat", AVATAR, "User")
    private val octokit = User(2, "octokit", AVATAR, "User")

    @Before
    fun setUp() {
        hilt.inject()
        repository.searchResults = listOf(octocat, octokit)
        repository.cacheDetail(
            UserDetail(
                id = 1, login = "octocat", avatarUrl = AVATAR, htmlUrl = "https://github.com/octocat",
                type = "User", name = "The Octocat", bio = "Mascot of GitHub", company = null,
                location = null, blog = null, twitterUsername = null, email = null,
                followers = 1500, following = 12, publicRepos = 345, publicGists = 0,
                createdAt = "2011-01-25T18:44:36Z",
            ),
        )
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() = scenario.close()

    @Test
    fun firstLaunchShowsTheStartPrompt() {
        onView(withText(R.string.empty_start)).check(matches(isDisplayed()))
    }

    @Test
    fun searchShowsResultsAndCount() {
        search("octo")

        onView(withText("octocat")).check(matches(isDisplayed()))
        onView(withText("octokit")).check(matches(isDisplayed()))
        onView(withId(R.id.results_count)).check(matches(withText("2 users")))
    }

    @Test
    fun tappingAResultOpensTheDetailAndBackReturns() {
        search("octo")
        openUser("octocat")

        onView(withText("The Octocat")).check(matches(isDisplayed()))
        onView(withText("@octocat")).check(matches(isDisplayed()))
        onView(withText("Mascot of GitHub")).check(matches(isDisplayed()))
        onView(withId(R.id.stat_followers)).check(matches(withText("1.5k")))
        onView(withId(R.id.stat_following)).check(matches(withText("12")))
        onView(withId(R.id.stat_repos)).check(matches(withText("345")))

        pressBack()
        onView(withId(R.id.results_count)).check(matches(isDisplayed()))
    }

    @Test
    fun clearingTheQueryShowsTheViewedUser() {
        search("octo")
        openUser("octocat")
        pressBack()

        // The clear icon only shows while the field has focus.
        onView(withId(R.id.search_input)).perform(click())
        onView(withContentDescription(R.string.clear_search)).perform(click())

        onView(withText(R.string.recently_viewed)).check(matches(isDisplayed()))
        onView(allOf(withText("octocat"), isDescendantOfA(withId(R.id.list)))).check(matches(isDisplayed()))
    }

    @Test
    fun offlineWithoutCacheExplainsItself() {
        repository.searchError = DataError.Network()

        search("octo")

        onView(withText(R.string.empty_not_loaded)).check(matches(isDisplayed()))
        onView(withText(R.string.error_offline)).check(matches(isDisplayed()))
    }

    @Test
    fun missingAccountShowsNotFound() {
        search("octo")
        repository.refreshError = DataError.NotFound()

        openUser("octokit")

        onView(withText(R.string.not_found_title)).check(matches(isDisplayed()))
    }

    private fun search(query: String) {
        onView(withId(R.id.search_input)).perform(typeText(query), pressImeActionButton())
    }

    private fun openUser(login: String) {
        onView(withId(R.id.list)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(login)), click()),
        )
    }

    private companion object {
        const val AVATAR = "https://avatars.invalid/u.png"
    }
}
