package examstaff.util;

import shared.model.Fee;
import shared.model.Payment;

import java.util.List;

/** Cách tính tổng lệ phí thủ tục thống nhất giữa nhật ký (audit) và báo cáo cuối ngày. */
public final class ProcedureFeeTotals {

    private ProcedureFeeTotals() {
    }

    /**
     * Ưu tiên tổng các dòng {@link Fee}; nếu tổng ≤ 0 thì dùng {@code paymentTotalAmount}.
     *
     * @param feeLines           danh sách dòng phí
     * @param paymentTotalAmount tổng trên Payment (fallback)
     * @return số tiền đã thu (≥ 0)
     */
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

    /**
     * Tính tiền đã thu từ {@link Payment} và danh sách Fee.
     *
     * @param payment  bản ghi thanh toán (null → 0)
     * @param feeLines dòng phí chi tiết
     * @return số tiền đã thu
     */
    public static double resolvePaidAmount(Payment payment, List<Fee> feeLines) {
        if (payment == null) {
            return 0;
        }
        return resolvePaidAmount(feeLines, payment.getTotalAmount());
    }
}
