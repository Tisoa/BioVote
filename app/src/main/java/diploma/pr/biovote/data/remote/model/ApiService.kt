// app/src/main/java/diploma/pr/biovote/data/remote/model/ApiService.kt
package diploma.pr.biovote.data.remote.model

import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.data.remote.model.responses.PollsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @Multipart
    @POST("auth/register")
    suspend fun registerUser(
        @Part("username")   username: RequestBody,
        @Part("fullName")   fullName:  RequestBody,
        @Part               face:      MultipartBody.Part
    ): Response<RegisterResponse>

    @Multipart
    @POST("auth/face_login")
    suspend fun loginUserByFace(
        @Part("username") username: RequestBody,
        @Part             face:     MultipartBody.Part
    ): Response<AuthResponse>

    @GET("polls")
    suspend fun getAllPolls(
        @Header("Authorization") auth: String
    ): Response<PollsResponse>

    @GET("polls/{id}")
    suspend fun getPoll(
        @Path("id")           pollId: Long,
        @Header("Authorization") auth: String
    ): Response<PollsResponse>

    @Multipart
    @POST("polls/{id}/vote")
    suspend fun submitVoteWithProof(
        @Header("Authorization") auth: String,
        @Path("id") pollId: Long,
        @Part("vote") vote: VoteRequest,
        @Part proof: MultipartBody.Part
    ): Response<Unit>

    @POST("polls/vote")
    suspend fun submitVote(
        @Header("Authorization") auth: String,
        @Body                    req: VoteRequest
    ): Response<Void>
}