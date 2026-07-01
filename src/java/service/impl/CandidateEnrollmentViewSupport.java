package service.impl;
import dao.CandidateDAO;
import dao.ExamEnrollmentDAO;
import dao.impl.CandidateDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dto.CandidateEnrollmentDTO;
import dto.CandidateProfileDTO;
import model.Candidate;
import model.ExamEnrollment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public final class CandidateEnrollmentViewSupport {
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    public List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId) {
        List<ExamEnrollment> enrollments = enrollmentDAO.getBySessionId(sessionId);
        if (enrollments.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> candidateIds = enrollments.stream()
                .map(ExamEnrollment::getCandidateId)
                .collect(Collectors.toList());
        Map<Integer, Candidate> candidates = new HashMap<>();
        for (Candidate candidate : candidateDAO.getAllByIds(candidateIds)) {
            candidates.put(candidate.getCandidateId(), candidate);
        }
        List<CandidateEnrollmentDTO> list = new ArrayList<>();
        for (ExamEnrollment enrollment : enrollments) {
            Candidate candidate = candidates.get(enrollment.getCandidateId());
            if (candidate != null) {
                list.add(toDto(candidate, enrollment));
            }
        }
        list.sort(Comparator.comparingInt(CandidateEnrollmentDTO::getSbd));
        return list;
    }
    public CandidateEnrollmentDTO toDto(Candidate candidate, ExamEnrollment enrollment) {
        CandidateProfileDTO profile = new CandidateProfileDTO();
        profile.setCandidateId(candidate.getCandidateId());
        profile.setCandidateNumber(parseCandidateNumber(candidate.getCandidateNumber()));
        profile.setFullName(candidate.getFullName());
        profile.setGovernmentIdNumber(candidate.getGovernmentIdNumber());
        profile.setAbsent(candidate.isAbsent());
        profile.setSuspended(candidate.isSuspended());
        profile.setPhotoImageUrl(candidate.getPhotoImageUrl());
        CandidateEnrollmentDTO dto = new CandidateEnrollmentDTO(profile, enrollment);
        dto.setDateOfBirth(candidate.getDateOfBirth());
        dto.setPhoneNo(candidate.getPhoneNumber());
        dto.setAddress(candidate.getAddress());
        dto.setReasonForTaking(candidate.getReasonForTaking());
        dto.setSex(candidate.isSex());
        return dto;
    }
    private static int parseCandidateNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
