---
layout: page
title: User Guide
---

Roster is a **desktop application for NUS teaching assistants to manage students and attendance across multiple tutorial groups**. It is optimized for fast, keyboard-driven use through a Command Line Interface (CLI), while retaining the convenience of a Graphical User Interface (GUI).

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from the [Roster releases page](https://github.com/AY2627S1-CS2103T-F12-4/tp/releases).

1. Copy the file to the folder you want to use as Roster's _home folder_.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Roster includes sample data on its first launch.<br>
   ![Roster user interface](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.

1. Try this typical attendance workflow:

   * `init grp/T09` : Creates tutorial group `T09`.
   * `add n/Alex Tan p/12345678 e/alex.tan@example.com a/Kent Ridge m/A0123456X grp/T09` : Adds Alex Tan to `T09`.
   * `session grp/T09 w/5 d/2026-09-15` : Creates the week 5 session for `T09`.
   * `list grp/T09` : Shows the students in `T09` and makes it the active group.
   * `mark 1 w/5 s/present` : Marks the first displayed student present for week 5.

1. Refer to the [Features](#features) section for full details of each command.

--------------------------------------------------------------------------------------------------------------------

## Understanding Roster

### Tutorial groups and sessions

Every student belongs to exactly one tutorial group. Create a group with `init` before adding students or sessions to it.

A session records that a tutorial group met during a particular teaching week and on a particular date. A group can have at most one session per teaching week.

### Attendance states

For each session in a student's tutorial group, their attendance is one of three states:

* **Present (`✓`)**: The student has been marked present.
* **Absent (`✗`)**: The student has been marked absent.
* **Unmarked (`—`)**: No attendance has been recorded yet. Unmarked is kept separate from absent so unfinished attendance is not mistaken for a confirmed absence.

Creating a session does not automatically mark any student. A student added after sessions have been created is also unmarked for those earlier sessions.

The MVP does not support returning a recorded status to unmarked. You can, however, replace `present` with `absent`, or vice versa, by running `mark` again.

### Displayed list and active group

* `list` shows every student and clears the active group.
* `list grp/GROUP` shows one group's students and makes that group active.
* `find` searches every group and clears the active group.
* Students are listed by matriculation number in ascending order.
* `INDEX` always refers to the list currently displayed. The same student may have a different index after `list`, `list grp/GROUP`, or `find`.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about command formats:**<br>

* Words in `UPPER_CASE` are parameters that you must replace with your own values.<br>
  For example, replace `GROUP` in `init grp/GROUP` with a group code such as `T09`.

* Items in square brackets are optional.<br>
  For example, `list [grp/GROUP]` can be used as either `list` or `list grp/T09`.

* Prefixed parameters can be given in any order.<br>
  For example, both `session grp/T09 w/5 d/2026-09-15` and `session d/2026-09-15 grp/T09 w/5` are valid.

* A prefix marks the start of a parameter. Because `/` marks the end of a prefix, parameter values cannot contain `/`.

* Unless a command states otherwise, each parameter may appear only once.

* `INDEX` must be a positive integer and always refers to the currently displayed list.

* If a command fails, Roster does not create, change, or delete any data, and the displayed list remains unchanged.

* If you are using a PDF version of this guide, take care when copying commands that span multiple lines, as spaces around line breaks may be omitted.
</div>

### Parameter rules

Parameter | Accepted value
----------|---------------
`grp/GROUP` | One or two letters followed by two digits, e.g. `T09` or `TG01`. Surrounding spaces are removed and letters are converted to uppercase.
`n/NAME` | 1–70 characters. Letters, spaces, hyphens and apostrophes only; must begin and end with a letter. Surrounding spaces are removed, repeated spaces become one space, and letter case is preserved.
`p/PHONE` | At least three digits, with no spaces or other characters.
`m/MATRIC` | `A`, followed by seven digits and one letter, e.g. `A0287654J`. Surrounding spaces are removed and letters are converted to uppercase.
`e/EMAIL` | A valid email address such as `alex.tan@example.com`. Surrounding spaces are removed and the address is converted to lowercase.
`a/ADDRESS` | Any non-blank text.
`t/TAG` | One or more letters or digits, with no spaces or other characters. Repeat `t/` to give a student multiple tags.
`w/WEEK` | A whole number from `1` to `13`, without a leading zero.
`d/DATE` | A real calendar date in `YYYY-MM-DD` format.
`s/STATUS` | `present` or `absent`, case-insensitive.
`INDEX` | A positive integer identifying a student in the currently displayed list.

### Viewing help: `help`

Shows a message explaining how to access this User Guide.

Format: `help`

![Help message](images/helpMessage.png)

### Creating a tutorial group: `init`

Creates a new, empty tutorial group. The new group becomes the active group and the displayed student list becomes empty.

Format: `init grp/GROUP`

Examples:

* `init grp/T09`
* `init grp/t10` creates group `T10` because group codes are normalized to uppercase.

### Creating a tutorial session: `session`

Creates a session for an existing tutorial group and teaching week. Every student in that group is initially unmarked for the session.

Format: `session grp/GROUP w/WEEK d/DATE`

Examples:

* `session grp/T09 w/5 d/2026-09-15`
* `session d/2026-09-22 grp/t09 w/6`

* A tutorial group can have only one session per teaching week. The date is not part of a session's identity.
* Sessions may be created out of order; attendance is displayed in teaching-week order.
* The date must be a real date, but Roster does not check whether it falls within the specified teaching week.
* Creating a session for an empty group is allowed.
* The MVP does not support editing or deleting sessions.

On success, Roster displays a confirmation such as `Created session: T09 week 5 (2026-09-15)`.

### Adding a student: `add`

Adds a student to an existing tutorial group. The student is unmarked for every existing session in that group.

Format: `add n/NAME p/PHONE e/EMAIL a/ADDRESS m/MATRIC grp/GROUP [t/TAG]...`

Examples:

* `add n/Alex Tan p/12345678 e/alex.tan@example.com a/Kent Ridge m/A0123456X grp/T09`
* The tutorial group must already exist.
* Matriculation numbers and email addresses must be unique across all tutorial groups.
* Names do not need to be unique because different students may share a name.

On success, Roster displays a confirmation such as `Added Alex Tan (A0123456X) to tutorial group T09.`


### Listing students: `list`

Shows all students or the students of one registered tutorial group, sorted by matriculation number.

Formats:

* `list` — list all students.
* `list grp/GROUP` — list students in one registered tutorial group.

Examples:
* `list` shows every student and clears the active group and any previous search filter.
* `list grp/T09` shows only T09's students and makes T09 the active group.
* `list grp/t09` works too: group codes ignore letter case and surrounding spaces.

Group codes have one or two letters followed by two digits, such as `T09` or `TG01`.
The group must already exist. Listing an empty group is allowed; listing a nonexistent group is an error.

| Situation                  | Feedback                                                                          |
| -------------------------- | --------------------------------------------------------------------------------- |
| All students               | `Listed all 58 students.` (with the actual count)                                 |
| One group                  | `Listed 20 students in T09.` (with the actual count and group)                    |
| Registered but empty group | `Tutorial group T09 has no students yet.`                                         |
| Empty roster               | `No students in Roster. Create a tutorial group and add students to get started.` |
| Unknown group              | `Tutorial group T09 does not exist. Create it first with: init grp/T09`           |

Each student's card shows their details and attendance for every session of their group, in teaching-week order:
A green `✓` means present, a red `✗` means absent, and a grey `—` means unmarked.
A student added after a session also shows `—` until their attendance is recorded. Weeks with no session have no cell. The list scrolls when needed.

The group scope is shown above the list. `find` searches across groups and clears the active group.
Adding or deleting a student refreshes the current results without clearing the filter.
Student indices are the row numbers currently on screen, so check the displayed list before using `delete`, `edit`,
or `mark`. A successful edit shows all students again in matriculation-number order. Editing a matriculation number
can therefore change row numbers. Moving a student to another group clears their old attendance records; their
attendance strip then uses the new group's sessions.

Only one `grp/` parameter is allowed. Missing group values, invalid codes, repeated parameters and unrelated arguments
are rejected. A rejected list command leaves the data, active group and displayed rows unchanged.

### Finding students by name: `find`

Finds students across all tutorial groups whose names contain the search phrase. A search clears the active group.

Format: `find n/KEYWORD`

* The `n/` prefix is required and must appear exactly once. The keyword may contain several words, such as `john tan`.
* Surrounding whitespace is ignored, and repeated whitespace is treated as one space in both the query and name.
* Copied commands can use Unicode spaces, including non-breaking spaces, around `find` and `n/`. Line breaks within a search phrase are also treated as spaces.
* The query must contain 1–100 characters after whitespace normalization: letters, spaces, hyphens, apostrophes or full stops, with at least one letter.
* The search is case-insensitive and considers only names. Accents and punctuation remain significant.
* The entire phrase must appear contiguously and in the same order. For example, `john tan` matches `John Tan` and `John Tanner`, but does not match `John Lim`, `Mei Tan` or `Tan John`.
* Results are sorted by matriculation number in ascending order, including both complete and partial name matches. For example, `find n/john` lists `Johnny Lim` with matriculation number `A0000001X` before `John Tan` with `A0000002X`.
* A successful search clears the active tutorial group and its selection in the group panel. Each result shows the student's matriculation number, tutorial group and attendance for that group's sessions.
* Each search considers all students, including when the previous search returned no matches.
* Successful searches show `Found COUNT student(s) matching "KEYWORD".` A search with no matches produces an empty list and shows `No students found matching "KEYWORD". Try another name.`
* Missing or blank queries, repeated `n/` prefixes, unsupported characters, text before `n/`, and other parameters produce an error without changing the displayed list or active tutorial group.
* `mark INDEX`, `delete INDEX` and `edit INDEX` use the displayed result indexes. Search does not change the saved roster order. Use `list` to show everyone again in ascending matriculation-number order.

Examples:
* `find n/John` returns `John Doe` and `Johnny Lim`, sorted by matriculation number.
* `find n/john tan` returns `John Tan` and `John Tanner`, sorted by matriculation number.
* `find n/joh` returns names containing `joh`, including `John Tan` and `Johnny Lim`.

### Deleting a student: `delete`

Deletes a student and all of their attendance records. The student's tutorial group and its sessions are kept, even if the group becomes empty.

Format: `delete INDEX`

* `INDEX` refers to the index number in the currently displayed student list.
* The index must be a positive integer within the displayed list.
* Deletion happens immediately, with no confirmation and no undo.
* After deletion, the displayed list is refreshed and its indexes are recalculated. Repeating the same command may therefore delete a different student.
* Re-adding the student creates a new record whose attendance is unmarked; deleted attendance is not restored.

On success, Roster names the deleted student and confirms that their attendance records were also removed.

Examples:

* `list` followed by `delete 2` deletes the second student in the full roster.
* `list grp/T09` followed by `delete 1` deletes the first displayed student in `T09`.
* `find n/Betsy` followed by `delete 1` deletes the first student in the search results.

### Marking attendance: `mark`

Records a displayed student as present or absent for one of their tutorial group's sessions.

Format: `mark INDEX w/WEEK s/STATUS`

Examples:

* `mark 3 w/5 s/present`
* `mark 1 s/absent w/6`

* `INDEX` refers to the currently displayed student list.
* The specified teaching week must have a session for that student's tutorial group.
* `STATUS` must be either `present` or `absent`, case-insensitive.
* Marking an already marked student replaces the earlier status and reports the previous value.
* The displayed list and its indexes stay unchanged after a successful mark.
* The updated attendance cell changes to `✓` or `✗`.
* Marking never creates a missing session. When the displayed list contains multiple groups, Roster looks up the teaching week in each selected student's own group.

On success, Roster names the student, status, tutorial group, and teaching week so you can verify the updated record.

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
Run `list grp/GROUP` before taking attendance so only that tutorial group's students are displayed.
</div>

### Exiting Roster: `exit`

Exits the application.

Format: `exit`

### Clearing all data: `clear`

Deletes every student, tutorial group, session and attendance record.

Format: `clear`

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
Clearing happens immediately, with no confirmation and no undo. Back up `data/addressbook.json` first if you may need to restore the data.
</div>

### Saving data

Roster automatically saves its data after each successful command. You do not need to save manually.

### Editing the data file

Roster data is saved as `[JAR file location]/data/addressbook.json`. Advanced users may edit this JSON file directly.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, Roster starts with an empty roster the next time it runs. The invalid file remains on disk until a successful command causes Roster to save again. Back up the file before editing it.<br>
Manual edits can also produce unexpected behavior if values do not follow Roster's rules. Edit the data file only if you are confident that you can update it correctly.
</div>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install Roster on the other computer and replace its `data/addressbook.json` file with the file from your previous Roster home folder.

**Q**: How do I correct a student's details?<br>
**A**: Use `edit INDEX` followed by the fields to change, for example `edit 1 e/alex.tan@example.com`.

**Q**: How do I switch between tutorial groups?<br>
**A**: Use `list grp/GROUP`, for example `list grp/T09`. There is no separate switch command.

**Q**: How do I change an attendance record?<br>
**A**: Run `mark` again for the same student and teaching week with the new status. Roster replaces the earlier status.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen and later use only the primary screen, the GUI may open off-screen. Delete the `preferences.json` file created by the application before running it again.
2. **If you minimize the Help Window** and run `help` again, the original window remains minimized and no new Help Window appears. Restore the minimized Help Window manually.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, example
-------|----------------
**Help** | `help`
**Create tutorial group** | `init grp/GROUP`<br>e.g. `init grp/T09`
**Create session** | `session grp/GROUP w/WEEK d/DATE`<br>e.g. `session grp/T09 w/5 d/2026-09-15`
**Add student** | `add n/NAME p/PHONE e/EMAIL a/ADDRESS m/MATRIC grp/GROUP [t/TAG]...`<br>e.g. `add n/Alex Tan p/12345678 e/alex.tan@example.com a/Kent Ridge m/A0123456X grp/T09`
**Edit student** | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [m/MATRIC] [grp/GROUP] [t/TAG]...`<br>e.g. `edit 1 e/alex.tan@example.com`
**List students** | `list [grp/GROUP]`<br>e.g. `list grp/T09`
**Find students** | `find n/KEYWORD`<br>e.g. `find n/John Tan`
**Delete student** | `delete INDEX`<br>e.g. `delete 3`
**Mark attendance** | `mark INDEX w/WEEK s/STATUS`<br>e.g. `mark 3 w/5 s/present`
**Exit** | `exit`
**Clear all data** | `clear`
