package examstaff.enums;

/**
 * Nhãn entity nghiệp vụ dùng trong nhật ký audit ExamStaff.
 * {@link #resolveLabel} đổi tên entity thô sang nhãn tiếng Việt nếu khớp một hằng.
 */
public enum AuditEntity {
    /** Thí sinh / hồ sơ thí sinh trên ca. */
    THI_SINH("Thí sinh"),
    /** Kết quả thi tổng hợp. */
    KET_QUA_THI("Kết quả thi"),
    /** Phòng thi (khu vực). */
    PHONG_THI("Phòng thi"),
    /** Thiết bị thi (máy / máy tính). */
    THIET_BI_THI("Thiết bị thi"),
    /** Ca thi / session điều hành. */
    CA_THI("Ca thi"),
    /** Phân công sát hạch viên (giám thị). */
    PHAN_CONG_SAT_HACH_VIEN("Phân công sát hạch viên"),
    /** Thao tác gọi thí sinh lên bảng / phòng. */
    GOI_THI_SINH("Gọi thí sinh"),
    /** Hồ sơ thủ tục hành chính. */
    HO_SO("Hồ sơ"),
    /** Thanh toán lệ phí thủ tục. */
    THANH_TOAN("Thanh toán"),
    /** Điểm thi từng phần. */
    DIEM_THI("Điểm thi"),
    /** Hàng đợi nhập điểm. */
    HANG_DOI_NHAP_DIEM("Hàng đợi nhập điểm");

    /** Nhãn tiếng Việt hiển thị trên audit. */
    private final String displayName;

    /**
     * Gán nhãn entity audit.
     *
     * @param displayName nhãn VI
     */
    AuditEntity(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy nhãn tiếng Việt của entity.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Đổi tên entity thô sang nhãn tiếng Việt nếu khớp một hằng (ignore case);
     * blank → {@code "-"}; không khớp → giữ nguyên chuỗi đã trim.
     *
     * @param entityName tên entity từ log
     * @return nhãn hiển thị
     */
    public static String resolveLabel(String entityName) {
        // Bước 1: thiếu tên → placeholder
        if (entityName == null || entityName.isBlank()) {
            return "-";
        }
        String trimmed = entityName.trim();
        // Bước 2: khớp displayName của từng hằng
        for (AuditEntity entity : values()) {
            if (entity.displayName.equalsIgnoreCase(trimmed)) {
                return entity.displayName;
            }
        }
        // Bước 3: giữ nguyên nếu không thuộc tập nhãn đã biết
        return trimmed;
    }
}
