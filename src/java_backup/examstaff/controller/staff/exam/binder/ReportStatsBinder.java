package examstaff.controller.staff.exam.binder;

import examstaff.dto.ExamReportStatsDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class ReportStatsBinder {

    private ReportStatsBinder() {
    }

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
        request.setAttribute("a1Count", stats.getA1Count());
        request.setAttribute("a1Completed", stats.getA1Completed());
        request.setAttribute("a1Passed", stats.getA1Passed());
        request.setAttribute("a1Failed", stats.getA1Failed());
        request.setAttribute("aCount", stats.getACount());
        request.setAttribute("aCompleted", stats.getACompleted());
        request.setAttribute("aPassed", stats.getAPassed());
        request.setAttribute("aFailed", stats.getAFailed());
        request.setAttribute("b1Count", stats.getB1Count());
        request.setAttribute("b1Completed", stats.getB1Completed());
        request.setAttribute("b1Passed", stats.getB1Passed());
        request.setAttribute("b1Failed", stats.getB1Failed());
        request.setAttribute("theoryCount", stats.getTheoryCount());
        request.setAttribute("theoryPassed", stats.getTheoryPassed());
        request.setAttribute("theoryFailed", stats.getTheoryFailed());
        request.setAttribute("practicalCount", stats.getPracticalCount());
        request.setAttribute("practicalPassed", stats.getPracticalPassed());
        request.setAttribute("practicalFailed", stats.getPracticalFailed());
        request.setAttribute("infractions", stats.getInfractions());
    }
}
