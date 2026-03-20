package love.moonc.sparklink.data.remote.model.request

import kotlinx.serialization.SerialName

class CreateRoomRequest (
    @SerialName("title") val title: String,
    @SerialName("cover") val cover: String = ""
)