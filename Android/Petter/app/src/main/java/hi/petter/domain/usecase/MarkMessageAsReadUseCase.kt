package hi.petter.domain.usecase

import hi.petter.domain.repository.IMessageRepository

class MarkMessageAsReadUseCase(
    private val messageRepository: IMessageRepository
) {
    suspend operator fun invoke(messageId: String) {
        if (messageId.isBlank()) {
            throw IllegalArgumentException("消息ID不能为空")
        }
        messageRepository.markAsRead(messageId)
    }
}