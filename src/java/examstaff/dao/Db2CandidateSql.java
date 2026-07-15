package examstaff.dao;

/**
 * Hằng SQL SELECT thí sinh ({@code Candidate} + {@code ExamEnrollment}) cho schema DLEM_DB_2.
 * Ghép cột hồ sơ, thanh toán, phân phòng LT/TH, trạng thái section và điểm
 * từ các bảng Enrolment / Section / Payment / ExamResult / ExamScore.
 * Caller gắn thêm mệnh đề {@code WHERE} khi thực thi.
 * <p>
 * Lớp tiện ích — không thể khởi tạo; dùng {@link #CANDIDATE_SELECT}
 * hoặc {@link #CANDIDATE_SELECT_MINIMAL}.
 */
public final class Db2CandidateSql {

    /**
     * Chặn khởi tạo: chỉ dùng hằng SQL tĩnh và phương thức ghép câu SELECT.
     */
    private Db2CandidateSql() {
    }

    /**
     * Biểu thức SQL chuyển cột giới tính {@code c.Sex} sang BIT alias {@code gender}
     * (1 = nam / các giá trị nhận diện được; ngược lại 0).
     */
    private static final String GENDER_AS_BIT = """
              CAST(CASE
                WHEN c.Sex IS NULL OR LTRIM(RTRIM(CAST(c.Sex AS NVARCHAR(20)))) = N'' THEN 0
                WHEN TRY_CAST(c.Sex AS INT) = 1 THEN 1
                WHEN UPPER(LTRIM(RTRIM(CAST(c.Sex AS NVARCHAR(20))))) IN (
                  N'NAM', N'M', N'MALE', N'TRUE', N'1'
                ) THEN 1
                ELSE 0
              END AS BIT) AS gender,""";

    /**
     * Phần đầu câu SELECT: mã thí sinh, kỳ thi, enrollment, số báo danh ({@code candidateNo})…
     */
    private static final String CANDIDATE_SELECT_HEAD = """
            SELECT
              c.CandidateId AS id,
              ee.ExamId AS examId,
              ee.ExamEnrollmentId AS examEnrollmentId,
              CAST(0 AS INT) AS personId,
              COALESCE(
                TRY_CAST(c.CandidateNumber AS INT),
                TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
              ) AS candidateNo,
              """;

    /**
     * Phần giữa SELECT: cờ thanh toán / có mặt / vắng / đình chỉ và hồ sơ cơ bản
     * (họ tên, CCCD, ngày sinh…).
     */
    private static final String CANDIDATE_SELECT_MID = """
              CAST(CASE WHEN pay.PaymentId IS NOT NULL THEN 1 ELSE 0 END AS BIT) AS isPaymentCompleted,
              CAST(CASE
                WHEN ISNULL(c.IsAbsent, 0) = 1 OR ISNULL(c.IsSuspended, 0) = 1 THEN 0
                ELSE 1 END AS BIT) AS isPresent,
              CAST(ISNULL(c.IsAbsent, 0) AS BIT) AS isAbsent,
              CAST(ISNULL(c.IsSuspended, 0) AS BIT) AS isSuspended,
              CAST(NULL AS DATETIME) AS presentMarkedAt,
              CAST(NULL AS NVARCHAR(500)) AS notes,
              c.FullName AS fullName,
              c.GovernmentIdNumber AS govIdNo,
              CAST(c.DateOfBirth AS DATE) AS dateOfBirth,
            """;

    /**
     * Phần đuôi cột SELECT: liên hệ, hạng GPLX, thiết bị, phân phòng / trạng thái section
     * (dùng biểu thức từ {@link Db2ExamSchemaSql}).
     */
    private static final String CANDIDATE_SELECT_TAIL =
            """
              c.PhoneNumber AS phoneNo,
              COALESCE(NULLIF(LTRIM(RTRIM(c.Email)), N''), u.Email) AS email,
              c.PhotoImageUrl AS photoUrl,
              l.LicenceClass AS licenseCode,
              dev.DeviceName AS computerCode,
              c.Address AS address,
              c.ReasonForTaking AS reasonForTaking,
              c.TakeTheory AS takeTheory,
              c.TakeLayout AS takePractical,
              CAST(ex.ExamDate AS DATE) AS examDate,
              """
            + Db2ExamSchemaSql.THEORY_STATUS_EXPR + " AS sectionStatus,\n"
            + Db2ExamSchemaSql.SIGNATURE_PRINTED_EXPR + " AS signaturePrinted,\n"
            + Db2ExamSchemaSql.ALLOCATED_AREA_EXPR + " AS allocatedAreaId,\n"
            + Db2ExamSchemaSql.ALLOCATED_AREA_NAME_EXPR + " AS allocatedAreaName,\n"
            + Db2ExamSchemaSql.PRACTICAL_ALLOCATED_AREA_EXPR + " AS practicalAllocatedAreaId,\n"
            + Db2ExamSchemaSql.PRACTICAL_ALLOCATED_AREA_NAME_EXPR + " AS practicalAllocatedAreaName,\n";

