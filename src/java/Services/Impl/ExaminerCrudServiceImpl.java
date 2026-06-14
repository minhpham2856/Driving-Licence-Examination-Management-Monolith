package Services.Impl;

import Constants.CandidateSectionStatus;
import Constants.ExamSectionType;
import Constants.ViolationReasonCodes;
import Controllers.Examiner.ExaminerScoreEntryQueue;
import Controllers.Staff.ExamStaff.ExaminerSlot;
import DAO.CandidateCallDAO;
import DAO.ExamDeviceDAO;
import DAO.ExamRegistrationDAO;
import DAO.Impl.CandidateCallDAOImpl;
import DAO.Impl.ExamDeviceDAOImpl;
import DAO.Impl.ExamRegistrationDAOImpl;
import Models.CandidateCall;
import Models.ExamRegistration;
import Models.User;
import Services.ExaminerCrudService;
import Services.ExaminerSessionContextService;
import Services.ExaminerViewDataService;
import Utils.AuditChangeDetails;
import Utils.AuditLogHelper;
import jakarta.servlet.http.HttpSession;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

public class ExaminerCrudServiceImpl implements ExaminerCrudService {

    private final ExamRegistrationDAO registrationDAO = new ExamRegistrationDAOImpl();
    private final CandidateCallDAO callDAO = new CandidateCallDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExaminerViewDataService viewDataService = new ExaminerViewDataServiceImpl();

    @Override
    public ExamRegistration findCandidate(int sessionId, String sbd) {
        return viewDataService.findRegistration(sessionId, sbd);
    }

    @Override
    public boolean updateCandidateProfile(int sessionId, String sbd, String fullName, String dobStr,
            String govIdNo, String email, String phoneNo, String address, String sex, String reasonForTaking,
            HttpSession session) {
        ExamRegistration reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        Date dob = parseDate(dobStr);
        if (dob == null) {
            return false;
        }
        String sexDb;
        if ("Nu".equalsIgnoreCase(sex) || "Nữ".equalsIgnoreCase(sex) || "1".equals(sex)) {
            sexDb = "Nữ";
        } else {
            sexDb = "Nam";
        }
        java.text.SimpleDateFormat dobFmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
        List<AuditChangeDetails.FieldChange> changes = new ArrayList<>();
        AuditChangeDetails.addIfChanged(changes, "Họ tên", reg.getFullName(), fullName.trim());
        AuditChangeDetails.addIfChanged(changes, "Ngày sinh",
                reg.getDateOfBirth() != null ? dobFmt.format(reg.getDateOfBirth()) : null,
                dobFmt.format(dob));
        AuditChangeDetails.addIfChanged(changes, "CCCD", reg.getGovIdNo(), govIdNo.trim());
        AuditChangeDetails.addIfChanged(changes, "Email", reg.getEmail(), email != null ? email.trim() : null);
        AuditChangeDetails.addIfChanged(changes, "SĐT", reg.getPhoneNo(), phoneNo != null ? phoneNo.trim() : null);
        AuditChangeDetails.addIfChanged(changes, "Địa chỉ", reg.getAddress(), address != null ? address.trim() : null);
        AuditChangeDetails.addIfChanged(changes, "Giới tính", reg.isGender() ? "Nữ" : "Nam", sexDb);
        AuditChangeDetails.addIfChanged(changes, "Lý do thi", reg.getReasonForTaking(),
                reasonForTaking != null ? reasonForTaking.trim() : null);
        boolean updated = registrationDAO.updateExaminerProfile(
                reg.getId(), fullName.trim(), dob, govIdNo.trim(),
                email != null ? email.trim() : null,
                phoneNo != null ? phoneNo.trim() : null,
                address != null ? address.trim() : null,
                sexDb,
                reasonForTaking != null ? reasonForTaking.trim() : null);
        if (updated && session != null && !changes.isEmpty()) {
            AuditLogHelper.persistFieldChanges(session, "UPDATE on Profile",
                    "Giám khảo cập nhật thông tin SBD " + reg.getSbd(), changes, null, reg.getId());
        }
        return updated;
    }

