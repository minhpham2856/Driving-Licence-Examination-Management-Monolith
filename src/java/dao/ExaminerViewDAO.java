package dao;
import java.util.List;
import java.util.Map;
public interface ExaminerDataDAO {
    String findLicenceClassByExamId(int examId);
    Integer findPrimarySessionAreaId(int sessionId);
    Map<Integer, int[]> loadTheoryStatsBySession(int sessionId);
    Map<Integer, Double> loadSectionScoresBySession(int sessionId, String sectionName);
    Map<Integer, Boolean> loadPassFlagsBySession(int sessionId);
    Map<Integer, String> loadDeviceNamesBySession(int sessionId);
    List<Map<String, Object>> loadScoreDeductionRules(String sectionName, int sessionId);
    Map<Integer, int[]> loadDeductionOccurrences(int candidateId, int sessionId);
    Map<Integer, java.util.Date> loadDeductionRecordedAt(int candidateId, int sessionId);
    Map<String, Object> loadScoreSummary(int candidateId, int sessionId, String sectionName);
}
