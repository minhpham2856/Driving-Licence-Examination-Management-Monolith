package service;

import enums.SectionType;
import dto.candidate.CandidateEnrollmentDTO;


import java.util.List;
import java.util.Map;

public interface ExaminerDataService {

    Map<String, Object> getCandidateCallData(int sessionId, String sbdParam);

    Map<String, Object> getCandidateCallData(int sessionId, String sbdParam, String searchQuery);

    List<Map<String, Object>> loadCandidateRows(int sessionId);

    List<Map<String, Object>> loadCandidateRows(int sessionId, SectionType sectionType, String sectionName);

    Map<String, Object> buildCandidateSummary(int sessionId, SectionType sectionType, String sectionName);

    Map<String, Object> getAuditLogsData(int sessionId, String pageParam);

    Map<String, Object> getAuditLogsData(int sessionId, String pageParam, String searchQuery);

    Map<String, Object> getPaperAnswersData(int sessionId, String sbd, String contextPath);

    int theoryPassThreshold();

    int theoryMaxQuestions();

    CandidateEnrollmentDTO findRegistration(int sessionId, String sbd);

    Map<String, Object> getScoreEntryData(int sessionId, String sbdParam);

    Map<String, Object> getResultDetailsEditData(int sessionId, String sbdParam);

    boolean isScoreQueueEligible(int sessionId, CandidateEnrollmentDTO reg,
            SectionType sectionType, String sectionName);

    Map<String, Object> getViolationData(int sessionId, String sbdParam);

    Map<String, Object> getDevicesData(int sessionId, String searchQuery);

    boolean isCallEligible(int sessionId, CandidateEnrollmentDTO reg, SectionType sectionType, String sectionName);
}

