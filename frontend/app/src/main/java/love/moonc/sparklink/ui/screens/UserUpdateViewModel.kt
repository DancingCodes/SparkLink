package love.moonc.sparklink.ui.screens

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.getOrThrow // 🚀 必带导入
import love.moonc.sparklink.data.remote.model.request.UserUpdateRequest

class UserUpdateViewModel : ViewModel() {
    private val userPreferences = UserPreferences.getInstance()

    var name by mutableStateOf("")
    var sex by mutableStateOf("")
    var avatar by mutableStateOf("")
    var password by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var updateSuccess by mutableStateOf(false)
    var isAccountClosed by mutableStateOf(false)

    init {
        viewModelScope.launch {
            userPreferences.userData.first()?.let { user ->
                name = user.name
                sex = user.sex
                avatar = user.avatar
            }
        }
    }

    fun updateUserInfo() {
        viewModelScope.launch {
            isLoading = true
            try {
                val request = UserUpdateRequest(
                    name = name,
                    sex = sex,
                    avatar = avatar,
                    password = password.ifEmpty { null }
                )

                NetworkModule.Api.updateUser(request).getOrThrow()

                userPreferences.userData.first()?.let {
                    val newUser = it.copy(name = name, sex = sex, avatar = avatar)
                    userPreferences.saveUser(newUser)
                }
                updateSuccess = true
            }  finally {
                isLoading = false
            }
        }
    }

    fun closeAccount() {
        viewModelScope.launch {
            isLoading = true
            try {
                NetworkModule.Api.closeAccount().getOrThrow()
                isAccountClosed = true
            }  catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}