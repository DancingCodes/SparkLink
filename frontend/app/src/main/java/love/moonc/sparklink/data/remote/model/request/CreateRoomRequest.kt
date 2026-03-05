package love.moonc.sparklink.data.remote.model.request

import com.google.gson.annotations.SerializedName

class CreateRoomRequest (
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String = ""
)