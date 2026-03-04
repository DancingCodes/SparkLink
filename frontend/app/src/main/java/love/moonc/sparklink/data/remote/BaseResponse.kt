package love.moonc.sparklink.data.remote

import com.google.gson.annotations.SerializedName

/**
 * 对应后端的 Response 结构体
 * @param T 泛型，代表 Data 字段的具体类型
 */
data class BaseResponse<T>(
    @SerializedName("code")
    val code: Int,

    @SerializedName("msg")
    val msg: String,

    @SerializedName("data")
    val data: T
)