package love.moonc.sparklink.data.remote

import love.moonc.sparklink.data.local.UserPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:10004/"

    private var _apiService: ApiService? = null

    // ✨ 提供一个初始化方法，在 App 启动时或初次使用前调用
    fun getApiService(userPrefs: UserPreferences): ApiService {
        return _apiService ?: synchronized(this) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // ✨ 将 userPrefs 传入拦截器
            val authInterceptor = AuthInterceptor(userPrefs)

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit.create(ApiService::class.java).also { _apiService = it }
        }
    }
}