package diploma.pr.biovote.data.remote

import diploma.pr.biovote.data.remote.model.AuthResponse
import diploma.pr.biovote.data.remote.model.PollResponse
import diploma.pr.biovote.data.remote.model.VoteRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ServiceApi {

    @Multipart
    @POST("auth/register")
    suspend fun registerUser(
        @Part("email") email: RequestBody,
        @Part("fullName") fullName: RequestBody,
        @Part faceImage: MultipartBody.Part
    ): Response<AuthResponse>

    @Multipart
    @POST("auth/loginByFace")
    suspend fun loginUserByFace(
        @Part("email") email: RequestBody,
        @Part("faceVector") faceVector: RequestBody
    ): Response<AuthResponse>

    @GET("polls")
    suspend fun getPolls(
        @Header("Authorization") token: String
    ): Response<List<PollResponse>>

    @POST("polls/vote")
    suspend fun submitVote(
        @Header("Authorization") token: String,
        @Body voteRequest: VoteRequest
    ): Response<Unit>
}