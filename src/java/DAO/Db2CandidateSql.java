package DAO;

/**
 * Truy vấn thí sinh theo schema DLEM_DB_2 (Candidate, Exam_Candidate, Session, …).
 */
public final class Db2CandidateSql {

    private Db2CandidateSql() {
    }

    public static final String CANDIDATE_SELECT = """
            SELECT
              c.CandidateId AS id,
              ec.SessionId AS examSessionId,
              er.ProfileId AS personId,
              TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT) AS candidateNo,
              CASE WHEN er.RegistrationStatus = 'WalkIn' THEN 'WalkIn' ELSE 'PreRegistered' END AS registrationType,
              CAST(CASE WHEN pay.PaymentId IS NOT NULL THEN 1 ELSE 0 END AS BIT) AS isPaymentCompleted,
              CAST(CASE WHEN er.RegistrationStatus IN ('CheckedIn','Present','Completed') THEN 1 ELSE 0 END AS BIT) AS isPresent,
              CAST(NULL AS DATETIME) AS presentMarkedAt,
              er.Notes AS notes,
              c.FullName AS fullName,
              ISNULL(c.GovernmentIdNumber, p.GovernmentIdNumber) AS govIdNo,
              CAST(c.DateOfBirth AS DATE) AS dateOfBirth,
              CASE WHEN ISNULL(c.Sex, p.Sex) IN (N'Nam', N'Male', N'M') THEN 0 ELSE 1 END AS gender,
              ISNULL(c.PhoneNumber, p.PhoneNumber) AS phoneNo,
              u.Email AS email,
              c.PhotoImageUrl AS photoUrl,
              l.LicenceClass AS licenseCode,
              dev.DeviceName AS computerCode,
              NULL AS allocatedAreaId,
              NULL AS allocatedAreaName,
              theory.scoreVal AS theoryScore,
              practical.scoreVal AS practicalScore,
              road.scoreVal AS roadTestScore
            FROM Candidate c
            INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
            INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
            INNER JOIN [Session] s ON s.SessionId = ec.SessionId
            INNER JOIN Exam ex ON ex.ExamId = ec.ExamId
            INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
            INNER JOIN [User] u ON u.UserId = c.UserId
            INNER JOIN Profile p ON p.ProfileId = er.ProfileId
            LEFT JOIN (
                SELECT p1.CandidateId, MIN(p1.PaymentId) AS PaymentId
                FROM Payment p1
                WHERE p1.PaymentStatus IN ('Completed', 'Paid')
                GROUP BY p1.CandidateId
            ) pay ON pay.CandidateId = c.CandidateId
            LEFT JOIN (
                SELECT ec2.CandidateId, ed.DeviceName,
                       ROW_NUMBER() OVER (PARTITION BY ec2.CandidateId ORDER BY tp.TheoryPaperId DESC) AS rn
                FROM Exam_Candidate ec2
                JOIN TheoryPaper tp ON tp.ExamCandidateId = ec2.ExamCandidateId
                JOIN ExamDevice ed ON ed.ExamDeviceId = tp.ExamDeviceId
            ) dev ON dev.CandidateId = c.CandidateId AND dev.rn = 1
            LEFT JOIN (
                SELECT ec3.CandidateId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM Exam_Candidate ec3
                JOIN ExamResult er2 ON er2.ExamCandidateId = ec3.ExamCandidateId
                JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionName LIKE N'%Lý thuyết%' OR sec.SectionName LIKE '%Theory%'
                GROUP BY ec3.CandidateId
            ) theory ON theory.CandidateId = c.CandidateId
            LEFT JOIN (
                SELECT ec3.CandidateId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM Exam_Candidate ec3
                JOIN ExamResult er2 ON er2.ExamCandidateId = ec3.ExamCandidateId
                JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionName LIKE N'%Thực hành%' OR sec.SectionName LIKE N'%Sa hình%' OR sec.SectionName LIKE '%Practical%'
                GROUP BY ec3.CandidateId
            ) practical ON practical.CandidateId = c.CandidateId
            LEFT JOIN (
                SELECT ec3.CandidateId, CAST(MAX(es.Score) AS INT) AS scoreVal
                FROM Exam_Candidate ec3
                JOIN ExamResult er2 ON er2.ExamCandidateId = ec3.ExamCandidateId
                JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE sec.SectionName LIKE N'%Đường%' OR sec.SectionName LIKE '%Road%'
                GROUP BY ec3.CandidateId
            ) road ON road.CandidateId = c.CandidateId
            """;
}
