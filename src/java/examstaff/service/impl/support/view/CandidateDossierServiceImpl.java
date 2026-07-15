package examstaff.service.impl.support.view;
import examstaff.service.impl.support.shared.LicenseClassRules;
import examstaff.service.impl.support.procedure.ProcedureFeeQueryServiceImpl;
import examstaff.service.impl.support.shared.ExamStaffExamQueryServiceImpl;
import examstaff.service.impl.support.call.CandidateQueueQueryServiceImpl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CandidateDossierViewDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.util.ExamStaffLabels;

import java.util.List;
import java.util.Locale;

/** Implementation: dựng view hồ sơ thí sinh (phí, ảnh, nhãn hạng). */
public class CandidateDossierServiceImpl {

    private final CandidateQueueQueryServiceImpl queueQueryService = new CandidateQueueQueryServiceImpl();
    private final ExamStaffExamQueryServiceImpl examQueryService = new ExamStaffExamQueryServiceImpl();
    private final ProcedureFeeQueryServiceImpl procedureFeeQueryService = new ProcedureFeeQueryServiceImpl();
    private final CandidatePhotoServiceImpl photoService = new CandidatePhotoServiceImpl();

    /**
     * Tải view hồ sơ thí sinh theo kỳ thi và SBD.
     *
     * @param examId  mã kỳ thi
     * @param sbd     số báo danh
     * @param webRoot thư mục gốc web (ảnh, tài liệu)
     * @return DTO hồ sơ hiển thị, hoặc null nếu không có
     */
    public CandidateDossierViewDTO loadDossier(int examId, String sbd, String webRoot) {
        CandidateDossierViewDTO view = new CandidateDossierViewDTO();
        // Validate
        if (sbd == null || sbd.isBlank()) {
            return view;
        }

        // Load: hồ sơ theo kỳ + SBD
        ExamRegistrationDTO profile = queueQueryService.findByExamIdAndSbd(examId, sbd.trim());
        if (profile == null) {
            return view;
        }

        // Mutate: ảnh, phí, hạng, nhãn dossier
        photoService.normalizePhotoPaths(webRoot, List.of(profile));
        ProcedureFeeResultDTO fees = procedureFeeQueryService.resolveProcedureFees(profile);
        ExamSummaryDTO examSummary = examQueryService.findByExamId(profile.getExamId());

        String rawLicenseCode = profile.getLicenseCode() != null ? profile.getLicenseCode() : profile.getClazz();
        String normalized = LicenseClassRules.normalizeManaged(rawLicenseCode);
        if (normalized == null || normalized.isBlank()) {
            normalized = rawLicenseCode != null ? rawLicenseCode.trim().toUpperCase(Locale.ROOT) : null;
        }
        profile.setLicenseCode(normalized);
        String licenseCode = normalized;
        // Result
        view.setProfile(profile);
        view.setExam(examSummary);
        view.setFees(fees);
        view.setHasPhotoFile(photoService.photoFileExists(webRoot, profile.getPhotoUrl()));
        view.setDossierTitle(ExamStaffLabels.resolveTitle(licenseCode));
        view.setDossierSubtitle(ExamStaffLabels.resolveSubtitle(licenseCode));
        return view;
    }
}
