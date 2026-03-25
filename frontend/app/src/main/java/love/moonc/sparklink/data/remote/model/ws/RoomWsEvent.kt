package love.moonc.sparklink.data.remote.model.ws

import kotlinx.serialization.Serializable

@Serializable
data class RoomWsEvent(
    val type: String,
    val uid: Int,
)