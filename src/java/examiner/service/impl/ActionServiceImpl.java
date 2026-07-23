package examiner.service.impl;

import examiner.dao.AuditDAO;
import examiner.dao.CandidateDAO;
import examiner.dao.DeductionRecordDAO;
import examiner.dao.ExamAreaDAO;
import examiner.dao.ExamDeviceDAO;
import examiner.dao.ExamEnrollmentDAO;
import examiner.dao.ExamEnrollmentSectionDAO;
import examiner.dao.ExamResultDAO;
import examiner.dao.ExamScoreDAO;
import examiner.dao.ExamSectionDAO;
import examiner.dao.ExaminerViewDAO;
import examiner.dao.ScoreDeductionDAO;
import examiner.dao.UserDAO;
import examiner.dao.impl.AuditDAOImpl;
import examiner.dao.impl.CandidateDAOImpl;
import examiner.dao.impl.DeductionRecordDAOImpl;
import examiner.dao.impl.ExamAreaDAOImpl;
import examiner.dao.impl.ExamDeviceDAOImpl;
import examiner.dao.impl.ExamEnrollmentDAOImpl;
import examiner.dao.impl.ExamEnrollmentSectionDAOImpl;
import examiner.dao.impl.ExamResultDAOImpl;
import examiner.dao.impl.ExamScoreDAOImpl;
import examiner.dao.impl.ExamSectionDAOImpl;
import examiner.dao.impl.ExaminerViewDAOImpl;
import examiner.dao.impl.ScoreDeductionDAOImpl;
import examiner.dao.impl.UserDAOImpl;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ServiceResult;
import shared.enums.AuditAction;
import shared.enums.SectionType;
import shared.enums.AuditEntity;
import shared.enums.CandidateStatus;
import shared.enums.ErrorType;
import shared.enums.ViolationReason;
import shared.model.Audit;
import shared.model.Candidate;
import shared.model.DeductionRecord;
import shared.model.ExamArea;
import shared.model.ExamDevice;
import shared.model.ExamEnrollment;
import shared.model.ExamResult;
import shared.model.ExamScore;
import shared.model.ScoreDeduction;
import shared.model.User;
import examiner.service.AuditService;
import examiner.service.ActionService;
import examiner.service.ExamViewService;
import examiner.service.impl.DispatchServiceImpl;
import examiner.service.impl.ProgressServiceImpl;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import shared.model.ExamSection;
import shared.queue.ExamRoomQueueRegistry;
import shared.util.PasswordUtil;
import examiner.service.DispatchService;
import examiner.service.ProgressService;
import examiner.service.EnrollmentService;

// Examiner action service: presence tracking, call-board actions, scoring, suspensions, and device operations.
public class ActionServiceImpl implements ActionService {

    private static final int PRACTICAL_PASS_SCORE = 80;

    private static final Map<Integer, Set<Integer>> PRESENT = new HashMap<>();
    private static final Map<Integer, Set<Integer>> PROCEDURE = new HashMap<>();

    private final AuditService auditService = new AuditServiceImpl();
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final ExamEnrollmentSectionDAO enrollmentSectionDAO = new ExamEnrollmentSectionDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamAreaDAO examAreaDAO = new ExamAreaDAOImpl();
    private final ExamResultDAO examResultDAO = new ExamResultDAOImpl();
    private final ExamScoreDAO examScoreDAO = new ExamScoreDAOImpl();
    private final DeductionRecordDAO deductionRecordDAO = new DeductionRecordDAOImpl();
    private final ScoreDeductionDAO scoreDeductionDAO = new ScoreDeductionDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();
    private final ExamViewService dataService = new ExamViewServiceImpl();
    private final ExaminerViewDAO examinerDataDAO = new ExaminerViewDAOImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final DispatchService dispatchService = new DispatchServiceImpl();
    private final ProgressService sectionProgressService = new ProgressServiceImpl();

    // Clears in-memory present flag for a candidate in the exam session.
    @Override
    public void clearPresent(int examId, int sbd) {
        presentSet(examId).remove(sbd);
    }

    // Marks a candidate present in session memory and removes them from the procedure queue.
    @Override
    public void markPresent(int examId, int sbd) {
        presentSet(examId).add(sbd);
        procedureSet(examId).remove(sbd);
    }

