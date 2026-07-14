package examstaff.dao;

import examstaff.dto.ExaminerSlotDTO;

import examstaff.dto.UserDTO;

import java.util.List;

/** DAO phân công sát hạch viên theo kỳ thi (Exam). */
public interface ExaminerAssignmentDAO {

    List<UserDTO> getActiveExaminers();

    boolean assign(ExaminerSlotDTO slot);

    boolean remove(String slotKey);

    List<ExaminerSlotDTO> getByExamId(int examId);
}
