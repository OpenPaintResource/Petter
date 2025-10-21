package hi.petter.presentation.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import hi.petter.domain.model.User
import hi.petter.domain.usecase.LoginUseCase
import hi.petter.domain.usecase.RegisterUseCase
import kotlinx.coroutines.Dispatchers

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    fun login(email: String, password: String) = liveData(Dispatchers.IO) {
        emit(Resource.Loading)
        try {
            val user = loginUseCase(email, password)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "登录失败"))
        }
    }

    fun register(username: String, email: String, password: String) = liveData(Dispatchers.IO) {
        emit(Resource.Loading)
        try {
            val user = registerUseCase(username, email, password)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "注册失败"))
        }
    }
}

sealed class Resource<T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}

class AuthViewModelFactory(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(loginUseCase, registerUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}