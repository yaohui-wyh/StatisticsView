package org.yh.statistics

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.PROJECT)
@State(name = "StatisticsView", storages = [Storage("statistics-view.xml")])
class PluginSettings : PersistentStateComponent<PluginSettings.MyState> {

    var enableLogging = true
    var showFileViewStatistics = true
    var showDirectoryStatistics = false
    var hideViewedFiles = false

    override fun getState(): MyState {
        val result = MyState()
        result.enableLogging = enableLogging
        result.showFileViewStatistics = showFileViewStatistics
        result.showDirectoryStatistics = showDirectoryStatistics
        result.hideViewedFiles = hideViewedFiles
        return result
    }

    override fun loadState(state: MyState) {
        enableLogging = state.enableLogging
        showFileViewStatistics = state.showFileViewStatistics
        showDirectoryStatistics = state.showDirectoryStatistics
        hideViewedFiles = state.hideViewedFiles
    }

    class MyState {
        @JvmField
        var enableLogging = true
        @JvmField
        var showFileViewStatistics = true
        @JvmField
        var showDirectoryStatistics = false
        @JvmField
        var hideViewedFiles = false
    }
}