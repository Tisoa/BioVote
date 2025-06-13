package diploma.pr.biovote.data.repository

import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.responses.Poll
import diploma.pr.biovote.data.remote.model.responses.PollsResponse
import retrofit2.HttpException
import java.io.IOException

class PollRepository(
    private val api: diploma.pr.biovote.data.remote.model.ApiService = ApiClient.service
) {
    suspend fun polls(token: String): Result<List<Poll>> = runCatching {
        val r = api.getPolls("Bearer $token")
        if (!r.isSuccessful) throw HttpException(r)
        (r.body() ?: PollsResponse(false,"", emptyList())).polls
    }.recoverCatching {
        when (it) {
            is IOException  -> error("Немає мережі")
            is HttpException-> error("HTTP ${it.code()}")
            else            -> throw it
        }
    }
}