package dao.impl;

import model.*;

import dao.ExamEnrollmentDAO;
import model.ExamEnrollment;
import java.util.ArrayList;
import java.util.List;
import dto.CandidateEnrollmentDTO;

public class ExamEnrollmentDAOImpl implements ExamEnrollmentDAO {

    @Override
    public ExamEnrollment getById(int examEnrollmentId) { return null; }

    @Override
    public int insert(ExamEnrollment enrollment) { return 0; }

    @Override
    public boolean update(ExamEnrollment enrollment) { return false; }

    @Override
    public boolean delete(int examEnrollmentId) { return false; }

    @Override
    public int countAll() { return 0; }

    @Override
    public List<ExamEnrollment> getBySessionId(int sessionId) { return new ArrayList<>(); }

    @Override
    public List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId) { return new ArrayList<>(); }

    @Override
    public boolean updateExaminerProfile(int candidateId, String fullName, String dob, String govIdNo, String email, String phoneNo, String address, String sexDb, String reasonForTaking) { return false; }
    @Override
    public boolean markAbsent(int candidateId) { return false; }
    @Override
    public boolean clearAbsentMarking(int candidateId) { return false; }
    public boolean assignExamDevice(int regId, int sessionId, int deviceId) { return false; }
    public ExamEnrollment getBySessionAndCandidate(int sessionId, int candidateId) { return null; }
}


