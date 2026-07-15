package examstaff.service.impl.support.procedure;

import examstaff.dao.FeeDAO;
import examstaff.dao.PaymentDAO;
import examstaff.dao.impl.FeeDAOImpl;
import examstaff.dao.impl.PaymentDAOImpl;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.dto.ReportPaymentSummaryDTO;
import examstaff.enums.PaymentStatus;
import shared.model.Fee;
import shared.model.Payment;
import examstaff.util.ProcedureFeeTotals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Đọc phí/thanh toán dùng chung Procedure + Document report.
 * Presentation không gọi DAO trực tiếp.
 */
public class ProcedureFeeQueryServiceImpl {

    private final FeeDAO feeDAO = new FeeDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

    /**
     * Xác định các khoản phí thủ tục áp dụng cho hồ sơ.
     *
     * @param profile hồ sơ đăng ký thí sinh
     * @return kết quả phí (khoản mục, tổng, …)
     */
    public ProcedureFeeResultDTO resolveProcedureFees(ExamRegistrationDTO profile) {
        ProcedureFeeResultDTO result = new ProcedureFeeResultDTO();
        result.setFeeLines(new ArrayList<>());
        // Validate
        if (profile == null) {
            return result;
        }
        // Load mã hạng
        String licenseCode = profile.getLicenseCode();
        if (licenseCode == null || licenseCode.isBlank()) {
            licenseCode = profile.getClazz();
        }
        // Load phí: ưu tiên theo Payment hiện có, fallback catalog theo hạng
        Payment payment = paymentDAO.getByCandidateId(profile.getId());
        List<Fee> feeLines = new ArrayList<>();
        boolean feesFromPayment = false;
        if (payment != null && payment.getPaymentId() > 0) {
            feeLines = feeDAO.getFeesByPaymentId(payment.getPaymentId());
            feeLines = filterApplicableFees(profile, feeLines);
            feesFromPayment = feeLines != null && !feeLines.isEmpty();
        }
        if (feeLines == null || feeLines.isEmpty()) {
            feeLines = feeDAO.getProcedureFees(licenseCode, false);
            feeLines = filterApplicableFees(profile, feeLines);
            feesFromPayment = false;
        }

        // Result: tổng tiền + gắn dòng phí
        double feeTotal = ProcedureFeeTotals.resolvePaidAmount(payment, feeLines);
        if (feeTotal <= 0) {
            feeTotal = feeLines.stream().mapToDouble(Fee::getAmount).sum();
        }

        result.setFeeLines(feeLines);
        result.setFeeTotal(feeTotal);
        result.setFeesFromPayment(feesFromPayment);
        return result;
    }

    /**
     * Tóm tắt thanh toán đã hoàn tất cho báo cáo kỳ thi.
     *
     * @param candidateId mã đăng ký thí sinh
     * @return summary (có thể rỗng nếu chưa thanh toán)
     */
    public ReportPaymentSummaryDTO findPaymentSummary(int candidateId) {
        ReportPaymentSummaryDTO summary = new ReportPaymentSummaryDTO();
        // Load payment — bỏ qua nếu chưa hoàn tất
        Payment payment = paymentDAO.getByCandidateId(candidateId);
        if (payment == null || payment.getPaymentId() <= 0
                || !PaymentStatus.isCompleted(payment.getPaymentStatus())) {
            return summary;
        }
        List<Fee> feeLines = feeDAO.getFeesByPaymentId(payment.getPaymentId());
        if (feeLines == null) {
            feeLines = new ArrayList<>();
        }
        // Result
        summary.setPayment(payment);
        summary.setFeeLines(feeLines);
        summary.setLineTotal(ProcedureFeeTotals.resolvePaidAmount(payment, feeLines));
        return summary;
    }

    /**
     * Lọc khoản phí theo phần thi thí sinh bỏ qua (lý thuyết / thực hành).
     *
     * @param profile  hồ sơ (cờ skipsTheory / skipsPractical)
     * @param feeLines danh sách phí gốc
     * @return danh sách đã lọc (không null)
     */
    private static List<Fee> filterApplicableFees(ExamRegistrationDTO profile, List<Fee> feeLines) {
        if (feeLines == null || feeLines.isEmpty() || profile == null) {
            return feeLines != null ? feeLines : new ArrayList<>();
        }
        List<Fee> filtered = new ArrayList<>();
        boolean skipsTheory = profile.skipsTheory();
        boolean skipsPractical = profile.skipsPractical();
        for (Fee fee : feeLines) {
            if (fee == null) {
                continue;
            }
            String name = normalize(fee.getFeeName());
            if (skipsTheory && containsAny(name, "ly thuyet")) {
                continue;
            }
            if (skipsPractical && containsAny(name, "trong hinh", "sa hinh", "thuc hanh trong", "thuc hanh")) {
                continue;
            }
            filtered.add(fee);
        }
        return filtered;
    }

    /**
     * Bỏ dấu, chữ thường để so khớp tên phí.
     *
     * @param value chuỗi gốc (có thể null)
     * @return chuỗi đã normalize
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    /**
     * Kiểm tra haystack chứa bất kỳ needle nào.
     *
     * @param haystack chuỗi đã normalize
     * @param needles  các mẫu cần tìm
     * @return true nếu chứa ít nhất một needle
     */
    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

}
