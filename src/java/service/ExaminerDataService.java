package service;
import dto.CandidateEnrollmentDTO;
import java.util.List;
import java.util.Map;
public interface ExaminerDataService {
    Map<String, Object> getCandidateCallData(int sessionId, Integer sbd);
    Map<String, Object> getCandidateCallData(int sessionId, Integer sbd, String searchQuery);
    List<Map<String, Object>> loadCandidateRows(int sessionId);
    List<Map<String, Object>> loadCandidateRows(int sessionId, boolean isTheory, String sectionName);
    Map<String, Object> buildCandidateSummary(int sessionId, boolean isTheory, String sectionName);
    Map<String, Object> getAuditLogsData(int sessionId, String pageParam);
    Map<String, Object> getAuditLogsData(int sessionId, String pageParam, String searchQuery);
    Map<String, Object> getPaperAnswersData(int sessionId, int sbd, String contextPath);
    int theoryPassThreshold();
    int theoryMaxQuestions();
    CandidateEnrollmentDTO findRegistration(int sessionId, int sbd);
    Map<String, Object> getScoreEntryData(int sessionId, Integer sbd, String sectionName);
    Map<String, Object> getResultDetailsEditData(int sessionId, Integer sbd);
    boolean isScoreQueueEligible(int sessionId, CandidateEnrollmentDTO reg,
            boolean isTheory, String sectionName);
    Map<String, Object> getViolationData(int sessionId, Integer sbd);
    Map<String, Object> getDevicesData(int sessionId, String searchQuery);
    Map<String, Object> getDevicesData(int sessionId, String searchQuery, Integer preferredAreaId);
    boolean isCallEligible(int sessionId, CandidateEnrollmentDTO reg, boolean isTheory, String sectionName);
    List<Map<String, Object>> orderCandidateRowsByQueue(List<Map<String, Object>> rows, boolean isTheory,
            String sectionName);
}
