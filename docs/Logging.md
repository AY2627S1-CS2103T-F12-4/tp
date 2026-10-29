---
layout: page
title: Logging guide
---

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

`LogicManager` logs the command's save lifecycle at `FINE`: when no roster save is required, when an unchanged
roster preserves undo/redo history, when a new history snapshot is committed in memory, and when the roster and
history have been saved successfully. These messages identify the command type without repeating its arguments.
Change `LogsCenter.LOG_LEVEL` from `Level.INFO` to `Level.FINE` and rebuild to see them in the console and log files.

A failed save is logged at `WARNING`, including the original exception and its stack trace, before the roster,
undo/redo history and student-list view are restored. A history-commit message alone does not mean the save
succeeded; look for the subsequent save-success message or rollback warning.
