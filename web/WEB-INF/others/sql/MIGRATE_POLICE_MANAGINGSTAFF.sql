-- ============================================================
-- MIGRATE_POLICE_MANAGINGSTAFF.sql
-- Nâng cấp DB đang có dữ liệu lên schema hợp nhất
-- (examiner/candidate hiện tại + police/managing staff từ mainTest).
-- Idempotent: chạy lại an toàn (IF COL_LENGTH / IF OBJECT_ID / IF NOT EXISTS).
-- Không drop bảng examiner (CandidateViolation, CheckedIn*, ExamPassword).
-- ============================================================

USE DLEM_DB_2;
GO

SET QUOTED_IDENTIFIER ON;
SET XACT_ABORT ON;
GO

BEGIN TRANSACTION;

-- ============================== ExamDates ==============================

IF COL_LENGTH(N'dbo.ExamDates', N'Status') IS NULL
    ALTER TABLE dbo.ExamDates ADD [Status] NVARCHAR(20) NOT NULL
        CONSTRAINT DF_ExamDates_Status DEFAULT N'Open';

IF COL_LENGTH(N'dbo.ExamDates', N'PoliceStatus') IS NULL
    ALTER TABLE dbo.ExamDates ADD PoliceStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_ExamDates_PoliceStatus DEFAULT N'NOT_SENT';

IF COL_LENGTH(N'dbo.ExamDates', N'CancelReason') IS NULL
    ALTER TABLE dbo.ExamDates ADD CancelReason NVARCHAR(500) NULL;

IF COL_LENGTH(N'dbo.ExamDates', N'CancelledAt') IS NULL
    ALTER TABLE dbo.ExamDates ADD CancelledAt DATETIME2 NULL;

IF COL_LENGTH(N'dbo.ExamDates', N'CancelledBy') IS NULL
    ALTER TABLE dbo.ExamDates ADD CancelledBy INT NULL
        CONSTRAINT FK_ExamDates_CancelledBy REFERENCES dbo.[User](UserId);

IF COL_LENGTH(N'dbo.ExamDates', N'CancelledRegistrationCount') IS NULL
    ALTER TABLE dbo.ExamDates ADD CancelledRegistrationCount INT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_ExamDates_Status' AND parent_object_id = OBJECT_ID(N'dbo.ExamDates')
)
    ALTER TABLE dbo.ExamDates ADD CONSTRAINT CK_ExamDates_Status
        CHECK ([Status] IN (N'Open', N'Locked', N'Cancelled'));

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_ExamDates_PoliceStatus' AND parent_object_id = OBJECT_ID(N'dbo.ExamDates')
)
    ALTER TABLE dbo.ExamDates ADD CONSTRAINT CK_ExamDates_PoliceStatus
        CHECK (PoliceStatus IN (N'NOT_SENT', N'PENDING', N'COMPLETED'));

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_ExamDates_CancelledRegistrationCount'
      AND parent_object_id = OBJECT_ID(N'dbo.ExamDates')
)
    ALTER TABLE dbo.ExamDates ADD CONSTRAINT CK_ExamDates_CancelledRegistrationCount
        CHECK (CancelledRegistrationCount IS NULL OR CancelledRegistrationCount >= 0);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_ExamDates_ExamDate' AND object_id = OBJECT_ID(N'dbo.ExamDates'))
    CREATE UNIQUE INDEX UX_ExamDates_ExamDate ON dbo.ExamDates(ExamDate);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ExamDates_Status_PoliceStatus_Date' AND object_id = OBJECT_ID(N'dbo.ExamDates')
)
    CREATE INDEX IX_ExamDates_Status_PoliceStatus_Date
        ON dbo.ExamDates([Status], PoliceStatus, ExamDate);

-- ============================== RegistrationDates ==============================

IF COL_LENGTH(N'dbo.RegistrationDates', N'PoliceStatus') IS NULL
    ALTER TABLE dbo.RegistrationDates ADD PoliceStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_RegistrationDates_PoliceStatus DEFAULT N'NOT_SENT';

IF COL_LENGTH(N'dbo.RegistrationDates', N'PoliceReason') IS NULL
    ALTER TABLE dbo.RegistrationDates ADD PoliceReason NVARCHAR(500) NULL;

IF COL_LENGTH(N'dbo.RegistrationDates', N'OfficialCandidateNumber') IS NULL
    ALTER TABLE dbo.RegistrationDates ADD OfficialCandidateNumber NVARCHAR(50) NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_RegistrationDates_PoliceStatus'
      AND parent_object_id = OBJECT_ID(N'dbo.RegistrationDates')
)
    ALTER TABLE dbo.RegistrationDates ADD CONSTRAINT CK_RegistrationDates_PoliceStatus
        CHECK (PoliceStatus IN (N'NOT_SENT', N'PENDING', N'APPROVED', N'REJECTED'));

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_RegistrationDates_ExamDate_PoliceStatus'
      AND object_id = OBJECT_ID(N'dbo.RegistrationDates')
)
    CREATE INDEX IX_RegistrationDates_ExamDate_PoliceStatus
        ON dbo.RegistrationDates(ExamDateId, PoliceStatus, IsActive);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_RegistrationDates_Date_CandidateNumber'
      AND object_id = OBJECT_ID(N'dbo.RegistrationDates')
)
    CREATE UNIQUE INDEX UX_RegistrationDates_Date_CandidateNumber
        ON dbo.RegistrationDates(ExamDateId, OfficialCandidateNumber)
        WHERE OfficialCandidateNumber IS NOT NULL;

