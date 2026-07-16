package examstaff.util;

import shared.model.Fee;
import shared.model.Payment;

import java.util.List;

/** Cách tính tổng lệ phí thủ tục thống nhất giữa nhật ký (audit) và báo cáo cuối ngày. */
public final class ProcedureFeeTotals {

    private ProcedureFeeTotals() {
    }

    public static double resolvePaidAmount(List<Fee> feeLines, double paymentTotalAmount) {
        if (feeLines == null || feeLines.isEmpty()) {
            return paymentTotalAmount > 0 ? paymentTotalAmount : 0;
        }
        double lineSum = feeLines.stream().mapToDouble(Fee::getAmount).sum();
        if (lineSum > 0) {
            return lineSum;
        }
        return paymentTotalAmount > 0 ? paymentTotalAmount : 0;
    }

    public static double resolvePaidAmount(Payment payment, List<Fee> feeLines) {
        if (payment == null) {
            return 0;
        }
        return resolvePaidAmount(feeLines, payment.getTotalAmount());
    }
}
