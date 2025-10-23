package hi.petter.presentation.ui.group

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hi.petter.R
import hi.petter.databinding.ActivityCreateGroupBinding
import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 创建群组Activity
 * 简化实现，避免Hilt依赖问题
 */
class CreateGroupActivity : AppCompatActivity() {

    private lateinit var mqttManager: SimplifiedMqttManager

    // 直接在Activity中实现状态管理
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage
    private var _binding: ActivityCreateGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(_binding?.root)

        // 初始化MQTT管理器
        initMqttManager()
        initViews()
        observeData()
    }

    private fun initMqttManager() {
        // 简化初始化，直接创建MQTT管理器实例
        mqttManager = SimplifiedMqttManager(this)
    }

    private fun initViews() {
        // Activity已经有ActionBar，不需要设置SupportActionBar
        // 如果需要自定义标题，可以直接使用actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnCreate.setOnClickListener {
            val groupName = binding.etGroupName.text.toString().trim()
            val groupDescription = binding.etGroupDescription.text.toString().trim()

            createGroup(groupName, groupDescription)
        }
    }

    private fun observeData() {
        // 观察加载状态
        isLoading.observe(this) { isLoading ->
            binding.btnCreate.isEnabled = !isLoading
            binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 观察成功消息
        successMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 观察错误消息
        errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
   * 创建群组
   */
  private fun createGroup(groupName: String, groupDescription: String = "") {
      CoroutineScope(Dispatchers.Main).launch {
          if (groupName.isBlank()) {
              _errorMessage.value = "群组名称不能为空"
              return@launch
          }

          try {
              _isLoading.value = true

              // 使用MQTT管理器创建群组
              val groupId = mqttManager.createGroup(groupName)

              if (groupId.isNotEmpty()) {
                  _successMessage.value = "群组创建成功：$groupName"
              } else {
                  _errorMessage.value = "群组创建失败"
              }
          } catch (e: Exception) {
              _errorMessage.value = "创建群组时发生错误：${e.message}"
          } finally {
              _isLoading.value = false
          }
      }
  }

  /**
   * 清除消息
   */
  private fun clearMessages() {
      _errorMessage.value = ""
      _successMessage.value = ""
  }

  override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}