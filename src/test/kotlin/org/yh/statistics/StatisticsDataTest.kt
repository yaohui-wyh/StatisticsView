package org.yh.statistics

import com.intellij.mock.MockVirtualFile
import com.intellij.openapi.components.service
import com.intellij.testFramework.LightPlatform4TestCase
import org.apache.commons.lang3.RandomUtils
import org.junit.Before
import org.junit.Test
import org.yh.statistics.StatisticsAction.FILE_CLOSED
import org.yh.statistics.StatisticsAction.FILE_OPENED
import org.yh.statistics.model.StatisticsEvent
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

class StatisticsDataTest : LightPlatform4TestCase() {

    private lateinit var instance: StatisticsData
    private lateinit var tags: Map<String, String>
    private lateinit var fileUri: String

    @Before
    fun before() {
        // StatisticsData is project-wide singleton, and keeps its internal state during each test methods
        // We initiated a new instance before each testcase
        instance = project.service()
        tags = mapOf()
        fileUri = MockVirtualFile(false, "test.txt").relativeUrl(project)
    }

    @Test
    fun `addEvents & clearEvents`() {
        val e1 = StatisticsEvent(System.currentTimeMillis(), FILE_OPENED, fileUri, tags)
        val e2 = StatisticsEvent(System.currentTimeMillis(), FILE_CLOSED, fileUri, tags)
        instance.addEvents(listOf(e1, e2))

        assertEquals("2 events with fileUri added", 2, instance.events.filter { it.file.isNotBlank() }.size)
        assertEquals("1 fileResult aggregated", 1, instance.resultMap.size)
        assertNotNull("aggregated fileResult should match fileUri key", instance.resultMap[fileUri])

        instance.clearData()
        assertTrue("events should be empty after clearData()", instance.events.isEmpty())
        assertTrue("resultMap should be empty after clearData()", instance.resultMap.isEmpty())
    }

    @Test
    fun `add random events & calculate duration`() {
        repeat((0..10).count()) {
            val minusDay = RandomUtils.nextLong(1, 5)
            val events = generateRandomEvents(RandomUtils.nextInt(2, 30), minusDay)
            instance.addEvents(events)
            val result = instance.getFileAggregateResult(fileUri)
            assertTrue(
                "[round $it]: duration=${result!!.totalInMillis}ms should not exceed $minusDay days",
                result.totalInMillis in 1..Duration.ofDays(minusDay).toMillis()
            )
            assertTrue(
                "[round $it]: openCounts=${result.openCounts} should not exceed ${events.size}",
                result.openCounts in 1..events.size
            )
            // manually clean data (lightTest's logFile might be failed to read)
            instance.events.clear()
            instance.resultMap.clear()
        }
    }

    /**
     * Generate randomEvents of size count, with ts in range (now-startTimeMinusDay, now)
     *
     * Event[0] is a FILE_OPENED event, and Event[size-1] is a FILE_CLOSED event.
     * Other events' action & ts are random generated
     */
    private fun generateRandomEvents(size: Int, startTimeMinusDay: Long = 1L): List<StatisticsEvent> {
        val startTime = LocalDateTime.now().minusDays(startTimeMinusDay).toMillis()
        val closeTime = LocalDateTime.now().toMillis()
        var currentTime = startTime

        return (0 until size).map {
            when (it) {
                0 -> StatisticsEvent(startTime, FILE_OPENED, fileUri, tags)
                size - 1 -> StatisticsEvent(closeTime, FILE_CLOSED, fileUri, tags)
                else -> {
                    currentTime = RandomUtils.nextLong(currentTime, closeTime)
                    StatisticsEvent(currentTime, StatisticsAction.values().random(), fileUri, tags)
                }
            }
        }
    }
}

fun LocalDateTime.toMillis() = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()