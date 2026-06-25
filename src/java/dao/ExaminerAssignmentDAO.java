package dao;

import dto.examiner.ExaminerSlotDTO;

import dto.user.UserDTO;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

// DAO interface for managing examiner-to-session assignments.
public interface ExaminerAssignmentDAO {

    // Returns all active examiners (user accounts with Role = 'Examiner' and Status = 1).
    List<UserDTO> getActiveExaminers();

    // Assigns an examiner to a session slot.
    boolean assign(ExaminerSlotDTO slot);

    // Removes an examiner assignment by its slot key.
    boolean remove(String slotKey);

    // Returns all assignment slots for a given session.
    List<ExaminerSlotDTO> getBySessionId(int sessionId);

    // Returns the currently in-progress assignments for a specific examiner.
    List<ExaminerSlotDTO> getInProgressAssignmentsForExaminer(int examinerUserId);

    // Returns assignment slots for sessions that occur on the given date.
    List<ExaminerSlotDTO> getByExamDate(Date examDate, Map<Integer, Date> sessionDates);

    // Returns the set of examiner user IDs who are busy (already assigned)
    Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates);
}
