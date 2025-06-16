package diploma.pr.biovote.data.repository

import com.google.gson.Gson
import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.data.remote.model.responses.Poll
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class PollRepository @Inject constructor() {

    /** 1️⃣ Fetch all polls **/
    suspend fun polls(token: String): Result<List<Poll>> = runCatching {
        val resp = ApiClient.service.getAllPolls("Bearer $token")
        if (!resp.isSuccessful) throw HttpException(resp)
        resp.body()?.polls.orEmpty()
    }

    /** 2️⃣ Fetch single-poll detail **/
    suspend fun pollDetail(token: String, pollId: Long): Result<Poll> = runCatching {
        val resp = ApiClient.service.getPoll(pollId, "Bearer $token")
        if (!resp.isSuccessful) throw HttpException(resp)
        val wrapper = resp.body() ?: error("Empty body")
        wrapper.polls.firstOrNull() ?: error("Poll #$pollId not found")
    }

    /** 3️⃣ Plain vote (no proof) **/
    suspend fun submitVote(token: String, req: VoteRequest): Result<Unit> = runCatching {
        val resp = ApiClient.service.submitVote("Bearer $token", req)
        if (!resp.isSuccessful) throw HttpException(resp)
    }

    /** 4️⃣ Vote + face-proof multipart upload **/
    suspend fun submitVoteWithProof(
        token: String,
        pollId: Long,
        answerId: Long,
        imageFile: File
    ): Result<Unit> = runCatching {
        // text/plain for simple parts
        val textType = "text/plain".toMediaType()
        val jsonType = "application/json".toMediaType()

        val pollPart = pollId
            .toString()
            .toRequestBody(textType)

        val answersJson = Gson()
            .toJson(listOf(answerId))

        val answersPart = answersJson
            .toRequestBody(jsonType)

        // JPEG image
        val imgReq  = imageFile
            .asRequestBody("image/jpeg".toMediaType())

        val imgPart = MultipartBody.Part.createFormData(
            name     = "faceImage",
            filename = imageFile.name,
            body     = imgReq
        )

        val resp = ApiClient.service.submitVoteWithProof(
            "Bearer $token",
            pollPart,
            answersPart,
            imgPart
        )
        if (!resp.isSuccessful) throw HttpException(resp)
    }
}