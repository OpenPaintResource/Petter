package hi.petter.domain.repository

import hi.petter.domain.model.User

interface IAuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(username: String, email: String, password: String): User
    suspend fun logout()
    fun getCurrentUser(): User?
}