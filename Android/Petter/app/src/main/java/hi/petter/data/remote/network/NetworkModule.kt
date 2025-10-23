package hi.petter.data.remote.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import android.content.Context
import com.google.gson.Gson
import javax.inject.Singleton

/**
 * 网络模块
 * 提供MQTT客户端依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideSimplifiedMqttManager(@ApplicationContext context: Context, gson: Gson): SimplifiedMqttManager {
        return SimplifiedMqttManager(context, gson)
    }
}