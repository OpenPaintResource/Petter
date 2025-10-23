package hi.petter.presentation.ui.group

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import hi.petter.R
import hi.petter.data.repository.GroupRepositoryImpl
import hi.petter.databinding.ActivityCreateGroupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 创建群组Activity
 * 使用Repository模式和本地持久化存储
 */
@AndroidEntryPoint
class CreateGroupActivity : AppCompatActivity() {

    @Inject
    lateinit var groupRepository: GroupRepositoryImpl

    private var _binding: ActivityCreateGroupBinding? = null
    private val binding get() = _binding!!

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(_binding?.root)

        // 设置当前用户ID
        setCurrentUserId()

        initViews()
    }

    private fun setCurrentUserId() {
        // 简化处理：使用设备ID作为用户ID
        val androidId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        val userId = "user_${androidId.take(8)}"
        android.util.Log.d("CreateGroupActivity", "设置用户ID: $userId")
        groupRepository.setCurrentUserId(userId)
    }

    private fun initViews() {
        // 设置ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "创建群组"

        binding.btnCreate.setOnClickListener {
            val groupName = binding.etGroupName.text.toString().trim()
            val groupDescription = binding.etGroupDescription.text.toString().trim()

            createGroup(groupName, groupDescription)
        }
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        binding.btnCreate.isEnabled = !loading
        binding.progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showMessage(message: String, isError: Boolean = false) {
        Toast.makeText(this, message, if (isError) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }

    private fun createGroup(groupName: String, groupDescription: String = "") {
        android.util.Log.d("CreateGroupActivity", "开始创建群组: $groupName")
        if (groupName.isBlank()) {
            showMessage("群组名称不能为空", true)
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                setLoading(true)
                android.util.Log.d("CreateGroupActivity", "调用Repository创建群组...")

                // 使用Repository创建群组，会同时保存到本地数据库和MQTT
                val result = groupRepository.createGroup(groupName, groupDescription)
                android.util.Log.d("CreateGroupActivity", "群组创建结果: $result")

                if (result.isSuccess) {
                    val createdGroup = result.getOrNull()
                    android.util.Log.d("CreateGroupActivity", "创建的群组: $createdGroup")
                    if (createdGroup != null) {
                        showMessage("群组创建成功：${createdGroup.name}")
                        android.util.Log.d("CreateGroupActivity", "群组创建成功，即将关闭Activity")
                        finish()
                    } else {
                        android.util.Log.e("CreateGroupActivity", "群组创建失败：返回数据为空")
                        showMessage("群组创建失败：返回数据为空", true)
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    android.util.Log.e("CreateGroupActivity", "群组创建失败", exception)
                    showMessage("群组创建失败：${exception?.message ?: "未知错误"}", true)
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateGroupActivity", "创建群组时发生错误", e)
                showMessage("创建群组时发生错误：${e.message}", true)
            } finally {
                setLoading(false)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}