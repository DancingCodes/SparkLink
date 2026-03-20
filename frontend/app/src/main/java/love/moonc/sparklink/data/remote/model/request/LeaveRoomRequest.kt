package love.moonc.sparklink.data.remote.model.request

import kotlinx.serialization.SerialName

class LeaveRoomRequest (
    @SerialName("room_id") val roomId: Long
)