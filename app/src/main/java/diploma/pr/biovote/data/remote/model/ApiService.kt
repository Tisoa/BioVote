package diploma.pr.biovote.data.remote.model


import diploma.pr.biovote.data.remote.model.requests.PollRequest
import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.data.remote.model.AuthResponse
import diploma.pr.biovote.data.remote.model.responses.Poll
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /* ----------  А в т е н т и ф і к а ц і я  ---------- */

    @Multipart
    @POST("auth/register")
    suspend fun registerUser(
        @Part("username") username: RequestBody,
        @Part("fullName") fullName: RequestBody,
        @Part faceImage: MultipartBody.Part
    ): Response<AuthResponse>

    @Multipart
    @POST("auth/face_login")
    suspend fun loginUserByFace(
        @Part("username") username: RequestBody,
        @Part faceImage: MultipartBody.Part
    ): Response<AuthResponse>

    /* ----------  Г о л о с у в а н н я  ---------- */

    @GET("polls")
    suspend fun getPolls(@Header("Authorization") bearer: String): Response<List<Poll>>

    @GET("polls/{id}")
    suspend fun getPoll(
        @Path("id") pollId: Long,
        @Header("Authorization") bearer: String
    ): Response<Poll>

    @POST("polls")
    suspend fun createPoll(
        @Header("Authorization") bearer: String,
        @Body payload: PollRequest
    ): Response<Poll>

    @POST("polls/vote")
    suspend fun submitVote(
        @Header("Authorization") bearer: String,
        @Body payload: VoteRequest
    ): Response<Void>

    @DELETE("polls/vote/{id}")
    suspend fun deleteVote(
        @Header("Authorization") bearer: String,
        @Path("id") voteId: Long
    ): Response<Void>

    @DELETE("polls/{id}")
    suspend fun deletePoll(
        @Header("Authorization") bearer: String,
        @Path("id") pollId: Long
    ): Response<Void>
}