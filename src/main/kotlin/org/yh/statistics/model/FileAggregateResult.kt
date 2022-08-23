package org.yh.statistics.model

import org.yh.statistics.StatisticsAction.*
import org.yh.statistics.delta
import org.yh.statistics.duration
import org.yh.statistics.localDate
import org.yh.statistics.model.FileState.*
import org.yh.statistics.model.FileState.UNKNOWN

class FileAggregateResult(
    var openCounts: Int = 0,
    var totalInMillis: Long = 0,
    private var lastOpenedTs: Timestamp = 0,
    private var lastClosedTs: Timestamp = 0,
    private var state: FileState = UNKNOWN
) {

    fun accumulate(event: StatisticsEvent) {
        val prevState = state
        state = when (event.action) {
            FILE_OPENED -> FOCUS_GAINED
            IDE_ACTIVATED -> FOCUS_GAINED
            FILE_CLOSED -> FOCUS_LOST
            IDE_DEACTIVATED -> FOCUS_LOST
            else -> prevState
        }
        if (prevState == FOCUS_GAINED && state == FOCUS_LOST) {
            if (lastOpenedTs > 0) {
                totalInMillis += (event.ts - lastOpenedTs)
            }
        }
        if (prevState in listOf(UNKNOWN, FOCUS_LOST) && state == FOCUS_GAINED) {
            lastOpenedTs = event.ts
            openCounts++
        }
        if (prevState == FOCUS_GAINED && event.action == FILE_CLOSED) {
            lastClosedTs = event.ts
        }
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

enum class FileState {
    UNKNOWN, FOCUS_GAINED, FOCUS_LOST,
}

typealias Timestamp = Long