package examstaff.dao;

/**
 * SQL fragments cho schema DLEM_DB_2 (không còn [Session]).
 * Tham số {@code sessionId} ở tầng BLL/UI được alias thành {@code ExamId}.
 */
public final class Db2ExamSchemaSql {

    private Db2ExamSchemaSql() {
    }

    public static final String THEORY_SECTION_TYPES =
            "N'Theory', N'Lý thuyết', N'LT'";

    public static final String PRACTICAL_SECTION_TYPES =
            "N'Practical', N'Thực hành', N'Thực hành trong hình', N'Sa hình', N'Layout', N'TH'";

    public static final String JOIN_THEORY_SECTION = """
            LEFT JOIN ExamSection theorySec ON theorySec.ExamId = ex.ExamId
              AND theorySec.SectionType IN (""" + THEORY_SECTION_TYPES + """
            )
            LEFT JOIN ExamEnrollmentSection theoryEes
              ON theoryEes.ExamEnrollmentId = ee.ExamEnrollmentId
             AND theoryEes.ExamSectionId = theorySec.ExamSectionId
            """;

    public static final String JOIN_PRACTICAL_SECTION = """
            LEFT JOIN ExamSection practicalSec ON practicalSec.ExamId = ex.ExamId
              AND practicalSec.SectionType IN (""" + PRACTICAL_SECTION_TYPES + """
            )
            LEFT JOIN ExamEnrollmentSection practicalEes
              ON practicalEes.ExamEnrollmentId = ee.ExamEnrollmentId
             AND practicalEes.ExamSectionId = practicalSec.ExamSectionId
            """;

    /** Trạng thái thủ tục / lý thuyết (thay SectionStatus trên ExamEnrollment). */
    public static final String THEORY_STATUS_EXPR = "theoryEes.Status";

    /**
     * Đã ký biên bản: CompletedAt được set khi markSignaturePrinted,
     * trước khi completeSection chuyển sang Done.
     */
    public static final String SIGNATURE_PRINTED_EXPR = """
            CAST(CASE
              WHEN theoryEes.CompletedAt IS NOT NULL
               AND theoryEes.Status = N'AwaitingSignature' THEN 1
              ELSE 0
            END AS BIT)""";

    public static final String ALLOCATED_AREA_EXPR = "COALESCE(theoryEes.ExamAreaId, ee.AllocatedExamAreaId)";

    public static final String ALLOCATED_AREA_NAME_EXPR =
            "COALESCE(theoryArea.AreaName, allocArea.AreaName)";

    public static final String PRACTICAL_ALLOCATED_AREA_EXPR = "practicalEes.ExamAreaId";

    public static final String PRACTICAL_ALLOCATED_AREA_NAME_EXPR = "practicalArea.AreaName";

    public static final String JOIN_ALLOCATED_AREA = """
            LEFT JOIN ExamArea theoryArea ON theoryArea.ExamAreaId = theoryEes.ExamAreaId
            LEFT JOIN ExamArea practicalArea ON practicalArea.ExamAreaId = practicalEes.ExamAreaId
            """;

    public static String sectionTypeFilter(String alias, String typesCsv) {
        return alias + ".SectionType IN (" + typesCsv + ")";
    }
}
