package service.impl;

import enums.SectionType;
import controller.examiner.ExaminerScoreEntryQueue;
import dto.examiner.ExaminerSlotDTO;
import dao.AuditDAO;
import dao.CandidateDAO;
import dao.TheoryPaperDAO;
import dao.ExamEnrollmentDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.CandidateDAOImpl;
import dao.impl.TheoryPaperDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dto.candidate.CandidateEnrollmentDTO;
import dto.examiner.ExaminerAnswerStatsDTO;
import dto.examiner.ExaminerPaperStateDTO;
import dto.score.TheoryPaperAnswerDTO;
import service.ExaminerDataService;
import model.user.Audit;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExaminerDataServiceImpl implements ExaminerDataService {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final int AUDIT_PAGE_SIZE = 20;
    private static final int THEORY_PASS_CORRECT = 32;
    private static final int THEORY_MAX_QUESTIONS = 35;

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();

    @Override
    public Map<String, Object> getCandidateCallData(int sessionId, String sbdParam) {
        return getCandidateCallData(sessionId, sbdParam, null);
    }

    @Override
    public Map<String, Object> getCandidateCallData(int sessionId, String sbdParam, String searchQuery) {
        Map<String, Object> model = new HashMap<>();
        List<CandidateEnrollmentDTO> registrations = enrollmentDAO.getCandidatesBySession(sessionId);
        
        List<Map<String, Object>> rows = new ArrayList<>();
        for (CandidateEnrollmentDTO reg : registrations) {
            Map<String, Object> row = new HashMap<>();
            row.put("sbd", reg.getSbd());
            row.put("fullName", reg.getCandidateName());
            row.put("dob", reg.getDob());
            rows.add(row);
        }
        model.put("candidateQueue", rows);
        return model;
    }

    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId, SectionType sectionType, String sectionName) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> buildCandidateSummary(int sessionId, SectionType sectionType, String sectionName) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getAuditLogsData(int sessionId, String pageParam) {
        return getAuditLogsData(sessionId, pageParam, null);
    }

    @Override
    public Map<String, Object> getAuditLogsData(int sessionId, String pageParam, String searchQuery) {
        Map<String, Object> model = new HashMap<>();
        List<Audit> logs = auditDAO.findAll();
        model.put("auditLogs", logs);
        return model;
    }

    @Override
    public Map<String, Object> getPaperAnswersData(int sessionId, String sbd, String contextPath) {
        return new HashMap<>();
    }

    @Override
    public int theoryPassThreshold() { return THEORY_PASS_CORRECT; }

    @Override
    public int theoryMaxQuestions() { return THEORY_MAX_QUESTIONS; }

    @Override
    public CandidateEnrollmentDTO findRegistration(int sessionId, String sbd) {
        List<CandidateEnrollmentDTO> registrations = enrollmentDAO.getCandidatesBySession(sessionId);
        for(CandidateEnrollmentDTO reg : registrations) {
            if (reg.getSbd().equals(sbd)) return reg;
        }
        return null;
    }

    @Override
    public Map<String, Object> getScoreEntryData(int sessionId, String sbdParam) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getResultDetailsEditData(int sessionId, String sbdParam) {
        return new HashMap<>();
    }

    @Override
    public boolean isScoreQueueEligible(int sessionId, CandidateEnrollmentDTO reg, SectionType sectionType, String sectionName) {
        return true;
    }

    @Override
    public Map<String, Object> getViolationData(int sessionId, String sbdParam) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getDevicesData(int sessionId, String searchQuery) {
        return new HashMap<>();
    }

    @Override
    public boolean isCallEligible(int sessionId, CandidateEnrollmentDTO reg, SectionType sectionType, String sectionName) {
        return true;
    }
}
