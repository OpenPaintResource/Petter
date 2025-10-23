package hi.petter.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import timber.log.Timber

/**
 * 标准Repository基类
 * 统一错误处理、数据缓存、日志记录
 */
abstract class BaseRepository {

    /**
     * 统一错误处理
     */
    protected suspend fun <T> safeCall(
        operation: suspend () -> T,
        errorMessage: String = "操作失败"
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            Timber.e("$errorMessage", e)
            Result.failure(e)
        }
    }

    /**
     * API调用统一错误处理 (别名方法)
     */
    protected suspend fun <T> safeApiCall(
        errorMessage: String = "API调用失败",
        operation: suspend () -> T
    ): Result<T> {
        return safeCall(operation, errorMessage)
    }

    /**
     * 统一缓存机制
     */
    protected suspend fun <T> safeCallWithCache(
        operation: suspend () -> T,
        errorMessage: String = "操作失败"
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            Timber.e("$errorMessage", e)
            Result.failure(e)
        }
    }

    /**
     * 流数据获取包装
     */
    protected fun <T> wrapWithFlow(
        operation: suspend () -> Flow<T>,
        errorMessage: String = "数据获取失败"
    ): Flow<T> {
        return kotlinx.coroutines.flow.flow {
            try {
                emitAll(operation())
            } catch (e: Exception) {
                Timber.e("$errorMessage", e)
                // Flow will end without emitting values on error
            }
        }
    }
}