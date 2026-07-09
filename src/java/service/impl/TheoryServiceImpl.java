package service.impl;

import dao.ExamDAO;
import dao.ExamEnrollmentDAO;
import dao.QuestionDAO;
import dao.SessionDAO;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.QuestionDAOImpl;
import dao.impl.SessionDAOImpl;
import dto.EnrollmentDTO;
import dto.ServiceResult;
import dto.TheoryEntranceDTO;
import dto.TheorySubmitDTO;
import enums.CandidateStatus;
import enums.ErrorType;
import model.Exam;
import model.ExamEnrollment;
import model.Question;
import model.Session;
import service.CallService;
import service.RegistrationService;
import service.SessionService;
import service.TheoryService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TheoryServiceImpl implements TheoryService {

    private static final int THEORY_PASS = 32;
    private static final int THEORY_MAX = 35;
    private static final Map<Integer, Double> FACE_RATES = new HashMap<>();
    private static final Map<String, Map<Integer, String>> DRAFT_ANSWERS = new HashMap<>();
    private static final Map<String, Boolean> SECTION_PASS = new HashMap<>();

    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final SessionService sessionService = new SessionServiceImpl();
    private final CallService callService = new CallServiceImpl();

    @Override
    public Integer getActiveSessionId() {
        int active = sessionService.getActiveSessionId();
        return active > 0 ? active : null;
    }

    @Override
    public ServiceResult<TheoryEntranceDTO> validateEntrance(int sbd) {
        Integer sessionId = getActiveSessionId();
        if (sessionId == null) {
            return entranceFail(ErrorType.NOT_FOUND, "noSession", "Chưa mở ca thi.");
        }
        if (sbd <= 0) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "invalidSbd", "Số báo danh không hợp lệ.");
        }
        EnrollmentDTO reg = findBySbd(sessionId, sbd);
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
        if (!callService.isPresent(sessionId, sbd)) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "notPresent",
                    "Thí sinh chưa được giám khảo điểm danh. Vui lòng liên hệ phòng thi.");
        }
        if (callService.isInProcedureQueue(sessionId, sbd)) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "procedure",
                    "Thí sinh đang chờ xử lý tại phòng thủ tục.");
        }
        TheoryEntranceDTO data = new TheoryEntranceDTO();
        data.setSessionId(sessionId);
        data.setCandidateNumber(sbd);
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
    public double scanFace(int sessionId, int sbd) {
        Random random = new Random(sessionId * 1000L + sbd);
        double rate = 85.0 + random.nextInt(15);
        synchronized (FACE_RATES) {
            FACE_RATES.put(sessionId * 100000 + sbd, rate);
        }
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
    public void saveDraftAnswers(int sessionId, int sbd, Map<Integer, String> answers) {
        synchronized (DRAFT_ANSWERS) {
            DRAFT_ANSWERS.put(draftKey(sessionId, sbd), new LinkedHashMap<>(answers));
        }
    }

    @Override
    public ServiceResult<TheorySubmitDTO> submitExam(int sessionId, int sbd, Map<Integer, String> answers) {
        ServiceResult<TheoryEntranceDTO> entrance = validateEntrance(sbd);
        if (!entrance.isSuccess()) {
            String errorCode = entrance.getData() != null ? entrance.getData().getErrorCode() : "entranceFailed";
            TheorySubmitDTO data = new TheorySubmitDTO();
            data.setErrorCode(errorCode);
            return ServiceResult.fail(entrance.getErrorType(), entrance.getMessage(), data);
        }
        List<Question> questions = loadExamQuestions(sessionId, sbd);
        if (questions.isEmpty()) {
            TheorySubmitDTO data = new TheorySubmitDTO();
            data.setErrorCode("noQuestions");
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không có câu hỏi cho bài thi.", data);
        }
        Map<Integer, String> merged = new HashMap<>(getDraftAnswers(sessionId, sbd));
        if (answers != null) {
            merged.putAll(answers);
        }
        saveDraftAnswers(sessionId, sbd, merged);
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
        synchronized (SECTION_PASS) {
            SECTION_PASS.put(draftKey(sessionId, sbd), passed);
        }
        EnrollmentDTO reg = findBySbd(sessionId, sbd);
        if (reg != null) {
            ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
            if (enrollment != null) {
                enrollment.setSectionStatus(CandidateStatus.AWAITING_SIGNATURE.getValue());
                enrollmentDAO.update(enrollment);
            }
        }
        TheorySubmitDTO data = new TheorySubmitDTO();
        data.setCorrect(correct);
        data.setTotal(questions.size());
        data.setPassed(passed);
        return ServiceResult.ok(data);
    }

    private Map<Integer, String> getDraftAnswers(int sessionId, int sbd) {
        synchronized (DRAFT_ANSWERS) {
            Map<Integer, String> saved = DRAFT_ANSWERS.get(draftKey(sessionId, sbd));
            if (saved == null) {
                return new HashMap<>();
            }
            return new LinkedHashMap<>(saved);
        }
    }

    private static String draftKey(int sessionId, int sbd) {
        return sessionId + "-" + sbd;
    }

    private ServiceResult<TheoryEntranceDTO> entranceFail(ErrorType type, String errorCode, String message) {
        TheoryEntranceDTO data = new TheoryEntranceDTO();
        data.setErrorCode(errorCode);
        return ServiceResult.fail(type, message, data);
    }

    private EnrollmentDTO findBySbd(int sessionId, int sbd) {
        for (EnrollmentDTO row : registrationService.getCandidatesBySession(sessionId)) {
            if (row.getCandidateNumber() == sbd) {
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
