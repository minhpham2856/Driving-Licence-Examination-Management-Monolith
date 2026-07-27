package examiner.service.impl;

import examiner.dao.CandidateDAO;
import examiner.dao.ExamDeviceDAO;
import examiner.dao.ExamEnrollmentDAO;
import examiner.dao.ExamEnrollmentSectionDAO;
import examiner.dao.PaymentDAO;
import examiner.dao.impl.CandidateDAOImpl;
import examiner.dao.impl.ExamDeviceDAOImpl;
import examiner.dao.impl.ExamEnrollmentDAOImpl;
import examiner.dao.impl.ExamEnrollmentSectionDAOImpl;
import examiner.dao.impl.PaymentDAOImpl;
import examiner.dao.impl.ProfileDAOImpl;
import examiner.dao.impl.UserDAOImpl;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ServiceResult;
import shared.enums.CandidateStatus;
import shared.util.SectionStatusUtil;
import shared.enums.ErrorType;
import shared.enums.SectionType;
import shared.enums.PaymentStatus;
import shared.model.Candidate;
import shared.model.ExamDevice;
import shared.model.ExamEnrollment;
import shared.model.Payment;
import shared.model.Profile;
import shared.model.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import examiner.service.ScoreService;
import examiner.service.EnrollmentService;

// Examiner registration service: enrollment lookups, candidate updates, and profile/user helpers.
public class EnrollmentServiceImpl implements EnrollmentService {

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final ExamEnrollmentSectionDAO enrollmentSectionDAO = new ExamEnrollmentSectionDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final ScoreService examScoreService = new ScoreServiceImpl();

    // Loads enrollment by exam and candidate number (SBD) with default section context.
    @Override
    public EnrollmentDTO getByExamAndSbd(int examId, int sbd) {
        return getByExamAndSbd(examId, sbd, null);
    }

    // Loads enrollment by exam, SBD, and section type for section-specific status.
    @Override
    public EnrollmentDTO getByExamAndSbd(int examId, int sbd, SectionType sectionType) {
        if (examId <= 0 || sbd <= 0) {
            return null;
        }
        for (EnrollmentDTO row : getAllByExam(examId, sectionType)) {
            if (row.getCandidateNumber() == sbd) {
                return row;
            }
        }
        return null;
    }

    // Lists all enrollments for an exam with default section context.
    @Override
    public List<EnrollmentDTO> getAllByExam(int examId) {
        return getAllByExam(examId, null);
    }

    // Lists all enrollments for an exam with section-specific status and flags.
    @Override
    public List<EnrollmentDTO> getAllByExam(int examId, SectionType sectionType) {
        return toEnrollmentDtoList(enrollmentDAO.getByExamId(examId), sectionType);
    }

    // Searches enrollments by keyword for the exam session.
    @Override
    public List<EnrollmentDTO> getFilteredByExam(int examId, String keyword) {
        return getFilteredByExam(examId, keyword, null);
    }

