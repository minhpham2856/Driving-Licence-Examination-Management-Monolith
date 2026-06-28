package dao;

import model.exam.ExaminerSchedule;
import java.util.List;

public interface ExaminerScheduleDAO {

    boolean insert(ExaminerSchedule schedule);

    boolean delete(int examinerScheduleId);

    List<ExaminerSchedule> getBySessionId(int sessionId);

    List<ExaminerSchedule> getByExaminerId(int examinerId);

    List<ExaminerSchedule> getBySessionIds(List<Integer> sessionIds);
}
