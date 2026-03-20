package love.moonc.sparklink.data.remote.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("phone") val phone: String,
    @SerialName("password") val password: String,
    @SerialName("name") val name: String,
    @SerialName("sex") val sex: String,
    @SerialName("avatar") val avatar: String
)