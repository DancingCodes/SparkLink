package love.moonc.sparklink.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnterRoomResponse(
    @SerialName("agora_token") val agoraToken: String,
    @SerialName("agora_uid") val agoraUid: Int,
    @SerialName("channel_name") val channelName: String
)