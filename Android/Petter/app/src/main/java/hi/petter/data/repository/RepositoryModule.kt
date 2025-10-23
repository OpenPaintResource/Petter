package hi.petter.data.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hi.petter.domain.repository.IContactRepository
import hi.petter.domain.repository.IGroupRepository
import hi.petter.domain.repository.IMessageRepository

/**
 * Repository相关的Hilt模块
 * 绑定Repository接口和实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMessageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ): IMessageRepository

    @Binds
    abstract fun bindContactRepository(
        contactRepositoryImpl: ContactRepositoryImpl
    ): IContactRepository

    @Binds
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): IGroupRepository
}