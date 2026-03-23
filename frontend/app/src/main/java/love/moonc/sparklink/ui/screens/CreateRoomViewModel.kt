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
import love.moonc.sparklink.data.remote.model.request.CreateRoomRequest
import love.moonc.sparklink.data.remote.model.response.EnterRoomResponse

class CreateRoomViewModel : ViewModel() {
    // 状态控制
    var isCreating by mutableStateOf(false)
        private set

    var isUploading by mutableStateOf(false)
        private set

    var uploadedCoverUrl by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)

    /**
     * 🚀 新增：统一处理图片上传
     */
    fun uploadCover(context: Context, uri: Uri) {
        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            NetworkModule.repository.uploadImage(context, uri, "cover")
                .onSuccess { url ->
                    uploadedCoverUrl = url
                }
                .onFailure { e ->
                    errorMessage = "封面上传失败: ${e.message}"
                    Log.e("API", errorMessage!!)
                }
            isUploading = false
        }
    }

    /**
     * 🚀 重构：创建并进入房间
     */
    fun createAndEnterRoom(
        title: String,
        onSuccess: (Long, EnterRoomResponse) -> Unit,
    ) {
        viewModelScope.launch {
            isCreating = true
            errorMessage = null

            // 使用 repository 创建房间（自动使用已上传的 uploadedCoverUrl）
            NetworkModule.repository.createRoom(CreateRoomRequest(title, uploadedCoverUrl))
                .onSuccess { room ->
                    val newRoomId = room.id
                    // 嵌套进入房间逻辑
                    NetworkModule.repository.enterRoom(newRoomId)
                        .onSuccess { enterData ->
                            onSuccess(newRoomId, enterData)
                        }
                        .onFailure { e ->
                            errorMessage = "进入房间失败: ${e.message}"
                        }
                }
                .onFailure { e ->
                    errorMessage = "创建房间失败: ${e.message}"
                }

            isCreating = false
        }
    }
}