package diploma.pr.biovote.data.repository

import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.data.remote.model.responses.Poll
import retrofit2.HttpException
import javax.inject.Inject

class PollRepository @Inject constructor() {
    suspend fun polls(token: String): Result<List<Poll>> = runCatching {
        val resp = ApiClient.service.getAllPolls("Bearer $token")
        if (resp.isSuccessful) {
            resp.body()?.polls.orEmpty()
        } else {
            throw HttpException(resp)
        }
    }

    suspend fun pollDetail(token: String, pollId: Long): Result<Poll> = runCatching {
        val resp = ApiClient.service.getPoll(pollId, "Bearer $token")
        if (!resp.isSuccessful) throw HttpException(resp)
        val wrapper = resp.body() ?: error("Empty body")
        wrapper.polls.firstOrNull() ?: error("Poll #$pollId not found")
    }

    suspend fun submitVote(token: String, req: VoteRequest): Result<Unit> = runCatching {
        val resp = ApiClient.service.submitVote("Bearer $token", req)
        if (!resp.isSuccessful) throw HttpException(resp)
    }
}