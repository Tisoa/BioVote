package diploma.pr.biovote.data.remote.model

import diploma.pr.biovote.data.remote.model.requests.PollRequest
import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.data.remote.model.responses.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("auth/register")
    suspend fun registerUser(
        @Part("username") email: RequestBody,
        @Part("fullName") fullName: RequestBody,
        @Part faceImage: MultipartBody.Part
    ): Response<RegisterResponse>

    @Multipart
    @POST("auth/face_login")
    suspend fun loginUserByFace(
        @Part("username") email: RequestBody,
        @Part faceImage: MultipartBody.Part
    ): Response<LoginResponse>

    @GET("polls")
    suspend fun getPolls(
        @Header("Authorization") token: String
    ): Response<List<Poll>>

    @GET("polls/{id}")
    suspend fun getPoll(
        @Path("id") pollId: Long,
        @Header("Authorization") token: String
    ): Response<Poll>

    @POST("polls")
    suspend fun createPoll(
        @Header("Authorization") token: String,
        @Body pollRequest: PollRequest
    ): Response<Poll>

    @POST("polls/vote")
    suspend fun submitVote(
        @Header("Authorization") token: String,
        @Body voteRequest: VoteRequest
    ): Response<Void>

    @DELETE("polls/vote/{id}")
    suspend fun deleteVote(
        @Header("Authorization") token: String,
        @Path("id") voteId: Long
    ): Response<Void>

    @DELETE("polls/{id}")
    suspend fun deletePoll(
        @Header("Authorization") token: String,
        @Path("id") pollId: Long
    ): Response<Void>
}