package dao;
import model.ExaminerSchedule;
import java.sql.Date;
import java.util.List;
import java.util.Set;
public interface ExaminerScheduleDAO {
    boolean insert(ExaminerSchedule schedule);
    boolean delete(int examinerScheduleId);
    boolean deleteBySlot(int sessionId, int areaId, int examinerId);
    List<ExaminerSchedule> getByExamId(int examId);
    List<ExaminerSchedule> getByExaminerId(int examinerId);
    List<ExaminerSchedule> getByExamIds(List<Integer> examIds);
    List<ExaminerSchedule> findByExamDate(Date examDate);
    Set<Integer> findBusyExaminerIdsByExamDate(Date examDate);
    List<ExaminerSchedule> findInProgressByExaminerId(int examinerUserId);
}
