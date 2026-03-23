package love.moonc.sparklink.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.model.entity.Room
import love.moonc.sparklink.data.remote.model.response.EnterRoomResponse

class HomeViewModel : ViewModel() {

    var rooms by mutableStateOf<List<Room>>(emptyList())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    // 添加错误提示状态
    var errorMessage by mutableStateOf<String?>(null)

    init {
        fetchRoomList()
    }

    fun fetchRoomList() {
        viewModelScope.launch {
            isRefreshing = true
            errorMessage = null

            // ✅ 使用 repository 获取房间列表
            NetworkModule.repository.getRoomList()
                .onSuccess { list ->
                    rooms = list
                }
                .onFailure { e ->
                    errorMessage = "获取列表失败: ${e.message}"
                    Log.e("API", errorMessage!!)
                }

            isRefreshing = false
        }
    }

    fun enterRoom(
        roomId: Long,
        onSuccess: (EnterRoomResponse) -> Unit
    ) {
        viewModelScope.launch {
            // ✅ 使用 repository 进入房间
            NetworkModule.repository.enterRoom(roomId)
                .onSuccess { enterData ->
                    onSuccess(enterData)
                }
                .onFailure { e ->
                    errorMessage = "进入房间失败: ${e.message}"
                    Log.e("API", errorMessage!!)
                }
        }
    }
}