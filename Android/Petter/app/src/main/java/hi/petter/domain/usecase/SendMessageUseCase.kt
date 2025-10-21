package hi.petter.domain.usecase

import hi.petter.domain.model.Message
import hi.petter.domain.repository.IMessageRepository

class SendMessageUseCase(
    private val messageRepository: IMessageRepository
) {
    suspend operator fun invoke(message: Message) {
        if (message.content.isBlank()) {
            throw IllegalArgumentException("消息内容不能为空")
        }
        if (message.to.isBlank()) {
            throw IllegalArgumentException("接收者ID不能为空")
        }

        messageRepository.sendMessage(message)
    }
}