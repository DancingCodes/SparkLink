package love.moonc.sparklink.data.remote

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import love.moonc.sparklink.data.local.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val userPrefs: UserPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // ✨ 获取 Token (这里假设你 isLoggedIn 存的是 true/false，建议以后专门存一个 Token 字符串)
        val token = runBlocking { userPrefs.isLoggedIn.first() }

        val originalRequest = chain.request()

        // 如果是登录或注册请求，不需要加 Token
        if (originalRequest.url.encodedPath.contains("login") ||
            originalRequest.url.encodedPath.contains("register") ||
            originalRequest.url.encodedPath.contains("upload")) {
            return chain.proceed(originalRequest)
        }

        val request = originalRequest.newBuilder().apply {
            // 只有 token 为 true (或有值) 时才注入 Header
            addHeader("Authorization", "Bearer $token")
        }.build()

        return chain.proceed(request)
    }
}