package examiner.service;

import examiner.dto.ExamStatsDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.CandidateRowDTO;
import shared.enums.SectionType;
import java.util.List;
import java.util.Map;

// Service contract for loading and shaping examiner screen data: lists, stats, audit, scoring, and devices.
public interface ExamViewService {

    // Load candidate rows for the active section; searchQuery nullable when unused.
    List<CandidateRowDTO> getAllFilteredByExam(int examId, SectionType sectionType, String searchQuery);

    // Load lightweight candidate rows for row-level actions without queue/area ordering.
    List<CandidateRowDTO> getActionCandidateListByExam(int examId, SectionType sectionType, String searchQuery);

    // Load simple dashboard rows with only candidates belonging to the exam.
    List<CandidateRowDTO> getDashboardCandidateListByExam(int examId, SectionType sectionType, String searchQuery);

    // Builds aggregate counts (total, done, testing, pending, passed, failed) for the exam section.
    ExamStatsDTO getStatsByExam(int examId, SectionType sectionType);

    // Builds aggregate counts from rows already loaded for a page.
    ExamStatsDTO getStatsByCandidateRows(int examId, SectionType sectionType, List<CandidateRowDTO> rows);

    // Loads paginated audit log rows for the exam (page 1 when search is unused).
    Map<String, Object> getAuditViewByExam(int examId, String pageParam);

    // Loads paginated audit log rows with optional keyword search.
    Map<String, Object> getAuditViewByExam(int examId, String pageParam, String searchQuery);

    // Loads theory paper answer detail and summary counts for one candidate.
    Map<String, Object> getPaperAnswersData(int examId, int sbd, String contextPath);

    // Returns the minimum correct-answer count required to pass the theory exam.
    int theoryPassThreshold();

    // Returns the total number of questions on the theory exam paper.
    int theoryMaxQuestions();

    // Finds enrollment by exam and SBD using default section context.
    EnrollmentDTO getIfByExamAndSbd(int examId, int sbd);

    // Finds enrollment by exam, SBD, and active section type.
    EnrollmentDTO getIfByExamAndSbd(int examId, int sbd, SectionType sectionType);

    // Loads a single candidate row DTO for detail or action screens.
    CandidateRowDTO getCandidateViewRow(int examId, int sbd, SectionType sectionType);

    // Builds the score-entry view model: candidates, vehicles, deductions, and active SBD.
    Map<String, Object> getScoreEntryViewByExam(int examId, Integer sbd, SectionType sectionType);

    // Builds result-details edit data for layout section (default section overload).
    Map<String, Object> getResultDetailsViewByExam(int examId, Integer sbd);

    // Builds result-details edit data for the given section type.
    Map<String, Object> getResultDetailsViewByExam(int examId, Integer sbd, SectionType sectionType);

    // Returns whether a candidate may appear in the score-entry queue.
    boolean isScoreQueueEligible(int examId, EnrollmentDTO enrollment, SectionType sectionType);

    // Builds violation-handling view data with default theory section.
    Map<String, Object> getViolationViewByExam(int examId, Integer sbd);

    // Builds violation-handling view data for the given section type.
    Map<String, Object> getViolationViewByExam(int examId, Integer sbd, SectionType sectionType);

    // Lists exam devices with optional search (theory section default).
    Map<String, Object> getDeviceViewByExam(int examId, String searchQuery);

    // Lists exam devices filtered by preferred theory room area.
    Map<String, Object> getDeviceViewByExam(int examId, String searchQuery, Integer preferredAreaId);

    // Lists devices or vehicles depending on section type (computers vs practical vehicles).
    Map<String, Object> getDeviceViewByExam(int examId, String searchQuery, Integer preferredAreaId,
            SectionType sectionType);

    // Returns whether a candidate is eligible for call-board invoke actions.
    boolean isActionEligible(int examId, EnrollmentDTO enrollment, SectionType sectionType);

}
