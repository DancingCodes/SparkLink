package love.moonc.sparklink.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
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

    var name by mutableStateOf("")
    var sex by mutableStateOf("")
    var avatar by mutableStateOf("")
    var password by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var isUploading by mutableStateOf(false) // ✅ 新增：上传状态
    var updateSuccess by mutableStateOf(false)
    var isAccountClosed by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        // 初始化：从本地缓存读取当前用户信息
        viewModelScope.launch {
            userPreferences.userData.first()?.let { user ->
                name = user.name
                sex = user.sex
                avatar = user.avatar
            }
        }
    }

    /**
     * 🚀 新增：头像上传逻辑
     */
    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            NetworkModule.repository.uploadImage(context, uri, "avatar")
                .onSuccess { url ->
                    avatar = url
                }
                .onFailure { e ->
                    errorMessage = "头像上传失败: ${e.message}"
                }
            isUploading = false
        }
    }

    /**
     * 更新用户信息
     */
    fun updateUserInfo() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val request = UserUpdateRequest(
                name = name,
                sex = sex,
                avatar = avatar,
                password = password.ifEmpty { null }
            )

            NetworkModule.repository.updateUser(request)
                .onSuccess {
                    // ✅ 同步更新本地缓存，确保首页等地方能实时看到变化
                    userPreferences.userData.first()?.let { oldUser ->
                        val newUser = oldUser.copy(name = name, sex = sex, avatar = avatar)
                        userPreferences.saveUser(newUser)
                    }
                    updateSuccess = true
                }
                .onFailure { e ->
                    errorMessage = e.message
                    Log.e("API", "更新失败: ${e.message}")
                }
            isLoading = false
        }
    }

    /**
     * 注销账号
     */
    fun closeAccount() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            NetworkModule.repository.closeAccount()
                .onSuccess { isAccountClosed = true }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }
}