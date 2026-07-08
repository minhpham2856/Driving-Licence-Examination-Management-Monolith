-- ============================================
-- Seed / reset SBD 001 – ca sáng lý thuyết B1
-- Mục đích: test in đề thi + hoàn tất phần thi -> chuyển queue
--
-- Trạng thái mặc định: Chờ ký + đã in biên bản (SignaturePrinted = 1)
--   -> Vào /views/examiner/candidate-call, bấm "Hoàn tất phần thi" để chuyển queue TH trong hình.
--
-- Muốn chỉ xem đề (không test queue): đổi @TargetStatus thành N'Đã thi'.
-- ============================================

USE DLEM_DB_2;
GO

DECLARE @EnrollmentId INT;
DECLARE @TheoryPaperId INT;
DECLARE @DeviceId INT;
DECLARE @AnswerCount INT;
DECLARE @TargetStatus NVARCHAR(50) = N'Chờ ký';  -- hoặc N'Đã thi'

SELECT
    @EnrollmentId = ec.ExamEnrollmentId,
    @DeviceId = COALESCE(
        ec.ExamDeviceId,
        (SELECT TOP 1 ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-04')
    )
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
JOIN Exam e ON e.ExamId = s.ExamId
WHERE c.CandidateNumber = N'001'
  AND e.ExamCode = N'B1-20260601'
  AND s.IsMorningSession = 1
  AND s.StartTime = '2026-06-01 07:30:00';

IF @EnrollmentId IS NULL
BEGIN
    RAISERROR(N'Không tìm thấy ghi danh SBD 001 ca sáng lý thuyết B1. Chạy DML_DLEM_DB.sql trước.', 16, 1);
    RETURN;
END;

-- Xóa đề / đáp án cũ
DELETE ca
FROM CandidateAnswer ca
JOIN TheoryPaper tp ON tp.TheoryPaperId = ca.TheoryPaperId
WHERE tp.ExamEnrollmentId = @EnrollmentId;

DELETE FROM TheoryPaper WHERE ExamEnrollmentId = @EnrollmentId;

-- Tạo đề ngẫu nhiên 35 câu (hạng B1)
INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
VALUES (
    @EnrollmentId,
    @DeviceId,
    DATEADD(MINUTE, -28, GETDATE()),
    DATEADD(MINUTE, -10, GETDATE())
);

SET @TheoryPaperId = SCOPE_IDENTITY();

INSERT INTO CandidateAnswer (TheoryPaperId, QuestionId, Answer)
SELECT
    @TheoryPaperId,
    picked.QuestionId,
    CASE
        WHEN ABS(CHECKSUM(NEWID())) % 10 < 8 THEN picked.CorrectAnswer
        ELSE CASE ABS(CHECKSUM(NEWID())) % 4
            WHEN 0 THEN N'A'
            WHEN 1 THEN N'B'
            WHEN 2 THEN N'C'
            ELSE N'D'
        END
    END
FROM (
    SELECT TOP 35
        q.QuestionId,
        q.CorrectAnswer
    FROM Question q
    INNER JOIN Licence_Question lq ON lq.QuestionId = q.QuestionId
    INNER JOIN Licence l ON l.LicenceId = lq.LicenceId AND l.LicenceClass = N'B1'
    ORDER BY NEWID()
) picked;

-- Cập nhật trạng thái thí sinh
UPDATE ExamEnrollment
SET SectionStatus = @TargetStatus,
    SignaturePrinted = CASE WHEN @TargetStatus = N'Chờ ký' THEN 1 ELSE SignaturePrinted END,
    ExamDeviceId = @DeviceId
WHERE ExamEnrollmentId = @EnrollmentId;

SELECT @AnswerCount = COUNT(*) FROM CandidateAnswer WHERE TheoryPaperId = @TheoryPaperId;

PRINT N'Đã seed SBD 001: TheoryPaperId = ' + CAST(@TheoryPaperId AS NVARCHAR(20))
    + N', trạng thái = ' + @TargetStatus
    + N', số câu trả lời = ' + CAST(@AnswerCount AS NVARCHAR(10));
GO
