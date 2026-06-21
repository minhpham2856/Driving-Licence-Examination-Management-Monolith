package Services.Impl;

import Utils.ExamConstants;
import Utils.ExamConstants.SectionType;
import Controllers.Examiner.ExaminerScoreEntryQueue;
import Controllers.Staff.ExamStaff.ExaminerSlot;
import DAOs.CandidateCallDAO;
import DAOs.ExamCandidateVehicleDAO;
import DAOs.ExamDeviceDAO;
import DAOs.CandidateDAO;
import DAOs.ExaminerSessionDataDAO;
import DAOs.Impl.CandidateCallDAOImpl;
import DAOs.Impl.ExamCandidateVehicleDAOImpl;
import DAOs.Impl.ExamDeviceDAOImpl;
import DAOs.Impl.CandidateDAOImpl;
import DAOs.Impl.ExaminerSessionDataDAOImpl;
import DTOs.CandidateCallDTO;
import DTOs.CandidateDTO;
import Models.User;
import Services.ExaminerService;
import Services.ExaminerSessionContextService;
import Services.ExaminerViewDataService;
import Utils.AuditChangeDetails;
import Utils.AuditLogHelper;
import jakarta.servlet.http.HttpSession;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

// Implementation of ExaminerCrudService handling candidate lifecycle and interactions
public class ExaminerServiceImpl implements ExaminerService {

    // DAOs required for database operations
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final CandidateCallDAO callDAO = new CandidateCallDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamCandidateVehicleDAO vehicleDAO = new ExamCandidateVehicleDAOImpl();
    private final ExaminerSessionDataDAO sessionDataDAO = new ExaminerSessionDataDAOImpl();
    // Service to load view data and resolve eligibility
    private final ExaminerViewDataService viewDataService = new ExaminerViewDataServiceImpl();

    // Looks up a candidate by session and SBD via the view-data service.
    @Override
    public CandidateDTO findCandidate(int sessionId, String sbd) {
        // Delegate to viewDataService to find the registration
        return viewDataService.findRegistration(sessionId, sbd);
    }

    // Updates a candidate's profile fields (name, DOB, government ID, contact info, gender, reason).
    @Override
    public boolean updateCandidateProfile(int sessionId, String sbd, String fullName, String dobStr,
            String govIdNo, String email, String phoneNo, String address, String sex, String reasonForTaking,
            HttpSession session) {
        // Look up the existing candidate to compare changes
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        // Parse the provided date string to a SQL Date object
        Date dob = parseDate(dobStr);
        if (dob == null) {
            return false;
        }
        // Normalize the gender string to Vietnamese standards
        String sexDb;
        if ("Nu".equalsIgnoreCase(sex) || "Nữ".equalsIgnoreCase(sex) || "1".equals(sex)) {
            sexDb = "Nữ";
        } else {
            sexDb = "Nam";
        }
        // Date formatter for comparing old and new dates
        java.text.SimpleDateFormat dobFmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
        // List to hold the field changes for the audit log
        List<AuditChangeDetails.FieldChange> changes = new ArrayList<>();
        // Compare each field and record if changed
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
        
        // Execute the update query
        boolean updated = candidateDAO.updateExaminerProfile(
                reg.getId(), fullName.trim(), dob, govIdNo.trim(),
                email != null ? email.trim() : null,
                phoneNo != null ? phoneNo.trim() : null,
                address != null ? address.trim() : null,
                sexDb,
                reasonForTaking != null ? reasonForTaking.trim() : null);
        
        // Persist the audit log if the update succeeded and changes were made
        if (updated && session != null && !changes.isEmpty()) {
            AuditLogHelper.persistFieldChanges(session, "UPDATE on Profile",
                    "sát hạch viên cập nhật thông tin SBD " + reg.getSbd(), changes, null, reg.getId());
        }
        return updated;
    }

    // Marks a candidate as absent for the current exam section.
    @Override
    public boolean markAbsent(int sessionId, String sbd, HttpSession session) {
        // Find the candidate
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        // Fail the candidate by setting scores to 0
        candidateDAO.updateScores(reg.getId(), 0, "failed", 0, "failed");
        // Mark the absence flag in the database
        boolean updated = candidateDAO.markAbsent(reg.getId());
        // Record the audit log
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "sát hạch viên xác nhận vắng thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    // Marks a candidate as absent within a score-entry context and returns the next candidate's SBD for seamless auto-advance.
    @Override
    public String markAbsentInScoreEntry(int sessionId, String sbd, HttpSession session) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        // Determine the next candidate in the queue *before* modifying the current one
        String nextSbd = ExaminerScoreEntryQueue.nextInQueueAfter(session, sessionId, sbd.trim());

        // Find the current candidate
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null) {
            return nextSbd;
        }
        // Fail their scores
        candidateDAO.updateScores(reg.getId(), 0, "failed", 0, "failed");
        // Mark them as absent
        candidateDAO.markAbsent(reg.getId());

