package examstaff.service.impl;

import examstaff.dto.AuditDTO;
import examstaff.dto.StaffAuditPageViewDTO;
import examstaff.dto.StaffProcedureKpiDTO;
import examstaff.service.AuditService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import examstaff.service.impl.support.audit.StaffAuditPageServiceImpl;
import examstaff.service.impl.support.audit.StaffAuditQueryServiceImpl;
import examstaff.service.impl.support.audit.StaffAuditExportServiceImpl;
import examstaff.service.impl.support.audit.StaffAuditLogServiceImpl;

/**
 * Implementation AuditService: facade nhật ký audit nhân viên kỳ thi.
 *
 * Ủy quyền support services:
 * - StaffAuditPageServiceImpl — buildPage
 * - StaffAuditQueryServiceImpl — đếm / liệt kê log, KPI thủ tục
 * - StaffAuditExportServiceImpl — exportAuditLog
 * - StaffAuditLogServiceImpl — logAction
 * Constructor mặc định tự wiring; constructor inject dùng cho unit test / composition root.
 */
public class AuditServiceImpl implements AuditService {

    private final StaffAuditPageServiceImpl page;
    private final StaffAuditQueryServiceImpl query;
    private final StaffAuditExportServiceImpl export;
    private final StaffAuditLogServiceImpl log;

    /** Wiring mặc định. */
    public AuditServiceImpl() {
        this.query = new StaffAuditQueryServiceImpl();
        this.page = new StaffAuditPageServiceImpl(this.query);
        this.export = new StaffAuditExportServiceImpl();
        this.log = new StaffAuditLogServiceImpl();
    }

    /**
     * Inject dependencies (test / composition).
     * @param page   dịch vụ trang audit
     * @param query  truy vấn log / KPI
     * @param export xuất file
     * @param log    ghi action
     */
    public AuditServiceImpl(StaffAuditPageServiceImpl page, StaffAuditQueryServiceImpl query,
            StaffAuditExportServiceImpl export, StaffAuditLogServiceImpl log) {
        this.page = page;
        this.query = query;
        this.export = export;
        this.log = log;
    }

    /**
     * Ủy quyền sang StaffAuditPageServiceImpl.buildPage.
     * @param userId               mã nhân viên
     * @param filterDate           ngày lọc
     * @param pageNum              trang
     * @param pageSize             kích thước trang
     * @param filterContextChanged có đổi bộ lọc
     * @return DTO trang audit
     */
    @Override
    public StaffAuditPageViewDTO buildPage(int userId, String filterDate, int pageNum, int pageSize,
            boolean filterContextChanged) {
        return page.buildPage(userId, filterDate, pageNum, pageSize, filterContextChanged);
    }

    /**
     * Ủy quyền sang StaffAuditQueryServiceImpl.countLogsByUserAndDate.
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return số bản ghi
     */
    @Override
    public int countLogsByUserAndDate(int userId, String filterDate) {
        return query.countLogsByUserAndDate(userId, filterDate);
    }

    /**
     * Ủy quyền sang StaffAuditQueryServiceImpl.listLogsByUserAndDatePaginated.
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @param page       trang
     * @param pageSize   kích thước trang
     * @return danh sách log
     */
    @Override
    public List<AuditDTO> listLogsByUserAndDatePaginated(int userId, String filterDate, int page, int pageSize) {
        return query.listLogsByUserAndDatePaginated(userId, filterDate, page, pageSize);
    }

    /**
     * Ủy quyền sang StaffAuditQueryServiceImpl.listLogsByUserAndDate.
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return danh sách log
     */
    @Override
    public List<AuditDTO> listLogsByUserAndDate(int userId, String filterDate) {
        return query.listLogsByUserAndDate(userId, filterDate);
    }

    /**
     * Ủy quyền sang StaffAuditQueryServiceImpl.getStaffProcedureKpi.
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @return KPI thủ tục
     */
    @Override
    public StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate) {
        return query.getStaffProcedureKpi(userId, filterDate);
    }

    /**
     * Ủy quyền sang StaffAuditExportServiceImpl.exportAuditLog.
     * @param out                 luồng ghi
     * @param logs                danh sách log
     * @param completedProcedures số thủ tục hoàn tất
     * @param totalFees           tổng phí
     * @param staffName           tên nhân viên
     * @param filterDateLabel     nhãn ngày
     * @throws IOException nếu ghi thất bại
     */
    @Override
    public void exportAuditLog(OutputStream out, List<AuditDTO> logs, int completedProcedures,
            double totalFees, String staffName, String filterDateLabel) throws IOException {
        export.exportAuditLog(out, logs, completedProcedures, totalFees, staffName, filterDateLabel);
    }

    /**
     * Ủy quyền sang StaffAuditLogServiceImpl.logAction kèm recordId.
     * @param userId   mã nhân viên
     * @param action   hành động
     * @param details  chi tiết
     * @param recordId mã bản ghi
     */
    @Override
    public void logAction(int userId, String action, String details, int recordId) {
        log.logAction(userId, action, details, recordId);
    }

    /**
     * Ghi action không gắn record (ủy quyền với recordId = 0).
     * @param userId  mã nhân viên
     * @param action  hành động
     * @param details chi tiết
     */
    @Override
    public void logAction(int userId, String action, String details) {
        log.logAction(userId, action, details, 0);
    }
}
