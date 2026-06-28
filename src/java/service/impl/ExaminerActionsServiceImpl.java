package service.impl;

import enums.SectionType;

import controller.examiner.ExaminerScoreEntryQueue;

import dto.examiner.ExaminerSlotDTO;

import dao.CandidateCallDAO;
import dao.ExamCandidateVehicleDAO;
import dao.ExamDeviceDAO;
import dao.CandidateDAO;
import dao.ExaminerSessionDataDAO;
import dao.impl.CandidateCallDAOImpl;
import dao.impl.ExamCandidateVehicleDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.CandidateDAOImpl;
import dao.impl.ExaminerSessionDataDAOImpl;

import dto.candidate.CandidateDTO;

import model.user.User;
import service.ExaminerActionsService;
import service.ExaminerSessionContextService;
import util.AuditChangeDetails;

import service.AuditLogService;

import jakarta.servlet.http.HttpSession;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import service.EnumMappingService;
import service.ExaminerDataService;

public class ExaminerActionsServiceImpl implements ExaminerActionsService {
    private final service.AuditLogService auditLogService = new service.impl.AuditLogServiceImpl();

    private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final CandidateCallDAO callDAO = new CandidateCallDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamCandidateVehicleDAO vehicleDAO = new ExamCandidateVehicleDAOImpl();
    private final ExaminerSessionDataDAO sessionDataDAO = new ExaminerSessionDataDAOImpl();
    private final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();

    @Override
    public CandidateDTO findCandidate(int sessionId, String sbd) {
        return viewDataService.findRegistration(sessionId, sbd);
    }

    @Override
    public boolean updateCandidateProfile(int sessionId, String sbd, String fullName, String dobStr,
            String govIdNo, String email, String phoneNo, String address, String sex, String reasonForTaking,
            HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
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
        boolean updated = candidateDAO.updateExaminerProfile(
                reg.getId(), fullName.trim(), dob, govIdNo.trim(),
                email != null ? email.trim() : null,
                phoneNo != null ? phoneNo.trim() : null,
                address != null ? address.trim() : null,
                sexDb,
                reasonForTaking != null ? reasonForTaking.trim() : null);
        if (updated && session != null && !changes.isEmpty()) {
            auditLogService.persistFieldChanges(session, "UPDATE on Profile",
                    "Giám khảo cập nhật thông tin SBD " + reg.getSbd(), changes, null, reg.getId());
        }
        return updated;
    }

