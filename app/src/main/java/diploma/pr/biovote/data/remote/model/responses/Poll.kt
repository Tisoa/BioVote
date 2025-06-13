// Poll.kt
package diploma.pr.biovote.data.remote.model.responses

data class Poll(
    val id: Long,
    val name: String,                 // <--  EXACTLY як у JSON
    val description: String,
    val endDate: String,
    val questions: List<Question>,
    val voted: Boolean,
    val voteCount: Int
)