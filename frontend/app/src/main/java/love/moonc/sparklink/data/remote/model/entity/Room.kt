package love.moonc.sparklink.data.remote.model.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Room(
    @SerialName("id") val id: Long,
    @SerialName("owner_id") val ownerId: Long,
    @SerialName("owner") val owner: User,
    @SerialName("title") val title: String,
    @SerialName("cover") val cover: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)