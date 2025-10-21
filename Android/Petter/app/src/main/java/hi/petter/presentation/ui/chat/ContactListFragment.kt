package hi.petter.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import hi.petter.databinding.FragmentContactListBinding
import hi.petter.domain.model.Contact
import hi.petter.presentation.ui.chat.adapter.ChatPreviewAdapter

class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private lateinit var contactAdapter: ChatPreviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        observeViewModel()
    }

    private fun initRecyclerView() {
        contactAdapter = ChatPreviewAdapter { contact ->
            // 跳转到聊天界面
            Toast.makeText(requireContext(), "聊天 ${contact.getDisplayName()}", Toast.LENGTH_SHORT).show()
        }

        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactAdapter
        }
    }

    private fun observeViewModel() {
        // 这里需要观察ViewModel的数据变化
        // 暂时显示空状态
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvContacts.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}