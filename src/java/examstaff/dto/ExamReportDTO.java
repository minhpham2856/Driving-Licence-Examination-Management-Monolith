package examstaff.dto;

import java.util.List;

// End-of-day exam report for a single exam, built by ExamViewService.
// Aggregates candidate results and deduction reasons so the JSP only renders.
public class ExamReportDTO {

    private int totalCandidates;
    private int completedCount;
    private int testingCount;
    private int pendingCount;
    private int passedCount;
    private int failedCount;
    private double passRate;

    // Per-licence breakdown (A1/A2 group vs B2 group).
    private int a1Count;
    private int a1Passed;
    private int a1Failed;
    private int b2Count;
    private int b2Passed;
    private int b2Failed;

    private List<CandidateRowDTO> candidateRows;
    private List<InfractionDTO> topInfractions;

    public int getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(int totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getTestingCount() {
        return testingCount;
    }

    public void setTestingCount(int testingCount) {
        this.testingCount = testingCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
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

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public int getA1Count() {
        return a1Count;
    }

    public void setA1Count(int a1Count) {
        this.a1Count = a1Count;
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

    public int getB2Count() {
        return b2Count;
    }

    public void setB2Count(int b2Count) {
        this.b2Count = b2Count;
    }

    public int getB2Passed() {
        return b2Passed;
    }

    public void setB2Passed(int b2Passed) {
        this.b2Passed = b2Passed;
    }

    public int getB2Failed() {
        return b2Failed;
    }

    public void setB2Failed(int b2Failed) {
        this.b2Failed = b2Failed;
    }

    public List<CandidateRowDTO> getCandidateRows() {
        return candidateRows;
    }

    public void setCandidateRows(List<CandidateRowDTO> candidateRows) {
        this.candidateRows = candidateRows;
    }

    public List<InfractionDTO> getTopInfractions() {
        return topInfractions;
    }

    public void setTopInfractions(List<InfractionDTO> topInfractions) {
        this.topInfractions = topInfractions;
    }
}
