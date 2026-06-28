package service.impl;

import dao.CandidateDAO;
import dao.ExamEnrollmentDAO;
import dao.DeductionRecordDAO;
import dao.impl.CandidateDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.DeductionRecordDAOImpl;
import dto.candidate.CandidateEnrollmentDTO;
import dto.candidate.CandidateProfileDTO;
import model.candidate.Candidate;
import model.exam.ExamEnrollment;
import service.ExamRegistrationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamRegistrationServiceImpl implements ExamRegistrationService {

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final DeductionRecordDAO deductionDAO = new DeductionRecordDAOImpl();

    @Override
    public CandidateEnrollmentDTO getBySessionAndSbd(int sessionId, String sbd) {
        Candidate c = candidateDAO.findByNumber(sbd);
        if (c == null) return null;
        ExamEnrollment e = enrollmentDAO.findBySessionAndCandidate(sessionId, c.getCandidateId());
        if (e == null) return null;
        return mapToDTO(c, e);
    }

    @Override
    public List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId) {
        List<ExamEnrollment> enrollments = enrollmentDAO.findBySessionId(sessionId);
        if (enrollments == null || enrollments.isEmpty()) return new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (ExamEnrollment e : enrollments) ids.add(e.getCandidateId());
        List<Candidate> candidates = candidateDAO.findByIds(ids);
        Map<Integer, Candidate> map = new HashMap<>();
        for (Candidate c : candidates) map.put(c.getCandidateId(), c);
        List<CandidateEnrollmentDTO> result = new ArrayList<>();
        for (ExamEnrollment e : enrollments) {
            Candidate c = map.get(e.getCandidateId());
            if (c != null) result.add(mapToDTO(c, e));
        }
        return result;
    }

    private CandidateEnrollmentDTO mapToDTO(Candidate c, ExamEnrollment e) {
        CandidateProfileDTO p = new CandidateProfileDTO();
        p.setCandidateId(c.getCandidateId());
        p.setCandidateNumber(c.getCandidateNumber());
        p.setFullName(c.getFullName());
        p.setGovernmentIdNumber(c.getGovernmentIdNumber());
        p.setAbsent(c.isAbsent());
        p.setSuspended(c.isSuspended());
        p.setPhotoImageUrl(c.getPhotoImageUrl());
        return new CandidateEnrollmentDTO(p, e);
    }

    @Override
    public boolean updateProfile(int candidateId, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo) {
        Candidate c = candidateDAO.findById(candidateId);
        if (c == null) return false;
        c.setFullName(fullName);
        if (dob != null) c.setDateOfBirth(new java.sql.Timestamp(dob.getTime()));
        c.setGovernmentIdNumber(govIdNo);
        c.setPhoneNumber(phoneNo);
        return candidateDAO.update(c);
    }

    @Override
    public boolean updatePhoto(int candidateId, String photoUrl) {
        Candidate c = candidateDAO.findById(candidateId);
        if (c == null) return false;
        c.setPhotoImageUrl(photoUrl);
        return candidateDAO.update(c);
    }

    @Override
    public boolean markAbsent(int candidateId) {
        Candidate c = candidateDAO.findById(candidateId);
        if (c == null) return false;
        c.setAbsent(true);
        return candidateDAO.update(c);
    }

    @Override
    public boolean clearAbsentMarking(int candidateId) {
        Candidate c = candidateDAO.findById(candidateId);
        if (c == null) return false;
        c.setAbsent(false);
        return candidateDAO.update(c);
    }

    @Override
    public boolean markSuspended(int candidateId) {
        Candidate c = candidateDAO.findById(candidateId);
        if (c == null) return false;
        c.setSuspended(true);
        return candidateDAO.update(c);
    }

    @Override
    public boolean undoSuspension(int candidateId) {
        Candidate c = candidateDAO.findById(candidateId);
        if (c == null) return false;
        c.setSuspended(false);
        return candidateDAO.update(c);
    }

    @Override
    public List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId) {
        return deductionDAO.findAppliedScoreDeductions(candidateId, sessionId);
    }
}
