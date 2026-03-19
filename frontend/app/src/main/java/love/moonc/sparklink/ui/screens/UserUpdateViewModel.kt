package love.moonc.sparklink.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.model.request.UserUpdateRequest

class UserUpdateViewModel : ViewModel() {
    private val userPreferences = UserPreferences.getInstance()

    // 状态表单
    var name by mutableStateOf("")
    var sex by mutableStateOf("")
    var avatar by mutableStateOf("")
    var password by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var updateSuccess by mutableStateOf(false)
    var isAccountClosed by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        // 初始化时加载本地缓存的用户数据
        viewModelScope.launch {
            userPreferences.userData.first()?.let { user ->
                name = user.name
                sex = user.sex
                avatar = user.avatar
            }
        }
    }

    // 更新用户信息
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
                val response = NetworkModule.Api.updateUser(request)

                if (response.code == 200) {
                    // 同步更新本地存储
                    val currentUser = userPreferences.userData.first()
                    currentUser?.let {
                        val newUser = it.copy(name = name, sex = sex, avatar = avatar)
                        userPreferences.saveUser(newUser)
                    }
                    updateSuccess = true
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "更新失败"
            } finally {
                isLoading = false
            }
        }
    }

    // 注销账户
    fun closeAccount() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = NetworkModule.Api.closeAccount()
                if (response.code == 200) {
                    isAccountClosed = true
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "注销失败"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() { errorMessage = null }
}