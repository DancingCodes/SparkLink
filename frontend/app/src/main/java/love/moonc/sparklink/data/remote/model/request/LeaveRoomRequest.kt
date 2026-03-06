package love.moonc.sparklink.data.remote.model.request

import com.google.gson.annotations.SerializedName

class LeaveRoomRequest (
    @SerializedName("room_id") val roomId: Long
)