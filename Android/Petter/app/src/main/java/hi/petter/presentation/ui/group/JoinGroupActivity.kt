package hi.petter.presentation.ui.group

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hi.petter.R

/**
 * 加入群组Activity - 占位符实现
 */
class JoinGroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_group)

        // TODO: 实现加入群组功能
        // Activity已经有ActionBar，只需要设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}