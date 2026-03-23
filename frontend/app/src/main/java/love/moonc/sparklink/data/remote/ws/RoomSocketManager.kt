package love.moonc.sparklink.data.remote.ws

import android.util.Log
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
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("RoomSocket", "WebSocket Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("RoomSocket", "Receive: $text")
                try {
                    val event = json.decodeFromString<RoomWsEvent>(text)
                    onEvent(event)
                } catch (e: Exception) {
                    Log.e("RoomSocket", "Decode Error: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("RoomSocket", "Connect Failure: ${t.message}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("RoomSocket", "Closing: $reason")
            }
        })
    }

    fun disconnect() {
        // 1000 代表正常关闭
        webSocket?.close(1000, "User quit room")
        webSocket = null
    }
}