package examstaff.enums;

import java.util.Locale;

/**
 * Enum trạng thái vòng đời kỳ thi (Exam) — chuẩn hóa chuỗi tiếng Việt trên UI/CSDL
 * và alias tiếng Anh legacy (scheduled, inProgress, …).
 *
 * Vai trò trong luồng examstaff:
 * Quyết định staff được phép start/pause/end ca, đổi hồ sơ thí sinh hay không.
 * normalize là điểm vào duy nhất: blank → CHUA_DIEN_RA; khớp displayName;
 * rồi map alias EN; không khớp → mặc định an toàn.
 * isLockedForStaffMutation khóa mutation khi kỳ HOAN_TAT hoặc DA_HUY.
 *
 * Chuỗi trạng thái:
 * - CHUA_DIEN_RA — chưa tới giờ / chưa mở ca.
 * - MO — đã mở, chưa bắt đầu (alias: scheduled, open).
 * - DANG_DIEN_RA — ca đang chạy (alias: inProgress).
 * - TAM_DUNG — tạm dừng (alias: paused).
 * - HOAN_TAT, DA_HUY — kết thúc hoặc hủy; khóa thao tác staff.
 *
 * Ai sử dụng:
 * ExamControlServiceImpl, ExamStaffPageBinder, ProcedureWorkflowServiceImpl,
 * CandidateCallServlet, AllocationPassRules, DocumentServiceImpl —
 * kiểm tra canStart, canEnd, isInProgress, isLockedForStaffMutation.
 */
public enum ExamStatus {
    /** Kỳ thi chưa tới giờ / chưa mở. */
    CHUA_DIEN_RA("Chưa diễn ra"),
    /** Đã mở, chưa bắt đầu ca (alias EN: scheduled, open). */
    MO("Mở"),
    /** Đang diễn ra (alias EN: inProgress). */
    DANG_DIEN_RA("Đang diễn ra"),
    /** Tạm dừng ca (alias EN: paused). */
    TAM_DUNG("Tạm dừng"),
    /** Đã hoàn tất (alias EN: completed). */
    HOAN_TAT("Hoàn tất"),
    /** Đã hủy (alias EN: cancelled). */
    DA_HUY("Đã hủy");

    /** Nhãn tiếng Việt khớp CSDL / so sánh ignore-case. */
    private final String displayName;

    /**
     * Gán nhãn hiển thị cho trạng thái kỳ thi.
     * @param displayName chuỗi VI
     */
    ExamStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy nhãn tiếng Việt của trạng thái.
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * So khớp chuỗi trạng thái với displayName (không phân biệt hoa thường).
     * @param value chuỗi từ DB/UI
     * @return true nếu khớp hằng này
     */
    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }

    /**
     * Chuẩn hóa chuỗi trạng thái (VI/EN) về enum; mặc định CHUA_DIEN_RA.
     * <p>
     * Luồng: blank → CHUA_DIEN_RA; khớp displayName; rồi map alias EN/VI không dấu;
     * không khớp → CHUA_DIEN_RA.
     * @param value chuỗi trạng thái
     * @return enum tương ứng
     */
    public static ExamStatus normalize(String value) {
        // Bước 1: thiếu dữ liệu
        if (value == null || value.isBlank()) {
            return CHUA_DIEN_RA;
        }
        String trimmed = value.trim();
        // Bước 2: khớp nhãn VI chính thức
        for (ExamStatus status : values()) {
            if (status.matches(trimmed)) {
                return status;
            }
        }
        // Bước 3: alias EN / biến thể
        return switch (trimmed.toLowerCase(Locale.ROOT)) {
            case "scheduled", "open" -> MO;
            case "inprogress", "in progress" -> DANG_DIEN_RA;
            case "paused", "pause", "tạm dừng", "tam dung" -> TAM_DUNG;
            case "completed", "complete" -> HOAN_TAT;
            case "cancelled", "canceled" -> DA_HUY;
            default -> CHUA_DIEN_RA;
        };
    }

    /**
     * Có thể bắt đầu ca khi trạng thái là CHUA_DIEN_RA hoặc MO.
     * @param status chuỗi trạng thái kỳ thi
     * @return true nếu được phép start
     */
    public static boolean canStart(String status) {
        ExamStatus normalized = normalize(status);
        return normalized == CHUA_DIEN_RA || normalized == MO;
    }

    /**
     * Kiểm tra kỳ đang diễn ra.
     * @param status chuỗi trạng thái
     * @return true nếu DANG_DIEN_RA
     */
    public static boolean isInProgress(String status) {
        return normalize(status) == DANG_DIEN_RA;
    }

    /**
     * Kiểm tra kỳ đang tạm dừng.
     * @param status chuỗi trạng thái
     * @return true nếu TAM_DUNG
     */
    public static boolean isPaused(String status) {
        return normalize(status) == TAM_DUNG;
    }

    /**
     * Có thể kết thúc ca khi đang diễn ra hoặc đang tạm dừng.
     * @param status chuỗi trạng thái
     * @return true nếu được phép end
     */
    public static boolean canEnd(String status) {
        ExamStatus normalized = normalize(status);
        return normalized == DANG_DIEN_RA || normalized == TAM_DUNG;
    }

    /**
     * Kỳ đã hoàn tất (HOAN_TAT).
     * @param status chuỗi trạng thái
     * @return true nếu đã hoàn tất
     */
    public static boolean isCompleted(String status) {
        return normalize(status) == HOAN_TAT;
    }

    /**
     * Khóa thao tác staff đổi hồ sơ / đình chỉ / hoàn tác khi kỳ đã đóng
     * (HOAN_TAT hoặc DA_HUY).
     * @param status chuỗi trạng thái
     * @return true nếu không cho staff mutation
     */
    public static boolean isLockedForStaffMutation(String status) {
        ExamStatus normalized = normalize(status);
        return normalized == HOAN_TAT || normalized == DA_HUY;
    }
}
