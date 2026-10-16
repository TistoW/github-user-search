package com.tisto.githubusers.data.di

import com.tisto.githubusers.data.local.GitHubDatabase
import com.tisto.githubusers.data.remote.GitHubApi
import com.tisto.githubusers.data.repository.UserRepositoryImpl
import com.tisto.githubusers.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// @Provides rather than @Binds so UserRepositoryImpl keeps its default clock parameter.
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(api: GitHubApi, database: GitHubDatabase): UserRepository =
        UserRepositoryImpl(api, database)
}
