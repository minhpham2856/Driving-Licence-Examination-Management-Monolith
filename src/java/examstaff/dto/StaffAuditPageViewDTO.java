package examstaff.dto;

import examstaff.dto.StaffProcedureKpiDTO;
import examstaff.dto.AuditDTO;
import examstaff.service.impl.support.allocation.AllocationStageHelper;

import java.util.List;

/**
 * View-model trang nhật ký audit của staff ExamStaff.
 *
 * Vai trò:
 * Gom logs cá nhân, slice phân trang, KPI thủ tục và filter để {@code AuditServlet} bind
 * lên {@code audit.jsp}.
 *
 * Ai tạo / tiêu thụ:
 * Tạo bởi {@code StaffAuditPageServiceImpl}; tiêu thụ bởi {@code AuditServlet} → {@code audit.jsp}.
 */
public class StaffAuditPageViewDTO {

    private List<AuditDTO> personalLogs;
    private AllocationStageHelper.PageSlice<AuditDTO> pageSlice;
    private StaffProcedureKpiDTO procedureKpi;
    private String filterKey;
    private int page;

    /** Danh sách bản ghi audit của staff (trang hiện tại hoặc full tùy service). */
    public List<AuditDTO> getPersonalLogs() {
        return personalLogs;
    }

    /** Gán danh sách log audit cá nhân. */
    public void setPersonalLogs(List<AuditDTO> personalLogs) {
        this.personalLogs = personalLogs;
    }

    /** Slice phân trang dùng chung helper allocation (total/page/items). */
    public AllocationStageHelper.PageSlice<AuditDTO> getPageSlice() {
        return pageSlice;
    }

    /** Gán thông tin phân trang log. */
    public void setPageSlice(AllocationStageHelper.PageSlice<AuditDTO> pageSlice) {
        this.pageSlice = pageSlice;
    }

    /** KPI thủ tục (số hồ sơ hoàn tất, tổng lệ phí) hiển thị đầu trang audit. */
    public StaffProcedureKpiDTO getProcedureKpi() {
        return procedureKpi;
    }

    /** Gán KPI thủ tục cho header audit. */
    public void setProcedureKpi(StaffProcedureKpiDTO procedureKpi) {
        this.procedureKpi = procedureKpi;
    }

    /** Khóa filter đang áp dụng (loại action / entity…). */
    public String getFilterKey() {
        return filterKey;
    }

    /** Gán khóa filter audit. */
    public void setFilterKey(String filterKey) {
        this.filterKey = filterKey;
    }

    /** Số trang hiện tại (1-based tùy quy ước service). */
    public int getPage() {
        return page;
    }

    /** Gán số trang đang xem. */
    public void setPage(int page) {
        this.page = page;
    }
}
