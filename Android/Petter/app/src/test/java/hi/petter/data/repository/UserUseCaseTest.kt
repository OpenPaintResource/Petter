package hi.petter.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import hi.petter.domain.usecase.UserUseCase

/**
 * UserUseCase单元测试
 * 验证用户相关的业务逻辑
 */
class UserUseCaseTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var userUseCase: UserUseCase

    @Before
    fun setup() {
        userUseCase = UserUseCase()
    }

    @Test
    fun `test getAllUsers should return non-empty list`() = runBlockingTest {
        // Given
        // 初始状态应该有用户

        // When
        val result = userUseCase.getAllUsers()
        val users = result.getOrThrow()

        // Then
        Assert.assertNotNull("用户列表不应为空", users)
        Assert.assertTrue("应该返回用户数据", users.isNotEmpty())
    }

    @Test
    fun `test getUserById should return user when found`() = runBlockingTest {
        // Given
        val userId = "1"

        // When
        val result = userUseCase.getUserById(userId)

        // Then
        Assert.assertTrue("应该返回用户", result.isSuccess)
        Assert.assertEquals("用户ID应该匹配", "1", result.getOrNull()?.id)
    }

    @Test
    fun `test getUserById should return null when not found`() = runBlockingTest {
        // Given
        val userId = "999"

        // When
        val result = userUseCase.getUserById(userId)

        // Then
        Assert.assertFalse("用户不存在时应该返回null", result.isSuccess)
        Assert.assertNull("用户不存在时应该返回null", result.getOrNull())
    }

    @Test
    fun `test searchUsers should return filtered results`() = runBlockingTest {
        // Given
        val query = "张"

        // When
        val result = userUseCase.searchUsers(query)

        // Then
        Assert.assertTrue("搜索应该成功", result.isNotEmpty())
        Assert.assertTrue("应该包含匹配的用户", result.any { it.name.contains(query, ignoreCase = true) })
    }

    @Test
    fun `test update user status should succeed`() = runBlocking {
        // Given
        val userId = "1"

        // When
        val result = userUseCase.updateUserStatus(userId, "away")

        // Then
        Assert.assertTrue("更新状态应该成功", result.isSuccess)
    }
}