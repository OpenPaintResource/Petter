package hi.petter.data.remote.repository

import hi.petter.data.remote.api.ApiServices
import hi.petter.data.repository.BaseRepository
import hi.petter.data.remote.network.NetworkModule
import hi.petter.data.remote.model.UserResponse
import hi.petter.data.remote.model.GroupResponse
import hi.petter.data.remote.model.MessageResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 远程用户Repository
 * 实现网络数据访问
 */
@Singleton
class RemoteUserRepository @Inject constructor(
    private val apiService: ApiServices
) : BaseRepository() {

    /**
     * 获取远程用户列表
     */
    suspend fun getUsers(): Flow<List<UserResponse>> {
        return flow {
            try {
                val response = apiService.getUsers()
                if (response.isSuccessful) {
                    response.body()?.let { users ->
                        emit(users)
                    }
                } else {
                    Timber.e("获取用户列表失败: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Timber.e("获取用户列表异常", e)
            }
        }
    }

    /**
     * 获取远程群组列表
     */
    suspend fun getGroups(): Flow<List<GroupResponse>> {
        return flow {
            try {
                val response = apiService.getGroups()
                if (response.isSuccessful) {
                    response.body()?.let { groups ->
                        emit(groups)
                    }
                } else {
                    Timber.e("获取群组列表失败: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Timber.e("获取群组列表异常", e)
            }
        }
    }

    /**
     * 获取消息列表
     */
    suspend fun getMessages(groupId: String): Flow<List<MessageResponse>> {
        return flow {
            try {
                val response = apiService.getMessages(groupId)
                if (response.isSuccessful) {
                    response.body()?.let { messages ->
                        emit(messages)
                    }
                } else {
                    Timber.e("获取消息列表失败: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Timber.e("获取消息列表异常", e)
            }
        }
    }
}