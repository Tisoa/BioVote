// app/src/main/java/diploma/pr/biovote/data/repository/AuthRepository.kt
package diploma.pr.biovote.data.repository

import diploma.pr.biovote.data.remote.model.ApiService
import diploma.pr.biovote.data.remote.model.AuthResponse
import diploma.pr.biovote.data.remote.model.RegisterResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun register(
        email: String,
        fullName: String,
        face: MultipartBody.Part
    ): Response<RegisterResponse> {
        val emailB = email.toRequestBody("text/plain".toMediaType())
        val nameB  = fullName.toRequestBody("text/plain".toMediaType())
        return api.registerUser(emailB, nameB, face)
    }

    suspend fun loginByFace(
        email: String,
        face: MultipartBody.Part
    ): Response<AuthResponse> {
        val emailB = email.toRequestBody("text/plain".toMediaType())
        return api.loginUserByFace(emailB, face)
    }
}