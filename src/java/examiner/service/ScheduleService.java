package examiner.service;

import shared.model.ExaminerSchedule;

import java.util.List;

public interface ScheduleService {

    List<ExaminerSchedule> getSchedulesByExaminerId(int examinerUserId);

    ExaminerSchedule getScheduleById(int examinerScheduleId);

    ExaminerSchedule getScheduleByExaminerAndExam(int examinerId, int examId);
}

