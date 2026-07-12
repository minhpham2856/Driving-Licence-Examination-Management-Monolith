package examstaff.dao;

public final class Db2CandidateSql {

    private Db2CandidateSql() {
    }

    private static final String GENDER_AS_BIT = """
              CAST(CASE
                WHEN c.Sex IS NULL OR LTRIM(RTRIM(CAST(c.Sex AS NVARCHAR(20)))) = N'' THEN 0
                WHEN TRY_CAST(c.Sex AS INT) = 1 THEN 1
                WHEN UPPER(LTRIM(RTRIM(CAST(c.Sex AS NVARCHAR(20))))) IN (
                  N'NAM', N'M', N'MALE', N'TRUE', N'1'
                ) THEN 1
                ELSE 0
              END AS BIT) AS gender,""";

    private static final String CANDIDATE_SELECT_HEAD = """
            SELECT
              c.CandidateId AS id,
              ee.ExamId AS examSessionId,
              ee.ExamEnrollmentId AS examEnrollmentId,
              CAST(0 AS INT) AS personId,
              COALESCE(
                TRY_CAST(c.CandidateNumber AS INT),
                TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
              ) AS candidateNo,
              """;

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

    private static final String CANDIDATE_PAYMENT_JOIN_END = """
                )
                GROUP BY p1.ExamEnrollmentId
            ) pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN ExamDevice dev ON dev.ExamDeviceId = COALESCE(theoryEes.ExamDeviceId, ee.ExamDeviceId)
            LEFT JOIN ExamArea allocArea ON allocArea.ExamAreaId = ee.AllocatedExamAreaId
            """;

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

    private static final String CANDIDATE_SCORE_COLUMNS = """
              theory.scoreVal AS theoryScore,
              practical.scoreVal AS practicalScore
            """;

    private static final String CANDIDATE_NULL_SCORE_COLUMNS = """
              CAST(NULL AS INT) AS theoryScore,
              CAST(NULL AS INT) AS practicalScore
            """;

    private static String buildCandidateSelect(String scoreColumns, String scoreJoins) {
        return CANDIDATE_SELECT_HEAD
                + shared.enums.RegistrationType.sqlCaseExpression("c.TakeNo")
                + " AS registrationType,\n"
                + CANDIDATE_SELECT_MID
                + GENDER_AS_BIT
                + CANDIDATE_SELECT_TAIL
                + scoreColumns
                + CANDIDATE_FROM_JOIN
                + shared.enums.PaymentStatus.sqlInClause()
                + CANDIDATE_PAYMENT_JOIN_END
                + scoreJoins;
    }

    public static final String CANDIDATE_SELECT =
            buildCandidateSelect(CANDIDATE_SCORE_COLUMNS, CANDIDATE_SCORE_JOINS);

    public static final String CANDIDATE_SELECT_MINIMAL =
            buildCandidateSelect(CANDIDATE_NULL_SCORE_COLUMNS, "");
}

