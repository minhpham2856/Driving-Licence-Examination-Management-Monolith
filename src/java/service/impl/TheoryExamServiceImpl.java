package service.impl;

import dao.ExamDAO;
import dao.ExamEnrollmentDAO;
import dao.QuestionDAO;
import dao.SessionDAO;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.QuestionDAOImpl;
import dao.impl.SessionDAOImpl;
import dto.CandidateEnrollmentDTO;
import dto.ServiceResult;
import dto.payload.TheoryEntranceData;
import dto.payload.TheorySubmitData;
import enums.CandidateStatus;
import enums.ErrorType;
import jakarta.servlet.ServletContext;
import model.Exam;
import model.ExamEnrollment;
import model.Question;
import model.Session;
import service.ExamRegistrationService;
import service.TheoryExamService;
import util.ExamSessionState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TheoryExamServiceImpl implements TheoryExamService {

    private static final int THEORY_PASS = 32;
    private static final int THEORY_MAX = 35;
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();

    @Override
    public Integer getActiveSessionId(ServletContext ctx) {
        Object active = ctx.getAttribute("examActiveSessionId");
        if (active instanceof Integer) {
            int sessionId = (Integer) active;
            return sessionId > 0 ? sessionId : null;
        }
        return null;
    }

    @Override
    public ServiceResult<TheoryEntranceData> validateEntrance(ServletContext ctx, int sbd) {
        Integer sessionId = getActiveSessionId(ctx);
        if (sessionId == null) {
            return entranceFail(ErrorType.NOT_FOUND, "noSession", "Chưa mở ca thi.");
        }
        if (sbd <= 0) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "invalidSbd", "Số báo danh không hợp lệ.");
        }
        CandidateEnrollmentDTO reg = findBySbd(sessionId, sbd);
        if (reg == null) {
            return entranceFail(ErrorType.NOT_FOUND, "notFound", "SBD không tồn tại trong ca thi.");
        }
        if (reg.isAbsent()) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "absent", "Thí sinh đã bị đánh dấu vắng.");
        }
        if (reg.isSuspended()) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "suspended", "Thí sinh đã bị đình chỉ.");
        }
        if (CandidateStatus.COMPLETED.getValue().equals(reg.getSectionStatus())) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "alreadyDone", "Thí sinh đã hoàn thành phần thi này.");
        }
        if (CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "awaiting", "Thí sinh đã nộp bài, chờ ký biên bản.");
        }
        if (!ExamSessionState.isPresent(ctx, sessionId, sbd)) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "notPresent",
                    "Thí sinh chưa được giám khảo điểm danh. Vui lòng liên hệ phòng thi.");
        }
        if (ExamSessionState.isInProcedureQueue(ctx, sessionId, sbd)) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "procedure",
                    "Thí sinh đang chờ xử lý tại phòng thủ tục.");
        }
        TheoryEntranceData data = new TheoryEntranceData();
        data.setSessionId(sessionId);
        data.setSbd(sbd);
        data.setFullName(reg.getFullName());
        SimpleDateFormat fmt = new SimpleDateFormat("dd / MM / yyyy");
        if (reg.getDateOfBirth() != null) {
            data.setDob(fmt.format(reg.getDateOfBirth()));
        } else if (reg.getDob() != null) {
            data.setDob(fmt.format(reg.getDob()));
        }
        data.setGovIdNo(reg.getGovIdNo());
        data.setLicenceClass(loadLicenceClass(sessionId));
        return ServiceResult.ok(data);
    }

    @Override
    public double scanFace(ServletContext ctx, int sessionId, int sbd) {
        Random random = new Random(sessionId * 1000L + sbd);
        double rate = 85.0 + random.nextInt(15);
        ExamSessionState.setFaceMatchRate(ctx, sessionId, sbd, rate);
        return rate;
    }

    @Override
    public List<Question> loadExamQuestions(int sessionId, int sbd) {
        List<Question> all = questionDAO.findAll();
        if (all == null || all.isEmpty()) {
            return new ArrayList<>();
        }
        List<Question> paper = new ArrayList<>();
        int limit = Math.min(THEORY_MAX, all.size());
        for (int i = 0; i < limit; i++) {
            paper.add(all.get(i));
        }
        return paper;
    }

    @Override
    public void saveDraftAnswers(ServletContext ctx, int sessionId, int sbd, Map<Integer, String> answers) {
        ExamSessionState.saveDraftAnswers(ctx, sessionId, sbd, answers);
    }

    @Override
    public ServiceResult<TheorySubmitData> submitExam(ServletContext ctx, int sessionId, int sbd,
            Map<Integer, String> answers) {
        ServiceResult<TheoryEntranceData> entrance = validateEntrance(ctx, sbd);
        if (!entrance.isSuccess()) {
            String errorCode = entrance.getData() != null ? entrance.getData().getErrorCode() : "entranceFailed";
            TheorySubmitData data = new TheorySubmitData();
            data.setErrorCode(errorCode);
            return ServiceResult.fail(entrance.getErrorType(), entrance.getMessage(), data);
        }
        List<Question> questions = loadExamQuestions(sessionId, sbd);
        if (questions.isEmpty()) {
            TheorySubmitData data = new TheorySubmitData();
            data.setErrorCode("noQuestions");
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không có câu hỏi cho bài thi.", data);
        }
        Map<Integer, String> merged = new HashMap<>(ExamSessionState.getDraftAnswers(ctx, sessionId, sbd));
        if (answers != null) {
            merged.putAll(answers);
        }
        ExamSessionState.saveDraftAnswers(ctx, sessionId, sbd, merged);
        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            int questionNo = i + 1;
            String given = merged.get(questionNo);
            if (given != null && given.equals(q.getCorrectAnswer())) {
                correct++;
            }
        }
        boolean passed = correct >= THEORY_PASS;
        ExamSessionState.setSectionPassed(ctx, sessionId, sbd, passed);
        CandidateEnrollmentDTO reg = findBySbd(sessionId, sbd);
        if (reg != null) {
            ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
            if (enrollment != null) {
                enrollment.setSectionStatus(CandidateStatus.AWAITING_SIGNATURE.getValue());
                enrollmentDAO.update(enrollment);
            }
        }
        TheorySubmitData data = new TheorySubmitData();
        data.setCorrect(correct);
        data.setTotal(questions.size());
        data.setPassed(passed);
        return ServiceResult.ok(data);
    }

    private ServiceResult<TheoryEntranceData> entranceFail(ErrorType type, String errorCode, String message) {
        TheoryEntranceData data = new TheoryEntranceData();
        data.setErrorCode(errorCode);
        return ServiceResult.fail(type, message, data);
    }

    private CandidateEnrollmentDTO findBySbd(int sessionId, int sbd) {
        for (CandidateEnrollmentDTO row : registrationService.getCandidatesBySession(sessionId)) {
            if (row.getSbd() == sbd) {
                return row;
            }
        }
        return null;
    }

    private String loadLicenceClass(int sessionId) {
        Session session = sessionDAO.getById(sessionId);
        if (session == null) {
            return "-";
        }
        Exam exam = examDAO.getById(session.getExamId());
        if (exam == null) {
            return "-";
        }
        return String.valueOf(exam.getLicenceId());
    }
}
