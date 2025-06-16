// app/src/main/java/diploma/pr/biovote/data/remote/model/responses/PollsResponse.kt
package diploma.pr.biovote.data.remote.model.responses

data class PollsResponse(
    val success: Boolean,
    val message: String,
    val polls: List<Poll>
)