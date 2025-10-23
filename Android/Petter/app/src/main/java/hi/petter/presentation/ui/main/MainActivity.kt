package hi.petter.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hi.petter.R
import hi.petter.databinding.ActivityMainBinding
import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import hi.petter.data.repository.GroupRepositoryImpl
import hi.petter.presentation.ui.main.viewmodel.MainViewModel
import hi.petter.presentation.ui.message.ChatActivity
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var mqttManager: SimplifiedMqttManager

    @Inject
    lateinit var groupRepository: GroupRepositoryImpl

    // 基于MQTT的用户ID - 使用设备唯一标识
    private val currentUserId: String by lazy {
        // 使用设备ID + 时间戳生成唯一用户标识
        "user_${android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)}_${System.currentTimeMillis()}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        initializeMqtt()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                reconnectMqtt()
                true
            }
            R.id.action_settings -> {
                showUserId()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chats -> {
                    replaceFragment(MessageFragment())
                    true
                }
                R.id.nav_contacts -> {
                    replaceFragment(ContactFragment())
                    true
                }
                R.id.nav_discover -> {
                    replaceFragment(DiscoverFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(GroupFragment())
                    true
                }
                else -> false
            }
        }

        // 设置默认选中的Fragment
        binding.bottomNavigation.selectedItemId = R.id.nav_chats
        replaceFragment(MessageFragment())
    }

    private fun initializeMqtt() {
        lifecycleScope.launch {
            try {
                // 设置MQTT管理器的用户ID
                mqttManager.setUserId(currentUserId)

                // 设置群组Repository的当前用户ID
                groupRepository.setCurrentUserId(currentUserId)

                // 连接MQTT
                val connected = mqttManager.connect()
                if (connected) {
                    Toast.makeText(this@MainActivity, "MQTT连接成功", Toast.LENGTH_SHORT).show()
                    // 监听MQTT消息
                    observeMqttMessages()
                } else {
                    Toast.makeText(this@MainActivity, "MQTT连接失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "连接错误: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeMqttMessages() {
        lifecycleScope.launch {
            // 监听MQTT消息并处理
            mqttManager.messageFlow.collect { message ->
                // 处理接收到的消息
                // 可以通过广播或事件总线通知UI更新
            }
        }

        lifecycleScope.launch {
            // 监听MQTT状态更新
            mqttManager.statusFlow.collect { status ->
                // 处理状态更新
            }
        }

        lifecycleScope.launch {
            // 监听MQTT错误
            mqttManager.errorFlow.collect { error ->
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "MQTT错误: $error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    private fun reconnectMqtt() {
        lifecycleScope.launch {
            try {
                // 断开MQTT连接
                mqttManager.disconnect()

                // 重新连接
                initializeMqtt()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "重连失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showUserId() {
        Toast.makeText(this, "用户ID: $currentUserId", Toast.LENGTH_LONG).show()
    }

    /**
     * 打开聊天界面
     */
    fun openChat(userId: String, isGroup: Boolean = false) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_USER_ID, userId)
            putExtra(ChatActivity.EXTRA_IS_GROUP, isGroup)
        }
        startActivity(intent)
    }
}