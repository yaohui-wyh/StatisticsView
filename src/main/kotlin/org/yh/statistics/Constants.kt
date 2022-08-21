package org.yh.statistics


object Constants {

    const val NAME = "Statistics View"
    const val DATA_FILE = "stat.log"
    const val GITHUB_URL = "https://github.com/yaohui-wyh/StatisticsView"
}

enum class StatisticsAction {
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
