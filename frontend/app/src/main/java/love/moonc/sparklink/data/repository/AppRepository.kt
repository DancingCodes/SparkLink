package love.moonc.sparklink.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import love.moonc.sparklink.data.remote.ApiService
import love.moonc.sparklink.data.remote.toResult
import love.moonc.sparklink.data.remote.model.request.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AppRepository(private val api: ApiService) {

    // 用户模块
    suspend fun login(req: LoginRequest) = api.login(req).toResult()
    suspend fun register(req: RegisterRequest) = api.register(req).toResult()
    suspend fun updateUser(req: UserUpdateRequest) = api.updateUser(req).toResult()
    suspend fun closeAccount() = api.closeAccount().toResult()

    // 房间模块
    suspend fun getRoomList() = api.getRoomList().toResult()
    suspend fun createRoom(req: CreateRoomRequest) = api.createRoom(req).toResult()
    suspend fun enterRoom(roomId: Long) = api.enterRoom(LeaveRoomRequest(roomId)).toResult()
    suspend fun getRoomInfo(id: Long) = api.getRoomInfo(id).toResult()
    suspend fun leaveRoom(roomId: Long) = api.leaveRoom(LeaveRoomRequest(roomId)).toResult()
    suspend fun dissolveRoom(roomId: Long) = api.dissolveRoom(DissolveRoomRequest(roomId)).toResult()

    suspend fun uploadImage(context: Context, uri: Uri, prefix: String = "upload"): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("无法读取图片")
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { inputStream.copyTo(it) }

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val res = api.uploadFile(body).toResult()
            file.delete()

            res.map { it.fileUrl }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}