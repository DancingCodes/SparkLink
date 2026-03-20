package love.moonc.sparklink.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class EnterRoomResponse(
    @SerializedName("agora_token") val agoraToken: String,
    @SerializedName("agora_uid") val agoraUid: Int,
    @SerializedName("channel_name") val channelName: String
)