    // Returns whether the candidate is marked present in session memory.
    @Override
    public boolean isPresent(int examId, int sbd) {
        return presentSet(examId).contains(sbd);
    }

    // Moves candidate from present list to the procedure-room queue.
    @Override
    public void sendToProcedure(int examId, int sbd) {
        procedureSet(examId).add(sbd);
        presentSet(examId).remove(sbd);
        removeFromAllQueues(examId, sbd);
    }

    // Returns whether the candidate is waiting in the procedure-room queue.
    @Override
    public boolean isInProcedureQueue(int examId, int sbd) {
        return procedureSet(examId).contains(sbd);
    }

    // Removes candidate from session tracking.
    @Override
    public void removeCandidate(int examId, int sbd) {
        presentSet(examId).remove(sbd);
        procedureSet(examId).remove(sbd);
    }

    // Lazy per-exam set of SBDs marked present in session memory.
    private Set<Integer> presentSet(int examId) {
        synchronized (PRESENT) {
            Set<Integer> set = PRESENT.get(examId);
            if (set == null) {
                set = new HashSet<>();
                PRESENT.put(examId, set);
            }
            return set;
        }
    }

    // Lazy per-exam set of SBDs waiting in the procedure-room queue.
    private Set<Integer> procedureSet(int examId) {
        synchronized (PROCEDURE) {
            Set<Integer> set = PROCEDURE.get(examId);
            if (set == null) {
                set = new HashSet<>();
                PROCEDURE.put(examId, set);
            }
            return set;
        }
    }

    // Loads enrollment DTO for the given exam and candidate number.
    @Override
    public EnrollmentDTO getIfByExamAndSbd(int examId, int sbd) {
        return enrollmentService.getByExamAndSbd(examId, sbd);
    }

    // Loads enrollment with section context for internal action handlers.
    private EnrollmentDTO loadEnrollment(int examId, int sbd, SectionType sectionType) {
        return enrollmentService.getByExamAndSbd(examId, sbd, sectionType);
    }

    // Returns DB section type value, defaulting to layout when null.
    private static String sectionTypeValue(SectionType sectionType) {
        return sectionType != null ? sectionType.getValue() : SectionType.LAYOUT.getValue();
    }

