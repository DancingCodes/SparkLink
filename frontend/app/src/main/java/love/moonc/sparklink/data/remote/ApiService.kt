package love.moonc.sparklink.data.remote

import love.moonc.sparklink.data.remote.model.entity.Room
import love.moonc.sparklink.data.remote.model.request.CreateRoomRequest
import love.moonc.sparklink.data.remote.model.request.DissolveRoomRequest
import love.moonc.sparklink.data.remote.model.request.LeaveRoomRequest
import love.moonc.sparklink.data.remote.model.request.LoginRequest
import love.moonc.sparklink.data.remote.model.response.LoginResponse
import love.moonc.sparklink.data.remote.model.request.RegisterRequest
import love.moonc.sparklink.data.remote.model.response.RoomDetailResponse
import love.moonc.sparklink.data.remote.model.response.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

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

    @GET("room/info/{id}")
    suspend fun getRoomInfo(@Path("id") roomId: Long): BaseResponse<RoomDetailResponse>

    @POST("room/dissolve")
    suspend fun dissolveRoom(@Body request: DissolveRoomRequest): BaseResponse<String>


    @POST("room/leave")
    suspend fun leaveRoom(@Body request: LeaveRoomRequest): BaseResponse<String>
}