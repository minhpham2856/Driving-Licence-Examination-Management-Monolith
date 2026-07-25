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

/**
 * Dựng view hồ sơ thí sinh cho trang dossier — phí, hạng GPLX, ảnh và nhãn hiển thị.
 * <p>
 * Orchestrate CandidateQueueQueryServiceImpl, ExamStaffExamQueryServiceImpl,
 * ProcedureFeeQueryServiceImpl và CandidatePhotoServiceImpl; không gọi servlet.
 *
 * Luồng loadDossier:
 * - Validate SBD; tìm ExamRegistrationDTO theo kỳ + SBD
 * - Chuẩn hoá đường dẫn ảnh (CandidatePhotoServiceImpl.normalizePhotoPaths)
 * - Resolve phí thủ tục (ProcedureFeeQueryServiceImpl)
 * - Chuẩn hóa mã hạng qua LicenseClassRules; gắn nhãn ExamStaffLabels
 * - Đóng gói examstaff.dto.CandidateDossierViewDTO
 *
 * Điểm gọi:
 * CandidateDossierServlet qua ExamStaffViewServiceImpl.
 */
public class CandidateDossierServiceImpl {

    private final CandidateQueueQueryServiceImpl queueQueryService = new CandidateQueueQueryServiceImpl();
    private final ExamStaffExamQueryServiceImpl examQueryService = new ExamStaffExamQueryServiceImpl();
    private final ProcedureFeeQueryServiceImpl procedureFeeQueryService = new ProcedureFeeQueryServiceImpl();
    private final CandidatePhotoServiceImpl photoService = new CandidatePhotoServiceImpl();

    /**
     * Tải view hồ sơ thí sinh theo kỳ thi và SBD.
     * @param examId mã kỳ thi
     * @param sbd    số báo danh
     * @return DTO hồ sơ hiển thị, hoặc null nếu không có
     */
    public CandidateDossierViewDTO loadDossier(int examId, String sbd) {
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
        photoService.normalizePhotoPaths(List.of(profile));
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
        view.setHasPhotoFile(photoService.photoFileExists(profile.getPhotoUrl()));
        view.setDossierTitle(ExamStaffLabels.resolveTitle(licenseCode));
        view.setDossierSubtitle(ExamStaffLabels.resolveSubtitle(licenseCode));
        return view;
    }
}
