package admin.model;

import java.math.BigDecimal;

/** Licence_Fee = mức tiền của 1 phí theo 1 hạng GPLX (LicenceId có thể null = áp dụng chung). */
public class LicenceFeeView {
    private int licenceFeeId;
    private Integer licenceId;   // null = áp dụng chung
    private String licenceClass;
    private int feeId;
    private String feeName;
    private String feeType;
    private BigDecimal amount;

    public int getLicenceFeeId() { return licenceFeeId; }
    public void setLicenceFeeId(int v) { this.licenceFeeId = v; }
    public int getId() { return licenceFeeId; }
    public Integer getLicenceId() { return licenceId; }
    public void setLicenceId(Integer v) { this.licenceId = v; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String v) { this.licenceClass = v; }
    public String getLicenceLabel() { return (licenceId == null) ? "Áp dụng chung" : licenceClass; }
    public int getFeeId() { return feeId; }
    public void setFeeId(int v) { this.feeId = v; }
    public String getFeeName() { return feeName; }
    public void setFeeName(String v) { this.feeName = v; }
    public String getFeeType() { return feeType; }
    public void setFeeType(String v) { this.feeType = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
}
