package examstaff.dao;

/**
 * Đoạn SQL tái sử dụng cho schema DLEM_DB_2 (Exam-centric).
 * Cung cấp hằng {@code SectionType}, biểu thức SELECT và cụm LEFT JOIN
 * gắn section lý thuyết / thực hành với {@code ExamEnrollmentSection} và {@code ExamArea}.
 * Tham số {@code examId} ở tầng BLL/UI map tới cột {@code ExamId}.
 * <p>
 * Lớp tiện ích — không thể khởi tạo; chỉ dùng các hằng public static.
 */
public final class Db2ExamSchemaSql {

    /**
     * Chặn khởi tạo: chỉ dùng hằng SQL tĩnh.
     */
    private Db2ExamSchemaSql() {
    }

    /**
     * Danh sách giá trị {@code SectionType} thuộc phần lý thuyết
     * (dùng trong mệnh đề {@code IN (...)} của SQL).
     */
    public static final String THEORY_SECTION_TYPES =
            "N'Theory', N'Lý thuyết', N'LT'";

    /**
     * Danh sách giá trị {@code SectionType} thuộc phần thực hành / sa hình
     * (dùng trong mệnh đề {@code IN (...)} của SQL).
     */
    public static final String PRACTICAL_SECTION_TYPES =
            "N'Practical', N'Thực hành', N'Thực hành trong hình', N'Thực hành trên đường', "
                    + "N'Sa hình', N'Layout', N'TH'";

    /**
     * Cụm LEFT JOIN lấy section lý thuyết của kỳ thi và bản ghi enrollment section tương ứng.
     * Nối {@code ExamSection theorySec} (lọc {@link #THEORY_SECTION_TYPES})
     * rồi {@code ExamEnrollmentSection theoryEes} theo {@code ExamEnrollmentId}.
     */
    public static final String JOIN_THEORY_SECTION = """
            LEFT JOIN ExamSection theorySec ON theorySec.ExamId = ex.ExamId
              AND theorySec.SectionType IN (""" + THEORY_SECTION_TYPES + """
            )
            LEFT JOIN ExamEnrollmentSection theoryEes
              ON theoryEes.ExamEnrollmentId = ee.ExamEnrollmentId
             AND theoryEes.ExamSectionId = theorySec.ExamSectionId
            """;

    /**
     * Cụm LEFT JOIN lấy section thực hành của kỳ thi và bản ghi enrollment section tương ứng.
     * Nối {@code ExamSection practicalSec} (lọc {@link #PRACTICAL_SECTION_TYPES})
     * rồi {@code ExamEnrollmentSection practicalEes}.
     */
    public static final String JOIN_PRACTICAL_SECTION = """
            LEFT JOIN ExamSection practicalSec ON practicalSec.ExamId = ex.ExamId
              AND practicalSec.SectionType IN (""" + PRACTICAL_SECTION_TYPES + """
            )
            LEFT JOIN ExamEnrollmentSection practicalEes
              ON practicalEes.ExamEnrollmentId = ee.ExamEnrollmentId
             AND practicalEes.ExamSectionId = practicalSec.ExamSectionId
            """;

    /**
     * Biểu thức cột trạng thái thủ tục / lý thuyết: {@code theoryEes.Status}.
     */
    public static final String THEORY_STATUS_EXPR = "theoryEes.Status";

    /**
     * Biểu thức BIT “đã ký / in biên bản”: {@code CompletedAt} được set khi in chữ ký
     * trong lúc {@code Status} vẫn là {@code AwaitingSignature}.
     */
    public static final String SIGNATURE_PRINTED_EXPR = """
            CAST(CASE
              WHEN theoryEes.CompletedAt IS NOT NULL
               AND theoryEes.Status = N'AwaitingSignature' THEN 1
              ELSE 0
            END AS BIT)""";

    /**
     * Biểu thức mã phòng lý thuyết đã phân:
     * ưu tiên {@code theoryEes.ExamAreaId}, fallback {@code ee.AllocatedExamAreaId}.
     */
    public static final String ALLOCATED_AREA_EXPR = "COALESCE(theoryEes.ExamAreaId, ee.AllocatedExamAreaId)";

    /**
     * Biểu thức tên phòng lý thuyết đã phân
     * ({@code theoryArea.AreaName} hoặc {@code allocArea.AreaName}).
     */
    public static final String ALLOCATED_AREA_NAME_EXPR =
            "COALESCE(theoryArea.AreaName, allocArea.AreaName)";

    /**
     * Biểu thức mã khu vực thực hành đã phân: {@code practicalEes.ExamAreaId}.
     */
    public static final String PRACTICAL_ALLOCATED_AREA_EXPR = "practicalEes.ExamAreaId";

    /**
     * Biểu thức tên khu vực thực hành đã phân: {@code practicalArea.AreaName}.
     */
    public static final String PRACTICAL_ALLOCATED_AREA_NAME_EXPR = "practicalArea.AreaName";

    /**
     * Cụm LEFT JOIN tên khu vực LT/TH đã phân bổ
     * ({@code ExamArea theoryArea} / {@code practicalArea} theo {@code ExamAreaId} của section).
     */
    public static final String JOIN_ALLOCATED_AREA = """
            LEFT JOIN ExamArea theoryArea ON theoryArea.ExamAreaId = theoryEes.ExamAreaId
            LEFT JOIN ExamArea practicalArea ON practicalArea.ExamAreaId = practicalEes.ExamAreaId
            """;
}
