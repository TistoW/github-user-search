package com.tisto.githubusers.domain.usecase

import com.tisto.githubusers.domain.repository.UserRepository
import javax.inject.Inject

class MarkUserViewed @Inject constructor(private val repository: UserRepository) {
    suspend operator fun invoke(login: String) = repository.markViewed(login)
}
