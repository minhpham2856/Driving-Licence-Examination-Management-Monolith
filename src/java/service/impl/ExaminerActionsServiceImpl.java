package service.impl;

import enums.SectionType;

import controller.examiner.ExaminerScoreEntryQueue;

import dto.examiner.ExaminerSlotDTO;

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

import dto.candidate.CandidateEnrollmentDTO;

import model.user.User;
import service.ExaminerActionsService;
import util.AuditChangeDetails;

import service.AuditLogService;


import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import service.EnumMappingService;
import service.ExaminerDataService;

public class ExaminerActionsServiceImpl implements ExaminerActionsService {
    private final service.AuditLogService auditLogService = new service.impl.AuditLogServiceImpl();

    private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();

    private final AuditDAO auditDAO = new AuditDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamEnrollmentDAO vehicleDAO = new ExamEnrollmentDAOImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();

    @Override
    public CandidateEnrollmentDTO findCandidate(int sessionId, String sbd) {
        return viewDataService.findRegistration(sessionId, sbd);
    }

    @Override
    public boolean updateCandidateProfile(int sessionId, String sbd, String fullName, String dobStr,
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
        String sexDb;
        if ("Nu".equalsIgnoreCase(sex) || "Ná»¯".equalsIgnoreCase(sex) || "1".equals(sex)) {
            sexDb = "Ná»¯";
        } else {
            sexDb = "Nam";
        }
        java.text.SimpleDateFormat dobFmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
        List<AuditChangeDetails.FieldChange> changes = new ArrayList<>();
        AuditChangeDetails.addIfChanged(changes, "Há» tÃªn", reg.getFullName(), fullName.trim());
        AuditChangeDetails.addIfChanged(changes, "NgÃ y sinh",
                reg.getDateOfBirth() != null ? dobFmt.format(reg.getDateOfBirth()) : null,
                dobFmt.format(dob));
        AuditChangeDetails.addIfChanged(changes, "CCCD", reg.getGovIdNo(), govIdNo.trim());
        AuditChangeDetails.addIfChanged(changes, "Email", reg.getEmail(), email != null ? email.trim() : null);
        AuditChangeDetails.addIfChanged(changes, "SÄT", reg.getPhoneNo(), phoneNo != null ? phoneNo.trim() : null);
        AuditChangeDetails.addIfChanged(changes, "Äá»‹a chá»‰", reg.getAddress(), address != null ? address.trim() : null);
        AuditChangeDetails.addIfChanged(changes, "Giá»›i tÃ­nh", reg.isSex() ? "Ná»¯" : "Nam", sexDb);
        AuditChangeDetails.addIfChanged(changes, "LÃ½ do thi", reg.getReasonForTaking(),
                reasonForTaking != null ? reasonForTaking.trim() : null);
        boolean updated = candidateDAO.updateExaminerProfile(
                reg.getId(), fullName.trim(), dob, govIdNo.trim(),
                email != null ? email.trim() : null,
                phoneNo != null ? phoneNo.trim() : null,
                address != null ? address.trim() : null,
                sexDb,
                reasonForTaking != null ? reasonForTaking.trim() : null);
        if (updated && actionUserId != null && !changes.isEmpty()) {
            auditLogService.persistFieldChanges(actionUserId, "UPDATE on Profile",
                    "GiÃ¡m kháº£o cáº­p nháº­t thÃ´ng tin SBD " + reg.getSbd(), changes, null, reg.getId());
        }
        return updated;
    }

