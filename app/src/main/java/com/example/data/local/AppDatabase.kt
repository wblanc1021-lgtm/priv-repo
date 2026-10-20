package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ProjectSyncDao
import com.example.data.local.dao.UserLoginDao
import com.example.data.local.entity.ProjectSyncStateEntity
import com.example.data.local.entity.UserLoginRecordEntity

@Database(
    entities = [
        ProjectSyncStateEntity::class,
        UserLoginRecordEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectSyncDao(): ProjectSyncDao
    abstract fun userLoginDao(): UserLoginDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "frameshift_workspace.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
