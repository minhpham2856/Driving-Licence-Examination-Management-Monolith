package examstaff.dao;

/**
 * Đoạn SQL tái sử dụng cho schema DLEM_DB_2 (Exam-centric).
 * Tham số {@code examId} ở tầng BLL/UI map tới cột {@code ExamId}.
 */
public final class Db2ExamSchemaSql {

    private Db2ExamSchemaSql() {
    }

    /** Giá trị {@code SectionType} thuộc phần lý thuyết. */
    public static final String THEORY_SECTION_TYPES =
            "N'Theory', N'Lý thuyết', N'LT'";

    /** Giá trị {@code SectionType} thuộc phần thực hành / sa hình. */
    public static final String PRACTICAL_SECTION_TYPES =
            "N'Practical', N'Thực hành', N'Thực hành trong hình', N'Sa hình', N'Layout', N'TH'";

    /** LEFT JOIN section + enrollment section lý thuyết. */
    public static final String JOIN_THEORY_SECTION = """
            LEFT JOIN ExamSection theorySec ON theorySec.ExamId = ex.ExamId
              AND theorySec.SectionType IN (""" + THEORY_SECTION_TYPES + """
            )
            LEFT JOIN ExamEnrollmentSection theoryEes
              ON theoryEes.ExamEnrollmentId = ee.ExamEnrollmentId
             AND theoryEes.ExamSectionId = theorySec.ExamSectionId
            """;

    /** LEFT JOIN section + enrollment section thực hành. */
    public static final String JOIN_PRACTICAL_SECTION = """
            LEFT JOIN ExamSection practicalSec ON practicalSec.ExamId = ex.ExamId
              AND practicalSec.SectionType IN (""" + PRACTICAL_SECTION_TYPES + """
            )
            LEFT JOIN ExamEnrollmentSection practicalEes
              ON practicalEes.ExamEnrollmentId = ee.ExamEnrollmentId
             AND practicalEes.ExamSectionId = practicalSec.ExamSectionId
            """;

    /** Biểu thức trạng thái thủ tục / lý thuyết. */
    public static final String THEORY_STATUS_EXPR = "theoryEes.Status";

    /**
     * Đã ký biên bản: {@code CompletedAt} được set khi in chữ ký,
     * trong lúc status vẫn là {@code AwaitingSignature}.
     */
    public static final String SIGNATURE_PRINTED_EXPR = """
            CAST(CASE
              WHEN theoryEes.CompletedAt IS NOT NULL
               AND theoryEes.Status = N'AwaitingSignature' THEN 1
              ELSE 0
            END AS BIT)""";

    /** Mã phòng lý thuyết đã phân (ưu tiên ExamEnrollmentSection). */
    public static final String ALLOCATED_AREA_EXPR = "COALESCE(theoryEes.ExamAreaId, ee.AllocatedExamAreaId)";

    /** Tên phòng lý thuyết đã phân. */
    public static final String ALLOCATED_AREA_NAME_EXPR =
            "COALESCE(theoryArea.AreaName, allocArea.AreaName)";

    /** Mã khu vực thực hành đã phân. */
    public static final String PRACTICAL_ALLOCATED_AREA_EXPR = "practicalEes.ExamAreaId";

    /** Tên khu vực thực hành đã phân. */
    public static final String PRACTICAL_ALLOCATED_AREA_NAME_EXPR = "practicalArea.AreaName";

    /** LEFT JOIN tên khu vực LT/TH đã phân bổ. */
    public static final String JOIN_ALLOCATED_AREA = """
            LEFT JOIN ExamArea theoryArea ON theoryArea.ExamAreaId = theoryEes.ExamAreaId
            LEFT JOIN ExamArea practicalArea ON practicalArea.ExamAreaId = practicalEes.ExamAreaId
            """;
}
