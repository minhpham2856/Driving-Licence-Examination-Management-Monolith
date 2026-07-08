-- Kiểm tra điều kiện lưu điểm sau import (chạy trước khi chấm trên allocation)
-- Thay @SBD và @SessionId theo ca bạn đang mở trên UI

DECLARE @SBD NVARCHAR(50) = N'052';
DECLARE @SessionId INT = 5;

SELECT N'1. Candidate' AS buoc,
       c.CandidateId, c.CandidateNumber, c.FullName, c.GovernmentIdNumber
FROM Candidate c
WHERE c.CandidateNumber = @SBD;

SELECT N'2. ExamEnrollment (phải có dòng SessionId = ca đang chấm)' AS buoc,
       c.CandidateNumber, ee.ExamEnrollmentId, ee.SessionId, s.SessionName, ee.SectionStatus
FROM Candidate c
JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
JOIN [Session] s ON s.SessionId = ee.SessionId
WHERE c.CandidateNumber = @SBD
ORDER BY ee.SessionId;

SELECT N'3. Session_ExamSection (ca phải gắn phần Lý thuyết)' AS buoc,
       s.SessionId, s.SessionName, es.ExamSectionId, es.SectionName
FROM [Session] s
JOIN Session_ExamSection ses ON ses.SessionId = s.SessionId
JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
WHERE s.SessionId = @SessionId;

SELECT N'4. Payment (thủ tục — gắn đúng ExamEnrollmentId của ca)' AS buoc,
       c.CandidateNumber, ee.SessionId, ee.ExamEnrollmentId,
       p.PaymentId, p.PaymentStatus, p.TotalAmount
FROM Candidate c
JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
LEFT JOIN Payment p ON p.ExamEnrollmentId = ee.ExamEnrollmentId
WHERE c.CandidateNumber = @SBD;

-- Lỗi thường gặp:
-- • Bước 2: không có dòng SessionId = @SessionId → import sai kỳ/ca hoặc SH chỉ H (kỳ không có ca SH)
-- • Bước 3: rỗng → thiếu Session_ExamSection (chạy lại DML mục 11, không xóa bảng này)
-- • Bước 4: Payment trên ca khác → làm thủ tục xong nhưng allocation ca @SessionId vẫn báo chưa thu phí
