package service;

import dto.CandidateEnrollmentDTO;
import dto.ExaminerCandidateRowDTO;
import dto.payload.CandidateCallDataDTO;
import dto.payload.CandidateSummaryDTO;
import enums.ExamSection;
import java.util.List;
import java.util.Map;

public interface ExaminerDataService {

    CandidateCallDataDTO getCandidateCallData(int sessionId, Integer sbd);

    CandidateCallDataDTO getCandidateCallData(int sessionId, Integer sbd, String searchQuery);

    List<ExaminerCandidateRowDTO> loadCandidateRows(int sessionId);

    List<ExaminerCandidateRowDTO> loadCandidateRows(int sessionId, boolean isTheory, String sectionName);

    CandidateSummaryDTO buildCandidateSummary(int sessionId, boolean isTheory, String sectionName);

    Map<String, Object> getAuditLogsData(int sessionId, String pageParam);

    Map<String, Object> getAuditLogsData(int sessionId, String pageParam, String searchQuery);

    Map<String, Object> getPaperAnswersData(int sessionId, int sbd, String contextPath);

    int theoryPassThreshold();

    int theoryMaxQuestions();

    CandidateEnrollmentDTO findRegistration(int sessionId, int sbd);

    ExaminerCandidateRowDTO getCandidateViewRow(int sessionId, int sbd, boolean isTheory, String sectionName);

    Map<String, Object> getScoreEntryData(int sessionId, Integer sbd, String sectionName);

    Map<String, Object> getResultDetailsEditData(int sessionId, Integer sbd);

    boolean isScoreQueueEligible(int sessionId, CandidateEnrollmentDTO reg,
            boolean isTheory, String sectionName);

    Map<String, Object> getViolationData(int sessionId, Integer sbd);

    Map<String, Object> getDevicesData(int sessionId, String searchQuery);

    Map<String, Object> getDevicesData(int sessionId, String searchQuery, Integer preferredAreaId);

    boolean isCallEligible(int sessionId, CandidateEnrollmentDTO reg, boolean isTheory, String sectionName);

    List<ExaminerCandidateRowDTO> orderCandidateRowsByQueue(List<ExaminerCandidateRowDTO> rows,
            ExamSection examSection);
}
