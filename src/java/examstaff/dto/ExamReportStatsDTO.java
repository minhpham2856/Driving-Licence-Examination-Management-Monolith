package examstaff.dto;

import java.util.List;
import java.util.Map;

/**
 * Thống kê kết quả kỳ thi cho màn báo cáo ExamStaff ({@code report.jsp} / bản in).
 *
 * <h2>Vai trò</h2>
 * Tổng hợp số liệu đậu/trượt/vắng/đình chỉ, theo phần lý thuyết–thực hành,
 * thống kê theo hạng bằng và danh sách lỗi / infractions.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * {@code ExamReportStatsServiceImpl} → {@code ReportServlet} (và luồng in báo cáo).
 */
public class ExamReportStatsDTO {

    private int totalCandidates;
    private int examCompletedCount;
    private int passedCount;
    private int failedCount;
    private int absentCount;
    private int suspendedCount;
    private double passRate;
    private List<Map<String, Object>> licenseStats;
    private int theoryCount;
    private int theoryPassed;
    private int theoryFailed;
    private int practicalCount;
    private int practicalPassed;
    private int practicalFailed;
    private List<Map<String, Object>> infractions;

    /** Tổng số thí sinh trong kỳ (mẫu thống kê). */
    public int getTotalCandidates() {
        return totalCandidates;
    }

    /** Gán tổng số thí sinh. */
    public void setTotalCandidates(int totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    /** Số thí sinh đã hoàn tất kỳ (theo định nghĩa báo cáo). */
    public int getExamCompletedCount() {
        return examCompletedCount;
    }

    /** Gán số đã hoàn tất kỳ. */
    public void setExamCompletedCount(int examCompletedCount) {
        this.examCompletedCount = examCompletedCount;
    }

    /** Số đậu cuối cùng. */
    public int getPassedCount() {
        return passedCount;
    }

    /** Gán số đậu. */
    public void setPassedCount(int passedCount) {
        this.passedCount = passedCount;
    }

    /** Số trượt. */
    public int getFailedCount() {
        return failedCount;
    }

    /** Gán số trượt. */
    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    /** Số vắng. */
    public int getAbsentCount() {
        return absentCount;
    }

    /** Gán số vắng. */
    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    /** Số đình chỉ. */
    public int getSuspendedCount() {
        return suspendedCount;
    }

    /** Gán số đình chỉ. */
    public void setSuspendedCount(int suspendedCount) {
        this.suspendedCount = suspendedCount;
    }

    /** Tỷ lệ đậu (0–100 hoặc 0–1 tùy service tính). */
    public double getPassRate() {
        return passRate;
    }

    /** Gán tỷ lệ đậu. */
    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    /** Thống kê theo hạng bằng (các map key/value do service dựng). */
    public List<Map<String, Object>> getLicenseStats() {
        return licenseStats;
    }

    /** Gán thống kê theo hạng bằng. */
    public void setLicenseStats(List<Map<String, Object>> licenseStats) {
        this.licenseStats = licenseStats;
    }

    /** Số thí sinh tham gia phần lý thuyết. */
    public int getTheoryCount() {
        return theoryCount;
    }

    /** Gán số dự thi lý thuyết. */
    public void setTheoryCount(int theoryCount) {
        this.theoryCount = theoryCount;
    }

    /** Số đậu lý thuyết. */
    public int getTheoryPassed() {
        return theoryPassed;
    }

    /** Gán số đậu lý thuyết. */
    public void setTheoryPassed(int theoryPassed) {
        this.theoryPassed = theoryPassed;
    }

    /** Số trượt lý thuyết. */
    public int getTheoryFailed() {
        return theoryFailed;
    }

    /** Gán số trượt lý thuyết. */
    public void setTheoryFailed(int theoryFailed) {
        this.theoryFailed = theoryFailed;
    }

    /** Số thí sinh tham gia phần thực hành. */
    public int getPracticalCount() {
        return practicalCount;
    }

    /** Gán số dự thi thực hành. */
    public void setPracticalCount(int practicalCount) {
        this.practicalCount = practicalCount;
    }

    /** Số đậu thực hành. */
    public int getPracticalPassed() {
        return practicalPassed;
    }

    /** Gán số đậu thực hành. */
    public void setPracticalPassed(int practicalPassed) {
        this.practicalPassed = practicalPassed;
    }

    /** Số trượt thực hành. */
    public int getPracticalFailed() {
        return practicalFailed;
    }

    /** Gán số trượt thực hành. */
    public void setPracticalFailed(int practicalFailed) {
        this.practicalFailed = practicalFailed;
    }

    /** Danh sách lỗi / vi phạm (map mô tả) trên báo cáo. */
    public List<Map<String, Object>> getInfractions() {
        return infractions;
    }

    /** Gán danh sách infractions. */
    public void setInfractions(List<Map<String, Object>> infractions) {
        this.infractions = infractions;
    }
}
