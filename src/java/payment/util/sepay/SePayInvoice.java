package payment.util.sepay;

/**
 * Mã hóa đơn SePay.
 * <ul>
 *   <li>Mới: {@code DLEM-{prefix}-{candidateId}-{enrollmentId}-{timestamp}}</li>
 *   <li>Cũ: {@code DLEM-{prefix}-{candidateId}-{timestamp}}</li>
 * </ul>
 */
public final class SePayInvoice {

    private SePayInvoice() {
    }

    public static String generate(String businessPrefix, long candidateId, long enrollmentId) {
        String prefix = blank(businessPrefix) ? "PAY" : businessPrefix.trim().toUpperCase();
        if (enrollmentId > 0) {
            return "DLEM-" + prefix + "-" + candidateId + "-" + enrollmentId + "-" + System.currentTimeMillis();
        }
        return "DLEM-" + prefix + "-" + candidateId + "-" + System.currentTimeMillis();
    }

    public static Integer parseCandidateId(String invoice) {
        String[] parts = split(invoice);
        if (parts == null || parts.length < 4) {
            return null;
        }
        return parsePositiveInt(parts[2]);
    }

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
