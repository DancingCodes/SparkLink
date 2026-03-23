package love.moonc.sparklink.data.remote

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import love.moonc.sparklink.data.local.UserPreferences
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:10004/"
    // private const val BASE_URL = "https://sparklink.moonc.love/"
    lateinit var Api: ApiService

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    fun init(context: Context) {
        val userPrefs = UserPreferences.getInstance()

        val client = OkHttpClient.Builder()
            .addInterceptor(ChuckerInterceptor.Builder(context).build())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val token = runBlocking { userPrefs.token.first() }
                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }.build()
                chain.proceed(request)
            }
            .build()

        Api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(jsonConfig.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}