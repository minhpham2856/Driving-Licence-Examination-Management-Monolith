package examiner.dao;
import java.util.List;
import java.util.Map;
import shared.model.ExamArea;

// DAO contract for ExaminerView persistence; examiner module SQL boundary.
public interface ExaminerViewDAO {
    // Returns the first exam area id linked to one exam (Exam_ExamArea).
    ExamArea getIfPrimaryByExam(int examId);

    // Batch-loads theory answer stats [correct, wrong, unanswered] per enrollment.
    Map<Integer, int[]> getAllTheoryStatsByExam(int examId);

    // Batch-loads section scores per enrollment for one exam and section type.
    Map<Integer, Double> getAllSectionScoresByExam(int examId, String sectionType);

    // Batch-loads overall pass flags per enrollment for one exam.
    Map<Integer, Boolean> getAllPassFlagsByExam(int examId);

    // Batch-loads device id to device name for devices used in one exam.
    Map<Integer, String> getAllDeviceNamesByExam(int examId);

    // Loads score deduction rules for one section type and exam licence.
    List<Map<String, Object>> getAllScoreDeductionRulesByExam(String sectionType, int examId);

    // Loads deduction occurrence counts keyed by ScoreDeductionId for one candidate/exam.
    Map<Integer, int[]> getAllDeductionOccurrencesByExam(int candidateId, int examId);

    // Loads current score and critical-disqualification flag for one candidate section.
    Map<String, Object> getIfScoreSummaryByCandidateAndExam(int candidateId, int examId, String sectionType);
}
