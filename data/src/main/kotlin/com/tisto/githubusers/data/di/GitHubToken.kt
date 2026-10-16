package com.tisto.githubusers.data.di

import javax.inject.Qualifier

// Each app module binds this from its own BuildConfig; an empty string means unauthenticated.
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GitHubToken
