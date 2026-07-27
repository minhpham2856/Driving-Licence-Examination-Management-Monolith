-- ============================================
-- Seed kỳ A1-123: 20 thí sinh đủ điều kiện (LT + TH)
-- Chạy SAU: DDL → DML → seed_real.sql
--
-- Ngày/giờ thi: hôm nay lúc 15:00 (GETDATE())
-- shv_tung → Lý thuyết (Phòng thi LT 2)
-- shv_lan  → Thực hành (Sân TH 1)
--
-- Ảnh chân dung (bắt buộc để isProcedureComplete = true):
--   Copy web/uploads/demo-dossier/portrait.jpg vào
--   %CATALINA_BASE%/dlem-data/candidate-photos/seed-portrait.jpg
--   (hoặc thư mục -Ddlem.photos.dir nếu có cấu hình JVM)
--
-- Tài khoản test (mật khẩu login123):
--   exam_hoa / exam_minh (cán bộ thi) | shv_tung / shv_lan (SHV)
-- Thí sinh: SBD 001–020 | Kiosk: mã kỳ A1-123 + OTP tạo trên dashboard
-- ============================================

USE DLEM_DB_2;
GO

SET NOCOUNT ON;
GO

DECLARE @Today DATE = CAST(GETDATE() AS DATE);
DECLARE @StartTime DATETIME = DATEADD(HOUR, 15, CAST(@Today AS DATETIME));
DECLARE @A1LicenceId INT = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1');
DECLARE @ExamId INT;
DECLARE @TheoryAreaId INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2');
DECLARE @LayoutAreaId INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân TH 1');
DECLARE @AssignedBy INT = (SELECT UserId FROM [User] WHERE Username = N'exam_minh');

IF @A1LicenceId IS NULL OR @TheoryAreaId IS NULL OR @LayoutAreaId IS NULL
BEGIN
    RAISERROR(N'Thiếu master data (Licence A1 / ExamArea). Chạy DML_DLEM_DB.sql trước.', 16, 1);
    RETURN;
END;

