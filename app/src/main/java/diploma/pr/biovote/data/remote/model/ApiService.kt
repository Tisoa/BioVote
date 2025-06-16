package diploma.pr.biovote.data.remote.model

import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.data.remote.model.AuthResponse
import diploma.pr.biovote.data.remote.model.responses.PollsResponse
import diploma.pr.biovote.data.remote.model.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    /* ---------- AUTH ---------- */
    @Multipart
    @POST("auth/register")
    suspend fun registerUser(
        @Part("username") username: RequestBody,
        @Part("fullName") fullName: RequestBody,
        @Part faceImage: MultipartBody.Part
    ): Response<RegisterResponse>

    @Multipart
    @POST("auth/face_login")
    suspend fun loginUserByFace(
        @Part("username") username: RequestBody,
        @Part faceImage: MultipartBody.Part
    ): Response<AuthResponse>

    /* ---------- POLLS ---------- */
    @GET("polls")
    suspend fun getAllPolls(
        @Header("Authorization") bearer: String
    ): Response<PollsResponse>

    @GET("polls/{id}")
    suspend fun getPoll(
        @Path("id") pollId: Long,
        @Header("Authorization") bearer: String
    ): Response<PollsResponse>

    @POST("polls/vote")
    suspend fun submitVote(
        @Header("Authorization") bearer: String,
        @Body payload: VoteRequest
    ): Response<Void>
}