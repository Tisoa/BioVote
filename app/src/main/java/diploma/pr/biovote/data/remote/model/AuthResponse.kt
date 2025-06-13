package diploma.pr.biovote.data.remote.model

/**
 * Єдина відповідь бекенду після реєстрації / логіну.
 */
data class AuthResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val token: String
)