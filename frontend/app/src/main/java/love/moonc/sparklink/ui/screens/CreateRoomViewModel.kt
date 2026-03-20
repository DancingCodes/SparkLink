package love.moonc.sparklink.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.model.request.CreateRoomRequest
import love.moonc.sparklink.data.remote.model.request.LeaveRoomRequest
import love.moonc.sparklink.data.remote.model.response.EnterRoomResponse

class CreateRoomViewModel : ViewModel() {
    var isCreating by mutableStateOf(false)
        private set
// 文件路径: love/moonc/sparklink/ui/screens/CreateRoomViewModel.kt

    fun createAndEnterRoom(
        title: String,
        cover: String = "",
        onSuccess: (Long, EnterRoomResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isCreating = true
            try {
                val createResponse = NetworkModule.Api.createRoom(CreateRoomRequest(title, cover))
                val newRoomId = createResponse.data.id

                val enterResponse = NetworkModule.Api.enterRoom(LeaveRoomRequest(newRoomId))

                onSuccess(newRoomId, enterResponse.data)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "开房流程出错")
            } finally {
                isCreating = false
            }
        }
    }
}