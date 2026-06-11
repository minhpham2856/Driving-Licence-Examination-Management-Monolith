-- Tạo bảng Candidate (SBD import Công an) và migrate dữ liệu cũ từ ExamRegistration.candidateNo.
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID('Candidate', 'U') IS NULL
BEGIN
    CREATE TABLE Candidate (
        id INT IDENTITY(1,1) PRIMARY KEY,
        examSessionId INT NOT NULL REFERENCES ExamSession(id),
        personId INT NULL REFERENCES Person(id),
        examRegistrationId INT NULL REFERENCES ExamRegistration(id),
        candidateNo NVARCHAR(50) NOT NULL,
        govIdNo NVARCHAR(50) NULL,
        fullName NVARCHAR(200) NULL,
        dateOfBirth DATE NULL,
        licenseCode NVARCHAR(10) NULL,
        importedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        importedBy INT NULL REFERENCES [User](id)
    );

    CREATE INDEX IX_Candidate_examSessionId ON Candidate(examSessionId);
    CREATE INDEX IX_Candidate_personId ON Candidate(personId);
    CREATE INDEX IX_Candidate_govIdNo ON Candidate(govIdNo);

    CREATE UNIQUE INDEX UQ_Candidate_session_sbd
        ON Candidate (examSessionId, candidateNo);

    CREATE UNIQUE INDEX UQ_Candidate_session_person
        ON Candidate (examSessionId, personId)
        WHERE personId IS NOT NULL;
END
GO

-- Chuyển SBD số cũ sang Candidate (nếu chưa có).
INSERT INTO Candidate (examSessionId, personId, examRegistrationId, candidateNo, govIdNo, fullName, dateOfBirth, licenseCode)
SELECT er.examSessionId,
       er.personId,
       er.id,
       CAST(er.candidateNo AS NVARCHAR(50)),
       p.govIdNo,
       p.fullName,
       p.dateOfBirth,
       lt.licenseCode
FROM ExamRegistration er
JOIN Person p ON p.id = er.personId
JOIN ExamSession es ON es.id = er.examSessionId
JOIN LicenseType lt ON lt.id = es.licenseTypeId
WHERE er.candidateNo IS NOT NULL
  AND er.candidateNo > 0
  AND NOT EXISTS (
      SELECT 1 FROM Candidate c
      WHERE c.examSessionId = er.examSessionId AND c.personId = er.personId
  );
GO
