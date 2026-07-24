package examstaff.util;

import java.util.Locale;

/**
 * Utility format/parse chuỗi kỹ thuật cho exam staff — SBD (số báo danh) và câu mô tả audit gọi thí sinh.
 * Pure static; không phụ thuộc Servlet API hay BLL.
 *
 * Vai trò trong luồng examstaff:
 * parseCandidateNo rút số nguyên từ SBD dạng A-12 hoặc 12 để sort/so sánh
 * hàng đợi và audit. formatDetail ghép đích gọi + mã kết quả (calling, absent)
 * thành câu tiếng Việt ngắn ghi vào nhật ký khi staff gọi thí sinh lên bảng/phòng.
 *
 * Cách hoạt động:
 * - Parse SBD — blank → 0; có - → phần sau dấu; NumberFormatException → 0.
 * - Format audit — outcome rỗng → “Gọi lên {destination}”; map calling/absent
 *       sang câu cố định; còn lại ghép destination - outcome.
 *
 * Ai gọi:
 * StaffCallServiceImpl, StaffAuditLogServiceImpl, CandidateQueueServiceImpl,
 * ExaminerAssignmentRules — sort SBD và ghi chi tiết thao tác gọi thí sinh.
 */
public final class ExamStaffFormat {

    /** Không cho khởi tạo — chỉ dùng static. */
    private ExamStaffFormat() {
    }

    // -------------------------------------------------------------------------
    // Candidate number
    // -------------------------------------------------------------------------

    /**
     * Lấy phần số từ candidate number (sau dấu - nếu có).
     * <p>
 *
     * Luồng parse:
     * - null/blank → 0
     * - Có dấu - → parse phần sau dấu -
     * - Không có - → parse toàn bộ chuỗi đã trim
     * - NumberFormatException → 0
     * @param candidateNumber chuỗi SBD/số thí sinh (ví dụ A-12 hoặc 12)
     * @return số nguyên hoặc 0 nếu không parse được
     */
    public static int parseCandidateNo(String candidateNumber) {
        // Bước 1: thiếu dữ liệu
        if (candidateNumber == null || candidateNumber.isBlank()) {
            return 0;
        }
        String trimmed = candidateNumber.trim();
        // Bước 2: có prefix trước '-' → lấy phần số sau dấu
        if (trimmed.contains("-")) {
            try {
                return Integer.parseInt(trimmed.substring(trimmed.indexOf('-') + 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        // Bước 3: parse nguyên chuỗi
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // -------------------------------------------------------------------------
    // Call audit detail
    // -------------------------------------------------------------------------

    /**
     * Ghép đích gọi và kết quả thành câu tiếng Việt ngắn cho audit “gọi thí sinh”.
     * <p>
 *
     * Luồng:
     * - Chuẩn hóa calledTo / result (null → rỗng)
     * - Outcome rỗng → chỉ mô tả đích hoặc “Gọi thí sinh”
     * - calling / absent → câu cố định theo đích
     * - Outcome khác → ghép destination - outcome hoặc chỉ outcome
     * @param calledTo đích / bàn gọi (có thể blank)
     * @param result   mã kết quả (calling, absent, …)
     * @return chuỗi mô tả thao tác
     */
    public static String formatDetail(String calledTo, String result) {
        // Bước 1: chuẩn hóa đầu vào
        String destination = calledTo == null ? "" : calledTo.trim();
        String outcome = result == null ? "" : result.trim();
        // Bước 2: không có mã kết quả → chỉ mô tả đích gọi
        if (outcome.isEmpty()) {
            return destination.isEmpty() ? "Gọi thí sinh" : "Gọi lên " + destination;
        }
        // Bước 3: map mã kết quả phổ biến sang câu tiếng Việt
        return switch (outcome.toLowerCase(Locale.ROOT)) {
            case "calling" -> destination.isEmpty()
                    ? "Gọi thí sinh lên bảng điện tử"
                    : "Gọi lên " + destination;
            case "absent" -> destination.isEmpty()
                    ? "Đánh dấu vắng mặt"
                    : "Vắng mặt - không lên " + destination;
            default -> destination.isEmpty()
                    ? outcome
                    : destination + " - " + outcome;
        };
    }
}
