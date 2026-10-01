---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* has a need to manage a significant number of contacts
* prefers desktop apps over other types of applications
* can type fast
* prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: Manage contacts faster than with a typical mouse-driven GUI application.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​                                    | I want to …​                     | So that I can…​                                                        |
| -------- | ------------------------------------------ | ------------------------------ | ---------------------------------------------------------------------- |
| `* * *`  | preoccupied TA | view the students of one tutorial group | confirm that each student is in the correct group |
| `* * *`  | TA who struggles to manage several tutorial groups | assign each student to exactly one tutorial group | keep students from different groups from being mixed together |
| `* * *`  | TA | view a student's attendance for every session of their tutorial group | understand the student's attendance pattern over time |
| `* *`    | new TA unfamiliar with the app | see the app pre-loaded with sample students and attendance records | know what the data should look like before entering my own |
| `* *`    | new TA | remove all sample data | start with a clean record |
| `* *`    | cautious TA just starting out | get a warning before permanently deleting a student | avoid losing data from an accidental keystroke |
| `* *`    | new TA | record whether a student has completed an assignment | monitor assignment progress |
| `* *`    | TA who occasionally mistypes a command | undo my last action | avoid redoing work after a small mistake |
| `* *`    | TA | move a student to another tutorial group | keep group changes accurate when a student swaps groups |
| `* *`    | TA | mark attendance for an entire tutorial group in one action | spend less time recording attendance |
| `* *`    | experienced TA | find a student by matriculation number | find the right student even when two students share a name |
| `* *`    | experienced TA | mark a student's attendance by matriculation number | avoid marking the wrong student after the displayed list is filtered |
| `* *`    | experienced TA | identify students with incomplete assignments | follow up on students who may be falling behind |
| `* *`    | experienced TA | view a summary of a tutorial group's attendance | identify general trends and students who may need support |
| `* *`    | experienced TA | export students' attendance records | share or analyse the records outside the app |
| `* *`    | experienced TA | export attendance with the columns my course coordinator specifies | load the file into the gradebook without editing it first |
| `* *`    | experienced TA | export attendance keyed by matriculation number | match rows to the official roster even when names are recorded differently |
| `*`      | TA getting familiar with the app | sort students by attendance percentage | quickly spot who is struggling |
| `*`      | TA | sort students by participation score | review students in a useful order |
| `*`      | TA | leave a short comment on a student | remember context about the student for later sessions |
| `*`      | experienced TA | archive completed tutorial groups | keep old records available without cluttering my active workspace |
| `*`      | experienced TA | use keyboard shortcuts for common actions | update records without repeatedly using the mouse |
| `*`      | experienced TA co-teaching a large module | see which TA is responsible for each student | avoid duplicating work across the teaching team |
| `*`      | experienced TA | review how a tutorial group's attendance and participation change over time | recognise broader changes in the group's engagement |

### Use cases

(For all use cases below, the **System** is `Roster` and the **Actor** is the `TA`, unless specified otherwise)

**Use case: UC01 - Create a tutorial group**

**MSS**

1.  TA requests to create a tutorial group, giving its group code.
2.  Roster creates the tutorial group with no students and confirms the creation.

    Use case ends.

**Extensions**

* 1a. The group code is missing or invalid.

    * 1a1. Roster shows an error message.

      Use case ends.

* 1b. A tutorial group with the same group code already exists.

    * 1b1. Roster shows an error message.

      Use case ends.

**Use case: UC02 - Create a session**

**MSS**

1.  TA requests to create a session for a tutorial group, giving the teaching week and date.
2.  Roster creates the session and confirms the creation. Every student in the tutorial group is now unmarked for that session.

    Use case ends.

**Extensions**

* 1a. A required detail is missing or invalid.

    * 1a1. Roster shows an error message.

      Use case ends.

* 1b. The tutorial group does not exist.

    * 1b1. Roster shows an error message stating that the tutorial group must be created first.

      Use case ends.

* 1c. The tutorial group already has a session for that teaching week.

    * 1c1. Roster shows an error message.

      Use case ends.

**Use case: UC03 - Add a student**

**MSS**

1.  TA requests to add a student, giving the student's name, matriculation number, tutorial group and email.
2.  Roster adds the student to the tutorial group, with no attendance records, and confirms the addition.

    Use case ends.

**Extensions**

* 1a. A required detail is missing or invalid.

    * 1a1. Roster shows an error message.

      Use case ends.

* 1b. The tutorial group does not exist.

    * 1b1. Roster shows an error message.

      Use case ends.

* 1c. Another student already has the same matriculation number or email.

    * 1c1. Roster shows an error message.

      Use case ends.

**Use case: UC04 - List students**

**MSS**

1.  TA requests to list students, optionally limited to one tutorial group.
2.  Roster shows the matching students sorted by matriculation number, each with their attendance for every session of their tutorial group.

    Use case ends.

**Extensions**

* 1a. The specified tutorial group does not exist.

    * 1a1. Roster shows an error message.

      Use case ends.

* 2a. There are no students to show.

    * 2a1. Roster shows a message stating that there are no students yet.

      Use case ends.

**Use case: UC05 - Find students by name**

**MSS**

