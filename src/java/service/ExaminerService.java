package service;

import model.ExaminerSchedule;

import java.util.List;

public interface ExaminerService {

    List<ExaminerSchedule> getSchedulesByExaminerId(int examinerUserId);

    ExaminerSchedule getScheduleById(int examinerScheduleId);
}
