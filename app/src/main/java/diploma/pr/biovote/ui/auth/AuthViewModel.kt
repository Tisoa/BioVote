package diploma.pr.biovote.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.AuthResponse
import diploma.pr.biovote.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

/* ---------- UI-state ---------- */
sealed interface UiState<out T> {
    object Idle                       : UiState<Nothing>
    object Loading                    : UiState<Nothing>
    data class Success<T>(val v: T)   : UiState<T>
    data class Error(val msg: String) : UiState<Nothing>
}

/* ---------- ViewModel ---------- */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val tokenMgr: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state

    /**
     * 1️⃣  /auth/register — створюємо акаунт (success=false ⇒ уже існує, і це OK)
     * 2️⃣  /auth/face_login — отримуємо JWT та зберігаємо
     */
    fun registerAndLogin(
        email: String,
        fullName: String,
        facePart: MultipartBody.Part
    ) = viewModelScope.launch {
        _state.value = UiState.Loading

        /* ---- 1. спроба зареєструвати ---- */
        val regResp = repo.register(email, fullName, facePart)
        if (!regResp.isSuccessful) {
            _state.value = UiState.Error("Registration HTTP ${regResp.code()}")
            return@launch
        }
        // Тіло відповіді може бути success=false, якщо акаунт уже є
        regResp.body() ?: Unit  // регBody не використовується далі

        /* ---- 2. Face-login ---- */
        val logResp = repo.login(email, facePart)
        if (!logResp.isSuccessful) {
            _state.value = UiState.Error("Login HTTP ${logResp.code()}")
            return@launch
        }

        val logBody: AuthResponse? = logResp.body()
        val token = logBody?.takeIf { it.success }?.message.orEmpty()

        if (token.isNotBlank()) {
            tokenMgr.saveToken(token)
            _state.value = UiState.Success(Unit)           // 🎉 успіх
        } else {
            _state.value = UiState.Error("Сервер не надав токен")
        }
    }
}