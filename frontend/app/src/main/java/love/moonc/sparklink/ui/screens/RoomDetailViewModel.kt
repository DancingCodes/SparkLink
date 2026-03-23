package love.moonc.sparklink.ui.screens

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.getOrThrow
import love.moonc.sparklink.data.remote.model.request.DissolveRoomRequest
import love.moonc.sparklink.data.remote.model.request.LeaveRoomRequest
import love.moonc.sparklink.data.remote.model.response.RoomDetailResponse
import love.moonc.sparklink.data.rtc.AgoraManager

class RoomDetailViewModel : ViewModel() {
    private val userPreferences = UserPreferences.getInstance()

    // 1. 房间静态详情
    var roomDetail by mutableStateOf<RoomDetailResponse?>(null)
        private set

    // 2. 🚀 动态成员列表（通过 WebSocket 实时更新）
    var occupantUids = mutableStateListOf<Int>()
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
            // 获取当前登录用户 ID，用于 WS 连接参数
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
                val response = NetworkModule.Api.getRoomInfo(roomId).getOrThrow()
                roomDetail = response

                // 3. ✅ 房间信息获取成功后，立即连接 WebSocket 监听成员变动
                connectRoomSocket(roomId)

            } catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * 🚀 启动 WebSocket 连接
     */
    private fun connectRoomSocket(roomId: Long) {
        if (currentUserId == 0L) {
            Log.e("WS", "Current UID is 0, cannot connect")
            return
        }

        NetworkModule.roomSocketManager.connect(
            roomId = roomId.toString(),
            uid = currentUserId.toInt()
        ) { event ->
            // OkHttp 回调在子线程，必须切回主线程更新 Compose 状态
            viewModelScope.launch(Dispatchers.Main) {
                when (event.type) {
                    "join" -> {
                        // 如果列表里没有，就加进去
                        if (!occupantUids.contains(event.uid)) {
                            occupantUids.add(event.uid)
                        }
                    }
                    "leave" -> {
                        // 有人走，移除掉
                        occupantUids.remove(event.uid)
                    }
                    "error" -> {
                        Log.e("WS", "业务报错: ${event.msg}")
                    }
                }
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
                // 停止 RTC
                AgoraManager.leaveChannel()
                // 停止 WS
                NetworkModule.roomSocketManager.disconnect()
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
                // 停止 RTC
                AgoraManager.leaveChannel()
                // 停止 WS
                NetworkModule.roomSocketManager.disconnect()
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
        // 🚀 确保清理：断开 WS 和 RTC 链接
        NetworkModule.roomSocketManager.disconnect()
        AgoraManager.leaveChannel()
    }
}