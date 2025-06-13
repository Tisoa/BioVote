// Question.kt
package diploma.pr.biovote.data.remote.model.responses

data class Question(
    val id: Long,
    val text: String,
    val answers: List<Answer>
)