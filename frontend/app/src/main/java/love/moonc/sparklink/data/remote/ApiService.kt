package love.moonc.sparklink.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import love.moonc.sparklink.data.remote.model.entity.Room
import love.moonc.sparklink.data.remote.model.request.CreateRoomRequest
import love.moonc.sparklink.data.remote.model.request.DissolveRoomRequest
import love.moonc.sparklink.data.remote.model.request.LeaveRoomRequest
import love.moonc.sparklink.data.remote.model.request.LoginRequest
import love.moonc.sparklink.data.remote.model.response.LoginResponse
import love.moonc.sparklink.data.remote.model.request.RegisterRequest
import love.moonc.sparklink.data.remote.model.request.UserUpdateRequest
import love.moonc.sparklink.data.remote.model.response.EnterRoomResponse
import love.moonc.sparklink.data.remote.model.response.RoomDetailResponse
import love.moonc.sparklink.data.remote.model.response.UploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File

interface ApiService {
    @Multipart
    @POST("common/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): BaseResponse<UploadResponse>

    @POST("user/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): BaseResponse<String>

    @POST("user/login")
    suspend fun login(
        @Body request: LoginRequest
    ): BaseResponse<LoginResponse>

    @GET("room/list")
    suspend fun getRoomList(): BaseResponse<List<Room>>

    @POST("room/create")
    suspend fun createRoom(@Body request: CreateRoomRequest): BaseResponse<Room>

    @POST("room/enter")
    suspend fun enterRoom(@Body request: LeaveRoomRequest): BaseResponse<EnterRoomResponse>

    @GET("room/info/{id}")
    suspend fun getRoomInfo(@Path("id") roomId: Long): BaseResponse<RoomDetailResponse>

    @POST("room/dissolve")
    suspend fun dissolveRoom(@Body request: DissolveRoomRequest): BaseResponse<String>

    @POST("room/leave")
    suspend fun leaveRoom(@Body request: LeaveRoomRequest): BaseResponse<String>

    @POST("user/update")
    suspend fun updateUser(@Body request: UserUpdateRequest): BaseResponse<String>

    @POST("user/close")
    suspend fun closeAccount(): BaseResponse<String>
}

suspend fun ApiService.uploadImage(
    context: android.content.Context,
    uri: android.net.Uri,
    prefix: String = "upload"
): String = withContext(Dispatchers.IO) {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw Exception("无法读取图片文件")

    val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
    val file = File(context.cacheDir, fileName)

    try {
        file.outputStream().use { output ->
            inputStream.use { input -> input.copyTo(output) }
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val response = this@uploadImage.uploadFile(body).getOrThrow()

        response.fileUrl
    } finally {
        if (file.exists()) file.delete()
    }
}