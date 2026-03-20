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
import love.moonc.sparklink.data.rtc.AgoraManager

class RoomDetailViewModel : ViewModel() {
    private val userPreferences = UserPreferences.getInstance()

    var roomDetail by mutableStateOf<RoomDetailResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // ✅ 修正：去掉 private set，允许 Screen 在权限回调中设置错误信息
    var errorMessage by mutableStateOf<String?>(null)

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

    // --- 声网 RTC 逻辑 ---

    fun joinRtcChannel(token: String, channelName: String, uid: Int) {
        AgoraManager.joinChannel(token, channelName, uid)
    }

    fun toggleMic(shouldSpeak: Boolean) {
        AgoraManager.setMicEnabled(shouldSpeak)
    }

    fun dissolveRoom(roomId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                NetworkModule.Api.dissolveRoom(DissolveRoomRequest(roomId))
                AgoraManager.leaveChannel()
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
                NetworkModule.Api.leaveRoom(LeaveRoomRequest(roomId))
                AgoraManager.leaveChannel()
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

    override fun onCleared() {
        super.onCleared()
        AgoraManager.leaveChannel()
    }
}