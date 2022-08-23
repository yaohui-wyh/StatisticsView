package org.yh.statistics.model

import kotlinx.serialization.Serializable
import org.yh.statistics.StatisticsAction

@Serializable
data class StatisticsEvent(
    var ts: Long = 0,
    var action: StatisticsAction = StatisticsAction.UNKNOWN,
    var file: String = "",
    var tags: Map<String, String> = mapOf()
)