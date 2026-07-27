-- Drop DeductionRecord.RecordedAt (fault time no longer stored/displayed).
IF COL_LENGTH('dbo.DeductionRecord', 'RecordedAt') IS NOT NULL
BEGIN
    ALTER TABLE dbo.DeductionRecord DROP COLUMN RecordedAt;
END
GO
