package service.impl;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateDstsImportCommitResultDTO;
import dto.examstaff.CandidateDstsImportPreviewDTO;
import service.CandidateDstsImportService;
import service.ExamRegistrationService;
import service.ExamStaffSessionQueryService;
import util.CandidateDstsImportParser;
import util.examstaff.ImportSectionMatch;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CandidateDstsImportServiceImpl implements CandidateDstsImportService {

    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();
    private final ExamStaffSessionQueryService sessionQuery = new ExamStaffSessionQueryServiceImpl();

    @Override
    public CandidateDstsImportPreviewDTO preview(byte[] fileBytes, String fileName,
            String examLicenseCode, int sessionId) throws IOException {
        CandidateDstsImportParser.ParseResult parsed = CandidateDstsImportParser.parse(
                fileBytes, fileName, examLicenseCode);
        int examId = resolveExamId(sessionId);
        Set<String> availableKinds = examId > 0
                ? registrationService.findAvailableSectionKindsForExam(examId)
                : Set.of();

        int validImportCount = 0;
        boolean hasInvalid = parsed.hasInvalidRows();
        for (ExamRegistrationDTO reg : parsed.getRows()) {
            if (!reg.isInvalid()) {
                markSectionMismatchIfAny(reg, availableKinds);
            }
            if (reg.isInvalid()) {
                hasInvalid = true;
                continue;
            }
            String govId = reg.getGovIdNo();
            if (govId == null || govId.isBlank()) {
                continue;
            }
            if (isDuplicateInExam(govId, examId, sessionId)) {
                reg.setDuplicate(true);
            }
            validImportCount++;
        }

        CandidateDstsImportPreviewDTO dto = new CandidateDstsImportPreviewDTO();
        dto.setRows(parsed.getRows());
        dto.setHasInvalidRows(hasInvalid);
        dto.setValidImportCount(validImportCount);
        return dto;
    }

    @Override
    public CandidateDstsImportCommitResultDTO commit(List<ExamRegistrationDTO> previewRows, int sessionId,
            Map<String, String> duplicateActionsByGovId) {
        CandidateDstsImportCommitResultDTO result = new CandidateDstsImportCommitResultDTO();
        if (previewRows == null || previewRows.isEmpty()) {
            return result;
        }

        int examId = resolveExamId(sessionId);
        if (examId <= 0) {
            result.setSkippedCount(previewRows.size());
            result.setSkipSummary("Không xác định được kỳ thi từ ca import.");
            return result;
        }

        Set<String> availableKinds = registrationService.findAvailableSectionKindsForExam(examId);
        int importedCount = 0;
        int skippedCount = 0;
        Map<String, Integer> skipReasons = new LinkedHashMap<>();
        for (ExamRegistrationDTO reg : previewRows) {
            try {
                if (reg.isDuplicate()) {
                    String dupAction = duplicateActionsByGovId != null
                            ? duplicateActionsByGovId.get(reg.getGovIdNo()) : null;
                    if ("skip".equals(dupAction)) {
                        skippedCount++;
                        bump(skipReasons, "Trùng CCCD trong kỳ (chọn Bỏ qua)");
                        continue;
                    }
                }

                markSectionMismatchIfAny(reg, availableKinds);
                if (reg.isInvalid()) {
                    skippedCount++;
                    String reason = reg.getValidationMessage();
                    bump(skipReasons, (reason != null && !reason.isBlank()) ? reason : "Dòng không hợp lệ");
                    continue;
                }

                Integer existingId = registrationService.findCandidateIdByGovId(reg.getGovIdNo());
                if (existingId == null) {
                    existingId = registrationService.findCandidateIdByGovIdAndExam(
                            reg.getGovIdNo(), examId);
                }
                if (existingId == null) {
                    existingId = registrationService.findCandidateIdByGovIdAndSession(
                            reg.getGovIdNo(), sessionId);
                }

                reg.setExamSessionId(sessionId);
                reg.setIsPresent(true);

                if (existingId != null) {
                    reg.setId(existingId);
                    if (!registrationService.ensureExamEnrollmentsForImport(existingId, examId,
                            reg.getTakeTheory(), reg.getTakePractical(), reg.getTakeOnRoad())) {
                        skippedCount++;
                        bump(skipReasons, describeEnrollmentMismatch(reg));
                        continue;
                    }
                    registrationService.updatePresent(existingId, true);
                    registrationService.updatePhoto(existingId, null);
                    importedCount++;
                } else if (registrationService.insertFromDstsImport(reg)) {
                    registrationService.updatePhoto(reg.getId(), null);
                    importedCount++;
                } else {
                    skippedCount++;
                    bump(skipReasons, describeEnrollmentMismatch(reg));
                }
            } catch (Exception ex) {
                System.err.println("Error importing: " + reg.getFullName() + " - " + ex.getMessage());
                ex.printStackTrace();
                skippedCount++;
                bump(skipReasons, "Lỗi hệ thống khi ghi CSDL");
            }
        }

        result.setImportedCount(importedCount);
        result.setSkippedCount(skippedCount);
        result.setSkipSummary(formatSkipSummary(skipReasons));
        return result;
    }

    private static void markSectionMismatchIfAny(ExamRegistrationDTO reg, Set<String> availableKinds) {
        if (reg == null || reg.isInvalid()) {
            return;
        }
        String mismatch = ImportSectionMatch.mismatchReason(
                reg.getTakeTheory(), reg.getTakePractical(), reg.getTakeOnRoad(), availableKinds);
        if (mismatch == null) {
            return;
        }
        reg.setInvalid(true);
        String existing = reg.getValidationMessage();
        if (existing == null || existing.isBlank()) {
            reg.setValidationMessage(mismatch);
        } else if (!existing.contains(mismatch)) {
            reg.setValidationMessage(existing + "; " + mismatch);
        }
    }

    private static void bump(Map<String, Integer> skipReasons, String reason) {
        skipReasons.merge(reason, 1, Integer::sum);
    }

    private static String formatSkipSummary(Map<String, Integer> skipReasons) {
        if (skipReasons.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : skipReasons.entrySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(e.getValue()).append(" — ").append(e.getKey());
        }
        return sb.toString();
    }

    private static String describeEnrollmentMismatch(ExamRegistrationDTO reg) {
        if (Boolean.FALSE.equals(reg.getTakeTheory())
                && !Boolean.FALSE.equals(reg.getTakePractical())
                && Boolean.FALSE.equals(reg.getTakeOnRoad())) {
            return "Chỉ thi sa hình (H) nhưng kỳ chưa có ca thực hành";
        }
        if (Boolean.FALSE.equals(reg.getTakeTheory())
                && Boolean.FALSE.equals(reg.getTakePractical())
                && !Boolean.FALSE.equals(reg.getTakeOnRoad())) {
            return "Chỉ thi đường trường (Đ) nhưng kỳ chưa có ca đường trường";
        }
        return "Không ghi danh được ca thi phù hợp với nội dung SH";
    }

    private int resolveExamId(int sessionId) {
        if (sessionId <= 0) {
            return 0;
        }
        SessionDTO session = sessionQuery.findBySessionId(sessionId);
        return session != null ? session.getExamId() : 0;
    }

    private boolean isDuplicateInExam(String govId, int examId, int sessionId) {
        if (examId > 0 && registrationService.findCandidateIdByGovIdAndExam(govId, examId) != null) {
            return true;
        }
        return registrationService.findCandidateIdByGovIdAndSession(govId, sessionId) != null;
    }
}
