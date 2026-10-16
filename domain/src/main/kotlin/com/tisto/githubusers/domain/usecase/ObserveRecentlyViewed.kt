package com.tisto.githubusers.domain.usecase

import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecentlyViewed @Inject constructor(private val repository: UserRepository) {
    operator fun invoke(): Flow<List<User>> = repository.observeRecentlyViewed()
}
