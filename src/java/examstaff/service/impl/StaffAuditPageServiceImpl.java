package examstaff.service.impl;

import examstaff.dto.staff.StaffProcedureKpiDTO;
import examstaff.dto.user.AuditDTO;
import examstaff.dto.StaffAuditPageViewDTO;
import examstaff.service.StaffAuditPageService;
import examstaff.service.StaffAuditQueryService;
import examstaff.util.AllocationStageHelper;
import examstaff.util.AuditExportLabels;
import examstaff.util.AuditFilterHelper;

import java.util.ArrayList;
import java.util.List;

/** Implementation: dựng view trang nhật ký audit (phân trang + KPI). */
public class StaffAuditPageServiceImpl implements StaffAuditPageService {

    private final StaffAuditQueryService auditQueryService;

    /** Wiring mặc định. */
    public StaffAuditPageServiceImpl() {
        this(new StaffAuditQueryServiceImpl());
    }

    /** Inject query service từ composition root. */
    public StaffAuditPageServiceImpl(StaffAuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    /** {@inheritDoc} */
    @Override
    public StaffAuditPageViewDTO buildPage(int userId, String filterDate, int page, int pageSize,
            boolean filterContextChanged) {
        StaffAuditPageViewDTO view = new StaffAuditPageViewDTO();
        String filterKey = AuditFilterHelper.normalizeFilterKey(filterDate);
        view.setFilterKey(filterKey);

        if (filterContextChanged) {
            page = 1;
        }

        int totalLogs = auditQueryService.countLogsByUserAndDate(userId, filterDate);
        int totalPages = totalLogs <= 0 ? 0 : (int) Math.ceil((double) totalLogs / pageSize);
        if (totalPages > 0 && page > totalPages) {
            page = 1;
        } else if (totalPages == 0) {
            page = 1;
        }
        view.setPage(page);

        List<AuditDTO> personalLogs = loadLogs(userId, filterDate, page, pageSize);
        applyVietnameseLabels(personalLogs);
        StaffProcedureKpiDTO procedureKpi = auditQueryService.getStaffProcedureKpi(userId, filterDate);

        view.setPersonalLogs(personalLogs);
        view.setPageSlice(new AllocationStageHelper.PageSlice<>(personalLogs, page, pageSize, totalLogs));
        view.setProcedureKpi(procedureKpi);
        return view;
    }

    /** Tải logs theo user/ngày; lỗi thì list rỗng. */
    private List<AuditDTO> loadLogs(int userId, String filterDate, int page, int pageSize) {
        try {
            return auditQueryService.listLogsByUserAndDatePaginated(userId, filterDate, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Gắn nhãn tiếng Việt cho từng AuditDTO. */
    private static void applyVietnameseLabels(List<AuditDTO> logs) {
        if (logs == null) {
            return;
        }
        for (AuditDTO log : logs) {
            AuditExportLabels.applyDisplayLabels(log);
        }
    }
}
