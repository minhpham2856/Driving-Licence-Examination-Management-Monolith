package dao;

import model.exam.ExaminerSchedule;
import java.util.List;
import dto.examiner.ExaminerSlotDTO;
import java.sql.Date;
import java.util.Map;
import java.util.Set;
import dto.user.UserDTO;

public interface ExaminerScheduleDAO {

    boolean insert(ExaminerSchedule schedule);

    boolean delete(int examinerScheduleId);

    List<ExaminerSchedule> getBySessionId(int sessionId);

    List<ExaminerSchedule> getByExaminerId(int examinerId);

    List<ExaminerSchedule> getBySessionIds(List<Integer> sessionIds);

    List<UserDTO> getActiveExaminers();
    List<ExaminerSlotDTO> getByExamDate(Date date, Map<Integer, Date> sessionDates);
    Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates);
    boolean assign(ExaminerSlotDTO slot);
    boolean remove(String slotKey);
    List<ExaminerSlotDTO> getInProgressAssignmentsForExaminer(int examinerUserId);
}

