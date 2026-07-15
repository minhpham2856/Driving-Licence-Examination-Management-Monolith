package examstaff.service;

import examstaff.dto.StaffProcedureKpiDTO;
import examstaff.dto.AuditDTO;

import java.util.List;

/**
 * Truy vấn nhật ký thao tác và KPI thủ tục của nhân viên.
 */
public interface StaffAuditQueryService {

    /**
     * Đếm số dòng audit theo nhân viên và ngày.
     *
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return tổng số bản ghi
     */
    int countLogsByUserAndDate(int userId, String filterDate);

    /**
     * Lấy audit theo nhân viên/ngày có phân trang.
     *
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @param page       trang hiện tại
     * @param pageSize   kích thước trang
     * @return danh sách audit của trang
     */
    List<AuditDTO> listLogsByUserAndDatePaginated(int userId, String filterDate, int page, int pageSize);

    /**
     * Lấy toàn bộ audit theo nhân viên và ngày (không phân trang).
     *
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return danh sách audit
     */
    List<AuditDTO> listLogsByUserAndDate(int userId, String filterDate);

    /**
     * Tính KPI thủ tục (số hoàn thành, tổng phí, …) của nhân viên trong ngày.
     *
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return KPI thủ tục
     */
    StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate);
}
