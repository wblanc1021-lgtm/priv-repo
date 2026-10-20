package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CharacterCastVault
import com.example.data.model.RecastProject
import com.example.service.recast.RecastEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FrameShift", appName)
  }

  @Test
  fun `test 11Labs-tier stock character vault validity`() {
    val characters = CharacterCastVault.stockCharacters
    assertTrue("Stock characters must not be empty", characters.isNotEmpty())
    
    val rachel = characters.find { it.name == "Rachel" }
    assertNotNull("Rachel character must exist", rachel)
    assertTrue("Rachel sample dialogue must not be blank", rachel!!.sampleDialogue.isNotBlank())
    assertTrue("Recommended strength must be between 50 and 100", rachel.recommendedStrength in 50..100)

    val adam = characters.find { it.name == "Adam" }
    assertNotNull("Adam character must exist", adam)
    assertTrue("Adam must have meeting capability", adam!!.optimalForMeetings)

    val antoni = characters.find { it.name == "Antoni" }
    assertNotNull("Antoni character must exist", antoni)
    assertEquals("Corporate / Meeting", antoni!!.category)
  }

  @Test
  fun `test recast engine estimation math`() {
    val engine = RecastEngine()
    val project = RecastProject(
      id = "test_proj",
      userId = "user_1",
      title = "Test Recast",
      sourceVideoName = "Test.mp4",
      sourceVideoDuration = 10L,
      qualityPreset = "high",
      variantsCount = 2L
    )

    val (estSeconds, estCredits) = engine.estimateGenerationTimeAndCredits(project)
    assertTrue("Estimated duration should be greater than 0", estSeconds > 0)
    assertTrue("Estimated credits should be greater than 0", estCredits > 0)
  }
}

