package examiner.service.impl;

import examiner.dao.AuditDAO;
import examiner.dao.CandidateDAO;
import examiner.dao.DeductionRecordDAO;
import examiner.dao.ExamDeviceDAO;
import examiner.dao.ExamEnrollmentDAO;
import examiner.dao.ExamResultDAO;
import examiner.dao.ExamScoreDAO;
import examiner.dao.ExamSectionDAO;
import examiner.dao.ExaminerViewDAO;
import examiner.dao.ScoreDeductionDAO;
import examiner.dao.impl.AuditDAOImpl;
import examiner.dao.impl.CandidateDAOImpl;
import examiner.dao.impl.DeductionRecordDAOImpl;
import examiner.dao.impl.ExamDeviceDAOImpl;
import examiner.dao.impl.ExamEnrollmentDAOImpl;
import examiner.dao.impl.ExamResultDAOImpl;
import examiner.dao.impl.ExamScoreDAOImpl;
import examiner.dao.impl.ExamSectionDAOImpl;
import examiner.dao.impl.ExaminerViewDAOImpl;
import examiner.dao.impl.ScoreDeductionDAOImpl;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ServiceResult;
import examiner.enums.AuditAction;
import examiner.enums.SectionType;
import examiner.enums.AuditEntity;
import examiner.enums.CandidateStatus;
import examiner.enums.ErrorType;
import examiner.enums.Sex;
import examiner.enums.ViolationReason;
import shared.model.Audit;
import shared.model.Candidate;
import shared.model.DeductionRecord;
import shared.model.ExamDevice;
import shared.model.ExamEnrollment;
import shared.model.ExamResult;
import shared.model.ExamScore;
import shared.model.ScoreDeduction;
import shared.model.User;
import examiner.service.AuditService;
import examiner.service.RegistrationService;
import examiner.service.ExamScoreService;
import examiner.service.CallService;
import examiner.service.ExamViewService;
import examiner.util.ExamQueue;
import examiner.util.ExamQueue.Lane;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import shared.model.ExamSection;

public class CallServiceImpl implements CallService {

    private static final Map<Integer, Set<Integer>> PRESENT = new HashMap<>();
    private static final Map<Integer, Set<Integer>> PROCEDURE = new HashMap<>();

    private final AuditService auditService = new AuditServiceImpl();
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamResultDAO examResultDAO = new ExamResultDAOImpl();
    private final ExamScoreDAO examScoreDAO = new ExamScoreDAOImpl();
    private final ExamScoreService examScoreService = new ExamScoreServiceImpl();
    private final DeductionRecordDAO deductionRecordDAO = new DeductionRecordDAOImpl();
    private final ScoreDeductionDAO scoreDeductionDAO = new ScoreDeductionDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();
    private final ExamViewService dataService = new ExamViewServiceImpl();
    private final ExaminerViewDAO examinerDataDAO = new ExaminerViewDAOImpl();
    private final RegistrationService registrationService = new RegistrationServiceImpl();

    @Override
    public void clearPresent(int examId, int sbd) {
        presentSet(examId).remove(sbd);
    }

    @Override
    public void markPresent(int examId, int sbd) {
        presentSet(examId).add(sbd);
        procedureSet(examId).remove(sbd);
    }

    @Override
    public boolean isPresent(int examId, int sbd) {
        return presentSet(examId).contains(sbd);
    }

    @Override
    public void sendToProcedure(int examId, int sbd) {
        procedureSet(examId).add(sbd);
        presentSet(examId).remove(sbd);
        for (Lane lane : Lane.values()) {
            ExamQueue.remove(lane, sbd);
        }
    }

    @Override
    public boolean isInProcedureQueue(int examId, int sbd) {
        return procedureSet(examId).contains(sbd);
    }

    @Override
    public void removeCandidate(int examId, int sbd) {
        presentSet(examId).remove(sbd);
        procedureSet(examId).remove(sbd);
    }

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

    @Override
    public EnrollmentDTO getRegistration(int examId, int sbd) {
        return dataService.findRegistration(examId, sbd);
    }

