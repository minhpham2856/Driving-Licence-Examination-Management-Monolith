package admin.model;

/** Fee = danh mục loại phí chung. */
public class FeeView {
    private int feeId;
    private String feeName;
    private String feeType;
    private boolean active;

    public int getFeeId() { return feeId; }
    public void setFeeId(int v) { this.feeId = v; }
    public int getId() { return feeId; }
    public String getFeeName() { return feeName; }
    public void setFeeName(String v) { this.feeName = v; }
    public String getFeeType() { return feeType; }
    public void setFeeType(String v) { this.feeType = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public String getStatus() { return active ? "active" : "inactive"; }
}
