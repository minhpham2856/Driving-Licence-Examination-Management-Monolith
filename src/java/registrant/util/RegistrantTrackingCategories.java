package registrant.util;

import registrant.dto.RegistrantTrackingLog;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Hằng số loại tác vụ và trạng thái trong nhật ký theo dõi hồ sơ — phục vụ filter {@code track-profile.jsp}.
 * <p>
 * Map {@code category} (upload, gửi duyệt, phê duyệt, profile, exam, payment…) và {@code statusClass}
 * sang nhãn tiếng Việt; phân loại dòng {@link registrant.dto.RegistrantTrackingLog}.
 */
public final class RegistrantTrackingCategories {

    public static final String DOCUMENT_UPLOAD = "document_upload";
    public static final String DOCUMENT_SUBMIT = "document_submit";
    public static final String DOCUMENT_APPROVE = "document_approve";
    public static final String DOCUMENT_REJECT = "document_reject";
    public static final String PROFILE = "profile";
    public static final String EXAM = "exam";
    public static final String RegistrantPayment = "RegistrantPayment";
    public static final String OTHER = "other";

    public static final String STATUS_NOT_SUBMITTED = "not_submitted";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_REJECTED = "rejected";

    private static final Map<String, String> CATEGORY_LABELS = new LinkedHashMap<>();
    private static final Map<String, String> STATUS_LABELS = new LinkedHashMap<>();

    static {
        CATEGORY_LABELS.put(DOCUMENT_UPLOAD, "Tải lên tài liệu");
        CATEGORY_LABELS.put(DOCUMENT_SUBMIT, "Gửi duyệt hồ sơ");
        CATEGORY_LABELS.put(DOCUMENT_APPROVE, "Phê duyệt tài liệu");
        CATEGORY_LABELS.put(DOCUMENT_REJECT, "Từ chối / yêu cầu bổ sung");
        CATEGORY_LABELS.put(PROFILE, "Hồ sơ cá nhân");
        CATEGORY_LABELS.put(EXAM, "Đăng ký đợt thi");
        CATEGORY_LABELS.put(RegistrantPayment, "Thanh toán lệ phí");
        CATEGORY_LABELS.put(OTHER, "Cập nhật khác");

        STATUS_LABELS.put(STATUS_NOT_SUBMITTED, "Chưa gửi duyệt");
        STATUS_LABELS.put(STATUS_PROCESSING, "Đang xử lý / chờ duyệt");
        STATUS_LABELS.put(STATUS_SUCCESS, "Thành công / đã duyệt");
        STATUS_LABELS.put(STATUS_REJECTED, "Từ chối / bổ sung");
    }

    private RegistrantTrackingCategories() {
    }

    /** Nhãn tiếng Việt cho khóa category tracking. */
    public static String categoryLabel(String category) {
        if (category == null || category.isBlank()) {
            return "Khác";
        }
        return CATEGORY_LABELS.getOrDefault(category, category);
    }

    /** Nhãn tiếng Việt cho khóa lọc trạng thái tracking. */
    public static String statusFilterLabel(String statusKey) {
        if (statusKey == null || statusKey.isBlank()) {
            return "Khác";
        }
        return STATUS_LABELS.getOrDefault(statusKey, statusKey);
    }

    /** Thứ tự cố định các category trên dropdown. */
    public static List<String> orderedCategoryKeys() {
        return new ArrayList<>(CATEGORY_LABELS.keySet());
    }

    /** Thứ tự cố định các status filter trên dropdown. */
    public static List<String> orderedStatusKeys() {
        return new ArrayList<>(STATUS_LABELS.keySet());
    }

    /** Map Action+EntityName audit → category tracking. */
    public static String categoryFromAuditAction(String action, String entityName) {
        String upper = action != null ? action.toUpperCase(Locale.ROOT) : "UPDATE";
        String entity = entityName != null ? entityName.toLowerCase(Locale.ROOT) : "";
        boolean document = entity.contains("document") || entity.contains("tài liệu");
        return switch (upper) {
            case "UPLOAD" -> document ? DOCUMENT_UPLOAD : OTHER;
            case "REQUEST" -> document ? DOCUMENT_SUBMIT : PROFILE;
            case "APPROVE" -> document ? DOCUMENT_APPROVE : PROFILE;
            case "REJECT" -> document ? DOCUMENT_REJECT : PROFILE;
            case "INSERT" -> entity.contains("exam") || entity.contains("candidate")
                    ? EXAM : PROFILE;
            case "DELETE" -> document ? DOCUMENT_REJECT : OTHER;
            default -> OTHER;
        };
    }

    /** Xác định category của một TrackingLog (field hoặc suy từ title). */
    public static String resolveCategory(RegistrantTrackingLog log) {
        if (log.getCategory() != null && !log.getCategory().isBlank()) {
            return log.getCategory();
        }
        String title = log.getEventTitle() != null ? log.getEventTitle().toLowerCase(Locale.ROOT) : "";
        if (title.startsWith("tải lên tài liệu")) {
            return DOCUMENT_UPLOAD;
        }
        if (title.startsWith("gửi duyệt") || title.contains("gửi hồ sơ chờ duyệt")) {
            return DOCUMENT_SUBMIT;
        }
        if (title.startsWith("phê duyệt tài liệu") || title.contains("phê duyệt hồ sơ")) {
            return DOCUMENT_APPROVE;
        }
        if (title.startsWith("yêu cầu bổ sung") || title.contains("từ chối")) {
            return DOCUMENT_REJECT;
        }
        if (title.contains("đăng ký đợt thi")) {
            return EXAM;
        }
        if (title.contains("thanh toán")) {
            return RegistrantPayment;
        }
        if (title.contains("hồ sơ") || title.contains("tạo hồ sơ") || title.contains("bổ sung hồ sơ")) {
            return PROFILE;
        }
        return OTHER;
    }

    /** True nếu log khớp categoryFilter (hoặc all). */
    public static boolean matchesCategory(RegistrantTrackingLog log, String categoryFilter) {
        if (categoryFilter == null || categoryFilter.isBlank() || "all".equalsIgnoreCase(categoryFilter)) {
            return true;
        }
        return categoryFilter.equals(resolveCategory(log));
    }

    /** True nếu log khớp statusFilter tracking. */
    public static boolean matchesStatusFilter(RegistrantTrackingLog log, String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank() || "all".equalsIgnoreCase(statusFilter)) {
            return true;
        }
        String label = log.getStatusLabel() != null ? log.getStatusLabel().trim() : "";
        String statusClass = log.getStatusClass() != null ? log.getStatusClass().trim() : "";
        return switch (statusFilter) {
            case STATUS_NOT_SUBMITTED -> "Chưa gửi duyệt".equalsIgnoreCase(label)
                    || "Chưa tải lên".equalsIgnoreCase(label);
            case STATUS_PROCESSING -> "Đang xử lý".equalsIgnoreCase(label)
                    || "Chờ duyệt".equalsIgnoreCase(label)
                    || "Đang bổ sung".equalsIgnoreCase(label)
                    || "pending".equalsIgnoreCase(statusClass);
            case STATUS_SUCCESS -> "Thành công".equalsIgnoreCase(label)
                    || "Đã duyệt".equalsIgnoreCase(label)
                    || "approved".equalsIgnoreCase(statusClass);
            case STATUS_REJECTED -> "Từ chối".equalsIgnoreCase(label)
                    || "Yêu cầu bổ sung".equalsIgnoreCase(label)
                    || "rejected".equalsIgnoreCase(statusClass)
                    || "danger".equalsIgnoreCase(statusClass);
            default -> true;
        };
    }
}
