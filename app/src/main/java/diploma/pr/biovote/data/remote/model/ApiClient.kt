// app/src/main/java/diploma/pr/biovote/data/remote/model/ApiClient.kt
package diploma.pr.biovote.data.remote.model

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // change to 10.0.2.2:8081 if you run the backend on the host and use an Android emulator
    private const val BASE_URL = "http://192.168.0.151:8081/"

    private fun httpLogger() = HttpLoggingInterceptor { msg ->
        if (msg.startsWith("--") || msg.startsWith("��")) return@HttpLoggingInterceptor
        Log.d("HTTP", msg)
    }.apply { level = HttpLoggingInterceptor.Level.BODY }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(httpLogger())
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
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