package com.tisto.githubusers.compose.di

import com.tisto.githubusers.data.di.RepositoryModule
import com.tisto.githubusers.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
abstract class FakeRepositoryModule {
    @Binds
    abstract fun bindRepository(fake: FakeUserRepository): UserRepository
}
