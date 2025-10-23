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

    private lateinit var groupAdapter: GroupAdapter

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

        // 设置当前用户ID
        setCurrentUserId()

        setupRecyclerView()
        setupClickListeners()
        loadGroups()
    }

    private fun setCurrentUserId() {
        // 简化处理：使用设备ID作为用户ID
        val androidId = android.provider.Settings.Secure.getString(
            requireContext().contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        val userId = "user_${androidId.take(8)}"
        android.util.Log.d("GroupFragment", "设置用户ID: $userId")
        groupRepository.setCurrentUserId(userId)
    }

    private fun setupRecyclerView() {
        // 初始化群组适配器
        groupAdapter = GroupAdapter { groupId ->
            navigateToGroupChat(groupId)
        }

        binding.rvGroups!!.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter
        }
    }

    private fun navigateToGroupChat(groupId: String) {
        android.util.Log.d("GroupFragment", "尝试导航到群组聊天: $groupId")
        try {
            // 获取群组信息
            lifecycleScope.launch {
                android.util.Log.d("GroupFragment", "获取群组信息...")
                val groupResult = groupRepository.getGroup(groupId)
                android.util.Log.d("GroupFragment", "群组结果: $groupResult")
                if (groupResult.isSuccess) {
                    val group = groupResult.getOrNull()
                    android.util.Log.d("GroupFragment", "群组信息: $group")
                    if (group != null) {
                        // 直接启动 ChatActivity
                        android.util.Log.d("GroupFragment", "创建Intent启动ChatActivity...")
                        val intent = Intent(requireContext(), hi.petter.presentation.ui.message.ChatActivity::class.java).apply {
                            putExtra(hi.petter.presentation.ui.message.ChatActivity.EXTRA_USER_ID, groupId)
                            putExtra(hi.petter.presentation.ui.message.ChatActivity.EXTRA_IS_GROUP, true)
                            putExtra(hi.petter.presentation.ui.message.ChatActivity.EXTRA_CHAT_NAME, group.name)
                        }
                        android.util.Log.d("GroupFragment", "启动Activity: ${intent.component}")
                        startActivity(intent)
                    } else {
                        // 群组不存在
                        android.util.Log.w("GroupFragment", "群组不存在: $groupId")
                        android.widget.Toast.makeText(requireContext(), "群组不存在", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 获取群组失败
                    android.util.Log.e("GroupFragment", "获取群组失败", groupResult.exceptionOrNull())
                    android.widget.Toast.makeText(requireContext(), "无法获取群组信息", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GroupFragment", "导航到群组聊天失败", e)
            android.widget.Toast.makeText(requireContext(), "导航失败", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.fabAddGroup!!.setOnClickListener {
            startActivity(Intent(requireContext(), CreateGroupActivity::class.java))
        }
    }

    private fun loadGroups() {
        // 使用 Flow 来观察群组数据变化
        lifecycleScope.launch {
            try {
                android.util.Log.d("GroupFragment", "开始加载群组列表...")
                // 检查 binding 是否可用
                _binding?.let { binding ->
                    binding.progressBar.visibility = View.VISIBLE
                }

                groupRepository.observeUserGroups().collect { groups ->
                    android.util.Log.d("GroupFragment", "收到群组列表更新: $groups")
                    // 检查 binding 是否仍然可用 (Fragment 可能已被销毁)
                    _binding?.let { binding ->
                        groupAdapter.submitList(groups)

                        if (groups.isEmpty()) {
                            android.util.Log.d("GroupFragment", "群组列表为空，显示空状态")
                            binding.llEmpty.visibility = View.VISIBLE
                        } else {
                            android.util.Log.d("GroupFragment", "群组列表有 ${groups.size} 个群组")
                            binding.llEmpty.visibility = View.GONE
                        }

                        binding.progressBar.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GroupFragment", "加载群组列表失败", e)
                // 检查 binding 是否可用
                _binding?.let { binding ->
                    binding.llEmpty.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}