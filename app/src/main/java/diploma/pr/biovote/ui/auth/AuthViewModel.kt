// app/src/main/java/diploma/pr/biovote/ui/auth/AuthViewModel.kt
package diploma.pr.biovote.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed interface UiState<out T> {
    object Idle                       : UiState<Nothing>
    object Loading                    : UiState<Nothing>
    data class Success<T>(val data: T): UiState<T>
    data class Error(val message: String): UiState<Nothing>
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val tokenMgr: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state

    /**
     * 1️⃣ Try to register (409 “already exists” is OK)
     * 2️⃣ Then login by face and save JWT
     */
    fun registerAndLogin(
        email: String,
        fullName: String,
        facePart: MultipartBody.Part
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading

            // 1️⃣ Registration
            try {
                val regResp = repo.register(email, fullName, facePart)
                if (!regResp.isSuccessful && regResp.code() != 409) {
                    _state.value = UiState.Error("Registration failed: HTTP ${regResp.code()}")
                    return@launch
                }
            } catch (e: IOException) {
                _state.value = UiState.Error("Network error during registration")
                return@launch
            } catch (e: HttpException) {
                _state.value = UiState.Error("Server error during registration")
                return@launch
            }

            // 2️⃣ Face‐login
            val loginResp = try {
                repo.loginByFace(email, facePart)
            } catch (e: IOException) {
                _state.value = UiState.Error("Network error during login")
                return@launch
            } catch (e: HttpException) {
                _state.value = UiState.Error("Server error during login")
                return@launch
            }

            if (!loginResp.isSuccessful) {
                _state.value = UiState.Error("Login failed: HTTP ${loginResp.code()}")
                return@launch
            }

            val body = loginResp.body()
            if (body == null || !body.success || body.message.isBlank()) {
                _state.value = UiState.Error("Login did not return a valid token")
                return@launch
            }

            // 🎉 Success — save token and notify UI
            tokenMgr.saveToken(body.message)
            _state.value = UiState.Success(Unit)
        }
    }
}