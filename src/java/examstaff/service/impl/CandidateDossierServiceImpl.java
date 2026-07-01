package examstaff.service.impl;

import dto.ExamSummaryDTO;
import dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateDossierViewDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.service.CandidateDossierService;
import examstaff.service.CandidatePhotoService;
import examstaff.service.CandidateQueueQueryService;
import examstaff.service.ExamStaffSessionQueryService;
import examstaff.service.ProcedureFeeQueryService;
import examstaff.util.DossierLabelUtil;
import examstaff.util.LicenseClassRules;

import java.util.List;
import java.util.Locale;

public class CandidateDossierServiceImpl implements CandidateDossierService {

    private final CandidateQueueQueryService queueQueryService = new CandidateQueueQueryServiceImpl();
    private final ExamStaffSessionQueryService sessionQueryService = new ExamStaffSessionQueryServiceImpl();
    private final ProcedureFeeQueryService procedureFeeQueryService = new ProcedureFeeQueryServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

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
        ExamSummaryDTO examSession = sessionQueryService.findByExamId(profile.getExamId());

        String rawLicenseCode = profile.getLicenseCode() != null ? profile.getLicenseCode() : profile.getClazz();
        String normalized = LicenseClassRules.normalizeManaged(rawLicenseCode);
        if (normalized == null || normalized.isBlank()) {
            normalized = rawLicenseCode != null ? rawLicenseCode.trim().toUpperCase(Locale.ROOT) : null;
        }
        profile.setLicenseCode(normalized);
        String licenseCode = normalized;
        view.setProfile(profile);
        view.setExamSession(examSession);
        view.setFees(fees);
        view.setHasPhotoFile(photoService.photoFileExists(webRoot, profile.getPhotoUrl()));
        view.setDossierTitle(DossierLabelUtil.resolveTitle(licenseCode));
        view.setDossierSubtitle(DossierLabelUtil.resolveSubtitle(licenseCode));
        return view;
    }
}
