package examstaff.dao;

/**
 * Hằng SQL SELECT thí sinh (Candidate + ExamEnrollment) cho schema DLEM_DB_2.
 *
 * Approach B — vì sao SELECT “full text”?:
 * Trước đây có thể ghép SQL bằng splice/CSV helper khó đọc. Mỗi hằng ở đây là
 * <b>một câu SELECT đầy đủ</b> (chưa có WHERE); caller chỉ nối điều kiện khi chạy.
 * Đoạn JOIN section LT/TH lấy từ Db2ExamSchemaSql để không lặp alias.
 *
 * Hai biến thể:
 * - CANDIDATE_SELECT — đủ cột + subquery điểm LT/TH (theoryScore/practicalScore)
 * - CANDIDATE_SELECT_MINIMAL — bỏ JOIN điểm (score = NULL); fallback khi query đầy đủ lỗi
 *
 * Ai dùng?:
 * Chủ yếu ExamRegistrationDAOImpl (getById, getCandidatesByExam,
 * getByExamAndSbd, …) → map ExamRegistrationDTO cho dashboard / allocation / call / procedure.
 *
 * Luồng đọc một thí sinh:
 * <pre>
 *   CANDIDATE_SELECT + " WHERE c.CandidateId = ?"
 *     → JOIN ExamEnrollment / Exam / Licence
 *     → LEFT JOIN theoryEes / practicalEes (phòng, status thủ tục)
 *     → LEFT JOIN payment + điểm LT/TH
 *     → ResultSet → ExamRegistrationDTO
 * </pre>
 * <p>Lớp tiện ích — không khởi tạo.
 */
public final class Db2CandidateSql {

    private Db2CandidateSql() {
    }

    /**
     * Marks an enrollment when a critical theory question is answered
     * incorrectly or left unanswered.
     */
    private static final String JOIN_WRONG_CRITICAL_THEORY = """
            LEFT JOIN (
                SELECT criticalEes.ExamEnrollmentId,
                       CAST(1 AS BIT) AS hasWrongCriticalTheory
                FROM CandidateAnswer ca
                INNER JOIN Question q ON q.QuestionId = ca.QuestionId
                INNER JOIN TheoryPaper tp ON tp.TheoryPaperId = ca.TheoryPaperId
                INNER JOIN ExamEnrollmentSection criticalEes
                    ON criticalEes.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
                WHERE q.IsCritical = 1
                  AND (
                    ca.Answer IS NULL
                    OR LTRIM(RTRIM(ca.Answer)) = N''
                    OR UPPER(LTRIM(RTRIM(ca.Answer)))
                       <> UPPER(LTRIM(RTRIM(q.CorrectAnswer)))
                  )
                GROUP BY criticalEes.ExamEnrollmentId
            ) criticalTheory ON criticalTheory.ExamEnrollmentId = ee.ExamEnrollmentId
            """;

