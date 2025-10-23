package hi.petter.presentation.ui.message

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hi.petter.databinding.ActivityChatBinding
import hi.petter.presentation.ui.message.adapter.MessageAdapter
import hi.petter.presentation.ui.message.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var messageAdapter: MessageAdapter

    // Intent 数据
    private var intentUserId: String = ""
    private var intentIsGroup: Boolean = false
    private var intentChatName: String = ""

    // 延迟初始化标志
    private var isViewModelInitialized = false

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_IS_GROUP = "extra_is_group"
        const val EXTRA_CHAT_NAME = "extra_chat_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 先提取Intent数据，但不立即初始化ViewModel
        intentUserId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
        intentIsGroup = intent.getBooleanExtra(EXTRA_IS_GROUP, false)
        intentChatName = intent.getStringExtra(EXTRA_CHAT_NAME) ?: ""

        // 设置Toolbar
        binding.toolbar.title = intentChatName
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        messageAdapter = MessageAdapter()
        setupRecyclerView()
        setupClickListeners()

        // 延迟初始化ViewModel到onResume中，确保Hilt完全准备就绪
    }

    override fun onResume() {
        super.onResume()

        // 只在第一次onResume时初始化ViewModel
        if (!isViewModelInitialized) {
            initializeViewModel()
            isViewModelInitialized = true
        }
    }

    private fun initializeViewModel() {
        try {
            android.util.Log.d("ChatActivity", "初始化ViewModel - userId: $intentUserId, isGroup: $intentIsGroup, chatName: $intentChatName")

            // 手动获取ViewModel
            viewModel = androidx.lifecycle.ViewModelProvider(this)[ChatViewModel::class.java]
            android.util.Log.d("ChatActivity", "ViewModel获取完成")

            // 现在可以安全地初始化ViewModel了
            viewModel.initializeChat(intentUserId, intentIsGroup, intentChatName)
            android.util.Log.d("ChatActivity", "ViewModel初始化完成")

            observeViewModel()
            android.util.Log.d("ChatActivity", "观察者设置完成")

            loadMessages()
            android.util.Log.d("ChatActivity", "开始加载消息")

            viewModel.connectMqttIfNeeded()
            android.util.Log.d("ChatActivity", "MQTT连接检查完成")
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "初始化ViewModel失败", e)
            android.widget.Toast.makeText(this, "初始化聊天失败: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish() // 关闭Activity
        }
    }

    private fun setupRecyclerView() {
        binding.rvMessages!!.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSend!!.setOnClickListener {
            val message = binding.etMessage!!.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
            }
        }

        binding.btnAttach!!.setOnClickListener {
            // TODO: 实现文件/图片发送
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            messageAdapter.submitList(messages)
            // 滚动到底部
            if (messages.isNotEmpty()) {
                binding.rvMessages!!.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar!!.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSend!!.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                // 显示错误消息
                viewModel.clearError()
            }
        }

        viewModel.messageSent.observe(this) { sent ->
            if (sent) {
                binding.etMessage.text?.clear()
                viewModel.clearMessageSent()
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            viewModel.loadMessages()
        }
    }

    private fun sendMessage(content: String) {
        lifecycleScope.launch {
            viewModel.sendMessage(content)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        // 只有在ViewModel已经初始化的情况下才断开MQTT连接
        if (isViewModelInitialized) {
            try {
                viewModel.disconnectMqtt()
            } catch (e: Exception) {
                // 忽略销毁时的异常，可能是Hilt已经不可用
            }
        }
    }
}