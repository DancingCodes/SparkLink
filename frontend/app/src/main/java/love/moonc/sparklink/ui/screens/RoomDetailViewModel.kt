package love.moonc.sparklink.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.exception.ApiException
import love.moonc.sparklink.data.remote.model.request.DissolveRoomRequest
import love.moonc.sparklink.data.remote.model.request.LeaveRoomRequest
import love.moonc.sparklink.data.remote.model.response.RoomDetailResponse

class RoomDetailViewModel : ViewModel() {
    private val userPreferences = UserPreferences.getInstance()

    var roomDetail by mutableStateOf<RoomDetailResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isDissolved by mutableStateOf(false)
        private set

    var isLeft by mutableStateOf(false)
        private set

    private var currentUserId = 0L

    init {
        viewModelScope.launch {
            currentUserId = userPreferences.userData.first()?.id ?: 0L
        }
    }

    val isOwner: Boolean
        get() = roomDetail?.room?.ownerId != 0L && roomDetail?.room?.ownerId == currentUserId

    fun fetchRoomInfo(roomId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = NetworkModule.Api.getRoomInfo(roomId)
                roomDetail = response.data
            } catch (e: ApiException) {
                errorMessage = e.message
            } catch (_: Exception) {
                errorMessage = "网络连接异常"
            } finally {
                isLoading = false
            }
        }
    }

    fun dissolveRoom(roomId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                val request = DissolveRoomRequest(roomId)
                NetworkModule.Api.dissolveRoom(request)
                isDissolved = true
            } catch (_: Exception) {
                errorMessage = "解散失败"
            } finally {
                isLoading = false
            }
        }
    }

    fun leaveRoom(roomId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                // ✅ 这里已经修正为 LeaveRoomRequest
                val request = LeaveRoomRequest(roomId)
                NetworkModule.Api.leaveRoom(request)
                isLeft = true
            } catch (_: Exception) {
                errorMessage = "退出房间失败"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}