// app/src/main/java/diploma/pr/biovote/data/remote/model/responses/Poll.kt
package diploma.pr.biovote.data.remote.model.responses

data class Poll(
    val id: Long,
    val name: String,
    val description: String,
    val endDate: String,
    val voted: Boolean,
    val voteCount: Int,
    val questions: List<Question> = emptyList()
) {
    companion object
}