package love.moonc.sparklink.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.moonc.sparklink.data.remote.model.entity.Room
import love.moonc.sparklink.data.remote.model.entity.RoomMember

@Serializable
data class RoomDetailResponse(
    @SerialName("room") val room: Room,
    @SerialName("members") val members: List<RoomMember>
)