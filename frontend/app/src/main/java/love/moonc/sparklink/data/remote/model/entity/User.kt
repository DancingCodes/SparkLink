package love.moonc.sparklink.data.remote.model.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id") val id: Long,
    @SerialName("phone") val phone: String,
    @SerialName("name") val name: String,
    @SerialName("sex") val sex: String,
    @SerialName("avatar") val avatar: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)