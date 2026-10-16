package com.tisto.githubusers.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
abstract class DaoTestBase {

    protected lateinit var db: GitHubDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GitHubDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    protected fun summary(id: Long, login: String = "user$id") = UserEntity(
        id = id,
        login = login,
        avatarUrl = "https://avatars/$id",
        htmlUrl = "https://github.com/$login",
        type = "User",
    )
}
