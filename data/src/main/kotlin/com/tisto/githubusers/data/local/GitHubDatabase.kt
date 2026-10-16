package com.tisto.githubusers.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, SearchResultEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class GitHubDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun searchResultDao(): SearchResultDao

    companion object {
        const val NAME = "github_users.db"
    }
}
