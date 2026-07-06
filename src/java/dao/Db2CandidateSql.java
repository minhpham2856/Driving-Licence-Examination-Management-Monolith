package dao;

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

    public static final String CANDIDATE_SELECT = """
            SELECT
              c.CandidateId AS id,
              ee.SessionId AS examSessionId,
              ee.ExamEnrollmentId AS examEnrollmentId,
              CAST(0 AS INT) AS personId,
              COALESCE(
                TRY_CAST(c.CandidateNumber AS INT),
                TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
              ) AS candidateNo,
              CASE WHEN ISNULL(c.TakeNo, 1) > 1 THEN N'Retake' ELSE N'PreRegistered' END AS registrationType,
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
            """ + GENDER_AS_BIT + """
              c.PhoneNumber AS phoneNo,
              u.Email AS email,
              c.PhotoImageUrl AS photoUrl,
              l.LicenceClass AS licenseCode,
              dev.DeviceName AS computerCode,
              c.Address AS address,
              c.ReasonForTaking AS reasonForTaking,
              c.TakeTheory AS takeTheory,
              c.TakeLayout AS takePractical,
              c.TakeRoad AS takeOnRoad,
              CAST(s.StartTime AS DATE) AS examDate,
              ee.SectionStatus AS sectionStatus,
              CAST(ISNULL(ee.SignaturePrinted, 0) AS BIT) AS signaturePrinted,
              allocArea.ExamAreaId AS allocatedAreaId,
              allocArea.AreaName AS allocatedAreaName,
              theory.scoreVal AS theoryScore,
              practical.scoreVal AS practicalScore,
              road.scoreVal AS roadTestScore
            FROM Candidate c
            INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
            INNER JOIN [Session] s ON s.SessionId = ee.SessionId
            INNER JOIN Exam ex ON ex.ExamId = s.ExamId
            INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
            LEFT JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
            LEFT JOIN [User] u ON u.UserId = prof.UserId
            LEFT JOIN (
                SELECT p1.ExamEnrollmentId, MIN(p1.PaymentId) AS PaymentId
                FROM Payment p1
                WHERE p1.PaymentStatus IN (N'Completed', N'Paid', N'Hoàn tất')
                GROUP BY p1.ExamEnrollmentId
            ) pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN ExamDevice dev ON dev.ExamDeviceId = ee.ExamDeviceId
            LEFT JOIN ExamArea allocArea ON allocArea.ExamAreaId = dev.ExamAreaId
            LEFT JOIN (
                SELECT er.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM ExamResult er
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionName LIKE N'%Lý thuyết%' OR sec.SectionName LIKE '%Theory%'
                GROUP BY er.ExamEnrollmentId
            ) theory ON theory.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN (
                SELECT er.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM ExamResult er
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionName LIKE N'%Thực hành%' OR sec.SectionName LIKE '%Practical%' OR sec.SectionName LIKE N'%Sa hình%'
                GROUP BY er.ExamEnrollmentId
            ) practical ON practical.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN (
                SELECT er.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM ExamResult er
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionName LIKE N'%Đường%' OR sec.SectionName LIKE '%Road%'
                GROUP BY er.ExamEnrollmentId
            ) road ON road.ExamEnrollmentId = ee.ExamEnrollmentId
            """;

    public static final String CANDIDATE_SELECT_MINIMAL = """
            SELECT
              c.CandidateId AS id,
              ee.SessionId AS examSessionId,
              ee.ExamEnrollmentId AS examEnrollmentId,
              CAST(0 AS INT) AS personId,
              COALESCE(
                TRY_CAST(c.CandidateNumber AS INT),
                TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
              ) AS candidateNo,
              CASE WHEN ISNULL(c.TakeNo, 1) > 1 THEN N'Retake' ELSE N'PreRegistered' END AS registrationType,
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
            """ + GENDER_AS_BIT + """
              c.PhoneNumber AS phoneNo,
              u.Email AS email,
              c.PhotoImageUrl AS photoUrl,
              l.LicenceClass AS licenseCode,
              dev.DeviceName AS computerCode,
              c.Address AS address,
              c.ReasonForTaking AS reasonForTaking,
              c.TakeTheory AS takeTheory,
              c.TakeLayout AS takePractical,
              c.TakeRoad AS takeOnRoad,
              CAST(s.StartTime AS DATE) AS examDate,
              ee.SectionStatus AS sectionStatus,
              CAST(ISNULL(ee.SignaturePrinted, 0) AS BIT) AS signaturePrinted,
              allocArea.ExamAreaId AS allocatedAreaId,
              allocArea.AreaName AS allocatedAreaName,
              CAST(NULL AS INT) AS theoryScore,
              CAST(NULL AS INT) AS practicalScore,
              CAST(NULL AS INT) AS roadTestScore
            FROM Candidate c
            INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
            INNER JOIN [Session] s ON s.SessionId = ee.SessionId
            INNER JOIN Exam ex ON ex.ExamId = s.ExamId
            INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
            LEFT JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
            LEFT JOIN [User] u ON u.UserId = prof.UserId
            LEFT JOIN (
                SELECT p1.ExamEnrollmentId, MIN(p1.PaymentId) AS PaymentId
                FROM Payment p1
                WHERE p1.PaymentStatus IN (N'Completed', N'Paid', N'Hoàn tất')
                GROUP BY p1.ExamEnrollmentId
            ) pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
            LEFT JOIN ExamDevice dev ON dev.ExamDeviceId = ee.ExamDeviceId
            LEFT JOIN ExamArea allocArea ON allocArea.ExamAreaId = dev.ExamAreaId
            """;
}
