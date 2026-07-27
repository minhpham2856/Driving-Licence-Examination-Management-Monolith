package examiner.dao;

import shared.model.ExaminerSchedule;
import java.sql.Date;
import java.util.List;
import java.util.Set;

// DAO contract for ExaminerSchedule persistence; examiner module SQL boundary.
public interface ExaminerScheduleDAO {

    // Inserts a new examiner schedule assignment row.
    boolean add(ExaminerSchedule schedule);

    // Removes one examiner assignment for an exam/area slot.
    boolean deleteBySlot(int examId, int areaId, int examinerId);

    // Lists all schedule rows for one exam.
    List<ExaminerSchedule> getByExamId(int examId);

    // Lists schedule rows for exams starting on one calendar date.
    List<ExaminerSchedule> getAllByExamDate(Date examDate);

    // Returns examiner user ids already scheduled on one exam date.
    Set<Integer> getAllBusyExaminerIdsByExamDate(Date examDate);

    // Lists in-progress exam schedule rows for one examiner.
    List<ExaminerSchedule> getAllInProgressByExaminer(int examinerUserId);

    // Lists all schedule rows for one examiner ordered by exam start time.
    List<ExaminerSchedule> getAllByExaminer(int examinerUserId);

    // Loads one schedule row by primary key.
    ExaminerSchedule get(int examinerScheduleId);

    // Loads the schedule row linking one examiner to one exam.
    ExaminerSchedule getIfByExaminerAndExam(int examinerId, int examId);
}
