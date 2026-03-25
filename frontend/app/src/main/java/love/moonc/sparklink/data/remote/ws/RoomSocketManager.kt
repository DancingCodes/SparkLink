package love.moonc.sparklink.data.remote.ws

import kotlinx.serialization.json.Json
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.model.ws.RoomWsEvent
import okhttp3.*

class RoomSocketManager(private val client: OkHttpClient) {
    private var webSocket: WebSocket? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun connect(roomId: String, uid: Int, onEvent: (RoomWsEvent) -> Unit) {
        // 使用 NetworkModule 动态生成的 ws 地址
        val url = NetworkModule.getWsUrl("ws/room?room_id=$roomId&uid=$uid")

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val event = json.decodeFromString<RoomWsEvent>(text)
                onEvent(event)
            }
        })
    }

    fun disconnect() {
        // 1000 代表正常关闭
        webSocket?.close(1000, "User quit room")
        webSocket = null
    }
}