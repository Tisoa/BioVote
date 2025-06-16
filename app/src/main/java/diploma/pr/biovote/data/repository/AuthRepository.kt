// app/src/main/java/diploma/pr/biovote/data/repository/AuthRepository.kt
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

    suspend fun register(
        email:    String,
        fullName: String,
        face:     MultipartBody.Part
    ): Response<RegisterResponse> {
        val userBody = email   .toRequestBody("text/plain".toMediaType())
        val nameBody = fullName.toRequestBody("text/plain".toMediaType())
        return api.registerUser(userBody, nameBody, face)
    }

    suspend fun login(
        email: String,
        face:  MultipartBody.Part
    ): Response<AuthResponse> {
        val userBody = email.toRequestBody("text/plain".toMediaType())
        return api.loginUserByFace(userBody, face)
    }
}