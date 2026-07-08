-- ============================================
-- Patch: ExamEnrollment ca lý thuyết kỳ B-20260615 cho SBD 050–060
-- Chạy khi thí sinh đã có trong Candidate nhưng chưa ghi danh ca
-- "Ca sáng - Lý thuyết B (kỳ 2)".
-- ============================================

USE DLEM_DB_2;
GO

INSERT INTO ExamEnrollment (CandidateId, SessionId, SectionStatus, SignaturePrinted, ExamDeviceId)
SELECT c.CandidateId, s.SessionId, N'Pending', 0, NULL
FROM Candidate c
CROSS JOIN [Session] s
WHERE c.CandidateNumber IN (
    N'050', N'051', N'052', N'053', N'054', N'055', N'056', N'057', N'058', N'059', N'060'
)
  AND s.SessionName = N'Ca sáng - Lý thuyết B (kỳ 2)'
  AND NOT EXISTS (
    SELECT 1 FROM ExamEnrollment ee
    WHERE ee.CandidateId = c.CandidateId AND ee.SessionId = s.SessionId
  );
GO
