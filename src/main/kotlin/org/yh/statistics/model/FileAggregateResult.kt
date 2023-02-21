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

    private var fileState: StatisticsAction? = null
    private var focusState: FocusState = FocusLost(0)

    private fun updateOpenCounts(event: StatisticsEvent) {
        val prevFileState = fileState
        if (event.action in setOf(FILE_OPENED, FILE_CLOSED)) {
            fileState = event.action
        }
        if (prevFileState == FILE_OPENED && fileState == FILE_CLOSED) {
            openCounts++
        }
    }

    private fun updateTotalTime(event: StatisticsEvent) {
        when (val prevFocusState = focusState) {
            is FocusLost -> if (event.action == FILE_OPENED || (fileState == FILE_OPENED && event.action == IDE_ACTIVATED)) {
                focusState = FocusGained(event.ts)
            }
            is FocusGained -> if (event.action in setOf(FILE_CLOSED, IDE_DEACTIVATED)) {
                focusState = FocusLost(event.ts)
                val duration = focusState.ts - prevFocusState.ts
                // TODO: makes timespans max duration configurable
                totalInMillis += minOf(duration, Duration.ofMinutes(30).toMillis())
            }
        }
    }

    private fun updateLastClosedTs(event: StatisticsEvent) {
        if (event.action == FILE_CLOSED) {
            lastClosedTs = event.ts
        }
    }

    fun accumulate(event: StatisticsEvent) {
        updateOpenCounts(event)
        updateTotalTime(event)
        updateLastClosedTs(event)
    }

    fun renderStatisticsHint() = template.render(this)
}

class StatisticsHintTemplate(private val template: String) {

    fun validate(): Boolean {
        // TODO: to be used in configuration panel
        return true
    }

    fun render(result: FileAggregateResult): String {
        var text = template
        val (openCounts, totalInMillis, lastClosedTs) = result
        text = text.replace(LAST_VIEWED.pattern, if (lastClosedTs > 0) lastClosedTs.localDate?.delta.orEmpty() else "")
        text = text.replace(OPEN_COUNTS.pattern, if (openCounts > 100) "100+" else openCounts.toString())
        text = text.replace(TOTAL_TIME.pattern, totalInMillis.duration)
        return text
    }
}

val template = StatisticsHintTemplate(System.getProperty("statisticsView.hint.template", DEFAULT_HINT_TEMPLATE))

sealed class FocusState(var ts: Timestamp)
class FocusGained(ts: Timestamp) : FocusState(ts)
class FocusLost(ts: Timestamp) : FocusState(ts)

typealias Timestamp = Long