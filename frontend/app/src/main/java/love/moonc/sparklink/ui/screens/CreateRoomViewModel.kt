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
import love.moonc.sparklink.data.remote.model.request.CreateRoomRequest
import love.moonc.sparklink.data.remote.model.request.LeaveRoomRequest
import love.moonc.sparklink.data.remote.model.response.EnterRoomResponse

class CreateRoomViewModel : ViewModel() {
    var isCreating by mutableStateOf(false)
        private set

    fun createAndEnterRoom(
        title: String,
        cover: String = "",
        onSuccess: (Long, EnterRoomResponse) -> Unit,
    ) {
        viewModelScope.launch {
            isCreating = true
            try {
                val room = NetworkModule.Api.createRoom(CreateRoomRequest(title, cover)).getOrThrow()
                val newRoomId = room.id
                val enterData = NetworkModule.Api.enterRoom(LeaveRoomRequest(newRoomId)).getOrThrow()
                onSuccess(newRoomId, enterData)
            } catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
            } finally {
                isCreating = false
            }
        }
    }
}