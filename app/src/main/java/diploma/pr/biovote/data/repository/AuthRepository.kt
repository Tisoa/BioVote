package diploma.pr.biovote.data.repository

import android.util.Log
import diploma.pr.biovote.data.remote.model.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepository {
    suspend fun register(
        username: String,
        fullName: String,
        imagePart: MultipartBody.Part
    ) = try {
        val userRb = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val nameRb = fullName.toRequestBody("text/plain".toMediaTypeOrNull())
        val resp = ApiClient.service.registerUser(userRb, nameRb, imagePart)
        Log.d("AuthRepo", "register → $resp")
        resp
    } catch (e: Exception) {
        Log.e("AuthRepo", "exception", e)
        throw e
    }
}