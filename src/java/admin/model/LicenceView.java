package admin.model;

/** Licence = hạng GPLX. */
public class LicenceView {
    private int licenceId;
    private String licenceClass;
    private String description;
    private int minimumAge;
    private int validForYears;
    private int feeCount;

    public int getLicenceId() { return licenceId; }
    public void setLicenceId(int v) { this.licenceId = v; }
    public int getId() { return licenceId; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String v) { this.licenceClass = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public int getMinimumAge() { return minimumAge; }
    public void setMinimumAge(int v) { this.minimumAge = v; }
    public int getValidForYears() { return validForYears; }
    public void setValidForYears(int v) { this.validForYears = v; }
    public int getFeeCount() { return feeCount; }
    public void setFeeCount(int v) { this.feeCount = v; }
}
