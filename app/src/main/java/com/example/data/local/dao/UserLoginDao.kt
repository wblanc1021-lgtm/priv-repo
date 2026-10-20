package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserLoginRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLoginDao {
    @Query("SELECT * FROM user_login_records ORDER BY lastLoginEpochMs DESC")
    fun observeAllLoginRecords(): Flow<List<UserLoginRecordEntity>>

    @Query("SELECT * FROM user_login_records ORDER BY lastLoginEpochMs DESC")
    suspend fun getAllLoginRecordsOnce(): List<UserLoginRecordEntity>

    @Query("SELECT * FROM user_login_records WHERE recordId = :recordId LIMIT 1")
    suspend fun getLoginRecordById(recordId: String): UserLoginRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLoginRecord(record: UserLoginRecordEntity)

    @Query("UPDATE user_login_records SET sessionStatus = 'SAVED' WHERE sessionStatus = 'ACTIVE'")
    suspend fun markAllSavedInactive()

    @Query("DELETE FROM user_login_records WHERE recordId = :recordId")
    suspend fun deleteLoginRecord(recordId: String)
}
