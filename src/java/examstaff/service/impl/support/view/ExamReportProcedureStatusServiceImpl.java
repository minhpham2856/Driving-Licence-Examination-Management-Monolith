package examstaff.service.impl.support.view;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamReportProcedureStatusDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * Phân tích trạng thái thủ tục (ảnh, hoàn tất, đang chờ) cho báo cáo kỳ thi.
 * <p>
 * Duyệt {@link ExamRegistrationDTO}; dùng {@link CandidatePhotoServiceImpl#resolveCapturedPhoto}
 * để xác định thiếu ảnh. Trả {@link examstaff.dto.ExamReportProcedureStatusDTO}.
 *
 * Phân loại thí sinh:
 * - <b>Hoàn tất thủ tục</b> — {@code isProcedureComplete()}; bỏ qua vắng mặt
 * - <b>Đang chờ</b> — chưa hoàn tất; gom vào {@code procedurePendingCandidates}
 * - <b>Thiếu ảnh</b> — trong nhóm chờ, {@code resolveCapturedPhoto} = false;
 *       ghi SBD + tên vào danh sách cảnh báo
 *
 * Điểm gọi:
 * {@code ReportServlet} qua {@code ExamStaffViewServiceImpl} khi dựng báo cáo tổng hợp.
 */
public class ExamReportProcedureStatusServiceImpl {

    private final CandidatePhotoServiceImpl photoService = new CandidatePhotoServiceImpl();

    /**
     * Phân loại / tổng hợp trạng thái thủ tục (ảnh, thanh toán, …) trong danh sách.
     * @param candidates danh sách thí sinh
     * @return DTO trạng thái thủ tục báo cáo
     */
    public ExamReportProcedureStatusDTO analyze(List<ExamRegistrationDTO> candidates) {
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
                boolean valid = photoService.resolveCapturedPhoto(reg);
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
