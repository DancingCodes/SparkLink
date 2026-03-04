package love.moonc.sparklink.data.remote

import love.moonc.sparklink.data.remote.model.LoginRequest
import love.moonc.sparklink.data.remote.model.LoginResponse
import love.moonc.sparklink.data.remote.model.RegisterRequest
import love.moonc.sparklink.data.remote.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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

}