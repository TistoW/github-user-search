package com.tisto.githubusers.data.di

import android.content.Context
import androidx.room.Room
import com.tisto.githubusers.data.local.GitHubDatabase
import com.tisto.githubusers.data.local.SearchResultDao
import com.tisto.githubusers.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GitHubDatabase =
        Room.databaseBuilder(context, GitHubDatabase::class.java, GitHubDatabase.NAME).build()

    @Provides
    fun provideUserDao(database: GitHubDatabase): UserDao = database.userDao()

    @Provides
    fun provideSearchResultDao(database: GitHubDatabase): SearchResultDao = database.searchResultDao()
}
