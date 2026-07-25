package examstaff.enums;

/**
 * Enum nhãn thực thể nghiệp vụ cho nhật ký audit ExamStaff — chuẩn hóa tên bảng/entity
 * kỹ thuật sang tiếng Việt hiển thị trên màn audit và export CSV.
 *
 * Vai trò trong luồng examstaff:
 * Mỗi hằng gắn displayName tiếng Việt (Thí sinh, Ca thi, Thu phí, …).
 * resolveLabel nhận chuỗi entity từ log thô và trả nhãn đã biết; không khớp → giữ nguyên.
 * Lớp util ExamStaffLabels#formatEntityLabel gọi resolveLabel sau bước map tên bảng SQL.
 *
 * Giá trị hằng số:
 * - THI_SINH — hồ sơ / enrollment thí sinh trên ca.
 * - KET_QUA_THI, DIEM_THI, HANG_DOI_NHAP_DIEM — điểm và hàng đợi nhập điểm.
 * - PHONG_THI, THIET_BI_THI — khu vực và thiết bị thi.
 * - CA_THI, PHAN_CONG_SAT_HACH_VIEN, GOI_THI_SINH — điều hành ca, phân công, gọi thí sinh.
 * - HO_SO, THANH_TOAN — thủ tục hành chính và thu lệ phí.
 *
 * Ai sử dụng:
 * ExamStaffLabels, StaffAuditPageServiceImpl, StaffAuditExportServiceImpl,
 * AuditServlet, AuditExportServlet — mọi nơi cần nhãn entity tiếng Việt trên UI audit.
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
     * @param displayName nhãn VI
     */
    AuditEntity(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy nhãn tiếng Việt của entity.
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Đổi tên entity thô sang nhãn tiếng Việt nếu khớp một hằng (ignore case);
     * blank → "-"; không khớp → giữ nguyên chuỗi đã trim.
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
