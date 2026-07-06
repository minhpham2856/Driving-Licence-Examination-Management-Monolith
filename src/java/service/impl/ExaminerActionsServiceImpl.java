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
import dto.CandidateEnrollmentDTO;
import dto.ServiceResult;
import dto.SessionDTO;
import dto.payload.AdjustScoreDeductionCommand;
import dto.payload.CallCandidateCommand;
import dto.payload.CandidateSessionCommand;
import dto.payload.ChangeCandidateVehicleCommand;
import dto.payload.DeviceActionCommand;
import dto.payload.RecordViolationCommand;
import dto.payload.ScoreEditCommand;
import dto.payload.UpdateCandidateProfileCommand;
import enums.AuditAction;
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
import service.AuditLogService;
import service.ExamRegistrationService;
import service.ExamScoreService;
import service.ExamSessionControlService;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import util.ExamQueue;
import util.ExamQueue.Lane;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ExaminerActionsServiceImpl implements ExaminerActionsService {

    private final AuditLogService auditLogService = new AuditLogServiceImpl();
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
    private final ExaminerDataService dataService = new ExaminerDataServiceImpl();
    private final ExaminerViewDAO examinerDataDAO = new ExaminerViewDAOImpl();
    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();
    private final ExamSessionControlService sessionControlService = new ExamSessionControlServiceImpl();

    @Override
    public CandidateEnrollmentDTO getRegistration(int sessionId, int sbd) {
        return dataService.findRegistration(sessionId, sbd);
    }

    @Override
    public ServiceResult<Void> updateCandidateProfile(UpdateCandidateProfileCommand command) {
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        String trimmedName = trimParam(command.getFullName());
        String trimmedGovId = trimParam(command.getGovernmentIdNumber());
        if (trimmedName.isEmpty() || trimmedGovId.isEmpty() || command.getDateOfBirth() == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin hồ sơ không hợp lệ.");
        }
        String trimmedPhone = trimParam(command.getPhoneNumber());
        String trimmedAddress = trimParam(command.getAddress());
        String trimmedReason = trimParam(command.getReasonForTaking());
        Sex sex = command.getSex() != null ? Sex.fromValue(command.getSex()) : Sex.MALE;
        String sexDb = sex != null ? sex.getValue() : Sex.MALE.getValue();
        SimpleDateFormat dobFmt = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder changes = new StringBuilder();
        appendChange(changes, "Họ và tên", reg.getFullName(), trimmedName);
        appendChange(changes, "Ngày sinh",
                reg.getDateOfBirth() != null ? dobFmt.format(reg.getDateOfBirth()) : null,
                dobFmt.format(command.getDateOfBirth()));
        appendChange(changes, "CCCD", reg.getGovIdNo(), trimmedGovId);
        appendChange(changes, "Số điện thoại", reg.getPhoneNo(), trimmedPhone);
        appendChange(changes, "Địa chỉ", reg.getAddress(), trimmedAddress);
        appendChange(changes, "Giới tính", reg.isSex() ? Sex.FEMALE.getValue() : Sex.MALE.getValue(), sexDb);
        appendChange(changes, "Lý do thi", reg.getReasonForTaking(), trimmedReason);
        boolean updated = enrollmentDAO.updateExaminerProfile(
                reg.getId(), trimmedName, command.getDateOfBirth(), trimmedGovId,
                trimmedPhone, trimmedAddress, sexDb, trimmedReason);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật hồ sơ thí sinh.");
        }
        if (command.getActionUserId() != null && changes.length() > 0) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Cập nhật hồ sơ SBD " + reg.getSbd() + ": " + changes, reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> callCandidate(CallCandidateCommand command) {
        if (command.getSbd() == null || command.getSbd() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        if (!dataService.isCallEligible(command.getSessionId(), reg, command.isTheory(), command.getSectionName())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không đủ điều kiện để gọi.");
        }
        boolean called = insertCall(command.getSessionId(), reg, command.getUser(),
                command.getActionUserId(), command.getCallDestination());
        if (!called) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận lệnh gọi thí sinh.");
        }
        Lane lane = ExamQueue.laneFor(command.getExamSection());
        ExamQueue.setCalledSbd(lane, reg.getSbd());
        ExamQueue.setActiveSbd(lane, reg.getSbd());
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Integer> callNextCandidate(CallCandidateCommand command) {
        Lane lane = ExamQueue.laneFor(command.getExamSection());
        Integer queued = ExamQueue.peekFirst(lane);
        if (queued != null && queued > 0) {
            CallCandidateCommand single = copyCallCommand(command);
            single.setSbd(queued);
            if (callCandidate(single).isSuccess()) {
                return ServiceResult.ok(queued);
            }
        }
        List<CandidateEnrollmentDTO> all = registrationService.getCandidatesBySession(command.getSessionId());
        for (CandidateEnrollmentDTO reg : all) {
            if (!dataService.isCallEligible(command.getSessionId(), reg, command.isTheory(), command.getSectionName())) {
                continue;
            }
            if (insertCall(command.getSessionId(), reg, command.getUser(),
                    command.getActionUserId(), command.getCallDestination())) {
                ExamQueue.setCalledSbd(lane, reg.getSbd());
                ExamQueue.setActiveSbd(lane, reg.getSbd());
                return ServiceResult.ok(reg.getSbd());
            }
        }
        return ServiceResult.fail(ErrorType.NOT_FOUND, "Không còn thí sinh đủ điều kiện để gọi.");
    }

    @Override
    public ServiceResult<Integer> callSelectedCandidates(CallCandidateCommand command) {
        int[] sbds = command.getSbds();
        if (sbds == null || sbds.length == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Chưa chọn thí sinh.");
        }
        int count = 0;
        for (int sbd : sbds) {
            if (sbd <= 0) {
                continue;
            }
            CallCandidateCommand single = copyCallCommand(command);
            single.setSbd(sbd);
            if (callCandidate(single).isSuccess()) {
                count++;
            }
        }
        if (count == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không gọi được thí sinh nào.");
        }
        return ServiceResult.ok(count);
    }

    @Override
    public ServiceResult<Void> callScoreEntryCandidate(CallCandidateCommand command) {
        Integer sbd = command.getSbd();
        if (sbd == null || sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), sbd);
        if (!dataService.isScoreQueueEligible(command.getSessionId(), reg, command.isTheory(), command.getSectionName())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không đủ điều kiện nhập điểm.");
        }
        boolean called = insertScoreEntryCall(command.getSessionId(), reg, command.getUser(),
                command.getActionUserId(), command.getCallDestination());
        if (!called) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận lệnh gọi nhập điểm.");
        }
        Lane lane = ExamQueue.laneFor(command.getExamSection());
        ExamQueue.setCalledSbd(lane, reg.getSbd());
        ExamQueue.setActiveSbd(lane, reg.getSbd());
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> setDeviceMaintenance(DeviceActionCommand command) {
        if (command.getDeviceId() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiết bị không hợp lệ.");
        }
        boolean updated = deviceDAO.updateStatus(command.getDeviceId(), false);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đặt thiết bị vào bảo trì.");
        }
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.EXAM_DEVICE,
                    "Đặt thiết bị vào bảo trì", command.getDeviceId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> setDeviceAvailable(DeviceActionCommand command) {
        if (command.getDeviceId() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiết bị không hợp lệ.");
        }
        boolean updated = deviceDAO.updateStatus(command.getDeviceId(), true);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đặt thiết bị sẵn sàng.");
        }
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.EXAM_DEVICE,
                    "Đặt thiết bị sẵn sàng", command.getDeviceId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> changeCandidateVehicle(ChangeCandidateVehicleCommand command) {
        if (command.getSessionId() <= 0 || command.getSbd() <= 0 || command.getDeviceId() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin gán xe không hợp lệ.");
        }
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        if (!isDeviceInSession(command.getSessionId(), command.getDeviceId())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiết bị không thuộc ca thi.");
        }
        boolean updated = enrollmentDAO.assignExamDevice(reg.getId(), command.getSessionId(), command.getDeviceId());
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể gán xe cho thí sinh.");
        }
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Gán xe #" + command.getDeviceId() + " cho SBD " + reg.getSbd(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> updateTheoryScore(ScoreEditCommand command) {
        if (command.getReasonCode() == null || command.getReasonCode().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn lý do sửa điểm.");
        }
        if (!verifyPassword(command.getUser(), command.getPassword())) {
            return ServiceResult.fail(ErrorType.PERMISSION_DENIED, "Mật khẩu xác nhận không đúng.");
        }
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        int newScore = command.getNewScore() != null ? command.getNewScore() : -1;
        int maxScore = dataService.theoryMaxQuestions();
        if (newScore < 0 || newScore > maxScore) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Điểm lý thuyết không hợp lệ.");
        }
        Integer oldScore = reg.getTheoryScore();
        String auditReason = buildReasonText(command.getReasonCode(), command.getReasonDetail());
        boolean updated = examScoreService.upsertTheoryCorrectCount(reg.getId(), newScore,
                dataService.theoryPassThreshold());
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật điểm lý thuyết.");
        }
        if (command.getActionUserId() != null) {
            String passed = newScore >= dataService.theoryPassThreshold() ? "Đạt" : "Trượt";
            String message = "Sửa điểm lý thuyết SBD " + reg.getSbd() + ": "
                    + (oldScore != null ? oldScore : "-") + " -> " + newScore + " (" + passed + ")";
            if (!auditReason.isBlank()) {
                message += " - Lý do: " + auditReason;
            }
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.EXAM_SCORE, message, reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> logPracticalScoreEditReason(ScoreEditCommand command) {
        if (command.getReasonCode() == null || command.getReasonCode().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn lý do sửa điểm.");
        }
        if (!verifyPassword(command.getUser(), command.getPassword())) {
            return ServiceResult.fail(ErrorType.PERMISSION_DENIED, "Mật khẩu xác nhận không đúng.");
        }
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        String auditReason = buildReasonText(command.getReasonCode(), command.getReasonDetail());
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    "Sửa điểm thực hành SBD " + reg.getSbd()
                    + (auditReason.isBlank() ? "" : " - Lý do: " + auditReason),
                    reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> recordViolation(RecordViolationCommand command) {
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null || reg.isSuspended()) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh hoặc thí sinh đã bị đình chỉ.");
        }
        if (command.getReasonCode() == null || command.getReasonCode().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn lý do vi phạm.");
        }
        ViolationReason reason = ViolationReason.fromValue(command.getReasonCode());
        String reasonLabel = reason != null ? reason.getValue()
                : command.getReasonCode().trim();
        String detail = command.getReasonDetail() != null ? command.getReasonDetail().trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, command.getEvidencePath());
        auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.CANDIDATE,
                "Vi phạm SBD " + reg.getSbd() + ": " + auditText, reg.getId(), auditText);
        Candidate candidate = candidateDAO.getById(reg.getId());
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy hồ sơ thí sinh.");
        }
        candidate.setSuspended(true);
        boolean updated = candidateDAO.update(candidate);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận vi phạm.");
        }
        removeFromAllQueues(reg.getSbd());
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> markPresent(CandidateSessionCommand command) {
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null || reg.isAbsent() || reg.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể điểm danh.");
        }
        if (CandidateStatus.COMPLETED.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh đã hoàn tất phần thi.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(command.getSessionId(), reg.getId());
        if (enrollment != null && CandidateStatus.fromValue(enrollment.getSectionStatus()) == CandidateStatus.NOT_STARTED) {
            enrollment.setSectionStatus(CandidateStatus.IN_PROGRESS.getValue());
            enrollmentDAO.update(enrollment);
        }
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Điểm danh SBD " + reg.getSbd(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> undoPresent(CandidateSessionCommand command) {
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null || reg.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể hoàn tác điểm danh.");
        }
        if (CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())
                || CandidateStatus.COMPLETED.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không thể hoàn tác điểm danh ở trạng thái hiện tại.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(command.getSessionId(), reg.getId());
        if (enrollment != null && CandidateStatus.fromValue(enrollment.getSectionStatus()) == CandidateStatus.IN_PROGRESS) {
            enrollment.setSectionStatus(CandidateStatus.NOT_STARTED.getValue());
            enrollmentDAO.update(enrollment);
        }
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Hoàn tác điểm danh SBD " + reg.getSbd(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> sendWrongInfoToProcedure(CandidateSessionCommand command) {
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        removeFromAllQueues(reg.getSbd());
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Sai thông tin - chuyển phòng thủ tục SBD " + reg.getSbd(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> adjustScoreDeduction(AdjustScoreDeductionCommand command) {
        if (command.getSbd() <= 0 || command.getDeductionId() <= 0 || command.getDelta() == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin điều chỉnh điểm không hợp lệ.");
        }
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể điều chỉnh điểm.");
        }
        boolean updated = adjustScoreDeductionOccurrence(reg.getId(), command.getSessionId(),
                command.getDeductionId(), command.getDelta());
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể điều chỉnh điểm trừ.");
        }
        if (command.getActionUserId() != null) {
            String action = command.getDelta() > 0 ? "cộng" : "trừ";
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    action + " điểm lỗi #" + command.getDeductionId() + " cho SBD " + reg.getSbd()
                    + " (Δ=" + command.getDelta() + ")",
                    reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> finalizeScoreEntry(CandidateSessionCommand command) {
        if (command.getSbd() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể hoàn tất nhập điểm.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(command.getSessionId(), reg.getId());
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy đăng ký thi.");
        }
        enrollment.setSectionStatus(CandidateStatus.AWAITING_SIGNATURE.getValue());
        boolean updated = enrollmentDAO.update(enrollment);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể hoàn tất nhập điểm.");
        }
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Giám khảo hoàn tất nhập điểm SBD " + reg.getSbd(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return user != null && password != null && !password.isBlank()
                && AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash());
    }

    @Override
    public ServiceResult<Void> printSignatureForm(CandidateSessionCommand command) {
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null || !CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh chưa sẵn sàng in biên bản.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(command.getSessionId(), reg.getId());
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy đăng ký thi.");
        }
        enrollment.setSignaturePrinted(true);
        boolean updated = enrollmentDAO.update(enrollment);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận in biên bản.");
        }
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "In biên bản kết quả thi SBD " + reg.getSbd(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> completeCandidateSection(CandidateSessionCommand command) {
        CandidateEnrollmentDTO reg = getRegistration(command.getSessionId(), command.getSbd());
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "notFound");
        }
        if (!CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "notAwaiting");
        }
        if (!reg.isSignaturePrinted()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "needSignaturePrint");
        }
        ExamEnrollment enrollment = enrollmentDAO.getBySessionAndCandidate(command.getSessionId(), reg.getId());
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "completeFailed");
        }
        enrollment.setSectionStatus(CandidateStatus.COMPLETED.getValue());
        boolean completed = enrollmentDAO.update(enrollment);
        if (!completed) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "completeFailed");
        }
        if (command.getActionUserId() != null) {
            auditLogService.logAction(command.getActionUserId(), AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Hoàn tất phần thi SBD " + reg.getSbd(), reg.getId());
        }
        boolean sectionPassed = command.getSectionPassedHint() != null
                ? command.getSectionPassedHint()
                : computeSectionPassed(command.getSessionId(), reg);
        if (!sectionPassed) {
            removeFromAllQueues(command.getSbd());
        } else {
            enqueueNextSection(command.getSessionId(), reg);
        }
        return ServiceResult.ok(null);
    }

    private static CallCandidateCommand copyCallCommand(CallCandidateCommand source) {
        CallCandidateCommand copy = new CallCandidateCommand();
        copy.setSessionId(source.getSessionId());
        copy.setUser(source.getUser());
        copy.setActionUserId(source.getActionUserId());
        copy.setTheory(source.isTheory());
        copy.setExamSection(source.getExamSection());
        copy.setSectionName(source.getSectionName());
        copy.setCallDestination(source.getCallDestination());
        copy.setScoreEntry(source.isScoreEntry());
        return copy;
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

    private boolean insertCall(int sessionId, CandidateEnrollmentDTO reg, User user, Integer actionUserId,
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
            auditLogService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Gọi SBD " + reg.getSbd(), reg.getId());
        }
        return insertedId > 0;
    }

    private boolean insertScoreEntryCall(int sessionId, CandidateEnrollmentDTO reg, User user, Integer actionUserId,
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
            auditLogService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Gọi SBD " + reg.getSbd(), reg.getId());
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

    private boolean computeSectionPassed(int sessionId, CandidateEnrollmentDTO reg) {
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

    private void enqueueNextSection(int sessionId, CandidateEnrollmentDTO reg) {
        int sbd = reg.getSbd();
        SessionDTO session = sessionControlService.getSessionById(sessionId);
        enums.ExamSection examSection = session != null && session.getExamSection() != null
                ? session.getExamSection() : enums.ExamSection.THEORY;
        Lane current = ExamQueue.laneFor(examSection);
        ExamQueue.remove(current, sbd);
        Candidate candidate = candidateDAO.getById(reg.getId());
        if (candidate == null) {
            return;
        }
        if (current == Lane.LY_THUYET) {
            if (Boolean.TRUE.equals(candidate.getTakeLayout())) {
                ExamQueue.offer(Lane.THUC_HANH_TRONG_HINH, sbd);
            } else if (Boolean.TRUE.equals(candidate.getTakeRoad())) {
                ExamQueue.offer(Lane.THUC_HANH_TREN_DUONG, sbd);
            }
        } else if (current == Lane.THUC_HANH_TRONG_HINH && Boolean.TRUE.equals(candidate.getTakeRoad())) {
            ExamQueue.offer(Lane.THUC_HANH_TREN_DUONG, sbd);
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
        model.ExamSection section = sectionDAO.getBySectionName(enums.ExamSection.LAYOUT.getValue());
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
