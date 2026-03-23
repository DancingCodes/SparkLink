package love.moonc.sparklink.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.getOrThrow
import love.moonc.sparklink.data.remote.model.entity.Room
import love.moonc.sparklink.data.remote.model.request.LeaveRoomRequest
import love.moonc.sparklink.data.remote.model.response.EnterRoomResponse

class HomeViewModel : ViewModel() {

    var rooms by mutableStateOf<List<Room>>(emptyList())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        fetchRoomList()
    }

    fun fetchRoomList() {
        viewModelScope.launch {
            isRefreshing = true
            try {
                rooms = NetworkModule.Api.getRoomList().getOrThrow()
            } catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }
    }

    fun enterRoom(
        roomId: Long,
        onSuccess: (EnterRoomResponse) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val enterData = NetworkModule.Api.enterRoom(LeaveRoomRequest(roomId)).getOrThrow()
                onSuccess(enterData)
            } catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
            }
        }
    }
}