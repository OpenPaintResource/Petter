package hi.petter.presentation.ui.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import hi.petter.R
import hi.petter.databinding.ActivityChatBinding
import hi.petter.presentation.ui.chat.adapter.MessageAdapter
import hi.petter.utils.Constants

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter

    private var otherUserId: String = ""
    private var otherUserName: String = ""
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的参数
        getIntentData()

        initToolbar()
        initViews()
        // observeViewModel()
    }

    private fun getIntentData() {
        otherUserId = intent.getStringExtra(Constants.EXTRA_USER_ID) ?: ""
        otherUserName = intent.getStringExtra(Constants.EXTRA_USER_NAME) ?: "未知用户"
        currentUserId = getCurrentUserId()
    }

    private fun getCurrentUserId(): String {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getString("user_id", "") ?: ""
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = otherUserName
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        messageAdapter = MessageAdapter()
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // TODO: 发送正在输入状态
            }
        })

        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage()
            }
            true
        }

        binding.btnAttach.setOnClickListener {
            Toast.makeText(this@ChatActivity, "附件功能待开发", Toast.LENGTH_SHORT).show()
        }

        binding.btnEmoji.setOnClickListener {
            Toast.makeText(this@ChatActivity, "表情功能待开发", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage() {
        val content = binding.etMessage.text.toString().trim()
        if (content.isNotEmpty()) {
            // TODO: 实现消息发送逻辑
            Toast.makeText(this, "发送: $content", Toast.LENGTH_SHORT).show()
            binding.etMessage.text?.clear()
        }
    }

    private fun observeViewModel() {
        // TODO: 实现ViewModel观察
        // viewModel.messages.observe(this) { messages ->
        //     messageAdapter.submitList(messages)
        // }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_user_info -> {
                // TODO: 显示用户信息
                true
            }
            R.id.action_clear_history -> {
                // TODO: 清空聊天记录
                true
            }
            R.id.action_mute_notifications -> {
                // TODO: 静音通知
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}