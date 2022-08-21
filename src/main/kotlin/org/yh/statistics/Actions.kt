package org.yh.statistics

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.impl.ProjectViewImpl
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ToggleOptionAction.Option
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.SPEEDSEARCH
import com.intellij.ui.awt.RelativePoint
import org.yh.statistics.Constants.NAME
import java.awt.event.MouseEvent
import java.util.function.Function
import java.util.function.Supplier

class StatisticsViewAction : ToggleAction(Supplier { NAME }, AllIcons.Actions.ProfileBlue), DumbAware {

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.text = if (isSelected(e)) "$NAME (Logging Enabled)" else "$NAME (Logging Disabled)"
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        return project.service<PluginSettings>().enableLogging
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val context = e.dataContext
        val event = e.inputEvent as? MouseEvent ?: return
        getPopup(context).show(RelativePoint(event))
    }
}

open class MyToggleOptionAction(supplier: (project: Project) -> Option) : ToggleOptionAction(Function { event ->
    val project = event.project ?: return@Function null
    val view = if (project.isDisposed) null else ProjectView.getInstance(project)
    if (view is ProjectViewImpl) supplier(project) else null
}), DumbAware

val enableLoggingAction = MyToggleOptionAction { project ->
    object : Option {
        override fun getName() = "Enable Logging"
        override fun isSelected() = project.service<PluginSettings>().enableLogging
        override fun setSelected(selected: Boolean) {
            project.service<PluginSettings>().enableLogging = selected
            project.refreshView()
        }
    }
}

val showFileViewStatisticsAction = MyToggleOptionAction { project ->
    object : Option {
        override fun getName() = "Show File View Statistics"
        override fun isSelected() = project.service<PluginSettings>().showFileViewStatistics
        override fun setSelected(selected: Boolean) {
            project.service<PluginSettings>().showFileViewStatistics = selected
            project.refreshView()
        }
    }
}

val showDirectoryStatisticsAction = MyToggleOptionAction { project ->
    object : Option {
        override fun getName() = "Show Directory Statistics"
        override fun isSelected() = project.service<PluginSettings>().showDirectoryStatistics
        override fun setSelected(selected: Boolean) {
            project.service<PluginSettings>().showDirectoryStatistics = selected
            project.refreshView()
        }
    }
}

val hideViewedFilesAction = MyToggleOptionAction { project ->
    object : Option {
        override fun getName() = "Hide Viewed Files"
        override fun isSelected() = project.service<PluginSettings>().hideViewedFiles
        override fun setSelected(selected: Boolean) {
            project.service<PluginSettings>().hideViewedFiles = selected
            project.refreshView()
        }
    }
}

val clearDataAction = object : DumbAwareAction("Clear Statistics Data") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val confirm = MessageDialogBuilder.yesNo("Clear Data", "Are you sure you want to clear all statistics data?")
            .ask(project)
        if (confirm) {
            project.service<StatisticsData>().clearData()
            project.refreshView()
        }
    }
}

fun getActions(): DefaultActionGroup {
    val actionGroup = DefaultActionGroup()
    actionGroup.isPopup = true
    actionGroup.add(enableLoggingAction)
    actionGroup.addSeparator("Project View")
    actionGroup.add(showFileViewStatisticsAction)
    actionGroup.add(showDirectoryStatisticsAction)
    actionGroup.add(hideViewedFilesAction)
    actionGroup.addSeparator("Statistics Data")
    actionGroup.add(clearDataAction)
    return actionGroup
}

fun getPopup(context: DataContext) = JBPopupFactory.getInstance().createActionGroupPopup(
    NAME,
    getActions(),
    context,
    SPEEDSEARCH,
    true
)
