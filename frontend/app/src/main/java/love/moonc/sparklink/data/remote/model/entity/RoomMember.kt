package love.moonc.sparklink.data.remote.model.entity

import com.google.gson.annotations.SerializedName

data class RoomMember(
    @SerializedName("id") val id: Long,
    @SerializedName("room_id") val roomId: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("user") val user: User,
    @SerializedName("role") val role: Int, // 1: 房主, 2: 成员
    @SerializedName("entered_at") val enteredAt: String
)