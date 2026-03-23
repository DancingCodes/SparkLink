package love.moonc.sparklink.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.model.request.RegisterRequest

class RegisterViewModel : ViewModel() {
    var isRegistering by mutableStateOf(false)
        private set

    // ✅ 新增：上传状态和上传后的头像地址
    var isUploading by mutableStateOf(false)
        private set

    var uploadedAvatarUrl by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)

    /**
     * 🚀 新增：处理头像上传逻辑
     */
    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            NetworkModule.repository.uploadImage(context, uri, "avatar")
                .onSuccess { url ->
                    uploadedAvatarUrl = url
                }
                .onFailure { e ->
                    errorMessage = "头像上传失败: ${e.message}"
                    Log.e("API", errorMessage!!)
                }
            isUploading = false
        }
    }

    /**
     * 🚀 重构：执行注册操作
     */
    fun register(
        phone: String,
        pass: String,
        name: String,
        sex: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isRegistering = true
            errorMessage = null

            val request = RegisterRequest(
                phone = phone,
                password = pass,
                name = name,
                sex = sex,
                avatar = uploadedAvatarUrl // 直接使用内部维护的 URL
            )

            NetworkModule.repository.register(request)
                .onSuccess { _: String ->
                    onSuccess()
                }
                .onFailure { e ->
                    errorMessage = e.message
                    Log.e("API", "注册失败: ${e.message}")
                }

            isRegistering = false
        }
    }
}