    // Performs examiner call-board action: candidate.
    @Override
    public ServiceResult<Void> actionCandidate(int examId, Integer sbd, User user, Integer actionUserId,
            SectionType sectionType, String actionDestination) {
        if (sbd == null || sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        if (!dataService.isActionEligible(examId, enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không đủ điều kiện để thao tác.");
        }
        boolean logged = insertActionLog(examId, enrollment, user, actionUserId, actionDestination);
        if (!logged) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận thao tác thí sinh.");
        }
        return ServiceResult.ok(null);
    }

    // Performs examiner call-board action: next candidate.
    @Override
    public ServiceResult<Integer> actionNextCandidate(int examId, int examAreaId, User user, Integer actionUserId,
            SectionType sectionType, String actionDestination) {
        if (examAreaId > 0) {
            List<Integer> order = ExamRoomQueueRegistry.displayOrder(examId, examAreaId, sectionType);
            for (Integer queuedSbd : order) {
                if (queuedSbd == null || queuedSbd <= 0) {
                    continue;
                }
                if (actionCandidate(examId, queuedSbd, user, actionUserId, sectionType,
                        actionDestination).isSuccess()) {
                    return ServiceResult.ok(queuedSbd);
                }
            }
        }
        List<EnrollmentDTO> all = enrollmentService.getAllByExam(examId, sectionType);
        for (EnrollmentDTO enrollment : all) {
            if (!dataService.isActionEligible(examId, enrollment, sectionType)) {
                continue;
            }
            if (insertActionLog(examId, enrollment, user, actionUserId, actionDestination)) {
                return ServiceResult.ok(enrollment.getCandidateNumber());
            }
        }
        return ServiceResult.fail(ErrorType.NOT_FOUND, "Không còn thí sinh đủ điều kiện để thao tác.");
    }

    // Performs examiner call-board action: selected candidates.
    @Override
    public ServiceResult<Integer> actionSelectedCandidates(int examId, User user, Integer actionUserId,
            SectionType sectionType, String actionDestination, int[] sbds) {
        if (sbds == null || sbds.length == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Chưa chọn thí sinh.");
        }
        int count = 0;
        for (int sbd : sbds) {
            if (sbd <= 0) {
                continue;
            }
            if (actionCandidate(examId, sbd, user, actionUserId, sectionType,
                    actionDestination).isSuccess()) {
                count++;
            }
        }
        if (count == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không thực hiện được thao tác cho thí sinh nào.");
        }
        return ServiceResult.ok(count);
    }

    // Performs examiner call-board action: score entry candidate.
    @Override
    public ServiceResult<Void> actionScoreEntryCandidate(int examId, Integer sbd, User user, Integer actionUserId,
            SectionType sectionType, String actionDestination, boolean scoreEntry) {
        if (sbd == null || sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (!dataService.isScoreQueueEligible(examId, enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không đủ điều kiện nhập điểm.");
        }
        if (!isPracticalSectionAllowed(examId, enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh chưa đủ điều kiện thi thực hành.");
        }
        boolean logged = insertScoreEntryActionLog(examId, enrollment, user, actionUserId, actionDestination);
        if (!logged) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận thao tác nhập điểm.");
        }
        return ServiceResult.ok(null);
    }

    // Marks an exam device as under maintenance and logs the change.
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

    // Marks an exam device as available and logs the change.
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

    // Changes candidate vehicle for a candidate.
    @Override
    public ServiceResult<Void> changeCandidateVehicle(int examId, int sbd, int deviceId, Integer actionUserId,
            SectionType sectionType) {
        if (examId <= 0 || sbd <= 0 || deviceId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin gán xe không hợp lệ.");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        if (!isDeviceInExam(examId, deviceId)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiết bị không thuộc ca thi.");
        }
        boolean updated = enrollmentDAO.assignExamDevice(enrollment.getCandidateId(), examId, deviceId);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể gán xe cho thí sinh.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Gán xe #" + deviceId + " cho SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Writes audit entry for practical score edit reason.
    @Override
    public ServiceResult<Void> logPracticalScoreEditReason(int examId, int sbd, User user, String password,
            String reasonCode, String reasonDetail, Integer actionUserId, SectionType sectionType) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn lý do sửa điểm.");
        }
        if (!verifyPassword(user, password)) {
            return ServiceResult.fail(ErrorType.PERMISSION_DENIED, "Mật khẩu xác nhận không đúng.");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    "Sửa điểm thực hành SBD " + enrollment.getCandidateNumber()
                    + (auditReason.isBlank() ? "" : " - Lý do: " + auditReason),
                    enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Records violation with audit trail.
    @Override
    public ServiceResult<Void> recordViolation(int examId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, String evidencePath, SectionType sectionType) {
        // Slim path: one-click suspend = Candidate.IsSuspended only (reason optional for audit).
        return markSuspended(examId, sbd, actionUserId, reasonCode, reasonDetail, sectionType);
    }

    // One-click suspend: sets Candidate.IsSuspended=true.
    @Override
    public ServiceResult<Void> markSuspended(int examId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, SectionType sectionType) {
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null || enrollment.isSuspended()) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh hoặc thí sinh đã bị đình chỉ.");
        }
        if (candidateDAO.get(enrollment.getCandidateId()) == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy hồ sơ thí sinh.");
        }
        if (!candidateDAO.updateSuspended(enrollment.getCandidateId(), true)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đình chỉ thí sinh.");
        }
        String auditText = "Đình chỉ SBD " + enrollment.getCandidateNumber();
        if (reasonCode != null && !reasonCode.isBlank()) {
            ViolationReason reason = ViolationReason.fromValue(reasonCode);
            String reasonLabel = reason != null ? reason.getValue() : reasonCode.trim();
            auditText = auditText + ": " + reasonLabel;
            if (reasonDetail != null && !reasonDetail.isBlank()) {
                auditText = auditText + " - " + reasonDetail.trim();
            }
        }
        auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                auditText, enrollment.getCandidateId());
        removeFromAllQueues(examId, enrollment.getCandidateNumber());
        return ServiceResult.ok(null);
    }