    @Override
    public boolean markAbsent(int sessionId, String sbd, HttpSession session) {
        ExamRegistration reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        registrationDAO.updateScores(reg.getId(), 0, "failed", 0, "failed");
        boolean updated = registrationDAO.markAbsent(reg.getId());
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "Giám khảo xác nhận vắng thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public boolean undoAbsent(int sessionId, String sbd, HttpSession session) {
        ExamRegistration reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        boolean updated = registrationDAO.clearAbsentMarking(reg.getId());
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "Giám khảo hoàn tác vắng thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public boolean callCandidate(int sessionId, String sbd, User user, HttpSession session) {
        ExamRegistration reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        ExamSectionType sectionType = resolveSectionType(session);
        String sectionName = resolveSectionName(session);
        if (!viewDataService.isCallEligible(sessionId, reg, sectionType, sectionName)) {
            return false;
        }
        return insertCall(sessionId, reg, user, session);
    }

    @Override
    public String callNextCandidate(int sessionId, User user, HttpSession session) {
        ExamSectionType sectionType = resolveSectionType(session);
        String sectionName = resolveSectionName(session);
        List<ExamRegistration> all = registrationDAO.getCandidatesBySession(sessionId);
        for (ExamRegistration reg : all) {
            if (!viewDataService.isCallEligible(sessionId, reg, sectionType, sectionName)) {
                continue;
            }
            if (insertCall(sessionId, reg, user, session)) {
                return reg.getSbd();
            }
        }
        return null;
    }

