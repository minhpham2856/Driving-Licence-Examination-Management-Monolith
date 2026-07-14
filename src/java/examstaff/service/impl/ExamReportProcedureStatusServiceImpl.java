package examstaff.service.impl;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamReportProcedureStatusDTO;
import examstaff.service.CandidatePhotoService;
import examstaff.service.ExamReportProcedureStatusService;

import java.util.ArrayList;
import java.util.List;

/** Implementation: phân tích trạng thái thủ tục (ảnh / hoàn tất) cho báo cáo. */
public class ExamReportProcedureStatusServiceImpl implements ExamReportProcedureStatusService {

    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

    /** {@inheritDoc} */
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
                    missingPhotoSbds.add(reg.getSbd() + " - " + reg.getName());
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
