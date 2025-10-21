package hi.petter.domain.usecase

import hi.petter.domain.model.Message
import hi.petter.domain.repository.IMessageRepository
import kotlinx.coroutines.flow.Flow

class GetMessagesUseCase(
    private val messageRepository: IMessageRepository
) {
    operator fun invoke(userId: String, otherUserId: String): Flow<List<Message>> {
        return messageRepository.getChatMessages(userId, otherUserId)
    }
}