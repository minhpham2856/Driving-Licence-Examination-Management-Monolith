package examstaff.util;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.ProcedureFeeResultDTO;

public final class ProcedurePaymentLabels {

    private ProcedurePaymentLabels() {
    }

    public static String formatFeeAmount(ProcedureFeeResultDTO feePreview) {
        if (feePreview == null || feePreview.getFeeTotal() <= 0) {
            return "200,000 đ";
        }
        return String.format("%,.0f đ", feePreview.getFeeTotal());
    }

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
