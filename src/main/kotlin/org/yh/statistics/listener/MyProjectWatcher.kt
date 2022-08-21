package org.yh.statistics.listener

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.yh.statistics.PluginSettings
import org.yh.statistics.StatisticsAction
import org.yh.statistics.StatisticsData
import org.yh.statistics.StatisticsTag.PROJECT_NAME
import org.yh.statistics.model.StatisticsEvent

object MyProjectWatcher {

    fun handleProjectEvent(project: Project, action: StatisticsAction) {
        if (!project.service<PluginSettings>().enableLogging) return
        project.service<StatisticsData>().addEvents(
            listOf(
                StatisticsEvent(
                    System.currentTimeMillis(),
                    action,
                    "",
                    mapOf(PROJECT_NAME.name to project.name)
                )
            )
        )
    }
}