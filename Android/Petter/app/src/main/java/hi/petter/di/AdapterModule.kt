package hi.petter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hi.petter.presentation.ui.group.adapter.GroupAdapter
import hi.petter.presentation.ui.main.adapter.ConversationAdapter
import javax.inject.Singleton

/**
 * Adapter相关的Hilt模块
 */
@Module
@InstallIn(SingletonComponent::class)
object AdapterModule {

    @Provides
    @Singleton
    fun provideGroupAdapter(): GroupAdapter {
        // 提供默认的空实现，实际使用时会在Fragment中设置
        return GroupAdapter { groupId -> }
    }

    @Provides
    @Singleton
    fun provideConversationAdapter(): ConversationAdapter {
        // 提供默认的空实现，实际使用时会在Fragment中设置
        return ConversationAdapter { conversationId, isGroup -> }
    }
}