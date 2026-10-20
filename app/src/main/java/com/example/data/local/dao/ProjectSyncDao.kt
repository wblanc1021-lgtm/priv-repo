package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProjectSyncStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectSyncDao {

    @Query("SELECT * FROM project_sync_states ORDER BY last_modified DESC")
    fun observeAllSyncStates(): Flow<List<ProjectSyncStateEntity>>

    @Query("SELECT * FROM project_sync_states WHERE project_id = :projectId")
    fun observeSyncState(projectId: String): Flow<ProjectSyncStateEntity?>

    @Query("SELECT * FROM project_sync_states WHERE device_id = :deviceId ORDER BY last_modified DESC")
    fun observeSyncStatesByDevice(deviceId: String): Flow<List<ProjectSyncStateEntity>>

    @Query("SELECT * FROM project_sync_states WHERE project_id = :projectId LIMIT 1")
    suspend fun getSyncState(projectId: String): ProjectSyncStateEntity?

    @Query("SELECT * FROM project_sync_states ORDER BY last_modified DESC")
    suspend fun getAllSyncStates(): List<ProjectSyncStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(syncState: ProjectSyncStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(syncStates: List<ProjectSyncStateEntity>)

    @Update
    suspend fun update(syncState: ProjectSyncStateEntity)

    @Query("UPDATE project_sync_states SET sync_status = :status, last_modified = :timestamp, last_modified_formatted = :formatted, device_id = :deviceId WHERE project_id = :projectId")
    suspend fun updateSyncStatus(
        projectId: String,
        status: String,
        timestamp: Long,
        formatted: String,
        deviceId: String
    )

    @Query("DELETE FROM project_sync_states WHERE project_id = :projectId")
    suspend fun deleteSyncState(projectId: String)

    @Query("DELETE FROM project_sync_states")
    suspend fun clearAll()
}
