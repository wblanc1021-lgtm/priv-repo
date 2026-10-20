package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ConversionDirectionMode
import com.example.data.model.RecastProject
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.SubscriptionTierOption
import com.example.data.repository.WorkspaceHandoffRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudioConfigAndAdminTest {

    private lateinit var context: Context
    private lateinit var configRepository: StudioConfigRepository
    private lateinit var workspaceRepository: WorkspaceHandoffRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("frameshift_studio_config_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        configRepository = StudioConfigRepository(context)
        workspaceRepository = WorkspaceHandoffRepository(context)
    }

    @Test
    fun rootAdminLogin_recognizesOwnerEmail_andUnlocksFounderTier() {
        val success = configRepository.loginAsAdmin("wblanc1021@gmail.com", "FS-ADMIN-2026")
        assertTrue("Root Admin login should succeed for wblanc1021@gmail.com", success)
        assertTrue(configRepository.adminSession.value.isAdminAuthenticated)
        assertEquals(
            SubscriptionTierOption.ADMIN_FOUNDER,
            configRepository.subscriptionState.value.selectedTier
        )

        // Verify non-owner email is strictly rejected
        configRepository.logoutAdmin()
        assertFalse(configRepository.adminSession.value.isAdminAuthenticated)
        val nonOwnerSuccess = configRepository.loginAsAdmin("guest@example.com", "FS-ADMIN-2026")
        assertFalse("Non-owner email must never unlock Root Admin", nonOwnerSuccess)
        assertFalse(configRepository.adminSession.value.isAdminAuthenticated)
    }

    @Test
    fun multiIdentityProviders_supportsAppleXEmailAndGoogleFederation() {
        // Link Apple ID
        configRepository.linkIdentityProvider("Apple", "creator@icloud.com")
        assertEquals("Apple", configRepository.adminSession.value.activeAuthProvider)
        assertTrue(configRepository.adminSession.value.linkedProviders.contains("Apple"))
        assertEquals("creator@icloud.com", configRepository.adminSession.value.activeUserHandle)

        // Link X (Twitter) handle
        configRepository.linkIdentityProvider("X", "@frameshift_x")
        assertEquals("X", configRepository.adminSession.value.activeAuthProvider)
        assertTrue(configRepository.adminSession.value.linkedProviders.contains("X"))
        assertTrue(configRepository.adminSession.value.linkedProviders.contains("Apple"))

        // Link Email for owner wblanc1021@gmail.com -> should auto-elevate owner admin
        configRepository.linkIdentityProvider("Email", "wblanc1021@gmail.com")
        assertTrue(configRepository.adminSession.value.linkedProviders.contains("Email"))
        assertTrue(
            "Owner email via Email Auth should unlock Root Admin session",
            configRepository.adminSession.value.isAdminAuthenticated
        )
    }

    @Test
    fun stagedSubscription_staysUnbilledUntilVerified() {
        configRepository.stageSubscriptionTier(SubscriptionTierOption.NEURAL_DIRECTOR)
        val stateBefore = configRepository.subscriptionState.value
        assertTrue("Subscription must remain staged before testing", stateBefore.isStagedUntilTested)
        assertFalse("Billing must not be implemented before runtime test", stateBefore.isBillingImplemented)
    }

    @Test
    fun twoWayConversionAndFidelitySliders_calculateAccurateTelemetry() {
        val project = RecastProject(
            id = "proj_test_fidelity",
            title = "Two-Way Fidelity Verification",
            resolution = "4K",
            frameRate = 60L,
            compressionLevel = 12L,
            qualityPreset = "ultra",
            characterStrength = 92L,
            sourceVideoDuration = 15L,
            conversionDirection = ConversionDirectionMode.BIDIRECTIONAL_CO_SWAP.key
        )

        assertEquals(ConversionDirectionMode.BIDIRECTIONAL_CO_SWAP, project.activeConversionMode())
        assertTrue("4K 60fps bitrate should be > 25 Mbps", project.estimatedBitrateMbps() > 25f)
        assertTrue("Estimated render time should be positive", project.estimatedRenderTimeSeconds() > 0)
        assertTrue("Estimated file size should be positive", project.estimatedFileSizeMb() > 0f)
    }

    @Test
    fun systemIntegrationBus_propagatesPromptsClipsAndEventsAcrossModules() {
        configRepository.publishOptimizedPrompt("Anamorphic 60fps neon alley recast with volumetric cyan rim")
        configRepository.publishSynthesizedVeoClip("Veo 3.1 Master (4K 60fps)")
        configRepository.publishSynthesizedNanoPortrait("Aria (Nano 2.1 4K Portrait)")

        val bus = configRepository.systemBus.value
        assertEquals("Anamorphic 60fps neon alley recast with volumetric cyan rim", bus.lastOptimizedPrompt)
        assertEquals("Veo 3.1 Master (4K 60fps)", bus.lastSynthesizedVeoClip)
        assertEquals("Aria (Nano 2.1 4K Portrait)", bus.lastSynthesizedNanoPortrait)
        assertTrue(bus.totalSystemOperations >= 4)

        // Verify Workspace Handoff Repository sync
        val shared = workspaceRepository.sharedProject.value
        assertNotNull(shared)
    }
}
