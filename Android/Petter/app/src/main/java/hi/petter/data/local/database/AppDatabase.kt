package hi.petter.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.room.Room
import hi.petter.data.local.database.dao.ContactDao
import hi.petter.data.local.database.dao.MessageDao
import hi.petter.data.local.database.dao.UserDao
import hi.petter.data.local.database.entities.ContactEntity
import hi.petter.data.local.database.entities.MessageEntity
import hi.petter.data.local.database.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ContactEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "petter_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}