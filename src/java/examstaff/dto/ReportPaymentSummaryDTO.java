package examstaff.dto;

import shared.model.Fee;
import shared.model.Payment;

import java.util.List;

/**
 * Tóm tắt thanh toán + dòng lệ phí cho báo cáo / hồ sơ (gói {@link Payment} + {@link Fee}).
 *
 * <h2>Vai trò</h2>
 * Hiển thị chi tiết một khoản thanh toán kèm các dòng phí và tổng dòng trên report/dossier.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * {@code ProcedureFeeQueryServiceImpl} → báo cáo thanh toán / view liên quan report.
 */
public class ReportPaymentSummaryDTO {

    private Payment payment;
    private List<Fee> feeLines;
    private double lineTotal;

    /** Bản ghi Payment (số tiền, trạng thái, thời điểm…). */
    public Payment getPayment() {
        return payment;
    }

    /** Gán bản ghi thanh toán. */
    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    /** Các dòng Fee gắn payment / báo cáo. */
    public List<Fee> getFeeLines() {
        return feeLines;
    }

    /** Gán danh sách dòng phí. */
    public void setFeeLines(List<Fee> feeLines) {
        this.feeLines = feeLines;
    }

    /** Tổng các dòng phí trong summary. */
    public double getLineTotal() {
        return lineTotal;
    }

    /** Gán tổng tiền các dòng. */
    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }
}
