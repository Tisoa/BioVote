// app/src/main/java/diploma/pr/biovote/ui/voting/PollListViewModel.kt
package diploma.pr.biovote.ui.voting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.responses.Poll
import diploma.pr.biovote.data.repository.PollRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PollListViewModel @Inject constructor(
    private val repo: PollRepository,
    private val tokenMgr: TokenManager
) : ViewModel() {
    private val _polls     = MutableStateFlow<List<Poll>>(emptyList())
    val polls: StateFlow<List<Poll>> = _polls

    private val _loading   = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _loading

    private val _error     = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        viewModelScope.launch {
            _loading.value = true
            repo.getAllPolls("Bearer ${tokenMgr.getToken().orEmpty()}")
                .onSuccess { _polls.value = it }
                .onFailure { _error.value = it.localizedMessage }
            _loading.value = false
        }
    }
}