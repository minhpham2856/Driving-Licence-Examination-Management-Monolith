-- ============================================
-- DML schema DLEM_DB_2 — thí sinh vừa import (chưa qua thủ tục)
-- Khác DML_DLEM_DB_2_examstaff_sample.sql:
--   - Có Candidate + ExamEnrollment + ExamEnrollmentSection
--   - KHÔNG có PhotoImageUrl, Payment, TheoryPaper, phân bổ phòng/máy
--   - Mọi phần thi: Status = N'Pending'
-- Đăng nhập examstaff: exam_hoa / login123
-- Chọn kỳ B1-20260601 — sessionId trên URL = ExamId (= 1 sau seed)
-- ============================================

USE DLEM_DB_2;
GO

DELETE FROM Audit;
DELETE FROM DeductionRecord;
DELETE FROM ExamScore;
DELETE FROM ExamResult;
DELETE FROM CandidateAnswer;
DELETE FROM TheoryPaper;
DELETE FROM Payment_Fee;
DELETE FROM Payment;
DELETE FROM Licence_Fee;
DELETE FROM ExamEnrollmentSection;
DELETE FROM ExamEnrollment;
DELETE FROM Candidate;
DELETE FROM ExamRegistration;
DELETE FROM Document;
DELETE FROM Profile;
DELETE FROM ExaminerSchedule;
DELETE FROM Exam_ExamArea;
DELETE FROM Licence_Question;
DELETE FROM Question;
DELETE FROM QuestionCategory;
DELETE FROM ScoreDeduction;
DELETE FROM ExamDevice;
DELETE FROM ExamSection;
DELETE FROM Exam;
DELETE FROM ExamArea;
DELETE FROM ExamZone;
DELETE FROM Fee;
DELETE FROM Licence;
DELETE FROM [User];
DELETE FROM [Role];
GO

-- ===================== Hạ tầng hệ thống =====================
INSERT INTO [Role] (RoleName) VALUES
(N'Quản trị viên'),
(N'Sát hạch viên'),
(N'Cán bộ quản lý'),
(N'Cán bộ kỳ thi'),
(N'Thí sinh'),
(N'Người đăng ký thi');
GO

INSERT INTO [User] (Username, Email, PasswordHash, RoleId, IsActive)
SELECT v.Username, v.Email, v.PasswordHash, r.RoleId, 1
FROM (VALUES
    (N'admin',     N'admin@trungtamsathach.vn',   N'login123', N'Quản trị viên'),
    (N'shv_tung',  N'tung.nguyen@sathach.vn',     N'login123', N'Sát hạch viên'),
    (N'shv_lan',   N'lan.pham@sathach.vn',        N'login123', N'Sát hạch viên'),
    (N'shv_duc',   N'duc.tran@sathach.vn',        N'login123', N'Sát hạch viên'),
    (N'shv_khanh', N'khanh.le@sathach.vn',        N'login123', N'Sát hạch viên'),
    (N'shv_phong', N'phong.vo@sathach.vn',        N'login123', N'Sát hạch viên'),
    (N'exam_hoa',  N'hoa.le@trungtamsathach.vn',  N'login123', N'Cán bộ kỳ thi'),
    (N'exam_minh', N'minh.vu@trungtamsathach.vn', N'login123', N'Cán bộ kỳ thi')
) v(Username, Email, PasswordHash, RoleName)
JOIN [Role] r ON r.RoleName = v.RoleName;
GO

INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears) VALUES
(N'B1', N'Ô tô số tự động tải trọng dưới 3.500 kg', 18, 0),
(N'A1', N'Xe mô tô hai bánh đến 125 cm³', 18, 0);
GO

INSERT INTO ExamZone (ZoneName, [Location], IsActive) VALUES
(N'Khu Hà Nội chính', N'Trung tâm Sát hạch Lái Vui – Hà Nội', 1);
GO

DECLARE @ZoneId INT = (SELECT TOP 1 ExamZoneId FROM ExamZone);

INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location], ExamZoneId) VALUES
(N'Phòng thủ tục 102',     N'Hỗn hợp',   30, N'Tầng 1, Tòa A', @ZoneId),
(N'Phòng thi lý thuyết 1', N'Lý thuyết', 30, N'Tầng 2, Tòa B', @ZoneId),
(N'Phòng thi lý thuyết 2', N'Lý thuyết', 30, N'Tầng 2, Tòa B', @ZoneId),
(N'Phòng thi lý thuyết 3', N'Lý thuyết', 30, N'Tầng 3, Tòa B', @ZoneId),
(N'Sân thi ô tô số 1',     N'Thực hành', 12, N'Khu sân số 2', @ZoneId),
(N'Sân thi ô tô số 2',     N'Thực hành', 12, N'Khu sân số 3', @ZoneId);
GO

DECLARE @LicB1 INT = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1');
DECLARE @LicA1 INT = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1');
DECLARE @ExamB1 INT;
DECLARE @ExamB1K2 INT;
DECLARE @ExamA1 INT;
DECLARE @SecB1Theory INT;
DECLARE @SecB1Practical INT;
DECLARE @AreaLT1 INT;
DECLARE @AreaLT2 INT;
DECLARE @AreaLT3 INT;
DECLARE @AreaPrac1 INT;
DECLARE @AreaPrac2 INT;

