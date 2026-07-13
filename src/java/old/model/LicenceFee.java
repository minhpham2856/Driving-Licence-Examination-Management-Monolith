package model;

public class LicenceFee {

    private int licenceFeeId;
    private Integer licenceId;
    private int feeId;
    private Double amount;
    private Licence licence;
    private Fee fee;

    public LicenceFee() {
    }

    public LicenceFee(int licenceFeeId, Integer licenceId, int feeId, Double amount) {
        this.licenceFeeId = licenceFeeId;
        this.licenceId = licenceId;
        this.feeId = feeId;
        this.amount = amount;
    }

    public int getLicenceFeeId() {
        return licenceFeeId;
    }

    public void setLicenceFeeId(int licenceFeeId) {
        this.licenceFeeId = licenceFeeId;
    }

    public Integer getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(Integer licenceId) {
        this.licenceId = licenceId;
    }

    public int getFeeId() {
        return feeId;
    }

    public void setFeeId(int feeId) {
        this.feeId = feeId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Licence getLicence() {
        return licence;
    }

    public void setLicence(Licence licence) {
        this.licence = licence;
    }

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }
}