    @Override
    public int callSelectedCandidates(int sessionId, String[] sbds, User user, HttpSession session) {
        if (sbds == null || sbds.length == 0) {
            return 0;
        }
        int count = 0;
        for (String sbd : sbds) {
            if (sbd == null || sbd.isBlank()) {
                continue;
            }
            if (callCandidate(sessionId, sbd.trim(), user, session)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String autoCallScoreEntryIfNeeded(int sessionId, User user, HttpSession session) {
        if (session == null) {
            return null;
        }
        String active = ExaminerScoreEntryQueue.getActiveSbd(session, sessionId);
        String called = ExaminerScoreEntryQueue.getCalledSbd(session, sessionId);
        if (active != null && !active.isBlank()) {
            return active;
        }
        if (called != null && !called.isBlank()) {
            ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, called);
            return called;
        }
        String first = ExaminerScoreEntryQueue.firstInQueue(session, sessionId);
        if (first == null || first.isBlank()) {
            return null;
        }
        if (callScoreEntryCandidate(sessionId, first, user, session)) {
            return first;
        }
        return null;
    }

    @Override
    public boolean callScoreEntryCandidate(int sessionId, String sbd, User user, HttpSession session) {
        if (sbd == null || sbd.isBlank()) {
            return false;
        }
        ExamRegistration reg = findCandidate(sessionId, sbd.trim());
        ExamSectionType sectionType = resolveSectionType(session);
        String sectionName = resolveSectionName(session);
        if (!viewDataService.isScoreQueueEligible(sessionId, reg, sectionType, sectionName)) {
            return false;
        }
        ExaminerScoreEntryQueue.setCalledSbd(session, sessionId, reg.getSbd());
        ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, reg.getSbd());
        return insertScoreEntryCall(sessionId, reg, user, session);
    }

    @Override
    public String deferScoreEntryAbsent(int sessionId, String sbd, User user, HttpSession session) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        ExamRegistration reg = findCandidate(sessionId, sbd.trim());
        if (reg == null) {
            return null;
        }
        String nextSbd = ExaminerScoreEntryQueue.moveToBottom(session, sessionId, reg.getSbd());
        if (session != null) {
            AuditLogHelper.persist(session, "UPDATE ScoreEntryQueue",
                    "Giám khảo đẩy xuống cuối hàng đợi nhập điểm (chưa vắng) SBD " + reg.getSbd(),
                    reg.getId());
        }
        if (nextSbd == null || nextSbd.isBlank()) {
            return null;
        }
        if (callScoreEntryCandidate(sessionId, nextSbd, user, session)) {
            return nextSbd;
        }
        ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, nextSbd);
        return nextSbd;
    }

    @Override
    public boolean setDeviceMaintenance(int deviceId, HttpSession session) {
        if (deviceId <= 0) {
            return false;
        }
        boolean updated = deviceDAO.updateStatus(deviceId, "Maintenance");
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamDevice",
                    "Giám khảo chuyển thiết bị #" + deviceId + " sang bảo trì", deviceId);
        }
        return updated;
    }

    @Override
    public boolean setDeviceAvailable(int deviceId, HttpSession session) {
        if (deviceId <= 0) {
            return false;
        }
        boolean updated = deviceDAO.updateStatus(deviceId, "Available");
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamDevice",
                    "Giám khảo chuyển thiết bị #" + deviceId + " sang sử dụng", deviceId);
        }
        return updated;
    }

    private static ExamSectionType resolveSectionType(HttpSession session) {
        if (session == null) {
            return ExamSectionType.THEORY;
        }
        Object value = session.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof ExamSectionType) {
            return (ExamSectionType) value;
        }
        return ExamSectionType.THEORY;
    }

    private static String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlot) {
            return ((ExaminerSlot) slotObj).getExamTypeName();
        }
        Object name = session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }

    private boolean insertCall(int sessionId, ExamRegistration reg, User user, HttpSession session) {
        CandidateCall call = new CandidateCall();
        call.setExamSessionId(sessionId);
        call.setCandidateNo(reg.getCandidateNo());
        call.setCalledTo("Phòng thi lý thuyết");
        call.setCalledBy(user != null && user.getId() > 0 ? user.getId() : 0);
        call.setResult("Calling");
        boolean inserted = callDAO.insert(call);
        if (inserted && session != null) {
            AuditLogHelper.persist(session, "INSERT on CandidateCall",
                    "Giám khảo gọi thí sinh SBD " + reg.getSbd(), reg.getId());
        }
        return inserted;
    }

    private boolean insertScoreEntryCall(int sessionId, ExamRegistration reg, User user, HttpSession session) {
        CandidateCall call = new CandidateCall();
        call.setExamSessionId(sessionId);
        call.setCandidateNo(reg.getCandidateNo());
        call.setCalledTo(resolveScoreEntryCallDestination(session));
        call.setCalledBy(user != null && user.getId() > 0 ? user.getId() : 0);
        call.setResult("Calling");
        boolean inserted = callDAO.insert(call);
        if (inserted && session != null) {
            AuditLogHelper.persist(session, "INSERT on CandidateCall",
                    "Giám khảo gọi thí sinh nhập điểm SBD " + reg.getSbd(), reg.getId());
        }
        return inserted;
    }

    private static String resolveScoreEntryCallDestination(HttpSession session) {
        if (session == null) {
            return "Khu vực thi thực hành";
        }
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlot slot && slot.getAreaName() != null && !slot.getAreaName().isBlank()) {
            return slot.getAreaName();
        }
        Object sectionName = session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        if (sectionName != null && !String.valueOf(sectionName).isBlank()) {
            return String.valueOf(sectionName);
        }
        return "Khu vực thi thực hành";
    }

    @Override
    public boolean updateTheoryScore(int sessionId, String sbd, int newScore, String reasonCode,
            String reasonDetail, User user, String password, HttpSession session) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }
        if (user == null || password == null || password.isBlank()
                || !AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash())) {
            return false;
        }
        ExamRegistration reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        int maxScore = viewDataService.theoryMaxQuestions();
        if (newScore < 0 || newScore > maxScore) {
            return false;
        }
        Integer oldScore = reg.getTheoryScore();
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        boolean updated = registrationDAO.updateTheoryCorrectCount(
                reg.getId(), newScore, viewDataService.theoryPassThreshold());
        if (updated && session != null) {
            String passed = newScore >= viewDataService.theoryPassThreshold() ? "passed" : "failed";
            AuditLogHelper.persistFieldChanges(session, "UPDATE ExamScore",
                    "Giám khảo điều chỉnh điểm LT SBD " + reg.getSbd(),
                    List.of(new AuditChangeDetails.FieldChange(
                            "Điểm lý thuyết",
                            oldScore != null ? String.valueOf(oldScore) : "—",
                            newScore + " (" + passed.toUpperCase() + ")")),
                    auditReason.isBlank() ? null : auditReason,
                    reg.getId());
        }
        return updated;
    }

    @Override
    public boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,
            String evidencePath, int[] deductionIds, HttpSession session) {
        ExamRegistration reg = findCandidate(sessionId, sbd);
        if (reg == null || reg.isSuspended()) {
            return false;
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }

        String reasonLabel = ViolationReasonCodes.labelOf(reasonCode);
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, evidencePath);
        boolean hasDeductions = deductionIds != null && deductionIds.length > 0;

        AuditLogHelper.persistWarning(session,
                "Đình chỉ vi phạm SBD " + reg.getSbd() + ": " + auditText, auditText, reg.getId());

        ExamSectionType sectionType = resolveSectionType(session);
        if (sectionType == ExamSectionType.SCORE_BASED && hasDeductions) {
            String sectionName = resolveSectionName(session);
            registrationDAO.applyScoreDeductions(reg.getId(), deductionIds, sectionName);
        }

        return registrationDAO.markSuspended(reg.getId());
    }

    @Override
    public boolean undoSuspension(int sessionId, String sbd, String reasonCode, String reasonDetail,
            HttpSession session) {
        ExamRegistration reg = findCandidate(sessionId, sbd);
        if (reg == null || !reg.isSuspended()) {
            return false;
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }

        String reasonLabel = ViolationReasonCodes.labelOf(reasonCode);
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, null);
        boolean undone = registrationDAO.undoSuspension(reg.getId());
        if (undone && session != null) {
            AuditLogHelper.persistFieldChanges(session, "UPDATE Candidate",
                    "Hoàn tác đình chỉ SBD " + reg.getSbd(),
                    List.of(new AuditChangeDetails.FieldChange(
                            "Trạng thái", "Đình chỉ", "Hoạt động bình thường")),
                    auditText, reg.getId());
        }
        return undone;
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return user != null && password != null && !password.isBlank()
                && AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash());
    }

    @Override
    public boolean printSignatureForm(int sessionId, String sbd, HttpSession session) {
        ExamRegistration reg = findCandidate(sessionId, sbd);
        if (reg == null || !CandidateSectionStatus.isAwaitingSignature(reg.getSectionStatus())) {
            return false;
        }
        boolean updated = registrationDAO.markSignaturePrinted(reg.getId(), sessionId);
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "Giám khảo in biên bản ký tên SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public String completeCandidateSection(int sessionId, String sbd, HttpSession session) {
        ExamRegistration reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return "notFound";
        }
        if (!CandidateSectionStatus.isAwaitingSignature(reg.getSectionStatus())) {
            return "notAwaiting";
        }
        if (!reg.isSignaturePrinted()) {
            return "needSignaturePrint";
        }
        boolean completed = registrationDAO.completeSection(reg.getId(), sessionId);
        if (!completed) {
            return "completeFailed";
        }
        if (session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "Giám khảo hoàn tất phần thi SBD " + reg.getSbd(), reg.getId());
        }
        return null;
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
}
