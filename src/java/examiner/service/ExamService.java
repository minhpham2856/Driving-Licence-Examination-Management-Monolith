package examiner.service;

import shared.model.Exam;
import shared.model.ExamArea;
import shared.model.ExamSection;
import shared.model.ExaminerSchedule;
import shared.model.Licence;

import java.util.List;

// Service contract for exam session metadata, schedules, sections, areas, and licence classes.
public interface ExamService {

    Exam get(int examId);

    ExamSection getBySectionId(int sectionId);

    ExamArea getByAreaId(int areaId);

    Licence getByLicenceId(int licenceId);

    ExaminerSchedule getByScheduleId(int scheduleId);

    List<ExaminerSchedule> getAllByExaminer(int examinerUserId);

    ExaminerSchedule getIfByExaminerAndExam(int examinerId, int examId);
}
