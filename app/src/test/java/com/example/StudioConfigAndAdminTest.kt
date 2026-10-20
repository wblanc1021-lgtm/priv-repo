package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.SubscriptionTierOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StudioConfigAndAdminTest {

    private lateinit var context: Context
    private lateinit var repository: StudioConfigRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("frameshift_studio_config_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        repository = StudioConfigRepository(context)
    }

    @Test
    fun adminLogin_verifiesRootAdminEmailAndPasskey() {
        repository.logoutAdmin()
        assertFalse(repository.adminSession.value.isAdminAuthenticated)

        // Wrong email should fail
        val invalid = repository.loginAsAdmin("intruder@example.com", "wrong-key")
        assertFalse(invalid)
        assertFalse(repository.adminSession.value.isAdminAuthenticated)

        // Root Admin email wblanc1021@gmail.com with passkey succeeds
        val success = repository.loginAsAdmin(
            StudioConfigRepository.ROOT_ADMIN_EMAIL,
            StudioConfigRepository.DEFAULT_ADMIN_PASSKEY
        )
        assertTrue(success)
        assertTrue(repository.adminSession.value.isAdminAuthenticated)
        assertEquals("wblanc1021@gmail.com", repository.adminSession.value.adminEmail)
        assertEquals(SubscriptionTierOption.ADMIN_FOUNDER, repository.subscriptionState.value.selectedTier)
    }

    @Test
    fun stagedSubscriptions_notImplementedUntilTestedOrAdmin() {
        repository.logoutAdmin()
        repository.selectStagedSubscription(SubscriptionTierOption.NEURAL_DIRECTOR)

        val stateAfterStage = repository.subscriptionState.value
        assertEquals(SubscriptionTierOption.NEURAL_DIRECTOR, stateAfterStage.stagedTier)
        assertTrue(stateAfterStage.isStagedUntilTested)
        assertFalse(stateAfterStage.isBillingImplemented)

        // Attempting to implement before runtime test when not admin should be blocked
        val prematureActivate = repository.toggleImplementTestedSubscription(true)
        assertFalse(prematureActivate)
        assertFalse(repository.subscriptionState.value.isBillingImplemented)
    }

    @Test
    fun apiAndConnectionSettings_persistUpdatesCleanly() {
        repository.updateApiConfig(
            geminiKey = "AIzaSyCustomTestKey999",
            veoKey = "veo_live_test_123",
            elevenLabsKey = "xi_voice_777",
            selectedModel = "gemini-4-argon",
            selectedVideo = "veo-3.1-cinema"
        )
        val api = repository.apiConfig.value
        assertEquals("AIzaSyCustomTestKey999", api.geminiApiKeyOverride)
        assertEquals("veo_live_test_123", api.veoVideoApiKey)
        assertEquals("xi_voice_777", api.elevenLabsVoiceKey)

        repository.updateConnectionConfig(
            cloudEndpoint = "wss://custom.frameshift.ai/sync",
            lanIp = "192.168.1.210",
            lanPort = "9095",
            protocol = "QUIC / HTTP3 Zero-RTT"
        )
        val conn = repository.connectionConfig.value
        assertEquals("wss://custom.frameshift.ai/sync", conn.cloudSyncEndpoint)
        assertEquals("192.168.1.210", conn.lanWorkstationIp)
        assertEquals("9095", conn.lanWorkstationPort)
        assertEquals("QUIC / HTTP3 Zero-RTT", conn.transportProtocol)
    }

    @Test
    fun systemBus_andRecastEngine_integrateAcrossModules() {
        repository.recordOptimizedPrompt("High-contrast cyberpunk neon alley tracking shot")
        repository.recordSynthesizedVeoClip("Veo 3.1 Master (1080p 16:9)")
        repository.recordSynthesizedNanoPortrait("Elena (Nano 2.1 4K)")

        val bus = repository.systemBus.value
        assertEquals("High-contrast cyberpunk neon alley tracking shot", bus.lastOptimizedPrompt)
        assertEquals("Veo 3.1 Master (1080p 16:9)", bus.lastSynthesizedVeoClip)
        assertEquals("Elena (Nano 2.1 4K)", bus.lastSynthesizedNanoPortrait)

        // Verify RecastEngine honors Root Admin unlimited credits override and marks staged plan verified
        repository.loginAsAdmin(
            StudioConfigRepository.ROOT_ADMIN_EMAIL,
            StudioConfigRepository.DEFAULT_ADMIN_PASSKEY
        )
        val engine = com.example.service.recast.RecastEngine(configRepo = repository)
        val project = com.example.data.model.RecastProject(
            id = "proj_integrated_1",
            title = "Integrated Pipeline Test",
            sourceVideoName = bus.lastSynthesizedVeoClip,
            primaryReferenceName = bus.lastSynthesizedNanoPortrait,
            variantsCount = 2,
            qualityPreset = "max"
        )
        val (_, estCredits) = engine.estimateGenerationTimeAndCredits(project)
        assertEquals(0, estCredits) // Root Admin unlimited override -> 0 credits

        repository.markPipelineVerifiedFromRun(project.title, 8)
        assertTrue(repository.subscriptionState.value.isRuntimeTested)
        assertTrue(repository.toggleImplementTestedSubscription(true))
        assertTrue(repository.subscriptionState.value.isBillingImplemented)
    }
}

