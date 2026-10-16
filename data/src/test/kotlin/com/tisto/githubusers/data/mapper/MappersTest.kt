package com.tisto.githubusers.data.mapper

import com.tisto.githubusers.data.local.UserEntity
import com.tisto.githubusers.data.remote.dto.UserDetailDto
import com.tisto.githubusers.data.remote.dto.UserSummaryDto
import com.tisto.githubusers.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    private val summaryDto = UserSummaryDto(1, "octocat", "https://avatar", "https://github.com/octocat", "User")

    private val detailDto = UserDetailDto(
        id = 1,
        login = "octocat",
        avatarUrl = "https://avatar",
        htmlUrl = "https://github.com/octocat",
        type = "User",
        name = "The Octocat",
        bio = null,
        company = "@github",
        location = "San Francisco",
        blog = "https://github.blog",
        twitterUsername = null,
        email = null,
        followers = 20000,
        following = 9,
        publicRepos = 8,
        publicGists = 8,
        createdAt = "2011-01-25T18:44:36Z",
    )

    @Test
    fun `summary dto becomes an entity without detail`() {
        val entity = summaryDto.toEntity()

        assertEquals("octocat", entity.login)
        assertEquals("https://github.com/octocat", entity.htmlUrl)
        assertNull(entity.followers)
        assertNull(entity.detailFetchedAt)
        assertNull(entity.lastViewedAt)
    }

    @Test
    fun `detail dto becomes an entity stamped with the fetch time`() {
        val entity = detailDto.toEntity(fetchedAt = 42L)

        assertEquals("The Octocat", entity.name)
        assertEquals(20000, entity.followers)
        assertEquals(8, entity.publicRepos)
        assertEquals("2011-01-25T18:44:36Z", entity.createdAt)
        assertEquals(42L, entity.detailFetchedAt)
        assertNull(entity.lastViewedAt)
    }

    @Test
    fun `entity maps to the list model`() {
        assertEquals(User(1, "octocat", "https://avatar", "User"), detailDto.toEntity(1L).toUser())
    }

    @Test
    fun `entity maps to the detail model and keeps nulls`() {
        val detail = detailDto.toEntity(1L).toUserDetail()

        assertEquals("San Francisco", detail.location)
        assertEquals(9, detail.following)
        assertNull(detail.bio)
        assertNull(detail.email)
    }

    @Test
    fun `entity without fetched detail maps to a detail with null counts`() {
        val entity = UserEntity(1, "octocat", "https://avatar", "https://github.com/octocat", "User")

        val detail = entity.toUserDetail()

        assertNull(detail.followers)
        assertNull(detail.createdAt)
    }
}
