package diploma.pr.biovote.data.remote.model.requests

data class PollRequest(
    val title: String,
    val description: String,
    val options: List<String>
)