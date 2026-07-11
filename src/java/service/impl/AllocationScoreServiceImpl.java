package service.impl;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.AllocationScoreResultDTO;
import service.AllocationScoreService;
import service.ExamRegistrationService;
import util.examstaff.AllocationPassRules;

public class AllocationScoreServiceImpl implements AllocationScoreService {

    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();

    @Override
    public AllocationScoreResultDTO submitTheoryScore(ExamRegistrationDTO profile, int sessionId, int score) {
        AllocationScoreResultDTO result = new AllocationScoreResultDTO();
        if (profile == null) {
            result.setErrorMessage("Không tìm thấy thí sinh.");
            return result;
        }
        int enrollSessionId = resolveEnrollmentSessionId(profile, sessionId);
        String license = AllocationPassRules.normalizeLicense(profile.getLicenseCode(), profile.getClazz());
        boolean theoryOk = AllocationPassRules.isTheoryPassed(license, score);
        String passed = AllocationPassRules.toPassFlag(theoryOk);
        Integer oldScore = profile.getTheoryScore();
        if (oldScore != null && oldScore == score) {
            result.setSaved(false);
            return result;
        }
        if (!registrationService.updateScores(profile.getId(), enrollSessionId,
                score, passed, null, null)) {
            result.setErrorMessage("Không lưu được điểm lý thuyết cho SBD " + profile.getSbd()
                    + ". Kiểm tra ExamEnrollment và ExamEnrollmentSection.");
            return result;
        }
        profile.setTheoryScore(score);
        profile.setTheoryPassed(passed);
        result.setSaved(true);
        result.setPassedFlag(passed);
        int need = AllocationPassRules.theoryMinCorrect(license);
        int total = AllocationPassRules.theoryQuestionTotal(license);
        String auditDetail = "Nhập điểm LÝ THUYẾT: " + score + "/" + total
                + " (đạt ≥" + need + ") → " + passed.toUpperCase()
                + " cho SBD " + profile.getSbd();
        if (theoryOk && profile.skipsPractical()) {
            auditDetail += " - bảo lưu thực hành/sa hình, đỗ kỳ thi";
        }
        result.setAuditDetail(auditDetail);
        return result;
    }

    @Override
    public AllocationScoreResultDTO submitPracticalScore(ExamRegistrationDTO profile, int sessionId, int score) {
        AllocationScoreResultDTO result = new AllocationScoreResultDTO();
        if (profile == null) {
            result.setErrorMessage("Không tìm thấy thí sinh.");
            return result;
        }
        int enrollSessionId = resolveEnrollmentSessionId(profile, sessionId);
        String passed = AllocationPassRules.toPassFlag(AllocationPassRules.isPracticalPassed(score));
        Integer oldScore = profile.getPracticalScore();
        if (oldScore != null && oldScore == score) {
            result.setSaved(false);
            return result;
        }
        if (!registrationService.updateScores(profile.getId(), enrollSessionId,
                null, null, score, passed)) {
            result.setErrorMessage("Không lưu được điểm thực hành/sa hình cho SBD " + profile.getSbd()
                    + ". Kiểm tra ExamEnrollment và ExamEnrollmentSection.");
            return result;
        }
        profile.setPracticalScore(score);
        profile.setPracticalPassed(passed);
        result.setSaved(true);
        result.setPassedFlag(passed);
        result.setAuditDetail("Nhập điểm THỰC HÀNH: " + score + " → " + passed.toUpperCase()
                + " cho SBD " + profile.getSbd());
        return result;
    }

    private static int resolveEnrollmentSessionId(ExamRegistrationDTO profile, int pageSessionId) {
        // Ca trên trang allocation (?sessionId=) là nguồn tin cậy khi chấm điểm
        if (pageSessionId > 0) {
            return pageSessionId;
        }
        if (profile != null && profile.getExamSessionId() > 0) {
            return profile.getExamSessionId();
        }
        return 0;
    }
}
