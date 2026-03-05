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
import love.moonc.sparklink.data.remote.model.request.CreateRoomRequest

class CreateRoomViewModel : ViewModel() {
    var isCreating by mutableStateOf(false)
        private set

    fun createRoom(
        title: String,
        cover: String = "",
        onSuccess: (Room) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isCreating = true
            try {
                val response = NetworkModule.Api.createRoom(CreateRoomRequest(title, cover))
                onSuccess(response.data)
            } catch (e: ApiException) {
                onError(e.message)
            } catch (e: Exception) {
                onError("创建失败: ${e.localizedMessage}")
            } finally {
                isCreating = false
            }
        }
    }
}