-- 1. Kỳ thi A1-123 (hôm nay 15:00, đang diễn ra)
IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamCode = N'A1-123')
BEGIN
    INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], ExamPassword, LicenceId, StartTime, EndTime)
    VALUES (N'A1-123', @Today, N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Đang diễn ra', NULL, @A1LicenceId, @StartTime, NULL);
    SET @ExamId = SCOPE_IDENTITY();

    INSERT INTO ExamSection (SectionType, LicenceId, DurationMinutes, ExamId) VALUES
    (N'Lý thuyết', @A1LicenceId, 19, @ExamId),
    (N'Thực hành trong hình', @A1LicenceId, NULL, @ExamId);
END
ELSE
BEGIN
    SELECT @ExamId = ExamId FROM Exam WHERE ExamCode = N'A1-123';
    UPDATE Exam
    SET ExamDate = @Today,
        StartTime = @StartTime,
        EndTime = NULL,
        [Status] = N'Đang diễn ra',
        ExamPassword = NULL
    WHERE ExamId = @ExamId;
END;

-- 2. Khu vực thi
INSERT INTO Exam_ExamArea (ExamId, ExamAreaId)
SELECT @ExamId, ea.ExamAreaId
FROM ExamArea ea
WHERE ea.AreaName IN (N'Phòng thi LT 2', N'Sân TH 1')
  AND NOT EXISTS (
      SELECT 1 FROM Exam_ExamArea x
      WHERE x.ExamId = @ExamId AND x.ExamAreaId = ea.ExamAreaId
  );

-- 3. Phân công SHV
DELETE FROM ExaminerSchedule WHERE ExamId = @ExamId;

INSERT INTO ExaminerSchedule (ExamId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt)
SELECT @ExamId, es.ExamSectionId, @TheoryAreaId, u.UserId, @AssignedBy, DATEADD(HOUR, -1, @StartTime)
FROM ExamSection es
JOIN [User] u ON u.Username = N'shv_tung'
WHERE es.ExamId = @ExamId AND es.SectionType = N'Lý thuyết';

INSERT INTO ExaminerSchedule (ExamId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt)
SELECT @ExamId, es.ExamSectionId, @LayoutAreaId, u.UserId, @AssignedBy, DATEADD(MINUTE, -55, @StartTime)
FROM ExamSection es
JOIN [User] u ON u.Username = N'shv_lan'
WHERE es.ExamId = @ExamId AND es.SectionType = N'Thực hành trong hình';

-- 4. Reset dữ liệu thi cũ của kỳ A1-123
DELETE ca
FROM CandidateAnswer ca
JOIN TheoryPaper tp ON tp.TheoryPaperId = ca.TheoryPaperId
JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
WHERE ee.ExamId = @ExamId;

DELETE tp
FROM TheoryPaper tp
JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
WHERE ee.ExamId = @ExamId;

DELETE dr
FROM DeductionRecord dr
JOIN ExamScore esc ON esc.ExamScoreId = dr.ExamScoreId
JOIN ExamResult er ON er.ExamResultId = esc.ExamResultId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
WHERE ee.ExamId = @ExamId;

DELETE esc
FROM ExamScore esc
JOIN ExamResult er ON er.ExamResultId = esc.ExamResultId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
WHERE ee.ExamId = @ExamId;

DELETE er
FROM ExamResult er
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
WHERE ee.ExamId = @ExamId;

DELETE p
FROM Payment p
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
WHERE ee.ExamId = @ExamId;

DELETE cv
FROM CandidateViolation cv
JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = cv.ExamEnrollmentSectionId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
WHERE ee.ExamId = @ExamId;

DELETE ees
FROM ExamEnrollmentSection ees
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
WHERE ee.ExamId = @ExamId;

DELETE ee
FROM ExamEnrollment ee
WHERE ee.ExamId = @ExamId;

DELETE c
FROM Candidate c
WHERE NOT EXISTS (SELECT 1 FROM ExamEnrollment x WHERE x.CandidateId = c.CandidateId);

-- 5. 20 thí sinh (SBD 001–020, LT+H, đã có ảnh + thanh toán)
INSERT INTO Candidate (
    CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex,
    GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeNo,
    ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended
) VALUES
(N'001', N'Đặng Quốc Trí', '1979-08-22', N'0347757721', NULL, 1, N'062079261742', N'24 Giải Phóng, Đống Đa, Hà Nội', 1, 1, 3, N'SH lại L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'002', N'Trịnh Diễm Hạnh', '1994-09-11', N'0767878019', NULL, 0, N'036194316664', N'247 Nguyễn Huệ, Đống Đa, Hà Nội', 1, 1, 2, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'003', N'Huỳnh Tuấn Nam', '2006-09-17', N'0866572200', NULL, 1, N'035206202298', N'154 Trần Phú, Thanh Xuân, Hà Nội', 1, 1, 2, N'SH lại L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'004', N'Trịnh Hồng Diễm', '1987-06-23', N'0702816149', NULL, 0, N'012187926559', N'36 Bạch Đằng, Đống Đa, Hà Nội', 1, 1, 2, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'005', N'Vũ Văn Thắng', '1983-02-10', N'0946414181', NULL, 1, N'010083843701', N'222 Lý Thường Kiệt, Quận 1, TP.HCM', 1, 1, 3, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'006', N'Trần Lan Quyên', '1997-01-10', N'0334460596', NULL, 0, N'031197048216', N'206 Nguyễn Xiển, Cầu Giấy, Hà Nội', 1, 1, 1, N'SH lại L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'007', N'Võ Văn Cường', '1979-08-21', N'0331918726', NULL, 1, N'033079165363', N'107 Nguyễn Huệ, Hoàn Kiếm, Hà Nội', 1, 1, 2, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'008', N'Tô Thanh Giang', '2006-04-19', N'0856294171', NULL, 0, N'068306875616', N'167 Lê Duẩn, Hoàn Kiếm, Hà Nội', 1, 1, 1, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'009', N'Phạm Thành Cường', '2001-10-07', N'0819749708', NULL, 1, N'067201762408', N'42 Nguyễn Huệ, Huế, Thừa Thiên Huế', 1, 1, 3, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'010', N'Bùi Bích Hạnh', '1999-02-02', N'0399369118', NULL, 0, N'074199278534', N'217 Nguyễn Xiển, Huế, Thừa Thiên Huế', 1, 1, 2, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'011', N'Huỳnh Tuấn Kiên', '1976-03-18', N'0888051582', NULL, 1, N'001076442788', N'239 Phạm Hùng, Cầu Giấy, Hà Nội', 1, 1, 3, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'012', N'Mai Bích Hiền', '1981-04-28', N'0924828077', NULL, 0, N'034181568045', N'70 Cầu Giấy, Hải Châu, Đà Nẵng', 1, 1, 3, N'SH lại L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'013', N'Đinh Tuấn Tùng', '1996-07-21', N'0775466458', NULL, 1, N'027096479059', N'103 Trần Phú, Thanh Khê, Đà Nẵng', 1, 1, 2, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'014', N'Đặng Quỳnh Liên', '1980-05-18', N'0985226522', NULL, 0, N'077180600433', N'84 Giải Phóng, Thanh Khê, Đà Nẵng', 1, 1, 2, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'015', N'Trịnh Thanh Kiên', '1988-11-08', N'0854325163', NULL, 1, N'084088206620', N'112 Lê Văn Lương, Quận 3, TP.HCM', 1, 1, 3, N'SH lại L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'016', N'Tô Quỳnh Vy', '1983-06-11', N'0869937836', NULL, 0, N'014183877840', N'40 Hoàng Hoa Thám, Ninh Kiều, Cần Thơ', 1, 1, 1, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'017', N'Đặng Tuấn Dũng', '1999-06-21', N'0940121920', NULL, 1, N'031099085834', N'132 Nguyễn Huệ, Đống Đa, Hà Nội', 1, 1, 2, N'SH lần đầu L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'018', N'Võ Quỳnh Mai', '1997-05-11', N'0389679866', NULL, 0, N'006197268745', N'242 Nguyễn Trãi, Hải Châu, Đà Nẵng', 1, 1, 3, N'SH lại L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'019', N'Hoàng Bảo Hoàng', '2004-09-27', N'0342137531', NULL, 1, N'045204135909', N'207 Giải Phóng, Thanh Khê, Đà Nẵng', 1, 1, 1, N'SH lại L+H', N'candidate-photos/seed-portrait.jpg', 0, 0),
(N'020', N'Ngô Thanh Ngân', '1977-02-01', N'0900472327', NULL, 0, N'084177798330', N'50 Phạm Hùng, Ninh Kiều, Cần Thơ', 1, 1, 3, N'SH lại L+H', N'candidate-photos/seed-portrait.jpg', 0, 0);

-- 6. Ghi danh
INSERT INTO ExamEnrollment (CandidateId, ExamId, AllocatedExamAreaId, ExamDeviceId)
SELECT c.CandidateId, @ExamId, NULL, NULL
FROM Candidate c
WHERE TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 20;

-- 7. Phần thi Lý thuyết
INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, ExamAreaId, ExamDeviceId, Status)
SELECT ec.ExamEnrollmentId, es.ExamSectionId, @TheoryAreaId, NULL, N'Chưa thi'
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN ExamSection es ON es.ExamId = ec.ExamId AND es.SectionType = N'Lý thuyết'
WHERE ec.ExamId = @ExamId
  AND c.TakeTheory = 1
  AND TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 20;

-- 8. Phần thi Thực hành trong hình
INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, ExamAreaId, ExamDeviceId, Status)
SELECT ec.ExamEnrollmentId, es.ExamSectionId, @LayoutAreaId, NULL, N'Chưa thi'
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN ExamSection es ON es.ExamId = ec.ExamId AND es.SectionType = N'Thực hành trong hình'
WHERE ec.ExamId = @ExamId
  AND c.TakeLayout = 1
  AND TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 20;

