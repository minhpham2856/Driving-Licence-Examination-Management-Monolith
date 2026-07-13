package examstaff.dao;

import java.util.List;
import examstaff.model.ExamEnrollmentSection;

public interface ExamEnrollmentSectionDAO {
    ExamEnrollmentSection getById(int id);
    ExamEnrollmentSection getByExamEnrollmentId(int examEnrollmentId);
    List<ExamEnrollmentSection> getByExamAndSection(int examId, int examSectionId);
    boolean updateStatus(int examEnrollmentSectionId, String status);
}
