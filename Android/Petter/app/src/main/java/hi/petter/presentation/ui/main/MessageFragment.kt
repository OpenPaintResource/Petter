package hi.petter.presentation.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hi.petter.databinding.FragmentMessageBinding
import hi.petter.data.conversation.ConversationManager
import hi.petter.presentation.ui.main.adapter.ConversationAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 消息列表Fragment - 微信风格
 */
@AndroidEntryPoint
class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var conversationManager: ConversationManager

    private lateinit var conversationAdapter: ConversationAdapter
    private var conversationJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置初始状态
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        setupRecyclerView()
        setupClickListeners()
        loadConversations()
    }

    private fun setupRecyclerView() {
        // 创建Adapter并设置点击监听器
        conversationAdapter = ConversationAdapter { conversationId, isGroup ->
            openConversation(conversationId, isGroup)
        }

        _binding?.let { binding ->
            binding.recyclerViewConversations!!.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = conversationAdapter
            }
        }
    }

    private fun setupClickListeners() {
        // TODO: 添加搜索、添加群组等功能
    }

    private fun loadConversations() {
        conversationJob?.cancel() // 取消之前的协程
        conversationJob = lifecycleScope.launch {
            try {
                // 设置当前用户ID
                val currentUserId = getCurrentUserId()
                conversationManager.setCurrentUserId(currentUserId)

                // 观察会话列表变化
                conversationManager.getAllConversations().collect { conversations ->
                    // 检查binding是否可用（Fragment可能已被销毁）
                    _binding?.let { binding ->
                        binding.progressBar.visibility = View.GONE

                        if (conversations.isEmpty()) {
                            binding.tvEmpty.visibility = View.VISIBLE
                            binding.tvEmpty.text = "暂无聊天记录"
                        } else {
                            binding.tvEmpty.visibility = View.GONE
                            conversationAdapter.submitList(conversations)
                        }
                    }
                }
            } catch (e: Exception) {
                // 检查binding是否可用
                _binding?.let { binding ->
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = "加载失败: ${e.message}"
                }

                if (isAdded) { // 确保Fragment仍然attached到Activity
                    Toast.makeText(requireContext(), "加载会话失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openConversation(conversationId: String, isGroup: Boolean) {
        lifecycleScope.launch {
            try {
                // 标记会话为已读
                conversationManager.markConversationAsRead(conversationId, isGroup)
            } catch (e: Exception) {
                // 即使标记已读失败，也继续打开聊天
                android.util.Log.w("MessageFragment", "标记已读失败", e)
            }
        }

        // 打开聊天界面
        val mainActivity = requireActivity() as MainActivity
        mainActivity.openChat(conversationId, isGroup)
    }

    private fun getCurrentUserId(): String {
        // 获取当前用户ID - 使用设备ID
        val androidId = android.provider.Settings.Secure.getString(
            requireContext().contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        return "user_${androidId.take(8)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        conversationJob?.cancel() // 取消协程
        _binding = null
    }
}