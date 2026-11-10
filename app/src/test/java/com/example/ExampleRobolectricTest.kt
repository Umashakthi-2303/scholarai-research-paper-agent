package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AiResearchRepositoryImpl
import kotlinx.coroutines.test.runTest
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
        assertEquals("ScholarAI", appName)
    }

    @Test
    fun `verify ai research repository creates structured academic draft`() = runTest {
        val repository = AiResearchRepositoryImpl()
        val result = repository.generatePaperDraft(
            topic = "Artificial Intelligence in Education",
            academicLevel = "Undergraduate",
            discipline = "Computer Science & AI"
        )

        assertTrue(result.isSuccess)
        val draft = result.getOrNull()
        assertNotNull(draft)
        draft?.let {
            assertTrue(it.academicTitle.isNotBlank())
            assertTrue(it.abstractText.isNotBlank())
            assertTrue(it.introduction.isNotBlank())
            assertTrue(it.literatureReviewGuidance.isNotBlank())
            assertTrue(it.researchObjectives.isNotEmpty())
            assertTrue(it.methodologySuggestions.isNotBlank())
            assertTrue(it.conclusion.isNotBlank())
            assertTrue(it.keywords.isNotEmpty())
            assertTrue(it.totalWordCount > 100)
        }
    }
}
