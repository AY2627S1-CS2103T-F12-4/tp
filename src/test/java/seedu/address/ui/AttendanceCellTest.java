package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_1;
import static seedu.address.testutil.TypicalSessions.T09_WEEK_2;
import static seedu.address.testutil.TypicalSessions.T10_WEEK_1;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.attendance.Status;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AttendanceCellTest {
    @Test
    public void forStudent_unmarkedStudent_usesAllGroupSessionsInWeekOrder() {
        assertEquals(List.of(new AttendanceCell(1, "\u2014"), new AttendanceCell(2, "\u2014")),
                AttendanceCell.forStudent(ALICE, List.of(T09_WEEK_2, T10_WEEK_1, T09_WEEK_1)));
    }

    @Test
    public void forStudent_recordedAndMissingAttendance_areDistinct() {
        Person student = new PersonBuilder(ALICE).withAttendance(T09_WEEK_1, Status.PRESENT).build();
        assertEquals(List.of(new AttendanceCell(1, "P"), new AttendanceCell(2, "\u2014")),
                AttendanceCell.forStudent(student, List.of(T09_WEEK_1, T09_WEEK_2)));
        Person updated = new PersonBuilder(student).withAttendance(T09_WEEK_2, Status.ABSENT).build();
        assertEquals(List.of(new AttendanceCell(1, "P"), new AttendanceCell(2, "A")),
                AttendanceCell.forStudent(updated, List.of(T09_WEEK_1, T09_WEEK_2)));
    }

    @Test
    public void forStudent_noSessionsForGroup_hasNoCells() {
        assertEquals(List.of(), AttendanceCell.forStudent(ALICE, List.of(T10_WEEK_1)));
    }
}
