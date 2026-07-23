IF COL_LENGTH('dbo.ExamEnrollmentSection', 'CheckedInAt') IS NULL
BEGIN
    ALTER TABLE dbo.ExamEnrollmentSection ADD CheckedInAt DATETIME NULL;
END;
GO

IF COL_LENGTH('dbo.ExamEnrollmentSection', 'CheckedInBy') IS NULL
BEGIN
    ALTER TABLE dbo.ExamEnrollmentSection ADD CheckedInBy INT NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_ExamEnrollmentSection_CheckedInBy_User'
)
BEGIN
    ALTER TABLE dbo.ExamEnrollmentSection
    ADD CONSTRAINT FK_ExamEnrollmentSection_CheckedInBy_User
    FOREIGN KEY (CheckedInBy) REFERENCES dbo.[User](UserId);
END;
GO

UPDATE ees
SET CheckedInAt = NULL,
    CheckedInBy = NULL
FROM dbo.ExamEnrollmentSection ees
WHERE ees.CheckedInAt IS NOT NULL
  AND ISNULL(ees.Status, N'Chưa thi') = N'Chưa thi';
GO
