package diploma.pr.biovote.ui.auth

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.data.remote.model.responses.Poll
import diploma.pr.biovote.data.repository.PollRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PollDetailViewModel @Inject constructor(
    private val repo: PollRepository,
    private val tokenMgr: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pollId: Long = savedStateHandle["pollId"] ?: -1L

    private val _poll = MutableStateFlow<Poll?>(null)
    val poll: StateFlow<Poll?> = _poll

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _voteHash = MutableStateFlow<String?>(null)
    val voteHash: StateFlow<String?> = _voteHash

    init {
        loadPoll()
    }

    private fun loadPoll() {
        viewModelScope.launch {
            Log.d("PollDetailVM", "Loading poll #$pollId…")
            _isLoading.value = true
            _error.value = null

            val token = tokenMgr.getToken().orEmpty()
            repo.pollDetail(token, pollId)
                .onSuccess { p ->
                    _poll.value = p
                    Log.d("PollDetailVM", "Loaded poll: ${p.name}")
                }
                .onFailure { ex ->
                    _error.value = ex.localizedMessage
                    Log.e("PollDetailVM", "Error loading poll", ex)
                }

            _isLoading.value = false
        }
    }

    fun submitVote(answerIds: List<Long>) {
        if (answerIds.isEmpty()) return
        viewModelScope.launch {
            val token = tokenMgr.getToken().orEmpty()
            repo.submitVote(token, VoteRequest(pollId, answerIds))
                .onSuccess {
                    _voteHash.value = UUID.randomUUID().toString()
                }
                .onFailure { ex ->
                    _error.value = ex.localizedMessage
                }
        }
    }
}