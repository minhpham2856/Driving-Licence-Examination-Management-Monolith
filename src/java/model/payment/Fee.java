package model.payment;

public class Fee {

    private int feeId;
    private String feeName;
    private String feeType;
    private double amount;
    private boolean isActive;

    public Fee() {
    }

    public Fee(int feeId, String feeName, String feeType, double amount, boolean isActive) {
        this.feeId = feeId;
        this.feeName = feeName;
        this.feeType = feeType;
        this.amount = amount;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
