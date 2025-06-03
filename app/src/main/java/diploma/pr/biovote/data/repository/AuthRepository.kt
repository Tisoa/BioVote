package diploma.pr.biovote.data.repository

import diploma.pr.biovote.data.remote.ApiClient
import diploma.pr.biovote.data.remote.model.AuthResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AuthRepository {

    suspend fun registerUser(
        email: RequestBody,
        fullName: RequestBody,
        faceImage: MultipartBody.Part
    ): Result<AuthResponse> = try {
        val response = ApiClient.service.registerUser(email, fullName, faceImage)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Registration failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun loginUserByFace(
        email: RequestBody,
        faceImage: MultipartBody.Part
    ): Result<AuthResponse> = try {
        val response = ApiClient.service.loginUserByFace(email, faceImage)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Login failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}