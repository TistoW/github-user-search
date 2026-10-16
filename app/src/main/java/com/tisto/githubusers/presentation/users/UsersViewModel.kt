package com.tisto.githubusers.presentation.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.usecase.ObserveRecentlyViewed
import com.tisto.githubusers.domain.usecase.ObserveSearchResults
import com.tisto.githubusers.domain.usecase.SearchUsers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeSearchResults: ObserveSearchResults,
    observeRecentlyViewed: ObserveRecentlyViewed,
    private val searchUsers: SearchUsers,
) : ViewModel() {

    private val queryState = savedStateHandle.getLiveData(KEY_QUERY, "")
    val query: LiveData<String> = queryState

    // A blank query shows the recently viewed users, anything else the cached results for it.
    val users: LiveData<List<User>> = queryState.switchMap { query ->
        if (query.isBlank()) observeRecentlyViewed().asLiveData() else observeSearchResults(query).asLiveData()
    }

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        if (query == queryState.value) return
        queryState.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch { searchUsers(query) }
    }

    private companion object {
        const val KEY_QUERY = "query"
    }
}
