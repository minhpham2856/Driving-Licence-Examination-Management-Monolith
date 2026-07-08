-- Per-section result document print timestamp (run once on existing DB).
IF COL_LENGTH('ExamEnrollmentSection', 'ResultPrintedAt') IS NULL
   AND COL_LENGTH('ExamEnrollmentSection', 'MinutesPrintedAt') IS NOT NULL
BEGIN
    EXEC sp_rename 'ExamEnrollmentSection.MinutesPrintedAt', 'ResultPrintedAt', 'COLUMN';
END
GO

IF COL_LENGTH('ExamEnrollmentSection', 'ResultPrintedAt') IS NULL
BEGIN
    ALTER TABLE ExamEnrollmentSection
        ADD ResultPrintedAt DATETIME NULL;
END
GO
