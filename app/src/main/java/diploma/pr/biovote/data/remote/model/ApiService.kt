package diploma.pr.biovote.data.remote.model

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("/auth/register")
    suspend fun registerUser(
        @Part("username") email: RequestBody,
        @Part("fullName") fullName: RequestBody,
        @Part faceImage: MultipartBody.Part
    ): Response<AuthResponse>

    @Multipart
    @POST("/auth/face_login")
    suspend fun loginUserByFace(
        @Part("username") email: RequestBody,
        @Part faceImage: MultipartBody.Part
    ): Response<AuthResponse>

    @POST("/polls/vote")
    suspend fun submitVote(
        @Header("Authorization") token: String,
        @Body voteRequest: VoteRequest
    ): Response<Unit>

    @GET("/polls")
    suspend fun getPolls(
        @Header("Authorization") token: String
    ): Response<List<PollResponse>>

    data class PollResponse(
        val id: Int,
        val title: String,
        val description: String,
        val endDate: String
    )
}
