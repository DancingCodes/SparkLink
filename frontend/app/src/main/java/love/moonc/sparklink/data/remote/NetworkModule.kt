package love.moonc.sparklink.data.remote

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
    // 10.0.2.2 是 Android 模拟器访问电脑宿主机本地服务的专用 IP
    // private const val BASE_URL = "http://10.0.2.2:10004/"
    private const val BASE_URL = "https://sparklink.moonc.love/"


    lateinit var Api: ApiService

    fun init() {
        // 直接获取已初始化的单例，保持全局唯一
        val userPrefs = UserPreferences.getInstance()

        val client = OkHttpClient.Builder()
            // 日志拦截器：开发阶段查看请求参数和返回结果非常有用
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            // 自动注入 Token 拦截器
            .addInterceptor { chain ->
                // 使用 runBlocking 是因为拦截器内部是同步执行的，而 DataStore 是异步的
                val token = runBlocking { userPrefs.token.first() }
                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }.build()
                chain.proceed(request)
            }
            // 统一错误处理拦截器
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())

                // 1. 如果是 200，直接放行
                if (response.isSuccessful) return@addInterceptor response

                // 2. 处理特定的错误码
                when (response.code) {
                    401 -> {
                        // 发送全局退出事件，MainActivity 会监听到并跳转登录页
                        runBlocking { AppEventBus.emit(AppEvent.Logout) }
                        throw ApiException(401, "登录已失效，请重新登录")
                    }
                    500 -> {
                        // 尝试解析后端返回的错误 JSON 消息
                        val jsonStr = response.body?.string() ?: ""
                        val msg = try {
                            JSONObject(jsonStr).getString("msg")
                        } catch (_: Exception) {
                            "服务器内部错误"
                        }
                        throw ApiException(500, msg)
                    }
                    else -> {
                        // 其他错误码的处理
                        throw ApiException(response.code, "网络异常: ${response.code}")
                    }
                }
            }.build()

        // 构建 Retrofit 实例
        Api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}