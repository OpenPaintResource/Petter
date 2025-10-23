package hi.petter.presentation.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hi.petter.databinding.FragmentMessageBinding
import hi.petter.presentation.ui.main.adapter.ConversationAdapter
import hi.petter.presentation.ui.main.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 消息列表Fragment
 */
@AndroidEntryPoint
class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var conversationAdapter: ConversationAdapter

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
        setupRecyclerView()
        loadConversations()
    }

    private fun setupRecyclerView() {
        // Adapter已经通过依赖注入提供
        binding.recyclerViewConversations!!.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }
    }

    private fun loadConversations() {
        lifecycleScope.launch {
            // TODO: 加载会话列表
            // 这里应该从Repository获取最近的会话
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}