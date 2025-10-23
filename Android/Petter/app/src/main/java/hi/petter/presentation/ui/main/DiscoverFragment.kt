package hi.petter.presentation.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hi.petter.databinding.FragmentDiscoverGroupBinding
import hi.petter.presentation.ui.main.adapter.PublicGroupAdapter
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 发现群组Fragment
 */
@AndroidEntryPoint
class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverGroupBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var groupRepository: hi.petter.data.repository.GroupRepositoryImpl

    private lateinit var publicGroupAdapter: PublicGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadPublicGroups()
    }

    private fun setupRecyclerView() {
        publicGroupAdapter = PublicGroupAdapter { groupId ->
            // 加入群组
            lifecycleScope.launch {
                try {
                    val result = groupRepository.joinGroup(groupId)
                    if (result.isSuccess) {
                        // 加入成功，可以刷新列表或显示提示
                        loadPublicGroups()
                    }
                } catch (e: Exception) {
                    // 处理加入群组失败
                }
            }
        }

        binding.rvGroups!!.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = publicGroupAdapter
        }
    }

    private fun loadPublicGroups() {
        lifecycleScope.launch {
            try {
                binding.progressBar!!.visibility = View.VISIBLE

                val groupsResult = groupRepository.getPublicGroups()
                if (groupsResult.isSuccess) {
                    val groups = groupsResult.getOrNull() ?: emptyList()
                    publicGroupAdapter.submitList(groups)

                    if (groups.isEmpty()) {
                        binding.llEmpty!!.visibility = View.VISIBLE
                    } else {
                        binding.llEmpty!!.visibility = View.GONE
                    }
                } else {
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