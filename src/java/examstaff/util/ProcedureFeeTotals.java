package examstaff.util;

import model.Fee;
import model.Payment;

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

    public static final String SQL_PAID_AMOUNT =
            "CASE WHEN fees.lineTotal IS NULL OR fees.lineTotal = 0 THEN p.TotalAmount ELSE fees.lineTotal END";

    public static final String SQL_FEE_LINES_JOIN = """
            LEFT JOIN (
                SELECT pf.PaymentId, SUM(f.Amount) AS lineTotal
                FROM Payment_Fee pf
                INNER JOIN Fee f ON f.FeeId = pf.FeeId
                GROUP BY pf.PaymentId
            ) fees ON fees.PaymentId = p.PaymentId
            """;

    public static final String SQL_PROCEDURE_PHOTO_OK = """
            c.PhotoImageUrl IS NOT NULL
            AND LEN(LTRIM(RTRIM(c.PhotoImageUrl))) > 0
            """;

    public static final String SQL_PAYMENT_ACTIVE = "p.PaymentStatus IN (N'Completed', N'Paid', N'Hoàn tất')";
}
