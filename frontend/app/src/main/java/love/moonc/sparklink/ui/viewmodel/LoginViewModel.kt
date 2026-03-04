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

class LoginViewModel(
    private val userPrefs: UserPreferences
) : ViewModel() {

    var isLoggingIn by mutableStateOf(false)
        private set

    fun login(
        phone: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoggingIn = true
            try {
                val response = NetworkModule.Api.login(LoginRequest(phone, pass))
                val token = response.data.token
                val user = response.data.user
                userPrefs.saveToken(token)
                userPrefs.saveUser(user)
                onSuccess()
            } catch (e: ApiException) {
                onError(e.message)
            } catch (e: Exception) {
                onError("无法连接到服务器: ${e.localizedMessage}")
            } finally {
                isLoggingIn = false
            }
        }
    }
}