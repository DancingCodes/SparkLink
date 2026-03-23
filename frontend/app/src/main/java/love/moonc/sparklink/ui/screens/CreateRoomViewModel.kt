package love.moonc.sparklink.ui.screens

import android.content.Context
import android.net.Uri
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


    /**
     * 🚀 新增：统一处理图片上传
     */
    fun uploadCover(context: Context, uri: Uri) {
        viewModelScope.launch {
            isUploading = true
            NetworkModule.repository.uploadImage(context, uri, "cover")
                .onSuccess { url ->
                    uploadedCoverUrl = url
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

            NetworkModule.repository.createRoom(CreateRoomRequest(title, uploadedCoverUrl))
                .onSuccess { room ->
                    val newRoomId = room.id
                    NetworkModule.repository.enterRoom(newRoomId)
                        .onSuccess { enterData ->
                            onSuccess(newRoomId, enterData)
                        }

                }
            isCreating = false
        }
    }
}