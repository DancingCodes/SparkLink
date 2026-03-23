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
import love.moonc.sparklink.data.remote.model.request.LoginRequest

class LoginViewModel : ViewModel() {
    private val userPrefs = UserPreferences.getInstance()

    var isLoggingIn by mutableStateOf(false)
        private set

    // 添加错误状态，方便界面显示具体的登录失败原因
    var errorMessage by mutableStateOf<String?>(null)

    fun login(
        phone: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoggingIn = true
            errorMessage = null // 每次登录前重置错误信息

            // ✅ 改为调用 repository，并使用 .onSuccess / .onFailure
            NetworkModule.repository.login(LoginRequest(phone, pass))
                .onSuccess { data ->
                    // data 已经是剥离壳子后的 LoginResponse 对象
                    userPrefs.saveToken(data.token)
                    userPrefs.saveUser(data.user)
                    onSuccess()
                }
                .onFailure { e ->
                    // Repository 内部已发送 Toast，这里可以根据需要记录日志或更新 UI 状态
                    errorMessage = e.message
                    Log.e("API", "登录失败: ${e.message}")
                }

            isLoggingIn = false
        }
    }
}