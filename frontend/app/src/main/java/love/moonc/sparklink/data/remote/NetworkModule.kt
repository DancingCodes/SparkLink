package love.moonc.sparklink.data.remote

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.ws.RoomSocketManager
import love.moonc.sparklink.data.repository.AppRepository // ✅ 导入仓库类
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {
    private const val BASE_URL = "http://192.168.110.144:10004/"

    fun getWsUrl(path: String): String {
        val wsBase = if (BASE_URL.startsWith("https")) {
            BASE_URL.replace("https://", "wss://")
        } else {
            BASE_URL.replace("http://", "ws://")
        }
        return "${wsBase.removeSuffix("/")}/${path.removePrefix("/")}"
    }

    // WebSocket 管理器保持单例
    private val okHttpClient = OkHttpClient.Builder().build()
    val roomSocketManager = RoomSocketManager(okHttpClient)

    lateinit var api: ApiService
    lateinit var repository: AppRepository

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

        api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(jsonConfig.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)

        repository = AppRepository(api)
    }
}