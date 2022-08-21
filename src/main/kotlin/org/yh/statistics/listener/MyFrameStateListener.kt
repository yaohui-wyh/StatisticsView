package org.yh.statistics.listener

import com.intellij.ide.FrameStateListener
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import org.yh.statistics.PluginSettings
import org.yh.statistics.StatisticsAction
import org.yh.statistics.StatisticsAction.IDE_ACTIVATED
import org.yh.statistics.StatisticsAction.IDE_DEACTIVATED
import org.yh.statistics.StatisticsData
import org.yh.statistics.model.StatisticsEvent


class MyFrameStateListener : FrameStateListener {

    override fun onFrameDeactivated() {
        ProjectManager.getInstance().openProjects.forEach { handleEvent(it, IDE_DEACTIVATED) }
    }

    override fun onFrameActivated() {
        ProjectManager.getInstance().openProjects.forEach { handleEvent(it, IDE_ACTIVATED) }
    }

    private fun handleEvent(project: Project, action: StatisticsAction) {
        if (project.service<PluginSettings>().enableLogging) {
            project.service<StatisticsData>().addEvents(
                listOf(StatisticsEvent(System.currentTimeMillis(), action, "", emptyMap()))
            )
        }
    }
}