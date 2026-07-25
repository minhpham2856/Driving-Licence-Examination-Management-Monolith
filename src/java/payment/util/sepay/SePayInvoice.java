package payment.util.sepay;

/**
 * Tiện ích sinh và parse mã hóa đơn SePay (order_invoice_number) gắn thí sinh/đăng ký thi.
 * Dùng ở bước checkout (sinh DLEM-CHK-{candidateId}-{enrollmentId}-{timestamp})
 * và bước IPN (parse CandidateId/ExamEnrollmentId để ghi bảng Payment).
 * Format cũ không enrollment vẫn hỗ trợ parse parts[2] là CandidateId.
 */
public final class SePayInvoice {

    private SePayInvoice() {
    }

    /** Sinh invoice; enrollmentId ≤ 0 → dạng cũ (không có phần enrollment). */
    public static String generate(String businessPrefix, long candidateId, long enrollmentId) {
        String prefix = blank(businessPrefix) ? "PAY" : businessPrefix.trim().toUpperCase();
        if (enrollmentId > 0) {
            return "DLEM-" + prefix + "-" + candidateId + "-" + enrollmentId + "-" + System.currentTimeMillis();
        }
        return "DLEM-" + prefix + "-" + candidateId + "-" + System.currentTimeMillis();
    }

    /** parts[2] — CandidateId; null nếu invoice không hợp lệ. */
    public static Integer parseCandidateId(String invoice) {
        String[] parts = split(invoice);
        if (parts == null || parts.length < 4) {
            return null;
        }
        return parsePositiveInt(parts[2]);
    }

    /** parts[3] — ExamEnrollmentId; chỉ có khi đủ 5 phần (format mới). */
    public static Integer parseEnrollmentId(String invoice) {
        String[] parts = split(invoice);
        if (parts == null || parts.length < 5) {
            return null;
        }
        return parsePositiveInt(parts[3]);
    }

    private static String[] split(String invoice) {
        if (blank(invoice)) {
            return null;
        }
        String[] parts = invoice.trim().split("-");
        if (parts.length < 4 || !"DLEM".equalsIgnoreCase(parts[0])) {
            return null;
        }
        return parts;
    }

    private static Integer parsePositiveInt(String raw) {
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
