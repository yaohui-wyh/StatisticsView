package org.yh.statistics.view

import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.yh.statistics.PluginSettings
import org.yh.statistics.StatisticsData
import org.yh.statistics.relativeUrl


class MyViewTreeStructureProvider(private val project: Project) : TreeStructureProvider {

    private val dataManager = project.service<StatisticsData>()

    override fun modify(
        parent: AbstractTreeNode<*>,
        children: MutableCollection<AbstractTreeNode<*>>,
        settings: ViewSettings?
    ): Collection<AbstractTreeNode<*>> {
        if (project.service<PluginSettings>().hideViewedFiles) {
            val parentNode = parent as? ProjectViewNode ?: return children
            val parentFile = parentNode.virtualFile ?: return children
            if (!parentFile.isDirectory) return children
            return children - children.filter { shouldHideNode(it) }.toSet()
        }
        return children
    }

    private fun shouldHideNode(node: AbstractTreeNode<*>): Boolean {
        return when (node) {
            is PsiFileNode -> node.virtualFile?.let { dataManager.isFileViewed(it.relativeUrl(project)) } ?: true
            is PsiDirectoryNode -> node.children.isEmpty() || node.children.all { shouldHideNode(it) }
            else -> false
        }
    }
}