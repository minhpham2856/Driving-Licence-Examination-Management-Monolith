package examstaff.dao;


import shared.model.Audit;
import examstaff.dto.AuditDTO;

import examstaff.dto.StaffProcedureKpiDTO;

import java.util.List;

/**
 * DAO cho thao tác với nhật ký kiểm tra (AuditLog) trong hệ thống.
 * Cung cấp các phương thức ghi nhật ký, truy vấn nhật ký theo người dùng,
 * ngày tháng, hỗ trợ phân trang và thống kê KPI cho cán bộ.
 */
public interface AuditLogDAO {

    /**
     * Ghi một bản ghi nhật ký kiểm tra mới.
     *
     * @param log đối tượng Audit chứa thông tin nhật ký
     * @return true nếu ghi thành công
     */
    boolean insert(Audit log);

    /**
     * Lấy danh sách nhật ký của người dùng theo ngày cụ thể.
     *
     * @param userId mã người dùng
     * @param dateStr ngày cần lọc (định dạng yyyy-MM-dd)
     * @return danh sách AuditDTO
     */
    List<AuditDTO> getLogsByUserAndDate(int userId, String dateStr);

    /**
     * Lấy danh sách nhật ký của người dùng theo ngày có phân trang.
     *
     * @param userId   mã người dùng
     * @param dateStr  ngày cần lọc (định dạng yyyy-MM-dd)
     * @param page     số trang (bắt đầu từ 1)
     * @param pageSize số lượng bản ghi trên mỗi trang
     * @return danh sách AuditDTO theo trang
     */
    List<AuditDTO> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize);

    /**
     * Đếm số lượng nhật ký của người dùng theo ngày.
     *
     * @param userId  mã người dùng
     * @param dateStr ngày cần lọc (định dạng yyyy-MM-dd)
     * @return số lượng bản ghi nhật ký
     */
    int getLogsCountByUserAndDate(int userId, String dateStr);

    /**
     * Lấy chỉ số KPI thủ tục của cán bộ (số thí sinh đã có ảnh + thanh toán do cán bộ đó thu).
     *
     * @param userId     mã cán bộ
     * @param filterDate ngày lọc (định dạng yyyy-MM-dd) hoặc null để lấy tất cả
     * @return StaffProcedureKpiDTO chứa thông tin KPI
     */
    StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate);
}
