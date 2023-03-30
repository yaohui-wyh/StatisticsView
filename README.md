<img src="src/main/resources/META-INF/pluginIcon.svg" width="60" height="60" alt="icon" align="left"/>

Statistics View IntelliJ Plugin
===

[![Build][github-action-svg]][github-action-build]
[![Version][plugin-version-svg]][plugin-repo]
[![Downloads][plugin-downloads-svg]][plugin-repo]
[![Rating][plugin-rating-svg]][plugin-repo]

<!-- Plugin description -->
**StatisticsView** IntelliJ plugin allows for the tracking of IDE code browsing actions and displays summarized statistical data (such as the frequency of file openings) on the Project view within the IDE.

Its purpose is to offer valuable insights into the productivity of IDE users, as well as allow for data analysis and visualizations based on the raw event logs.

Additional details can be discovered on [GitHub](https://github.com/yaohui-wyh/StatisticsView).

<!-- Plugin description end -->

<img src="docs/demo.gif" width="640" alt="demo"/>

> There is a backstory behind the creation of the StatisticsView plugin: [docs/README.md](./docs/README.md)

## Installation

- **StatisticsView** can be installed from an IntelliJ IDE via `Settings | Plugins`. See the [detailed instructions](https://www.jetbrains.com/help/idea/managing-plugins.html#) from the JetBrains guide.
- Alternatively, download the latest plugin zip file from the [GitHub release](https://github.com/yaohui-wyh/StatisticsView/releases/latest) and install manually via `Settings | Plugins | Install plugin from disk`

## Usage

The **Project** tool window now includes a title bar button through the plugin. This button presents a range of menu options, including:

- **Enable Logging**: Enables/disables logging for the current IDE project. The button shows the current state of logging, so users don't have to worry about logging running in the background without their knowledge.
- Project View:
    - **Show File Statistics**: Displays/hides aggregated statistical information about each file, such as "Last viewed" summaries and the total time viewed. Duration times are not accounted for when the IDE frame is deactivated.
    - **Show Directory Statistics**: Shows the percentage of viewed files in each directory.
    - **Hide Viewed Files**: Hides viewed files and empty middle directories from the Project view.
- Statistics Data:
    - **Clear Statistics Data**: Clears old raw events and pre-aggregated results for the current project. Note that this operation is unrecoverable.
    - **Show Data File**: Locates the current project's event log file in Finder/Explorer.

## How it works

The **StatisticsView** plugin draws inspiration from the [**activity-tracker**](https://github.com/dkandalov/activity-tracker) and certain components of the [**intellij-community**](https://github.com/JetBrains/intellij-community) project. In addition to event tracking, this plugin offers real-time analytical capabilities that are seamlessly integrated with the IDE, such as supplementary information in the Project view.

### Event

StatisticsView subscribes to a range of IDE events including file opening, code completion, jumping to definition and more, using [listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html) provided by the IntelliJ Platform.

> The current version (1.0.0) of StatisticsView is limited to handling events related to opening/closing files and activating/deactivating the IDE.

Whenever an event is received, StatisticsView records the event type, timestamp, and fileUri (only if the event is file related). StatisticsView won't process events for files that are not valid source files (e.g. binary files, library folders, intentionally "marked as excluded" directories would be ignored).

### Data storage

To minimize dependencies, StatisticsView directly stores events onto a disk file. However, this approach has limitations that may prevent complex queries on file-based datasets, such as filtering by time range or grouping by directory. As we must display summarized information in real-time within the IDE Project view, queries can only be executed on pre-aggregated data structures stored in memory.

After debounce and deduplication, the write load is not very high, with a maximum of 10+ writes per second. Events can be safely queued and periodically saved to disk in a background thread without affecting the performance of the IDE.

> The practice of using a pre-aggregated hashMap and an asynchronous file writer is not capable of handling back-filling scenarios. For instance, if a user renames a file in the IDE, events with the previous fileUri need to be modified.

If you plan on collecting event logs for an extended period, it is recommended that you transfer the raw data to an external data storage system (such as TSDB) and conduct analysis on it offline.

### Data format

> As of version 1.0.0, events are serialized into JSON string format, but this may be subject to revision in upcoming releases.

JSON serialization is picked by its simplicity (provided by `kotlinx.serialization` API out-of-box), and it can be easily parsed line by line using shell scripts.

Example of log file:

```
{"ts":1661075776169,"action":"IDE_ACTIVATED","file":"","tags":{}}
{"ts":1661075789662,"action":"FILE_OPENED","file":"/README.md","tags":{"FILE_LINE_OF_CODE":"14"}}
{"ts":1661075798429,"action":"FILE_CLOSED","file":"/README.md","tags":{"FILE_LINE_OF_CODE":"14"}}
{"ts":1661075798429,"action":"FILE_OPENED","file":"/src/main/kotlin/org/yh/statistics/listener/MyFileEditorManagerListener.kt","tags":{"FILE_LINE_OF_CODE":"49"}}
{"ts":1661075799293,"action":"FILE_CLOSED","file":"/src/main/kotlin/org/yh/statistics/listener/MyFileEditorManagerListener.kt","tags":{"FILE_LINE_OF_CODE":"49"}}
{"ts":1661075839459,"action":"FILE_OPENED","file":"/src/main/kotlin/org/yh/statistics/view/MyViewTreeStructureProvider.kt","tags":{"FILE_LINE_OF_CODE":"43"}}
{"ts":1661075841073,"action":"FILE_CLOSED","file":"/src/main/kotlin/org/yh/statistics/view/MyViewTreeStructureProvider.kt","tags":{"FILE_LINE_OF_CODE":"43"}}
{"ts":1661075841073,"action":"FILE_OPENED","file":"/src/main/kotlin/org/yh/statistics/view/MyViewNodeDecorator.kt","tags":{"FILE_LINE_OF_CODE":"71"}}
{"ts":1661075850804,"action":"IDE_DEACTIVATED","file":"","tags":{}}
```

## Impact on IDE (FAQ)

### Performance

1. Will enabling logging all the time cause a slower IDE startup?
    - No. Long-running & IO-bound tasks (e.g. serialize/deserialize events data, R/W log file) are executed on the pooled thread and will not affect the IDE responsiveness. You can check the startup cost for plugins using the IntelliJ IDE's built-in diagnostic tools: `IDE Help | Diagnostic Tools | Analyze Plugin Startup Performance`:
    - <img src="docs/ide-diagnostic.png" width="480" alt="diagnostic"/>
    - The handling of IDE events occurs on the event dispatch thread (EDT), which avoids blocking the UI thread.
2. As events continue to pile up, will they consume my memory?
    - The raw events are queued in memory and written to the disk file periodically, and once the writing is done, they are cleared from the queue and got garbage collected.
3. The sole drawback to performance comes from using the `Show File Statistics`/`Show Directory Statistics` options, which display statistical data alongside file and directory nodes in the Project view. While ProjectView rendering typically occurs quickly, large projects with many files may experience a noticeable delay. If you encounter sluggishness in the Project view, simply disable the statistical options and re-enable them when you require the data.

### Data

1. Where can the raw event log be found?
    - The raw event log (`stat.log`) is saved in the project `.idea` folder, and IDE doesn't need your permission to read/write from somewhere other than the current project's folder. It's by nature project-wide, and you won't bother cleaning up the log once you delete the IDE project. It would also be less likely to mess up your VCS since most projects' `.idea` folder is already ignored.
2. Is it secure to share the log file with another person?
    - Certainly. No personal identification details (such as username or hostname) are recorded, and all `fileUri` are preserved as relative paths from the project's root.
3. Is there any telemetry or data reporting?
    - Absolutely not, and it will never happen.

## Data analysis & IDE productivity [WIP]

> Note: Most of this section is not directly relevant to the responsibilities of the StatisticsView plugin. However, there are significant similarities between the concepts of microservices observability and monitoring IDE code activities.

### Import event data into PostgresSQL [Experimental]

checkout [data-analysis-poc](./data-analysis-poc) for a k8s manifest for importing event logs into PostgresSQL and visualizing in Grafana dashboard

<img src="docs/grafana-dashboard.png" width="800" alt="grafana dashboard"/>

<!-- Badges -->

[github-action-build]: https://github.com/yaohui-wyh/StatisticsView/actions/workflows/build.yml
[github-action-svg]: https://github.com/yaohui-wyh/StatisticsView/actions/workflows/build.yml/badge.svg
[plugin-repo]: https://plugins.jetbrains.com/plugin/19747-statistics-view
[plugin-downloads-svg]: http://img.shields.io/jetbrains/plugin/d/19747
[plugin-rating-svg]: http://img.shields.io/jetbrains/plugin/r/stars/19747
[plugin-version-svg]: https://img.shields.io/jetbrains/plugin/v/19747?label=version