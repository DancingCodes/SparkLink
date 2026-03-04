package love.moonc.sparklink.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import love.moonc.sparklink.data.remote.model.User

// 定义 DataStore 扩展属性
private val Context.dataStore by preferencesDataStore(name = "user_settings")
// 这里的 Gson 可以保持私有，或者放在单例里复用
private val gson = Gson()

class UserPreferences private constructor(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_KEY = stringPreferencesKey("user_info")

        @Volatile
        private var INSTANCE: UserPreferences? = null

        /**
         * 初始化方法：建议在 Application 的 onCreate 中调用
         */
        fun init(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                // 使用 applicationContext 防止内存泄漏
                val instance = UserPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        /**
         * 获取实例：在 ViewModel 或 UI 中直接调用
         */
        fun getInstance(): UserPreferences {
            return INSTANCE ?: throw IllegalStateException(
                "UserPreferences 尚未初始化，请先在 Application 中调用 init(context)"
            )
        }
    }

    // 获取 Token 的 Flow
    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

    // 获取用户数据的 Flow
    val userData: Flow<User?> = context.dataStore.data.map { preferences ->
        val json = preferences[USER_KEY]
        if (json.isNullOrEmpty()) null else gson.fromJson(json, User::class.java)
    }

    // 保存 Token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    // 保存用户信息
    suspend fun saveUser(user: User) {
        val json = gson.toJson(user)
        context.dataStore.edit { it[USER_KEY] = json }
    }

    // 清空数据（登出时使用）
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}