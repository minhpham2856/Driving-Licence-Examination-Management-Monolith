package examstaff.dto;

/**
 * KPI thủ tục của staff trên màn audit: số hồ sơ hoàn tất và tổng lệ phí đã thu.
 *
 * Vai trò:
 * Hiển thị nhanh hiệu suất bàn thủ tục của user đang xem nhật ký (audit.jsp).
 *
 * Ai tạo / tiêu thụ:
 * AuditLogDAOImpl → gắn vào StaffAuditPageViewDTO.getProcedureKpi() → AuditServlet.
 */
public class StaffProcedureKpiDTO {
    private final int completedCount;
    private final double totalFees;

    /**
     * Tạo KPI bất biến cho một staff.
     * @param completedCount số hồ sơ thủ tục đã hoàn tất
     * @param totalFees      tổng lệ phí tương ứng
     */
    public StaffProcedureKpiDTO(int completedCount, double totalFees) { this.completedCount = completedCount; this.totalFees = totalFees; }

    /** Số hồ sơ thủ tục staff đã hoàn tất (trong phạm vi KPI). */
    public int getCompletedCount() { return completedCount; }

    /** Tổng lệ phí đã thu gắn các hồ sơ hoàn tất. */
    public double getTotalFees() { return totalFees; }
}
