package love.moonc.sparklink.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.moonc.sparklink.data.events.AppEvent
import love.moonc.sparklink.data.events.AppEventBus
import love.moonc.sparklink.data.remote.exception.ApiException

@Serializable
data class BaseResponse<T>(
    @SerialName("code") val code: Int,
    @SerialName("msg") val msg: String,
    @SerialName("data") val data: T? = null
) {
    val isSuccess: Boolean get() = code == 200
}

/**
 * 🚀 核心扩展函数：简化业务报错处理
 * 只有在 ViewModel 中显式调用 .getOrThrow() 时才会触发弹窗和报错
 */
suspend fun <T> BaseResponse<T>.getOrThrow(): T {
    if (this.isSuccess) return this.data!!

    AppEventBus.emit(AppEvent.ShowToast(this.msg))

    if (this.code == 401) {
        AppEventBus.emit(AppEvent.Logout)
    }

    throw ApiException(this.code, this.msg)
}