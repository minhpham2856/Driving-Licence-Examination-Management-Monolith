package policestaff.dao;

import java.util.List;
import policestaff.dto.PoliceSubmissionDTO;
import policestaff.dto.PoliceCandidateDTO;
import policestaff.dto.OfficialExamCandidateDTO;

public interface PoliceSubmissionDAO {
    List<PoliceSubmissionDTO> findRecentSubmissions(int limit);
    List<PoliceSubmissionDTO> findSubmissions(String policeStatus, Integer year, int offset, int limit);
    int countSubmissions(String policeStatus, Integer year);
    int countPendingCandidates();
    List<Integer> findCompletedYears();
    PoliceSubmissionDTO findById(int examDateId);
    List<PoliceCandidateDTO> findCandidates(int examDateId);
    List<PoliceCandidateDTO> findCandidates(int examDateId, int offset, int limit);
    int countCandidates(int examDateId);
    int reviewCandidate(int registrationDateId, String decision, String reason,
            String participationType);
    int completeSubmission(int examDateId);
    List<OfficialExamCandidateDTO> findOfficialCandidates(int examDateId);
    List<OfficialExamCandidateDTO> findOfficialCandidates(int examDateId, int offset, int limit);
    int countOfficialCandidates(int examDateId);
    List<String> findActiveManagingStaffEmails();
    boolean canAccessDocument(int documentId);
}
