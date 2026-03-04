package love.moonc.sparklink.data.remote

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
    ): BaseResponse<UploadData>

    @POST("user/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): BaseResponse<String>
}