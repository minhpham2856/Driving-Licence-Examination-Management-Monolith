-- Idempotent migration for existing DLEM_DB_2 dev databases.
-- Fresh setup: chỉ cần DDL_DLEM_DB.sql + DML_DLEM_DB.sql.

USE DLEM_DB_2;
GO

IF COL_LENGTH('dbo.ExamRegistration', 'IsRetake') IS NULL
BEGIN
    ALTER TABLE ExamRegistration
        ADD IsRetake BIT NOT NULL CONSTRAINT DF_ExamRegistration_IsRetake DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.OfficialExamCandidate', 'ExamParticipationType') IS NULL
BEGIN
    ALTER TABLE OfficialExamCandidate
        ADD ExamParticipationType NVARCHAR(30) NOT NULL
            CONSTRAINT DF_OfficialExamCandidate_Participation DEFAULT N'FULL_EXAM';
END
GO

IF COL_LENGTH('dbo.Exam', 'ExamPassword') IS NULL
BEGIN
    ALTER TABLE Exam ADD ExamPassword NVARCHAR(255) NULL;
END
GO

IF COL_LENGTH('dbo.ExamEnrollmentSection', 'CheckedInAt') IS NULL
BEGIN
    ALTER TABLE ExamEnrollmentSection ADD CheckedInAt DATETIME NULL;
END
GO

IF COL_LENGTH('dbo.ExamEnrollmentSection', 'CheckedInBy') IS NULL
BEGIN
    ALTER TABLE ExamEnrollmentSection ADD CheckedInBy INT NULL REFERENCES [User](UserId);
END
GO

IF COL_LENGTH('dbo.ExamEnrollmentSection', 'ResultPrintedAt') IS NULL
BEGIN
    ALTER TABLE ExamEnrollmentSection ADD ResultPrintedAt DATETIME NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = N'CandidateViolation' AND schema_id = SCHEMA_ID(N'dbo'))
BEGIN
    CREATE TABLE CandidateViolation (
        CandidateViolationId INT PRIMARY KEY IDENTITY(1,1),
        ExamEnrollmentSectionId INT NOT NULL REFERENCES ExamEnrollmentSection(ExamEnrollmentSectionId),
        Reason NVARCHAR(100) NOT NULL,
        Details NVARCHAR(2000),
        EvidenceUrl NVARCHAR(500) NOT NULL,
        CreatedBy INT NOT NULL REFERENCES [User](UserId),
        CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
    );
END
GO

PRINT N'ALTER_merge_mainTest_registration.sql completed.';
GO
