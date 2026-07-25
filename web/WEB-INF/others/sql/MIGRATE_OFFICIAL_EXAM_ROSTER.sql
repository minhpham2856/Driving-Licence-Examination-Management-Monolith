USE DLEM_DB_2;
GO
SET QUOTED_IDENTIFIER ON;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.OfficialExamCandidate', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.OfficialExamCandidate (
        OfficialExamCandidateId INT IDENTITY(1,1) PRIMARY KEY,
        ExamDateId INT NOT NULL REFERENCES dbo.ExamDates(ExamDateId),
        ExamRegistrationId INT NULL REFERENCES dbo.ExamRegistration(ExamRegistrationId),
        LicenceId INT NOT NULL REFERENCES dbo.Licence(LicenceId),
        CandidateNumber NVARCHAR(50) NULL,
        FullName NVARCHAR(255) NOT NULL,
        DateOfBirth DATE NOT NULL,
        GovernmentIdNumber NVARCHAR(100) NOT NULL,
        PhoneNumber NVARCHAR(20) NOT NULL,
        Email NVARCHAR(255) NOT NULL,
        SourceUnitCode NVARCHAR(50) NOT NULL,
        SourceUnitName NVARCHAR(255) NOT NULL,
        CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_OfficialExamCandidate_CreatedAt DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_OfficialExamCandidate_Date_CCCD UNIQUE(ExamDateId, GovernmentIdNumber)
    );
    CREATE UNIQUE INDEX UX_OfficialExamCandidate_Date_Number
        ON dbo.OfficialExamCandidate(ExamDateId, CandidateNumber)
        WHERE CandidateNumber IS NOT NULL;
    CREATE INDEX IX_OfficialExamCandidate_Date ON dbo.OfficialExamCandidate(ExamDateId);
END;

IF COL_LENGTH(N'dbo.Candidate', N'SourceUnitCode') IS NULL
    ALTER TABLE dbo.Candidate ADD SourceUnitCode NVARCHAR(50) NULL;
IF COL_LENGTH(N'dbo.Candidate', N'SourceUnitName') IS NULL
    ALTER TABLE dbo.Candidate ADD SourceUnitName NVARCHAR(255) NULL;

-- Bù dữ liệu cho những danh sách đã được CSGT hoàn tất trước khi có bảng mới.
INSERT dbo.OfficialExamCandidate
    (ExamDateId,ExamRegistrationId,LicenceId,CandidateNumber,FullName,DateOfBirth,
     GovernmentIdNumber,PhoneNumber,Email,SourceUnitCode,SourceUnitName)
SELECT rd.ExamDateId,er.ExamRegistrationId,er.LicenceId,rd.OfficialCandidateNumber,
       p.FullName,CAST(p.DateOfBirth AS date),p.GovernmentIdNumber,p.PhoneNumber,u.Email,
       N'LAIVUI',N'Trung tâm sát hạch Lái Vui'
FROM dbo.RegistrationDates rd
JOIN dbo.ExamDates ed ON ed.ExamDateId=rd.ExamDateId AND ed.PoliceStatus=N'COMPLETED'
JOIN dbo.ExamRegistration er ON er.ExamRegistrationId=rd.ExamRegistrationId
JOIN dbo.Profile p ON p.ProfileId=er.ProfileId
JOIN dbo.[User] u ON u.UserId=p.UserId
WHERE rd.IsActive=1 AND rd.PoliceStatus=N'APPROVED'
  AND NOT EXISTS (SELECT 1 FROM dbo.OfficialExamCandidate o
                  WHERE o.ExamDateId=rd.ExamDateId AND o.GovernmentIdNumber=p.GovernmentIdNumber);

UPDATE dbo.OfficialExamCandidate
SET SourceUnitName=N'Trung tâm sát hạch Lái Vui'
WHERE SourceUnitCode=N'LAIVUI';

COMMIT TRANSACTION;
GO
