package love.moonc.sparklink.data.remote

import com.google.gson.annotations.SerializedName

/**
 * 对应你 Go 后端的注册模型
 * 字段名必须与后端 JSON 标签一致
 */
data class RegisterRequest(
    @SerializedName("phone")
    val phone: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("name")
    val name: String, // 对应后端的 Name 字段

    @SerializedName("sex")
    val sex: String,  // 对应后端的 Sex 字段

    @SerializedName("avatar")
    val avatar: String // 这里是上传头像成功后拿到的图片 URL
)