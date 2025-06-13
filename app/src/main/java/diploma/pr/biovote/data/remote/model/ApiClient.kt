package diploma.pr.biovote.data.remote.model

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Вкажіть правильну адресу вашого серверу
    private const val BASE_URL = "http://192.168.31.190:8081/"

    private fun httpLogger() = HttpLoggingInterceptor { msg ->
        if (msg.startsWith("--") || msg.startsWith("��")) return@HttpLoggingInterceptor
        Log.d("HTTP", msg)
    }.apply { level = HttpLoggingInterceptor.Level.BODY }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(httpLogger())
        .build()

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}