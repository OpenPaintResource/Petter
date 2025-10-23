package hi.petter.presentation.ui.message

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: MessageAdapter

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_IS_GROUP = "extra_is_group"
        const val EXTRA_CHAT_NAME = "extra_chat_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageAdapter = MessageAdapter()
        extractIntentData()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadMessages()

        viewModel.connectMqttIfNeeded()
    }

    private fun extractIntentData() {
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
        val isGroup = intent.getBooleanExtra(EXTRA_IS_GROUP, false)
        val chatName = intent.getStringExtra(EXTRA_CHAT_NAME) ?: ""

        viewModel.initializeChat(userId, isGroup, chatName)

        binding.toolbar.title = chatName
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        viewModel.disconnectMqtt()
    }
}