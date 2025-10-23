package hi.petter.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hi.petter.databinding.FragmentGroupListBinding
import hi.petter.presentation.ui.group.CreateGroupActivity
import hi.petter.presentation.ui.group.adapter.GroupAdapter
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 群组列表Fragment
 */
@AndroidEntryPoint
class GroupFragment : Fragment() {

    private var _binding: FragmentGroupListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var groupAdapter: GroupAdapter

    @Inject
    lateinit var groupRepository: hi.petter.data.repository.GroupRepositoryImpl

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        loadGroups()
    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter { groupId ->
            (activity as? MainActivity)?.openChat(groupId, isGroup = true)
        }

        binding.rvGroups!!.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddGroup!!.setOnClickListener {
            startActivity(Intent(requireContext(), CreateGroupActivity::class.java))
        }
    }

    private fun loadGroups() {
        lifecycleScope.launch {
            try {
                binding.progressBar!!.visibility = View.VISIBLE

                val groupsResult = groupRepository.getUserGroups()
                if (groupsResult.isSuccess) {
                    val groups = groupsResult.getOrNull() ?: emptyList()
                    groupAdapter.submitList(groups)

                    if (groups.isEmpty()) {
                        binding.llEmpty!!.visibility = View.VISIBLE
                    } else {
                        binding.llEmpty!!.visibility = View.GONE
                    }
                } else {
                    // 处理错误
                    binding.llEmpty!!.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.llEmpty!!.visibility = View.VISIBLE
            } finally {
                binding.progressBar!!.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}