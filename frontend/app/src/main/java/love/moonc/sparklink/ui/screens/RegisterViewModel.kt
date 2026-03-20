package love.moonc.sparklink.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.exception.ApiException
import love.moonc.sparklink.data.remote.model.request.RegisterRequest

class RegisterViewModel : ViewModel() {
    var isRegistering by mutableStateOf(false)
        private set

    fun register(
        request: RegisterRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isRegistering = true
            try {
                NetworkModule.Api.register(request)
                onSuccess()
            } catch (e: ApiException) {
                onError(e.message)
            } catch (e: Exception) {
                onError("网络异常: ${e.localizedMessage ?: "连接服务器失败"}")
            } finally {
                isRegistering = false
            }
        }
    }
}