package love.moonc.sparklink.data.remote.model.request

data class UserUpdateRequest(
    val name: String? = null,
    val sex: String? = null,
    val avatar: String? = null,
    val password: String? = null
)