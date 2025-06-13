package diploma.pr.biovote.data.remote.model

/**
 * Відповідь /auth/register
 * (у беку =  {"success":true|false,"message":"…"} )
 */
data class RegisterResponse(
    val success: Boolean,
    val message: String
)