-- 9. Thanh toán hoàn tất
INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
SELECT N'Hoàn tất',
       N'Chuyển khoản',
       N'A1-123-PAY-' + RIGHT(N'000000' + CAST(ec.ExamEnrollmentId AS NVARCHAR(10)), 6),
       550000.00,
       DATEADD(DAY, -3, @StartTime),
       ec.ExamEnrollmentId
FROM ExamEnrollment ec
WHERE ec.ExamId = @ExamId;

-- 10. TH luôn Chưa thi khi LT chưa xong
UPDATE eesLayout
SET eesLayout.Status = N'Chưa thi',
    eesLayout.CheckedInAt = NULL,
    eesLayout.CheckedInBy = NULL,
    eesLayout.StartedAt = NULL,
    eesLayout.CompletedAt = NULL,
    eesLayout.ResultPrintedAt = NULL
FROM ExamEnrollmentSection eesLayout
JOIN ExamSection secLayout ON secLayout.ExamSectionId = eesLayout.ExamSectionId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = eesLayout.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
JOIN ExamSection secTheory ON secTheory.ExamId = ee.ExamId AND secTheory.SectionType = N'Lý thuyết'
JOIN ExamEnrollmentSection eesTheory
    ON eesTheory.ExamEnrollmentId = ee.ExamEnrollmentId
   AND eesTheory.ExamSectionId = secTheory.ExamSectionId
WHERE ee.ExamId = @ExamId
  AND secLayout.SectionType = N'Thực hành trong hình'
  AND c.TakeTheory = 1
  AND ISNULL(eesTheory.Status, N'Chưa thi') <> N'Đã thi';

PRINT N'seed_real.sql hoàn tất: kỳ A1-123, ' + CONVERT(NVARCHAR(10), @Today, 120)
    + N' 15:00, 20 thí sinh SBD 001–020.';
GO