    @Override
    public boolean markAbsent(int sessionId, String sbd, HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        candidateDAO.updateScores(reg.getId(), 0, "failed", 0, "failed");
        boolean updated = candidateDAO.markAbsent(reg.getId());
        if (updated && session != null) {
            auditLogService.persist(session, "UPDATE ExamRegistration",
                    "Giám khảo xác nhận vắng thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public boolean undoAbsent(int sessionId, String sbd, HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        boolean updated = candidateDAO.clearAbsentMarking(reg.getId());
        if (updated && session != null) {
            auditLogService.persist(session, "UPDATE ExamRegistration",
                    "Giám khảo hoàn tác vắng thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public boolean callCandidate(int sessionId, String sbd, User user, HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        SectionType sectionType = resolveSectionType(session);
        String sectionName = resolveSectionName(session);
        if (!viewDataService.isCallEligible(sessionId, reg, sectionType, sectionName)) {
            return false;
        }
        return insertCall(sessionId, reg, user, session);
    }

    @Override
    public String callNextCandidate(int sessionId, User user, HttpSession session) {
        SectionType sectionType = resolveSectionType(session);
        String sectionName = resolveSectionName(session);
        List<CandidateDTO> all = candidateDAO.getCandidatesBySession(sessionId);
        for (CandidateDTO reg : all) {
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
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        SectionType sectionType = resolveSectionType(session);
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
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null) {
            return null;
        }
        String nextSbd = ExaminerScoreEntryQueue.moveToBottom(session, sessionId, reg.getSbd());
        if (session != null) {
            auditLogService.persist(session, "UPDATE ScoreEntryQueue",
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
            auditLogService.persist(session, "UPDATE ExamDevice",
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
            auditLogService.persist(session, "UPDATE ExamDevice",
                    "Giám khảo chuyển thiết bị #" + deviceId + " sang sử dụng", deviceId);
        }
        return updated;
    }

    @Override
    public boolean changeCandidateVehicle(int sessionId, String sbd, int deviceId, HttpSession session) {
        if (sessionId <= 0 || sbd == null || sbd.isBlank() || deviceId <= 0) {
            return false;
        }
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null) {
            return false;
        }
        if (!isDeviceInSession(sessionId, deviceId)) {
            return false;
        }
        boolean updated = vehicleDAO.assignExamDevice(reg.getId(), sessionId, deviceId);
        if (updated && session != null) {
            auditLogService.persist(session, "UPDATE Exam_Candidate",
                    "Giám khảo đổi xe thi cho SBD " + reg.getSbd() + " sang thiết bị #" + deviceId,
                    reg.getId());
        }
        return updated;
    }

    private boolean isDeviceInSession(int sessionId, int deviceId) {
        for (java.util.Map<String, Object> device : sessionDataDAO.findDevicesBySessionId(sessionId)) {
            Object idObj = device.get("id");
            if (idObj instanceof Number && ((Number) idObj).intValue() == deviceId) {
                return true;
            }
        }
        return false;
    }

    private static SectionType resolveSectionType(HttpSession session) {
        if (session == null) {
            return SectionType.THEORY;
        }
        Object value = session.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof SectionType) {
            return (SectionType) value;
        }
        return SectionType.THEORY;
    }

    private static String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            return ((ExaminerSlotDTO) slotObj).getExamTypeName();
        }
        Object name = session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }

    private boolean insertCall(int sessionId, CandidateDTO reg, User user, HttpSession session) {
        model.candidate.CandidateCall call = new model.candidate.CandidateCall();
        call.setExamSessionId(sessionId);
        call.setCandidateNo(reg.getCandidateNo());
        call.setCalledTo("Phòng thi lý thuyết");
        call.setCalledBy(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        call.setResult("Calling");
        boolean inserted = callDAO.insert(call);
        if (inserted && session != null) {
            auditLogService.persist(session, "INSERT on CandidateCall",
                    "Giám khảo gọi thí sinh SBD " + reg.getSbd(), reg.getId());
        }
        return inserted;
    }

    private boolean insertScoreEntryCall(int sessionId, CandidateDTO reg, User user, HttpSession session) {
        model.candidate.CandidateCall call = new model.candidate.CandidateCall();
        call.setExamSessionId(sessionId);
        call.setCandidateNo(reg.getCandidateNo());
        call.setCalledTo(resolveScoreEntryCallDestination(session));
        call.setCalledBy(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        call.setResult("Calling");
        boolean inserted = callDAO.insert(call);
        if (inserted && session != null) {
            auditLogService.persist(session, "INSERT on CandidateCall",
                    "Giám khảo gọi thí sinh nhập điểm SBD " + reg.getSbd(), reg.getId());
        }
        return inserted;
    }

    private static String resolveScoreEntryCallDestination(HttpSession session) {
        if (session == null) {
            return "Khu vực thi thực hành";
        }
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO slot && slot.getAreaName() != null && !slot.getAreaName().isBlank()) {
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
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        int maxScore = viewDataService.theoryMaxQuestions();
        if (newScore < 0 || newScore > maxScore) {
            return false;
        }
        Integer oldScore = reg.getTheoryScore();
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        boolean updated = candidateDAO.updateTheoryCorrectCount(
                reg.getId(), newScore, viewDataService.theoryPassThreshold());
        if (updated && session != null) {
            String passed = newScore >= viewDataService.theoryPassThreshold() ? "passed" : "failed";
            auditLogService.persistFieldChanges(session, "UPDATE ExamScore",
                    "Giám khảo điều chỉnh điểm LT SBD " + reg.getSbd(),
                    List.of(new AuditChangeDetails.FieldChange(
                            "Điểm lý thuyết",
                            oldScore != null ? String.valueOf(oldScore) : "-",
                            newScore + " (" + passed.toUpperCase() + ")")),
                    auditReason.isBlank() ? null : auditReason,
                    reg.getId());
        }
        return updated;
    }

    @Override
    public boolean logPracticalScoreEditReason(int sessionId, String sbd, String reasonCode,
            String reasonDetail, User user, String password, HttpSession session) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }
        if (user == null || password == null || password.isBlank()
                || !AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash())) {
            return false;
        }
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        if (session != null) {
            auditLogService.persist(session, "UPDATE ExamScore",
                    "Giám khảo xác nhận sửa kết quả thực hành SBD " + reg.getSbd()
                    + (auditReason.isBlank() ? "" : " - Lý do: " + auditReason),
                    reg.getId());
        }
        return true;
    }

    @Override
    public boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,
            String evidencePath, int[] deductionIds, HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || reg.isSuspended()) {
            return false;
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }

