package org.yh.statistics


object Constants {

    const val NAME = "Statistics View"
    const val DATA_FILE = "stat.dat"
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
