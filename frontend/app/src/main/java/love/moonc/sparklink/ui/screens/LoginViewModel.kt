package love.moonc.sparklink.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.getOrThrow // 🚀 必带导入
import love.moonc.sparklink.data.remote.model.request.LoginRequest

class LoginViewModel : ViewModel() {
    private val userPrefs = UserPreferences.getInstance()

    var isLoggingIn by mutableStateOf(false)
        private set

    fun login(
        phone: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoggingIn = true
            try {
                val data = NetworkModule.Api.login(LoginRequest(phone, pass)).getOrThrow()
                userPrefs.saveToken(data.token)
                userPrefs.saveUser(data.user)
                onSuccess()
            } catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
            } finally {
                isLoggingIn = false
            }
        }
    }
}