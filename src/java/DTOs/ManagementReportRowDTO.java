package DTOs;

public class ManagementReportRowDTO {

    private String periodLabel;
    private String licenceClass;
    private int totalCount;
    private int absentCount;
    private int passCount;
    private int failCount;
    private int pendingCount;

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public String getSessionName() {
        return periodLabel;
    }

    public String getLicenceClass() {
        return licenceClass;
    }

    public String getLicenseClass() {
        return licenceClass;
    }

    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public double getPassRate() {
        int evaluated = passCount + failCount;
        return evaluated == 0 ? 0.0 : round(passCount * 100.0 / evaluated);
    }

    public double getPassShare() {
        return share(passCount);
    }

    public double getFailShare() {
        return share(failCount);
    }

    public double getAbsentShare() {
        return share(absentCount);
    }

    public double getPendingShare() {
        return share(pendingCount);
    }

    private double share(int value) {
        return totalCount == 0 ? 0.0 : round(value * 100.0 / totalCount);
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
