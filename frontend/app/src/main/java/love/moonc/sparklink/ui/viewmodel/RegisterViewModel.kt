package love.moonc.sparklink.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.exception.ApiException
import love.moonc.sparklink.data.remote.model.RegisterRequest

class RegisterViewModel : ViewModel() {
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
                // 直接调用全局 NetworkModule 里的 Api 单例
                NetworkModule.Api.register(request)
                onSuccess()
            } catch (e: ApiException) {
                // 拦截器捕获到的后端业务错误（如：手机号已注册）
                onError(e.message)
            } catch (e: Exception) {
                // 网络异常、超时或代码逻辑崩溃
                onError("网络异常: ${e.localizedMessage ?: "连接服务器失败"}")
            } finally {
                // 无论成功还是失败，都要关闭 Loading 状态
                isRegistering = false
            }
        }
    }
}