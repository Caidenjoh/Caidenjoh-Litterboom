package com.example.litterboom.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class, Event::class, WasteCategory::class, WasteSubCategory::class,
        LoggingField::class, SubCategoryField::class, Bag::class,
        ItemPhoto::class, LoggedWaste::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun wasteDao(): WasteDao
    abstract fun bagDao(): BagDao
    abstract fun loggedWasteDao(): LoggedWasteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "litterboom_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(CoroutineScope(Dispatchers.IO)))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    seedAdminUser(database.userDao())
                }
            }
        }

        private suspend fun seedAdminUser(userDao: UserDao) {
            val existingAdmin = userDao.getUser("admin", "admin")
            if (existingAdmin == null) {
                val adminUser = User(username = "admin", password = "admin", role = "Admin")
                userDao.insertUser(adminUser)
            }
        }
    }
}
