package examiner.service.impl;

import examiner.dao.AuditDAO;
import examiner.dao.CandidateDAO;
import examiner.dao.CandidateViolationDAO;
import examiner.dao.DeductionRecordDAO;
import examiner.dao.ExamAreaDAO;
import examiner.dao.ExamDeviceDAO;
import examiner.dao.ExamEnrollmentDAO;
import examiner.dao.ExamEnrollmentSectionDAO;
import examiner.dao.ExamResultDAO;
import examiner.dao.ExamScoreDAO;
import examiner.dao.ExamSectionDAO;
import examiner.dao.ExaminerScheduleDAO;
import examiner.dao.ExaminerViewDAO;
import examiner.dao.ScoreDeductionDAO;
import examiner.dao.UserDAO;
import examiner.dao.impl.AuditDAOImpl;
import examiner.dao.impl.CandidateDAOImpl;
import examiner.dao.impl.CandidateViolationDAOImpl;
import examiner.dao.impl.DeductionRecordDAOImpl;
import examiner.dao.impl.ExamAreaDAOImpl;
import examiner.dao.impl.ExamDeviceDAOImpl;
import examiner.dao.impl.ExamEnrollmentDAOImpl;
import examiner.dao.impl.ExamEnrollmentSectionDAOImpl;
import examiner.dao.impl.ExamResultDAOImpl;
import examiner.dao.impl.ExamScoreDAOImpl;
import examiner.dao.impl.ExamSectionDAOImpl;
import examiner.dao.impl.ExaminerScheduleDAOImpl;
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
import shared.model.CandidateViolation;
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
import java.util.List;
import java.util.Map;
import shared.model.ExamSection;
import shared.model.ExaminerSchedule;
import shared.util.PasswordUtil;
import examiner.service.DispatchService;
import examiner.service.ProgressService;
import examiner.service.EnrollmentService;

// Examiner action service: presence tracking, call-board actions, scoring, suspensions, and device operations.
public class ActionServiceImpl implements ActionService {

    private static final int PRACTICAL_PASS_SCORE = 80;

    private final AuditService auditService = new AuditServiceImpl();
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final CandidateViolationDAO candidateViolationDAO = new CandidateViolationDAOImpl();
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
    private final ExaminerScheduleDAO scheduleDAO = new ExaminerScheduleDAOImpl();
    private final ExamViewService dataService = new ExamViewServiceImpl();
    private final ExaminerViewDAO examinerDataDAO = new ExaminerViewDAOImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final DispatchService dispatchService = new DispatchServiceImpl();
    private final ProgressService sectionProgressService = new ProgressServiceImpl();

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

