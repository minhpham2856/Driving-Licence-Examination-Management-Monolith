package examstaff.util;

import examstaff.enums.ExamSection;
import shared.enums.ExamAreaType;

/**
 * Phân loại khu vực thi (lý thuyết / thực hành) qua nhãn và alias schema.
 * <p>
 * Pure helper — không gọi BLL/DAO. Dùng khi query exact AreaType trên schema Clean
 * hoặc map alias schema SWP/DLEM ({@code Phòng thi}/{@code Sân thi}).
 */
public final class ExamAreaTypeResolver {

    /** Nhãn AreaType thực hành dùng khi query exact (schema Clean). */
    public static final String PRACTICAL_AREA_TYPE = "Thực hành";

    /** Không cho khởi tạo — chỉ dùng static. */
    private ExamAreaTypeResolver() {
    }

    /**
     * Giá trị AreaType “chuẩn” cho khu vực lý thuyết khi query exact (schema Clean).
     * Lấy từ {@link ExamSection#LY_THUYET} display name.
     *
     * @return chuỗi nhãn lý thuyết (ví dụ {@code "Lý thuyết"})
     */
    public static String theoryAreaTypeLabel() {
        return ExamSection.LY_THUYET.getDisplayName();
    }

    /**
     * Giá trị AreaType “chuẩn” cho khu vực thực hành khi query exact (schema Clean).
     *
     * @return {@link #PRACTICAL_AREA_TYPE}
     */
    public static String practicalAreaTypeLabel() {
        return PRACTICAL_AREA_TYPE;
    }

    /**
     * Alias schema SWP/DLEM cho lý thuyết: {@code Phòng thi}
     * ({@link ExamAreaType#EXAM_ROOM}).
     *
     * @return giá trị enum AreaType phòng thi
     */
    public static String theoryAreaTypeAlias() {
        return ExamAreaType.EXAM_ROOM.getValue();
    }

    /**
     * Alias schema SWP/DLEM cho thực hành: {@code Sân thi}
     * ({@link ExamAreaType#EXAM_GROUND}).
     *
     * @return giá trị enum AreaType sân thi
     */
    public static String practicalAreaTypeAlias() {
        return ExamAreaType.EXAM_GROUND.getValue();
    }
}
