package diploma.pr.biovote.data.remote.model.requests

data class VoteRequest(
    val pollId: Long,
    val answerIds: List<Long>
)