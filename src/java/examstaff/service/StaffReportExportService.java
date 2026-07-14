package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamReportStatsDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Xuất báo cáo tổng hợp kỳ thi (file) cho nhân viên.
 */
public interface StaffReportExportService {

    /**
     * Ghi báo cáo kỳ thi ra luồng xuất (ví dụ PDF/Excel tùy triển khai).
     *
     * @param out          luồng ghi file
     * @param exam         thông tin tóm tắt kỳ thi
     * @param candidates   danh sách thí sinh trong báo cáo
     * @param stats        thống kê đã tính sẵn
     * @param exporterName tên người xuất báo cáo
     * @throws IOException nếu ghi file thất bại
     */
    void exportExamReport(OutputStream out, ExamSummaryDTO exam,
            List<ExamRegistrationDTO> candidates, ExamReportStatsDTO stats,
            String exporterName) throws IOException;
}
