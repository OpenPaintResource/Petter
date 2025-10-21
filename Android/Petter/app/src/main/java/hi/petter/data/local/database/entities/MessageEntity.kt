package hi.petter.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hi.petter.domain.model.Message

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "from_user")
    val from: String,

    @ColumnInfo(name = "to_user")
    val to: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "metadata")
    val metadata: String = "{}"
) {
    fun toDomainModel(): Message {
        return Message(
            id = id,
            type = Message.MessageType.valueOf(type),
            from = from,
            to = to,
            content = content,
            timestamp = timestamp,
            status = Message.MessageStatus.valueOf(status),
            metadata = emptyMap()
        )
    }
}

// 扩展函数用于转换
fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        type = type.name,
        from = from,
        to = to,
        content = content,
        timestamp = timestamp,
        status = status.name,
        metadata = "{}"
    )
}