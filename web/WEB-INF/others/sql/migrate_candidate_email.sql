-- Candidate.Email đã có trong DDL_DLEM_DB.sql (CREATE TABLE Candidate).
-- Script này giữ để tương thích pipeline cũ — no-op trên DB mới.
USE DLEM_DB_2;
GO

-- No-op: Email đã nằm trong DDL. Chỉ ALTER nếu DB cũ thiếu cột.
IF COL_LENGTH('dbo.Candidate', 'Email') IS NULL
BEGIN
    ALTER TABLE dbo.Candidate ADD Email NVARCHAR(255) NULL;
END
GO
