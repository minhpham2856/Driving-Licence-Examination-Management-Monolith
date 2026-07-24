package examstaff.dto;

import shared.model.Fee;

import java.util.List;

/**
 * Kết quả truy vấn lệ phí thủ tục: dòng phí, tổng tiền và nguồn dữ liệu.
 *
 * Vai trò:
 * Cung cấp bảng lệ phí trên wizard thủ tục (procedure.jsp) và dossier;
 * feesFromPayment cho biết lấy từ Payment đã thu hay bảng Fee mặc định.
 *
 * Ai tạo / tiêu thụ:
 * ProcedureFeeQueryServiceImpl → ExamStaffPageBinder#bindProcedureFees,
 * CandidateDossierViewDTO.
 */
public class ProcedureFeeResultDTO {

    private List<Fee> feeLines;
    private double feeTotal;
    private boolean feesFromPayment;

    /** Các dòng lệ phí (mô tả + số tiền). */
    public List<Fee> getFeeLines() {
        return feeLines;
    }

    /** Gán danh sách dòng phí. */
    public void setFeeLines(List<Fee> feeLines) {
        this.feeLines = feeLines;
    }

    /** Tổng tiền lệ phí cần thu / đã thu. */
    public double getFeeTotal() {
        return feeTotal;
    }

    /** Gán tổng lệ phí. */
    public void setFeeTotal(double feeTotal) {
        this.feeTotal = feeTotal;
    }

    /** true nếu dòng phí lấy từ bản ghi Payment đã phát sinh; false nếu từ catalog Fee. */
    public boolean isFeesFromPayment() {
        return feesFromPayment;
    }

    /** Gán cờ nguồn phí từ Payment. */
    public void setFeesFromPayment(boolean feesFromPayment) {
        this.feesFromPayment = feesFromPayment;
    }
}
