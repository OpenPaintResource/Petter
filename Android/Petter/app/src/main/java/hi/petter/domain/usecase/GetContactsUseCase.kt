package hi.petter.domain.usecase

import hi.petter.domain.model.Contact
import hi.petter.domain.repository.IContactRepository

class GetContactsUseCase(
    private val contactRepository: IContactRepository
) {
    suspend operator fun invoke(): List<Contact> {
        return contactRepository.getContacts()
    }
}