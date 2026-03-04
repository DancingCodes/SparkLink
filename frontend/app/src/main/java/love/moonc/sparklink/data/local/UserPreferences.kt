package love.moonc.sparklink.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import love.moonc.sparklink.data.remote.model.User

private val Context.dataStore by preferencesDataStore(name = "user_settings")
private val gson = Gson()

class UserPreferences(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_KEY = stringPreferencesKey("user_info")
    }

    val Token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val UserData: Flow<User?> = context.dataStore.data.map { preferences ->
        val json = preferences[USER_KEY]
        if (json.isNullOrEmpty()) null else gson.fromJson(json, User::class.java)
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }
    suspend fun saveUser(user: User) {
        val json = gson.toJson(user)
        context.dataStore.edit { it[USER_KEY] = json }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}