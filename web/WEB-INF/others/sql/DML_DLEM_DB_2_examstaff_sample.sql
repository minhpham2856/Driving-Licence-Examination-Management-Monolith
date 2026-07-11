-- ============================================
-- DML mẫu schema DLEM_DB_2 (không còn [Session])
-- Mục tiêu: test examstaff + public-call (gọi loa TV)
-- Đăng nhập examstaff: exam_hoa / login123
-- Chọn kỳ B1-20260601 — sessionId trên URL = ExamId
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

INSERT INTO [Role] (RoleName) VALUES
(N'Quản trị viên'),
(N'Sát hạch viên'),
(N'Cán bộ quản lý'),
(N'Cán bộ kỳ thi'),
(N'Thí sinh'),
(N'Người đăng ký thi');
GO

INSERT INTO [User] (Username, Email, PasswordHash, RoleId, IsActive) VALUES
(N'admin',     N'admin@trungtamsathach.vn',       N'login123', 1, 1),
(N'shv_tung',  N'tung.nguyen@sathach.vn',         N'login123', 2, 1),
(N'shv_lan',   N'lan.pham@sathach.vn',            N'login123', 2, 1),
(N'shv_duc',   N'duc.tran@sathach.vn',            N'login123', 2, 1),
(N'shv_khanh', N'khanh.le@sathach.vn',            N'login123', 2, 1),
(N'shv_phong', N'phong.vo@sathach.vn',            N'login123', 2, 1),
(N'exam_hoa',  N'hoa.le@trungtamsathach.vn',      N'login123', 4, 1),
(N'exam_minh', N'minh.vu@trungtamsathach.vn',     N'login123', 4, 1);
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

