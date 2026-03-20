package love.moonc.sparklink.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.moonc.sparklink.data.remote.model.entity.User

@Serializable
data class LoginResponse(
    @SerialName("token") val token: String,
    @SerialName("user") val user: User
)