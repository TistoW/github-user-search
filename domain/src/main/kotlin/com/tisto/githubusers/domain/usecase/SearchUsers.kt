package com.tisto.githubusers.domain.usecase

import com.tisto.githubusers.domain.repository.UserRepository
import javax.inject.Inject

class SearchUsers @Inject constructor(private val repository: UserRepository) {
    suspend operator fun invoke(query: String): Result<Unit> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return Result.success(Unit)
        return repository.searchUsers(trimmed)
    }
}
