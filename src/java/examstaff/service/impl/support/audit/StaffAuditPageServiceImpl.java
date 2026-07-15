package examstaff.service.impl.support.audit;
import examstaff.service.impl.support.allocation.AllocationStageHelper;

import examstaff.dto.StaffProcedureKpiDTO;
import examstaff.dto.AuditDTO;
import examstaff.dto.StaffAuditPageViewDTO;
import examstaff.util.ExamStaffLabels;
import examstaff.util.AuditFilterHelper;

import java.util.ArrayList;
import java.util.List;

/** Implementation: dựng view trang nhật ký audit (phân trang + KPI). */
public class StaffAuditPageServiceImpl {

    private final StaffAuditQueryServiceImpl auditQueryService;

    /** Wiring mặc định. */
    public StaffAuditPageServiceImpl() {
        this(new StaffAuditQueryServiceImpl());
    }

    /** Inject query service từ composition root. */
    public StaffAuditPageServiceImpl(StaffAuditQueryServiceImpl auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    /**
     * Xây dựng view trang audit theo lọc ngày và phân trang.
     *
     * @param userId               mã nhân viên
     * @param filterDate           ngày lọc (chuỗi nghiệp vụ; có thể rỗng)
     * @param page                 trang hiện tại (1-based)
     * @param pageSize             số dòng mỗi trang
     * @param filterContextChanged true nếu bộ lọc vừa đổi (reset ngữ cảnh phân trang)
     * @return DTO hiển thị trang audit
     */
    public StaffAuditPageViewDTO buildPage(int userId, String filterDate, int page, int pageSize,
            boolean filterContextChanged) {
        StaffAuditPageViewDTO view = new StaffAuditPageViewDTO();
        // Validate / chuẩn hoá bộ lọc
        String filterKey = AuditFilterHelper.normalizeFilterKey(filterDate);
        view.setFilterKey(filterKey);

        if (filterContextChanged) {
            page = 1;
        }

        // Load: tổng dòng → clamp page
        int totalLogs = auditQueryService.countLogsByUserAndDate(userId, filterDate);
        int totalPages = totalLogs <= 0 ? 0 : (int) Math.ceil((double) totalLogs / pageSize);
        if (totalPages > 0 && page > totalPages) {
            page = 1;
        } else if (totalPages == 0) {
            page = 1;
        }
        view.setPage(page);

        // Mutate: tải slice + nhãn VI + KPI
        List<AuditDTO> personalLogs = loadLogs(userId, filterDate, page, pageSize);
        applyVietnameseLabels(personalLogs);
        StaffProcedureKpiDTO procedureKpi = auditQueryService.getStaffProcedureKpi(userId, filterDate);

        // Result
        view.setPersonalLogs(personalLogs);
        view.setPageSlice(new AllocationStageHelper.PageSlice<>(personalLogs, page, pageSize, totalLogs));
        view.setProcedureKpi(procedureKpi);
        return view;
    }

    /**
     * Tải logs theo user/ngày; lỗi thì list rỗng.
     *
     * @param userId     mã nhân viên
     * @param filterDate ngày lọc
     * @param page       trang 1-based
     * @param pageSize   kích thước trang
     * @return danh sách audit trang hiện tại
     */
    private List<AuditDTO> loadLogs(int userId, String filterDate, int page, int pageSize) {
        try {
            return auditQueryService.listLogsByUserAndDatePaginated(userId, filterDate, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Gắn nhãn tiếng Việt cho từng {@link AuditDTO}.
     *
     * @param logs danh sách audit (có thể null)
     */
    private static void applyVietnameseLabels(List<AuditDTO> logs) {
        if (logs == null) {
            return;
        }
        for (AuditDTO log : logs) {
            ExamStaffLabels.applyDisplayLabels(log);
        }
    }
}
