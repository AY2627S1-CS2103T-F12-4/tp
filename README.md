# Roster

[![CI Status](https://github.com/AY2627S1-CS2103T-F12-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F12-4/tp/actions/workflows/gradle.yml)

[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F12-4/tp/graph/badge.svg?token=81CFRoKpqY)](https://codecov.io/gh/AY2627S1-CS2103T-F12-4/tp)

![Roster user interface](docs/images/Ui.png)

> **Alpha testing:** Download `roster.jar` from the [latest release](https://github.com/AY2627S1-CS2103T-F12-4/tp/releases/latest) and follow the [User Guide](docs/UserGuide.md#quick-start).

**Roster is a desktop application that helps teaching assistants manage students across multiple tutorial groups.**

Designed for fast, keyboard-driven use during class, Roster allows TAs to record attendance and short comments, search student records and export attendance to CSV. Keeping student details and attendance together helps TAs identify students who may need additional support.

## Who is Roster for?

Roster is designed for NUS teaching assistants who manage multiple tutorial groups from a laptop during class. It is aimed at TAs who are comfortable typing commands and need to update records while teaching, then review those records after class.

## Why Roster?

Keep each student's details, tutorial group and attendance together instead of maintaining separate spreadsheets. Roster aims to reduce the effort of finding and updating class records, so TAs can focus on teaching and identifying students who may need timely, targeted support.

## Features

Roster supports:

- **Tutorial groups:** Create separate groups and move between group views without mixing up their student records.
- **Student records:** Add students with their identifying details and tutorial group, and delete records that are no longer needed.
- **Roster views:** List students across all groups or view the students in one tutorial group.
- **Student search:** Find students by complete or partial name, exact matriculation number, tutorial group, or a combination of these filters.
- **Attendance:** Record a student as present or absent for a tutorial session, while keeping unrecorded attendance distinct from confirmed absence.
- **Comments:** Append, replace or clear a short comment for a student.
- **Undo:** Reverse changes during the current application session.
- **CSV export:** Export attendance for all students or a specified tutorial group to a new CSV file.

Participation records and assignment-completion tracking are not implemented. Roster does not cover grading or communicating with students.

## Documentation

Visit the [Roster product website](https://ay2627s1-cs2103t-f12-4.github.io/tp/) for the project's documentation.

- Learn how to use Roster in the [User Guide](docs/UserGuide.md).
- Learn about its design and implementation in the [Developer Guide](docs/DeveloperGuide.md).
- Meet the development team on the [About Us](docs/AboutUs.md) page.

## Acknowledgements

Roster is based on the [AddressBook Level 3](https://se-education.org/addressbook-level3/) project created by the [SE-EDU initiative](https://se-education.org/).

The project uses [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), and [JUnit 5](https://junit.org/junit5/).
