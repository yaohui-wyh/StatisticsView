package org.yh.statistics.view

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.packageDependencies.ui.PackageDependenciesNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import com.intellij.structuralsearch.plugin.util.SmartPsiPointer
import com.intellij.ui.ColoredTreeCellRenderer
import org.yh.statistics.PluginSettings
import org.yh.statistics.StatisticsData
import org.yh.statistics.relativeUrl
import org.yh.statistics.shouldHandleStatistics
import kotlin.math.roundToInt


class MyViewNodeDecorator(private val project: Project) : ProjectViewNodeDecorator {

    private val settings = project.service<PluginSettings>()
    private val dataManager = project.service<StatisticsData>()

    override fun decorate(node: ProjectViewNode<*>?, data: PresentationData?) {
        if (node == null || data == null) return
        val psiElement = when (node.value) {
            is PsiElement -> node.value as PsiElement
            is SmartPsiPointer -> (node.value as SmartPsiPointer).element
            else -> return
        }
        val file = when (psiElement) {
            is PsiFile -> psiElement.virtualFile
            else -> PsiUtilCore.getVirtualFile(psiElement)
        } ?: return
        if (!file.shouldHandleStatistics(project)) return
        val localString = if (file.isDirectory) decorateDirectoryNode(file) else decorateFileNode(file)
        if (localString.isNotBlank() && data.locationString != localString) {
            data.locationString = localString
        }
    }

    private fun decorateFileNode(file: VirtualFile): String {
        if (!settings.showFileViewStatistics) return ""
        return dataManager.getFileAggregateResult(file.relativeUrl(project))?.getLastViewedHintText().orEmpty()
    }

    private fun decorateDirectoryNode(file: VirtualFile): String {
        if (!settings.showDirectoryStatistics) return ""
        val directoryInfo = DirectoryViewedInfo()
        VfsUtil.iterateChildrenRecursively(file, { it.shouldHandleStatistics(project) }) { f ->
            if (!f.isDirectory) {
                directoryInfo.total++
                if (dataManager.isFileViewed(f.relativeUrl(project))) {
                    directoryInfo.viewed++
                }
            }
            return@iterateChildrenRecursively true
        }
        return directoryInfo.toString()
    }

    override fun decorate(node: PackageDependenciesNode?, cellRenderer: ColoredTreeCellRenderer?) {}
}

data class DirectoryViewedInfo(var total: Int = 0, var viewed: Int = 0) {
    override fun toString() = if (total > 0 && viewed > 0) "${(100.0 * viewed / total).roundToInt()}% viewed" else ""
}