package com.tisto.githubusers.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tisto.githubusers.domain.model.DataError
import com.tisto.githubusers.domain.model.UserDetail
import com.tisto.githubusers.domain.usecase.GetUserDetail
import com.tisto.githubusers.domain.usecase.MarkUserViewed
import com.tisto.githubusers.domain.usecase.RefreshUserDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserDetailUiState(
    val user: UserDetail? = null,
    val isRefreshing: Boolean = false,
    // One-shot: cleared by onErrorShown().
    val error: DataError? = null,
    // The account is gone on GitHub; sticky until a refresh succeeds.
    val notFound: Boolean = false,
)

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getUserDetail: GetUserDetail,
    private val refreshUserDetail: RefreshUserDetail,
    private val markUserViewed: MarkUserViewed,
) : ViewModel() {

    private class Status(
        val isRefreshing: Boolean = false,
        val error: DataError? = null,
        val notFound: Boolean = false,
    )

    private val login: String = checkNotNull(savedStateHandle["login"])
    private val status = MutableStateFlow(Status())

    val uiState: LiveData<UserDetailUiState> = combine(getUserDetail(login), status) { user, s ->
        UserDetailUiState(user, s.isRefreshing, s.error, s.notFound)
    }.asLiveData()

    init {
        viewModelScope.launch {
            markUserViewed(login)
            fetch()
        }
    }

    fun refresh() {
        viewModelScope.launch { fetch() }
    }

    fun onErrorShown() {
        status.update { Status(it.isRefreshing, error = null, notFound = it.notFound) }
    }

    private suspend fun fetch() {
        if (status.value.isRefreshing) return
        status.update { Status(isRefreshing = true, notFound = it.notFound) }
        val failure = refreshUserDetail(login).exceptionOrNull()
        val error = failure?.let { it as? DataError ?: DataError.Unknown(it) }
        status.value = when (error) {
            null -> Status()
            is DataError.NotFound -> Status(notFound = true)
            // A transient failure keeps whatever state we had: cached data stays on screen.
            else -> Status(error = error, notFound = status.value.notFound)
        }
    }
}
