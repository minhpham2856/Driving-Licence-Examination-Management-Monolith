package examstaff.dao;

/**
 * Đoạn SQL tái sử dụng cho schema DLEM_DB_2 (Exam-centric).
 *
 * Vấn đề schema giải quyết:
 * Một kỳ thi có nhiều ExamSection (LT / TH / sa hình…); tên SectionType
 * trong DB có thể là tiếng Anh hoặc tiếng Việt. Thay vì hard-code một literal,
 * mọi filter LT/TH dùng THEORY_SECTION_TYPES / PRACTICAL_SECTION_TYPES.
 *
 * Ba nhóm hằng:
 * - <b>IN-list SectionType</b> — nhúng vào WHERE ... IN (...) hoặc JOIN
 * - <b>JOIN blocks</b> — JOIN_THEORY_SECTION, JOIN_PRACTICAL_SECTION,
 *       JOIN_ALLOCATED_AREA gắn alias theoryEes/practicalEes/theoryArea
 * - <b>Column expressions</b> — status thủ tục, chữ ký in, mã/tên phòng đã phân
 *
 * Ai dùng?:
 * Db2CandidateSql, ExamEnrollmentSectionSupport,
 * ReportInfractionViewDAOImpl, và mọi UPDATE phân phòng theo LT/TH.
 * <p>Tham số examId ở BLL/UI map tới cột ExamId. Lớp tiện ích — không khởi tạo.
 */
public final class Db2ExamSchemaSql {

    /**
     * Chặn khởi tạo: chỉ dùng hằng SQL tĩnh.
     */
    private Db2ExamSchemaSql() {
    }

    /**
     * Danh sách giá trị SectionType thuộc phần lý thuyết
     * (dùng trong mệnh đề IN (...) của SQL).
     */
    public static final String THEORY_SECTION_TYPES =
            "N'Theory', N'Lý thuyết', N'LT'";

    /**
     * Danh sách giá trị SectionType thuộc phần thực hành / sa hình
     * (dùng trong mệnh đề IN (...) của SQL).
     */
    public static final String PRACTICAL_SECTION_TYPES =
            "N'Practical', N'Thực hành', N'Thực hành trong hình', N'Thực hành trên đường', "
                    + "N'Sa hình', N'Layout', N'TH'";

    /**
     * Cụm LEFT JOIN lấy section lý thuyết của kỳ thi và bản ghi enrollment section tương ứng.
     * Nối ExamSection theorySec (lọc THEORY_SECTION_TYPES)
     * rồi ExamEnrollmentSection theoryEes theo ExamEnrollmentId.
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
     * Nối ExamSection practicalSec (lọc PRACTICAL_SECTION_TYPES)
     * rồi ExamEnrollmentSection practicalEes.
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
     * Biểu thức cột trạng thái thủ tục / lý thuyết: theoryEes.Status.
     */
    public static final String THEORY_STATUS_EXPR = "theoryEes.Status";

    /**
     * Biểu thức BIT “đã ký / in biên bản”: CompletedAt được set khi in chữ ký
     * trong lúc Status vẫn là AwaitingSignature.
     */
    public static final String SIGNATURE_PRINTED_EXPR = """
            CAST(CASE
              WHEN theoryEes.CompletedAt IS NOT NULL
               AND theoryEes.Status = N'AwaitingSignature' THEN 1
              ELSE 0
            END AS BIT)""";

    /**
     * Biểu thức mã phòng lý thuyết đã phân:
     * ưu tiên theoryEes.ExamAreaId, fallback ee.AllocatedExamAreaId.
     */
    public static final String ALLOCATED_AREA_EXPR = "COALESCE(theoryEes.ExamAreaId, ee.AllocatedExamAreaId)";

    /**
     * Biểu thức tên phòng lý thuyết đã phân
     * (theoryArea.AreaName hoặc allocArea.AreaName).
     */
    public static final String ALLOCATED_AREA_NAME_EXPR =
            "COALESCE(theoryArea.AreaName, allocArea.AreaName)";

    /**
     * Biểu thức mã khu vực thực hành đã phân: practicalEes.ExamAreaId.
     */
    public static final String PRACTICAL_ALLOCATED_AREA_EXPR = "practicalEes.ExamAreaId";

    /**
     * Biểu thức tên khu vực thực hành đã phân: practicalArea.AreaName.
     */
    public static final String PRACTICAL_ALLOCATED_AREA_NAME_EXPR = "practicalArea.AreaName";

    /**
     * Cụm LEFT JOIN tên khu vực LT/TH đã phân bổ
     * (ExamArea theoryArea / practicalArea theo ExamAreaId của section).
     */
    public static final String JOIN_ALLOCATED_AREA = """
            LEFT JOIN ExamArea theoryArea ON theoryArea.ExamAreaId = theoryEes.ExamAreaId
            LEFT JOIN ExamArea practicalArea ON practicalArea.ExamAreaId = practicalEes.ExamAreaId
            """;
}
