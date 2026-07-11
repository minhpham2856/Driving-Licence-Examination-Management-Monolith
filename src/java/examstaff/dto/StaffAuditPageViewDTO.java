package examstaff.dto;

import examstaff.dto.staff.StaffProcedureKpiDTO;
import examstaff.dto.user.AuditDTO;
import examstaff.util.AllocationStageHelper;

import java.util.List;

public class StaffAuditPageViewDTO {

    private List<AuditDTO> personalLogs;
    private AllocationStageHelper.PageSlice<AuditDTO> pageSlice;
    private StaffProcedureKpiDTO procedureKpi;
    private String filterKey;
    private int page;

    public List<AuditDTO> getPersonalLogs() {
        return personalLogs;
    }

    public void setPersonalLogs(List<AuditDTO> personalLogs) {
        this.personalLogs = personalLogs;
    }

    public AllocationStageHelper.PageSlice<AuditDTO> getPageSlice() {
        return pageSlice;
    }

    public void setPageSlice(AllocationStageHelper.PageSlice<AuditDTO> pageSlice) {
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