        // Clear the active SBD from the session queue state so it auto-advances
        ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, null);

        // Record the audit log
        if (session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "sát hạch viên đánh vắng SBD " + reg.getSbd() + " (nhập điểm)", reg.getId());
        }
        return nextSbd;
    }

    // Reverses a previous absence marking, restoring the candidate's active status.
    @Override
    public boolean undoAbsent(int sessionId, String sbd, HttpSession session) {
        // Find the candidate
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        // Clear the absent flag in the database
        boolean updated = candidateDAO.clearAbsentMarking(reg.getId());
        // Record the audit log
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "sát hạch viên hoàn tác vắng thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    // Calls a specific candidate for their exam section.
    @Override
    public boolean callCandidate(int sessionId, String sbd, User user, HttpSession session) {
        // Find the candidate
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        // Determine the section type and name for the caller
        SectionType sectionType = resolveSectionType(session);
        String sectionName = resolveSectionName(session);
        // Ensure the candidate is eligible to be called
        if (!viewDataService.isCallEligible(sessionId, reg, sectionType, sectionName)) {
            return false;
        }
        // Insert the call record into the database
        return insertCall(sessionId, reg, user, session);
    }

    // Calls the next eligible candidate automatically.
    @Override
    public String callNextCandidate(int sessionId, User user, HttpSession session) {
        // Resolve the caller's section type and name
        SectionType sectionType = resolveSectionType(session);
        String sectionName = resolveSectionName(session);
        // Get all candidates
        List<CandidateDTO> all = candidateDAO.getCandidatesBySession(sessionId);
        // Find the first eligible candidate
        for (CandidateDTO reg : all) {
            if (!viewDataService.isCallEligible(sessionId, reg, sectionType, sectionName)) {
                continue;
            }
            // Execute the call and return their SBD
            if (insertCall(sessionId, reg, user, session)) {
                return reg.getSbd();
            }
        }
        // No candidates eligible
        return null;
    }

    // Calls multiple selected candidates in batch.
    @Override
    public int callSelectedCandidates(int sessionId, String[] sbds, User user, HttpSession session) {
        // Return 0 if the array is null or empty
        if (sbds == null || sbds.length == 0) {
            return 0;
        }
        int count = 0;
        // Call each candidate and increment the success counter
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

    // Automatically re-establishes the active score-entry candidate from session state.
    @Override
    public String autoCallScoreEntryIfNeeded(int sessionId, User user, HttpSession session) {
        if (session == null) {
            return null;
        }
        // Check if there is an active candidate
        String active = ExaminerScoreEntryQueue.getActiveSbd(session, sessionId);
        String called = ExaminerScoreEntryQueue.getCalledSbd(session, sessionId);
        if (active != null && !active.isBlank()) {
            return active;
        }
        // Check if there is a called candidate that isn't active
        if (called != null && !called.isBlank()) {
            ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, called);
            return called;
        }
        // Check if the queue has candidates
        String first = ExaminerScoreEntryQueue.firstInQueue(session, sessionId);
        if (first == null || first.isBlank()) {
            return null;
        }
        // Call the first candidate in the queue
        if (callScoreEntryCandidate(sessionId, first, user, session)) {
            return first;
        }
        return null;
    }

    // Calls a candidate specifically for score entry and records the call in the session queue.
    @Override
    public boolean callScoreEntryCandidate(int sessionId, String sbd, User user, HttpSession session) {
        if (sbd == null || sbd.isBlank()) {
            return false;
        }
        // Find the candidate
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        SectionType sectionType = resolveSectionType(session);
        String sectionName = resolveSectionName(session);
        // Verify score entry queue eligibility
        if (!viewDataService.isScoreQueueEligible(sessionId, reg, sectionType, sectionName)) {
            return false;
        }
        // Update session queue states
        ExaminerScoreEntryQueue.setCalledSbd(session, sessionId, reg.getSbd());
        ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, reg.getSbd());
        // Insert the call record
        return insertScoreEntryCall(sessionId, reg, user, session);
    }

    // Defers an absent candidate by pushing them to the bottom of the score-entry queue and calling the next available candidate.
    @Override
    public String deferScoreEntryAbsent(int sessionId, String sbd, User user, HttpSession session) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null) {
            return null;
        }
        // Move the candidate to the bottom of the queue
        String nextSbd = ExaminerScoreEntryQueue.moveToBottom(session, sessionId, reg.getSbd());
        // Record the deferral in the audit log
        if (session != null) {
            AuditLogHelper.persist(session, "UPDATE ScoreEntryQueue",
                    "sát hạch viên đẩy xuống cuối hàng đợi nhập điểm (chưa vắng) SBD " + reg.getSbd(),
                    reg.getId());
        }
        // Auto-call the next candidate if one exists
        if (nextSbd == null || nextSbd.isBlank()) {
            return null;
        }
        if (callScoreEntryCandidate(sessionId, nextSbd, user, session)) {
            return nextSbd;
        }
        ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, nextSbd);
        return nextSbd;
    }

    // Sets an exam device to maintenance mode.
    @Override
    public boolean setDeviceMaintenance(int deviceId, HttpSession session) {
        if (deviceId <= 0) {
            return false;
        }
        // Update status to Maintenance in the database
        boolean updated = deviceDAO.updateStatus(deviceId, "Maintenance");
        // Log the change
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamDevice",
                    "sát hạch viên chuyển thiết bị #" + deviceId + " sang bảo trì", deviceId);
        }
        return updated;
    }

    // Sets an exam device back to available (operational) mode.
    @Override
    public boolean setDeviceAvailable(int deviceId, HttpSession session) {
        if (deviceId <= 0) {
            return false;
        }
        // Update status to Available in the database
        boolean updated = deviceDAO.updateStatus(deviceId, "Available");
        // Log the change
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamDevice",
                    "sát hạch viên chuyển thiết bị #" + deviceId + " sang sử dụng", deviceId);
        }
        return updated;
    }

    // Changes the vehicle (exam device) assigned to a candidate.
    @Override
    public boolean changeCandidateVehicle(int sessionId, String sbd, int deviceId, HttpSession session) {
        if (sessionId <= 0 || sbd == null || sbd.isBlank() || deviceId <= 0) {
            return false;
        }
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null) {
            return false;
        }
        // Ensure the device belongs to the current session
        if (!isDeviceInSession(sessionId, deviceId)) {
            return false;
        }
        // Update the candidate's assigned vehicle in the database
        boolean updated = vehicleDAO.assignExamDevice(reg.getId(), sessionId, deviceId);
        // Log the assignment
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE Exam_Candidate",
                    "sát hạch viên đổi xe thi cho SBD " + reg.getSbd() + " sang thiết bị #" + deviceId,
                    reg.getId());
        }
        return updated;
    }

    // Checks whether a device ID belongs to any device in the given session.
    private boolean isDeviceInSession(int sessionId, int deviceId) {
        // Iterate through all devices linked to the session
        for (java.util.Map<String, Object> device : sessionDataDAO.findDevicesBySessionId(sessionId)) {
            Object idObj = device.get("id");
            if (idObj instanceof Number && ((Number) idObj).intValue() == deviceId) {
                return true;
            }
        }
        return false;
    }

    // Resolves the section type from the session's cached context.
    private static SectionType resolveSectionType(HttpSession session) {
        if (session == null) {
            return SectionType.THEORY;
        }
        // Extract section type enum from session attribute
        Object value = session.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof SectionType) {
            return (SectionType) value;
        }
        return SectionType.THEORY;
    }

    // Resolves the section display name from the session's cached slot.
    private static String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        // Attempt to extract from ExaminerSlot
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlot) {
            return ((ExaminerSlot) slotObj).getExamTypeName();
        }
        // Fallback to direct attribute
        Object name = session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }

    // Inserts a standard candidate call record and writes an audit log.
    private boolean insertCall(int sessionId, CandidateDTO reg, User user, HttpSession session) {
        CandidateCallDTO call = new CandidateCallDTO();
        call.setExamSessionId(sessionId);
        call.setCandidateNo(reg.getCandidateNo());
        // Default call destination for standard calls
        call.setCalledTo("Phòng thi lý thuyết");
        call.setCalledBy(user != null && user.getId() > 0 ? user.getId() : 0);
        call.setResult("Calling");
        // Insert record
        boolean inserted = callDAO.insert(call);
        // Log the call
        if (inserted && session != null) {
            AuditLogHelper.persist(session, "INSERT on CandidateCall",
                    "sát hạch viên gọi thí sinh SBD " + reg.getSbd(), reg.getId());
        }
        return inserted;
    }

    // Inserts a score-entry candidate call record and writes an audit log.
    private boolean insertScoreEntryCall(int sessionId, CandidateDTO reg, User user, HttpSession session) {
        CandidateCallDTO call = new CandidateCallDTO();
        call.setExamSessionId(sessionId);
        call.setCandidateNo(reg.getCandidateNo());
        // Resolve dynamic destination for score-entry calls
        call.setCalledTo(resolveScoreEntryCallDestination(session));
        call.setCalledBy(user != null && user.getId() > 0 ? user.getId() : 0);
        call.setResult("Calling");
        // Insert record
        boolean inserted = callDAO.insert(call);
        // Log the call
        if (inserted && session != null) {
            AuditLogHelper.persist(session, "INSERT on CandidateCall",
                    "sát hạch viên gọi thí sinh nhập điểm SBD " + reg.getSbd(), reg.getId());
        }
        return inserted;
    }

    // Resolves the destination string for score-entry call records.
    private static String resolveScoreEntryCallDestination(HttpSession session) {
        if (session == null) {
            return "Khu vực thi thực hành";
        }
        // Try getting the area name from the slot
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlot) {
            ExaminerSlot slot = (ExaminerSlot) slotObj;
            if (slot.getAreaName() != null && !slot.getAreaName().isBlank()) {
                return slot.getAreaName();
            }
        }
        // Fallback to section name
        Object sectionName = session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        if (sectionName != null && !String.valueOf(sectionName).isBlank()) {
            return String.valueOf(sectionName);
        }
        return "Khu vực thi thực hành";
    }

    // Updates a candidate's theory score with password-protected authorisation.
    @Override
    public boolean updateTheoryScore(int sessionId, String sbd, int newScore, String reasonCode,
            String reasonDetail, User user, String password, HttpSession session) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }
        // Verify the examiner's password
        if (user == null || password == null || password.isBlank()
                || !AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash())) {
            return false;
        }
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        // Ensure the score is within valid bounds
        int maxScore = viewDataService.theoryMaxQuestions();
        if (newScore < 0 || newScore > maxScore) {
            return false;
        }
        Integer oldScore = reg.getTheoryScore();
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        // Update the theory score in the database
        boolean updated = candidateDAO.updateTheoryCorrectCount(
                reg.getId(), newScore, viewDataService.theoryPassThreshold());
        // Record the field change in the audit log
        if (updated && session != null) {
            String passed = newScore >= viewDataService.theoryPassThreshold() ? "passed" : "failed";
            AuditLogHelper.persistFieldChanges(session, "UPDATE ExamScore",
                    "sát hạch viên điều chỉnh điểm LT SBD " + reg.getSbd(),
                    List.of(new AuditChangeDetails.FieldChange(
                            "Điểm lý thuyết",
                            oldScore != null ? String.valueOf(oldScore) : "-",
                            newScore + " (" + passed.toUpperCase() + ")")),
                    auditReason.isBlank() ? null : auditReason,
                    reg.getId());
        }
        return updated;
    }

    // Logs a practical score edit reason to the audit trail.
    @Override
    public boolean logPracticalScoreEditReason(int sessionId, String sbd, String reasonCode,
            String reasonDetail, User user, String password, HttpSession session) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return false;
        }
        // Verify password
        if (user == null || password == null || password.isBlank()
                || !AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash())) {
            return false;
        }
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return false;
        }
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        // Write the log
        if (session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamScore",
                    "sát hạch viên xác nhận sửa kết quả thực hành SBD " + reg.getSbd()
                    + (auditReason.isBlank() ? "" : " - Lý do: " + auditReason),
                    reg.getId());
        }
        return true;
    }

    // Records a violation against a candidate (suspension + optional score deductions).
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

        // Build the audit text describing the violation
        String reasonLabel = ExamConstants.violationLabel(reasonCode);
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, evidencePath);
        boolean hasDeductions = deductionIds != null && deductionIds.length > 0;

        // Persist the warning to the audit log
        AuditLogHelper.persistWarning(session,
                "Đình chỉ vi phạm SBD " + reg.getSbd() + ": " + auditText, auditText, reg.getId());

        // Apply score deductions if applicable
        SectionType sectionType = resolveSectionType(session);
        if (sectionType == SectionType.SCORE_BASED && hasDeductions) {
            String sectionName = resolveSectionName(session);
            candidateDAO.applyScoreDeductions(reg.getId(), deductionIds, sectionName);
        }

        // Suspend the candidate
        return candidateDAO.markSuspended(reg.getId());
    }

    // Reverses a previous candidate suspension.
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

        String reasonLabel = ExamConstants.violationLabel(reasonCode);
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, null);
        // Execute the undo logic
        boolean undone = candidateDAO.undoSuspension(reg.getId());
        // Record the undo in the audit log
        if (undone && session != null) {
            AuditLogHelper.persistFieldChanges(session, "UPDATE Candidate",
                    "Hoàn tác đình chỉ SBD " + reg.getSbd(),
                    List.of(new AuditChangeDetails.FieldChange(
                            "Trạng thái", "Đình chỉ", "Hoạt động bình thường")),
                    auditText, reg.getId());
        }
        return undone;
    }

    // Adjusts a score-deduction occurrence count (adds or removes one instance).
    @Override
    public boolean adjustScoreDeduction(int sessionId, String sbd, int deductionId, int delta, HttpSession session) {
        if (sbd == null || sbd.isBlank() || deductionId <= 0 || delta == 0) {
            return false;
        }
        CandidateDTO reg = findCandidate(sessionId, sbd.trim());
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return false;
        }
        // Adjust the deduction occurrence
        boolean updated = candidateDAO.adjustScoreDeductionOccurrence(
                reg.getId(), sessionId, deductionId, delta);
        // Log the change
        if (updated && session != null) {
            String action = delta > 0 ? "cộng" : "trừ";
            AuditLogHelper.persist(session, "UPDATE Score_Deduction",
                    "sát hạch viên " + action + " lỗi trừ điểm SBD " + reg.getSbd()
                    + " (mã lỗi #" + deductionId + ", Δ=" + delta + ")",
                    reg.getId());
        }
        return updated;
    }

    // Finalises the score entry for a candidate, moving them to "AwaitingSignature" or "Done" state.
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
        // Compute and finalize scores in the database
        boolean updated = candidateDAO.finalizeScoreEntry(reg.getId(), sessionId, sectionKeyword);
        // Record the event
        if (updated && session != null) {
            ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, null);
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "sát hạch viên hoàn tất nhập điểm SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    // Verifies a user's password against the stored hash.
    @Override
    public boolean verifyPassword(User user, String password) {
        return user != null && password != null && !password.isBlank()
                && AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash());
    }

    // Marks a candidate's signature form (biên bản kết quả thi) as printed.
    @Override
    public boolean printSignatureForm(int sessionId, String sbd, HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null || !ExamConstants.isCandidateAwaitingSignature(reg.getSectionStatus())) {
            return false;
        }
        // Update database
        boolean updated = candidateDAO.markSignaturePrinted(reg.getId(), sessionId);
        // Log action
        if (updated && session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "sát hạch viên in biên bản kết quả thi SBD " + reg.getSbd(), reg.getId());
        }
        return updated;
    }

    // Completes a candidate's exam section after signature has been printed.
    @Override
    public String completeCandidateSection(int sessionId, String sbd, HttpSession session) {
        CandidateDTO reg = findCandidate(sessionId, sbd);
        if (reg == null) {
            return "notFound";
        }
        if (!ExamConstants.isCandidateAwaitingSignature(reg.getSectionStatus())) {
            return "notAwaiting";
        }
        if (!reg.isSignaturePrinted()) {
            return "needSignaturePrint";
        }
        // Transition state to Done
        boolean completed = candidateDAO.completeSection(reg.getId(), sessionId);
        if (!completed) {
            return "completeFailed";
        }
        // Log completion
        if (session != null) {
            AuditLogHelper.persist(session, "UPDATE ExamRegistration",
                    "sát hạch viên hoàn tất phần thi SBD " + reg.getSbd(), reg.getId());
        }
        return null;
    }

    // Parses a date string in dd/MM/yyyy or yyyy-MM-dd format.
    private static Date parseDate(String dobStr) {
        if (dobStr == null || dobStr.isBlank()) {
            return null;
        }
        try {
            // Attempt Vietnamese format
            if (dobStr.contains("/")) {
                String[] parts = dobStr.split("/");
                if (parts.length == 3) {
                    return Date.valueOf(parts[2] + "-" + parts[1] + "-" + parts[0]);
                }
            }
            // Fallback to standard SQL format
            return Date.valueOf(dobStr);
        } catch (Exception e) {
            return null;
        }
    }

    // Builds an audit text string from violation reason label, detail, and evidence path.
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

    // Builds a reason text string from a reason code and optional detail.
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
