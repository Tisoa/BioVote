package diploma.pr.biovote.data.remote.model

/**
 * /auth/face_login
 */
data class AuthResponse(
    val success: Boolean,
    /** бекенд кладe сам JWT-token саме сюди */
    val message: String
)