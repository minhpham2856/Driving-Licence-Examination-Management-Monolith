package Controllers.Staff.ExamStaff;

public final class ReportExportStats {

    public final int totalCandidates;
    public final int examCompletedCount;
    public final int passedCount;
    public final int failedCount;
    public final int absentCount;
    public final double passRate;
    public final int theoryCount;
    public final int theoryPassed;
    public final int theoryFailed;
    public final int practicalCount;
    public final int practicalPassed;
    public final int practicalFailed;
    public final int roadCount;
    public final int roadPassed;
    public final int roadFailed;

    public ReportExportStats(int totalCandidates, int examCompletedCount, int passedCount, int failedCount,
            int absentCount, double passRate, int theoryCount, int theoryPassed, int theoryFailed,
            int practicalCount, int practicalPassed, int practicalFailed,
            int roadCount, int roadPassed, int roadFailed) {
        this.totalCandidates = totalCandidates;
        this.examCompletedCount = examCompletedCount;
        this.passedCount = passedCount;
        this.failedCount = failedCount;
        this.absentCount = absentCount;
        this.passRate = passRate;
        this.theoryCount = theoryCount;
        this.theoryPassed = theoryPassed;
        this.theoryFailed = theoryFailed;
        this.practicalCount = practicalCount;
        this.practicalPassed = practicalPassed;
        this.practicalFailed = practicalFailed;
        this.roadCount = roadCount;
        this.roadPassed = roadPassed;
        this.roadFailed = roadFailed;
    }

    public static ReportExportStats fromRequest(jakarta.servlet.http.HttpServletRequest request) {
        return new ReportExportStats(
                intAttr(request, "totalCandidates"),
                intAttr(request, "examCompletedCount"),
                intAttr(request, "passedCount"),
                intAttr(request, "failedCount"),
                intAttr(request, "absentCount"),
                doubleAttr(request, "passRate"),
                intAttr(request, "theoryCount"),
                intAttr(request, "theoryPassed"),
                intAttr(request, "theoryFailed"),
                intAttr(request, "practicalCount"),
                intAttr(request, "practicalPassed"),
                intAttr(request, "practicalFailed"),
                intAttr(request, "roadCount"),
                intAttr(request, "roadPassed"),
                intAttr(request, "roadFailed"));
    }

    private static int intAttr(jakarta.servlet.http.HttpServletRequest request, String key) {
        Object v = request.getAttribute(key);
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return 0;
    }

    private static double doubleAttr(jakarta.servlet.http.HttpServletRequest request, String key) {
        Object v = request.getAttribute(key);
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        return 0;
    }
}