    @Override
    public ServiceResult<Void> updateCandidateProfile(int examId, int sbd, Integer actionUserId, String fullName,
            Date dateOfBirth, String governmentIdNumber, String phoneNumber, String address, String sex,
            String reasonForTaking) {
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh.");
        }
        String trimmedName = trimParam(fullName);
        String trimmedGovId = trimParam(governmentIdNumber);
        if (trimmedName.isEmpty() || trimmedGovId.isEmpty() || dateOfBirth == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ´ng tin há»“ sÆ¡ khÃ´ng há»£p lá»‡.");
        }
        String trimmedPhone = trimParam(phoneNumber);
        String trimmedAddress = trimParam(address);
        String trimmedReason = trimParam(reasonForTaking);
        Sex sexEnum = sex != null ? Sex.fromValue(sex) : Sex.MALE;
        String sexDb = sexEnum != null ? sexEnum.getValue() : Sex.MALE.getValue();
        SimpleDateFormat dobFmt = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder changes = new StringBuilder();
        appendChange(changes, "Há» vÃ  tÃªn", reg.getFullName(), trimmedName);
        appendChange(changes, "NgÃ y sinh",
                reg.getDateOfBirth() != null ? dobFmt.format(reg.getDateOfBirth()) : null,
                dobFmt.format(dateOfBirth));
        appendChange(changes, "CCCD", reg.getGovIdNo(), trimmedGovId);
        appendChange(changes, "Sá»‘ Ä‘iá»‡n thoáº¡i", reg.getPhoneNo(), trimmedPhone);
        appendChange(changes, "Äá»‹a chá»‰", reg.getAddress(), trimmedAddress);
        appendChange(changes, "Giá»›i tÃ­nh", reg.isSex() ? Sex.FEMALE.getValue() : Sex.MALE.getValue(), sexDb);
        appendChange(changes, "LÃ½ do thi", reg.getReasonForTaking(), trimmedReason);
        boolean updated = enrollmentDAO.updateExaminerProfile(
                reg.getId(), trimmedName, dateOfBirth, trimmedGovId,
                trimmedPhone, trimmedAddress, sexDb, trimmedReason);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ cáº­p nháº­t há»“ sÆ¡ thÃ­ sinh.");
        }
        if (actionUserId != null && changes.length() > 0) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Cáº­p nháº­t há»“ sÆ¡ SBD " + reg.getCandidateNumber() + ": " + changes, reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> callCandidate(int examId, Integer sbd, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination) {
        if (sbd == null || sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sá»‘ bÃ¡o danh khÃ´ng há»£p lá»‡.");
        }
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh.");
        }
        if (!dataService.isCallEligible(examId, reg, isTheory, sectionName)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ­ sinh khÃ´ng Ä‘á»§ Ä‘iá»u kiá»‡n Ä‘á»ƒ gá»i.");
        }
        boolean called = insertCall(examId, reg, user, actionUserId, callDestination);
        if (!called) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ ghi nháº­n lá»‡nh gá»i thÃ­ sinh.");
        }
        Lane lane = ExamQueue.laneFor(examSection);
        ExamQueue.setCalledSbd(lane, reg.getCandidateNumber());
        ExamQueue.setActiveSbd(lane, reg.getCandidateNumber());
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Integer> callNextCandidate(int examId, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination) {
        Lane lane = ExamQueue.laneFor(examSection);
        Integer queued = ExamQueue.peekFirst(lane);
        if (queued != null && queued > 0) {
            if (callCandidate(examId, queued, user, actionUserId, examSection, isTheory, sectionName,
                    callDestination).isSuccess()) {
                return ServiceResult.ok(queued);
            }
        }
        List<EnrollmentDTO> all = registrationService.getCandidatesByExam(examId);
        for (EnrollmentDTO reg : all) {
            if (!dataService.isCallEligible(examId, reg, isTheory, sectionName)) {
                continue;
            }
            if (insertCall(examId, reg, user, actionUserId, callDestination)) {
                ExamQueue.setCalledSbd(lane, reg.getCandidateNumber());
                ExamQueue.setActiveSbd(lane, reg.getCandidateNumber());
                return ServiceResult.ok(reg.getCandidateNumber());
            }
        }
        return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng cÃ²n thÃ­ sinh Ä‘á»§ Ä‘iá»u kiá»‡n Ä‘á»ƒ gá»i.");
    }

    @Override
    public ServiceResult<Integer> callSelectedCandidates(int examId, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination, int[] sbds) {
        if (sbds == null || sbds.length == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ChÆ°a chá»n thÃ­ sinh.");
        }
        int count = 0;
        for (int sbd : sbds) {
            if (sbd <= 0) {
                continue;
            }
            if (callCandidate(examId, sbd, user, actionUserId, examSection, isTheory, sectionName,
                    callDestination).isSuccess()) {
                count++;
            }
        }
        if (count == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "KhÃ´ng gá»i Ä‘Æ°á»£c thÃ­ sinh nÃ o.");
        }
        return ServiceResult.ok(count);
    }

    @Override
    public ServiceResult<Void> callScoreEntryCandidate(int examId, Integer sbd, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination,
            boolean scoreEntry) {
        if (sbd == null || sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sá»‘ bÃ¡o danh khÃ´ng há»£p lá»‡.");
        }
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (!dataService.isScoreQueueEligible(examId, reg, isTheory, sectionName)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ­ sinh khÃ´ng Ä‘á»§ Ä‘iá»u kiá»‡n nháº­p Ä‘iá»ƒm.");
        }
        boolean called = insertScoreEntryCall(examId, reg, user, actionUserId, callDestination);
        if (!called) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ ghi nháº­n lá»‡nh gá»i nháº­p Ä‘iá»ƒm.");
        }
        Lane lane = ExamQueue.laneFor(examSection);
        ExamQueue.setCalledSbd(lane, reg.getCandidateNumber());
        ExamQueue.setActiveSbd(lane, reg.getCandidateNumber());
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> setDeviceMaintenance(int deviceId, Integer actionUserId) {
        if (deviceId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiáº¿t bá»‹ khÃ´ng há»£p lá»‡.");
        }
        boolean updated = deviceDAO.updateStatus(deviceId, false);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ Ä‘áº·t thiáº¿t bá»‹ vÃ o báº£o trÃ¬.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_DEVICE,
                    "Äáº·t thiáº¿t bá»‹ vÃ o báº£o trÃ¬", deviceId);
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> setDeviceAvailable(int deviceId, Integer actionUserId) {
        if (deviceId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiáº¿t bá»‹ khÃ´ng há»£p lá»‡.");
        }
        boolean updated = deviceDAO.updateStatus(deviceId, true);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ Ä‘áº·t thiáº¿t bá»‹ sáºµn sÃ ng.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_DEVICE,
                    "Äáº·t thiáº¿t bá»‹ sáºµn sÃ ng", deviceId);
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> changeCandidateVehicle(int examId, int sbd, int deviceId, Integer actionUserId) {
        if (examId <= 0 || sbd <= 0 || deviceId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ´ng tin gÃ¡n xe khÃ´ng há»£p lá»‡.");
        }
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh.");
        }
        if (!isDeviceInExam(examId, deviceId)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiáº¿t bá»‹ khÃ´ng thuá»™c ca thi.");
        }
        boolean updated = enrollmentDAO.assignExamDevice(reg.getId(), examId, deviceId);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ gÃ¡n xe cho thÃ­ sinh.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "GÃ¡n xe #" + deviceId + " cho SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> updateTheoryScore(int examId, int sbd, User user, String password, Integer newScore,
            String reasonCode, String reasonDetail, Integer actionUserId) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lÃ²ng chá»n lÃ½ do sá»­a Ä‘iá»ƒm.");
        }
        if (!verifyPassword(user, password)) {
            return ServiceResult.fail(ErrorType.PERMISSION_DENIED, "Máº­t kháº©u xÃ¡c nháº­n khÃ´ng Ä‘Ãºng.");
        }
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh.");
        }
        int score = newScore != null ? newScore : -1;
        int maxScore = dataService.theoryMaxQuestions();
        if (score < 0 || score > maxScore) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Äiá»ƒm lÃ½ thuyáº¿t khÃ´ng há»£p lá»‡.");
        }
        Integer oldScore = reg.getTheoryScore();
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        boolean updated = examScoreService.upsertTheoryCorrectCount(reg.getId(), score,
                dataService.theoryPassThreshold());
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ cáº­p nháº­t Ä‘iá»ƒm lÃ½ thuyáº¿t.");
        }
        if (actionUserId != null) {
            String passed = score >= dataService.theoryPassThreshold() ? "Äáº¡t" : "TrÆ°á»£t";
            String message = "Sá»­a Ä‘iá»ƒm lÃ½ thuyáº¿t SBD " + reg.getCandidateNumber() + ": "
                    + (oldScore != null ? oldScore : "-") + " -> " + score + " (" + passed + ")";
            if (!auditReason.isBlank()) {
                message += " - LÃ½ do: " + auditReason;
            }
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE, message, reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> logPracticalScoreEditReason(int examId, int sbd, User user, String password,
            String reasonCode, String reasonDetail, Integer actionUserId) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lÃ²ng chá»n lÃ½ do sá»­a Ä‘iá»ƒm.");
        }
        if (!verifyPassword(user, password)) {
            return ServiceResult.fail(ErrorType.PERMISSION_DENIED, "Máº­t kháº©u xÃ¡c nháº­n khÃ´ng Ä‘Ãºng.");
        }
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh.");
        }
        String auditReason = buildReasonText(reasonCode, reasonDetail);
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    "Sá»­a Ä‘iá»ƒm thá»±c hÃ nh SBD " + reg.getCandidateNumber()
                    + (auditReason.isBlank() ? "" : " - LÃ½ do: " + auditReason),
                    reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> recordViolation(int examId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, String evidencePath) {
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null || reg.isSuspended()) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh hoáº·c thÃ­ sinh Ä‘Ã£ bá»‹ Ä‘Ã¬nh chá»‰.");
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lÃ²ng chá»n lÃ½ do vi pháº¡m.");
        }
        ViolationReason reason = ViolationReason.fromValue(reasonCode);
        String reasonLabel = reason != null ? reason.getValue()
                : reasonCode.trim();
        String detail = reasonDetail != null ? reasonDetail.trim() : "";
        String auditText = buildViolationAuditText(reasonLabel, detail, evidencePath);
        auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                "Vi pháº¡m SBD " + reg.getCandidateNumber() + ": " + auditText, reg.getId(), auditText);
        Candidate candidate = candidateDAO.getById(reg.getId());
        if (candidate == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y há»“ sÆ¡ thÃ­ sinh.");
        }
        candidate.setSuspended(true);
        boolean updated = candidateDAO.update(candidate);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ ghi nháº­n vi pháº¡m.");
        }
        removeFromAllQueues(reg.getCandidateNumber());
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> markPresent(int examId, int sbd, Integer actionUserId) {
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null || reg.isAbsent() || reg.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ­ sinh khÃ´ng thá»ƒ Ä‘iá»ƒm danh.");
        }
        if (CandidateStatus.COMPLETED.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ­ sinh Ä‘Ã£ hoÃ n táº¥t pháº§n thi.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, reg.getId());
        if (enrollment != null && CandidateStatus.fromValue(enrollment.getSectionStatus()) == CandidateStatus.NOT_STARTED) {
            enrollment.setSectionStatus(CandidateStatus.IN_PROGRESS.getValue());
            enrollmentDAO.update(enrollment);
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Äiá»ƒm danh SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> undoPresent(int examId, int sbd, Integer actionUserId) {
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null || reg.isSuspended()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ­ sinh khÃ´ng thá»ƒ hoÃ n tÃ¡c Ä‘iá»ƒm danh.");
        }
        if (CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())
                || CandidateStatus.COMPLETED.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "KhÃ´ng thá»ƒ hoÃ n tÃ¡c Ä‘iá»ƒm danh á»Ÿ tráº¡ng thÃ¡i hiá»‡n táº¡i.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, reg.getId());
        if (enrollment != null && CandidateStatus.fromValue(enrollment.getSectionStatus()) == CandidateStatus.IN_PROGRESS) {
            enrollment.setSectionStatus(CandidateStatus.NOT_STARTED.getValue());
            enrollmentDAO.update(enrollment);
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "HoÃ n tÃ¡c Ä‘iá»ƒm danh SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> sendWrongInfoToProcedure(int examId, int sbd, Integer actionUserId) {
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh.");
        }
        removeFromAllQueues(reg.getCandidateNumber());
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "Sai thÃ´ng tin - chuyá»ƒn phÃ²ng thá»§ tá»¥c SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> adjustScoreDeduction(int examId, int sbd, int deductionId, int delta,
            Integer actionUserId) {
        if (sbd <= 0 || deductionId <= 0 || delta == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ´ng tin Ä‘iá»u chá»‰nh Ä‘iá»ƒm khÃ´ng há»£p lá»‡.");
        }
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ­ sinh khÃ´ng thá»ƒ Ä‘iá»u chá»‰nh Ä‘iá»ƒm.");
        }
        boolean updated = adjustScoreDeductionOccurrence(reg.getId(), examId, deductionId, delta);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ Ä‘iá»u chá»‰nh Ä‘iá»ƒm trá»«.");
        }
        if (actionUserId != null) {
            String action = delta > 0 ? "cá»™ng" : "trá»«";
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                    action + " Ä‘iá»ƒm lá»—i #" + deductionId + " cho SBD " + reg.getCandidateNumber()
                    + " (Î”=" + delta + ")",
                    reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> finalizeScoreEntry(int examId, int sbd, Integer actionUserId) {
        if (sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sá»‘ bÃ¡o danh khÃ´ng há»£p lá»‡.");
        }
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ­ sinh khÃ´ng thá»ƒ hoÃ n táº¥t nháº­p Ä‘iá»ƒm.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, reg.getId());
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Ä‘Äƒng kÃ½ thi.");
        }
        enrollment.setSectionStatus(CandidateStatus.AWAITING_SIGNATURE.getValue());
        boolean updated = enrollmentDAO.update(enrollment);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ hoÃ n táº¥t nháº­p Ä‘iá»ƒm.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "GiÃ¡m kháº£o hoÃ n táº¥t nháº­p Ä‘iá»ƒm SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return user != null && password != null && !password.isBlank()
                && AuthServiceImpl.passwordsMatch(password.trim(), user.getPasswordHash());
    }

    @Override
    public ServiceResult<Void> printSignatureForm(int examId, int sbd, Integer actionUserId) {
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null || !CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "ThÃ­ sinh chÆ°a sáºµn sÃ ng in biÃªn báº£n.");
        }
        ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, reg.getId());
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Ä‘Äƒng kÃ½ thi.");
        }
        enrollment.setSignaturePrinted(true);
        boolean updated = enrollmentDAO.update(enrollment);
        if (!updated) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ ghi nháº­n in biÃªn báº£n.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                    "In biÃªn báº£n káº¿t quáº£ thi SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> completeCandidateSection(int examId, int sbd, Integer actionUserId,
            Boolean sectionPassedHint) {
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "notFound");
        }
        if (!CandidateStatus.AWAITING_SIGNATURE.getValue().equals(reg.getSectionStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "notAwaiting");
        }
        if (!reg.isSignaturePrinted()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "needSignaturePrint");
        }
        ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, reg.getId());
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
                    "HoÃ n táº¥t pháº§n thi SBD " + reg.getCandidateNumber(), reg.getId());
        }
        boolean sectionPassed = sectionPassedHint != null
                ? sectionPassedHint
                : computeSectionPassed(examId, reg);
        if (!sectionPassed) {
            removeFromAllQueues(sbd);
        } else {
            enqueueNextSection(examId, reg);
        }
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> recordProcedureCall(int examId, int sbd, String result, String callDestination,
            Integer actionUserId) {
        if (sbd <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sá»‘ bÃ¡o danh khÃ´ng há»£p lá»‡.");
        }
        EnrollmentDTO reg = getRegistration(examId, sbd);
        if (reg == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh.");
        }
        Audit audit = new Audit();
        audit.setUserId(actionUserId != null ? actionUserId : 0);
        audit.setAction("CALL");
        audit.setEntityName("Candidate");
        audit.setEntityId(examId + "-" + sbd);
        String detail = "calledTo=" + (callDestination != null ? callDestination : "")
                + ";result=" + (result != null ? result : "");
        audit.setReason(detail);
        audit.setNewValue(detail);
        int insertedId = auditDAO.insert(audit);
        if (insertedId <= 0) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "KhÃ´ng thá»ƒ ghi nháº­n lá»‡nh gá»i.");
        }
        if (actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Gá»i thá»§ tá»¥c SBD " + sbd + ": " + result, reg.getId());
        }
        return ServiceResult.ok(null);
    }

    private boolean isDeviceInExam(int examId, int deviceId) {
        Integer primaryAreaId = examinerDataDAO.findPrimaryExamAreaId(examId);
        if (primaryAreaId == null || primaryAreaId <= 0) {
            return false;
        }
        for (ExamDevice device : deviceDAO.getDevicesByAreaId(primaryAreaId)) {
            if (device.getExamDeviceId() == deviceId) {
                return true;
            }
        }
        return false;
    }

    private boolean insertCall(int examId, EnrollmentDTO reg, User user, Integer actionUserId,
            String callDestination) {
        Audit audit = new Audit();
        audit.setUserId(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        audit.setAction("CALL");
        String entityId = examId + "-" + reg.getCandidateNo();
        String detail = "calledTo=" + callDestination + ";result=Calling";
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        int insertedId = auditDAO.insert(audit);
        if (insertedId > 0 && actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Gá»i SBD " + reg.getCandidateNumber(), reg.getId());
        }
        return insertedId > 0;
    }

    private boolean insertScoreEntryCall(int examId, EnrollmentDTO reg, User user, Integer actionUserId,
            String callDestination) {
        Audit audit = new Audit();
        audit.setUserId(user != null && user.getUserId() > 0 ? user.getUserId() : 0);
        audit.setAction("CALL");
        String entityId = examId + "-" + reg.getCandidateNo();
        String detail = "calledTo=" + callDestination + ";result=Calling";
        audit.setReason(detail);
        audit.setEntityName("Candidate");
        audit.setEntityId(entityId);
        audit.setNewValue(detail);
        int insertedId = auditDAO.insert(audit);
        if (insertedId > 0 && actionUserId != null) {
            auditService.logAction(actionUserId, AuditAction.CREATE, AuditEntity.CANDIDATE_CALL,
                    "Gá»i SBD " + reg.getCandidateNumber(), reg.getId());
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

    private boolean computeSectionPassed(int examId, EnrollmentDTO reg) {
        int enrollmentId = reg.getEnrollment() != null ? reg.getEnrollment().getExamEnrollmentId() : 0;
        if (enrollmentId > 0) {
            Map<Integer, Boolean> flags = examinerDataDAO.loadPassFlagsByExam(examId);
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

    private void enqueueNextSection(int examId, EnrollmentDTO reg) {
        int sbd = reg.getCandidateNumber();
        // Examiner proctors a single section (THEORY/LAYOUT) per exam.
        examiner.enums.SectionType examSection = examiner.enums.SectionType.THEORY;
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

    private boolean adjustScoreDeductionOccurrence(int candidateId, int examId, int scoreDeductionId, int delta) {
        if (candidateId <= 0 || examId <= 0 || scoreDeductionId <= 0 || delta == 0) {
            return false;
        }
        ScoreDeduction rule = scoreDeductionDAO.getById(scoreDeductionId);
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
        int sectionId = rule.getExamSectionId();
        if (sectionId <= 0) {
            sectionId = loadExamSectionId(examId);
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

    private int loadExamSectionId(int examId) {
        ExamSection section = sectionDAO.getBySectionType(SectionType.LAYOUT.getValue());
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
