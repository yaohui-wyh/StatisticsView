package org.yh.statistics.model

import org.yh.statistics.*
import org.yh.statistics.StatisticsAction.*
import org.yh.statistics.StatisticsHintTemplateMarker.*
import java.time.Duration

data class FileAggregateResult(
    var openCounts: Int = 0,
    var totalInMillis: Long = 0,
    var lastClosedTs: Timestamp = 0
) {
    var fileState: StatisticsAction? = null
    var focusState: FocusState = FocusLost(0)
    private val openCountsUpdater = OpenCountsUpdater()
    private val totalTimeUpdater = TotalTimeUpdater()
    private val lastClosedTsUpdater = LastClosedTsUpdater()

    /**
     * Accumulates statistics for a single event
     */
    fun accumulate(event: StatisticsEvent) {
        openCountsUpdater.update(event, this)
        totalTimeUpdater.update(event, this)
        lastClosedTsUpdater.update(event, this)
    }

    /**
     * Renders a formatted string containing the statistics for this file
     */
    fun renderStatisticsHint() = formatter.format(this)
}

class OpenCountsUpdater {
    fun update(event: StatisticsEvent, result: FileAggregateResult) {
        val prevFileState = result.fileState
        if (event.action in setOf(FILE_OPENED, FILE_CLOSED)) {
            result.fileState = event.action
        }
        if (prevFileState == FILE_OPENED && result.fileState == FILE_CLOSED) {
            result.openCounts++
        }
    }
}

class TotalTimeUpdater {
    fun update(event: StatisticsEvent, result: FileAggregateResult) {
        when (val prevFocusState = result.focusState) {
            is FocusLost -> if (event.action == FILE_OPENED || (result.fileState == FILE_OPENED && event.action == IDE_ACTIVATED)) {
                result.focusState = FocusGained(event.ts)
            }
            is FocusGained -> if (event.action in setOf(FILE_CLOSED, IDE_DEACTIVATED)) {
                result.focusState = FocusLost(event.ts)
                val duration = result.focusState.ts - prevFocusState.ts
                // TODO: makes timespans max duration configurable
                result.totalInMillis += minOf(duration, Duration.ofMinutes(30).toMillis())
            }
        }
    }
}

class LastClosedTsUpdater {
    fun update(event: StatisticsEvent, result: FileAggregateResult) {
        if (event.action == FILE_CLOSED) {
            result.lastClosedTs = event.ts
        }
    }
}

class FileStatisticsHintFormatter(private val template: String) {

    fun validate(): Boolean {
        // TODO: to be used in configuration panel
        return true
    }

    fun format(result: FileAggregateResult): String {
        var text = template
        val (openCounts, totalInMillis, lastClosedTs) = result
        text = text.replace(LAST_VIEWED.pattern, if (lastClosedTs > 0) lastClosedTs.localDate?.delta.orEmpty() else "")
        text = text.replace(OPEN_COUNTS.pattern, if (openCounts > 100) "100+" else openCounts.toString())
        text = text.replace(TOTAL_TIME.pattern, totalInMillis.duration)
        return text
    }
}

val formatter = FileStatisticsHintFormatter(System.getProperty("statisticsView.hint.template", DEFAULT_HINT_TEMPLATE))

sealed class FocusState(var ts: Timestamp)
class FocusGained(ts: Timestamp) : FocusState(ts)
class FocusLost(ts: Timestamp) : FocusState(ts)

typealias Timestamp = Long