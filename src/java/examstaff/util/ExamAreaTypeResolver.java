package examstaff.util;

import examstaff.enums.ExamSection;
import shared.enums.ExamAreaType;

/**
 * Utility phân loại khu vực thi (lý thuyết / thực hành) qua nhãn chuẩn và alias schema SWP/DLEM.
 * Pure helper — không gọi BLL/DAO; cung cấp chuỗi AreaType cho JDBC query.
 *
 * Vai trò trong luồng examstaff:
 * CSDL có thể lưu AreaType theo schema Clean (Lý thuyết/Thực hành) hoặc
 * alias SWP (Phòng thi/Sân thi). Resolver trả cả hai biến thể để DAO
 * (ví dụ ExamAreaDAOImpl#getActiveTheoryRooms) gộp không trùng ExamAreaId.
 *
 * Nhãn và alias:
 * - Lý thuyết — theoryAreaTypeLabel() = ExamSection.LY_THUYET display name;
 *       alias theoryAreaTypeAlias() = ExamAreaType.EXAM_ROOM.
 * - Thực hành — practicalAreaTypeLabel() = PRACTICAL_AREA_TYPE;
 *       alias practicalAreaTypeAlias() = ExamAreaType.EXAM_GROUND.
 *
 * Ai gọi:
 * ExamAreaDAOImpl, ExaminerAllocationServiceImpl, AllocationStageHelper,
 * ExaminerAssignmentRules — query phòng LT/sân TH theo loại khu vực.
 */
public final class ExamAreaTypeResolver {

    /** Nhãn AreaType thực hành dùng khi query exact (schema Clean). */
    public static final String PRACTICAL_AREA_TYPE = "Thực hành";

    /** Không cho khởi tạo — chỉ dùng static. */
    private ExamAreaTypeResolver() {
    }

    /**
     * Giá trị AreaType “chuẩn” cho khu vực lý thuyết khi query exact (schema Clean).
     * Lấy từ ExamSection.LY_THUYET display name.
     * @return chuỗi nhãn lý thuyết (ví dụ "Lý thuyết")
     */
    public static String theoryAreaTypeLabel() {
        return ExamSection.LY_THUYET.getDisplayName();
    }

    /**
     * Giá trị AreaType “chuẩn” cho khu vực thực hành khi query exact (schema Clean).
     * @return PRACTICAL_AREA_TYPE
     */
    public static String practicalAreaTypeLabel() {
        return PRACTICAL_AREA_TYPE;
    }

    /**
     * Alias schema SWP/DLEM cho lý thuyết: Phòng thi
     * (ExamAreaType.EXAM_ROOM).
     * @return giá trị enum AreaType phòng thi
     */
    public static String theoryAreaTypeAlias() {
        return ExamAreaType.EXAM_ROOM.getValue();
    }

    /**
     * Alias schema SWP/DLEM cho thực hành: Sân thi
     * (ExamAreaType.EXAM_GROUND).
     * @return giá trị enum AreaType sân thi
     */
    public static String practicalAreaTypeAlias() {
        return ExamAreaType.EXAM_GROUND.getValue();
    }
}
