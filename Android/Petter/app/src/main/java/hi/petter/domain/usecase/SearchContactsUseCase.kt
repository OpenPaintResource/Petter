package hi.petter.domain.usecase

import hi.petter.domain.model.User
import hi.petter.domain.repository.IContactRepository

class SearchContactsUseCase(
    private val contactRepository: IContactRepository
) {
    suspend operator fun invoke(query: String): List<User> {
        return contactRepository.searchUsers(query)
    }
}