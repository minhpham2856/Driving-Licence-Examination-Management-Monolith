package policestaff.service;

import java.util.List;
import policestaff.dto.PoliceSubmissionDTO;
import policestaff.dto.PoliceCandidateDTO;
import policestaff.dto.OfficialExamCandidateDTO;
import policestaff.dto.OfficialRosterPublishResult;

public interface PoliceDashboardService {
    List<PoliceSubmissionDTO> loadSubmissions(int limit);
    List<PoliceSubmissionDTO> loadSubmissions(String status, Integer year, int page, int pageSize);
    int countSubmissions(String status, Integer year);
    int countPendingCandidates();
    List<Integer> loadCompletedYears();
    PoliceSubmissionDTO findSubmission(int examDateId);
    List<PoliceCandidateDTO> loadCandidates(int examDateId);
    List<PoliceCandidateDTO> loadCandidates(int examDateId, int page, int pageSize);
    int countCandidates(int examDateId);
    boolean review(int registrationDateId, String decision, String reason,
            String participationType);
    OfficialRosterPublishResult complete(int examDateId);
    List<OfficialExamCandidateDTO> loadOfficialCandidates(int examDateId);
    List<OfficialExamCandidateDTO> loadOfficialCandidates(int examDateId, int page, int pageSize);
    int countOfficialCandidates(int examDateId);
}
