package diploma.pr.biovote.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import diploma.pr.biovote.data.remote.model.AuthResponse
import diploma.pr.biovote.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

sealed interface UiState<out T> {
    object Idle                    : UiState<Nothing>
    object Loading                 : UiState<Nothing>
    data class Success<T>(val v:T) : UiState<T>
    data class Error(val msg:String) : UiState<Nothing>
}

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
): ViewModel() {

    private val _state = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val state: StateFlow<UiState<AuthResponse>> = _state

    fun register(mail:RequestBody, name:RequestBody, face:MultipartBody.Part) =
        callSafely { repo.register(mail.toString(), name.toString(), face) }

    /* ---------- helpers ---------- */

    private fun callSafely(block:suspend ()->retrofit2.Response<AuthResponse>) =
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val r = block()
                _state.value = if (r.isSuccessful) UiState.Success(r.body()!!)
                else UiState.Error("HTTP ${r.code()}")
            } catch (e:Exception) {
                _state.value = UiState.Error(e.localizedMessage ?: "exception")
            }
        }
}