package examstaff.service;

import examstaff.dto.AuditDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Xuất file nhật ký thao tác và KPI liên quan của nhân viên.
 */
public interface StaffAuditExportService {

    /**
     * Ghi nhật ký audit ra luồng xuất kèm chỉ số thủ tục đã hoàn thành.
     *
     * @param out                 luồng ghi file
     * @param logs                danh sách audit
     * @param completedProcedures số thủ tục hoàn thành
     * @param totalFees           tổng phí
     * @param staffName           tên nhân viên
     * @param filterDateLabel     nhãn ngày lọc hiển thị trên file
     * @throws IOException nếu ghi file thất bại
     */
    void exportAuditLog(OutputStream out, List<AuditDTO> logs, int completedProcedures,
            double totalFees, String staffName, String filterDateLabel) throws IOException;
}
