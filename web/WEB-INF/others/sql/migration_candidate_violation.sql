IF OBJECT_ID(N'dbo.CandidateViolation', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.CandidateViolation (
        CandidateViolationId INT PRIMARY KEY IDENTITY(1,1),
        ExamEnrollmentSectionId INT NOT NULL REFERENCES dbo.ExamEnrollmentSection(ExamEnrollmentSectionId),
        Reason NVARCHAR(100) NOT NULL,
        Details NVARCHAR(2000) NULL,
        EvidenceUrl NVARCHAR(500) NOT NULL,
        CreatedBy INT NOT NULL REFERENCES dbo.[User](UserId),
        CreatedAt DATETIME NOT NULL CONSTRAINT DF_CandidateViolation_CreatedAt DEFAULT GETDATE()
    );
END;
GO