    private static boolean isSectionRequired(EnrollmentDTO enrollment, SectionType sectionType) {
        if (enrollment == null) {
            return false;
        }
        SectionType current = sectionType != null ? sectionType : SectionType.LAYOUT;
        if (current == SectionType.THEORY) {
            return enrollment.isTakeTheory();
        }
        if (current == SectionType.LAYOUT) {
            return enrollment.isTakeLayout();
        }
        return true;
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
        }
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
        }
        if (enrollment.getSectionStatus() != CandidateStatus.COMPLETED) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "scoreEditNotAllowed");
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

    // Updates practical score directly after password-confirmed reason validation.
    @Override
    public ServiceResult<Void> updatePracticalScoreWithReason(int examId, int sbd, int newScore, User user,
            String password, String reasonCode, String reasonDetail, Integer actionUserId, SectionType sectionType) {
        if (newScore < 0 || newScore > 100 || newScore % 5 != 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "invalidScore");
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn lý do sửa điểm.");
        }
        if (!verifyPassword(user, password)) {
            return ServiceResult.fail(ErrorType.PERMISSION_DENIED, "Mật khẩu xác nhận không đúng.");
        }
        SectionType targetSection = sectionType != null ? sectionType : SectionType.LAYOUT;
        if (targetSection != SectionType.LAYOUT) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "scoreEditNotAllowed");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, targetSection);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "notFound");
        }
        if (!isSectionRequired(enrollment, targetSection)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "scoreEditNotAllowed");
        }
        if (enrollment.getSectionStatus() != CandidateStatus.COMPLETED) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "scoreEditNotAllowed");
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        if (enrollmentRecord == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "notFound");
        }
        int examScoreId = ensurePracticalExamScore(enrollment.getCandidateId(), examId);
        if (examScoreId <= 0 || !examScoreDAO.updateScore(examScoreId, newScore)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "scoreUpdateFailed");
        }
        if (!persistPracticalOutcome(examId, enrollmentRecord.getExamEnrollmentId(), false)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "scoreUpdateFailed");
        }
        if (actionUserId != null) {
            String reasonText = buildReasonText(reasonCode, reasonDetail);
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    "Cập nhật điểm thực hành SBD " + enrollment.getCandidateNumber()
                    + " thành " + newScore
                    + (reasonText.isBlank() ? "" : " - Lý do: " + reasonText),
                    enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    // Records violation with audit trail.
    @Override
    public ServiceResult<Void> recordViolation(int examId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, String evidencePath, SectionType sectionType) {
        ViolationReason reason = ViolationReason.fromValue(reasonCode);
        if (actionUserId == null || reason == null || evidencePath == null || evidencePath.isBlank()
                || (reason == ViolationReason.OTHER && (reasonDetail == null || reasonDetail.isBlank()))) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "violationInvalid");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null || enrollment.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "violationInvalid");
        }
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "violationInvalid");
        }
        int sectionRowId = findSectionRowId(enrollment.getExamEnrollmentId(), sectionType);
        CandidateViolation violation = new CandidateViolation();
        violation.setExamEnrollmentSectionId(sectionRowId);
        violation.setReason(reason.getValue());
        violation.setDetails(reasonDetail != null ? reasonDetail.trim() : null);
        violation.setEvidenceUrl(evidencePath);
        violation.setCreatedBy(actionUserId);
        if (sectionRowId <= 0 || !candidateViolationDAO.addAndSuspend(enrollment.getCandidateId(), violation)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "violationFailed");
        }
        enrollmentSectionDAO.updateDevice(enrollment.getExamEnrollmentId(),
                (sectionType != null ? sectionType : SectionType.LAYOUT).getValue(), null);
        removeFromAllQueues(examId, sbd);
        auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                buildViolationAuditText(reason.getValue(), reasonDetail, evidencePath), enrollment.getCandidateId());
        return ServiceResult.ok(null);
    }

    // One-click suspend: sets Candidate.IsSuspended=true.
    @Override
    public ServiceResult<Void> markSuspended(int examId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, SectionType sectionType) {
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null || enrollment.isSuspended()) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy thí sinh hoặc thí sinh đã bị đình chỉ.");
        }
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
        }
        if (candidateDAO.get(enrollment.getCandidateId()) == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy hồ sơ thí sinh.");
        }
        if (!candidateDAO.updateSuspended(enrollment.getCandidateId(), true)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đình chỉ thí sinh.");
        }
        enrollmentSectionDAO.updateDevice(enrollment.getExamEnrollmentId(),
                (sectionType != null ? sectionType : SectionType.LAYOUT).getValue(), null);
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
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
        }
        if (enrollmentRecord != null
                && (enrollment.getSectionStatus() == null || enrollment.getSectionStatus() == CandidateStatus.NOT_STARTED)) {
            ensureSectionAreaFromSchedule(examId, enrollmentRecord.getExamEnrollmentId(), sessionSection, actionUserId);
            if (!enrollmentSectionDAO.markCheckedIn(
                    enrollmentRecord.getExamEnrollmentId(), sessionSection.getValue(), actionUserId)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể cập nhật trạng thái phần thi.");
            }
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Điểm danh SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        return ServiceResult.ok(null);
    }

    private void ensureSectionAreaFromSchedule(int examId, int examEnrollmentId, SectionType sectionType,
            Integer examinerUserId) {
        if (examId <= 0 || examEnrollmentId <= 0 || sectionType == null || examinerUserId == null) {
            return;
        }
        int existingAreaId = enrollmentSectionDAO.getIfAreaIdByEnrollmentAndSection(
                examEnrollmentId, sectionType.getValue());
        if (existingAreaId > 0) {
            return;
        }
        for (ExaminerSchedule schedule : scheduleDAO.getByExamId(examId)) {
            if (schedule == null || schedule.getExaminerId() != examinerUserId
                    || schedule.getExamAreaId() == null || schedule.getExamAreaId() <= 0
                    || schedule.getExamSectionId() == null) {
                continue;
            }
            ExamSection scheduledSection = sectionDAO.get(schedule.getExamSectionId());
            if (scheduledSection == null || SectionType.fromValue(scheduledSection.getSectionType()) != sectionType) {
                continue;
            }
            enrollmentSectionDAO.updateExamAreaIdByEnrollmentIdAndSectionType(
                    examEnrollmentId, sectionType.getValue(), schedule.getExamAreaId());
            return;
        }
    }

    // Reverses attendance and rolls back in-progress section status when allowed.
    @Override
    public ServiceResult<Void> undoPresent(int examId, int sbd, Integer actionUserId, SectionType sectionType) {
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, sectionType);
        if (enrollment == null || enrollment.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thể hoàn tác điểm danh.");
        }
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
        }
        if (enrollment.getSectionStatus() != CandidateStatus.NOT_STARTED) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không thể hoàn tác điểm danh ở trạng thái hiện tại.");
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        if (enrollmentRecord != null) {
            SectionType sessionSection = sectionType != null ? sectionType : SectionType.LAYOUT;
            if (!enrollmentSectionDAO.clearCheckedInIfNotStarted(
                    enrollmentRecord.getExamEnrollmentId(), sessionSection.getValue())) {
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
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

    @Override
    public ServiceResult<Void> savePracticalScore(int examId, int examAreaId, int sbd, int deviceId,
            int elapsedSeconds, Map<Integer, Integer> occurrences, Integer actionUserId) {
        if (examAreaId <= 0 || deviceId <= 0 || elapsedSeconds < 0 || occurrences == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "scorePayloadInvalid");
        }
        EnrollmentDTO enrollment = loadEnrollment(examId, sbd, SectionType.LAYOUT);
        if (!isSectionRequired(enrollment, SectionType.LAYOUT)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "candidateNotRequired");
        }
        if (!isPracticalSectionAllowed(examId, enrollment, SectionType.LAYOUT)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "candidateNotEligibleForPractical");
        }
        ExamDevice device = deviceDAO.get(deviceId);
        if (enrollment == null || enrollment.isSuspended() || device == null
                || device.getExamAreaId() != examAreaId || !device.isActive()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "vehicleInvalid");
        }
        if (!enrollment.isPresent()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "candidateNotCheckedIn");
        }
        CandidateStatus currentStatus = enrollment.getSectionStatus() != null
                ? enrollment.getSectionStatus()
                : CandidateStatus.NOT_STARTED;
        if (currentStatus != CandidateStatus.NOT_STARTED && currentStatus != CandidateStatus.IN_PROGRESS) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "scoreAlreadySaved");
        }
        int assignedArea = enrollmentSectionDAO.getIfAreaIdByEnrollmentAndSection(
                enrollment.getExamEnrollmentId(), SectionType.LAYOUT.getValue());
        if (assignedArea != examAreaId) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "areaMismatch");
        }
        if (!enrollmentSectionDAO.isDeviceAvailable(deviceId, enrollment.getExamEnrollmentId())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "vehicleOccupied");
        }
        if (!changeCandidateVehicle(examId, sbd, deviceId, actionUserId, SectionType.LAYOUT).isSuccess()) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "vehicleFailed");
        }
        if (!enrollmentSectionDAO.updateDevice(
                enrollment.getExamEnrollmentId(), SectionType.LAYOUT.getValue(), deviceId)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "vehicleFailed");
        }
        if (enrollment.getSectionStatus() == CandidateStatus.NOT_STARTED
                && !sectionProgressService.update(
                        enrollment.getExamEnrollmentId(), SectionType.LAYOUT, CandidateStatus.IN_PROGRESS)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "statusFailed");
        }
        for (Map.Entry<Integer, Integer> item : occurrences.entrySet()) {
            int deductionId = item.getKey();
            int requested = item.getValue() != null ? item.getValue() : 0;
            if (deductionId <= 0 || requested < 0 || requested > 100) {
                return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "scorePayloadInvalid");
            }
            ScoreDeduction rule = scoreDeductionDAO.get(deductionId);
            if (rule == null || rule.getExamSectionId() <= 0) {
                return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "deductionInvalid");
            }
            int examScoreId = ensurePracticalExamScore(enrollment.getCandidateId(), examId);
            if (examScoreId <= 0) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "scoreFailed");
            }
            int current = deductionRecordDAO.getOccurrenceCount(examScoreId, deductionId);
            if (current != requested && !applyDeductionDelta(examScoreId, deductionId, requested - current)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "scoreFailed");
            }
            if (!examScoreDAO.recalculateFromDeductions(examScoreId)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "scoreFailed");
            }
        }
        ServiceResult<Void> result = finalizeScoreEntry(examId, sbd, actionUserId, SectionType.LAYOUT);
        if (result.isSuccess() && actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    "Lưu điểm thực hành SBD " + sbd + ", thời gian " + elapsedSeconds + " giây",
                    enrollment.getCandidateId());
        }
        return result;
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thí sinh không thi phần này.");
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        if (enrollmentRecord == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy đăng ký thi.");
        }
        CandidateStatus current = enrollment.getSectionStatus() != null ? enrollment.getSectionStatus() : CandidateStatus.NOT_STARTED;
        SectionType sessionSection = sectionType != null ? sectionType : SectionType.LAYOUT;
        if (current == CandidateStatus.AWAITING_SIGNATURE) {
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
        if (!isSectionRequired(enrollment, sectionType)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "notRequired");
        }
        if (enrollment.getSectionStatus() == CandidateStatus.COMPLETED) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "alreadyCompleted");
        }
        ExamEnrollment enrollmentRecord = enrollmentDAO.getByExamAndCandidate(examId, enrollment.getCandidateId());
        if (enrollmentRecord == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "completeFailed");
        }
        if (!sectionProgressService.isResultPrinted(enrollmentRecord.getExamEnrollmentId(), completedSection)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "needResultPrint");
        }
        if (!sectionProgressService.update(
                enrollmentRecord.getExamEnrollmentId(), completedSection, CandidateStatus.COMPLETED)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "completeFailed");
        }
        enrollmentSectionDAO.updateDevice(
                enrollmentRecord.getExamEnrollmentId(), completedSection.getValue(), null);
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Hoàn tất phần thi SBD " + enrollment.getCandidateNumber(), enrollment.getCandidateId());
        }
        boolean sectionPassed = sectionPassedHint != null
                ? sectionPassedHint
                : computeSectionPassed(examId, enrollment);
        removeFromAllQueues(examId, sbd);
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

    // Legacy queue hook kept as a no-op; examiner action now uses DB state only.
    private static void removeFromAllQueues(int examId, int sbd) {
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

    private int findSectionRowId(int enrollmentId, SectionType sectionType) {
        SectionType expected = sectionType != null ? sectionType : SectionType.LAYOUT;
        for (shared.model.ExamEnrollmentSection row : enrollmentSectionDAO.getAllByEnrollmentId(enrollmentId)) {
            if (row.getExamSectionId() == null) {
                continue;
            }
            ExamSection section = sectionDAO.get(row.getExamSectionId());
            if (section != null && SectionType.fromValue(section.getSectionType()) == expected) {
                return row.getExamEnrollmentSectionId();
            }
        }
        return 0;
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

    private int ensurePracticalExamScore(int candidateId, int examId) {
        ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, candidateId);
        if (enrollment == null) {
            return 0;
        }
        int resultId = examResultDAO.getExamResultIdByExamEnrollmentId(enrollment.getExamEnrollmentId());
        if (resultId <= 0) {
            ExamResult result = new ExamResult();
            result.setExamEnrollmentId(enrollment.getExamEnrollmentId());
            result.setPassed(false);
            resultId = examResultDAO.add(result);
        }
        int sectionId = loadLayoutSectionId(examId);
        if (resultId <= 0 || sectionId <= 0) {
            return 0;
        }
        ExamScore score = examScoreDAO.getByExamResultAndSection(resultId, sectionId);
        if (score != null) {
            return score.getExamScoreId();
        }
        score = new ExamScore();
        score.setExamResultId(resultId);
        score.setExamSectionId(sectionId);
        score.setScore(100);
        return examScoreDAO.add(score);
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
