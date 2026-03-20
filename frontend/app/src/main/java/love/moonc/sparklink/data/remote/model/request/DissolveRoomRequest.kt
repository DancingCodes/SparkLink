package love.moonc.sparklink.data.remote.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DissolveRoomRequest(
    @SerialName("room_id") val roomId: Long
)