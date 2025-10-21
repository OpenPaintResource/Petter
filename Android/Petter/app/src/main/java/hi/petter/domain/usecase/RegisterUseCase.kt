package hi.petter.domain.usecase

import hi.petter.domain.model.User
import hi.petter.domain.repository.IAuthRepository

class RegisterUseCase(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(username: String, email: String, password: String): User {
        if (username.isBlank()) {
            throw IllegalArgumentException("用户名不能为空")
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw IllegalArgumentException("请输入有效的邮箱地址")
        }
        if (password.length < 6) {
            throw IllegalArgumentException("密码至少6位")
        }

        return authRepository.register(username, email, password)
    }
}