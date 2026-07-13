package model;

public class Fee {

    private int feeId;
    private String feeName;
    private String feeType;
    private boolean isActive;

    public Fee() {
    }

    public Fee(int feeId, String feeName, String feeType, boolean isActive) {
        this.feeId = feeId;
        this.feeName = feeName;
        this.feeType = feeType;
        this.isActive = isActive;
    }

    public int getFeeId() {
        return feeId;
    }

    public void setFeeId(int feeId) {
        this.feeId = feeId;
    }

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
