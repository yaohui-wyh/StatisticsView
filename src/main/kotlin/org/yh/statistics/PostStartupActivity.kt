package org.yh.statistics

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Disposer
import org.yh.statistics.StatisticsAction.PROJECT_CLOSED
import org.yh.statistics.StatisticsAction.PROJECT_OPENED
import org.yh.statistics.listener.MyProjectWatcher


class PostStartupActivity : StartupActivity.DumbAware, Disposable {

    override fun runActivity(project: Project) {
        if (project.isOpen) {
            MyProjectWatcher.handleProjectEvent(project, PROJECT_OPENED)
            Disposer.register(project) {
                MyProjectWatcher.handleProjectEvent(project, PROJECT_CLOSED)
                runInEdt { project.service<StatisticsData>().writeEventsToLog() }
            }
        }
    }

    override fun dispose() {
    }
}