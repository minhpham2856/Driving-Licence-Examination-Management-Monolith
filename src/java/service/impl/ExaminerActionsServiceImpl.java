package service.impl;
import dto.*;
import model.*;
import java.text.*;
import dao.*;
import dao.impl.*;
import enums.*;
import model.*;
import service.*;
import service.impl.*;
import dto.ExaminerSlotDTO;
import dao.AuditDAO;
import dao.ExamEnrollmentDAO;
import dao.ExamDeviceDAO;
import dao.SessionDAO;
import dao.CandidateDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.CandidateDAOImpl;
import dao.impl.SessionDAOImpl;
import dto.CandidateEnrollmentDTO;
import model.User;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import util.ExamQueue;
import util.ExamQueue.Lane;
import util.examstaff.CallAuditFormatter;
import service.AuditLogService;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import enums.Gender;
import enums.SectionStatus;
import enums.ViolationReason;
public class ExaminerActionsServiceImpl implements ExaminerActionsService {
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final CandidateEnrollmentViewSupport enrollmentViewSupport = new CandidateEnrollmentViewSupport();
    private final AuditDAO auditDAO = new AuditDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamEnrollmentDAO vehicleDAO = new ExamEnrollmentDAOImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final SessionViewSupport sessionViewSupport = new SessionViewSupport();
    private final ExamScoreDAO examScoreDAO = new ExamScoreDAOImpl();
    private final ExamResultDAO examResultDAO = new ExamResultDAOImpl();
    private final ScoreDeductionDAO scoreDeductionDAO = new ScoreDeductionDAOImpl();
    private final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    @Override
    public CandidateEnrollmentDTO findCandidate(int sessionId, int sbd) {
        return viewDataService.findRegistration(sessionId, sbd);
    }
    @Override
    public boolean updateCandidateProfile(int sessionId, int sbd, String fullName, String dobStr,
            String govIdNo, String email, String phoneNo, String address, String sex, String reasonForTaking,
            Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        Date dob = parseDate(dobStr);
        if (dob == null) {
            return false;
        }
        String sexDb = Gender.isFemale(sex) ? Gender.NU.getDisplayName() : Gender.NAM.getDisplayName();
        SimpleDateFormat dobFmt = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder changes = new StringBuilder();
        appendChange(changes, "Họ và tên", reg.getFullName(), fullName.trim());
        appendChange(changes, "Ngày sinh",
                reg.getDateOfBirth() != null ? dobFmt.format(reg.getDateOfBirth()) : null, dobFmt.format(dob));
        appendChange(changes, "CCCD", reg.getGovIdNo(), govIdNo.trim());
        appendChange(changes, "Email", reg.getEmail(), email != null ? email.trim() : null);
        appendChange(changes, "Số điện thoại", reg.getPhoneNo(),
                phoneNo != null ? phoneNo.trim() : null);
        appendChange(changes, "Địa chỉ", reg.getAddress(), address != null ? address.trim() : null);
        appendChange(changes, "Giới tính", reg.isSex() ? "Nữ" : "Nam", sexDb);
        appendChange(changes, "Lý do thi", reg.getReasonForTaking(),
                reasonForTaking != null ? reasonForTaking.trim() : null);
        boolean updated = enrollmentDAO.updateExaminerProfile(
                reg.getId(), fullName.trim(), dob, govIdNo.trim(),
                email != null ? email.trim() : null,
                phoneNo != null ? phoneNo.trim() : null,
                address != null ? address.trim() : null,
                sexDb,
                reasonForTaking != null ? reasonForTaking.trim() : null);
        if (updated && actionUserId != null && changes.length() > 0) {
            auditLogService.logAction(actionUserId, "UPDATE Candidate",
                    "Cập nhật hồ sơ SBD " + reg.getSbd() + ": " + changes, reg.getId());
        }
        return updated;
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
    @Override
    public boolean markAbsent(int sessionId, int sbd, Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        examScoreDAO.updateScores(reg.getId(), 0, "failed", 0, "failed");
        boolean updated = enrollmentDAO.markAbsent(reg.getId());
        if (updated) {
            removeFromAllQueues(reg.getSbd());
        }
        if (updated && actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE ExamRegistration",
                    "Đánh dấu vắng SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }
    @Override
    public boolean undoAbsent(int sessionId, int sbd, Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        boolean updated = enrollmentDAO.clearAbsentMarking(reg.getId());
        if (updated && actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE ExamRegistration",
                    "Bỏ đánh dấu vắng SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }
    @Override
    public boolean callCandidate(int sessionId, int sbd, User user, Integer actionUserId, boolean isTheory,
            String sectionName, String callDestination) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        if (!viewDataService.isCallEligible(sessionId, reg, isTheory, sectionName)) {
            return false;
        }
        boolean called = insertCall(sessionId, reg, user, actionUserId, callDestination);
        if (called) {
            Lane lane = ExamQueue.resolveLane(isTheory, sectionName);
            ExamQueue.setCalledSbd(lane, reg.getSbd());
            ExamQueue.setActiveSbd(lane, reg.getSbd());
        }
        return called;
    }
    @Override
    public Integer callNextCandidate(int sessionId, User user, Integer actionUserId, boolean isTheory,
            String sectionName, String callDestination) {
        Lane lane = ExamQueue.resolveLane(isTheory, sectionName);
        Integer queued = ExamQueue.peekFirst(lane);
        if (queued != null && queued > 0) {
            if (callCandidate(sessionId, queued, user, actionUserId, isTheory, sectionName, callDestination)) {
                return queued;
            }
        }
        List<CandidateEnrollmentDTO> all = enrollmentViewSupport.getCandidatesBySession(sessionId);
        for (CandidateEnrollmentDTO reg : all) {
            if (!viewDataService.isCallEligible(sessionId, reg, isTheory, sectionName)) {
                continue;
            }
            if (insertCall(sessionId, reg, user, actionUserId, callDestination)) {
                ExamQueue.setCalledSbd(lane, reg.getSbd());
                ExamQueue.setActiveSbd(lane, reg.getSbd());
                return reg.getSbd();
            }
        }
        return null;
    }
    @Override
    public int callSelectedCandidates(int sessionId, int[] sbds, User user, Integer actionUserId,
            boolean isTheory, String sectionName, String callDestination) {
        if (sbds == null || sbds.length == 0) {
            return 0;
        }
        int count = 0;
        for (int sbd : sbds) {
            if (sbd <= 0) {
                continue;
            }
            if (callCandidate(sessionId, sbd, user, actionUserId, isTheory, sectionName, callDestination)) {
                count++;
            }
        }
        return count;
    }
    @Override
    public boolean callScoreEntryCandidate(int sessionId, int sbd, User user, Integer actionUserId,
            boolean isTheory, String sectionName, String callDestination) {
        if (sbd <= 0) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (!viewDataService.isScoreQueueEligible(sessionId, reg, isTheory, sectionName)) {
            return false;
        }
        boolean called = insertScoreEntryCall(sessionId, reg, user, actionUserId, callDestination);
        if (called) {
            Lane lane = ExamQueue.resolveLane(isTheory, sectionName);
            ExamQueue.setCalledSbd(lane, reg.getSbd());
            ExamQueue.setActiveSbd(lane, reg.getSbd());
        }
        return called;
    }
    @Override
    public boolean setDeviceMaintenance(int deviceId, Integer actionUserId) {
        if (deviceId <= 0) {
            return false;
        }
        boolean updated = deviceDAO.updateStatus(deviceId, false);
        if (updated && actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE ExamDevice",
                    "Đặt thiết bị vào bảo trì", deviceId);
        }
        return updated;
    }
    @Override
    public boolean setDeviceAvailable(int deviceId, Integer actionUserId) {
        if (deviceId <= 0) {
            return false;
        }
        boolean updated = deviceDAO.updateStatus(deviceId, true);
        if (updated && actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE ExamDevice",
                    "Đặt thiết bị sẵn sàng", deviceId);
        }
        return updated;
    }
    @Override
    public boolean changeCandidateVehicle(int sessionId, int sbd, int deviceId, Integer actionUserId) {
        if (sessionId <= 0 || sbd <= 0 || deviceId <= 0) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        if (!isDeviceInSession(sessionId, deviceId)) {
            return false;
        }
        boolean updated = vehicleDAO.assignExamDevice(reg.getId(), sessionId, deviceId);
        if (updated && actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE ExamEnrollment",
                    "Gán xe #" + deviceId + " cho SBD " + reg.getSbd(),
                    reg.getId());
        }
        return updated;
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
        String detail = CallAuditFormatter.formatDetail(callDestination, "Calling");
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        int insertedId = auditDAO.insert(audit);
        if (insertedId > 0 && actionUserId != null) {
            auditLogService.logAction(actionUserId, "INSERT on CandidateCall",
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
        String detail = CallAuditFormatter.formatDetail(callDestination, "Calling");
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        int insertedId = auditDAO.insert(audit);
        if (insertedId > 0 && actionUserId != null) {
            auditLogService.logAction(actionUserId, "INSERT on CandidateCall",
                    "Gọi SBD " + reg.getSbd(), reg.getId());
        }
        return insertedId > 0;
    }
    @Override
    public boolean updateTheoryScore(int sessionId, int sbd, int newScore, String reasonCode,
            String reasonDetail, User user, String password, Integer actionUserId) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }
        if (user == null || password == null || password.isBlank()
                || !AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash())) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        int maxScore = viewDataService.theoryMaxQuestions();
        if (newScore < 0 || newScore > maxScore) {
            return false;
        }
        Integer oldScore = reg.getTheoryScore();
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        boolean updated = examResultDAO.updateTheoryCorrectCount(reg.getId(), newScore,
                viewDataService.theoryPassThreshold());
        if (updated && actionUserId != null) {
            String passed = newScore >= viewDataService.theoryPassThreshold() ? "Đạt" : "Trượt";
            String message = "Sửa điểm lý thuyết SBD " + reg.getSbd() + ": "
                    + (oldScore != null ? oldScore : "-") + " -> " + newScore + " (" + passed + ")";
            if (!auditReason.isBlank()) {
                message += " - Lý do: " + auditReason;
            }
            auditLogService.logAction(actionUserId, "UPDATE ExamScore", message, reg.getId());
        }
        return updated;
    }
    @Override
    public boolean logPracticalScoreEditReason(int sessionId, int sbd, String reasonCode,
            String reasonDetail, User user, String password, Integer actionUserId) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }
        if (user == null || password == null || password.isBlank()
                || !AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash())) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        if (actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE ExamScore",
                    "Sửa điểm thực hành SBD " + reg.getSbd()
                    + (auditReason.isBlank() ? "" : " - Lý do: " + auditReason),
                    reg.getId());
        }
        return true;
    }
    @Override
    public boolean recordViolation(int sessionId, int sbd, String reasonCode, String reasonDetail,
            String evidencePath, int[] deductionIds, Integer actionUserId, boolean isTheory,
            String sectionName) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || reg.isSuspended()) {
            return false;
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }
        String reasonLabel = ViolationReason.resolveLabel(reasonCode);
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, evidencePath);
        boolean hasDeductions = deductionIds != null && deductionIds.length > 0;
        auditLogService.logWarning(actionUserId,
                "Vi phạm SBD " + reg.getSbd() + ": " + auditText, auditText, reg.getId());
        if (!isTheory && hasDeductions) {
            // applyScoreDeductions removed
        }
        Candidate c = candidateDAO.getById(reg.getId());
        if (c != null) {
            c.setSuspended(true);
            return candidateDAO.update(c);
        }
        return false;
    }
    @Override
    public boolean undoSuspension(int sessionId, int sbd, String reasonCode, String reasonDetail,
            Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || !reg.isSuspended()) {
            return false;
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }
        String reasonLabel = ViolationReason.resolveLabel(reasonCode);
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, null);
        Candidate c = candidateDAO.getById(reg.getId());
        boolean undone = false;
        if (c != null) {
            c.setSuspended(false);
            undone = candidateDAO.update(c);
        }
        if (undone && actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE Candidate",
                    "Bỏ đình chỉ SBD " + reg.getSbd() + " - Trạng thái: Đình chỉ -> Hoạt động bình thường"
                    + (auditText.isBlank() ? "" : " - " + auditText),
                    reg.getId());
        }
        return undone;
    }
    @Override
    public boolean adjustScoreDeduction(int sessionId, int sbd, int deductionId, int delta, Integer actionUserId) {
        if (sbd <= 0 || deductionId <= 0 || delta == 0) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return false;
        }
        boolean updated = scoreDeductionDAO.adjustScoreDeductionOccurrence(
                reg.getId(), sessionId, deductionId, delta);
        if (updated && actionUserId != null) {
            String action = delta > 0 ? "cộng" : "trừ";
            auditLogService.logAction(actionUserId, "UPDATE Score_Deduction",
                    action + " điểm lỗi #" + deductionId + " cho SBD " + reg.getSbd()
                    + " (Δ=" + delta + ")",
                    reg.getId());
        }
        return updated;
    }
    @Override
    public boolean finalizeScoreEntry(int sessionId, int sbd, Integer actionUserId, String sectionKeyword) {
        if (sbd <= 0) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return false;
        }
        ExamEnrollment e = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
        boolean updated = false;
        if (e != null) {
            e.setSectionStatus(SectionStatus.CHO_KY.getDisplayName());
            updated = enrollmentDAO.update(e);
        }
        if (updated && actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE ExamRegistration",
                    "Giám khảo hoàn tất nhập điểm SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }
    @Override
    public boolean verifyPassword(User user, String password) {
        return user != null && password != null && !password.isBlank()
                && AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash());
    }
    @Override
    public boolean printSignatureForm(int sessionId, int sbd, Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || !SectionStatus.isAwaitingSignature(reg.getSectionStatus())) {
            return false;
        }
        ExamEnrollment e = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
        boolean updated = false;
        if (e != null) {
            e.setSignaturePrinted(true);
            updated = enrollmentDAO.update(e);
        }
        if (updated && actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE ExamRegistration",
                    "In biên bản kết quả thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }
    @Override
    public String completeCandidateSection(int sessionId, int sbd, Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return "notFound";
        }
        if (!SectionStatus.isAwaitingSignature(reg.getSectionStatus())) {
            return "notAwaiting";
        }
        if (!reg.isSignaturePrinted()) {
            return "needSignaturePrint";
        }
        ExamEnrollment e = enrollmentDAO.getBySessionAndCandidate(sessionId, reg.getId());
        boolean completed = false;
        if (e != null) {
            e.setSectionStatus(SectionStatus.DA_THI.getDisplayName());
            completed = enrollmentDAO.update(e);
        }
        if (!completed) {
            return "completeFailed";
        }
        if (actionUserId != null) {
            auditLogService.logAction(actionUserId, "UPDATE ExamRegistration",
                    "Hoàn tất phần thi SBD " + reg.getSbd(), reg.getId());
        }
        enqueueNextSection(sessionId, reg);
        return null;
    }
    private void enqueueNextSection(int sessionId, CandidateEnrollmentDTO reg) {
        int sbd = reg.getSbd();
        SessionDTO session = sessionViewSupport.toDto(sessionId);
        String sectionName = session != null ? session.getExamTypeName() : null;
        boolean isTheory = enums.ExamSection.isTheory(sectionName);
        Lane current = ExamQueue.resolveLane(isTheory, sectionName);
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
    private static Date parseDate(String dobStr) {
        if (dobStr == null || dobStr.isBlank()) {
            return null;
        }
        try {
            if (dobStr.contains("/")) {
                String[] parts = dobStr.split("/");
                if (parts.length == 3) {
                    return Date.valueOf(parts[2] + "-" + parts[1] + "-" + parts[0]);
                }
            }
            return Date.valueOf(dobStr);
        } catch (Exception e) {
            return null;
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
            case "cham-sai" ->
                "Chấm sai";
            case "nhap-nham" ->
                "Nhập nhầm điểm";
            case "khieu-nai" ->
                "Thí sinh khiếu nại";
            case "khac" ->
                "Lý do khác";
            default ->
                "";
        };
        if (reasonDetail != null && !reasonDetail.isBlank()) {
            return label.isBlank() ? reasonDetail.trim() : label + ": " + reasonDetail.trim();
        }
        return label;
    }
}
