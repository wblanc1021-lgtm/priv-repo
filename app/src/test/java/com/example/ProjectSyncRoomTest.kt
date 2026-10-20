package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.ProjectSyncDao
import com.example.data.local.entity.ProjectSyncStateEntity
import com.example.data.model.RecastProject
import com.example.data.repository.ProjectSyncRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProjectSyncRoomTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ProjectSyncDao
    private lateinit var context: Context
    private lateinit var repository: ProjectSyncRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.projectSyncDao()
        repository = ProjectSyncRepository(dao, context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveProjectSyncState() = runBlocking {
        val now = System.currentTimeMillis()
        val entity = ProjectSyncStateEntity(
            projectId = "proj_test_001",
            deviceId = "dev_phone_native",
            deviceName = "Pixel 9 Pro Mobile Studio",
            deviceType = "phone",
            lastModified = now,
            lastModifiedFormatted = "Just now",
            syncStatus = "SYNCED",
            projectTitle = "Neo Tokyo Cyber Recast",
            characterStrength = 88L,
            qualityPreset = "cinematic",
            voiceName = "Aura Noir"
        )

        dao.insertOrUpdate(entity)

        val retrieved = dao.getSyncState("proj_test_001")
        assertNotNull("Project sync state must be retrievable", retrieved)
        assertEquals("proj_test_001", retrieved?.projectId)
        assertEquals("dev_phone_native", retrieved?.deviceId)
        assertEquals("Pixel 9 Pro Mobile Studio", retrieved?.deviceName)
        assertEquals(now, retrieved?.lastModified)
        assertEquals("SYNCED", retrieved?.syncStatus)
        assertEquals("Neo Tokyo Cyber Recast", retrieved?.projectTitle)
        assertEquals(88L, retrieved?.characterStrength)
    }

    @Test
    fun testLastModifiedTimestampOrdering() = runBlocking {
        val t1 = 1000L
        val t2 = 2000L
        val t3 = 3000L

        dao.insertOrUpdate(
            ProjectSyncStateEntity(
                projectId = "p1",
                deviceId = "dev_phone",
                lastModified = t1,
                projectTitle = "Oldest"
            )
        )
        dao.insertOrUpdate(
            ProjectSyncStateEntity(
                projectId = "p3",
                deviceId = "dev_computer",
                lastModified = t3,
                projectTitle = "Newest"
            )
        )
        dao.insertOrUpdate(
            ProjectSyncStateEntity(
                projectId = "p2",
                deviceId = "dev_web",
                lastModified = t2,
                projectTitle = "Middle"
            )
        )

        val list = dao.observeAllSyncStates().first()
        assertEquals(3, list.size)
        // Must be ordered by last_modified DESC
        assertEquals("p3", list[0].projectId)
        assertEquals("p2", list[1].projectId)
        assertEquals("p1", list[2].projectId)
    }

    @Test
    fun testObserveSyncStatesByDevice() = runBlocking {
        dao.insertOrUpdate(
            ProjectSyncStateEntity(
                projectId = "p_mac_1",
                deviceId = "dev_computer_pro",
                deviceName = "Mac Studio",
                lastModified = 100L
            )
        )
        dao.insertOrUpdate(
            ProjectSyncStateEntity(
                projectId = "p_phone_1",
                deviceId = "dev_phone_native",
                deviceName = "Pixel 9",
                lastModified = 200L
            )
        )
        dao.insertOrUpdate(
            ProjectSyncStateEntity(
                projectId = "p_mac_2",
                deviceId = "dev_computer_pro",
                deviceName = "Mac Studio",
                lastModified = 300L
            )
        )

        val macProjects = dao.observeSyncStatesByDevice("dev_computer_pro").first()
        assertEquals(2, macProjects.size)
        assertTrue(macProjects.all { it.deviceId == "dev_computer_pro" })
        assertEquals("p_mac_2", macProjects[0].projectId) // Ordered DESC
        assertEquals("p_mac_1", macProjects[1].projectId)
    }

    @Test
    fun testRecordHandoffBeamedInRepository() = runBlocking {
        repository.recordHandoffBeamed(
            projectId = "proj_beam_777",
            projectTitle = "Quantum Void Recast",
            sourceDeviceId = "dev_phone_native",
            sourceDeviceName = "Pixel 9 Pro Mobile",
            targetDeviceId = "dev_computer_pro",
            targetDeviceName = "Mac Studio M3",
            characterStrength = 95L,
            qualityPreset = "ultra",
            voiceName = "Viper 8-Bit"
        )

        val retrieved = repository.getSyncState("proj_beam_777")
        assertNotNull(retrieved)
        assertEquals("proj_beam_777", retrieved?.projectId)
        assertEquals("dev_computer_pro", retrieved?.deviceId) // target device holds beamed project
        assertEquals("Mac Studio M3", retrieved?.deviceName)
        assertEquals("BEAMED", retrieved?.syncStatus)
        assertEquals("Quantum Void Recast", retrieved?.projectTitle)
        assertEquals(95L, retrieved?.characterStrength)
        assertTrue("last_modified timestamp must be recorded", (retrieved?.lastModified ?: 0L) > 0L)
    }

    @Test
    fun testRecordProjectModificationIncrementsVersion() = runBlocking {
        val project = RecastProject(
            id = "proj_edit_888",
            title = "First Version",
            characterStrength = 80L,
            qualityPreset = "high",
            voiceName = "Aura Noir"
        )

        repository.recordProject(project)
        val v1 = repository.getSyncState("proj_edit_888")
        assertNotNull(v1)
        assertEquals(1L, v1?.localVersion)

        // Modify
        val projectV2 = project.copy(
            title = "Second Version",
            characterStrength = 90L
        )
        repository.recordProject(projectV2)
        val v2 = repository.getSyncState("proj_edit_888")
        assertNotNull(v2)
        assertEquals(2L, v2?.localVersion)
        assertEquals("Second Version", v2?.projectTitle)
        assertEquals(90L, v2?.characterStrength)
        assertTrue("Newer modification timestamp", (v2?.lastModified ?: 0L) >= (v1?.lastModified ?: 0L))
    }

    @Test
    fun testSeedInitialWorkspaceStates() = runBlocking {
        val projects = listOf(
            RecastProject(id = "seed_1", title = "Seed Project 1"),
            RecastProject(id = "seed_2", title = "Seed Project 2"),
            RecastProject(id = "seed_3", title = "Seed Project 3")
        )

        repository.seedInitialWorkspaceStates(projects)

        val states = repository.allSyncStates.first()
        assertEquals(3, states.size)
        // Verify cross-device distribution
        val deviceIds = states.map { it.deviceId }.toSet()
        assertTrue("Multiple devices represented across seeds", deviceIds.size > 1)
    }
}
