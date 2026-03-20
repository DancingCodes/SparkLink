package love.moonc.sparklink.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.exception.ApiException
import love.moonc.sparklink.data.remote.model.entity.Room
import love.moonc.sparklink.data.remote.model.request.LeaveRoomRequest
import love.moonc.sparklink.data.remote.model.response.EnterRoomResponse

class HomeViewModel : ViewModel() {

    var rooms by mutableStateOf<List<Room>>(emptyList())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var lastEnterData by mutableStateOf<EnterRoomResponse?>(null)

    init {
        fetchRoomList()
    }

    fun fetchRoomList(onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            isRefreshing = true
            try {
                val response = NetworkModule.Api.getRoomList()
                android.util.Log.d("API_DEBUG", "Rooms Data: ${response.data}")
                rooms = response.data
            } catch (e: ApiException) {
                onError(e.message)
            } catch (e: Exception) {
                onError("无法连接到服务器: ${e.localizedMessage}")
            } finally {
                isRefreshing = false
            }
        }
    }

    fun enterRoom(
        roomId: Long,
        onSuccess: (EnterRoomResponse) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = NetworkModule.Api.enterRoom(LeaveRoomRequest(roomId))
                onSuccess(response.data)
            } catch (e: ApiException) {
                onError(e.message)
            } catch (e: Exception) {
                onError("进入房间失败: ${e.localizedMessage}")
            }
        }
    }
}