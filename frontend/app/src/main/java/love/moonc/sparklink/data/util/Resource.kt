package love.moonc.sparklink.data.util

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val code: Int, val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}