1.  TA requests to find students whose names contain a keyword.
2.  Roster shows all matching students across all tutorial groups.

    Use case ends.

**Extensions**

* 1a. The keyword is missing or invalid.

    * 1a1. Roster shows an error message.

      Use case ends.

* 2a. No student matches the keyword.

    * 2a1. Roster shows a message stating that no students were found.

      Use case ends.

**Use case: UC06 - Delete a student**

**MSS**

1.  TA <u>lists students (UC04)</u>.
2.  TA requests to delete a specific student in the list.
3.  Roster deletes the student and all their attendance records, and confirms the deletion.

    Use case ends.

**Extensions**

* 1a. The list is empty.

  Use case ends.

* 2a. The given index is invalid.

    * 2a1. Roster shows an error message.

      Use case resumes at step 2.

**Use case: UC07 - Mark a student's attendance**

**MSS**

1.  TA <u>lists students (UC04)</u>.
2.  TA requests to mark a specific student in the list as present or absent for a teaching week.
3.  Roster records the attendance and confirms it, naming the student.

    Use case ends.

**Extensions**

* 1a. The list is empty.

  Use case ends.

* 2a. The given index is invalid.

    * 2a1. Roster shows an error message.

      Use case resumes at step 2.

* 2b. The given attendance status is neither present nor absent.

    * 2b1. Roster shows an error message.

      Use case resumes at step 2.

* 2c. The student's tutorial group has no session for that teaching week.

    * 2c1. Roster shows an error message stating that the session must be created first.

      Use case ends.

* 3a. The student already has an attendance record for that session.

    * 3a1. Roster replaces the record and includes the previous status in the confirmation.

      Use case ends.

**Use case: UC08 - Take attendance for a tutorial**

**MSS**

1.  TA <u>creates a session for the tutorial group and teaching week (UC02)</u>.
2.  TA <u>lists the students of that tutorial group (UC04)</u>.
3.  TA <u>marks a student's attendance (UC07)</u>.

    Step 3 is repeated until every student in the tutorial group has been marked.

    Use case ends.

**Extensions**

* 1a. The session already exists.

  Use case resumes at step 2.

* 2a. The tutorial group has no students.

  Use case ends.

### Non-Functional Requirements

1. Compatibility: Roster should run on Windows, macOS, and Linux with Java 25 and the required JavaFX runtime, without requiring users to modify or compile its source code.

2. Offline operation: After installation, users should be able to manage tutorial groups, update student records, and review recorded information without an internet connection.

3. Keyboard usability: Users should be able to perform routine tasks, including adding, listing, finding and deleting students, switching tutorial groups, and recording attendance, through typed commands without requiring mouse interaction.

4. Local storage: Roster should store application data locally in a human-editable text format, without requiring a separate database server or an online account.

5. Data persistence: Following a successful save and normal application shutdown, reopening Roster should restore the saved tutorial groups, student details, and recorded attendance, participation, and assignment-completion information without alteration.

6. Input-error tolerance: Invalid commands or parameter values should not terminate the application or modify existing records. After displaying an error, Roster should remain available for the user to enter another command.

7. Error-message clarity: Input-error messages should identify the invalid or missing input and explain the expected format or accepted values, so that users can correct their commands without interpreting technical exception messages.

8. Terminology consistency: The user interface, command feedback, User Guide, and Developer Guide should use consistent terms for students, tutorial groups, sessions, attendance, participation, and assignment completion.


### Glossary

* **Attendance**: The record that a student was present or absent at one session.
* **Attendance percentage**: The number of sessions a student was marked present for, divided by the number of sessions the student has been marked for. Unmarked sessions are excluded.
* **Attendance strip**: The row on a student's entry that shows one cell per session of their tutorial group, in teaching-week order: `P` for present, `A` for absent and `—` for unmarked.
* **Course coordinator**: The staff member in charge of a module, who collects attendance records from the TAs.
* **Index**: The one-based position of a student in the currently displayed list. It is not a property of the student: the same student's index changes when the list is filtered.
* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Matriculation number**: The unique identifier NUS assigns to each student, e.g. `A0287654J`. Roster uses it to tell apart students with the same name.
* **Participation score**: A measure of how much a student contributed during a session, recorded alongside attendance.
* **Present / Absent**: The two attendance statuses a TA can record for a student at a session.
* **Session**: A record that a tutorial group met in a particular teaching week, together with the date of that meeting. A tutorial group has at most one session per teaching week.
* **Student**: A person enrolled in one of the TA's tutorial groups. Each student belongs to exactly one tutorial group.
* **Teaching assistant (TA)**: The target user of Roster. A person who conducts tutorials for a module, usually for one to three tutorial groups of around 20 students each.
* **Teaching week**: One of the 13 weeks of instruction in an NUS semester, numbered 1 to 13. Recess week is not a teaching week.
* **Tutorial group**: A fixed set of students who meet weekly with a TA. It is identified by a group code made up of one or two letters followed by two digits, e.g. `T09`.
* **Unmarked**: The state of a student at a session for which the TA has not yet recorded present or absent. It is not stored; it is the absence of an attendance record. It is kept distinct from absent so that unprocessed records are never mistaken for absences.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
