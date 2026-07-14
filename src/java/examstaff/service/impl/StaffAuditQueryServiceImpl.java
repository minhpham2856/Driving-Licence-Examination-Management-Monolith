package examstaff.service.impl;

import examstaff.dao.AuditLogDAO;
import examstaff.dao.impl.AuditLogDAOImpl;
import examstaff.dto.staff.StaffProcedureKpiDTO;
import examstaff.dto.user.AuditDTO;
import examstaff.service.StaffAuditQueryService;

import java.util.ArrayList;
import java.util.List;

/** Implementation: truy vấn audit log và KPI thủ tục qua {@link AuditLogDAO}. */
public class StaffAuditQueryServiceImpl implements StaffAuditQueryService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    /**
     * Đếm số dòng audit theo nhân viên và ngày.
     *
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return tổng số bản ghi
     */
    @Override
    public int countLogsByUserAndDate(int userId, String filterDate) {
        return auditLogDAO.getLogsCountByUserAndDate(userId, filterDate);
    }

    /**
     * Lấy audit theo nhân viên/ngày có phân trang.
     *
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @param page       trang hiện tại
     * @param pageSize   kích thước trang
     * @return danh sách audit của trang
     */
    @Override
    public List<AuditDTO> listLogsByUserAndDatePaginated(int userId, String filterDate, int page, int pageSize) {
        try {
            return auditLogDAO.getLogsByUserAndDatePaginated(userId, filterDate, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lấy toàn bộ audit theo nhân viên và ngày (không phân trang).
     *
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return danh sách audit
     */
    @Override
    public List<AuditDTO> listLogsByUserAndDate(int userId, String filterDate) {
        try {
            if (filterDate != null && !filterDate.isBlank()) {
                return auditLogDAO.getLogsByUserAndDate(userId, filterDate);
            }
            return auditLogDAO.getLogsByUserAndDate(userId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Tính KPI thủ tục (số hoàn thành, tổng phí, …) của nhân viên trong ngày.
     *
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return KPI thủ tục
     */
    @Override
    public StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate) {
        return auditLogDAO.getStaffProcedureKpi(userId, filterDate);
    }
}