    // Clears Candidate.IsSuspended and writes a short audit entry.
    @Override
    public ServiceResult<Void> undoSuspension(int examId, int sbd, Integer actionUserId, SectionType sectionType) {
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null || !enrollment.isSuspended()) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh đang bị đình chỉ.");
        }
        if (candidateDAO.get(enrollment.getCandidateId()) == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy hồ sơ thí sinh.");
        }
        if (!candidateDAO.updateSuspended(enrollment.getCandidateId(), false)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể gỡ đình chỉ thí sinh.");
        }
        auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                "Gỡ đình chỉ SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        return ServiceResult.ok(null);
    }

    // Marks candidate as present with audit.
    @Override
    public ServiceResult<Void> markPresent(int examId, int sbd, Integer actionUserId, SectionType sectionType) {
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null || enrollment.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể điểm danh.");
        }
        if (enrollment.getSectionStatus() == CandidateStatus.COMPLETED) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh đã hoàn tất phần thi.");
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        SectionType sessionSection = sectionType != null ? sectionType : SectionType.LAYOUT;
        if (enrollmentRecord != null) {
            if (sessionSection == SectionType.LAYOUT) {
                Candidate candidate = candidateDAO.get(enrollment.getCandidateId());
                boolean takeTheory = candidate != null && Boolean.TRUE.equals(candidate.getTakeTheory());
                boolean takeLayout = candidate != null && Boolean.TRUE.equals(candidate.getTakeLayout());
                if (!sectionProgressService.isPracticalEntryAllowed(
                        enrollmentRecord.getExamEnrollmentId(), takeTheory, takeLayout)) {
                    return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                            "Thí sinh chưa đủ điều kiện thi thực hành.");
                }
            }
            int areaId = enrollmentSectionDAO.getIfAreaIdByEnrollmentAndSection(
                    enrollmentRecord.getExamEnrollmentId(), sectionTypeValue(sectionType));
            if (areaId <= 0) {
                Candidate candidate = candidateDAO.get(enrollment.getCandidateId());
                if (candidate != null) {
                    dispatchService.passOn(candidate, examId, sessionSection);
                }
            }
        }
        if (enrollmentRecord != null
                && (enrollment.getSectionStatus() == null || enrollment.getSectionStatus() == CandidateStatus.NOT_STARTED)) {
            if (!sectionProgressService.update(
                    enrollmentRecord.getExamEnrollmentId(), sessionSection, CandidateStatus.IN_PROGRESS)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật trạng thái phần thi.");
            }
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Điểm danh SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Reverses attendance and rolls back in-progress section status when allowed.
    @Override
    public ServiceResult<Void> undoPresent(int examId, int sbd, Integer actionUserId, SectionType sectionType) {
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null || enrollment.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể hoàn tác điểm danh.");
        }
        if (enrollment.getSectionStatus() == CandidateStatus.AWAITING_SIGNATURE
                || enrollment.getSectionStatus() == CandidateStatus.COMPLETED) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không thể hoàn tác điểm danh ở trạng thái hiện tại.");
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        if (enrollmentRecord != null
                && enrollment.getSectionStatus() == CandidateStatus.IN_PROGRESS) {
            SectionType sessionSection = sectionType != null ? sectionType : SectionType.LAYOUT;
            if (!sectionProgressService.update(
                    enrollmentRecord.getExamEnrollmentId(), sessionSection, CandidateStatus.NOT_STARTED)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể hoàn tác trạng thái phần thi.");
            }
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Hoàn tác điểm danh SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Sends candidate back to procedure room after wrong personal information.
    @Override
    public ServiceResult<Void> sendWrongInfoToProcedure(int examId, int sbd, Integer actionUserId,
            SectionType sectionType) {
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        Candidate candidate = candidateDAO.get(enrollment.getCandidateId());
        if (candidate != null) {
            dispatchService.passBack(candidate, examId);
        } else {
            removeFromAllQueues(examId, enrollment.getCandidateNumber());
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Sai thông tin - chuyển phòng thủ tục SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Adjusts score deduction during score entry.
    @Override
    public ServiceResult<Void> adjustScoreDeduction(int examId, int sbd, int deductionId, int delta,
            Integer actionUserId, SectionType sectionType) {
        if (sbd <= 0 || deductionId <= 0 || delta == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thông tin điều chỉnh điểm không hợp lệ.");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null || enrollment.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể điều chỉnh điểm.");
        }
        if (!isPracticalSectionAllowed(examId, enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh chưa đủ điều kiện thi thực hành.");
        }
        boolean updated = adjustScoreDeductionOccurrence(enrollment.getCandidateId(), examId, deductionId, delta);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể điều chỉnh điểm trừ.");
        }
        if (actionUserId != null) {
            String action = delta > 0 ? "cộng" : "trừ";
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    action + " điểm lỗi #" + deductionId + " cho SBD " + enrollment.getCandidateNumber(),
                    enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Finalizes score entry and persists score outcome.
    @Override
    public ServiceResult<Void> finalizeScoreEntry(int examId, int sbd, Integer actionUserId, SectionType sectionType) {
        if (sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null || enrollment.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể hoàn tất nhập điểm.");
        }
        if (!isPracticalSectionAllowed(examId, enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh chưa đủ điều kiện thi thực hành.");
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        if (enrollmentRecord == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy đăng ký thi.");
        }
        if (!persistPracticalOutcome(examId, enrollmentRecord.getExamEnrollmentId(), true)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể lưu kết quả điểm thực hành.");
        }
        SectionType sessionSection = sectionType != null ? sectionType : SectionType.LAYOUT;
        if (!sectionProgressService.update(
                enrollmentRecord.getExamEnrollmentId(), sessionSection, CandidateStatus.AWAITING_SIGNATURE)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể hoàn tất nhập điểm.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Giám khảo hoàn tất nhập điểm SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Verifies password before sensitive action.
    @Override
    public boolean verifyPassword(User user, String password) {
        if (user == null || password == null || password.isBlank()) {
            return false;
        }
        // Session User may lack passwordHash; always verify against DB
        User dbUser = userDAO.get(user.getUserId());
        if (dbUser == null || dbUser.getPasswordHash() == null) {
            return false;
        }
        String raw = password.trim();
        String stored = dbUser.getPasswordHash().trim();
        if (PasswordUtil.matches(raw, stored)) {
            return true;
        }
        // Backward compatibility: some seeded accounts may still store plaintext passwords.
        return raw.equals(stored);
    }

    // Generates printable result form for one candidate.
    @Override
    public ServiceResult<Void> printResultForm(int examId, int sbd, Integer actionUserId, SectionType sectionType) {
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh chưa sẵn sàng in biên bản.");
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        if (enrollmentRecord == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy đăng ký thi.");
        }
        CandidateStatus current = enrollment.getSectionStatus() != null ? enrollment.getSectionStatus() : CandidateStatus.NOT_STARTED;
        SectionType sessionSection = sectionType != null ? sectionType : SectionType.LAYOUT;
        if (sessionSection == SectionType.THEORY && current == CandidateStatus.IN_PROGRESS) {
            if (!sectionProgressService.update(
                    enrollmentRecord.getExamEnrollmentId(), sessionSection, CandidateStatus.AWAITING_SIGNATURE)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật trạng thái phần thi.");
            }
        } else if (enrollment.getSectionStatus() != CandidateStatus.AWAITING_SIGNATURE) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh chưa sẵn sàng in biên bản.");
        }
        sectionProgressService.markResultPrinted(enrollmentRecord.getExamEnrollmentId(), sessionSection);
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "In biên bản kết quả thi SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Completes candidate section and advances dispatch.
    @Override
    public ServiceResult<Void> completeCandidateSection(int examId, int sbd, Integer actionUserId,
            Boolean sectionPassedHint, SectionType sectionType) {
        SectionType completedSection = sectionType != null ? sectionType : SectionType.LAYOUT;
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "notFound");
        }
        if (enrollment.getSectionStatus() == CandidateStatus.COMPLETED) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "alreadyCompleted");
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        if (enrollmentRecord == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "completeFailed");
        }
        if (!sectionProgressService.update(
                enrollmentRecord.getExamEnrollmentId(), completedSection, CandidateStatus.COMPLETED)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "completeFailed");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Hoàn tất phần thi SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        boolean sectionPassed = sectionPassedHint != null
                ? sectionPassedHint
                : computeSectionPassed(examId, enrollment);
        int areaId = enrollmentSectionDAO.getIfAreaIdByEnrollmentAndSection(
                enrollmentRecord.getExamEnrollmentId(), sectionTypeValue(sectionType));
        if (areaId <= 0 && enrollmentRecord.getAllocatedExamAreaId() != null) {
            areaId = enrollmentRecord.getAllocatedExamAreaId();
        }
        if (areaId > 0) {
            dispatchService.onSectionComplete(examId, sbd, completedSection, areaId);
        } else {
            ExamRoomQueueRegistry.removeCandidate(examId, sbd);
        }
        if (sectionPassed && completedSection == SectionType.THEORY) {
            Candidate candidate = candidateDAO.get(enrollment.getCandidateId());
            if (candidate != null && Boolean.TRUE.equals(candidate.getTakeLayout())) {
                dispatchService.passOn(candidate, examId, SectionType.LAYOUT);
            }
        }
        return ServiceResult.ok(null);
    }

    // Records procedure action with audit trail.
    @Override
    public ServiceResult<Void> recordProcedureAction(int examId, int sbd, String result, String actionDestination,
            Integer actionUserId, SectionType sectionType) {
        if (sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số báo danh không hợp lệ.");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh.");
        }
        Audit audit = new Audit();
        audit.setUserId(actionUserId != null ? actionUserId : 0);
        audit.setAction("CALL");
        audit.setEntityName("Candidate");
        audit.setEntityId(examId + "-" + sbd);
        String detail = "actionTo=" + (actionDestination != null ? actionDestination : "")
                + ";result=" + (result != null ? result : "");
        audit.setReason(detail);
        audit.setNewValue(detail);
        int insertedId = auditDAO.add(audit);
        if (insertedId <= 0) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể ghi nhận thao tác.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Thao tác thủ tục SBD " + sbd + ": " + result, enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Returns whether the device belongs to an exam area linked to this exam.
    private boolean isDeviceInExam(int examId, int deviceId) {
        ExamDevice target = deviceDAO.get(deviceId);
        if (target == null) {
            return false;
        }
        List<ExamArea> areas = examAreaDAO.getAreasByExamId(examId);
        if (areas == null || areas.isEmpty()) {
            return false;
        }
        for (ExamArea area : areas) {
            if (area != null && area.getExamAreaId() == target.getExamAreaId()) {
                return true;
            }
        }
        return false;
    }

    // Private helper: insert action log.
    private boolean insertActionLog(int examId, EnrollmentDTO enrollment, User user, Integer actionUserId,
            String actionDestination) {
        Audit audit = new Audit();
        audit.setUserId(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        audit.setAction("CALL");
        String entityId = examId + "-" + enrollment.getCandidateNumber();
        String detail = "actionTo=" + actionDestination + ";result=Calling";
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        int insertedId = auditDAO.add(audit);
        if (insertedId > 0 && actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Thao tác SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        return insertedId > 0;
    }

    // Private helper: insert score entry action log.
    private boolean insertScoreEntryActionLog(int examId, EnrollmentDTO enrollment, User user, Integer actionUserId,
            String actionDestination) {
        Audit audit = new Audit();
        audit.setUserId(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        audit.setAction("CALL");
        String entityId = examId + "-" + enrollment.getCandidateNumber();
        String detail = "actionTo=" + actionDestination + ";result=Calling";
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        int insertedId = auditDAO.add(audit);
        if (insertedId > 0 && actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Thao tác SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        return insertedId > 0;
    }

    private static String trimParam(String value) {
        return value == null ? "" : value.trim();
    }

    // Layout actions are blocked until theory section is completed (when required).
    private boolean isPracticalSectionAllowed(int examId, EnrollmentDTO enrollment, SectionType sectionType) {
        SectionType sessionSection = sectionType != null ? sectionType : SectionType.LAYOUT;
        if (sessionSection != SectionType.LAYOUT || enrollment == null) {
            return true;
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        if (enrollmentRecord == null) {
            return false;
        }
        return sectionProgressService.isPracticalEntryAllowed(
                enrollmentRecord.getExamEnrollmentId(),
                enrollment.isTakeTheory(),
                enrollment.isTakeLayout());
    }

    // Private helper: compute section passed.
    private boolean computeSectionPassed(int examId, EnrollmentDTO enrollment) {
        int enrollmentId = enrollment.getExamEnrollmentId();
        if (enrollmentId > 0) {
            Map<Integer, Double> scores = examinerDataDAO.getAllSectionScoresByExam(
                    examId, SectionType.LAYOUT.getValue());
            Double practical = scores.get(enrollmentId);
            if (practical != null) {
                return practical >= PRACTICAL_PASS_SCORE;
            }
            Map<Integer, Boolean> flags = examinerDataDAO.getAllPassFlagsByExam(examId);
            Boolean passed = flags.get(enrollmentId);
            if (passed != null) {
                return passed;
            }
        }
        if (enrollment.getTheoryScore() != null && enrollment.getTheoryScore() >= dataService.theoryPassThreshold()) {
            return true;
        }
        if (enrollment.getTheoryScore() != null) {
            return false;
        }
        return true;
    }

    // Removes a candidate from the in-memory room queue registry.
    private static void removeFromAllQueues(int examId, int sbd) {
        ExamRoomQueueRegistry.removeCandidate(examId, sbd);
    }

    // Private helper: build violation audit text.
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

    // Private helper: build reason text.
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

    // Private helper: adjust score deduction occurrence.
    private boolean adjustScoreDeductionOccurrence(int candidateId, int examId, int scoreDeductionId, int delta) {
        if (candidateId <= 0 || examId <= 0 || scoreDeductionId <= 0 || delta == 0) {
            return false;
        }
        ScoreDeduction rule = scoreDeductionDAO.get(scoreDeductionId);
        if (rule == null) {
            return false;
        }
        ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, candidateId);
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
        int sectionId = loadLayoutSectionId(examId);
        if (sectionId <= 0) {
            sectionId = rule.getExamSectionId();
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
        if (!examScoreDAO.recalculateFromDeductions(examScoreId)) {
            return false;
        }
        // Keep IsPassed in sync while scoring; ResultDate is stamped only on finalize.
        return persistPracticalOutcome(examId, enrollment.getExamEnrollmentId(), false);
    }

    // Ensures ExamScore exists, then sets IsPassed from score >= 80. When stampResultDate,
    // also writes ResultDate = now (SHV bấm hoàn tất nhập điểm).
    private boolean persistPracticalOutcome(int examId, int examEnrollmentId, boolean stampResultDate) {
        if (examEnrollmentId <= 0) {
            return false;
        }
        int examResultId = examResultDAO.getExamResultIdByExamEnrollmentId(examEnrollmentId);
        if (examResultId <= 0) {
            ExamResult result = new ExamResult();
            result.setExamEnrollmentId(examEnrollmentId);
            result.setPassed(false);
            examResultId = examResultDAO.add(result);
        }
        if (examResultId <= 0) {
            return false;
        }
        int sectionId = loadLayoutSectionId(examId);
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
            if (examScoreId <= 0) {
                return false;
            }
            examScore = examScoreDAO.get(examScoreId);
            if (examScore == null) {
                examScore = newScore;
            }
        } else {
            examScoreId = examScore.getExamScoreId();
        }
        if (examScoreId <= 0) {
            return false;
        }
        boolean passed = examScore.getScore() >= PRACTICAL_PASS_SCORE;
        if (stampResultDate) {
            return examResultDAO.updatePassed(examResultId, passed);
        }
        return examResultDAO.updateIsPassed(examResultId, passed);
    }

    // Layout section of the active exam (not the seed ExamSectionId on ScoreDeduction).
    private int loadLayoutSectionId(int examId) {
        if (examId <= 0) {
            return 0;
        }
        for (ExamSection section : sectionDAO.getAllByExamId(examId)) {
            if (section != null && SectionType.LAYOUT.getValue().equals(section.getSectionType())) {
                return section.getExamSectionId();
            }
        }
        return 0;
    }

    // Private helper: apply deduction delta.
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

