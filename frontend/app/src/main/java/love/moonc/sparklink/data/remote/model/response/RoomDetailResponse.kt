package love.moonc.sparklink.data.remote.model.response

import com.google.gson.annotations.SerializedName
import love.moonc.sparklink.data.remote.model.entity.Room
import love.moonc.sparklink.data.remote.model.entity.RoomMember

data class RoomDetailResponse(
    @SerializedName("room") val room: Room,
    @SerializedName("members") val members: List<RoomMember>
)