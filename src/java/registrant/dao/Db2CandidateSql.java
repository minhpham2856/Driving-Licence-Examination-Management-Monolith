package registrant.dao;

import examstaff.dao.Db2ExamSchemaSql;
import examstaff.enums.PaymentStatus;
import examstaff.enums.RegistrationType;
import registrant.enums.ExamRegistrationLifecycleStatus;

/**
 * SELECT thí sinh đã enroll (ngày thi) — getById/list theo Exam.
 * Portal đăng ký đợt thi không dùng query này (chỉ ghi ExamRegistration).
 */
public final class Db2CandidateSql {

    private Db2CandidateSql() {
    }

    private static final String SELECT_HEAD = """
            SELECT
              c.CandidateId AS id,
              ee.ExamId AS examSessionId,
              prof.ProfileId AS personId,
              c.CandidateNumber AS candidateNumber,
              COALESCE(
                TRY_CAST(c.CandidateNumber AS INT),
                TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
              ) AS candidateNo,
            """;

    private static final String SELECT_MID = """
              CAST(CASE WHEN pay.PaymentId IS NOT NULL THEN 1 ELSE 0 END AS BIT) AS isPaymentCompleted,
              CAST(CASE
                WHEN ISNULL(c.IsAbsent, 0) = 1 OR ISNULL(c.IsSuspended, 0) = 1 THEN 0
                ELSE CASE WHEN er.RegistrationStatus IN (N'CheckedIn', N'Present', N'Completed') THEN 1 ELSE 0 END
              END AS BIT) AS isPresent,
              CAST(ISNULL(c.IsAbsent, 0) AS BIT) AS isAbsent,
              CAST(ISNULL(c.IsSuspended, 0) AS BIT) AS isSuspended,
              CAST(NULL AS DATETIME) AS presentMarkedAt,
              er.Notes AS notes,
              c.FullName AS fullName,
              COALESCE(c.GovernmentIdNumber, prof.GovernmentIdNumber) AS govIdNo,
              CAST(c.DateOfBirth AS DATE) AS dateOfBirth,
              CAST(CASE
                WHEN c.Sex IS NULL THEN 0
                WHEN TRY_CAST(c.Sex AS INT) = 1 THEN 1
                WHEN UPPER(LTRIM(RTRIM(CAST(c.Sex AS NVARCHAR(20))))) IN (N'NAM', N'M', N'MALE', N'TRUE', N'1') THEN 1
                ELSE 0
              END AS BIT) AS gender,
              c.PhoneNumber AS phoneNo,
              COALESCE(NULLIF(LTRIM(RTRIM(c.Email)), N''), u.Email) AS email,
              c.PhotoImageUrl AS photoUrl,
              l.LicenceClass AS licenseCode,
              dev.DeviceName AS computerCode,
              c.Address AS address,
              c.ReasonForTaking AS reasonForTaking,
              CAST(ex.ExamDate AS DATE) AS examDate,
            """;

    private static final String SELECT_TAIL = """
              CAST(NULL AS INT) AS allocatedAreaId,
              CAST(NULL AS NVARCHAR(255)) AS allocatedAreaName,
              theory.scoreVal AS theoryScore,
              practical.scoreVal AS practicalScore,
              CAST(NULL AS INT) AS roadTestScore
            """;

    private static final String FROM_JOIN = """
            FROM Candidate c
            INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
            INNER JOIN Exam ex ON ex.ExamId = ee.ExamId
            INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
            """
            + Db2ExamSchemaSql.JOIN_THEORY_SECTION
            + """
            LEFT JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
            LEFT JOIN [User] u ON u.UserId = prof.UserId
            LEFT JOIN ExamRegistration er ON er.ProfileId = prof.ProfileId
              AND """
            + ExamRegistrationLifecycleStatus.SQL_LIFECYCLE_ONLY
            + """
              AND """
            + ExamRegistrationLifecycleStatus.SQL_EXCLUDE_PROFILE_DOC
            + """
            LEFT JOIN (
                SELECT p1.ExamEnrollmentId, MIN(p1.PaymentId) AS PaymentId
                FROM Payment p1
                WHERE p1.PaymentStatus IN (
            """
            + PaymentStatus.sqlInClause()
            + """
                )
                GROUP BY p1.ExamEnrollmentId
            ) pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN ExamDevice dev ON dev.ExamDeviceId = COALESCE(theoryEes.ExamDeviceId, ee.ExamDeviceId)
            LEFT JOIN (
                SELECT er2.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM ExamResult er2
                JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionType IN (
            """
            + Db2ExamSchemaSql.THEORY_SECTION_TYPES
            + """
                )
                GROUP BY er2.ExamEnrollmentId
            ) theory ON theory.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN (
                SELECT er2.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM ExamResult er2
                JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionType IN (
            """
            + Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES
            + """
                )
                GROUP BY er2.ExamEnrollmentId
            ) practical ON practical.ExamEnrollmentId = ee.ExamEnrollmentId
            """;

    public static final String CANDIDATE_SELECT =
            SELECT_HEAD
            + RegistrationType.sqlCaseExpression("c.TakeNo")
            + " AS registrationType,\n"
            + SELECT_MID
            + Db2ExamSchemaSql.THEORY_STATUS_EXPR + " AS sectionStatus,\n"
            + Db2ExamSchemaSql.SIGNATURE_PRINTED_EXPR + " AS signaturePrinted,\n"
            + SELECT_TAIL
            + FROM_JOIN;
}
