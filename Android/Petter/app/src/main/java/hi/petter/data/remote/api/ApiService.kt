package hi.petter.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import timber.log.Timber
import hi.petter.data.remote.model.UserResponse
import hi.petter.data.remote.model.GroupResponse
import hi.petter.data.remote.model.MessageResponse

/**
 * API服务接口
 * 定义所有网络请求的端点
 */
interface ApiServices {

    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("groups")
    suspend fun getGroups(): Response<List<GroupResponse>>

    @GET("messages")
    suspend fun getMessages(@Query("groupId") groupId: String): Response<List<MessageResponse>>

    companion object {
        const val BASE_URL = "https://api.example.com/"
        const val TIMEOUT = 30_000L
    }
}