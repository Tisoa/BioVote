package diploma.pr.biovote.data.remote.model

import diploma.pr.biovote.data.remote.model.responses.*
import diploma.pr.biovote.data.remote.model.requests.PollRequest
import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /* ---------- AUTH ---------- */

    @Multipart @POST("auth/register")
    suspend fun registerUser(
        @Part("username")  username : RequestBody,
        @Part("fullName")  fullName : RequestBody,
        @Part              faceImage: MultipartBody.Part
    ): Response<RegisterResponse>

    @Multipart @POST("auth/face_login")
    suspend fun loginUserByFace(
        @Part("username") username : RequestBody,
        @Part              faceImage: MultipartBody.Part
    ): Response<AuthResponse>

    /* ---------- POLLS ---------- */

    @GET("polls")
    suspend fun getPolls(
        @Header("Authorization") bearer: String
    ): Response<PollsResponse>                 // <-- wrapper

    @GET("polls/{id}")
    suspend fun getPoll(
        @Path("id") pollId: Long,
        @Header("Authorization") bearer: String
    ): Response<Poll>                          // <-- один poll без wrapper’а

    @POST("polls/vote")
    suspend fun submitVote(
        @Header("Authorization") bearer: String,
        @Body payload: VoteRequest
    ): Response<Void>
}