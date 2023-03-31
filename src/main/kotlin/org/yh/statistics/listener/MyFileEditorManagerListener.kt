package org.yh.statistics.listener

import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import org.yh.statistics.StatisticsAction.FILE_CLOSED
import org.yh.statistics.StatisticsAction.FILE_OPENED
import org.yh.statistics.StatisticsTag.FILE_LINE_OF_CODE
import org.yh.statistics.model.StatisticsEvent
import org.yh.statistics.model.addEvents
import org.yh.statistics.refreshView
import org.yh.statistics.relativeUrl
import org.yh.statistics.shouldHandleStatistics


/**
 * Listen to Editor fileOpen / fileClose event
 */
class MyFileEditorManagerListener(val project: Project) : FileEditorManagerListener {

    private fun buildStatisticsEvents(project: Project, event: FileEditorManagerEvent): List<StatisticsEvent> {
        val oldEditor = (event.oldEditor as? TextEditor)?.editor
        val oldDocument = oldEditor?.document
        val closeEvent = event.oldFile?.let {
            if (it.shouldHandleStatistics(project)) {
                StatisticsEvent(
                    action = FILE_CLOSED,
                    file = it.relativeUrl(project),
                    tags = mapOf(FILE_LINE_OF_CODE.name to oldDocument?.lineCount.toString())
                )
            } else null
        }
        val newEditor = (event.newEditor as? TextEditor)?.editor
        val newDocument = newEditor?.document
        val openEvent = event.newFile?.let {
            if (it.shouldHandleStatistics(project)) {
                StatisticsEvent(
                    action = FILE_OPENED,
                    file = it.relativeUrl(project),
                    tags = mapOf(FILE_LINE_OF_CODE.name to newDocument?.lineCount.toString())
                )
            } else null
        }
        return listOfNotNull(closeEvent, openEvent)
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        addEvents(project) { buildStatisticsEvents(project, event) }
        project.refreshView()
    }
}