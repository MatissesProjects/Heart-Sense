package com.heart.sense.data

import com.heart.sense.data.db.InterventionDao
import com.heart.sense.data.db.TypeScore
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InterventionRepositoryTest {

    private lateinit var interventionDao: InterventionDao
    private lateinit var repository: InterventionRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        interventionDao = mockk(relaxed = true)
        repository = InterventionRepository(interventionDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startIntervention inserts intervention and returns id`() = runTest {
        coEvery { interventionDao.insert(any()) } returns 123L
        
        val id = repository.startIntervention("Breathing", "Noise", 90, 30f, "v1")
        
        assertEquals(123L, id)
        coVerify { interventionDao.insert(match {
            it.type == "Breathing" && it.trigger == "Noise" && it.startHr == 90 && it.visitId == "v1"
        }) }
    }

    @Test
    fun `getRecommendation returns best intervention for trigger`() = runTest {
        coEvery { interventionDao.getBestInterventionsForTrigger("Noise") } returns listOf(TypeScore("Deep Breathing", 10.0f))
        
        val result = repository.getRecommendation("Noise")
        
        assertEquals("Deep Breathing", result)
    }

    @Test
    fun `getRecommendation returns default when no candidates found`() = runTest {
        coEvery { interventionDao.getOverallBestInterventions() } returns emptyList()
        
        val result = repository.getRecommendation(null)
        
        assertEquals("Box Breathing", result)
    }
}
