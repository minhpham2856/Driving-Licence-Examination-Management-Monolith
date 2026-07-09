package service.impl;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateDossierViewDTO;
import dto.examstaff.ProcedureFeeResultDTO;
import service.CandidateDossierService;
import service.CandidatePhotoService;
import service.CandidateQueueQueryService;
import service.ExamStaffSessionQueryService;
import service.ProcedureFeeQueryService;
import util.examstaff.DossierLabelUtil;
import util.examstaff.LicenseClassRules;

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
        SessionDTO examSession = sessionQueryService.findBySessionId(profile.getExamSessionId());

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
