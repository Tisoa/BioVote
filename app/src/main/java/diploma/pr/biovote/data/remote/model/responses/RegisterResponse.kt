package diploma.pr.biovote.data.remote.model.responses

data class RegisterResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val token: String
)