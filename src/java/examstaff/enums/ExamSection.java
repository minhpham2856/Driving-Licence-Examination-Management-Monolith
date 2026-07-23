package examstaff.enums;

/**
 * Enum phần thi trong kỳ — ánh xạ nhãn tiếng Việt sang {@code ExamTypeId} trên CSDL
 * (lý thuyết, thực hành trong hình, thực hành trên đường).
 *
 * Vai trò trong luồng examstaff:
 * Chuẩn hóa tên phần thi khi JOIN điểm, lọc thiết bị, phân loại khu vực thi và hiển thị báo cáo.
 * {@link #LY_THUYET} dùng làm nhãn AreaType “chuẩn” schema Clean; thực hành có thể alias
 * qua {@code ExamAreaTypeResolver}.
 *
 * Giá trị và ExamTypeId:
 * - {@link #LY_THUYET} — {@code ExamTypeId = 1}.
 * - {@link #THUC_HANH_TRONG_HINH} — {@code ExamTypeId = 2} (sa hình).
 * - {@link #THUC_HANH_TREN_DUONG} — {@code ExamTypeId = 4} (đường trường).
 *
 * Ai sử dụng:
 * {@code ExamAreaDAOImpl}, {@code ExamEnrollmentSectionSupport}, {@code Db2CandidateSql},
 * {@code Db2ExamSchemaSql}, {@code AllocationPassRules}, {@code ReportInfractionViewDAOImpl},
 * {@code ExamAreaTypeResolver} — mọi query/filter theo loại phần thi.
 */
public enum ExamSection {
    /** Phần lý thuyết (ExamTypeId = 1). */
    LY_THUYET("Lý thuyết", 1),
    /** Thực hành trong hình / sa hình (ExamTypeId = 2). */
    THUC_HANH_TRONG_HINH("Thực hành trong hình", 2),
    /** Thực hành trên đường trường (ExamTypeId = 4). */
    THUC_HANH_TREN_DUONG("Thực hành trên đường", 4);

    /** Nhãn tiếng Việt hiển thị. */
    private final String displayName;
    /** Khóa ExamTypeId tương ứng trong CSDL. */
    private final int examTypeId;

    /**
     * Gán nhãn và ExamTypeId cho phần thi.
     * @param displayName nhãn VI
     * @param examTypeId  id loại phần thi
     */
    ExamSection(String displayName, int examTypeId) {
        this.displayName = displayName;
        this.examTypeId = examTypeId;
    }

    /**
     * Lấy nhãn tiếng Việt của phần thi.
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Lấy {@code ExamTypeId} dùng trong query / join điểm.
     * @return mã loại phần thi (1, 2 hoặc 4)
     */
    public int getExamTypeId() {
        return examTypeId;
    }
}
