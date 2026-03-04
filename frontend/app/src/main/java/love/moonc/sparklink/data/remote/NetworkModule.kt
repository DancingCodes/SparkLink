package love.moonc.sparklink.data.remote

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import love.moonc.sparklink.data.events.AppEvent
import love.moonc.sparklink.data.events.AppEventBus
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.exception.ApiException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:10004/"

    lateinit var Api: ApiService

    fun init(context: Context) {
        val userPrefs = UserPreferences(context.applicationContext)

        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor { chain ->
                val token = runBlocking { userPrefs.Token.first() }
                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) addHeader("Authorization", "Bearer $token")
                }.build()
                chain.proceed(request)
            }
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.code == 200) return@addInterceptor response
                if (response.code == 401) {
                    runBlocking { AppEventBus.emit(AppEvent.Logout) }
                    throw ApiException(401, "登录已失效")
                }
                if (response.code == 500) {
                    val jsonStr = response.body?.string() ?: ""
                    val msg = JSONObject(jsonStr).getString("msg")
                    throw ApiException(500, msg)
                }
                response
            }.build()

        Api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}