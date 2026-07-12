package dao;

import model.ExaminerSchedule;
import java.sql.Date;
import java.util.List;
import java.util.Set;

public interface ExaminerScheduleDAO {

    boolean insert(ExaminerSchedule schedule);

    boolean deleteBySlot(int examId, int areaId, int examinerId);

    List<ExaminerSchedule> getByExamId(int examId);

    List<ExaminerSchedule> findByExamDate(Date examDate);

    Set<Integer> findBusyExaminerIdsByExamDate(Date examDate);

    List<ExaminerSchedule> findInProgressByExaminerId(int examinerUserId);

    List<ExaminerSchedule> findByExaminerId(int examinerUserId);

    ExaminerSchedule getById(int examinerScheduleId);

    ExaminerSchedule getScheduleByExaminerAndExam(int examinerId, int examId);
}
