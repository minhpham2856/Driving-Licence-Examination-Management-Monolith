USE DLEM_DB_2;
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRANSACTION;

IF COL_LENGTH('dbo.ExamDates', 'Status') IS NULL
BEGIN
    ALTER TABLE dbo.ExamDates
        ADD Status NVARCHAR(20) NOT NULL
            CONSTRAINT DF_ExamDates_Status DEFAULT N'Open' WITH VALUES;
END;

IF COL_LENGTH('dbo.ExamDates', 'CancelReason') IS NULL
    ALTER TABLE dbo.ExamDates ADD CancelReason NVARCHAR(500) NULL;

IF COL_LENGTH('dbo.ExamDates', 'CancelledAt') IS NULL
    ALTER TABLE dbo.ExamDates ADD CancelledAt DATETIME2 NULL;

IF COL_LENGTH('dbo.ExamDates', 'CancelledBy') IS NULL
    ALTER TABLE dbo.ExamDates ADD CancelledBy INT NULL;

IF COL_LENGTH('dbo.ExamDates', 'CancelledRegistrationCount') IS NULL
    ALTER TABLE dbo.ExamDates ADD CancelledRegistrationCount INT NULL;

GO

IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.ExamDates')
      AND name = 'CK_ExamDates_Status'
)
BEGIN
    ALTER TABLE dbo.ExamDates DROP CONSTRAINT CK_ExamDates_Status;
END;

ALTER TABLE dbo.ExamDates WITH CHECK
    ADD CONSTRAINT CK_ExamDates_Status
        CHECK (Status IN (N'Open', N'Locked', N'Cancelled'));

COMMIT TRANSACTION;

GO

SELECT ExamDateId, ExamDate, LicenceId, Status, CancelReason,
       CancelledAt, CancelledBy, CancelledRegistrationCount
FROM dbo.ExamDates
ORDER BY ExamDate DESC, ExamDateId DESC;
