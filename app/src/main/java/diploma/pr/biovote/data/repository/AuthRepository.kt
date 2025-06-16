package diploma.pr.biovote.data.repository

import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.AuthResponse
import diploma.pr.biovote.data.remote.model.RegisterResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {
    private val api = ApiClient.service

    /**
     * Реєстрація нового користувача або повернення помилки
     */
    suspend fun register(
        email: String,
        fullName: String,
        face: MultipartBody.Part
    ): Response<RegisterResponse> {
        val eBody = email.toRequestBody("text/plain".toMediaType())
        val nBody = fullName.toRequestBody("text/plain".toMediaType())
        return api.registerUser(eBody, nBody, face)
    }

    /**
     * Логін за Face ID
     */
    suspend fun login(
        email: String,
        face: MultipartBody.Part
    ): Response<AuthResponse> {
        val eBody = email.toRequestBody("text/plain".toMediaType())
        return api.loginUserByFace(eBody, face)
    }
}