package examstaff.service.impl.support.view;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamReportProcedureStatusDTO;
import java.util.ArrayList;
import java.util.List;

/** Implementation: phân tích trạng thái thủ tục (ảnh / hoàn tất) cho báo cáo. */
public class ExamReportProcedureStatusServiceImpl {

    private final CandidatePhotoServiceImpl photoService = new CandidatePhotoServiceImpl();

    /**
     * Phân loại / tổng hợp trạng thái thủ tục (ảnh, thanh toán, …) trong danh sách.
     *
     * @param candidates danh sách thí sinh
     * @param webRoot    thư mục gốc web để kiểm tra ảnh vật lý nếu cần
     * @return DTO trạng thái thủ tục báo cáo
     */
    public ExamReportProcedureStatusDTO analyze(List<ExamRegistrationDTO> candidates, String webRoot) {
        ExamReportProcedureStatusDTO status = new ExamReportProcedureStatusDTO();
        List<String> missingPhotoSbds = new ArrayList<>();
        List<ExamRegistrationDTO> missingPhotoCandidates = new ArrayList<>();
        List<ExamRegistrationDTO> procedurePendingCandidates = new ArrayList<>();

        int missingPhotoCount = 0;
        int procedureCompleteCount = 0;
        int procedurePendingCount = 0;

        // Mutate: duyệt danh sách → phân loại hoàn tất / pending / thiếu ảnh
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

        // Result
        status.setMissingPhotoCount(missingPhotoCount);
        status.setProcedureCompleteCount(procedureCompleteCount);
        status.setProcedurePendingCount(procedurePendingCount);
        status.setMissingPhotoSbds(missingPhotoSbds);
        status.setMissingPhotoCandidates(missingPhotoCandidates);
        status.setProcedurePendingCandidates(procedurePendingCandidates);
        return status;
    }
}