INSERT INTO Exam (ExamCode, ExamDate, StartTime, EndTime, CentreName, [Status], LicenceId) VALUES
(N'B1-20260601', '2026-06-01', '2026-06-01 07:30:00', NULL,
 N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Đang diễn ra', @LicB1),
(N'B1-20260615', '2026-06-15', '2026-06-15 07:30:00', NULL,
 N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Chưa diễn ra', @LicB1);
GO

DECLARE @ExamB1 INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260601');
DECLARE @ExamB1K2 INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260615');
DECLARE @LicB1Id INT = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1');
DECLARE @SecTheory INT, @SecPractical INT;
DECLARE @AreaLT1 INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1');
DECLARE @AreaLT2 INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2');
DECLARE @AreaLT3 INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 3');
DECLARE @AreaProc INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thủ tục 102');
DECLARE @AreaPrac1 INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1');
DECLARE @AreaPrac2 INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 2');

INSERT INTO ExamSection (SectionType, LicenceId, DurationMinutes, ExamId) VALUES
(N'Lý thuyết', @LicB1Id, 20, @ExamB1),
(N'Thực hành trong hình', @LicB1Id, 18, @ExamB1),
(N'Lý thuyết', @LicB1Id, 20, @ExamB1K2),
(N'Thực hành trong hình', @LicB1Id, 18, @ExamB1K2);

SET @SecTheory = (SELECT ExamSectionId FROM ExamSection WHERE ExamId = @ExamB1 AND SectionType = N'Lý thuyết');
SET @SecPractical = (SELECT ExamSectionId FROM ExamSection WHERE ExamId = @ExamB1 AND SectionType = N'Thực hành trong hình');

INSERT INTO Exam_ExamArea (ExamId, ExamAreaId) VALUES
(@ExamB1, @AreaProc),
(@ExamB1, @AreaLT1),
(@ExamB1, @AreaLT2),
(@ExamB1, @AreaLT3),
(@ExamB1, @AreaPrac1),
(@ExamB1, @AreaPrac2),
(@ExamB1K2, @AreaProc),
(@ExamB1K2, @AreaLT1),
(@ExamB1K2, @AreaLT2),
(@ExamB1K2, @AreaLT3),
(@ExamB1K2, @AreaPrac1),
(@ExamB1K2, @AreaPrac2);

-- Phân giám khảo kỳ B1-20260601 (mỗi phòng/sân một SHV)
INSERT INTO ExaminerSchedule (ExamId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt) VALUES
(@ExamB1, @SecTheory, @AreaLT1,
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), GETDATE()),
(@ExamB1, @SecTheory, @AreaLT2,
 (SELECT UserId FROM [User] WHERE Username = N'shv_lan'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), GETDATE()),
(@ExamB1, @SecTheory, @AreaLT3,
 (SELECT UserId FROM [User] WHERE Username = N'shv_duc'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), GETDATE()),
(@ExamB1, @SecPractical, @AreaPrac1,
 (SELECT UserId FROM [User] WHERE Username = N'shv_khanh'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), GETDATE()),
(@ExamB1, @SecPractical, @AreaPrac2,
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

INSERT INTO Fee (FeeName, FeeType, IsActive) VALUES
(N'Lệ phí thi lý thuyết', N'Lệ phí thi', 1);
GO

-- Thí sinh kỳ B1-20260601 (SBD 050–055) — đủ trạng thái thủ tục / gọi loa
INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address,
    TakeTheory, TakeLayout, TakeNo, ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended) VALUES
(N'050', N'Lê Thanh Bình',   '1998-07-12', N'0905005001', 1, N'001198071201', N'10 Phạm Văn Đồng, Hà Nội', 1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'051', N'Phạm Thu Hà',     '1999-03-25', N'0905005101', 0, N'001199032501', N'22 Nguyễn Chí Thanh, Hà Nội', 1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'052', N'Hoàng Minh Tuấn', '2000-11-08', N'0905005201', 1, N'001200110801', N'5 Láng Hạ, Hà Nội', 1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'053', N'Võ Thị Lan',      '1997-01-19', N'0905005301', 0, N'001197011901', N'88 Kim Mã, Hà Nội', 1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'054', N'Đặng Văn Phúc',   '1996-06-30', N'0905005401', 1, N'001196063001', N'15 Xã Đàn, Hà Nội', 1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0),
(N'055', N'Bùi Thị Ngọc',    '2001-12-02', N'0905005501', 0, N'001201120201', N'40 Giảng Võ, Hà Nội', 1, 1, 1, N'Thi cấp mới hạng B', NULL, 0, 0);
GO

DECLARE @ExamId INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260601');
DECLARE @TheorySec INT = (SELECT ExamSectionId FROM ExamSection WHERE ExamId = @ExamId AND SectionType = N'Lý thuyết');
DECLARE @PracSec INT = (SELECT ExamSectionId FROM ExamSection WHERE ExamId = @ExamId AND SectionType = N'Thực hành trong hình');
DECLARE @Dev1 INT = (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-01');
DECLARE @Dev2 INT = (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-02');
DECLARE @AreaLT1 INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1');

INSERT INTO ExamEnrollment (CandidateId, ExamId, AllocatedExamAreaId, ExamDeviceId)
SELECT c.CandidateId, @ExamId, NULL, NULL
FROM Candidate c
WHERE c.CandidateNumber IN (N'050', N'051', N'052', N'053', N'054', N'055');

INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, Status, ExamAreaId, ExamDeviceId, CompletedAt)
SELECT ee.ExamEnrollmentId, @TheorySec,
       CASE c.CandidateNumber
         WHEN N'050' THEN N'Done'
         WHEN N'051' THEN N'AwaitingSignature'
         WHEN N'052' THEN N'Testing'
         WHEN N'053' THEN N'Pending'
         WHEN N'054' THEN N'AwaitingSignature'
         ELSE N'Pending'
       END,
       CASE WHEN c.CandidateNumber IN (N'051', N'052', N'054') THEN @AreaLT1 ELSE NULL END,
       CASE c.CandidateNumber
         WHEN N'051' THEN @Dev1
         WHEN N'052' THEN @Dev2
         WHEN N'054' THEN @Dev1
         ELSE NULL
       END,
       CASE WHEN c.CandidateNumber = N'050' THEN GETDATE() ELSE NULL END
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE ee.ExamId = @ExamId
  AND c.CandidateNumber IN (N'050', N'051', N'052', N'053', N'054', N'055');

INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, Status)
SELECT ee.ExamEnrollmentId, @PracSec, N'Pending'
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE ee.ExamId = @ExamId
  AND c.CandidateNumber IN (N'050', N'051', N'052', N'053', N'054', N'055');
GO

INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
SELECT N'Hoàn tất', N'Tiền mặt', N'TM-' + c.CandidateNumber, 350000.00, GETDATE(), ee.ExamEnrollmentId
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
JOIN Exam ex ON ex.ExamId = ee.ExamId
WHERE ex.ExamCode = N'B1-20260601';
GO

-- Patch DB đã import seed cũ (EndTime = 17:00 khi kỳ chưa kết thúc):
-- EndTime chỉ được ghi khi examstaff bấm "Kết thúc kỳ thi".
UPDATE Exam
SET EndTime = NULL
WHERE [Status] IN (N'Chưa diễn ra', N'Mở', N'Đang diễn ra', N'Scheduled', N'Open', N'InProgress');
GO

-- Kiểm tra nhanh:
-- SELECT ExamId, ExamCode, StartTime, EndTime FROM Exam;
-- SELECT c.CandidateNumber, ees.Status, ees.ExamDeviceId FROM Candidate c
--   JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
--   JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
--   JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
--   WHERE es.SectionType = N'Lý thuyết' ORDER BY c.CandidateNumber;
