package service;

import dto.ExamStatsDTO;
import dto.EnrollmentDTO;
import dto.CandidateRowDTO;
import dto.ExamReportDTO;
import enums.SectionType;
import java.util.List;
import java.util.Map;

public interface ExamViewService {

    List<CandidateRowDTO> loadCandidateRows(int sessionId);

    List<CandidateRowDTO> loadCandidateRows(int sessionId, boolean isTheory, String sectionName);

    List<CandidateRowDTO> loadCandidateRows(int sessionId, boolean isTheory, String sectionName,
            String searchQuery);

    ExamStatsDTO buildCandidateSummary(int sessionId, boolean isTheory, String sectionName);

    // Builds the end-of-day exam report for a session (result + licence breakdown
    // + top deduction reasons). Used by the examstaff report screen.
    ExamReportDTO buildExamReport(int sessionId);

    Map<String, Object> getAuditLogsData(int sessionId, String pageParam);

    Map<String, Object> getAuditLogsData(int sessionId, String pageParam, String searchQuery);

    Map<String, Object> getPaperAnswersData(int sessionId, int sbd, String contextPath);

    int theoryPassThreshold();

    int theoryMaxQuestions();

    EnrollmentDTO findRegistration(int sessionId, int sbd);

    CandidateRowDTO getCandidateViewRow(int sessionId, int sbd, boolean isTheory, String sectionName);

    Map<String, Object> getScoreEntryData(int sessionId, Integer sbd, String sectionName);

    Map<String, Object> getResultDetailsEditData(int sessionId, Integer sbd);

    boolean isScoreQueueEligible(int sessionId, EnrollmentDTO reg,
            boolean isTheory, String sectionName);

    Map<String, Object> getViolationData(int sessionId, Integer sbd);

    Map<String, Object> getDevicesData(int sessionId, String searchQuery);

    Map<String, Object> getDevicesData(int sessionId, String searchQuery, Integer preferredAreaId);

    boolean isCallEligible(int sessionId, EnrollmentDTO reg, boolean isTheory, String sectionName);

    List<CandidateRowDTO> orderCandidateRowsByQueue(List<CandidateRowDTO> rows,
            SectionType examSection);
}
