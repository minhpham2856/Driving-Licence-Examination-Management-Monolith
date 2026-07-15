package examstaff.service.impl;

import examstaff.dao.FeeDAO;
import examstaff.dao.PaymentDAO;
import examstaff.dao.impl.FeeDAOImpl;
import examstaff.dao.impl.PaymentDAOImpl;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import shared.model.Fee;
import shared.model.Payment;
import examstaff.service.ProcedureFeeQueryService;
import examstaff.util.ProcedureFeeTotals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/** Implementation: tính và lọc khoản phí thủ tục theo hồ sơ thí sinh. */
public class ProcedureFeeQueryServiceImpl implements ProcedureFeeQueryService {

    private final FeeDAO feeDAO = new FeeDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

    /**
     * Xác định các khoản phí thủ tục áp dụng cho hồ sơ.
     *
     * @param profile hồ sơ đăng ký thí sinh
     * @return kết quả phí (khoản mục, tổng, …)
     */
    @Override
    public ProcedureFeeResultDTO resolveProcedureFees(ExamRegistrationDTO profile) {
        ProcedureFeeResultDTO result = new ProcedureFeeResultDTO();
        result.setFeeLines(new ArrayList<>());
        if (profile == null) {
            return result;
        }
        String licenseCode = profile.getLicenseCode();
        if (licenseCode == null || licenseCode.isBlank()) {
            licenseCode = profile.getClazz();
        }
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

        double feeTotal = ProcedureFeeTotals.resolvePaidAmount(payment, feeLines);
        if (feeTotal <= 0) {
            feeTotal = feeLines.stream().mapToDouble(Fee::getAmount).sum();
        }

        result.setFeeLines(feeLines);
        result.setFeeTotal(feeTotal);
        result.setFeesFromPayment(feesFromPayment);
        return result;
    }

    /** Lọc khoản phí theo phần thi thí sinh bỏ qua. */
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

    /** Bỏ dấu, chữ thường để so khớp tên phí. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    /** Haystack chứa bất kỳ needle nào. */
    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

}
