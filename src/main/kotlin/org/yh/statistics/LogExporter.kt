package org.yh.statistics

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.Project.DIRECTORY_STORE_FOLDER
import com.intellij.util.io.createFile
import com.intellij.util.io.exists
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.yh.statistics.Constants.DATA_FILE
import org.yh.statistics.Constants.MAX_LOG_LINE
import org.yh.statistics.model.StatisticsEvent
import java.io.File
import java.lang.System.lineSeparator
import java.nio.file.Paths


/**
 * Read/Write raw log events to file.
 *
 * - A Log is a timestamped {@link StatisticsEvent} emitted by DataProvider {@link StatisticsData}.
 * - FS is the only underlying storage system right now,
 * however we could use some Log agent (e.g. OTel Collector) to emit logs to some backend
 */
@Service(Service.Level.PROJECT)
class LogExporter(private val project: Project) {

    private val json = Json {
        ignoreUnknownKeys = true
        // invalid property value would be coerced into the default value
        coerceInputValues = true
        // default values are not encoded
        encodeDefaults = false
    }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> Json.decode(string: String): T? {
        try {
            return decodeFromString<T>(string)
        } catch (ex: SerializationException) {
            thisLogger().d { "decode exception, rawString=$string, $ex" }
        }
        return null
    }

    var logFile = initDataFile()

    private fun initDataFile(): File? {
        val basePath = project.basePath ?: return null
        val path = Paths.get(basePath, DIRECTORY_STORE_FOLDER, DATA_FILE)
        return if (!path.exists()) path.createFile().toFile() else path.toFile()
    }

    fun importEvents(): List<StatisticsEvent> {
        logFile?.let { file ->
            return try {
                file.useLines { parseLines(it) }
            } catch (ex: Exception) {
                thisLogger().d { "readDate exception, $ex" }
                emptyList()
            }
        }
        return emptyList()
    }

    private fun parseLines(lines: Sequence<String>): List<StatisticsEvent> {
        return lines
            .take(MAX_LOG_LINE)
            .mapNotNull { json.decode<StatisticsEvent>(it) }
            .toList()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun writeEvents(events: List<StatisticsEvent>) {
        logFile?.let { file ->
            try {
                file.appendText("${events.joinToString(lineSeparator()) { json.encodeToString(it) }}${lineSeparator()}")
            } catch (ex: Exception) {
                thisLogger().d { "writeEvents exception, $ex" }
            }
        }
    }

    fun clearData() {
        logFile?.writeText("")
    }
}