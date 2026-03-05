package love.moonc.sparklink.data.remote.model.entity

import com.google.gson.annotations.SerializedName

data class Room(
    @SerializedName("id") val id: Long,
    @SerializedName("owner_id") val ownerId: Long,
    @SerializedName("owner") val owner: User,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)