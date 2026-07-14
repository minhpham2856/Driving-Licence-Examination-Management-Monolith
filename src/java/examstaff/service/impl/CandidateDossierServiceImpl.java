package examstaff.service.impl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateDossierViewDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.service.CandidateDossierService;
import examstaff.service.CandidatePhotoService;
import examstaff.service.CandidateQueueQueryService;
import examstaff.service.ExamStaffExamQueryService;
import examstaff.service.ProcedureFeeQueryService;
import examstaff.util.DossierLabelUtil;
import examstaff.util.LicenseClassRules;

import java.util.List;
import java.util.Locale;

/** Implementation: dựng view hồ sơ thí sinh (phí, ảnh, nhãn hạng). */
public class CandidateDossierServiceImpl implements CandidateDossierService {

    private final CandidateQueueQueryService queueQueryService = new CandidateQueueQueryServiceImpl();
    private final ExamStaffExamQueryService examQueryService = new ExamStaffExamQueryServiceImpl();
    private final ProcedureFeeQueryService procedureFeeQueryService = new ProcedureFeeQueryServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

    /**
     * Tải view hồ sơ thí sinh theo kỳ thi và SBD.
     *
     * @param examId  mã kỳ thi
     * @param sbd     số báo danh
     * @param webRoot thư mục gốc web (ảnh, tài liệu)
     * @return DTO hồ sơ hiển thị, hoặc null nếu không có
     */
    @Override
    public CandidateDossierViewDTO loadDossier(int examId, String sbd, String webRoot) {
        CandidateDossierViewDTO view = new CandidateDossierViewDTO();
        if (sbd == null || sbd.isBlank()) {
            return view;
        }

        ExamRegistrationDTO profile = queueQueryService.findByExamIdAndSbd(examId, sbd.trim());
        if (profile == null) {
            return view;
        }

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
        view.setProfile(profile);
        view.setExam(examSummary);
        view.setFees(fees);
        view.setHasPhotoFile(photoService.photoFileExists(webRoot, profile.getPhotoUrl()));
        view.setDossierTitle(DossierLabelUtil.resolveTitle(licenseCode));
        view.setDossierSubtitle(DossierLabelUtil.resolveSubtitle(licenseCode));
        return view;
    }
}
