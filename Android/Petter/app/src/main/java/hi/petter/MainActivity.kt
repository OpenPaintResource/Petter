package hi.petter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hi.petter.presentation.ui.auth.LoginActivity
import hi.petter.presentation.ui.chat.ContactListActivity
import hi.petter.utils.Constants

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查用户是否已登录
        if (isUserLoggedIn()) {
            startActivity(Intent(this, ContactListActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }

    private fun isUserLoggedIn(): Boolean {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getString(Constants.KEY_USER_ID, null)
        return !userId.isNullOrEmpty()
    }
}