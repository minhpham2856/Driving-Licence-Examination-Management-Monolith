package dao;

import controller.staff.exam.ExaminerSlot;
import model.user.User;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExaminerAssignmentDAO {

    List<User> getActiveExaminers();
    boolean assign(ExaminerSlot slot);
    boolean remove(String slotKey);
    List<ExaminerSlot> getBySessionId(int sessionId);
    List<ExaminerSlot> getInProgressAssignmentsForExaminer(int examinerUserId);
    List<ExaminerSlot> getByExamDate(Date examDate, Map<Integer, Date> sessionDates);
    Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates);
}
