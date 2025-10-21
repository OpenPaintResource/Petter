package hi.petter

import android.app.Application

class PetterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化全局配置
        initInstance()
    }

    private fun initInstance() {
        // 这里可以进行全局初始化，比如：
        // - 初始化网络库
        // - 初始化数据库
        // - 初始化推送服务
        // - 初始化MQTT客户端配置
        // - 设置日志级别等
    }
}