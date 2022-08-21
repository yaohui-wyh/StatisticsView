package org.yh.statistics.listener

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import org.yh.statistics.*
import org.yh.statistics.StatisticsAction.FILE_CLOSED
import org.yh.statistics.StatisticsAction.FILE_OPENED
import org.yh.statistics.StatisticsTag.FILE_LINE_OF_CODE
import org.yh.statistics.model.StatisticsEvent


class MyFileEditorManagerListener(val project: Project) : FileEditorManagerListener {

    private val settings = project.service<PluginSettings>()
    private val statisticsData = project.service<StatisticsData>()

    override fun selectionChanged(event: FileEditorManagerEvent) {
        if (!settings.enableLogging) return
        val oldEditor = (event.oldEditor as? TextEditor)?.editor
        val oldDocument = oldEditor?.document
        val closeEvent = event.oldFile?.let {
            if (it.shouldHandleStatistics(project)) {
                StatisticsEvent(
                    System.currentTimeMillis(),
                    FILE_CLOSED,
                    it.relativeUrl(project),
                    mapOf(FILE_LINE_OF_CODE.name to oldDocument?.lineCount.toString())
                )
            } else null
        }
        val newEditor = (event.newEditor as? TextEditor)?.editor
        val newDocument = newEditor?.document
        val openEvent = event.newFile?.let {
            if (it.shouldHandleStatistics(project)) {
                StatisticsEvent(
                    System.currentTimeMillis(),
                    FILE_OPENED,
                    it.relativeUrl(project),
                    mapOf(FILE_LINE_OF_CODE.name to newDocument?.lineCount.toString())
                )
            } else null
        }
        statisticsData.addEvents(listOfNotNull(closeEvent, openEvent))
        if (settings.showFileViewStatistics) project.refreshView()
    }
}