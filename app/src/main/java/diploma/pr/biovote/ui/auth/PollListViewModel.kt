package diploma.pr.biovote.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.responses.Poll
import diploma.pr.biovote.data.repository.PollRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PollListViewModel @Inject constructor(
    private val repo: PollRepository,
    private val tokenMgr: TokenManager
) : ViewModel(){
    private val _polls = MutableStateFlow<List<Poll>>(emptyList())
    val polls: StateFlow<List<Poll>> = _polls

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadPolls()
    }

    private fun loadPolls() {
        viewModelScope.launch {
            Log.d("PollListVM", "Loading polls…")
            _isLoading.value = true
            _error.value = null

            val token = tokenMgr.getToken().orEmpty()
            repo.polls(token)
                .onSuccess { list ->
                    _polls.value = list
                    Log.d("PollListVM", "Loaded ${list.size} polls")
                }
                .onFailure { ex ->
                    _error.value = ex.localizedMessage
                    Log.e("PollListVM", "Error loading polls", ex)
                }

            _isLoading.value = false
        }
    }
}