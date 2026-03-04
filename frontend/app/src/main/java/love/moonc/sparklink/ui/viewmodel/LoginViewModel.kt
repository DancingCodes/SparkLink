package love.moonc.sparklink.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.exception.ApiException
import love.moonc.sparklink.data.remote.model.LoginRequest
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val userPrefs: UserPreferences
) : ViewModel(){

    // 登录状态控制（Loading）
    var isLoggingIn by mutableStateOf(false)
        private set

    /**
     * 执行登录
     */
    fun login(
        phone: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoggingIn = true
            try {
                // 1. 调用接口
                val response = NetworkModule.Api.login(LoginRequest(phone, pass))

                // 2. 登录成功，保存 Token
                val token = response.data.token
                val user = response.data.user
                userPrefs.saveToken(token)
                userPrefs.saveUser(user)

                // 3. 回调给 UI
                onSuccess()
            } catch (e: ApiException) {
                onError(e.message)
            } catch (_: Exception) {
                onError("无法连接到服务器")
            } finally {
                isLoggingIn = false
            }
        }
    }
}