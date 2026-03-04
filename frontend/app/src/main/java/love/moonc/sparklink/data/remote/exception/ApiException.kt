package love.moonc.sparklink.data.remote.exception

/**
 * 自定义业务异常：当后端返回 code != 200 时抛出
 */
class ApiException(val code: Int, override val message: String) : Exception(message)