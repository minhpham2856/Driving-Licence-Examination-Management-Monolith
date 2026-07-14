package examstaff.controller.staff.exam.binder;

import examstaff.dto.ExamReportStatsDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Bind thống kê báo cáo kỳ thi từ {@link ExamReportStatsDTO} lên request attributes.
 */
public final class ReportStatsBinder {

    private ReportStatsBinder() {
    }

    /**
     * Set counts (total/passed/failed/absent/suspended), passRate, licenseStats,
     * theory/practical breakdown và infractions.
     */
    public static void bind(HttpServletRequest request, ExamReportStatsDTO stats) {
        if (request == null || stats == null) {
            return;
        }
        request.setAttribute("totalCandidates", stats.getTotalCandidates());
        request.setAttribute("examCompletedCount", stats.getExamCompletedCount());
        request.setAttribute("passedCount", stats.getPassedCount());
        request.setAttribute("failedCount", stats.getFailedCount());
        request.setAttribute("absentCount", stats.getAbsentCount());
        request.setAttribute("suspendedCount", stats.getSuspendedCount());
        request.setAttribute("passRate", stats.getPassRate());
        request.setAttribute("licenseStats", stats.getLicenseStats());
        request.setAttribute("theoryCount", stats.getTheoryCount());
        request.setAttribute("theoryPassed", stats.getTheoryPassed());
        request.setAttribute("theoryFailed", stats.getTheoryFailed());
        request.setAttribute("practicalCount", stats.getPracticalCount());
        request.setAttribute("practicalPassed", stats.getPracticalPassed());
        request.setAttribute("practicalFailed", stats.getPracticalFailed());
        request.setAttribute("infractions", stats.getInfractions());
    }
}
