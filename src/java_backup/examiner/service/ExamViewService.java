package examiner.service;

import examiner.dto.ExamStatsDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.CandidateRowDTO;
import examiner.dto.ExamReportDTO;
import examiner.enums.SectionType;
import java.util.List;
import java.util.Map;

public interface ExamViewService {

    List<CandidateRowDTO> loadCandidateRows(int examId);

    List<CandidateRowDTO> loadCandidateRows(int examId, boolean isTheory, String sectionName);

    List<CandidateRowDTO> loadCandidateRows(int examId, boolean isTheory, String sectionName,
            String searchQuery);

    ExamStatsDTO buildCandidateSummary(int examId, boolean isTheory, String sectionName);

    // Builds the end-of-day exam report for an exam (result + licence breakdown
    // + top deduction reasons). Used by the examstaff report screen.
    ExamReportDTO buildExamReport(int examId);

    Map<String, Object> getAuditLogsData(int examId, String pageParam);

    Map<String, Object> getAuditLogsData(int examId, String pageParam, String searchQuery);

    Map<String, Object> getPaperAnswersData(int examId, int sbd, String contextPath);

    int theoryPassThreshold();

    int theoryMaxQuestions();

    EnrollmentDTO findRegistration(int examId, int sbd);

    CandidateRowDTO getCandidateViewRow(int examId, int sbd, boolean isTheory, String sectionName);

    Map<String, Object> getScoreEntryData(int examId, Integer sbd, String sectionName);

    Map<String, Object> getResultDetailsEditData(int examId, Integer sbd);

    boolean isScoreQueueEligible(int examId, EnrollmentDTO reg,
            boolean isTheory, String sectionName);

    Map<String, Object> getViolationData(int examId, Integer sbd);

    Map<String, Object> getDevicesData(int examId, String searchQuery);

    Map<String, Object> getDevicesData(int examId, String searchQuery, Integer preferredAreaId);

    boolean isCallEligible(int examId, EnrollmentDTO reg, boolean isTheory, String sectionName);

    List<CandidateRowDTO> orderCandidateRowsByQueue(List<CandidateRowDTO> rows,
            SectionType examSection);
}
