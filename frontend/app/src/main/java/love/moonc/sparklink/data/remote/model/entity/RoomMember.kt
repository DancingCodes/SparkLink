package love.moonc.sparklink.data.remote.model.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomMember(
    @SerialName("id") val id: Long,
    @SerialName("room_id") val roomId: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("user") val user: User,
    @SerialName("role") val role: Int, // 1: 房主, 2: 成员
    @SerialName("entered_at") val enteredAt: String
)