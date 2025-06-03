package diploma.pr.biovote.data.remote.model

data class VoteRequest(
    val pollId: Int,
    val answerIds: List<Int>
)
