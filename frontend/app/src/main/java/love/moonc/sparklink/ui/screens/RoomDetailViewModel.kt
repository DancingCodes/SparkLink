package love.moonc.sparklink.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.model.entity.RoomUser
import love.moonc.sparklink.data.remote.model.response.RoomDetailResponse
import love.moonc.sparklink.data.rtc.AgoraManager

class RoomDetailViewModel : ViewModel() {
    private val userPrefs = UserPreferences.getInstance()

    // 房间数据状态
    var roomDetail by mutableStateOf<RoomDetailResponse?>(null)
    val occupants = mutableStateListOf<RoomUser>()

    /**
     * 进入房间初始化：获取详情 + 开启 WebSocket
     */
    fun enterRoom(roomId: Long) {
        viewModelScope.launch {
            NetworkModule.repository.getRoomInfo(roomId).onSuccess { res ->
                roomDetail = res
                occupants.clear()
                occupants.addAll(res.members)

                // 自动连接 WebSocket 监听成员变动
                val user = userPrefs.userData.first()
                val uid = user?.id?.toInt() ?: 0

                NetworkModule.roomSocketManager.connect(roomId.toString(), uid) { _ ->
                    // 收到加入或离开信号，刷新成员列表
                    viewModelScope.launch {
                        refreshMembers(roomId)
                    }
                }
            }
        }
    }

    private suspend fun refreshMembers(roomId: Long) {
        NetworkModule.repository.getRoomInfo(roomId).onSuccess {
            occupants.clear()
            occupants.addAll(it.members)
        }
    }

    /**
     * 退出房间清理：根据身份发送请求 + 断开所有连接
     */
    fun exitRoom(roomId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentUserId = userPrefs.userData.first()?.id
            val isOwner = roomDetail?.room?.ownerId == currentUserId

            if (isOwner) {
                NetworkModule.repository.dissolveRoom(roomId)
            } else {
                NetworkModule.repository.leaveRoom(roomId)
            }

            // 彻底清理
            cleanup()
            onComplete()
        }
    }

    private fun cleanup() {
        AgoraManager.leaveChannel()
        NetworkModule.roomSocketManager.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}