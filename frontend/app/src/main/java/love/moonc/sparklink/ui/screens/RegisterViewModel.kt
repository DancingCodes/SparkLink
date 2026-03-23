package love.moonc.sparklink.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.getOrThrow // 🚀 别忘了导入它
import love.moonc.sparklink.data.remote.model.request.RegisterRequest

class RegisterViewModel : ViewModel() {
    var isRegistering by mutableStateOf(false)
        private set

    fun register(
        request: RegisterRequest,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isRegistering = true
            try {
                NetworkModule.Api.register(request).getOrThrow()
                onSuccess()
            } catch (e: Exception) {
                Log.e("API", "请求失败: ${e.message}")
            } finally {
                isRegistering = false
            }
        }
    }
}