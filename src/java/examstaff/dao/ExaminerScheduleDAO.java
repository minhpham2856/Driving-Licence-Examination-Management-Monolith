package examstaff.dao;

import examstaff.model.ExaminerSchedule;
import java.sql.Date;
import java.util.List;
import java.util.Set;

public interface ExaminerScheduleDAO {

    boolean insert(ExaminerSchedule schedule);

    // --- CleanMyBranch methods (ưu tiên) ---
    boolean delete(int examinerScheduleId);

    boolean deleteBySlot(int examId, int areaId, int examinerId);

    List<ExaminerSchedule> getByExamId(int examId);

    List<ExaminerSchedule> getByExaminerId(int examinerId);

    List<ExaminerSchedule> getByExamIds(List<Integer> examIds);

    List<ExaminerSchedule> findByExamDate(Date examDate);

    Set<Integer> findBusyExaminerIdsByExamDate(Date examDate);

    List<ExaminerSchedule> findInProgressByExaminerId(int examinerUserId);

    // --- mainTest-only methods ---
    /** Alias của {@link #getByExaminerId(int)} */
    List<ExaminerSchedule> findByExaminerId(int examinerUserId);

    ExaminerSchedule getById(int examinerScheduleId);

    ExaminerSchedule getScheduleByExaminerAndExam(int examinerId, int examId);
}
