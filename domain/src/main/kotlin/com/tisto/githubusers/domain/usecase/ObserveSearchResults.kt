package com.tisto.githubusers.domain.usecase

import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveSearchResults @Inject constructor(private val repository: UserRepository) {
    operator fun invoke(query: String): Flow<List<User>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) flowOf(emptyList()) else repository.observeSearchResults(trimmed)
    }
}
