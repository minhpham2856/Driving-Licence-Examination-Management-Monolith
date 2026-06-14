package Services;

import Constants.ExamSectionType;
import Models.ExamRegistration;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * Loads examiner portal view models (candidates list, summary, detail) from DB.
 */
public interface ExaminerViewDataService {

    void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam);

    void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam, String searchQuery);

    List<Map<String, Object>> loadCandidateRows(int sessionId);

    List<Map<String, Object>> loadCandidateRows(int sessionId, ExamSectionType sectionType, String sectionName);

    Map<String, Object> buildCandidateSummary(int sessionId, ExamSectionType sectionType, String sectionName);

    void attachAuditLogs(HttpServletRequest request, int sessionId, String pageParam);

    void attachAuditLogs(HttpServletRequest request, int sessionId, String pageParam, String searchQuery);

    void attachPaperAnswers(HttpServletRequest request, int sessionId, String sbd, String contextPath);

    int theoryPassThreshold();

    int theoryMaxQuestions();

    ExamRegistration findRegistration(int sessionId, String sbd);

    void attachScoreEntry(HttpServletRequest request, int sessionId, String sbdParam);

    boolean isScoreQueueEligible(int sessionId, ExamRegistration reg,
            ExamSectionType sectionType, String sectionName);

    void attachViolation(HttpServletRequest request, int sessionId, String sbdParam);

    void attachDevices(HttpServletRequest request, int sessionId, String searchQuery);

    boolean isCallEligible(int sessionId, ExamRegistration reg, ExamSectionType sectionType, String sectionName);
}
