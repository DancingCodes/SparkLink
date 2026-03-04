package love.moonc.sparklink.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    val id: Int,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_uuid") val fileUuid: String,
    @SerializedName("file_size") val fileSize: Int,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("created_at") val createdAt: String
)