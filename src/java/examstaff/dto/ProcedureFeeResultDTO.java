package examstaff.dto;

import shared.model.Fee;

import java.util.List;

public class ProcedureFeeResultDTO {

    private List<Fee> feeLines;
    private double feeTotal;
    private boolean feesFromPayment;

    public List<Fee> getFeeLines() {
        return feeLines;
    }

    public void setFeeLines(List<Fee> feeLines) {
        this.feeLines = feeLines;
    }

    public double getFeeTotal() {
        return feeTotal;
    }

    public void setFeeTotal(double feeTotal) {
        this.feeTotal = feeTotal;
    }

    public boolean isFeesFromPayment() {
        return feesFromPayment;
    }

    public void setFeesFromPayment(boolean feesFromPayment) {
        this.feesFromPayment = feesFromPayment;
    }
}
