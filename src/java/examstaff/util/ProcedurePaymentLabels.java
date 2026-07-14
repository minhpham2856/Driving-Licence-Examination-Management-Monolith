package examstaff.util;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.ProcedureFeeResultDTO;

/** Nhãn / chuỗi hiển thị liên quan thu phí và phân bổ tự động. */
public final class ProcedurePaymentLabels {

    private ProcedurePaymentLabels() {
    }

    /**
     * Format số tiền lệ phí; thiếu preview thì dùng mặc định 200.000 đ.
     *
     * @param feePreview kết quả tính phí (có thể null)
     * @return chuỗi tiền kèm đơn vị {@code đ}
     */
    public static String formatFeeAmount(ProcedureFeeResultDTO feePreview) {
        if (feePreview == null || feePreview.getFeeTotal() <= 0) {
            return "200,000 đ";
        }
        return String.format("%,.0f đ", feePreview.getFeeTotal());
    }

    /**
     * Cụm mô tả kết quả tự phân bổ phòng sau khi thu phí.
     *
     * @param allocResult kết quả auto-allocate (có thể null)
     * @return chuỗi bổ sung (thành công / lỗi / cảnh báo)
     */
    public static String formatAutoAllocateDetail(AutoAllocateResultDTO allocResult) {
        if (allocResult != null && allocResult.allocatedCount > 0) {
            return " và tự động phân bổ vào phòng thi";
        }
        if (allocResult != null && allocResult.errorMsg != null && !allocResult.errorMsg.isBlank()) {
            return " (" + allocResult.errorMsg.trim() + ")";
        }
        return " (chưa phân được phòng - kiểm tra phân công giám khảo phòng lý thuyết)";
    }
}
