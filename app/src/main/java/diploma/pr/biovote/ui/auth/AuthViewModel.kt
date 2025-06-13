package diploma.pr.biovote.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.AuthResponse        // /auth/face_login
import diploma.pr.biovote.data.remote.model.RegisterResponse    // /auth/register
import diploma.pr.biovote.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

/* ---------- Ui-state ---------- */
sealed interface UiState<out T> {
    object Idle                       : UiState<Nothing>
    object Loading                    : UiState<Nothing>
    data class Success<T>(val v: T)   : UiState<T>
    data class Error(val msg: String) : UiState<Nothing>
}

/* ---------- ViewModel ---------- */
class AuthViewModel(
    private val tokenStore: TokenManager,
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val  state : StateFlow<UiState<Unit>> = _state

    /**
     * 1️⃣  Реєструємо користувача
     * 2️⃣  У будь-якому випадку пробуємо Face-login
     *     (якщо обліковка вже існує, бекенд повертає success =false → це не помилка)
     */
    fun registerAndLogin(
        email: String,
        fullName: String,
        facePart: MultipartBody.Part
    ) = viewModelScope.launch {
        _state.value = UiState.Loading

        /* ---- 1. спроба /auth/register ---- */
        val regResp = repo.register(email, fullName, facePart)
        if (!regResp.isSuccessful) {
            _state.value = UiState.Error("Registration HTTP ${regResp.code()}")
            return@launch
        }

        // нам важливий лише факт наявності акаунта; success==false → вже є
        val regBody: RegisterResponse? = regResp.body()

        /* ---- 2. /auth/face_login ---- */
        val logResp = repo.login(email, facePart)
        if (!logResp.isSuccessful) {
            _state.value = UiState.Error("Login HTTP ${logResp.code()}")
            return@launch
        }

        val logBody: AuthResponse? = logResp.body()
        if (logBody?.success == true && logBody.message.isNotBlank()) {
            saveToken(logBody.message)                     // ⬅️  JWT з поля message
            _state.value = UiState.Success(Unit)           // 🎉  готово
        } else {
            _state.value = UiState.Error("Сервер не надав токен")
        }
    }

    /* ---------- helpers ---------- */
    private fun saveToken(token: String) = tokenStore.saveToken(token)
}