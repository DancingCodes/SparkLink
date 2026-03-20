package love.moonc.sparklink.data.remote.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest (
    @SerialName("title") val title: String,
    @SerialName("cover") val cover: String = ""
)