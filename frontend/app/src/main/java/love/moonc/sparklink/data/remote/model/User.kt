package love.moonc.sparklink.data.remote.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val phone: String,
    val password: String,
    val name: String,
    val sex: String,
    val avatar: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)