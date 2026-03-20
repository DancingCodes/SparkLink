package love.moonc.sparklink.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val id: Int,
    @SerialName("file_name") val fileName: String,
    @SerialName("file_uuid") val fileUuid: String,
    @SerialName("file_size") val fileSize: Int,
    @SerialName("file_type") val fileType: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("file_url") val fileUrl: String,
)