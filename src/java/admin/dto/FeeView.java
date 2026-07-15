package admin.dto;

import java.sql.Timestamp;


public class FeeView {

    private int feeId;
    private String feeName;
    private String feeType;      // theory | practical | rent | license
    private java.math.BigDecimal amount;
    private boolean active;      // Fee.IsActive
    private Timestamp updatedAt;
    private Integer licenceId;   // nullable
    private String licenceClass; // joined from Licence

    // JSP aliases (exam-fee.jsp uses fee.code / name / licenceClass / category / amount / status / lastUpdated / id)
    public int getId() { return feeId; }
    public String getCode() { return String.format("PH-%04d", feeId); }
    public String getName() { return feeName; }
    public String getCategory() { return feeType; }
    public String getStatus() { return active ? "active" : "inactive"; }
    public Timestamp getLastUpdated() { return updatedAt; }

    public int getFeeId() { return feeId; }
    public void setFeeId(int v) { this.feeId = v; }
    public String getFeeName() { return feeName; }
    public void setFeeName(String v) { this.feeName = v; }
    public String getFeeType() { return feeType; }
    public void setFeeType(String v) { this.feeType = v; }
    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal v) { this.amount = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp v) { this.updatedAt = v; }
    public Integer getLicenceId() { return licenceId; }
    public void setLicenceId(Integer v) { this.licenceId = v; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String v) { this.licenceClass = v; }
}
