package Services;

import Utils.ExamConstants.SectionType;
import DTOs.CandidateDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

// Contract for loading and attaching examiner view data to HTTP requests for JSP rendering
public interface ExaminerViewDataService {

    // Loads all dashboard view models (candidates, summary, audit, paper answers) and sets request attributes
    void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam);

    // Same as above but also filters audit logs by a search keyword
    void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam, String searchQuery);

    // Returns the flat candidate row list for the examiner table (no request binding)
    List<Map<String, Object>> loadCandidateRows(int sessionId);

    // Returns candidate rows filtered by exam section type and name
    List<Map<String, Object>> loadCandidateRows(int sessionId, SectionType sectionType, String sectionName);

    // Builds a summary statistics map (total, done, testing, pending, passed, failed)
    Map<String, Object> buildCandidateSummary(int sessionId, SectionType sectionType, String sectionName);

    // Loads paginated audit logs and attaches them to the request as attributes
    void attachAuditLogs(HttpServletRequest request, int sessionId, String pageParam);

    // Same as above but with an additional search query filter for audit log entries
    void attachAuditLogs(HttpServletRequest request, int sessionId, String pageParam, String searchQuery);

    // Loads a candidate's theory paper answers and attaches them to the request for review display
    void attachPaperAnswers(HttpServletRequest request, int sessionId, String sbd, String contextPath);

    // Returns the minimum number of correct answers required to pass the theory exam
    int theoryPassThreshold();

    // Returns the total number of questions in the theory exam (maximum possible score)
    int theoryMaxQuestions();

    // Finds a candidate registration by session and SBD (returns null if not found)
    CandidateDTO findRegistration(int sessionId, String sbd);

    // Loads score-entry view data (deductions, devices, queue) and attaches to the request
    void attachScoreEntry(HttpServletRequest request, int sessionId, String sbdParam);

    // Loads result-details-edit view data (score history, reason codes) and attaches to the request
    void attachResultDetailsEdit(HttpServletRequest request, int sessionId, String sbdParam);

    // Checks whether a candidate is eligible to enter the score-entry queue for the given section
    boolean isScoreQueueEligible(int sessionId, CandidateDTO reg,
            SectionType sectionType, String sectionName);

    // Loads violation entry data (deduction rules, candidate info) and attaches to the request
    void attachViolation(HttpServletRequest request, int sessionId, String sbdParam);

    // Loads the device list filtered by search query and attaches to the request
    void attachDevices(HttpServletRequest request, int sessionId, String searchQuery);

    // Checks whether a candidate is eligible to be called for the given section
    boolean isCallEligible(int sessionId, CandidateDTO reg, SectionType sectionType, String sectionName);
}
