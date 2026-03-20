package love.moonc.sparklink.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 对应后端的 Response 结构体
 * @param T 泛型，代表 Data 字段的具体类型
 */
@Serializable
data class BaseResponse<T>(
    @SerialName("code")
    val code: Int,

    @SerialName("msg")
    val msg: String,

    @SerialName("data")
    val data: T
)