package payment.util.sepay;

/**
 * Mã hóa đơn SePay — gắn Candidate / Enrollment để IPN biết ghi Payment cho ai.
 * <ul>
 *   <li><b>Mới (desk CHK):</b> {@code DLEM-{prefix}-{candidateId}-{enrollmentId}-{timestamp}}
 *       ví dụ {@code DLEM-CHK-42-1001-1710000000000}</li>
 *   <li><b>Cũ (không enrollment):</b> {@code DLEM-{prefix}-{candidateId}-{timestamp}}</li>
 * </ul>
 * Parse: {@code parts[0]=DLEM}, {@code parts[1]=prefix}, {@code parts[2]=candidateId},
 * {@code parts[3]=enrollmentId} (chỉ khi length ≥ 5).
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
