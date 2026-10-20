package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.RecastProject
import com.example.data.repository.RecastRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RecastRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun testCreateAndReadRecastProjectForAuthenticatedUser() = runBlocking {
    val uid = signInTestUser("tester_${System.currentTimeMillis()}@example.com")
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val repository = RecastRepository(firestore, context)

    val project = RecastProject(
      id = "proj_test_1",
      userId = uid,
      title = "Noir Alley Swap",
      sourceVideoName = "alley.mp4",
      characterStrength = 85L,
      qualityPreset = "high",
      status = "draft"
    )

    repository.saveProject(project)

    val list = repository.observeProjects().first()
    assertNotNull(list)
    val saved = list.find { it.id == "proj_test_1" }
    assertNotNull(saved)
    assertEquals("Noir Alley Swap", saved?.title)
  }
}
