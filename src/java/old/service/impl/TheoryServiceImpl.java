package service.impl;

import dao.ExamDAO;
import dao.ExamEnrollmentDAO;
import dao.QuestionDAO;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.QuestionDAOImpl;
import dto.EnrollmentDTO;
import dto.ServiceResult;
import dto.TheoryEntranceDTO;
import dto.TheorySubmitDTO;
import enums.CandidateStatus;
import enums.ErrorType;
import model.Exam;
import model.ExamEnrollment;
import model.Question;
import service.CallService;
import service.RegistrationService;
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
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final CallService callService = new CallServiceImpl();

    @Override
    public Integer getActiveExamId() {
        List<Exam> active = examDAO.getByStatus(enums.ExamStatus.IN_PROGRESS);
        if (active != null && !active.isEmpty()) {
            return active.get(0).getExamId();
        }
        return null;
    }

    @Override
    public ServiceResult<TheoryEntranceDTO> validateEntrance(int sbd) {
        Integer examId = getActiveExamId();
        if (examId == null) {
            return entranceFail(ErrorType.NOT_FOUND, "noExam", "Chưa mở kỳ thi.");
        }
        if (sbd <= 0) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "invalidSbd", "Số báo danh không hợp lệ.");
        }
        EnrollmentDTO reg = findBySbd(examId, sbd);
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
        if (!callService.isPresent(examId, sbd)) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "notPresent",
                    "Thí sinh chưa được giám khảo điểm danh. Vui lòng liên hệ phòng thi.");
        }
        if (callService.isInProcedureQueue(examId, sbd)) {
            return entranceFail(ErrorType.VALIDATION_FAILED, "procedure",
                    "Thí sinh đang chờ xử lý tại phòng thủ tục.");
        }
        TheoryEntranceDTO data = new TheoryEntranceDTO();
        data.setExamId(examId);
        data.setCandidateNumber(sbd);
        data.setFullName(reg.getFullName());
        SimpleDateFormat fmt = new SimpleDateFormat("dd / MM / yyyy");
        if (reg.getDateOfBirth() != null) {
            data.setDob(fmt.format(reg.getDateOfBirth()));
        } else if (reg.getDob() != null) {
            data.setDob(fmt.format(reg.getDob()));
        }
        data.setGovIdNo(reg.getGovIdNo());
        data.setLicenceClass(loadLicenceClass(examId));
        return ServiceResult.ok(data);
    }

    @Override
    public double scanFace(int examId, int sbd) {
        Random random = new Random(examId * 1000L + sbd);
        double rate = 85.0 + random.nextInt(15);
        synchronized (FACE_RATES) {
            FACE_RATES.put(examId * 100000 + sbd, rate);
        }
        return rate;
    }

    @Override
    public List<Question> loadExamQuestions(int examId, int sbd) {
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
    public void saveDraftAnswers(int examId, int sbd, Map<Integer, String> answers) {
        synchronized (DRAFT_ANSWERS) {
            DRAFT_ANSWERS.put(draftKey(examId, sbd), new LinkedHashMap<>(answers));
        }
    }

    @Override
    public ServiceResult<TheorySubmitDTO> submitExam(int examId, int sbd, Map<Integer, String> answers) {
        ServiceResult<TheoryEntranceDTO> entrance = validateEntrance(sbd);
        if (!entrance.isSuccess()) {
            String errorCode = entrance.getData() != null ? entrance.getData().getErrorCode() : "entranceFailed";
            TheorySubmitDTO data = new TheorySubmitDTO();
            data.setErrorCode(errorCode);
            return ServiceResult.fail(entrance.getErrorType(), entrance.getMessage(), data);
        }
        List<Question> questions = loadExamQuestions(examId, sbd);
        if (questions.isEmpty()) {
            TheorySubmitDTO data = new TheorySubmitDTO();
            data.setErrorCode("noQuestions");
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không có câu hỏi cho bài thi.", data);
        }
        Map<Integer, String> merged = new HashMap<>(getDraftAnswers(examId, sbd));
        if (answers != null) {
            merged.putAll(answers);
        }
        saveDraftAnswers(examId, sbd, merged);
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
            SECTION_PASS.put(draftKey(examId, sbd), passed);
        }
        EnrollmentDTO reg = findBySbd(examId, sbd);
        if (reg != null) {
            ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, reg.getId());
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

    private Map<Integer, String> getDraftAnswers(int examId, int sbd) {
        synchronized (DRAFT_ANSWERS) {
            Map<Integer, String> saved = DRAFT_ANSWERS.get(draftKey(examId, sbd));
            if (saved == null) {
                return new HashMap<>();
            }
            return new LinkedHashMap<>(saved);
        }
    }

    private static String draftKey(int examId, int sbd) {
        return examId + "-" + sbd;
    }

    private ServiceResult<TheoryEntranceDTO> entranceFail(ErrorType type, String errorCode, String message) {
        TheoryEntranceDTO data = new TheoryEntranceDTO();
        data.setErrorCode(errorCode);
        return ServiceResult.fail(type, message, data);
    }

    private EnrollmentDTO findBySbd(int examId, int sbd) {
        for (EnrollmentDTO row : registrationService.getCandidatesByExam(examId)) {
            if (row.getCandidateNumber() == sbd) {
                return row;
            }
        }
        return null;
    }

    private String loadLicenceClass(int examId) {
        Exam exam = examDAO.getById(examId);
        if (exam == null) {
            return "-";
        }
        return String.valueOf(exam.getLicenceId());
    }
}
