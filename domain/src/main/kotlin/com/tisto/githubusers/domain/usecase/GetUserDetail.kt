package com.tisto.githubusers.domain.usecase

import com.tisto.githubusers.domain.model.UserDetail
import com.tisto.githubusers.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserDetail @Inject constructor(private val repository: UserRepository) {
    operator fun invoke(login: String): Flow<UserDetail?> = repository.observeUser(login)
}
