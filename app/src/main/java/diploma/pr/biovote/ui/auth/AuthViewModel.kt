package diploma.pr.biovote.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import diploma.pr.biovote.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val data: diploma.pr.biovote.data.remote.model.RegisterResponse) : RegistrationState()
    data class Error(val msg: String) : RegistrationState()
}

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()
    private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val state: StateFlow<RegistrationState> = _state

    fun register(username: String, fullName: String, image: MultipartBody.Part) {
        viewModelScope.launch {
            _state.value = RegistrationState.Loading
            try {
                val r = repo.register(username, fullName, image)
                if (r.isSuccessful) {
                    val body = r.body()!!
                    Log.d("AuthVM", "OK → $body")
                    _state.value = RegistrationState.Success(body)
                } else {
                    val err = r.errorBody()?.string().orEmpty()
                    Log.e("AuthVM", "${r.code()} $err")
                    _state.value = RegistrationState.Error("HTTP ${r.code()}: $err")
                }
            } catch (e: Exception) {
                Log.e("AuthVM", "exception", e)
                _state.value = RegistrationState.Error(e.localizedMessage ?: "Unknown")
            }
        }
    }
}