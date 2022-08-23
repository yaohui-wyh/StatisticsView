package org.yh.statistics.model

import org.yh.statistics.StatisticsAction
import org.yh.statistics.StatisticsAction.*
import org.yh.statistics.delta
import org.yh.statistics.duration
import org.yh.statistics.localDate
import java.time.Duration

class FileAggregateResult(
    var openCounts: Int = 0,
    var totalInMillis: Long = 0,
    private var lastClosedTs: Timestamp = 0
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

    fun getLastViewedHintText(): String {
        var lastViewedHintText = ""
        if (lastClosedTs > 0) {
            lastClosedTs.localDate?.delta?.let { lastViewedHintText = "Last viewed: $it." }
        }
        if (openCounts > 1 && totalInMillis > 1_000) {
            val countsStr = if (openCounts > 100) "100+" else openCounts
            lastViewedHintText += " ($countsStr times, total ${totalInMillis.duration})"
        }
        return lastViewedHintText
    }
}

sealed class FocusState(var ts: Timestamp)
class FocusGained(ts: Timestamp) : FocusState(ts)
class FocusLost(ts: Timestamp) : FocusState(ts)

typealias Timestamp = Long