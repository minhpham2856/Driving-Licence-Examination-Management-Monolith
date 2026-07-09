package dto.examstaff;

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
    private int a1Count;
    private int a1Completed;
    private int a1Passed;
    private int a1Failed;
    private int aCount;
    private int aCompleted;
    private int aPassed;
    private int aFailed;
    private int b1Count;
    private int b1Completed;
    private int b1Passed;
    private int b1Failed;
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

    public int getA1Count() {
        return a1Count;
    }

    public void setA1Count(int a1Count) {
        this.a1Count = a1Count;
    }

    public int getA1Completed() {
        return a1Completed;
    }

    public void setA1Completed(int a1Completed) {
        this.a1Completed = a1Completed;
    }

    public int getA1Passed() {
        return a1Passed;
    }

    public void setA1Passed(int a1Passed) {
        this.a1Passed = a1Passed;
    }

    public int getA1Failed() {
        return a1Failed;
    }

    public void setA1Failed(int a1Failed) {
        this.a1Failed = a1Failed;
    }

    public int getACount() {
        return aCount;
    }

    public void setACount(int aCount) {
        this.aCount = aCount;
    }

    public int getACompleted() {
        return aCompleted;
    }

    public void setACompleted(int aCompleted) {
        this.aCompleted = aCompleted;
    }

    public int getAPassed() {
        return aPassed;
    }

    public void setAPassed(int aPassed) {
        this.aPassed = aPassed;
    }

    public int getAFailed() {
        return aFailed;
    }

    public void setAFailed(int aFailed) {
        this.aFailed = aFailed;
    }

    public int getB1Count() {
        return b1Count;
    }

    public void setB1Count(int b1Count) {
        this.b1Count = b1Count;
    }

    public int getB1Completed() {
        return b1Completed;
    }

    public void setB1Completed(int b1Completed) {
        this.b1Completed = b1Completed;
    }

    public int getB1Passed() {
        return b1Passed;
    }

    public void setB1Passed(int b1Passed) {
        this.b1Passed = b1Passed;
    }

    public int getB1Failed() {
        return b1Failed;
    }

    public void setB1Failed(int b1Failed) {
        this.b1Failed = b1Failed;
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
