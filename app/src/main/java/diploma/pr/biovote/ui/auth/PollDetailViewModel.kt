package diploma.pr.biovote.ui.auth

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.repository.PollRepository
import diploma.pr.biovote.data.remote.model.responses.Poll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PollDetailViewModel @Inject constructor(
    private val repo: PollRepository,
    private val tokenMgr: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pollId: Long = savedStateHandle["pollId"] ?: error("Missing pollId")

    private val _poll = MutableStateFlow<Poll?>(null)
    val poll: StateFlow<Poll?> = _poll.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _photoFile = MutableStateFlow<File?>(null)
    val photoFile: StateFlow<File?> = _photoFile.asStateFlow()

    init {
        // Load poll immediately
        viewModelScope.launch {
            _isLoading.value = true
            val token = tokenMgr.getToken().orEmpty()
            repo.pollDetail(token, pollId)
                .onSuccess { dto ->
                    _poll.value = dto
                }
                .onFailure { ex ->
                    _error.value = ex.localizedMessage
                }
            _isLoading.value = false
        }
    }

    fun onPhotoReady(file: File) {
        _photoFile.value = file
    }

    fun submitVoteWithProof(answerId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = tokenMgr.getToken().orEmpty()
            val file = _photoFile.value ?: return@launch
            repo.submitVoteWithProof(token, pollId, answerId, file)
                .onSuccess {
                    Log.d("PollDetailVM", "Vote submitted")
                    onDone()
                }
                .onFailure { ex ->
                    _error.value = ex.localizedMessage
                }
            _isLoading.value = false
        }
    }
}