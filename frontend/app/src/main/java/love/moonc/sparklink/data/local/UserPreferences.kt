// 文件路径: love/moonc/sparklink/data/local/UserPreferences.kt

package love.moonc.sparklink.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import love.moonc.sparklink.data.remote.model.entity.User

private val Context.dataStore by preferencesDataStore(name = "user_settings")
private val gson = Gson()

class UserPreferences private constructor(context: Context) {

    private val appContext = context.applicationContext

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

    // 使用 appContext 调用 dataStore
    val token: Flow<String?> = appContext.dataStore.data.map { it[TOKEN_KEY] }

    val userData: Flow<User?> = appContext.dataStore.data.map { preferences ->
        val json = preferences[USER_KEY]
        if (json.isNullOrEmpty()) null else gson.fromJson(json, User::class.java)
    }

    suspend fun saveToken(token: String) {
        appContext.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun saveUser(user: User) {
        val json = gson.toJson(user)
        appContext.dataStore.edit { it[USER_KEY] = json }
    }

    suspend fun clear() {
        appContext.dataStore.edit { it.clear() }
    }
}