package hi.petter.presentation.ui.group

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hi.petter.R

/**
 * 群组聊天Activity - 占位符实现
 */
class GroupChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        // TODO: 实现群组聊天功能
        // Activity已经有ActionBar，只需要设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}