package hi.petter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hi.petter.data.conversation.ConversationManager
import hi.petter.data.repository.GroupRepositoryImpl
import hi.petter.data.repository.MessageRepositoryImpl
import hi.petter.data.local.database.MessageDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConversationModule {

    @Provides
    @Singleton
    fun provideConversationManager(
        messageDatabase: MessageDatabase,
        groupRepository: GroupRepositoryImpl,
        messageRepository: MessageRepositoryImpl
    ): ConversationManager {
        return ConversationManager(messageDatabase, groupRepository, messageRepository)
    }
}