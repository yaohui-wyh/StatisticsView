package org.yh.statistics.listener

import com.intellij.ide.FrameStateListener
import com.intellij.openapi.project.ProjectManager
import org.yh.statistics.StatisticsAction.IDE_ACTIVATED
import org.yh.statistics.StatisticsAction.IDE_DEACTIVATED
import org.yh.statistics.model.addIDEEvent


/**
 * Listen to IDE activated/deactivated events (i.e., IDE move back to foreground or lost focus)
 */
class MyFrameStateListener : FrameStateListener {

    override fun onFrameDeactivated() {
        ProjectManager.getInstance().openProjects.forEach { addIDEEvent(it, IDE_DEACTIVATED) }
    }

    override fun onFrameActivated() {
        ProjectManager.getInstance().openProjects.forEach { addIDEEvent(it, IDE_ACTIVATED) }
    }
}