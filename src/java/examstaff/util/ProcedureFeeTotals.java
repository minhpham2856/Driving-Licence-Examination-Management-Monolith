package examstaff.util;

import shared.model.Fee;
import shared.model.Payment;

import java.util.List;

/**
 * Utility tính tổng lệ phí thủ tục thống nhất — ưu tiên cộng dòng Fee,
 * fallback Payment.getTotalAmount() khi tổng dòng ≤ 0.
 *
 * Vai trò trong luồng examstaff:
 * Một payment có thể có nhiều dòng phí chi tiết (Fee) hoặc chỉ tổng trên Payment.
 * Audit cuối ngày và báo cáo thu phí cần cùng một quy tắc tính “tiền đã thu” để số khớp
 * giữa StaffAuditExportServiceImpl và ReportServlet.
 *
 * Cách hoạt động:
 * - double) — feeLines rỗng → paymentTotal nếu > 0;
 *       cộng Fee; lineSum > 0 → lineSum; không thì fallback payment.
 * - List) — payment null → 0; ủy quyền overload trên.
 *
 * Ai gọi:
 * ProcedureFeeQueryServiceImpl, StaffAuditExportServiceImpl, ReportServlet —
 * tổng hợp doanh thu lệ phí thủ tục theo ca/ngày.
 */
public final class ProcedureFeeTotals {

    /** Không cho khởi tạo — chỉ dùng static. */
    private ProcedureFeeTotals() {
    }

    /**
     * Ưu tiên tổng các dòng Fee; nếu tổng ≤ 0 thì dùng paymentTotalAmount.
     * <p>
 *
     * Luồng:
     * - feeLines null/rỗng → trả paymentTotalAmount nếu > 0, ngược lại 0
     * - Cộng amount các dòng Fee
     * - lineSum > 0 → trả lineSum; không thì fallback paymentTotalAmount (≥ 0)
     * @param feeLines           danh sách dòng phí
     * @param paymentTotalAmount tổng trên Payment (fallback)
     * @return số tiền đã thu (≥ 0)
     */
    public static double resolvePaidAmount(List<Fee> feeLines, double paymentTotalAmount) {
        // Bước 1: không có dòng phí → chỉ dùng tổng Payment
        if (feeLines == null || feeLines.isEmpty()) {
            return paymentTotalAmount > 0 ? paymentTotalAmount : 0;
        }
        // Bước 2: cộng chi tiết các dòng Fee
        double lineSum = feeLines.stream().mapToDouble(Fee::getAmount).sum();
        if (lineSum > 0) {
            return lineSum;
        }
        // Bước 3: dòng phí không dương → fallback tổng Payment
        return paymentTotalAmount > 0 ? paymentTotalAmount : 0;
    }

    /**
     * Tính tiền đã thu từ Payment và danh sách Fee.
     * <p>
     * payment null → 0; ngược lại ủy quyền double).
     * @param payment  bản ghi thanh toán (null → 0)
     * @param feeLines dòng phí chi tiết
     * @return số tiền đã thu
     */
    public static double resolvePaidAmount(Payment payment, List<Fee> feeLines) {
        // Bước 1: thiếu Payment → không có tiền thu
        if (payment == null) {
            return 0;
        }
        // Bước 2: ủy quyền overload có tổng Payment
        return resolvePaidAmount(feeLines, payment.getTotalAmount());
    }
}
