package diploma.pr.biovote.data.remote.model.responses

data class Poll(
    val id: Long,
    val title: String,
    val description: String,
    val options: List<Option>
)