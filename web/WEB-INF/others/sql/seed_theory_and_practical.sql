USE DLEM_DB_2;
GO

DECLARE @SessionId INT = (SELECT TOP 1 SessionId FROM [Session] WHERE SessionName LIKE N'%B-1292%');
DECLARE @TheorySectionId INT = 1;
DECLARE @PracticalSectionId INT = 2;
DECLARE @DeviceId INT = (SELECT TOP 1 ExamDeviceId FROM ExamDevice WHERE DeviceType = 'computer');

-- Chọn 20 ExamEnrollmentId từ B1292
DECLARE @EnrollmentList TABLE (ExamEnrollmentId INT, CandidateId INT);
INSERT INTO @EnrollmentList
SELECT TOP 20 ExamEnrollmentId, CandidateId 
FROM ExamEnrollment 
WHERE SessionId = @SessionId
ORDER BY ExamEnrollmentId;

-- Xóa dữ liệu cũ nếu chạy lại
DELETE FROM DeductionRecord WHERE ExamScoreId IN (SELECT ExamScoreId FROM ExamScore WHERE ExamResultId IN (SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId IN (SELECT ExamEnrollmentId FROM @EnrollmentList)));
DELETE FROM ExamScore WHERE ExamResultId IN (SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId IN (SELECT ExamEnrollmentId FROM @EnrollmentList));
DELETE FROM ExamResult WHERE ExamEnrollmentId IN (SELECT ExamEnrollmentId FROM @EnrollmentList);

-- Tạo ExamResult (đại diện cho việc đã có kết quả)
INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ExamEnrollmentId, 1, GETDATE() - 1
FROM @EnrollmentList;

-- Tạo ExamScore (lưu điểm lý thuyết)
INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId, @TheorySectionId, 35
FROM ExamResult er
JOIN @EnrollmentList e ON er.ExamEnrollmentId = e.ExamEnrollmentId;

-- 2. Seed Vi phạm Sa hình (Deduction) cho 5 trong số 20 thí sinh
DECLARE @ViolationList TABLE (ExamEnrollmentId INT);
INSERT INTO @ViolationList
SELECT TOP 5 ExamEnrollmentId FROM @EnrollmentList ORDER BY ExamEnrollmentId DESC;

-- Tạo điểm Sa hình (ban đầu 95)
INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId, @PracticalSectionId, 95
FROM ExamResult er
JOIN @ViolationList v ON er.ExamEnrollmentId = v.ExamEnrollmentId;

-- Thêm DeductionRecord (Lỗi đè vạch)
INSERT INTO DeductionRecord (ExamScoreId, ScoreDeductionId, OccurrenceCount, RecordedAt)
SELECT 
    es.ExamScoreId,
    (SELECT TOP 1 ScoreDeductionId FROM ScoreDeduction WHERE Reason LIKE N'%đè vạch%' OR Points = 5),
    1,
    GETDATE()
FROM ExamScore es
JOIN ExamResult er ON es.ExamResultId = er.ExamResultId
JOIN @ViolationList v ON er.ExamEnrollmentId = v.ExamEnrollmentId
WHERE es.ExamSectionId = @PracticalSectionId;
GO
