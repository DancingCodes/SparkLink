package love.moonc.sparklink.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import love.moonc.sparklink.data.remote.model.entity.User

// 定义 DataStore 扩展属性
private val Context.dataStore by preferencesDataStore(name = "user_settings")

class UserPreferences private constructor(context: Context) {

    private val appContext = context.applicationContext

    // 配置 Json 解析器，与 NetworkModule 保持一致，增加容错性
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_KEY = stringPreferencesKey("user_info")

        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun init(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context).also { INSTANCE = it }
            }
        }

        fun getInstance(): UserPreferences {
            return INSTANCE ?: throw IllegalStateException(
                "UserPreferences 尚未初始化，请先在 Application 中调用 init(context)"
            )
        }
    }

    // 获取 Token 流
    val token: Flow<String?> = appContext.dataStore.data.map { it[TOKEN_KEY] }

    // 获取用户信息流：将 JSON 字符串反序列化为 User 对象
    val userData: Flow<User?> = appContext.dataStore.data.map { preferences ->
        val jsonStr = preferences[USER_KEY]
        if (jsonStr.isNullOrEmpty()) {
            null
        } else {
            try {
                // ✅ 使用 Kotlinx Serialization 替代 gson.fromJson
                json.decodeFromString<User>(jsonStr)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // 保存 Token
    suspend fun saveToken(token: String) {
        appContext.dataStore.edit { it[TOKEN_KEY] = token }
    }

    // 保存用户信息：将 User 对象序列化为 JSON 字符串
    suspend fun saveUser(user: User) {
        // ✅ 使用 Json.encodeToString 替代 gson.toJson
        val jsonStr = json.encodeToString(user)
        appContext.dataStore.edit { it[USER_KEY] = jsonStr }
    }

    // 清除所有数据（退出登录时使用）
    suspend fun clear() {
        appContext.dataStore.edit { it.clear() }
    }
}