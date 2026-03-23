package love.moonc.sparklink.ui.screens

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.getOrThrow // 🚀 导入
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

    /**
     * 获取房间详情
     */
    fun fetchRoomInfo(roomId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                roomDetail = NetworkModule.Api.getRoomInfo(roomId).getOrThrow()
            } catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
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

    /**
     * 解散房间（房主操作）
     */
    fun dissolveRoom(roomId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                NetworkModule.Api.dissolveRoom(DissolveRoomRequest(roomId)).getOrThrow()
                AgoraManager.leaveChannel()
                isDissolved = true
            } catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * 退出房间（普通成员操作）
     */
    fun leaveRoom(roomId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                NetworkModule.Api.leaveRoom(LeaveRoomRequest(roomId)).getOrThrow()
                AgoraManager.leaveChannel()
                isLeft = true
            } catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
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
        // 确保 ViewModel 销毁时彻底断开 RTC 链接
        AgoraManager.leaveChannel()
    }
}