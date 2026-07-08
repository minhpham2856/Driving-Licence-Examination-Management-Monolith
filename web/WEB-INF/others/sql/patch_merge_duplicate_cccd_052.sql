-- Gộp thí sinh trùng CCCD 038400100002 (Lê Văn Chi): giữ CandidateId 123, xóa 178
-- Chạy SAU KHI xác nhận thủ tục/ảnh/lệ phí đang nằm trên bản ghi 123 (không phải 178).
-- Kiểm tra: SELECT CandidateId, FullName, GovernmentIdNumber FROM Candidate WHERE GovernmentIdNumber = N'038400100002';

BEGIN TRANSACTION;

DECLARE @keep INT = 123;
DECLARE @drop INT = 178;

-- Chuyển thanh toán sang enrollment của bản ghi giữ (nếu bản xóa có payment mà bản giữ chưa có)
UPDATE p
SET p.ExamEnrollmentId = keepEe.ExamEnrollmentId
FROM Payment p
INNER JOIN ExamEnrollment dropEe ON dropEe.ExamEnrollmentId = p.ExamEnrollmentId
INNER JOIN ExamEnrollment keepEe ON keepEe.CandidateId = @keep AND keepEe.SessionId = dropEe.SessionId
WHERE dropEe.CandidateId = @drop
  AND NOT EXISTS (
      SELECT 1 FROM Payment p2
      WHERE p2.ExamEnrollmentId = keepEe.ExamEnrollmentId
  );

-- Xóa kết quả/điểm của bản ghi trùng (thứ tự: DeductionRecord → ExamScore → ExamResult)
DELETE dr FROM DeductionRecord dr
INNER JOIN ExamScore es ON es.ExamScoreId = dr.ExamScoreId
INNER JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
WHERE ee.CandidateId = @drop;

DELETE es FROM ExamScore es
INNER JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
WHERE ee.CandidateId = @drop;

DELETE er FROM ExamResult er
INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
WHERE ee.CandidateId = @drop;

DELETE pf FROM Payment_Fee pf
INNER JOIN Payment p ON p.PaymentId = pf.PaymentId
INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
WHERE ee.CandidateId = @drop;

DELETE p FROM Payment p
INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
WHERE ee.CandidateId = @drop;

DELETE FROM ExamEnrollment WHERE CandidateId = @drop;
DELETE FROM Candidate WHERE CandidateId = @drop;

COMMIT TRANSACTION;

-- Sau patch: chỉ còn một dòng với ca kỳ 2
-- SELECT c.CandidateId, s.SessionName, ee.ExamEnrollmentId
-- FROM Candidate c
-- JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
-- JOIN [Session] s ON s.SessionId = ee.SessionId
-- WHERE c.GovernmentIdNumber = N'038400100002';
