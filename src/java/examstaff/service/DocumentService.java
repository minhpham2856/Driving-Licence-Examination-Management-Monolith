package examstaff.service;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamReportStatsDTO;
import examstaff.dto.ExamSummaryDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Facade xuất tài liệu / báo cáo kỳ thi ra file cho nhân viên kỳ thi.
 *
 * Phạm vi hiện tại:
 * - <b>Excel</b> — {@code exportExamReport} xuất báo cáo tổng hợp kỳ thi
 *       (thí sinh, thống kê, vi phạm, phí) ra {@link OutputStream}
 * Dữ liệu đầu vào ({@link ExamSummaryDTO}, {@link ExamRegistrationDTO},
 * {@link ExamReportStatsDTO}) do {@link ExamStaffViewService} / servlet chuẩn bị sẵn.
 */
public interface DocumentService {

    /**
     * Xuất báo cáo tổng hợp kỳ thi ra luồng (Excel).
     * @param out          luồng ghi file
     * @param exam         tóm tắt kỳ thi
     * @param candidates   danh sách thí sinh
     * @param stats        thống kê đã tính
     * @param exporterName tên người xuất
     * @throws IOException nếu ghi thất bại
     */
    void exportExamReport(OutputStream out, ExamSummaryDTO exam,
            List<ExamRegistrationDTO> candidates, ExamReportStatsDTO stats,
            String exporterName) throws IOException;
}
