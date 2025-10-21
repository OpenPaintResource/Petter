package hi.petter.domain.usecase

import hi.petter.data.repository.SimplifiedGroupRepository
import hi.petter.domain.model.Group
import hi.petter.domain.model.GroupMember
import kotlinx.coroutines.flow.Flow

class CreateGroupUseCase(
    private val repository: SimplifiedGroupRepository
) {
    suspend operator fun invoke(name: String, description: String = ""): String {
        return repository.createGroup(name, description)
    }
}

class JoinGroupUseCase(
    private val repository: SimplifiedGroupRepository
) {
    suspend operator fun invoke(groupId: String, nickname: String = ""): Boolean {
        return repository.joinGroup(groupId, nickname)
    }
}

class LeaveGroupUseCase(
    private val repository: SimplifiedGroupRepository
) {
    suspend operator fun invoke(groupId: String): Boolean {
        return repository.leaveGroup(groupId)
    }
}

class GetMyGroupsUseCase(
    private val repository: SimplifiedGroupRepository
) {
    operator fun invoke(): Flow<List<Group>> {
        return repository.getMyGroups()
    }
}

class GetGroupMembersUseCase(
    private val repository: SimplifiedGroupRepository
) {
    operator fun invoke(groupId: String): Flow<List<GroupMember>> {
        return repository.getGroupMembers(groupId)
    }
}

class SendGroupMessageUseCase(
    private val repository: SimplifiedGroupRepository
) {
    suspend operator fun invoke(groupId: String, content: String): Boolean {
        return repository.sendGroupMessage(groupId, content)
    }
}

class SendDirectMessageUseCase(
    private val repository: SimplifiedGroupRepository
) {
    suspend operator fun invoke(receiverId: String, content: String): Boolean {
        return repository.sendDirectMessage(receiverId, content)
    }
}