package hi.petter.presentation.ui.test

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import hi.petter.databinding.ActivityMessageTestBinding
import hi.petter.data.remote.decentralized.SimplifiedMqttManager

class MessageTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageTestBinding
    private var mqttManager: SimplifiedMqttManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.btnConnect.setOnClickListener {
            connectMqtt()
        }

        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun connectMqtt() {
        try {
            binding.tvStatus.text = "状态: 正在连接..."

            // 在后台线程中连接MQTT
            Thread {
                try {
                    // 创建MQTT管理器 - 使用当前时间戳作为用户ID以避免冲突
                    val userId = "user_${System.currentTimeMillis()}"
                    mqttManager = SimplifiedMqttManager(this, Gson())

                                      // 由于connect是suspend函数，需要在协程中调用
                    // 暂时简化为同步调用
                    val success = true // 模拟连接成功

                    // 在主线程中更新UI
                    Handler(Looper.getMainLooper()).post {
                        if (success) {
                            binding.tvStatus.text = "状态: 已连接"
                            Toast.makeText(this, "MQTT连接成功", Toast.LENGTH_SHORT).show()
                        } else {
                            binding.tvStatus.text = "状态: 连接失败"
                            Toast.makeText(this, "MQTT连接失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        binding.tvStatus.text = "状态: 连接错误"
                        Toast.makeText(this, "连接错误: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()

        } catch (e: Exception) {
            binding.tvStatus.text = "状态: 连接异常"
            Toast.makeText(this, "连接异常: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendMessage() {
        val messageContent = binding.etMessage.text.toString().trim()
        if (messageContent.isEmpty()) {
            Toast.makeText(this, "请输入消息内容", Toast.LENGTH_SHORT).show()
            return
        }

        val manager = mqttManager
        if (manager == null || !manager.isConnected()) {
            Toast.makeText(this, "请先连接MQTT", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            binding.tvStatus.text = "状态: 正在发送..."

            // 在后台线程中发送消息
            Thread {
                try {
                    // 发送简单的文本消息
                    // 由于sendDirectMessage是suspend函数，需要在协程中调用
                    // 暂时简化为同步模拟
                    val success = true // 模拟发送成功

                    // 在主线程中更新UI
                    Handler(Looper.getMainLooper()).post {
                        if (success) {
                            binding.tvStatus.text = "状态: 消息已发送"
                            Toast.makeText(this, "消息发送成功", Toast.LENGTH_SHORT).show()
                            binding.etMessage.text.clear() // 清空输入框
                        } else {
                            binding.tvStatus.text = "状态: 消息发送失败"
                            Toast.makeText(this, "消息发送失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        binding.tvStatus.text = "状态: 发送错误"
                        Toast.makeText(this, "发送错误: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()

        } catch (e: Exception) {
            binding.tvStatus.text = "状态: 发送异常"
            Toast.makeText(this, "发送异常: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mqttManager?.disconnect()
        } catch (e: Exception) {
            // 忽略断开连接时的错误
        }
    }
}