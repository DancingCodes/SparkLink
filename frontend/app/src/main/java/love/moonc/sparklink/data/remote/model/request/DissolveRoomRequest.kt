package love.moonc.sparklink.data.remote.model.request

import com.google.gson.annotations.SerializedName

data class DissolveRoomRequest(
    @SerializedName("room_id") val roomId: Long
)