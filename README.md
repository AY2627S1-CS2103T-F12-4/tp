# Roster

[![CI Status](https://github.com/AY2627S1-CS2103T-F12-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F12-4/tp/actions/workflows/gradle.yml)

[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F12-4/tp/graph/badge.svg?token=81CFRoKpqY)](https://codecov.io/gh/AY2627S1-CS2103T-F12-4/tp)

![Roster user interface](docs/images/Ui.png)

> **Development status:** Roster is under development. This page describes the intended product; the planned MVP focuses on student rosters and attendance.

**Roster is a desktop application that helps teaching assistants manage students across multiple tutorial groups.**

Designed for fast, keyboard-driven use during class, Roster allows TAs to record attendance, track participation and assignment completion, and review student progress without juggling multiple spreadsheets. By keeping each student's information and progress records in one place, Roster helps TAs identify students who may require timely and targeted support.

## Who is Roster for?

Roster is designed for NUS teaching assistants who manage multiple tutorial groups from a laptop during class. It is aimed at TAs who are comfortable typing commands and need to update records while teaching, then review those records after class.

## Why Roster?

Keep each student's details, tutorial group and attendance together instead of maintaining separate spreadsheets. Roster aims to reduce the effort of finding and updating class records, so TAs can focus on teaching and identifying students who may need timely, targeted support.

## Planned features

The initial MVP is planned to support:

- **Tutorial groups:** Create separate groups and move between group views without mixing up their student records.
- **Student records:** Add students with their identifying details and tutorial group, and delete records that are no longer needed.
- **Roster views:** List students across all groups or view the students in one tutorial group.
- **Name search:** Find students across tutorial groups using a complete or partial name.
- **Attendance:** Record a student as present or absent for a tutorial session, while keeping unrecorded attendance distinct from confirmed absence.

Participation records and assignment-completion tracking are part of the broader product direction, beyond the initial MVP. Roster does not cover grading or communicating with students.

## Documentation

Visit the [Roster product website](https://ay2627s1-cs2103t-f12-4.github.io/tp/) for the project's documentation.

- Learn how to use Roster in the [User Guide](docs/UserGuide.md).
- Learn about its design and implementation in the [Developer Guide](docs/DeveloperGuide.md).
- Meet the development team on the [About Us](docs/AboutUs.md) page.

## Acknowledgements

Roster is based on the [AddressBook Level 3](https://se-education.org/addressbook-level3/) project created by the [SE-EDU initiative](https://se-education.org/).

The project uses [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), and [JUnit 5](https://junit.org/junit5/).
