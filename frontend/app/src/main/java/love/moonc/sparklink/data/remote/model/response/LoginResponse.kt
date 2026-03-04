package love.moonc.sparklink.data.remote.model.response

import com.google.gson.annotations.SerializedName
import love.moonc.sparklink.data.remote.model.entity.User

data class LoginResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("user")
    val user: User
)