    // Searches enrollments by keyword for the exam session.
    @Override
    public List<EnrollmentDTO> getFilteredByExam(int examId, String keyword, SectionType sectionType) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }
        return toEnrollmentDtoList(enrollmentDAO.getFilteredByExam(examId, keyword), sectionType);
    }

    // Builds EnrollmentDTOs from enrollments by joining Candidate + section status.
    private List<EnrollmentDTO> toEnrollmentDtoList(List<ExamEnrollment> enrollments, SectionType sectionType) {
        if (enrollments == null || enrollments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> candidateIds = new ArrayList<>();
        List<Integer> enrollmentIds = new ArrayList<>();
        for (ExamEnrollment enrollment : enrollments) {
            candidateIds.add(enrollment.getCandidateId());
            enrollmentIds.add(enrollment.getExamEnrollmentId());
        }

        Map<Integer, Candidate> candidates = new HashMap<>();
        for (Candidate candidate : candidateDAO.getAllByIds(candidateIds)) {
            candidates.put(candidate.getCandidateId(), candidate);
        }
        String sectionTypeValue = sectionType != null ? sectionType.getValue() : null;
        Map<Integer, String> sectionStatuses = sectionTypeValue != null
                ? enrollmentSectionDAO.getStatusByEnrollmentIds(enrollmentIds, sectionTypeValue)
                : new HashMap<>();
        Map<Integer, Boolean> resultPrinted = sectionTypeValue != null
                ? enrollmentSectionDAO.getResultPrintedByEnrollmentIds(enrollmentIds, sectionTypeValue)
                : new HashMap<>();
        Map<Integer, Boolean> checkedIn = sectionTypeValue != null
                ? enrollmentSectionDAO.getCheckedInByEnrollmentIds(enrollmentIds, sectionTypeValue)
                : new HashMap<>();

        List<EnrollmentDTO> list = new ArrayList<>();
        for (ExamEnrollment enrollment : enrollments) {
            Candidate candidate = candidates.get(enrollment.getCandidateId());
            if (candidate != null) {
                list.add(toEnrollmentDto(candidate, enrollment, sectionStatuses, resultPrinted,
                        checkedIn, sectionTypeValue));
            }
        }
        list.sort(Comparator.comparingInt(EnrollmentDTO::getCandidateNumber));
        return list;
    }

    // Private helper: build flat enrollment dto from candidate and enrollment rows.
    private EnrollmentDTO toEnrollmentDto(Candidate candidate, ExamEnrollment enrollment,
            Map<Integer, String> sectionStatuses, Map<Integer, Boolean> resultPrinted,
            Map<Integer, Boolean> checkedIn, String sectionTypeValue) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setCandidateId(candidate.getCandidateId());
        dto.setCandidateNumber(parseCandidateNumber(candidate.getCandidateNumber()));
        dto.setFullName(candidate.getFullName() != null ? candidate.getFullName() : "");
        dto.setDateOfBirth(candidate.getDateOfBirth());
        dto.setGovernmentIdNumber(candidate.getGovernmentIdNumber() != null ? candidate.getGovernmentIdNumber() : "");
        dto.setPhoneNumber(candidate.getPhoneNumber() != null ? candidate.getPhoneNumber() : "");
        dto.setAddress(candidate.getAddress() != null ? candidate.getAddress() : "");
        dto.setEmail(candidate.getEmail() != null ? candidate.getEmail() : "");
        dto.setReasonForTaking(candidate.getReasonForTaking() != null ? candidate.getReasonForTaking() : "");
        dto.setSex(candidate.isSex());
        dto.setPhotoImageUrl(candidate.getPhotoImageUrl());
        dto.setTakeTheory(candidate.getTakeTheory());
        dto.setTakeLayout(candidate.getTakeLayout());
        dto.setAbsent(candidate.isAbsent());
        dto.setSuspended(candidate.isSuspended());
        dto.setPresent(false);
        String photoUrl = candidate.getPhotoImageUrl();
        dto.setValidCapturedPhoto(photoUrl != null && !photoUrl.isBlank());
        if (enrollment != null) {
            dto.setExamEnrollmentId(enrollment.getExamEnrollmentId());
            dto.setExamId(enrollment.getExamId());
            dto.setExamDeviceId(enrollment.getExamDeviceId());
            dto.setPaymentCompleted(paymentDAO.hasCompletedPayment(enrollment.getExamEnrollmentId()));
            String status = sectionStatuses != null
                    ? sectionStatuses.get(enrollment.getExamEnrollmentId()) : null;
            CandidateStatus parsed = CandidateStatus.fromValue(SectionStatusUtil.normalize(status));
            dto.setSectionStatus(parsed != null ? parsed : CandidateStatus.NOT_STARTED);
            Boolean printed = resultPrinted != null
                    ? resultPrinted.get(enrollment.getExamEnrollmentId()) : null;
            dto.setResultPrinted(Boolean.TRUE.equals(printed));
            Boolean present = checkedIn != null
                    ? checkedIn.get(enrollment.getExamEnrollmentId()) : null;
            dto.setPresent(Boolean.TRUE.equals(present));
        } else {
            dto.setSectionStatus(CandidateStatus.NOT_STARTED);
            dto.setResultPrinted(false);
            dto.setPresent(false);
            dto.setPaymentCompleted(false);
        }
        return dto;
    }

    // Private helper: parse candidate number string to int.
    private static int parseCandidateNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // Updates photo in the database.
    @Override
    public ServiceResult<Void> updatePhoto(int candidateId, String photoUrl) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        Candidate candidate = candidateDAO.get(candidateId);
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        candidate.setPhotoImageUrl(photoUrl);
        if (!candidateDAO.update(candidate)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật ảnh chân dung.");
        }
        return ServiceResult.ok(null);
    }

    // Marks candidate as absent with audit.
    @Override
    public ServiceResult<Void> markAbsent(int candidateId) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        if (!enrollmentDAO.markAbsent(candidateId)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đánh dấu vắng mặt.");
        }
        return ServiceResult.ok(null);
    }

    // Clears absent marking state.
    @Override
    public ServiceResult<Void> clearAbsentMarking(int candidateId) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        if (!enrollmentDAO.clearAbsentMarking(candidateId)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể xóa đánh dấu vắng mặt.");
        }
        return ServiceResult.ok(null);
    }

    // Marks candidate as suspended with audit.
    @Override
    public ServiceResult<Void> markSuspended(int candidateId) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        Candidate candidate = candidateDAO.get(candidateId);
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        if (!candidateDAO.updateSuspended(candidateId, true)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đình chỉ thí sinh.");
        }
        return ServiceResult.ok(null);
    }

    // Reverses prior suspension for the candidate.
    @Override
    public ServiceResult<Void> undoSuspension(int candidateId) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        Candidate candidate = candidateDAO.get(candidateId);
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        if (!candidateDAO.updateSuspended(candidateId, false)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể gỡ đình chỉ thí sinh.");
        }
        return ServiceResult.ok(null);
    }

    // Finds applied score deductions for examiner workflow.
    @Override
    public List<Map<String, Object>> getAllAppliedDeductionsByCandidate(int candidateId, int examId) {
        return new ArrayList<>();
    }

    // Updates scores in the database.
    @Override
    public ServiceResult<Void> updateScores(int candidateId, Integer theoryScore, String theoryResult,
            Integer practicalScore, String practicalResult) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        if (theoryScore != null) {
            boolean passed = "passed".equalsIgnoreCase(theoryResult);
            if (!examScoreService.update(candidateId, SectionType.THEORY,
                    theoryScore, passed)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật điểm lý thuyết.");
            }
        }
        if (practicalScore != null) {
            boolean passed = "passed".equalsIgnoreCase(practicalResult);
            if (!examScoreService.update(candidateId, SectionType.LAYOUT,
                    practicalScore, passed)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật điểm thực hành.");
            }
        }
        return ServiceResult.ok(null);
    }

    // Updates present in the database.
    @Override
    public ServiceResult<Void> updatePresent(int candidateId, boolean isPresent) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        if (!candidateDAO.updateAbsent(candidateId, !isPresent)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật trạng thái có mặt.");
        }
        return ServiceResult.ok(null);
    }

    // Updates allocated room in the database.
    @Override
    public ServiceResult<Void> updateAllocatedRoom(int candidateId, int areaId, String areaName) {
        if (candidateId <= 0 || areaId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin phân phòng không hợp lệ.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getLatestByCandidateId(candidateId);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy ghi danh của thí sinh.");
        }
        List<ExamDevice> devices = deviceDAO.getDevicesByAreaId(areaId);
        Integer deviceId = null;
        for (ExamDevice device : devices) {
            if (device.isActive()) {
                deviceId = device.getExamDeviceId();
                break;
            }
        }
        if (deviceId == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thiết bị khả dụng trong khu vực thi.");
        }
        if (!enrollmentDAO.assignExamDevice(candidateId, enrollment.getExamId(), deviceId)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể phân phòng cho thí sinh.");
        }
        return ServiceResult.ok(null);
    }

    // Updates payment in the database.
    @Override
    public ServiceResult<Void> updatePayment(int candidateId, boolean isPaid) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        if (!isPaid) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Chỉ hỗ trợ ghi nhận đã thanh toán.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getLatestByCandidateId(candidateId);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy ghi danh của thí sinh.");
        }
        if (paymentDAO.hasCompletedPayment(enrollment.getExamEnrollmentId())) {
            return ServiceResult.ok(null);
        }
        Payment payment = new Payment();
        payment.setExamEnrollmentId(enrollment.getExamEnrollmentId());
        payment.setPaymentStatus(PaymentStatus.COMPLETED.getValue());
        payment.setPaymentMethod("Cash");
        payment.setTotalAmount(0);
        payment.setPaidAt(new Timestamp(System.currentTimeMillis()));
        if (!paymentDAO.add(payment)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận thanh toán.");
        }
        return ServiceResult.ok(null);
    }

    // Inserts payment into the database.
    @Override
    public boolean add(Payment payment) {
        return paymentDAO.add(payment);
    }

    // Loads enrollment DTO by candidate id with theory section status defaults.
    @Override
    public EnrollmentDTO get(int candidateId) {
        if (candidateId <= 0) {
            return null;
        }
        Candidate candidate = candidateDAO.get(candidateId);
        if (candidate == null) {
            return null;
        }
        ExamEnrollment enrollment = enrollmentDAO.getLatestByCandidateId(candidateId);
        if (enrollment == null) {
            return toEnrollmentDto(candidate, null, null, null, null, null);
        }
        // get has no section context — default to theory status for profile views.
        String sectionType = SectionType.THEORY.getValue();
        Map<Integer, String> statuses = enrollmentSectionDAO.getStatusByEnrollmentIds(
                List.of(enrollment.getExamEnrollmentId()), sectionType);
        Map<Integer, Boolean> resultPrinted = enrollmentSectionDAO.getResultPrintedByEnrollmentIds(
                List.of(enrollment.getExamEnrollmentId()), sectionType);
        Map<Integer, Boolean> checkedIn = enrollmentSectionDAO.getCheckedInByEnrollmentIds(
                List.of(enrollment.getExamEnrollmentId()), sectionType);
        return toEnrollmentDto(candidate, enrollment, statuses, resultPrinted, checkedIn, sectionType);
    }

    // Finds candidate id by gov id and exam for examiner workflow.
    @Override
    public Integer getIfByGovIdAndExam(String governmentIdNumber, int examId) {
        ExamEnrollment enrollment = enrollmentDAO.getIfByGovIdAndExam(governmentIdNumber, examId);
        return enrollment != null ? enrollment.getCandidateId() : null;
    }

    // Inserts profile into the database.
    @Override
    public boolean add(Profile profile) {
        return new ProfileDAOImpl().add(profile);
    }

    // Updates profile in the database.
    @Override
    public boolean updateProfile(Profile profile) {
        return new ProfileDAOImpl().update(profile);
    }

    // Loads a profile by government identification number.
    @Override
    public Profile getByGovId(String govId) {
        return new ProfileDAOImpl().getByGovIdNo(govId);
    }

    // Inserts user into the database.
    @Override
    public boolean add(User user) {
        return new UserDAOImpl().add(user);
    }

    // Loads a user account by username.
    @Override
    public User getByUsername(String username) {
        return new UserDAOImpl().getByUsername(username);
    }
}
