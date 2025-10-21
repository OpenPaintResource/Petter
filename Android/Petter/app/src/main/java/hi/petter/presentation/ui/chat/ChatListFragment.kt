package hi.petter.presentation.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import hi.petter.databinding.FragmentChatListBinding
import hi.petter.domain.model.Contact
import hi.petter.presentation.ui.chat.adapter.ChatPreviewAdapter
import hi.petter.presentation.ui.auth.LoginActivity
import hi.petter.utils.Constants

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatPreviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        observeViewModel()
    }

    private fun initRecyclerView() {
        chatAdapter = ChatPreviewAdapter { contact ->
            // 跳转到聊天界面
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra(Constants.EXTRA_USER_ID, contact.user.id)
                putExtra(Constants.EXTRA_USER_NAME, contact.getDisplayName())
            }
            startActivity(intent)
        }

        binding.rvChatList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        binding.fabNewChat.setOnClickListener {
            // 打开新建聊天界面
            // TODO: 实现新建聊天功能
        }
    }

    private fun observeViewModel() {
        // 这里需要观察ViewModel的数据变化
        // 暂时显示空状态
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvChatList.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}