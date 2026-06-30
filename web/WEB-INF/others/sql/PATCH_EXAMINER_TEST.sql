-- =============================================================================
-- PATCH: Sẵn sàng test cổng Sát hạch viên (Examiner)
-- Chạy trên SQL Server SAU KHI đã chạy DDL_DLEM_DB.sql + DML_DLEM_DB.sql
-- Database: DLEM_DB_2
-- Mật khẩu mặc định mọi tài khoản seed: login123
-- =============================================================================
--
-- ┌──────────────────────────────────────────────────────────────────────────┐
-- │ VIỆC EXAM STAFF PHẢI LÀM TRÊN GIAO DIỆN (nếu không chạy script SQL này) │
-- └──────────────────────────────────────────────────────────────────────────┘
--
-- Đăng nhập Exam Staff: exam_hoa hoặc exam_minh
-- URL:  {context}/staff/login
--
-- BƯỚC 1 – Phân bổ sát hạch viên (BẮT BUỘC)
--   Menu: Phân bổ sát hạch viên
--   URL:  {context}/views/staff/exam/examiner-allocation
--   Yêu cầu: Mỗi ca thi phải có ít nhất 1 SHV được gán vào phòng/khu vực thi
--            (bảng ExaminerSchedule: SessionId + ExamSectionId + ExamAreaId + ExaminerId)
--
-- BƯỚC 2 – Bắt đầu ca thi (BẮT BUỘC)
--   Trên trang phân bổ SHV hoặc Tổng quan → nút "Bắt đầu ca thi"
--   URL:  {context}/views/staff/exam/session-control  (action=startSession)
--   Yêu cầu: [Session].[Status] = N'InProgress'
--            (Examiner chỉ mở khóa menu khi có ca InProgress + đã được phân công)
--
-- BƯỚC 3 – Thủ tục / làm hồ sơ thí sinh (KHUYẾN NGHỊ trước khi gọi thi)
--   URL:  {context}/views/staff/exam/procedure
--   Yêu cầu: Thanh toán xong, có ảnh chân dung, thí sinh trong danh sách ca
--
-- BƯỚC 4 – Tải danh sách thí sinh (TÙY CHỌN nếu DB chưa có đủ người)
--   URL:  {context}/views/staff/exam/upload
--
-- ┌──────────────────────────────────────────────────────────────────────────┐
-- │ TÀI KHOẢN EXAMINER – ĐĂNG NHẬP TẠI /staff/login (KHÔNG dùng /login)    │
-- └──────────────────────────────────────────────────────────────────────────┘
--
-- Kịch bản A – Lý thuyết (DML_DLEM_DB.sql):
--   Username: examiner_tung
--   Ca thi:   "Ca sáng - Lý thuyết B"  |  Phòng: Phòng LT 1  |  Phần: Lý thuyết
--   Thí sinh mẫu SBD: 046, 123, 456, 048, 049
--
-- Kịch bản B – Sa hình / Đường trường (DML_DLEM_DB.sql):
--   examiner_tung  → "Ca sáng - Sa hình B"      (Sân thi số 1)   ← test Nhập điểm
--   examiner_lan   → "Ca chiều - Đường trường B" (Đường trường 1)
--
-- Script bên dưới ghi trực tiếp DB = tương đương Exam Staff đã làm Bước 1 + 2 (+ một phần 3).
-- =============================================================================

USE DLEM_DB_2;
GO

-- -----------------------------------------------------------------------------
-- 1. Bắt đầu ca thi (tương đương Exam Staff bấm "Bắt đầu ca")
-- -----------------------------------------------------------------------------
UPDATE [Session] SET [Status] = N'InProgress'
WHERE SessionName = N'Ca sáng - Lý thuyết B';
GO

-- -----------------------------------------------------------------------------
-- 2. Phân công SHV (tương đương Exam Staff → Phân bổ sát hạch viên)
--    Chỉ chèn nếu chưa có – không ghi đè phân công hiện tại
-- -----------------------------------------------------------------------------
IF NOT EXISTS (
    SELECT 1 FROM ExaminerSchedule es
    JOIN [User] u ON u.UserId = es.ExaminerId
    JOIN [Session] s ON s.SessionId = es.SessionId
    WHERE u.Username = N'examiner_tung' AND s.SessionName = N'Ca sáng - Lý thuyết B'
)
AND EXISTS (SELECT 1 FROM [User] WHERE Username = N'examiner_tung')
AND EXISTS (SELECT 1 FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B')
BEGIN
    INSERT INTO ExaminerSchedule (SessionId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt)
    SELECT
        (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1'),
        (SELECT UserId FROM [User] WHERE Username = N'examiner_tung'),
        (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'),
        GETDATE();
END
GO

-- -----------------------------------------------------------------------------
-- 3. Trạng thái thí sinh trong ca (SectionStatus trên ExamEnrollment)
--    Giúp test: Gọi thí sinh, In biên bản, Chờ ký
-- -----------------------------------------------------------------------------

-- Lý thuyết: thí sinh đã nộp bài → chờ ký
UPDATE ec
SET SectionStatus = N'AwaitingSignature'
FROM ExamEnrollment ec
JOIN TheoryPaper tp ON tp.ExamEnrollmentId = ec.ExamEnrollmentId
WHERE tp.SubmittedAt IS NOT NULL
  AND ec.SectionStatus IN (N'Pending', N'Testing');

-- Ca Lý thuyết B: đặt vài TS ở trạng thái chờ ký để test in biên bản / hoàn tất
UPDATE ec
SET SectionStatus = N'AwaitingSignature', SignaturePrinted = 0
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE s.SessionName = N'Ca sáng - Lý thuyết B'
  AND c.CandidateNumber IN (N'046', N'456');

GO

-- -----------------------------------------------------------------------------
-- 4. Kiểm tra nhanh sau khi chạy patch (kết quả mong đợi)
-- -----------------------------------------------------------------------------
SELECT
    u.Username AS ExaminerLogin,
    s.SessionName,
    s.[Status] AS SessionStatus,
    sec.SectionName AS PhanThi,
    ea.AreaName AS PhongThi
FROM ExaminerSchedule es
JOIN [User] u ON u.UserId = es.ExaminerId
JOIN [Session] s ON s.SessionId = es.SessionId
LEFT JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
LEFT JOIN ExamArea ea ON ea.ExamAreaId = es.ExamAreaId
WHERE s.[Status] = N'InProgress'
ORDER BY u.Username, s.SessionName;
GO

SELECT
    s.SessionName,
    c.CandidateNumber AS SBD,
    c.FullName,
    ec.SectionStatus,
    ec.SignaturePrinted
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE s.[Status] = N'InProgress'
ORDER BY s.SessionName, c.CandidateNumber;
GO

-- -----------------------------------------------------------------------------
-- 5. Chuẩn hóa SBD chỉ còn chữ số (bỏ tiền tố B-, SBD-, ...)
-- -----------------------------------------------------------------------------
UPDATE Candidate
SET CandidateNumber = CASE
    WHEN CandidateNumber LIKE N'%-%' THEN
        RIGHT(N'000' + CAST(
            TRY_CAST(SUBSTRING(CandidateNumber, CHARINDEX(N'-', CandidateNumber) + 1, 20) AS INT) AS NVARCHAR(20)
        ), 3)
    ELSE CandidateNumber
END
WHERE CandidateNumber LIKE N'%-%'
   OR CandidateNumber LIKE N'SBD%';
GO
