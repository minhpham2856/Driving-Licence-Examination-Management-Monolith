package examstaff.dao;

import examstaff.dto.ExaminerSlotDTO;

import examstaff.dto.user.UserDTO;

import java.util.List;

// DAO interface for managing examiner-to-session assignments.
public interface ExaminerAssignmentDAO {

    // Returns all active examiners (user accounts with Role = 'Examiner' and Status = 1).
    List<UserDTO> getActiveExaminers();

    // Assigns an examiner to a session slot.
    boolean assign(ExaminerSlotDTO slot);

    // Removes an examiner assignment by its slot key.
    boolean remove(String slotKey);

    // Returns all assignment slots for a given session.
    List<ExaminerSlotDTO> getByExamId(int examId);

    // Returns the currently in-progress assignments for a specific examiner.
    List<ExaminerSlotDTO> getInProgressAssignmentsForExaminer(int examinerUserId);
}
