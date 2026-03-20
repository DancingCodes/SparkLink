package love.moonc.sparklink.data.remote

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import love.moonc.sparklink.data.events.AppEvent
import love.moonc.sparklink.data.events.AppEventBus
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.exception.ApiException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {
    private const val BASE_URL = "https://sparklink.moonc.love/"
    lateinit var Api: ApiService

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // 注意：init 现在需要传入 Context
    fun init(context: Context) {
        val userPrefs = UserPreferences.getInstance()

        val client = OkHttpClient.Builder()
            // 1. Chucker 拦截器 (放在最前面，确保能抓到所有修改后的 Header)
            .addInterceptor(ChuckerInterceptor.Builder(context).build())

            // 2. 日志拦截器
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })

            // 3. Token 拦截器
            .addInterceptor { chain ->
                val token = runBlocking { userPrefs.token.first() }
                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }.build()
                chain.proceed(request)
            }

            // 4. 错误处理拦截器
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.isSuccessful) return@addInterceptor response

                when (response.code) {
                    401 -> {
                        runBlocking { AppEventBus.emit(AppEvent.Logout) }
                        throw ApiException(401, "登录已失效")
                    }
                    500 -> {
                        val jsonStr = response.peekBody(Long.MAX_VALUE).string()
                        val msg = try { JSONObject(jsonStr).getString("msg") } catch (_: Exception) { "服务器错误" }
                        throw ApiException(500, msg)
                    }
                    else -> throw ApiException(response.code, "网络异常: ${response.code}")
                }
            }.build()

        Api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(jsonConfig.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}