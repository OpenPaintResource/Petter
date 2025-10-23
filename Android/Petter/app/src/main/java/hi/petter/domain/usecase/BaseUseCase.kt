package hi.petter.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 标准UseCase基类
 * 统一协程调度、错误处理、日志记录
 */
abstract class BaseUseCase(
    protected val defaultDispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
) {

    /**
     * 在IO线程中执行操作
     */
    protected suspend fun <T> executeOnIO(
        operation: suspend () -> T,
        errorMessage: String = "操作失败"
    ): Result<T> {
        return try {
            withContext(defaultDispatcher) {
                Result.success(operation())
            }
        } catch (e: Exception) {
            Timber.e("$errorMessage", e)
            Result.failure(e)
        }
    }

    /**
     * 在主线程中执行操作
     */
    protected suspend fun <T> executeOnMain(
        operation: suspend () -> T,
        errorMessage: String = "操作失败"
    ): Result<T> {
        return try {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                Result.success(operation())
            }
        } catch (e: Exception) {
            Timber.e("$errorMessage", e)
            Result.failure(e)
        }
    }
}