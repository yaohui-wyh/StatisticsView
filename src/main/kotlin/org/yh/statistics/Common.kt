package org.yh.statistics

import org.yh.statistics.StatisticsHintTemplateMarker.*


object Constants {
    const val NAME = "Statistics View"
    const val DATA_FILE = "stat.log"
    const val GITHUB_URL = "https://github.com/yaohui-wyh/StatisticsView"
    const val MAX_LOG_LINE = 500 * 1000
}

val DEFAULT_HINT_TEMPLATE = "Last viewed: ${LAST_VIEWED.pattern}. (${OPEN_COUNTS.pattern} times, total ${TOTAL_TIME.pattern})"

enum class StatisticsHintTemplateMarker(val pattern: String, val desc: String) {
    LAST_VIEWED("{lv}", "Last viewed"),
    OPEN_COUNTS("{oc}", "Open counts"),
    TOTAL_TIME("{tm}", "Total viewed time (ms)");
}

enum class StatisticsAction {
    UNKNOWN,

    IDE_ACTIVATED,
    IDE_DEACTIVATED,

    PROJECT_OPENED,
    PROJECT_CLOSED,

    FILE_OPENED,
    FILE_CLOSED,
}

enum class StatisticsTag {
    PROJECT_NAME,
    FILE_LINE_OF_CODE,
}
