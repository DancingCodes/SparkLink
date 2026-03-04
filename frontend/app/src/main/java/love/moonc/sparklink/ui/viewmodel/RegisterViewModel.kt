package love.moonc.sparklink.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.exception.ApiException
import love.moonc.sparklink.data.remote.model.RegisterRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class RegisterViewModel : ViewModel() {

    // 观察注册状态（Loading）
    var isRegistering by mutableStateOf(false)
        private set

    /**
     * 执行注册逻辑
     */
    fun register(
        request: RegisterRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isRegistering = true
            try {
                // 直接调用全局 Api 单例
                NetworkModule.Api.register(request)
                onSuccess()
            } catch (e: ApiException) {
                // 拦截器抛出的业务错误
                onError(e.message)
            } catch (_: Exception) {
                // 网络崩溃等未知错误
                onError("连接服务器失败")
            } finally {
                isRegistering = false
            }
        }
    }
}