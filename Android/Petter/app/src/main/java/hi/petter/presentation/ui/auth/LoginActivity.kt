package hi.petter.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import hi.petter.databinding.LoginBinding
import hi.petter.presentation.ui.chat.ContactListActivity
import hi.petter.utils.Constants

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.btnLogin?.setOnClickListener {
            performLogin()
        }

        binding.tvRegister?.setOnClickListener {
            // 跳转到注册界面
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val email = binding.etEmail?.text.toString().trim()
        val password = binding.etPassword?.text.toString()

        if (validateInputs(email, password)) {
            // TODO: 实现登录逻辑
            // 暂时直接跳转到主界面
            Toast.makeText(this, "登录功能待实现", Toast.LENGTH_SHORT).show()
            navigateToMain()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail?.error = "请输入有效的邮箱地址"
            isValid = false
        } else {
            binding.tilEmail?.error = null
        }

        if (password.length < 6) {
            binding.tilPassword?.error = "密码至少6位"
            isValid = false
        } else {
            binding.tilPassword?.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar?.isVisible = show
        binding.btnLogin?.isEnabled = !show
        binding.etEmail?.isEnabled = !show
        binding.etPassword?.isEnabled = !show
    }

    private fun navigateToMain() {
        startActivity(Intent(this, ContactListActivity::class.java))
        finish()
    }
}