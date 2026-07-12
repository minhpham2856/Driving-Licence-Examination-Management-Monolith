package examstaff.dto;

import shared.model.Fee;
import shared.model.Payment;

import java.util.List;

public class ReportPaymentSummaryDTO {

    private Payment payment;
    private List<Fee> feeLines;
    private double lineTotal;

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public List<Fee> getFeeLines() {
        return feeLines;
    }

    public void setFeeLines(List<Fee> feeLines) {
        this.feeLines = feeLines;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }
}

