package diploma.pr.biovote.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val service: ServiceApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // або свою адресу
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceApi::class.java)
    }
}