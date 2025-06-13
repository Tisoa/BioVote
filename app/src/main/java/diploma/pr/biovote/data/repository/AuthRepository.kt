
package diploma.pr.biovote.data.repository

import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.AuthResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AuthRepository {

    private val api = ApiClient.service

    suspend fun register(
        email: String,
        fullName: String,
        face: MultipartBody.Part
    ): Response<AuthResponse> {
        val emailBody    = email.toRequestBody("text/plain".toMediaType())
        val fullNameBody = fullName.toRequestBody("text/plain".toMediaType())
        return api.registerUser(emailBody, fullNameBody, face)
    }

    suspend fun login(
        email: String,
        face: MultipartBody.Part
    ): Response<AuthResponse> {
        val emailBody = email.toRequestBody("text/plain".toMediaType())
        return api.loginUserByFace(emailBody, face)
    }
}