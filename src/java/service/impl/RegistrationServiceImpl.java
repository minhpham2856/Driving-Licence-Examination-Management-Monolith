package service.impl;

import dao.CandidateDAO;
import dao.ExamDeviceDAO;
import dao.ExamEnrollmentDAO;
import dao.PaymentDAO;
import dao.impl.CandidateDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.PaymentDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.UserDAOImpl;
import dto.EnrollmentDTO;
import dto.CandidateProfileDTO;
import dto.ServiceResult;
import dto.UploadRowDTO;
import java.sql.Date;
import enums.CandidateStatus;
import enums.ErrorType;
import enums.SectionType;
import enums.PaymentStatus;
import model.Candidate;
import model.ExamDevice;
import model.ExamEnrollment;
import model.Payment;
import model.Profile;
import model.User;
import service.RegistrationService;
import service.ExamScoreService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationServiceImpl implements RegistrationService {

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final ExamScoreService examScoreService = new ExamScoreServiceImpl();

    @Override
    public EnrollmentDTO getBySessionAndSbd(int sessionId, int sbd) {
        if (sessionId <= 0 || sbd <= 0) {
            return null;
        }
        for (EnrollmentDTO row : getCandidatesBySession(sessionId)) {
            if (row.getCandidateNumber() == sbd) {
                return row;
            }
        }
        return null;
    }

    @Override
    public List<EnrollmentDTO> getCandidatesBySession(int sessionId) {
        return toEnrollmentDtoList(enrollmentDAO.getBySessionId(sessionId));
    }

    @Override
    public List<EnrollmentDTO> searchCandidatesBySession(int sessionId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }
        return toEnrollmentDtoList(enrollmentDAO.searchBySession(sessionId, keyword));
    }

    // Builds EnrollmentDTOs from enrollments by joining their Candidate details.
    private List<EnrollmentDTO> toEnrollmentDtoList(List<ExamEnrollment> enrollments) {
        if (enrollments == null || enrollments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> candidateIds = new ArrayList<>();
        for (ExamEnrollment enrollment : enrollments) {
            candidateIds.add(enrollment.getCandidateId());
        }

        Map<Integer, Candidate> candidates = new HashMap<>();
        for (Candidate candidate : candidateDAO.getAllByIds(candidateIds)) {
            candidates.put(candidate.getCandidateId(), candidate);
        }

        List<EnrollmentDTO> list = new ArrayList<>();
        for (ExamEnrollment enrollment : enrollments) {
            Candidate candidate = candidates.get(enrollment.getCandidateId());
            if (candidate != null) {
                list.add(toEnrollmentDto(candidate, enrollment));
            }
        }
        list.sort(Comparator.comparingInt(EnrollmentDTO::getCandidateNumber));
        return list;
    }

    private EnrollmentDTO toEnrollmentDto(Candidate candidate, ExamEnrollment enrollment) {
        CandidateProfileDTO profile = new CandidateProfileDTO();
        profile.setCandidateId(candidate.getCandidateId());
        profile.setCandidateNumber(parseCandidateNumber(candidate.getCandidateNumber()));
        profile.setFullName(candidate.getFullName());
        profile.setGovernmentIdNumber(candidate.getGovernmentIdNumber());
        profile.setAbsent(candidate.isAbsent());
        profile.setSuspended(candidate.isSuspended());
        profile.setPhotoImageUrl(candidate.getPhotoImageUrl());
        EnrollmentDTO dto = new EnrollmentDTO(profile, enrollment);
        dto.setDateOfBirth(candidate.getDateOfBirth());
        dto.setPhoneNo(candidate.getPhoneNumber());
        dto.setAddress(candidate.getAddress());
        dto.setReasonForTaking(candidate.getReasonForTaking());
        dto.setSex(candidate.isSex());
        return dto;
    }

    private int parseCandidateNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String formatCandidateNumber(int candidateNo) {
        if (candidateNo <= 0) {
            return "0";
        }
        return String.format("%03d", candidateNo);
    }

    @Override
    public ServiceResult<Void> updateProfile(int candidateId, String fullName, Date dateOfBirth,
            String governmentIdNumber, String phoneNumber) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        boolean updated = candidateDAO.updateExaminerProfile(
                candidateId,
                fullName,
                dateOfBirth,
                governmentIdNumber,
                phoneNumber,
                candidate.getAddress(),
                candidate.isSex(),
                candidate.getReasonForTaking());
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật hồ sơ thí sinh.");
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> updatePhoto(int candidateId, String photoUrl) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        candidate.setPhotoImageUrl(photoUrl);
        if (!candidateDAO.update(candidate)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật ảnh chân dung.");
        }
        return ServiceResult.ok(null);
    }

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

    @Override
    public ServiceResult<Void> markSuspended(int candidateId) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        candidate.setSuspended(true);
        if (!candidateDAO.update(candidate)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đình chỉ thí sinh.");
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> undoSuspension(int candidateId) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        candidate.setSuspended(false);
        if (!candidateDAO.update(candidate)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể gỡ đình chỉ thí sinh.");
        }
        return ServiceResult.ok(null);
    }

    @Override
    public List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId) {
        return new ArrayList<>();
    }

    @Override
    public ServiceResult<Void> updateScores(int candidateId, Integer theoryScore, String theoryResult,
            Integer practicalScore, String practicalResult) {
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không hợp lệ.");
        }
        if (theoryScore != null) {
            boolean passed = "passed".equalsIgnoreCase(theoryResult);
            if (!examScoreService.upsertSectionScore(candidateId, SectionType.THEORY,
                    theoryScore, passed)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật điểm lý thuyết.");
            }
        }
        if (practicalScore != null) {
            boolean passed = "passed".equalsIgnoreCase(practicalResult);
            if (!examScoreService.upsertSectionScore(candidateId, SectionType.LAYOUT,
                    practicalScore, passed)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật điểm thực hành.");
            }
        }
        return ServiceResult.ok(null);
    }

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
        if (!enrollmentDAO.assignExamDevice(candidateId, enrollment.getSessionId(), deviceId)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể phân phòng cho thí sinh.");
        }
        return ServiceResult.ok(null);
    }

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
        if (!paymentDAO.insert(payment)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận thanh toán.");
        }
        return ServiceResult.ok(null);
    }

    @Override
    public boolean insertPayment(Payment payment) {
        return paymentDAO.insert(payment);
    }

    @Override
    public EnrollmentDTO getById(int candidateId) {
        if (candidateId <= 0) {
            return null;
        }
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            return null;
        }
        ExamEnrollment enrollment = enrollmentDAO.getLatestByCandidateId(candidateId);
        return toEnrollmentDto(candidate, enrollment);
    }

    @Override
    public Integer findCandidateIdByGovIdAndSession(String governmentIdNumber, int sessionId) {
        return enrollmentDAO.findCandidateIdByGovIdAndSession(governmentIdNumber, sessionId);
    }

    @Override
    public ServiceResult<Void> insert(UploadRowDTO dto) {
        if (dto == null || dto.getExamSessionId() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Dữ liệu import không hợp lệ.");
        }
        if (dto.getGovIdNo() == null || dto.getGovIdNo().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiếu số CCCD.");
        }
        Integer existingId = findCandidateIdByGovIdAndSession(dto.getGovIdNo(), dto.getExamSessionId());
        if (existingId != null) {
            dto.setId(existingId);
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh đã tồn tại trong ca thi.");
        }
        Candidate candidate = new Candidate();
        candidate.setCandidateNumber(formatCandidateNumber(dto.getCandidateNo()));
        candidate.setFullName(dto.getFullName());
        if (dto.getDateOfBirth() != null) {
            candidate.setDateOfBirth(new Timestamp(dto.getDateOfBirth().getTime()));
        }
        candidate.setPhoneNumber(dto.getPhoneNo());
        candidate.setSex(false);
        candidate.setGovernmentIdNumber(dto.getGovIdNo());
        candidate.setAddress("");
        candidate.setTakeTheory(true);
        candidate.setTakeLayout(true);
        candidate.setTakeNo(1);
        candidate.setReasonForTaking("Import CSV");
        candidate.setAbsent(!dto.isPresent());
        candidate.setSuspended(false);
        int candidateId = candidateDAO.insert(candidate);
        if (candidateId <= 0) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể tạo thí sinh.");
        }
        ExamEnrollment enrollment = new ExamEnrollment();
        enrollment.setCandidateId(candidateId);
        enrollment.setSessionId(dto.getExamSessionId());
        enrollment.setSectionStatus(CandidateStatus.NOT_STARTED.getValue());
        enrollment.setSignaturePrinted(false);
        int enrollmentId = enrollmentDAO.insert(enrollment);
        if (enrollmentId <= 0) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi danh thí sinh vào ca thi.");
        }
        dto.setId(candidateId);
        return ServiceResult.ok(null);
    }

    @Override
    public boolean insertProfile(Profile profile) {
        return new ProfileDAOImpl().insert(profile);
    }

    @Override
    public boolean updateProfile(Profile profile) {
        return new ProfileDAOImpl().update(profile);
    }

    @Override
    public Profile getProfileByGovId(String govId) {
        return new ProfileDAOImpl().getByGovIdNo(govId);
    }

    @Override
    public boolean insertUser(User user) {
        return new UserDAOImpl().insert(user);
    }

    @Override
    public User getUserByUsername(String username) {
        return new UserDAOImpl().getByUsername(username);
    }
}
