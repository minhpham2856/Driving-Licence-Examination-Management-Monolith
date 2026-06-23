package service.impl;

import dao.AuditDAO;
import dao.CandidateDAO;
import dao.DeductionRecordDAO;
import dao.ExamDeviceDAO;
import dao.ExamEnrollmentDAO;
import dao.ExamResultDAO;
import dao.ExamScoreDAO;
import dao.ExamSectionDAO;
import dao.ExaminerViewDAO;
import dao.ScoreDeductionDAO;
import dao.SessionDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.CandidateDAOImpl;
import dao.impl.DeductionRecordDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.ExamResultDAOImpl;
import dao.impl.ExamScoreDAOImpl;
import dao.impl.ExamSectionDAOImpl;
import dao.impl.ExaminerViewDAOImpl;
import dao.impl.ScoreDeductionDAOImpl;
import dao.impl.SessionDAOImpl;
import dto.EnrollmentDTO;
import dto.ServiceResult;
import dto.SessionViewDTO;
import enums.AuditAction;
import enums.SectionType;
import enums.AuditEntity;
import enums.CandidateStatus;
import enums.ErrorType;
import enums.Sex;
import enums.ViolationReason;
import model.Audit;
import model.Candidate;
import model.DeductionRecord;
import model.ExamDevice;
import model.ExamEnrollment;
import model.ExamResult;
import model.ExamScore;
import model.ScoreDeduction;
import model.User;
import service.AuditService;
import service.RegistrationService;
import service.ExamScoreService;
import service.SessionService;
import service.CallService;
import service.ExamViewService;
import util.ExamQueue;
import util.ExamQueue.Lane;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallServiceImpl implements CallService {

    private static final Map<Integer, Set<Integer>> PRESENT = new HashMap<>();
    private static final Map<Integer, Set<Integer>> PROCEDURE = new HashMap<>();

    private final AuditService auditService = new AuditServiceImpl();
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamResultDAO examResultDAO = new ExamResultDAOImpl();
    private final ExamScoreDAO examScoreDAO = new ExamScoreDAOImpl();
    private final ExamScoreService examScoreService = new ExamScoreServiceImpl();
    private final DeductionRecordDAO deductionRecordDAO = new DeductionRecordDAOImpl();
    private final ScoreDeductionDAO scoreDeductionDAO = new ScoreDeductionDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();
    private final ExamViewService dataService = new ExamViewServiceImpl();
    private final ExaminerViewDAO examinerDataDAO = new ExaminerViewDAOImpl();
    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final SessionService sessionService = new SessionServiceImpl();

    @Override
    public void clearPresent(int sessionId, int sbd) {
        presentSet(sessionId).remove(sbd);
    }

    @Override
    public void markPresent(int sessionId, int sbd) {
        presentSet(sessionId).add(sbd);
        procedureSet(sessionId).remove(sbd);
    }

    @Override
    public boolean isPresent(int sessionId, int sbd) {
        return presentSet(sessionId).contains(sbd);
    }

    @Override
    public void sendToProcedure(int sessionId, int sbd) {
        procedureSet(sessionId).add(sbd);
        presentSet(sessionId).remove(sbd);
        for (Lane lane : Lane.values()) {
            ExamQueue.remove(lane, sbd);
        }
    }

    @Override
    public boolean isInProcedureQueue(int sessionId, int sbd) {
        return procedureSet(sessionId).contains(sbd);
    }

    @Override
    public void removeCandidate(int sessionId, int sbd) {
        presentSet(sessionId).remove(sbd);
        procedureSet(sessionId).remove(sbd);
    }

    private Set<Integer> presentSet(int sessionId) {
        synchronized (PRESENT) {
            Set<Integer> set = PRESENT.get(sessionId);
            if (set == null) {
                set = new HashSet<>();
                PRESENT.put(sessionId, set);
            }
            return set;
        }
    }

    private Set<Integer> procedureSet(int sessionId) {
        synchronized (PROCEDURE) {
            Set<Integer> set = PROCEDURE.get(sessionId);
            if (set == null) {
                set = new HashSet<>();
                PROCEDURE.put(sessionId, set);
            }
            return set;
        }
    }

    @Override
    public EnrollmentDTO getRegistration(int sessionId, int sbd) {
        return dataService.findRegistration(sessionId, sbd);
    }

    @Override
    public ServiceResult<Void> updateCandidateProfile(int sessionId, int sbd, Integer actionUserId, String fullName,
            Date dateOfBirth, String governmentIdNumber, String phoneNumber, String address, String sex,
            String reasonForTaking) {
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        String trimmedName = trimParam(fullName);
        String trimmedGovId = trimParam(governmentIdNumber);
        if (trimmedName.isEmpty() || trimmedGovId.isEmpty() || dateOfBirth == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin hồ sơ không hợp lệ.");
        }
        String trimmedPhone = trimParam(phoneNumber);
        String trimmedAddress = trimParam(address);
        String trimmedReason = trimParam(reasonForTaking);
        Sex sexEnum = sex != null ? Sex.fromValue(sex) : Sex.MALE;
        String sexDb = sexEnum != null ? sexEnum.getValue() : Sex.MALE.getValue();
        SimpleDateFormat dobFmt = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder changes = new StringBuilder();
        appendChange(changes, "Họ và tên", reg.getFullName(), trimmedName);
        appendChange(changes, "Ngày sinh",
                reg.getDateOfBirth() != null ? dobFmt.format(reg.getDateOfBirth()) : null,
                dobFmt.format(dateOfBirth));
        appendChange(changes, "CCCD", reg.getGovIdNo(), trimmedGovId);
        appendChange(changes, "Số điện thoại", reg.getPhoneNo(), trimmedPhone);
        appendChange(changes, "Địa chỉ", reg.getAddress(), trimmedAddress);
        appendChange(changes, "Giới tính", reg.isSex() ? Sex.FEMALE.getValue() : Sex.MALE.getValue(), sexDb);
        appendChange(changes, "Lý do thi", reg.getReasonForTaking(), trimmedReason);
        boolean updated = enrollmentDAO.updateExaminerProfile(
                reg.getId(), trimmedName, dateOfBirth, trimmedGovId,
                trimmedPhone, trimmedAddress, sexDb, trimmedReason);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật hồ sơ thí sinh.");
        }
        if (actionUserId != null && changes.length() > 0) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Cập nhật hồ sơ SBD " + reg.getCandidateNumber() + ": " + changes, reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> callCandidate(int sessionId, Integer sbd, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination) {
        if (sbd == null || sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        if (!dataService.isCallEligible(sessionId, reg, isTheory, sectionName)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không đủ điều kiện để gọi.");
        }
        boolean called = insertCall(sessionId, reg, user, actionUserId, callDestination);
        if (!called) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận lệnh gọi thí sinh.");
        }
        Lane lane = ExamQueue.laneFor(examSection);
        ExamQueue.setCalledSbd(lane, reg.getCandidateNumber());
        ExamQueue.setActiveSbd(lane, reg.getCandidateNumber());
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Integer> callNextCandidate(int sessionId, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination) {
        Lane lane = ExamQueue.laneFor(examSection);
        Integer queued = ExamQueue.peekFirst(lane);
        if (queued != null && queued > 0) {
            if (callCandidate(sessionId, queued, user, actionUserId, examSection, isTheory, sectionName,
                    callDestination).isSuccess()) {
                return ServiceResult.ok(queued);
            }
        }
        List<EnrollmentDTO> all = registrationService.getCandidatesBySession(sessionId);
        for (EnrollmentDTO reg : all) {
            if (!dataService.isCallEligible(sessionId, reg, isTheory, sectionName)) {
                continue;
            }
            if (insertCall(sessionId, reg, user, actionUserId, callDestination)) {
                ExamQueue.setCalledSbd(lane, reg.getCandidateNumber());
                ExamQueue.setActiveSbd(lane, reg.getCandidateNumber());
                return ServiceResult.ok(reg.getCandidateNumber());
            }
        }
        return ServiceResult.fail(ErrorType.NOT_FOUND, "Không còn thí sinh đủ điều kiện để gọi.");
    }

    @Override
    public ServiceResult<Integer> callSelectedCandidates(int sessionId, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination, int[] sbds) {
        if (sbds == null || sbds.length == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Chưa chọn thí sinh.");
        }
        int count = 0;
        for (int sbd : sbds) {
            if (sbd <= 0) {
                continue;
            }
            if (callCandidate(sessionId, sbd, user, actionUserId, examSection, isTheory, sectionName,
                    callDestination).isSuccess()) {
                count++;
            }
        }
        if (count == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không gọi được thí sinh nào.");
        }
        return ServiceResult.ok(count);
    }

    @Override
    public ServiceResult<Void> callScoreEntryCandidate(int sessionId, Integer sbd, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination,
            boolean scoreEntry) {
        if (sbd == null || sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (!dataService.isScoreQueueEligible(sessionId, reg, isTheory, sectionName)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không đủ điều kiện nhập điểm.");
        }
        boolean called = insertScoreEntryCall(sessionId, reg, user, actionUserId, callDestination);
        if (!called) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận lệnh gọi nhập điểm.");
        }
        Lane lane = ExamQueue.laneFor(examSection);
        ExamQueue.setCalledSbd(lane, reg.getCandidateNumber());
        ExamQueue.setActiveSbd(lane, reg.getCandidateNumber());
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> setDeviceMaintenance(int deviceId, Integer actionUserId) {
        if (deviceId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiết bị không hợp lệ.");
        }
        boolean updated = deviceDAO.updateStatus(deviceId, false);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đặt thiết bị vào bảo trì.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_DEVICE,
                    "Đặt thiết bị vào bảo trì", deviceId);
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> setDeviceAvailable(int deviceId, Integer actionUserId) {
        if (deviceId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiết bị không hợp lệ.");
        }
        boolean updated = deviceDAO.updateStatus(deviceId, true);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đặt thiết bị sẵn sàng.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_DEVICE,
                    "Đặt thiết bị sẵn sàng", deviceId);
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> changeCandidateVehicle(int sessionId, int sbd, int deviceId, Integer actionUserId) {
        if (sessionId <= 0 || sbd <= 0 || deviceId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin gán xe không hợp lệ.");
        }
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        if (!isDeviceInSession(sessionId, deviceId)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiết bị không thuộc ca thi.");
        }
        boolean updated = enrollmentDAO.assignExamDevice(reg.getId(), sessionId, deviceId);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể gán xe cho thí sinh.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Gán xe #" + deviceId + " cho SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> updateTheoryScore(int sessionId, int sbd, User user, String password, Integer newScore,
            String reasonCode, String reasonDetail, Integer actionUserId) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn lý do sửa điểm.");
        }
        if (!verifyPassword(user, password)) {
            return ServiceResult.fail(ErrorType.PERMISSION_DENIED, "Mật khẩu xác nhận không đúng.");
        }
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        int score = newScore != null ? newScore : -1;
        int maxScore = dataService.theoryMaxQuestions();
        if (score < 0 || score > maxScore) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Điểm lý thuyết không hợp lệ.");
        }
        Integer oldScore = reg.getTheoryScore();
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        boolean updated = examScoreService.upsertTheoryCorrectCount(reg.getId(), score,
                dataService.theoryPassThreshold());
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật điểm lý thuyết.");
        }
        if (actionUserId != null) {
            String passed = score >= dataService.theoryPassThreshold() ? "Đạt" : "Trượt";
            String message = "Sửa điểm lý thuyết SBD " + reg.getCandidateNumber() + ": "
                    + (oldScore != null ? oldScore : "-") + " -> " + score + " (" + passed + ")";
            if (!auditReason.isBlank()) {
                message += " - Lý do: " + auditReason;
            }
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE, message, reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> logPracticalScoreEditReason(int sessionId, int sbd, User user, String password,
            String reasonCode, String reasonDetail, Integer actionUserId) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn lý do sửa điểm.");
        }
        if (!verifyPassword(user, password)) {
            return ServiceResult.fail(ErrorType.PERMISSION_DENIED, "Mật khẩu xác nhận không đúng.");
        }
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    "Sửa điểm thực hành SBD " + reg.getCandidateNumber()
                    + (auditReason.isBlank() ? "" : " - Lý do: " + auditReason),
                    reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> recordViolation(int sessionId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, String evidencePath) {
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null || reg.isSuspended()) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh hoặc thí sinh đã bị đình chỉ.");
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn lý do vi phạm.");
        }
        ViolationReason reason = ViolationReason.fromValue(reasonCode);
        String reasonLabel = reason != null ? reason.getValue()
                : reasonCode.trim();
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, evidencePath);
        auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                "Vi phạm SBD " + reg.getCandidateNumber() + ": " + auditText, reg.getId(), auditText);
        Candidate candidate = candidateDAO.getById(reg.getId());
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy hồ sơ thí sinh.");
        }
        candidate.setSuspended(true);
        boolean updated = candidateDAO.update(candidate);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận vi phạm.");
        }
        removeFromAllQueues(reg.getCandidateNumber());
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> markPresent(int sessionId, int sbd, Integer actionUserId) {
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null || reg.isAbsent() || reg.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể điểm danh.");
        }
        if (CandidateStatus.COMPLETED.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh đã hoàn tất phần thi.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
        if (enrollment != null && CandidateStatus.fromValue(enrollment.getSectionStatus()) == CandidateStatus.NOT_STARTED) {
            enrollment.setSectionStatus(CandidateStatus.IN_PROGRESS.getValue());
            enrollmentDAO.update(enrollment);
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Điểm danh SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> undoPresent(int sessionId, int sbd, Integer actionUserId) {
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null || reg.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể hoàn tác điểm danh.");
        }
        if (CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())
                || CandidateStatus.COMPLETED.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không thể hoàn tác điểm danh ở trạng thái hiện tại.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
        if (enrollment != null && CandidateStatus.fromValue(enrollment.getSectionStatus()) == CandidateStatus.IN_PROGRESS) {
            enrollment.setSectionStatus(CandidateStatus.NOT_STARTED.getValue());
            enrollmentDAO.update(enrollment);
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Hoàn tác điểm danh SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> sendWrongInfoToProcedure(int sessionId, int sbd, Integer actionUserId) {
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        removeFromAllQueues(reg.getCandidateNumber());
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Sai thông tin - chuyển phòng thủ tục SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> adjustScoreDeduction(int sessionId, int sbd, int deductionId, int delta,
            Integer actionUserId) {
        if (sbd <= 0 || deductionId <= 0 || delta == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin điều chỉnh điểm không hợp lệ.");
        }
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể điều chỉnh điểm.");
        }
        boolean updated = adjustScoreDeductionOccurrence(reg.getId(), sessionId, deductionId, delta);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể điều chỉnh điểm trừ.");
        }
        if (actionUserId != null) {
            String action = delta > 0 ? "cộng" : "trừ";
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    action + " điểm lỗi #" + deductionId + " cho SBD " + reg.getCandidateNumber()
                    + " (Δ=" + delta + ")",
                    reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> finalizeScoreEntry(int sessionId, int sbd, Integer actionUserId) {
        if (sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể hoàn tất nhập điểm.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy đăng ký thi.");
        }
        enrollment.setSectionStatus(CandidateStatus.AWAITING_SIGNATURE.getValue());
        boolean updated = enrollmentDAO.update(enrollment);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể hoàn tất nhập điểm.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Giám khảo hoàn tất nhập điểm SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return user != null && password != null && !password.isBlank()
                && AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash());
    }

    @Override
    public ServiceResult<Void> printSignatureForm(int sessionId, int sbd, Integer actionUserId) {
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null || !CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh chưa sẵn sàng in biên bản.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy đăng ký thi.");
        }
        enrollment.setSignaturePrinted(true);
        boolean updated = enrollmentDAO.update(enrollment);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận in biên bản.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "In biên bản kết quả thi SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> completeCandidateSection(int sessionId, int sbd, Integer actionUserId,
            Boolean sectionPassedHint) {
        EnrollmentDTO reg = getRegistration(sessionId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "notFound");
        }
        if (!CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "notAwaiting");
        }
        if (!reg.isSignaturePrinted()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "needSignaturePrint");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "completeFailed");
        }
        enrollment.setSectionStatus(CandidateStatus.COMPLETED.getValue());
        boolean completed = enrollmentDAO.update(enrollment);
        if (!completed) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "completeFailed");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Hoàn tất phần thi SBD " + reg.getCandidateNumber(), reg.getId());
        }
        boolean sectionPassed = sectionPassedHint != null
                ? sectionPassedHint
                : computeSectionPassed(sessionId, reg);
        if (!sectionPassed) {
            removeFromAllQueues(sbd);
        } else {
            enqueueNextSection(sessionId, reg);
        }
        return ServiceResult.ok(null);
    }

    private boolean isDeviceInSession(int sessionId, int deviceId) {
        List<Integer> areaIds = sessionDAO.getExamAreaIds(sessionId);
        for (ExamDevice device : deviceDAO.getAllByAreaIds(areaIds)) {
            if (device.getExamDeviceId() == deviceId) {
                return true;
            }
        }
        return false;
    }

    private boolean insertCall(int sessionId, EnrollmentDTO reg, User user, Integer actionUserId,
            String callDestination) {
        Audit audit = new Audit();
        audit.setUserId(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        audit.setAction("CALL");
        String entityId = sessionId + "-" + reg.getCandidateNo();
        String detail = "calledTo=" + callDestination + ";result=Calling";
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        int insertedId = auditDAO.insert(audit);
        if (insertedId > 0 && actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Gọi SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return insertedId > 0;
    }

    private boolean insertScoreEntryCall(int sessionId, EnrollmentDTO reg, User user, Integer actionUserId,
            String callDestination) {
        Audit audit = new Audit();
        audit.setUserId(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        audit.setAction("CALL");
        String entityId = sessionId + "-" + reg.getCandidateNo();
        String detail = "calledTo=" + callDestination + ";result=Calling";
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        int insertedId = auditDAO.insert(audit);
        if (insertedId > 0 && actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Gọi SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return insertedId > 0;
    }

    private static String trimParam(String value) {
        return value == null ? "" : value.trim();
    }

    private static void appendChange(StringBuilder changes, String field, String oldValue, String newValue) {
        String oldNorm = oldValue == null ? "" : oldValue.trim();
        String newNorm = newValue == null ? "" : newValue.trim();
        if (!oldNorm.equals(newNorm)) {
            if (changes.length() > 0) {
                changes.append("; ");
            }
            changes.append(field).append(": ")
                    .append(oldNorm.isEmpty() ? "-" : oldNorm)
                    .append(" -> ")
                    .append(newNorm.isEmpty() ? "-" : newNorm);
        }
    }

    private boolean computeSectionPassed(int sessionId, EnrollmentDTO reg) {
        int enrollmentId = reg.getEnrollment() != null ? reg.getEnrollment().getExamEnrollmentId() : 0;
        if (enrollmentId > 0) {
            Map<Integer, Boolean> flags = examinerDataDAO.loadPassFlagsBySession(sessionId);
            Boolean passed = flags.get(enrollmentId);
            if (passed != null) {
                return passed;
            }
        }
        if (reg.getTheoryScore() != null && reg.getTheoryScore() >= dataService.theoryPassThreshold()) {
            return true;
        }
        if (reg.getTheoryScore() != null) {
            return false;
        }
        return true;
    }

    private void enqueueNextSection(int sessionId, EnrollmentDTO reg) {
        int sbd = reg.getCandidateNumber();
        SessionViewDTO session = sessionService.getSessionById(sessionId);
        enums.SectionType examSection = session != null && session.getExamSection() != null
                ? session.getExamSection() : enums.SectionType.THEORY;
        Lane current = ExamQueue.laneFor(examSection);
        ExamQueue.remove(current, sbd);
        Candidate candidate = candidateDAO.getById(reg.getId());
        if (candidate == null) {
            return;
        }
        if (current == Lane.LY_THUYET) {
            if (Boolean.TRUE.equals(candidate.getTakeLayout())) {
                ExamQueue.offer(Lane.THUC_HANH_TRONG_HINH, sbd);
            }
        }
    }

    private static void removeFromAllQueues(int sbd) {
        for (Lane lane : Lane.values()) {
            ExamQueue.remove(lane, sbd);
        }
    }

    private static String buildViolationAuditText(String reasonLabel, String reasonDetail, String evidencePath) {
        StringBuilder text = new StringBuilder();
        if (reasonLabel != null && !reasonLabel.isBlank()) {
            text.append(reasonLabel);
        }
        if (reasonDetail != null && !reasonDetail.isBlank()) {
            if (text.length() > 0) {
                text.append(": ");
            }
            text.append(reasonDetail.trim());
        }
        if (evidencePath != null && !evidencePath.isBlank()) {
            if (text.length() > 0) {
                text.append(" | ");
            }
            text.append("Minh chứng: ").append(evidencePath);
        }
        return text.toString();
    }

    private static String buildReasonText(String reasonCode, String reasonDetail) {
        String label = switch (reasonCode != null ? reasonCode : "") {
            case "cham-sai" -> "Chấm sai";
            case "nhap-nham" -> "Nhập nhầm điểm";
            case "khieu-nai" -> "Thí sinh khiếu nại";
            case "khac" -> "Lý do khác";
            default -> "";
        };
        if (reasonDetail != null && !reasonDetail.isBlank()) {
            return label.isBlank() ? reasonDetail.trim() : label + ": " + reasonDetail.trim();
        }
        return label;
    }

    private boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int scoreDeductionId, int delta) {
        if (candidateId <= 0 || sessionId <= 0 || scoreDeductionId <= 0 || delta == 0) {
            return false;
        }
        ScoreDeduction rule = scoreDeductionDAO.getById(scoreDeductionId);
        if (rule == null) {
            return false;
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(sessionId, candidateId);
        if (enrollment == null) {
            return false;
        }
        int examResultId = examResultDAO.getExamResultIdByExamEnrollmentId(enrollment.getExamEnrollmentId());
        if (examResultId <= 0) {
            ExamResult result = new ExamResult();
            result.setExamEnrollmentId(enrollment.getExamEnrollmentId());
            result.setPassed(false);
            examResultId = examResultDAO.add(result);
        }
        if (examResultId <= 0) {
            return false;
        }
        int sectionId = rule.getExamSectionId();
        if (sectionId <= 0) {
            sectionId = loadSessionExamSectionId(sessionId);
        }
        if (sectionId <= 0) {
            return false;
        }
        ExamScore examScore = examScoreDAO.getByExamResultAndSection(examResultId, sectionId);
        int examScoreId;
        if (examScore == null) {
            ExamScore newScore = new ExamScore();
            newScore.setExamResultId(examResultId);
            newScore.setExamSectionId(sectionId);
            newScore.setScore(100);
            examScoreId = examScoreDAO.add(newScore);
        } else {
            examScoreId = examScore.getExamScoreId();
        }
        if (examScoreId <= 0) {
            return false;
        }
        if (!applyDeductionDelta(examScoreId, scoreDeductionId, delta)) {
            return false;
        }
        return examScoreDAO.recalculateFromDeductions(examScoreId);
    }

    private int loadSessionExamSectionId(int sessionId) {
        Integer sessionSectionId = sessionDAO.getExamSectionId(sessionId);
        if (sessionSectionId != null && sessionSectionId > 0) {
            return sessionSectionId;
        }
        model.ExamSection section = sectionDAO.getBySectionName(enums.SectionType.LAYOUT.getValue());
        if (section != null) {
            return section.getExamSectionId();
        }
        return 0;
    }

    private boolean applyDeductionDelta(int examScoreId, int scoreDeductionId, int delta) {
        int current = deductionRecordDAO.getOccurrenceCount(examScoreId, scoreDeductionId);
        int next = current + delta;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (current == 0 && delta > 0) {
            DeductionRecord record = new DeductionRecord();
            record.setExamScoreId(examScoreId);
            record.setScoreDeductionId(scoreDeductionId);
            record.setOccurrenceCount(delta);
            record.setRecordedAt(now);
            return deductionRecordDAO.add(record);
        }
        if (current > 0) {
            if (next <= 0) {
                return deductionRecordDAO.deleteByExamScoreAndRule(examScoreId, scoreDeductionId);
            }
            return deductionRecordDAO.updateOccurrence(examScoreId, scoreDeductionId, next, now);
        }
        return false;
    }
}
