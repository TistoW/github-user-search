package com.tisto.githubusers.presentation.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.usecase.ObserveRecentlyViewed
import com.tisto.githubusers.domain.usecase.ObserveSearchResults
import com.tisto.githubusers.domain.usecase.SearchUsers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val observeSearchResults: ObserveSearchResults,
    private val observeRecentlyViewed: ObserveRecentlyViewed,
    private val searchUsers: SearchUsers,
) : ViewModel() {

    private class Search(val query: String, val force: Boolean)

    private class Status(
        val isLoading: Boolean = false,
        val error: DataError? = null,
        val errorHandled: Boolean = false,
    )

    private val query = MutableStateFlow(savedStateHandle.get<String>(KEY_QUERY).orEmpty())
    private val submits = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val status = MutableStateFlow(Status())

    val currentQuery: String get() = query.value

    private val state: StateFlow<UsersUiState> = merge(
        // Clearing the field should not wait for the debounce.
        query.debounce { if (it.isBlank()) 0L else DEBOUNCE_MS }.map { Search(it.trim(), force = false) },
        submits.map { Search(query.value.trim(), force = true) },
    )
        .distinctUntilChanged { old, new -> !new.force && old.query == new.query }
        .flatMapLatest { stateFor(it.query) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialState())

    val uiState: LiveData<UsersUiState> = state.asLiveData()

    fun onQueryChanged(text: String) {
        savedStateHandle[KEY_QUERY] = text
        query.value = text
    }

    // IME search and retry both skip the debounce and search again even for an unchanged query.
    fun submit() {
        submits.tryEmit(Unit)
    }

    fun onErrorShown() {
        status.update { Status(it.isLoading, it.error, errorHandled = true) }
    }

    private fun initialState(): UsersUiState {
        val saved = query.value
        return UsersUiState(
            query = saved.trim(),
            section = if (saved.isBlank()) UsersSection.RecentlyViewed else UsersSection.Results,
        )
    }

    private fun stateFor(query: String): Flow<UsersUiState> =
        if (query.isEmpty()) {
            observeRecentlyViewed()
                .onStart { status.value = Status() }
                .map { UsersUiState(query, UsersSection.RecentlyViewed, it) }
        } else {
            channelFlow {
                // The cached results are shown at once, the network call only refreshes them.
                launch {
                    status.value = Status(isLoading = true)
                    val failure = searchUsers(query).exceptionOrNull()
                    status.value = Status(error = failure?.let { it as? DataError ?: DataError.Unknown(it) })
                }
                combine(observeSearchResults(query), status) { users, s ->
                    UsersUiState(query, UsersSection.Results, users, s.isLoading, s.error, s.errorHandled)
                }.collect { send(it) }
            }
        }

    private companion object {
        const val KEY_QUERY = "query"
        const val DEBOUNCE_MS = 400L
    }
}
