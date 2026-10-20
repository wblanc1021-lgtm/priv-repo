package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DeviceNode
import com.example.data.model.HandoffSession
import com.example.data.model.RecastProject
import com.example.data.model.SyncState
import com.example.data.model.WorkspaceViewMode
import com.example.data.repository.WorkspaceHandoffRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorkspaceHandoffTest {

    private lateinit var context: Context
    private lateinit var repository: WorkspaceHandoffRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        repository = WorkspaceHandoffRepository(context)
    }

    @Test
    fun testDefaultDeviceRegistryHasTriPlatformNodes() = runBlocking {
        val devices = repository.devices.first()
        assertTrue("Device registry should not be empty", devices.isNotEmpty())
        assertEquals(3, devices.size)

        val phone = devices.find { it.type == "phone" }
        assertNotNull("Phone node must exist", phone)
        assertTrue("Phone should be current device", phone!!.isCurrentDevice)
        assertTrue("Phone must be online", phone.isOnline)

        val computer = devices.find { it.type == "computer" }
        assertNotNull("Computer node must exist", computer)
        assertFalse("Computer should not be current device", computer!!.isCurrentDevice)
        assertTrue("Computer must be online", computer.isOnline)

        val web = devices.find { it.type == "web" }
        assertNotNull("Web node must exist", web)
        assertTrue("Web node must be online", web!!.isOnline)
    }

    @Test
    fun testSendAndAcceptHandoffBeam() = runBlocking {
        val devices = repository.devices.first()
        val phone = devices.find { it.type == "phone" }!!
        val computer = devices.find { it.type == "computer" }!!

        val testProject = RecastProject(
            id = "proj_test_beam",
            title = "Cyberpunk Neo-Tokyo",
            characterStrength = 92L,
            qualityPreset = "max",
            voiceName = "Antoni"
        )

        var beamSent = false
        repository.sendHandoffBeam(
            sourceDevice = phone,
            targetDevice = computer,
            project = testProject
        ) {
            beamSent = true
        }

        // Give coroutine a brief tick
        kotlinx.coroutines.delay(800)
        assertTrue("Beam callback should fire", beamSent)

        val session = repository.activeHandoff.first()
        assertNotNull("Active handoff session must exist after beam", session)
        assertEquals("Cyberpunk Neo-Tokyo", session?.projectTitle)
        assertEquals(92L, session?.characterStrength)
        assertEquals("max", session?.qualityPreset)
        assertEquals("Antoni", session?.voiceName)
        assertEquals(phone.id, session?.sourceDeviceId)
        assertEquals(computer.id, session?.targetDeviceId)

        // Test accepting handoff
        var acceptedSession: HandoffSession? = null
        repository.acceptHandoff { accepted ->
            acceptedSession = accepted
        }

        assertNotNull("Accepted session must be delivered to receiver", acceptedSession)
        assertEquals("Cyberpunk Neo-Tokyo", acceptedSession?.projectTitle)

        // Test dismissing handoff
        repository.dismissHandoff()
        assertNull("Active handoff must be cleared on dismiss", repository.activeHandoff.first())
    }

    @Test
    fun testPairNewDeviceNode() = runBlocking {
        val initialCount = repository.devices.first().size
        var pairCompleted = false

        repository.pairNewDevice(
            name = "Linux VFX Server",
            type = "computer",
            pairingCode = "FS-9911"
        ) {
            pairCompleted = true
        }

        kotlinx.coroutines.delay(1200)
        assertTrue("Pairing callback should fire", pairCompleted)

        val updatedDevices = repository.devices.first()
        assertEquals(initialCount + 1, updatedDevices.size)

        val linuxNode = updatedDevices.find { it.name == "Linux VFX Server" }
        assertNotNull("New paired node must be present in registry", linuxNode)
        assertEquals("computer", linuxNode?.type)
        assertTrue("New paired node should be online", linuxNode?.isOnline == true)
    }

    @Test
    fun testWorkspaceViewModesEnumIntegrity() {
        assertEquals(4, WorkspaceViewMode.values().size)
        assertTrue(WorkspaceViewMode.values().contains(WorkspaceViewMode.UNIFIED))
        assertTrue(WorkspaceViewMode.values().contains(WorkspaceViewMode.PHONE))
        assertTrue(WorkspaceViewMode.values().contains(WorkspaceViewMode.COMPUTER))
        assertTrue(WorkspaceViewMode.values().contains(WorkspaceViewMode.WEB))
    }

    @Test
    fun testSyncStateTransitions() = runBlocking {
        val initialSyncState = repository.syncState.first()
        assertEquals(SyncState.SYNCED, initialSyncState)

        var syncDone = false
        repository.triggerSync {
            syncDone = true
        }

        kotlinx.coroutines.delay(1100)
        assertTrue("Sync onComplete callback should trigger", syncDone)
        assertEquals(SyncState.SYNCED, repository.syncState.first())
    }
}
