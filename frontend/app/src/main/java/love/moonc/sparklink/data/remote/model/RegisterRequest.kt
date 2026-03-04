package love.moonc.sparklink.data.remote.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("sex") val sex: String,
    @SerializedName("avatar") val avatar: String
)