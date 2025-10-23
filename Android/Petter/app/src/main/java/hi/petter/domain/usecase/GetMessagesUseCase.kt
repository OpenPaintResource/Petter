package hi.petter.domain.usecase

import hi.petter.domain.model.Message
import hi.petter.domain.repository.IMessageRepository
import kotlinx.coroutines.flow.Flow

class GetMessagesUseCase(
    private val messageRepository: IMessageRepository
) {
    operator fun invoke(userId: String, otherUserId: String): Flow<List<Message>> {
        // 获取两个用户之间的消息，这里先使用userId作为参数
        // 实际实现可能需要根据业务逻辑调整
        return messageRepository.observeMessages(userId, false)
    }
}