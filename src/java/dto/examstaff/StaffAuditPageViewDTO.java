package dto.examstaff;

import dto.staff.StaffProcedureKpiDTO;
import dto.user.AuditDTO;
import util.examstaff.AllocationStageUtil;

import java.util.List;

public class StaffAuditPageViewDTO {

    private List<AuditDTO> personalLogs;
    private AllocationStageUtil.PageSlice<AuditDTO> pageSlice;
    private StaffProcedureKpiDTO procedureKpi;
    private String filterKey;
    private int page;

    public List<AuditDTO> getPersonalLogs() {
        return personalLogs;
    }

    public void setPersonalLogs(List<AuditDTO> personalLogs) {
        this.personalLogs = personalLogs;
    }

    public AllocationStageUtil.PageSlice<AuditDTO> getPageSlice() {
        return pageSlice;
    }

    public void setPageSlice(AllocationStageUtil.PageSlice<AuditDTO> pageSlice) {
        this.pageSlice = pageSlice;
    }

    public StaffProcedureKpiDTO getProcedureKpi() {
        return procedureKpi;
    }

    public void setProcedureKpi(StaffProcedureKpiDTO procedureKpi) {
        this.procedureKpi = procedureKpi;
    }

    public String getFilterKey() {
        return filterKey;
    }

    public void setFilterKey(String filterKey) {
        this.filterKey = filterKey;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
