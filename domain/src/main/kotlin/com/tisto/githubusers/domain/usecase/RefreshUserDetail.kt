package com.tisto.githubusers.domain.usecase

import com.tisto.githubusers.domain.repository.UserRepository
import javax.inject.Inject

class RefreshUserDetail @Inject constructor(private val repository: UserRepository) {
    suspend operator fun invoke(login: String): Result<Unit> = repository.refreshUser(login)
}
