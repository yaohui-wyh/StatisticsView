package org.yh.statistics

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.Project.DIRECTORY_STORE_FOLDER
import com.intellij.util.io.createFile
import com.intellij.util.io.exists
import org.yh.statistics.Constants.DATA_FILE
import org.yh.statistics.model.StatisticsEvent
import java.io.File
import java.nio.file.Paths


@Service(Service.Level.PROJECT)
class LogExporter(private val project: Project) {

    var logFile = initDataFile()

    private fun initDataFile(): File? {
        val basePath = project.basePath ?: return null
        val path = Paths.get(basePath, DIRECTORY_STORE_FOLDER, DATA_FILE)
        return if (!path.exists()) path.createFile().toFile() else path.toFile()
    }

    fun importEvents(): List<StatisticsEvent> {
        logFile?.let { file ->
            return try {
                file.readLines().mapNotNull { StatisticsEvent.fromString(it) }
            } catch (ex: Exception) {
                thisLogger().d { "readDate exception, $ex" }
                emptyList()
            }
        }
        return emptyList()
    }

    fun writeEvents(events: List<StatisticsEvent>) {
        logFile?.let { file ->
            try {
                file.appendText("\n${events.joinToString("\n") { it.toJsonString() }}")
            } catch (ex: Exception) {
                thisLogger().d { "writeEvents exception, $ex" }
            }
        }
    }

    fun clearData() {
        logFile?.writeText("")
    }
}