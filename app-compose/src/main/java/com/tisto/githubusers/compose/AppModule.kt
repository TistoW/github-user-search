package com.tisto.githubusers.compose

import com.tisto.githubusers.data.di.GitHubToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @GitHubToken
    fun provideGitHubToken(): String = BuildConfig.GITHUB_TOKEN
}
