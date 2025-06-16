// app/src/main/java/diploma/pr/biovote/data/repository/PollRepository.kt
package diploma.pr.biovote.data.repository

import diploma.pr.biovote.data.remote.model.ApiService
import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.data.remote.model.responses.Answer
import diploma.pr.biovote.data.remote.model.responses.Poll
import diploma.pr.biovote.data.remote.model.responses.Question
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollRepository @Inject constructor(
    private val api: ApiService
) {
    private val dummy = listOf(
        Poll(
            id          = 1L,
            name        = "Diploma Master Election",
            description = "Choose your Diploma Master for 2025/2026",
            endDate     = "2025-12-31",
            voted       = false,
            voteCount   = 0,
            questions   = listOf(
                Question(
                    id      = 1L,
                    text    = "Who should be the next Diploma Master?",
                    answers = listOf(
                        Answer(id = 10L, text = "Alice"),
                        Answer(id = 20L, text = "Bob"),
                        Answer(id = 30L, text = "Charlie"),
                        Answer(id = 40L, text = "Diana")
                    )
                )
            )
        )
    )

    suspend fun getAllPolls(token: String): Result<List<Poll>> = runCatching {
        val r = api.getAllPolls("Bearer $token")
        val list = r.body()?.polls
        if (r.isSuccessful && !list.isNullOrEmpty()) list else dummy
    }

    suspend fun getPollDetail(token: String, pollId: Long): Result<Poll> = runCatching {
        val r = api.getPoll(pollId, "Bearer $token")
        val p = r.body()?.polls?.firstOrNull()
        if (r.isSuccessful && p != null) p else dummy.first { it.id == pollId }
    }

    suspend fun submitVoteWithProof(
        token: String,
        pollId: Long,
        answerId: Long,
        photo: File
    ): Result<Unit> = runCatching {
        val proof = photo.toProofPart()
        val req   = VoteRequest(pollId = pollId, answerIds = listOf(answerId))
        api.submitVoteWithProof("Bearer $token", pollId, req, proof)
    }

    private fun File.toProofPart(): MultipartBody.Part {
        val mt   = "image/jpeg".toMediaType()
        val body = this.asRequestBody(mt)
        return MultipartBody.Part.createFormData(
            name     = "proof",
            filename = this.name,
            body     = body
        )
    }
}