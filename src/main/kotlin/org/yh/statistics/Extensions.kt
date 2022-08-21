package org.yh.statistics

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

inline fun Logger.d(message: () -> String) {
    debug("[StatisticsView] ${message()}")
}

fun VirtualFile.relativeUrl(project: Project) =
    url.substringAfter(project.basePath.orEmpty())

fun VirtualFile.shouldHandleStatistics(project: Project): Boolean {
    // check filetype
    val isBinary = !this.isDirectory && this.fileType.isBinary
    val isIgnored = FileTypeManager.getInstance().isFileIgnored(this)
    if (isBinary || isIgnored) {
        return false
    }
    // ignore .idea
    project.projectFile?.parent?.let {
        if (VfsUtilCore.isAncestor(it, this, true)) {
            return false
        }
    }
    // check fileIndex
    val indexer = ProjectRootManager.getInstance(project).fileIndex
    if (indexer.isExcluded(this) || !indexer.isInContent(this) ||
        indexer.isUnderIgnored(this) || indexer.isInLibrary(this)
    ) {
        return false
    }
    return true
}

fun Long.toLocalDate(): LocalDateTime? = LocalDateTime.ofInstant(
    Instant.ofEpochMilli(this),
    ZoneId.systemDefault()
)

fun Long.duration(): String {
    val millis = Duration.ofMillis(this)
    return when {
        millis.toMinutes() <= 0 -> "${millis.toSeconds()}s"
        millis.toHours() <= 0 -> "${millis.toMinutes()}m"
        millis.toDays() <= 0 -> "${millis.toHours()}h"
        else -> "${millis.toDays()}d"
    }
}

fun LocalDateTime.delta(): String {
    val delta = Duration.between(this, LocalDateTime.now())
    return when {
        delta.toMinutes() <= 0 -> "just now"
        delta.toHours() <= 0 -> "${delta.toMinutes()} minutes ago"
        delta.toDays() <= 0 -> "${delta.toHours()} hours ago"
        else -> "${delta.toDays()} days ago"
    }
}

fun Project.refreshView() {
    ProjectView.getInstance(this).refresh()
}