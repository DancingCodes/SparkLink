package love.moonc.sparklink.data.remote.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest(
    val name: String? = null,
    val sex: String? = null,
    val avatar: String? = null,
    val password: String? = null
)