-- ============================== OfficialExamCandidate ==============================

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

-- ============================== Exam ==============================

IF COL_LENGTH(N'dbo.Exam', N'SourceExamDateId') IS NULL
    ALTER TABLE dbo.Exam ADD SourceExamDateId INT NULL
        CONSTRAINT FK_Exam_SourceExamDateId REFERENCES dbo.ExamDates(ExamDateId);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_Exam_SourceExamDateId' AND object_id = OBJECT_ID(N'dbo.Exam')
)
    CREATE UNIQUE INDEX UX_Exam_SourceExamDateId
        ON dbo.Exam(SourceExamDateId)
        WHERE SourceExamDateId IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_Exam_Date_Status' AND object_id = OBJECT_ID(N'dbo.Exam')
)
    CREATE INDEX IX_Exam_Date_Status ON dbo.Exam(ExamDate, [Status]);

-- ============================== Candidate ==============================

IF COL_LENGTH(N'dbo.Candidate', N'SourceUnitCode') IS NULL
    ALTER TABLE dbo.Candidate ADD SourceUnitCode NVARCHAR(50) NULL;

IF COL_LENGTH(N'dbo.Candidate', N'SourceUnitName') IS NULL
    ALTER TABLE dbo.Candidate ADD SourceUnitName NVARCHAR(255) NULL;

-- ============================== ExamEnrollment ==============================

IF COL_LENGTH(N'dbo.ExamEnrollment', N'ExamRegistrationId') IS NULL
    ALTER TABLE dbo.ExamEnrollment ADD ExamRegistrationId INT NULL
        CONSTRAINT FK_ExamEnrollment_ExamRegistrationId
        REFERENCES dbo.ExamRegistration(ExamRegistrationId);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_ExamEnrollment_Exam_Registration'
      AND object_id = OBJECT_ID(N'dbo.ExamEnrollment')
)
    CREATE UNIQUE INDEX UX_ExamEnrollment_Exam_Registration
        ON dbo.ExamEnrollment(ExamId, ExamRegistrationId)
        WHERE ExamRegistrationId IS NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ExamEnrollment_Exam' AND object_id = OBJECT_ID(N'dbo.ExamEnrollment')
)
    CREATE INDEX IX_ExamEnrollment_Exam ON dbo.ExamEnrollment(ExamId);

-- ============================== Indexes khác ==============================

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ExamRegistration_Profile_Status'
      AND object_id = OBJECT_ID(N'dbo.ExamRegistration')
)
    CREATE INDEX IX_ExamRegistration_Profile_Status
        ON dbo.ExamRegistration(ProfileId, RegistrationStatus);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_Document_Profile_Type' AND object_id = OBJECT_ID(N'dbo.Document')
)
    CREATE INDEX IX_Document_Profile_Type
        ON dbo.Document(ProfileId, DocumentTypeId);

-- ============================== Role Cán bộ CSGT ==============================

IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE RoleName = N'Cán bộ CSGT')
    INSERT INTO dbo.[Role](RoleName) VALUES (N'Cán bộ CSGT');

-- ============================== Backfill OfficialExamCandidate ==============================
-- LicenceId lấy từ ExamDates (ed.LicenceId) cho khớp PoliceSubmissionDAOImpl

INSERT dbo.OfficialExamCandidate
    (ExamDateId, ExamRegistrationId, LicenceId, CandidateNumber, FullName, DateOfBirth,
     GovernmentIdNumber, PhoneNumber, Email, SourceUnitCode, SourceUnitName)
SELECT rd.ExamDateId, er.ExamRegistrationId, ed.LicenceId, rd.OfficialCandidateNumber,
       p.FullName, CAST(p.DateOfBirth AS date), p.GovernmentIdNumber, p.PhoneNumber, u.Email,
       N'LAIVUI', N'Trung tâm sát hạch Lái Vui'
FROM dbo.RegistrationDates rd
JOIN dbo.ExamDates ed ON ed.ExamDateId = rd.ExamDateId AND ed.PoliceStatus = N'COMPLETED'
JOIN dbo.ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
JOIN dbo.Profile p ON p.ProfileId = er.ProfileId
JOIN dbo.[User] u ON u.UserId = p.UserId
WHERE rd.IsActive = 1 AND rd.PoliceStatus = N'APPROVED'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.OfficialExamCandidate o
      WHERE o.ExamDateId = rd.ExamDateId AND o.GovernmentIdNumber = p.GovernmentIdNumber
  );

UPDATE dbo.OfficialExamCandidate
SET SourceUnitName = N'Trung tâm sát hạch Lái Vui'
WHERE SourceUnitCode = N'LAIVUI'
  AND (SourceUnitName IS NULL OR SourceUnitName <> N'Trung tâm sát hạch Lái Vui');

COMMIT TRANSACTION;
GO

PRINT N'MIGRATE_POLICE_MANAGINGSTAFF: hoàn tất.';
GO
