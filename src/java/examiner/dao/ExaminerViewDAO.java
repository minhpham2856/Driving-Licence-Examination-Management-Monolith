package examiner.dao;
import java.util.List;
import java.util.Map;
public interface ExaminerViewDAO {
    String findLicenceClassByExamId(int examId);
    Integer findPrimaryExamAreaId(int examId);
    List<Integer> findExamAreaIds(int examId);
    Map<Integer, int[]> loadTheoryStatsByExam(int examId);
    Map<Integer, Double> loadSectionScoresByExam(int examId, String sectionName);
    Map<Integer, Boolean> loadPassFlagsByExam(int examId);
    Map<Integer, String> loadDeviceNamesByExam(int examId);
    List<Map<String, Object>> loadScoreDeductionRules(String sectionName, int examId);
    Map<Integer, int[]> loadDeductionOccurrences(int candidateId, int examId);
    Map<Integer, java.util.Date> loadDeductionRecordedAt(int candidateId, int examId);
    Map<String, Object> loadScoreSummary(int candidateId, int examId, String sectionName);
}