INSERT INTO Exam (ExamCode, ExamDate, StartTime, EndTime, CentreName, [Status], LicenceId) VALUES
(N'B1-20260601', '2026-06-01', '2026-06-01 07:30:00', NULL,
 N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Đang diễn ra', @LicB1),
(N'B1-20260615', '2026-06-15', '2026-06-15 07:30:00', NULL,
 N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Chưa diễn ra', @LicB1),
(N'A1-20260601', '2026-06-01', '2026-06-01 07:30:00', NULL,
 N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Đang diễn ra', @LicA1);

SET @ExamB1 = (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260601');
SET @ExamB1K2 = (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260615');
SET @ExamA1 = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601');

INSERT INTO ExamSection (SectionType, LicenceId, DurationMinutes, ExamId) VALUES
(N'Lý thuyết', @LicB1, 20, @ExamB1),
(N'Thực hành trong hình', @LicB1, 18, @ExamB1),
(N'Lý thuyết', @LicB1, 20, @ExamB1K2),
(N'Thực hành trong hình', @LicB1, 18, @ExamB1K2),
(N'Lý thuyết', @LicA1, 19, @ExamA1),
(N'Thực hành trong hình', @LicA1, NULL, @ExamA1);

INSERT INTO Exam_ExamArea (ExamId, ExamAreaId)
SELECT ex.ExamId, ea.ExamAreaId
FROM Exam ex
JOIN ExamArea ea ON ea.AreaName IN (
    N'Phòng thủ tục 102',
    N'Phòng thi lý thuyết 1', N'Phòng thi lý thuyết 2', N'Phòng thi lý thuyết 3',
    N'Sân thi ô tô số 1', N'Sân thi ô tô số 2')
WHERE ex.ExamCode IN (N'B1-20260601', N'B1-20260615', N'A1-20260601');

SET @SecB1Theory = (SELECT ExamSectionId FROM ExamSection WHERE ExamId = @ExamB1 AND SectionType = N'Lý thuyết');
SET @SecB1Practical = (SELECT ExamSectionId FROM ExamSection WHERE ExamId = @ExamB1 AND SectionType = N'Thực hành trong hình');
SET @AreaLT1 = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1');
SET @AreaLT2 = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2');
SET @AreaLT3 = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 3');
SET @AreaPrac1 = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1');
SET @AreaPrac2 = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 2');

INSERT INTO ExaminerSchedule (ExamId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt) VALUES
(@ExamB1, @SecB1Theory, @AreaLT1,
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), GETDATE()),
(@ExamB1, @SecB1Theory, @AreaLT2,
 (SELECT UserId FROM [User] WHERE Username = N'shv_lan'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), GETDATE()),
(@ExamB1, @SecB1Theory, @AreaLT3,
 (SELECT UserId FROM [User] WHERE Username = N'shv_duc'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), GETDATE()),
(@ExamB1, @SecB1Practical, @AreaPrac1,
 (SELECT UserId FROM [User] WHERE Username = N'shv_khanh'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), GETDATE()),
(@ExamB1, @SecB1Practical, @AreaPrac2,
 (SELECT UserId FROM [User] WHERE Username = N'shv_phong'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), GETDATE());
GO

INSERT INTO ExamDevice (DeviceName, DeviceType, IsActive, ExamAreaId) VALUES
(N'MT-LT-01', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-02', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-03', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-11', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2')),
(N'MT-LT-12', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2')),
(N'MT-LT-21', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 3')),
(N'MT-LT-22', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 3'));
GO

-- Danh mục phí (dùng khi cán bộ thu lệ phí tại quầy — chưa có Payment cho thí sinh)
INSERT INTO Fee (FeeName, FeeType, IsActive) VALUES
(N'Lệ phí thi lý thuyết', N'Lệ phí thi', 1),
(N'Lệ phí thi thực hành trong hình', N'Lệ phí thi', 1),
(N'Phí xét hồ sơ và in ấn biểu mẫu', N'Phí hành chính', 1);
GO

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
SELECT l.LicenceId, f.FeeId, v.Amount
FROM Licence l
JOIN (VALUES
    (N'B1', N'Lệ phí thi lý thuyết', 100000.00),
    (N'B1', N'Lệ phí thi thực hành trong hình', 250000.00),
    (N'A1', N'Lệ phí thi lý thuyết', 65000.00),
    (N'A1', N'Lệ phí thi thực hành trong hình', 350000.00)
) v(LicenceClass, FeeName, Amount) ON v.LicenceClass = l.LicenceClass
JOIN Fee f ON f.FeeName = v.FeeName;
GO

-- ===================== Thí sinh vừa import =====================
-- Chỉ dữ liệu hồ sơ từ DSTS: không ảnh, chưa thanh toán, chưa phân bổ
INSERT INTO Candidate (
    CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex,
    GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeNo,
    ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended
) VALUES
-- Kỳ B1-20260601 (11 thí sinh — test gọi loa / hàng đợi thủ tục)
(N'050', N'Lê Thanh Bình',   '1998-07-12', N'0905005001', NULL, 1, N'001198071201', N'10 Phạm Văn Đồng, Hà Nội',       1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'051', N'Phạm Thu Hà',     '1999-03-25', N'0905005101', NULL, 0, N'001199032501', N'22 Nguyễn Chí Thanh, Hà Nội',    1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'052', N'Hoàng Minh Tuấn', '2000-11-08', N'0905005201', NULL, 1, N'001200110801', N'5 Láng Hạ, Hà Nội',              1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'053', N'Võ Thị Lan',      '1997-01-19', N'0905005301', NULL, 0, N'001197011901', N'88 Kim Mã, Hà Nội',              1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'054', N'Đặng Văn Phúc',   '1996-06-30', N'0905005401', NULL, 1, N'001196063001', N'15 Xã Đàn, Hà Nội',              1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'055', N'Bùi Thị Ngọc',    '2001-12-02', N'0905005501', NULL, 0, N'001201120201', N'40 Giảng Võ, Hà Nội',            1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'056', N'Nguyễn Quốc Huy', '1995-04-17', N'0905005601', NULL, 1, N'001195041701', N'72 Trần Duy Hưng, Hà Nội',       1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'057', N'Trần Thị Mai',    '1998-09-21', N'0905005701', NULL, 0, N'001198092101', N'9 Hoàng Quốc Việt, Hà Nội',      1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'058', N'Lý Văn Đạt',      '1999-08-14', N'0905005801', NULL, 1, N'001199081401', N'31 Đội Cấn, Hà Nội',             1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'059', N'Phan Thị Oanh',   '2000-02-27', N'0905005901', NULL, 0, N'001200022701', N'60 Thái Hà, Hà Nội',             1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'060', N'Mai Văn Sơn',     '1997-10-05', N'0905006001', NULL, 1, N'001197100501', N'18 Chùa Bộc, Hà Nội',            1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
-- Kỳ A1-20260601 (3 thí sinh — chỉ LT + sa hình)
(N'010', N'Phạm Thị Dung', '2002-01-28', N'0934567890', NULL, 0, N'001202012801', N'56 Hai Bà Trưng, Hà Nội', 1, 1, 1, N'Thi cấp mới hạng A1', NULL, 0, 0),
(N'011', N'Đỗ Văn Hải',    '2001-04-20', N'0945678901', NULL, 1, N'001201042001', N'90 Lê Lợi, TP.HCM',       1, 1, 1, N'Thi cấp mới hạng A1', NULL, 0, 0),
(N'012', N'Ngô Thị Kim',   '1999-09-09', N'0923456780', NULL, 0, N'001199090901', N'23 Bạch Đằng, Đà Nẵng',    1, 1, 1, N'Thi cấp mới hạng A1', NULL, 0, 0);
GO

-- Ghi danh kỳ thi (một enrollment / thí sinh / kỳ — như sau import)
DECLARE @ExamB1Id INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260601');
DECLARE @ExamA1Id INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601');

INSERT INTO ExamEnrollment (CandidateId, ExamId, AllocatedExamAreaId, ExamDeviceId)
SELECT c.CandidateId, @ExamB1Id, NULL, NULL
FROM Candidate c
WHERE c.CandidateNumber BETWEEN N'050' AND N'060';

INSERT INTO ExamEnrollment (CandidateId, ExamId, AllocatedExamAreaId, ExamDeviceId)
SELECT c.CandidateId, @ExamA1Id, NULL, NULL
FROM Candidate c
WHERE c.CandidateNumber IN (N'010', N'011', N'012');
GO

-- ExamEnrollmentSection: tạo đủ phần thi theo TakeTheory / TakeLayout, tất cả Pending
DECLARE @B1Exam INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260601');
DECLARE @A1Exam INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601');

INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, Status)
SELECT ee.ExamEnrollmentId, es.ExamSectionId, N'Pending'
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
JOIN ExamSection es ON es.ExamId = ee.ExamId
WHERE ee.ExamId = @B1Exam
  AND c.CandidateNumber BETWEEN N'050' AND N'060'
  AND (
        (es.SectionType = N'Lý thuyết' AND ISNULL(c.TakeTheory, 1) = 1)
     OR (es.SectionType = N'Thực hành trong hình' AND ISNULL(c.TakeLayout, 1) = 1)
  );

INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, Status)
SELECT ee.ExamEnrollmentId, es.ExamSectionId, N'Pending'
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
JOIN ExamSection es ON es.ExamId = ee.ExamId
WHERE ee.ExamId = @A1Exam
  AND c.CandidateNumber IN (N'010', N'011', N'012')
  AND (
        (es.SectionType = N'Lý thuyết' AND ISNULL(c.TakeTheory, 1) = 1)
     OR (es.SectionType = N'Thực hành trong hình' AND ISNULL(c.TakeLayout, 1) = 1)
  );
GO

-- Không seed: Payment, Payment_Fee, TheoryPaper, ExamResult, ExamScore, CandidateAnswer

-- Kiểm tra nhanh sau khi chạy:
-- SELECT c.CandidateNumber, c.PhotoImageUrl, pay.PaymentId
-- FROM Candidate c
-- JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
-- LEFT JOIN Payment pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
-- WHERE c.CandidateNumber = N'050';
--
-- SELECT c.CandidateNumber, es.SectionType, ees.Status, ees.ExamAreaId, ees.ExamDeviceId
-- FROM Candidate c
-- JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
-- JOIN Exam ex ON ex.ExamId = ee.ExamId
-- JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
-- JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
-- WHERE ex.ExamCode = N'B1-20260601'
-- ORDER BY c.CandidateNumber, es.SectionType;
