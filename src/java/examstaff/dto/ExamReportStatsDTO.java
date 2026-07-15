package examstaff.dto;

import java.util.List;
import java.util.Map;

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

    public int getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(int totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public int getExamCompletedCount() {
        return examCompletedCount;
    }

    public void setExamCompletedCount(int examCompletedCount) {
        this.examCompletedCount = examCompletedCount;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(int passedCount) {
        this.passedCount = passedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public int getSuspendedCount() {
        return suspendedCount;
    }

    public void setSuspendedCount(int suspendedCount) {
        this.suspendedCount = suspendedCount;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public List<Map<String, Object>> getLicenseStats() {
        return licenseStats;
    }

    public void setLicenseStats(List<Map<String, Object>> licenseStats) {
        this.licenseStats = licenseStats;
    }

    public int getTheoryCount() {
        return theoryCount;
    }

    public void setTheoryCount(int theoryCount) {
        this.theoryCount = theoryCount;
    }

    public int getTheoryPassed() {
        return theoryPassed;
    }

    public void setTheoryPassed(int theoryPassed) {
        this.theoryPassed = theoryPassed;
    }

    public int getTheoryFailed() {
        return theoryFailed;
    }

    public void setTheoryFailed(int theoryFailed) {
        this.theoryFailed = theoryFailed;
    }

    public int getPracticalCount() {
        return practicalCount;
    }

    public void setPracticalCount(int practicalCount) {
        this.practicalCount = practicalCount;
    }

    public int getPracticalPassed() {
        return practicalPassed;
    }

    public void setPracticalPassed(int practicalPassed) {
        this.practicalPassed = practicalPassed;
    }

    public int getPracticalFailed() {
        return practicalFailed;
    }

    public void setPracticalFailed(int practicalFailed) {
        this.practicalFailed = practicalFailed;
    }

    public List<Map<String, Object>> getInfractions() {
        return infractions;
    }

    public void setInfractions(List<Map<String, Object>> infractions) {
        this.infractions = infractions;
    }
}
