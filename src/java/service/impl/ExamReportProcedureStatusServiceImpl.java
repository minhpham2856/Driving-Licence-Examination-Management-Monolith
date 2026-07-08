package service.impl;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ExamReportProcedureStatusDTO;
import service.CandidatePhotoService;
import service.ExamReportProcedureStatusService;

import java.util.ArrayList;
import java.util.List;

public class ExamReportProcedureStatusServiceImpl implements ExamReportProcedureStatusService {

    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

    @Override
    public ExamReportProcedureStatusDTO analyze(List<ExamRegistrationDTO> candidates, String webRoot) {
        ExamReportProcedureStatusDTO status = new ExamReportProcedureStatusDTO();
        List<String> missingPhotoSbds = new ArrayList<>();
        List<ExamRegistrationDTO> missingPhotoCandidates = new ArrayList<>();
        List<ExamRegistrationDTO> procedurePendingCandidates = new ArrayList<>();

        int missingPhotoCount = 0;
        int procedureCompleteCount = 0;
        int procedurePendingCount = 0;

        if (candidates != null) {
            for (ExamRegistrationDTO reg : candidates) {
                boolean valid = photoService.resolveCapturedPhoto(webRoot, reg);
                if (reg.isAbsent()) {
                    continue;
                }
                if (reg.isProcedureComplete()) {
                    procedureCompleteCount++;
                    continue;
                }
                procedurePendingCount++;
                procedurePendingCandidates.add(reg);
                if (!valid) {
                    missingPhotoCount++;
                    missingPhotoSbds.add(reg.getSbd() + " — " + reg.getName());
                    missingPhotoCandidates.add(reg);
                }
            }
        }

        status.setMissingPhotoCount(missingPhotoCount);
        status.setProcedureCompleteCount(procedureCompleteCount);
        status.setProcedurePendingCount(procedurePendingCount);
        status.setMissingPhotoSbds(missingPhotoSbds);
        status.setMissingPhotoCandidates(missingPhotoCandidates);
        status.setProcedurePendingCandidates(procedurePendingCandidates);
        return status;
    }
}
