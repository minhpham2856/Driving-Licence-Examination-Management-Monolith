IF COL_LENGTH('dbo.Exam', 'ExamPassword') IS NULL
BEGIN
    ALTER TABLE dbo.Exam ADD ExamPassword NVARCHAR(255) NULL;
END
GO

UPDATE dbo.Exam
SET ExamPassword = N'exam123'
WHERE ExamCode = N'A1-20260601-1000'
  AND (ExamPassword IS NULL OR LTRIM(RTRIM(ExamPassword)) = N'');
GO
