package hi.petter.domain.usecase

import hi.petter.domain.model.User
import hi.petter.domain.repository.IAuthRepository

class LoginUseCase(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(email: String, password: String): User {
        if (email.isBlank()) {
            throw IllegalArgumentException("邮箱不能为空")
        }
        if (password.length < 6) {
            throw IllegalArgumentException("密码至少6位")
        }

        return authRepository.login(email, password)
    }
}