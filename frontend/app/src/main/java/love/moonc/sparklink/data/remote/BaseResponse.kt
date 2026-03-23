package love.moonc.sparklink.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.moonc.sparklink.data.events.AppEvent
import love.moonc.sparklink.data.events.AppEventBus

@Serializable
data class BaseResponse<T>(
    @SerialName("code") val code: Int,
    @SerialName("msg") val msg: String,
    @SerialName("data") val data: T? = null
) {
    val isSuccess: Boolean get() = code == 200
}

/**
 * 将 BaseResponse 转换为 Kotlin 的 Result 类型
 */
suspend fun <T> BaseResponse<T>.toResult(): Result<T> {
    if (this.isSuccess) {
        // 关键：处理 data 为空但业务成功的情况（如退出房间）
        @Suppress("UNCHECKED_CAST")
        return Result.success((data ?: Unit) as T)
    }

    // 统一处理报错
    AppEventBus.emit(AppEvent.ShowToast(this.msg))
    if (this.code == 401) {
        AppEventBus.emit(AppEvent.Logout)
    }
    return Result.failure(Exception(this.msg))
}