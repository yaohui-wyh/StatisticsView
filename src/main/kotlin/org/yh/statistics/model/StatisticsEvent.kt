package org.yh.statistics.model

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.serialization.Serializable
import org.yh.statistics.PluginSettings
import org.yh.statistics.StatisticsAction
import org.yh.statistics.StatisticsAction.UNKNOWN
import org.yh.statistics.StatisticsData
import org.yh.statistics.StatisticsTag.PROJECT_NAME

@Serializable
data class StatisticsEvent(
    val ts: Long = System.currentTimeMillis(),
    val action: StatisticsAction = UNKNOWN,
    val file: String = "",
    val tags: Map<String, String> = mapOf()
)

fun addEvents(project: Project, events: List<StatisticsEvent>) {
    if (project.service<PluginSettings>().enableLogging) {
        project.service<StatisticsData>().addEvents(events)
    }
}

/**
 * add events to StatisticsData, build events only when enableLogging is true
 *
 * @param eventsBuilder: lazy evaluation of events
 */
fun addEvents(project: Project, eventsBuilder: () -> List<StatisticsEvent>) {
    if (project.service<PluginSettings>().enableLogging) {
        project.service<StatisticsData>().addEvents(eventsBuilder())
    }
}

fun addEvent(project: Project, event: StatisticsEvent) {
    addEvents(project, listOf(event))
}

/**
 * add IDE-wide, file irrelevant event
 */
fun addIDEEvent(project: Project, action: StatisticsAction) {
    addEvent(project, StatisticsEvent(action = action))
}

/**
 * add project-wide, file irrelevant event
 */
fun addProjectEvent(project: Project, action: StatisticsAction) {
    addEvent(project, StatisticsEvent(action = action, tags = mapOf(PROJECT_NAME.name to project.name)))
}