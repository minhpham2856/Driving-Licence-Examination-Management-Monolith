-- SBD chưa gán khi đăng ký; staff import từ danh sách Công an sau.
-- Chạy trên DB đã có DDL cũ (candidateNo NOT NULL + unique).
SET QUOTED_IDENTIFIER ON;
GO

IF EXISTS (
    SELECT 1 FROM sys.key_constraints
    WHERE name = 'UQ_ExamRegistration_session_candidate'
)
BEGIN
    ALTER TABLE ExamRegistration DROP CONSTRAINT UQ_ExamRegistration_session_candidate;
END
GO

ALTER TABLE ExamRegistration ALTER COLUMN candidateNo INT NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UQ_ExamRegistration_session_candidate_assigned'
      AND object_id = OBJECT_ID('ExamRegistration')
)
BEGIN
    CREATE UNIQUE INDEX UQ_ExamRegistration_session_candidate_assigned
        ON ExamRegistration (examSessionId, candidateNo)
        WHERE candidateNo IS NOT NULL AND candidateNo > 0;
END
GO