        String reasonLabel = enumMappingService.violationLabel(reasonCode);
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, evidencePath);
        boolean hasDeductions = deductionIds != null && deductionIds.length > 0;

        auditLogService.persistWarning(session,
                "Đình chỉ vi phạm SBD " + reg.getSbd() + ": " + auditText, auditText, reg.getId());

        SectionType sectionType = resolveSectionType(session);
        if (sectionType == SectionType.SCORE_BASED && hasDeductions) {
            String sectionName = resolveSectionName(session);
            candidateDAO.applyScoreDeductions(reg.getId(), deductionIds, sectionName);
        }

        return candidateDAO.markSuspended(reg.getId());
    }

    @Override
    public boolean undoSuspension(int sessionId, String sbd, String reasonCode, String reasonDetail,
            HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || !reg.isSuspended()) {
            return false;
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }

        String reasonLabel = enumMappingService.violationLabel(reasonCode);
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, null);
        boolean undone = candidateDAO.undoSuspension(reg.getId());
        if (undone && session != null) {
            auditLogService.persistFieldChanges(session, "UPDATE Candidate",
                    "Hoàn tác đình chỉ SBD " + reg.getSbd(),
                    List.of(new AuditChangeDetails.FieldChange(
                            "Trạng thái", "Đình chỉ", "Hoạt động bình thường")),
                    auditText, reg.getId());
        }
        return undone;
    }

    @Override
    public boolean adjustScoreDeduction(int sessionId, String sbd, int deductionId, int delta, HttpSession session) {
        if (sbd == null || sbd.isBlank() || deductionId <= 0 || delta == 0) {
            return false;
        }
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return false;
        }
        boolean updated = candidateDAO.adjustScoreDeductionOccurrence(
                reg.getId(), sessionId, deductionId, delta);
        if (updated && session != null) {
            String action = delta > 0 ? "cộng" : "trừ";
            auditLogService.persist(session, "UPDATE Score_Deduction",
                    "Giám khảo " + action + " lỗi trừ điểm SBD " + reg.getSbd()
                    + " (mã lỗi #" + deductionId + ", Δ=" + delta + ")",
                    reg.getId());
        }
        return updated;
    }

    @Override
    public boolean finalizeScoreEntry(int sessionId, String sbd, HttpSession session) {
        if (sbd == null || sbd.isBlank()) {
            return false;
        }
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return false;
        }
        String sectionKeyword = resolveSectionName(session);
        boolean updated = candidateDAO.finalizeScoreEntry(reg.getId(), sessionId, sectionKeyword);
        if (updated && session != null) {
            ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, null);
            auditLogService.persist(session, "UPDATE ExamRegistration",
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
    public boolean printSignatureForm(int sessionId, String sbd, HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || !enumMappingService.isCandidateAwaitingSignature(reg.getSectionStatus())) {
            return false;
        }
        boolean updated = candidateDAO.markSignaturePrinted(reg.getId(), sessionId);
        if (updated && session != null) {
            auditLogService.persist(session, "UPDATE ExamRegistration",
                    "Giám khảo in biên bản kết quả thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public String completeCandidateSection(int sessionId, String sbd, HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return "notFound";
        }
        if (!enumMappingService.isCandidateAwaitingSignature(reg.getSectionStatus())) {
            return "notAwaiting";
        }
        if (!reg.isSignaturePrinted()) {
            return "needSignaturePrint";
        }
        boolean completed = candidateDAO.completeSection(reg.getId(), sessionId);
        if (!completed) {
            return "completeFailed";
        }
        if (session != null) {
            auditLogService.persist(session, "UPDATE ExamRegistration",
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