    @Override
    public boolean markAbsent(int sessionId, String sbd, Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        candidateDAO.updateScores(reg.getId(), 0, "failed", 0, "failed");
        boolean updated = candidateDAO.markAbsent(reg.getId());
        if (updated && actionUserId != null) {
            auditLogService.persist(actionUserId, "UPDATE ExamRegistration",
                    "GiÃ¡m kháº£o xÃ¡c nháº­n váº¯ng thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public boolean undoAbsent(int sessionId, String sbd, Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        boolean updated = candidateDAO.clearAbsentMarking(reg.getId());
        if (updated && actionUserId != null) {
            auditLogService.persist(actionUserId, "UPDATE ExamRegistration",
                    "GiÃ¡m kháº£o hoÃ n tÃ¡c váº¯ng thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public boolean callCandidate(int sessionId, String sbd, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
                        if (!viewDataService.isCallEligible(sessionId, reg, sectionType, sectionName)) {
            return false;
        }
        return insertCall(sessionId, reg, user, actionUserId, callDestination);
    }

    @Override
    public String callNextCandidate(int sessionId, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {
                        List<CandidateEnrollmentDTO> all = candidateDAO.getCandidatesBySession(sessionId);
        for (CandidateEnrollmentDTO reg : all) {
            if (!viewDataService.isCallEligible(sessionId, reg, sectionType, sectionName)) {
                continue;
            }
            if (insertCall(sessionId, reg, user, actionUserId, callDestination)) {
                return reg.getSbd();
            }
        }
        return null;
    }

    @Override
    public int callSelectedCandidates(int sessionId, String[] sbds, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {
        if (sbds == null || sbds.length == 0) {
            return 0;
        }
        int count = 0;
        for (String sbd : sbds) {
            if (sbd == null || sbd.isBlank()) {
                continue;
            }
            if (callCandidate(sessionId, sbd.trim(), user, actionUserId, sectionType, sectionName, callDestination)) {
                count++;
            }
        }
        return count;
    }


    @Override
    public boolean callScoreEntryCandidate(int sessionId, String sbd, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {
        if (sbd == null || sbd.isBlank()) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd.trim());
                        if (!viewDataService.isScoreQueueEligible(sessionId, reg, sectionType, sectionName)) {
            return false;
        }
        
        
        return insertScoreEntryCall(sessionId, reg, user, actionUserId, callDestination);
    }

    @Override
    public boolean setDeviceMaintenance(int deviceId, Integer actionUserId) {
        if (deviceId <= 0) {
            return false;
        }
        boolean updated = deviceDAO.updateStatus(deviceId, "Maintenance");
        if (updated && actionUserId != null) {
            auditLogService.persist(actionUserId, "UPDATE ExamDevice",
                    "GiÃ¡m kháº£o chuyá»ƒn thiáº¿t bá»‹ #" + deviceId + " sang báº£o trÃ¬", deviceId);
        }
        return updated;
    }

    @Override
    public boolean setDeviceAvailable(int deviceId, Integer actionUserId) {
        if (deviceId <= 0) {
            return false;
        }
        boolean updated = deviceDAO.updateStatus(deviceId, "Available");
        if (updated && actionUserId != null) {
            auditLogService.persist(actionUserId, "UPDATE ExamDevice",
                    "GiÃ¡m kháº£o chuyá»ƒn thiáº¿t bá»‹ #" + deviceId + " sang sá»­ dá»¥ng", deviceId);
        }
        return updated;
    }

    @Override
    public boolean changeCandidateVehicle(int sessionId, String sbd, int deviceId, Integer actionUserId) {
        if (sessionId <= 0 || sbd == null || sbd.isBlank() || deviceId <= 0) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null) {
            return false;
        }
        if (!isDeviceInSession(sessionId, deviceId)) {
            return false;
        }
        boolean updated = vehicleDAO.assignExamDevice(reg.getId(), sessionId, deviceId);
        if (updated && actionUserId != null) {
            auditLogService.persist(actionUserId, "UPDATE Exam_Candidate",
                    "GiÃ¡m kháº£o Ä‘á»•i xe thi cho SBD " + reg.getSbd() + " sang thiáº¿t bá»‹ #" + deviceId,
                    reg.getId());
        }
        return updated;
    }

    private boolean isDeviceInSession(int sessionId, int deviceId) {
        List<Integer> areaIds = sessionDAO.getExamAreaIds(sessionId);
        for (model.exam.ExamDevice device : deviceDAO.findByAreaIds(areaIds)) {
            if (device.getExamDeviceId() == deviceId) {
                return true;
            }
        }
        return false;
    }
    private boolean insertCall(int sessionId, CandidateEnrollmentDTO reg, User user, Integer actionUserId, String callDestination) {
        model.user.Audit audit = new model.user.Audit();
        audit.setUserId(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        audit.setAction("CALL");
        String entityId = sessionId + "-" + reg.getCandidateNo();
        String detail = "calledTo=PhÃ²ng thi lÃ½ thuyáº¿t;result=Calling";
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        boolean inserted = auditDAO.insert(audit);
        if (inserted && actionUserId != null) {
            auditLogService.persist(actionUserId, "INSERT on CandidateCall",
                    "GiÃ¡m kháº£o gá»i thÃ­ sinh SBD " + reg.getSbd(), reg.getId());
        }
        return inserted;
    }

    private boolean insertScoreEntryCall(int sessionId, CandidateEnrollmentDTO reg, User user, Integer actionUserId, String callDestination) {
        model.user.Audit audit = new model.user.Audit();
        audit.setUserId(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        audit.setAction("CALL");
        String entityId = sessionId + "-" + reg.getCandidateNo();
        String detail = "calledTo=" + callDestination + ";result=Calling";
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        boolean inserted = auditDAO.insert(audit);
        if (inserted && actionUserId != null) {
            auditLogService.persist(actionUserId, "INSERT on CandidateCall",
                    "GiÃ¡m kháº£o gá» i thÃ­ sinh nháº­p Ä‘iá»ƒm SBD " + reg.getSbd(), reg.getId());
        }
        return inserted;
    }

    @Override
    public boolean updateTheoryScore(int sessionId, String sbd, int newScore, String reasonCode,
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
        boolean updated = candidateDAO.updateTheoryCorrectCount(
                reg.getId(), newScore, viewDataService.theoryPassThreshold());
        if (updated && actionUserId != null) {
            String passed = newScore >= viewDataService.theoryPassThreshold() ? "passed" : "failed";
            auditLogService.persistFieldChanges(actionUserId, "UPDATE ExamScore",
                    "GiÃ¡m kháº£o Ä‘iá»u chá»‰nh Ä‘iá»ƒm LT SBD " + reg.getSbd(),
                    List.of(new AuditChangeDetails.FieldChange(
                            "Äiá»ƒm lÃ½ thuyáº¿t",
                            oldScore != null ? String.valueOf(oldScore) : "-",
                            newScore + " (" + passed.toUpperCase() + ")")),
                    auditReason.isBlank() ? null : auditReason,
                    reg.getId());
        }
        return updated;
    }

    @Override
    public boolean logPracticalScoreEditReason(int sessionId, String sbd, String reasonCode,
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
            auditLogService.persist(actionUserId, "UPDATE ExamScore",
                    "GiÃ¡m kháº£o xÃ¡c nháº­n sá»­a káº¿t quáº£ thá»±c hÃ nh SBD " + reg.getSbd()
                    + (auditReason.isBlank() ? "" : " - LÃ½ do: " + auditReason),
                    reg.getId());
        }
        return true;
    }

    @Override
    public boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,
            String evidencePath, int[] deductionIds, Integer actionUserId, enums.SectionType sectionType, String sectionName) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
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

        auditLogService.persistWarning(actionUserId,
                "ÄÃ¬nh chá»‰ vi pháº¡m SBD " + reg.getSbd() + ": " + auditText, auditText, reg.getId());

                if (sectionType == SectionType.SCORE_BASED && hasDeductions) {
                        // applyScoreDeductions removed
        }

        model.candidate.Candidate c = candidateDAO.findById(reg.getId());
        if (c != null) {
            c.setSuspended(true);
            return candidateDAO.update(c);
        }
        return false;
    }

    @Override
    public boolean undoSuspension(int sessionId, String sbd, String reasonCode, String reasonDetail,
            Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || !reg.isSuspended()) {
            return false;
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }

        String reasonLabel = enumMappingService.violationLabel(reasonCode);
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, null);
        model.candidate.Candidate c = candidateDAO.findById(reg.getId());
        boolean undone = false;
        if (c != null) {
            c.setSuspended(false);
            undone = candidateDAO.update(c);
        }
        if (undone && actionUserId != null) {
            auditLogService.persistFieldChanges(actionUserId, "UPDATE Candidate",
                    "HoÃ n tÃ¡c Ä‘Ã¬nh chá»‰ SBD " + reg.getSbd(),
                    List.of(new AuditChangeDetails.FieldChange(
                            "Tráº¡ng thÃ¡i", "ÄÃ¬nh chá»‰", "Hoáº¡t Ä‘á»™ng bÃ¬nh thÆ°á»ng")),
                    auditText, reg.getId());
        }
        return undone;
    }

    @Override
    public boolean adjustScoreDeduction(int sessionId, String sbd, int deductionId, int delta, Integer actionUserId) {
        if (sbd == null || sbd.isBlank() || deductionId <= 0 || delta == 0) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return false;
        }
        boolean updated = candidateDAO.adjustScoreDeductionOccurrence(
                reg.getId(), sessionId, deductionId, delta);
        if (updated && actionUserId != null) {
            String action = delta > 0 ? "cá»™ng" : "trá»«";
            auditLogService.persist(actionUserId, "UPDATE Score_Deduction",
                    "GiÃ¡m kháº£o " + action + " lá»—i trá»« Ä‘iá»ƒm SBD " + reg.getSbd()
                    + " (mÃ£ lá»—i #" + deductionId + ", Î”=" + delta + ")",
                    reg.getId());
        }
        return updated;
    }

    @Override
    public boolean finalizeScoreEntry(int sessionId, String sbd, Integer actionUserId, String sectionKeyword) {
        if (sbd == null || sbd.isBlank()) {
            return false;
        }
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return false;
        }
                model.exam.ExamEnrollment e = enrollmentDAO.findBySessionAndCandidate(sessionId, reg.getId());
        boolean updated = false;
        if (e != null) {
            e.setSectionStatus("AwaitingSignature");
            updated = enrollmentDAO.update(e);
        }
        if (updated && actionUserId != null) {
            
            auditLogService.persist(actionUserId, "UPDATE ExamRegistration",
                    "GiÃ¡m kháº£o hoÃ n táº¥t nháº­p Ä‘iá»ƒm SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return user != null && password != null && !password.isBlank()
                && AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash());
    }

    @Override
    public boolean printSignatureForm(int sessionId, String sbd, Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || !enumMappingService.isCandidateAwaitingSignature(reg.getSectionStatus())) {
            return false;
        }
        model.exam.ExamEnrollment e = enrollmentDAO.findBySessionAndCandidate(sessionId, reg.getId());
        boolean updated = false;
        if (e != null) {
            e.setSignaturePrinted(true);
            updated = enrollmentDAO.update(e);
        }
        if (updated && actionUserId != null) {
            auditLogService.persist(actionUserId, "UPDATE ExamRegistration",
                    "GiÃ¡m kháº£o in biÃªn báº£n káº¿t quáº£ thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    @Override
    public String completeCandidateSection(int sessionId, String sbd, Integer actionUserId) {
        CandidateEnrollmentDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return "notFound";
        }
        if (!enumMappingService.isCandidateAwaitingSignature(reg.getSectionStatus())) {
            return "notAwaiting";
        }
        if (!reg.isSignaturePrinted()) {
            return "needSignaturePrint";
        }
        model.exam.ExamEnrollment e = enrollmentDAO.findBySessionAndCandidate(sessionId, reg.getId());
        boolean completed = false;
        if (e != null) {
            e.setSectionStatus("Done");
            completed = enrollmentDAO.update(e);
        }
        if (!completed) {
            return "completeFailed";
        }
        if (actionUserId != null) {
            auditLogService.persist(actionUserId, "UPDATE ExamRegistration",
                    "GiÃ¡m kháº£o hoÃ n táº¥t pháº§n thi SBD " + reg.getSbd(), reg.getId());
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
            text.append("Minh chá»©ng: ").append(evidencePath);
        }
        return text.toString();
    }

    private static String buildReasonText(String reasonCode, String reasonDetail) {
        String label = switch (reasonCode != null ? reasonCode : "") {
            case "cham-sai" ->
                "Cháº¥m sai";
            case "nhap-nham" ->
                "Nháº­p nháº§m Ä‘iá»ƒm";
            case "khieu-nai" ->
                "ThÃ­ sinh khiáº¿u náº¡i";
            case "khac" ->
                "LÃ½ do khÃ¡c";
            default ->
                "";
        };
        if (reasonDetail != null && !reasonDetail.isBlank()) {
            return label.isBlank() ? reasonDetail.trim() : label + ": " + reasonDetail.trim();
        }
        return label;
    }
}


