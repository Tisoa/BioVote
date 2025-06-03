package diploma.pr.biovote.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

object ApiClient {
    val service: ServiceApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // або свою адресу
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceApi::class.java)
    }

    @Multipart
    @POST("auth/face_login")
    suspend fun loginUserByFace(
        @Part("username") email: RequestBody,
        @Part faceImage: MultipartBody.Part
    ) {

    }}