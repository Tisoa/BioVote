// PollsResponse.kt  (wrapper для   GET /polls)
package diploma.pr.biovote.data.remote.model.responses

data class PollsResponse(
    val success: Boolean,
    val message: String,
    val polls: List<Poll>
)