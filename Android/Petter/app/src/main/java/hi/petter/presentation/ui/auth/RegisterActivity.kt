package hi.petter.presentation.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import hi.petter.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    // TODO: 添加ViewModel实例

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.tvLogin.setOnClickListener {
            finish() // 返回登录界面
        }
    }

    private fun performRegister() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (validateInputs(username, email, password, confirmPassword)) {
            // TODO: 实现注册逻辑
            Toast.makeText(this, "注册功能待实现", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (username.length < 2) {
            binding.tilUsername.error = "用户名至少2位"
            isValid = false
        } else {
            binding.tilUsername.error = null
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "请输入有效的邮箱地址"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.length < 6) {
            binding.tilPassword.error = "密码至少6位"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "两次密码不一致"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }
}