    /**
     * Câu SELECT thí sinh đầy đủ (có JOIN điểm LT/TH).
     * Gắn thêm WHERE từ caller khi thực thi.
     */
    public static final String CANDIDATE_SELECT = """
            SELECT
              c.CandidateId AS id,
              ee.ExamId AS examId,
              ee.ExamEnrollmentId AS examEnrollmentId,
              CAST(0 AS INT) AS personId,
              COALESCE(
                TRY_CAST(c.CandidateNumber AS INT),
                TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
              ) AS candidateNo,
              """
            + examstaff.enums.RegistrationType.sqlCaseExpression("c.TakeNo")
            + """
               AS registrationType,
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
              CAST(CASE
                WHEN c.Sex IS NULL OR LTRIM(RTRIM(CAST(c.Sex AS NVARCHAR(20)))) = N'' THEN 0
                WHEN TRY_CAST(c.Sex AS INT) = 1 THEN 1
                WHEN UPPER(LTRIM(RTRIM(CAST(c.Sex AS NVARCHAR(20))))) IN (
                  N'NAM', N'M', N'MALE', N'TRUE', N'1'
                ) THEN 1
                ELSE 0
              END AS BIT) AS gender,
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
            + Db2ExamSchemaSql.PRACTICAL_ALLOCATED_AREA_NAME_EXPR + " AS practicalAllocatedAreaName,\n"
            + """
              theory.scoreVal AS theoryScore,
              CAST(ISNULL(criticalTheory.hasWrongCriticalTheory, 0) AS BIT) AS hasWrongCriticalTheory,
              practical.scoreVal AS practicalScore
            FROM Candidate c
            INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
            INNER JOIN Exam ex ON ex.ExamId = ee.ExamId
            INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
            """
            + Db2ExamSchemaSql.JOIN_THEORY_SECTION
            + Db2ExamSchemaSql.JOIN_PRACTICAL_SECTION
            + Db2ExamSchemaSql.JOIN_ALLOCATED_AREA
            + JOIN_WRONG_CRITICAL_THEORY
            + """
            LEFT JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
            LEFT JOIN [User] u ON u.UserId = prof.UserId
            LEFT JOIN (
                SELECT p1.ExamEnrollmentId, MIN(p1.PaymentId) AS PaymentId
                FROM Payment p1
                WHERE p1.PaymentStatus IN (
            """
            + examstaff.enums.PaymentStatus.sqlInClause()
            + """
                )
                GROUP BY p1.ExamEnrollmentId
            ) pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN ExamDevice dev ON dev.ExamDeviceId = COALESCE(theoryEes.ExamDeviceId, ee.ExamDeviceId)
            LEFT JOIN ExamArea allocArea ON allocArea.ExamAreaId = ee.AllocatedExamAreaId
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
     * Câu SELECT tối thiểu (không JOIN điểm) — fallback khi query đầy đủ thất bại.
     * Điểm LT/TH trả về NULL.
     */
    public static final String CANDIDATE_SELECT_MINIMAL = """
            SELECT
              c.CandidateId AS id,
              ee.ExamId AS examId,
              ee.ExamEnrollmentId AS examEnrollmentId,
              CAST(0 AS INT) AS personId,
              COALESCE(
                TRY_CAST(c.CandidateNumber AS INT),
                TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
              ) AS candidateNo,
              """
            + examstaff.enums.RegistrationType.sqlCaseExpression("c.TakeNo")
            + """
               AS registrationType,
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
              CAST(CASE
                WHEN c.Sex IS NULL OR LTRIM(RTRIM(CAST(c.Sex AS NVARCHAR(20)))) = N'' THEN 0
                WHEN TRY_CAST(c.Sex AS INT) = 1 THEN 1
                WHEN UPPER(LTRIM(RTRIM(CAST(c.Sex AS NVARCHAR(20))))) IN (
                  N'NAM', N'M', N'MALE', N'TRUE', N'1'
                ) THEN 1
                ELSE 0
              END AS BIT) AS gender,
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
            + Db2ExamSchemaSql.PRACTICAL_ALLOCATED_AREA_NAME_EXPR + " AS practicalAllocatedAreaName,\n"
            + """
              CAST(NULL AS INT) AS theoryScore,
              CAST(ISNULL(criticalTheory.hasWrongCriticalTheory, 0) AS BIT) AS hasWrongCriticalTheory,
              CAST(NULL AS INT) AS practicalScore
            FROM Candidate c
            INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
            INNER JOIN Exam ex ON ex.ExamId = ee.ExamId
            INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
            """
            + Db2ExamSchemaSql.JOIN_THEORY_SECTION
            + Db2ExamSchemaSql.JOIN_PRACTICAL_SECTION
            + Db2ExamSchemaSql.JOIN_ALLOCATED_AREA
            + JOIN_WRONG_CRITICAL_THEORY
            + """
            LEFT JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
            LEFT JOIN [User] u ON u.UserId = prof.UserId
            LEFT JOIN (
                SELECT p1.ExamEnrollmentId, MIN(p1.PaymentId) AS PaymentId
                FROM Payment p1
                WHERE p1.PaymentStatus IN (
            """
            + examstaff.enums.PaymentStatus.sqlInClause()
            + """
                )
                GROUP BY p1.ExamEnrollmentId
            ) pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN ExamDevice dev ON dev.ExamDeviceId = COALESCE(theoryEes.ExamDeviceId, ee.ExamDeviceId)
            LEFT JOIN ExamArea allocArea ON allocArea.ExamAreaId = ee.AllocatedExamAreaId
            """;
}
