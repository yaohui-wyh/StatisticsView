package org.yh.statistics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.annotations.VisibleForTesting
import org.yh.statistics.model.FileAggregateResult
import org.yh.statistics.model.StatisticsEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


/**
 * A project-level singleton that serves as a holder for aggregated statistics data.
 * This class provides thread-safe access to a list of `StatisticsEvent` objects
 * and a map that maps file URIs to `FileAggregateResult` objects.
 * It also provides functions for adding events, processing events,
 * and retrieving file-specific aggregate results.
 *
 * Its instance will be created in {@link PostStartupActivity#runActivity}.
 * Getting this service can be performed from any thread.
 */
@Service(Service.Level.PROJECT)
class StatisticsData(private val project: Project) {

    @VisibleForTesting
    val events = ContainerUtil.createConcurrentList<StatisticsEvent>()

    @VisibleForTesting
    val resultMap = ConcurrentHashMap<String, FileAggregateResult>()

    private val log = project.service<LogExporter>()
    private val scheduler = AppExecutorUtil.getAppScheduledExecutorService()
    private val app = ApplicationManager.getApplication()
    var summaryInfo = StatisticsSummaryInfo()

    init {
        // request pool thread to execute IO bound tasks
        app.executeOnPooledThread { processEvents(log.importEvents()) }
        scheduler.scheduleWithFixedDelay({
            if (project.service<PluginSettings>().enableLogging) {
                app.executeOnPooledThread { writeEventsToLog() }
            }
        }, 30, 30, TimeUnit.SECONDS)
    }

    fun getFileAggregateResult(fileUri: String) = resultMap[fileUri]

    fun isFileViewed(fileUri: String) = resultMap[fileUri]?.let { it.openCounts > 0 } ?: false

    fun addEvents(list: List<StatisticsEvent>) {
        runInEdt {
            events.addAll(list)
            processEvents(list)
        }
    }

    fun writeEventsToLog() {
        if (events.isNotEmpty()) {
            log.writeEvents(events)
            events.clear()
        }
    }

    fun clearData() {
        log.clearData()
        events.clear()
        resultMap.clear()
        summaryInfo = StatisticsSummaryInfo()
    }

    private fun processEvents(list: List<StatisticsEvent>) {
        list
            .groupBy { it.file }
            .forEach { (file, events) ->
                if (file.isNotBlank()) {
                    // for file-specific events, accumulate to pre-aggregate result
                    events.forEach { event -> resultMap.getOrPut(file) { FileAggregateResult() }.accumulate(event) }
                } else {
                    // for file-irrelevant events (e.g., IDE focus lost), add to every file's pre-aggregate result
                    events.forEach { event -> resultMap.values.forEach { it.accumulate(event) } }
                }
            }
        summaryInfo = summaryInfo.copy(eventCounts = summaryInfo.eventCounts + list.size)
    }

}

data class StatisticsSummaryInfo(val eventCounts: Long = 0)