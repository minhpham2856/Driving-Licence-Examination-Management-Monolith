package examstaff.service;

import examstaff.dto.AuditDTO;
import examstaff.dto.StaffAuditPageViewDTO;
import examstaff.dto.StaffProcedureKpiDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Facade nhật ký audit nhân viên kỳ thi: trang lọc, truy vấn, xuất file và ghi log hành động.
 *
 * Bốn nhóm chức năng:
 * - <b>Page</b> — buildPage ghép view lọc ngày + phân trang + KPI thủ tục
 * - <b>Query</b> — countLogsByUserAndDate, listLogsByUserAndDatePaginated,
 *       listLogsByUserAndDate, getStaffProcedureKpi
 * - <b>Export</b> — exportAuditLog ghi nhật ký ra luồng (Excel/CSV)
 * - <b>Log</b> — logAction ghi hành động staff (có hoặc không recordId)
 * Presentation gọi facade này thay vì truy cập trực tiếp AuditLogDAO.
 */
public interface AuditService {

    /**
     * Ghép view trang nhật ký audit (lọc ngày, phân trang, KPI).
     * @param userId               mã nhân viên
     * @param filterDate           ngày lọc (yyyy-MM-dd hoặc rỗng)
     * @param page                 trang (1-based)
     * @param pageSize             số dòng mỗi trang
     * @param filterContextChanged có thay đổi bộ lọc (reset trang)
     * @return DTO trang audit
     */
    StaffAuditPageViewDTO buildPage(int userId, String filterDate, int page, int pageSize,
            boolean filterContextChanged);

    /**
     * Đếm số bản ghi audit theo user và ngày.
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return số bản ghi
     */
    int countLogsByUserAndDate(int userId, String filterDate);

    /**
     * Danh sách audit phân trang theo user và ngày.
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @param page       trang
     * @param pageSize   kích thước trang
     * @return danh sách log
     */
    List<AuditDTO> listLogsByUserAndDatePaginated(int userId, String filterDate, int page, int pageSize);

    /**
     * Toàn bộ log theo user và ngày (không phân trang).
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return danh sách log
     */
    List<AuditDTO> listLogsByUserAndDate(int userId, String filterDate);

    /**
     * KPI thủ tục hoàn tất / tổng phí của nhân viên trong ngày.
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return KPI
     */
    StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate);

    /**
     * Xuất file nhật ký audit ra luồng.
     * @param out                 luồng ghi
     * @param logs                danh sách log
     * @param completedProcedures số thủ tục hoàn tất
     * @param totalFees           tổng phí
     * @param staffName           tên nhân viên
     * @param filterDateLabel     nhãn ngày lọc
     * @throws IOException nếu ghi thất bại
     */
    void exportAuditLog(OutputStream out, List<AuditDTO> logs, int completedProcedures,
            double totalFees, String staffName, String filterDateLabel) throws IOException;

    /**
     * Ghi một action audit gắn recordId.
     * @param userId   mã nhân viên
     * @param action   mã / tên hành động
     * @param details  mô tả chi tiết
     * @param recordId mã bản ghi liên quan
     */
    void logAction(int userId, String action, String details, int recordId);

    /**
     * Ghi action audit không gắn record (recordId = 0).
     * @param userId  mã nhân viên
     * @param action  mã / tên hành động
     * @param details mô tả chi tiết
     */
    void logAction(int userId, String action, String details);
}
