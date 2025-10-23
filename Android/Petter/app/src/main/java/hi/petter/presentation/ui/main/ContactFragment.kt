package hi.petter.presentation.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hi.petter.databinding.FragmentContactListBinding
import hi.petter.presentation.ui.main.adapter.ContactAdapter
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 联系人列表Fragment
 */
@AndroidEntryPoint
class ContactFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var contactRepository: hi.petter.data.repository.ContactRepositoryImpl

    private lateinit var contactAdapter: ContactAdapter

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
        setupRecyclerView()
        loadContacts()
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter { userId ->
            (activity as? MainActivity)?.openChat(userId, isGroup = false)
        }

        binding.rvContacts!!.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contactAdapter
        }
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            try {
                val contactsResult = contactRepository.getAllContacts()
                if (contactsResult.isSuccess) {
                    val contacts = contactsResult.getOrNull() ?: emptyList()
                    contactAdapter.submitList(contacts)

                    if (contacts.isEmpty()) {
                        binding.tvEmpty!!.visibility = View.VISIBLE
                    } else {
                        binding.tvEmpty!!.visibility = View.GONE
                    }
                } else {
                    binding.tvEmpty!!.visibility = View.VISIBLE
                    binding.tvEmpty!!.text = "加载联系人失败"
                }
            } catch (e: Exception) {
                binding.tvEmpty!!.visibility = View.VISIBLE
                binding.tvEmpty!!.text = "加载联系人出错"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}