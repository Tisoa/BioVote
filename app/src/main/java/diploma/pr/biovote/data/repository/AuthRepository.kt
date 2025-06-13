package diploma.pr.biovote.data.repository

import diploma.pr.biovote.data.remote.model.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AuthRepository {

    private val api = ApiClient.service

    /* ---------- реєстрація ---------- */
    suspend fun register(
        email: String,
        fullName: String,
        face: MultipartBody.Part
    ): Response<RegisterResponse> {                 // 🔥
        val eBody = email.toRequestBody("text/plain".toMediaType())
        val nBody = fullName.toRequestBody("text/plain".toMediaType())
        return api.registerUser(eBody, nBody, face)
    }

    /* ---------- логін ---------- */
    suspend fun login(
        email: String,
        face: MultipartBody.Part
    ): Response<AuthResponse> {
        val eBody = email.toRequestBody("text/plain".toMediaType())
        return api.loginUserByFace(eBody, face)
    }
}