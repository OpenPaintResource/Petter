package hi.petter.domain.usecase

import hi.petter.domain.repository.IContactRepository

class AddContactUseCase(
    private val contactRepository: IContactRepository
) {
    suspend operator fun invoke(userId: String) {
        if (userId.isBlank()) {
            throw IllegalArgumentException("用户ID不能为空")
        }
        contactRepository.addContact(userId)
    }
}