    /**
     * Cụm FROM/JOIN cơ bản: Candidate → ExamEnrollment → Exam → Licence,
     * section LT/TH, Profile/User, bắt đầu subquery Payment.
     */
    private static final String CANDIDATE_FROM_JOIN =
            """
            FROM Candidate c
            INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
            INNER JOIN Exam ex ON ex.ExamId = ee.ExamId
            INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
            """
            + Db2ExamSchemaSql.JOIN_THEORY_SECTION
            + Db2ExamSchemaSql.JOIN_PRACTICAL_SECTION
            + Db2ExamSchemaSql.JOIN_ALLOCATED_AREA
            + """
            LEFT JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
            LEFT JOIN [User] u ON u.UserId = prof.UserId
            LEFT JOIN (
                SELECT p1.ExamEnrollmentId, MIN(p1.PaymentId) AS PaymentId
                FROM Payment p1
                WHERE p1.PaymentStatus IN (
            """;

    /**
     * Kết thúc subquery Payment + JOIN thiết bị / khu vực fallback phân bổ enrollment.
     */
    private static final String CANDIDATE_PAYMENT_JOIN_END = """
                )
                GROUP BY p1.ExamEnrollmentId
            ) pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN ExamDevice dev ON dev.ExamDeviceId = COALESCE(theoryEes.ExamDeviceId, ee.ExamDeviceId)
            LEFT JOIN ExamArea allocArea ON allocArea.ExamAreaId = ee.AllocatedExamAreaId
            """;

    /**
     * Cụm LEFT JOIN subquery tính điểm lý thuyết và thực hành
     * từ {@code ExamResult} / {@code ExamScore} / {@code ExamSection}.
     */
    private static final String CANDIDATE_SCORE_JOINS =
            """
            LEFT JOIN (
                SELECT er.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM ExamResult er
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionType IN ("""
            + Db2ExamSchemaSql.THEORY_SECTION_TYPES
            + """
                )
                GROUP BY er.ExamEnrollmentId
            ) theory ON theory.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN (
                SELECT er.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM ExamResult er
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionType IN ("""
            + Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES
            + """
                )
                GROUP BY er.ExamEnrollmentId
            ) practical ON practical.ExamEnrollmentId = ee.ExamEnrollmentId
            """;

    /**
     * Cột điểm LT/TH lấy từ subquery (alias {@code theoryScore}, {@code practicalScore}).
     */
    private static final String CANDIDATE_SCORE_COLUMNS = """
              theory.scoreVal AS theoryScore,
              practical.scoreVal AS practicalScore
            """;

    /**
     * Cột điểm null — dùng khi fallback không JOIN bảng điểm.
     */
    private static final String CANDIDATE_NULL_SCORE_COLUMNS = """
              CAST(NULL AS INT) AS theoryScore,
              CAST(NULL AS INT) AS practicalScore
            """;

    /**
     * Ghép đầy đủ câu SELECT thí sinh từ các mảnh cột / JOIN.
     * Chèn biểu thức {@code registrationType}, trạng thái thanh toán ({@code PaymentStatus.sqlInClause}),
     * và phần điểm tùy chọn.
     *
     * @param scoreColumns đoạn cột điểm (có điểm thật hoặc CAST NULL)
     * @param scoreJoins   cụm LEFT JOIN điểm; chuỗi rỗng nếu không lấy điểm
     * @return chuỗi SQL SELECT đầy đủ (chưa có WHERE); caller gắn điều kiện khi chạy
     */
    private static String buildCandidateSelect(String scoreColumns, String scoreJoins) {
        return CANDIDATE_SELECT_HEAD
                + examstaff.enums.RegistrationType.sqlCaseExpression("c.TakeNo")
                + " AS registrationType,\n"
                + CANDIDATE_SELECT_MID
                + GENDER_AS_BIT
                + CANDIDATE_SELECT_TAIL
                + scoreColumns
                + CANDIDATE_FROM_JOIN
                + examstaff.enums.PaymentStatus.sqlInClause()
                + CANDIDATE_PAYMENT_JOIN_END
                + scoreJoins;
    }

    /**
     * Câu SELECT thí sinh đầy đủ (có JOIN điểm LT/TH).
     * Gắn thêm {@code WHERE} từ caller khi thực thi.
     */
    public static final String CANDIDATE_SELECT =
            buildCandidateSelect(CANDIDATE_SCORE_COLUMNS, CANDIDATE_SCORE_JOINS);

    /**
     * Câu SELECT tối thiểu (không JOIN điểm) — fallback khi query đầy đủ thất bại.
     * Điểm LT/TH trả về {@code NULL}.
     */
    public static final String CANDIDATE_SELECT_MINIMAL =
            buildCandidateSelect(CANDIDATE_NULL_SCORE_COLUMNS, "");
}
