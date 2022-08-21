package org.yh.statistics.model

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.yh.statistics.StatisticsAction

@Serializable
data class StatisticsEvent(
    var ts: Long,
    var action: StatisticsAction,
    var file: String,
    var tags: Map<String, String>
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun toJsonString(): String {
        return try {
            Json.encodeToString(this)
        } catch (ex: SerializationException) {
            ""
        }
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromString(jsonString: String): StatisticsEvent? {
            return try {
                Json.decodeFromString(jsonString)
            } catch (ex: SerializationException) {
                null
            }
        }
    }
}