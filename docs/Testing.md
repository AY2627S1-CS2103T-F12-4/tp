---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`

--------------------------------------------------------------------------------------------------------------------

## Style checks and coverage

Run `./gradlew check coverage` (Windows: `gradlew check coverage`) before submitting a change.
This runs the tests and Checkstyle, then writes an HTML coverage report to
`build/reports/jacoco/coverage/html/index.html` and the XML report used by Codecov to
`build/reports/jacoco/coverage/coverage.xml`. The GitHub workflow runs the checks on Linux, macOS and Windows.

Inspect uncovered branches as well as lines, especially validation failures and file errors. Coverage
shows which paths ran; tests still need assertions about results, preserved data and error messages.
For storage tests, use temporary folders and keep test output separate from real roster files.

Java assertions must be enabled when testing internal invariants. Gradle's test runner enables them;
for an IDE or packaged application, add VM option `-ea`, for example `java -ea -jar roster.jar`.
The [manual-testing appendix](DeveloperGuide.md#appendix-instructions-for-manual-testing) covers launch,
deletion and data recovery, and should also be used when testing a release candidate.
