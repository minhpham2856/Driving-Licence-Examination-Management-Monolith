-- ============================================
-- SEED – hạ tầng kỳ thi (KHÔNG có thí sinh)
-- DB: DLEM_DB_2
-- Mục đích: ca thi, phòng/sân, thiết bị, phí, SHV, lỗi trừ điểm
--          sẵn sàng để import DSTS và test lần lượt.
-- Cách chạy:
--   1) DDL_DLEM_DB.sql (nếu DB mới)
--   2) Script này
--   3) (Tuỳ chọn) 600_DML_DLEM_DB.sql nếu cần ngân hàng câu hỏi LT
-- Mật khẩu tài khoản hệ thống: login123
-- Tài khoản CB kỳ thi: exam_hoa / exam_minh
-- Tài khoản SHV: shv_tung / shv_lan / shv_dung
-- ============================================

USE DLEM_DB_2;
GO

SET NOCOUNT ON;
GO

-- Xoá dữ liệu giao dịch / thí sinh / lịch thi (giữ schema)
DELETE FROM Audit;
DELETE FROM DeductionRecord;
DELETE FROM ExamScore;
DELETE FROM ExamResult;
DELETE FROM CandidateAnswer;
DELETE FROM TheoryPaper;
DELETE FROM Payment_Fee;
DELETE FROM Payment;
DELETE FROM ExamEnrollment;
DELETE FROM Candidate;
DELETE FROM ExaminerSchedule;
DELETE FROM Session_ExamArea;
DELETE FROM Session_ExamSection;
DELETE FROM ExamDevice;
DELETE FROM [Session];
DELETE FROM Exam;
DELETE FROM ScoreDeduction;
DELETE FROM Licence_Fee;
DELETE FROM Licence_ExamSection;
DELETE FROM Licence_Question;
-- Giữ Question/QuestionCategory nếu đã chạy 600_DML; bỏ comment 2 dòng dưới nếu muốn reset catalog
-- DELETE FROM Question;
-- DELETE FROM QuestionCategory;
DELETE FROM ExamRegistration;
DELETE FROM Document;
DELETE FROM Profile;
DELETE FROM ExamArea;
DELETE FROM ExamZone;
DELETE FROM ExamSection;
DELETE FROM Fee;
DELETE FROM Licence;
DELETE FROM [User];
DELETE FROM [Role];
GO

-- ============================================
-- 1. VAI TRÒ
-- ============================================
INSERT INTO [Role] (RoleName) VALUES
(N'Quản trị viên'),
(N'Sát hạch viên'),
(N'Cán bộ quản lý'),
(N'Cán bộ kỳ thi'),
(N'Thí sinh'),
(N'Người đăng ký thi');
GO

-- ============================================
-- 2. NGƯỜI DÙNG HỆ THỐNG (chỉ staff)
-- ============================================
INSERT INTO [User] (Username, Email, PasswordHash, RoleId, IsActive) VALUES
(N'admin',      N'admin@trungtamsathach.vn',       N'login123', 1, 1),
(N'shv_tung',   N'tung.nguyen@sathach.vn',         N'login123', 2, 1),
(N'shv_lan',    N'lan.tran@sathach.vn',            N'login123', 2, 1),
(N'shv_dung',   N'dung.hoang@sathach.vn',          N'login123', 2, 1),
(N'qly123',     N'quanly.hoso@trungtamsathach.vn', N'login123', 3, 1),
(N'exam_hoa',   N'hoa.le@trungtamsathach.vn',      N'login123', 4, 1),
(N'exam_minh',  N'minh.vu@trungtamsathach.vn',     N'login123', 4, 1);
GO

-- ============================================
-- 3. HỒ SƠ STAFF
-- ============================================
INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId) VALUES
(N'Phạm Văn Minh',   '1985-01-10', N'0901000001', 1, N'001085000001', N'Trung tâm Sát hạch Lái Vui, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'admin')),
(N'Nguyễn Văn Tùng', '1988-06-15', N'0911223344', 1, N'001088061501', N'12 Phạm Hùng, Nam Từ Liêm, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'shv_tung')),
(N'Trần Thị Lan',    '1990-03-22', N'0922334455', 0, N'001090032201', N'45 Lê Văn Lương, Thanh Xuân, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'shv_lan')),
(N'Hoàng Văn Dũng',  '1991-07-19', N'0933112233', 1, N'001091071901', N'88 Nguyễn Xiển, Thanh Trì, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'shv_dung')),
(N'Lê Thị Quỳnh',    '1992-08-08', N'0933445566', 0, N'001092080801', N'56 Hai Bà Trưng, Hoàn Kiếm, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'qly123')),
(N'Lê Văn Hòa',      '1991-11-11', N'0944556677', 1, N'001091111101', N'78 Trần Phú, Hải Châu, Đà Nẵng', (SELECT UserId FROM [User] WHERE Username = N'exam_hoa')),
(N'Vũ Minh Khang',   '1993-04-04', N'0955667788', 1, N'001093040401', N'34 Nguyễn Trãi, Hà Đông, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'exam_minh'));
GO

-- ============================================
-- 4. HẠNG GPLX
-- ============================================
INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId) VALUES
(N'A1', N'Xe mô tô hai bánh có dung tích xi-lanh đến 125 cm³', 18, 0, NULL),
(N'A',  N'Xe mô tô hai bánh có dung tích xi-lanh trên 125 cm³', 18, 0, NULL),
(N'B1', N'Ô tô số tự động tải trọng dưới 3.500 kg', 18, 0, NULL),
(N'B',  N'Ô tô chở người đến 8 chỗ và ô tô tải dưới 3.500 kg', 18, 10, NULL),
(N'C1', N'Ô tô tải từ 3.500 kg đến 7.500 kg', 21, 10, NULL),
(N'C',  N'Ô tô tải trên 7.500 kg', 21, 5, NULL),
(N'D1', N'Xe khách từ 10 đến 16 chỗ (không kể ghế lái)', 24, 5, NULL);
GO

UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')  WHERE LicenceClass = N'A';
UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')  WHERE LicenceClass = N'B';
UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')   WHERE LicenceClass = N'C1';
UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1') WHERE LicenceClass = N'C';
UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')   WHERE LicenceClass = N'D1';
GO

-- ============================================
-- 5. PHẦN THI
-- ============================================
INSERT INTO ExamSection (SectionName) VALUES
(N'Lý thuyết'),
(N'Thực hành trong hình'),
(N'Thực hành trên đường');
GO

-- ============================================
-- 6. HẠNG ↔ PHẦN THI (LT: A/A1 = 27', B+ = 33')
-- ============================================
INSERT INTO Licence_ExamSection (LicenceId, ExamSectionId, DurationMinutes) VALUES
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 27),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), NULL),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 27),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), NULL),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 33),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), 18),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường'), 30),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 33),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), 15),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường'), 30),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 33),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), 15),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường'), 30),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 33),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), 15),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường'), 30),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 33),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), 15),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường'), 30);
GO

-- ============================================
-- 7. KHU VỰC (ExamZone) + ĐỊA ĐIỂM (ExamArea)
-- ============================================
INSERT INTO ExamZone (ZoneName, [Location], IsActive) VALUES
(N'Khu Hà Nội chính', N'Trung tâm Sát hạch Lái Vui – Hà Nội', 1);
GO

DECLARE @ZoneId INT = (SELECT TOP 1 ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu Hà Nội chính');

INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location], ExamZoneId) VALUES
(N'Phòng thủ tục 102',     N'Hỗn hợp',   30,   N'Tầng 1, Tòa A – Trung tâm Sát hạch Lái Vui', @ZoneId),
(N'Phòng thi lý thuyết 1', N'Lý thuyết', 30,   N'Tầng 2, Tòa B', @ZoneId),
(N'Phòng thi lý thuyết 2', N'Lý thuyết', 30,   N'Tầng 2, Tòa B', @ZoneId),
(N'Sân thi mô tô A1',      N'Thực hành', 20,   N'Khu sân thi thực hành số 1', @ZoneId),
(N'Sân thi ô tô số 1',     N'Thực hành', 12,   N'Khu sân thi thực hành số 2', @ZoneId),
(N'Đường thi thực hành',   N'Thực hành', NULL, N'Lộ trình đường thi ngoài khuôn viên', @ZoneId);
GO

-- ============================================
-- 8. KỲ THI
--   B-20260601  : đủ 3 ca (LT + sa hình + đường trường) – Mở
--   B-20260615  : chỉ ca LT (test content SH = L)
--   A1-20260601 : LT + sa hình
-- ============================================
INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId) VALUES
(N'B-20260601',  '2026-06-01 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Mở',           (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'B-20260615',  '2026-06-15 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Chưa diễn ra', (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'A1-20260601', '2026-06-01 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Mở',           (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'));
GO

-- ============================================
-- 9. CA THI (IsMorningSession: sáng LT/sa hình, chiều đường trường)
-- ============================================
DECLARE @ExamB1 INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'B-20260601');
DECLARE @ExamB2 INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'B-20260615');
DECLARE @ExamA1 INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601');

DECLARE @SessB_LT INT, @SessB_Layout INT, @SessB_Road INT, @SessB2_LT INT, @SessA1_LT INT, @SessA1_Layout INT;

INSERT INTO [Session] (IsMorningSession, StartTime, EndTime, [Status], ExamId) VALUES
(1, '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'Chưa diễn ra', @ExamB1),
(1, '2026-06-01 09:30:00', '2026-06-01 11:30:00', N'Chưa diễn ra', @ExamB1),
(0, '2026-06-01 13:00:00', '2026-06-01 16:00:00', N'Chưa diễn ra', @ExamB1),
(1, '2026-06-15 07:30:00', '2026-06-15 09:00:00', N'Chưa diễn ra', @ExamB2),
(1, '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'Chưa diễn ra', @ExamA1),
(1, '2026-06-01 09:30:00', '2026-06-01 11:00:00', N'Chưa diễn ra', @ExamA1);

-- Lookup SessionId by ExamCode + IsMorningSession + StartTime (không phụ thuộc SessionName)
SET @SessB_LT      = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamB1 AND IsMorningSession = 1 AND StartTime = '2026-06-01 07:30:00');
SET @SessB_Layout  = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamB1 AND IsMorningSession = 1 AND StartTime = '2026-06-01 09:30:00');
SET @SessB_Road    = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamB1 AND IsMorningSession = 0 AND StartTime = '2026-06-01 13:00:00');
SET @SessB2_LT     = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamB2 AND IsMorningSession = 1 AND StartTime = '2026-06-15 07:30:00');
SET @SessA1_LT     = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamA1 AND IsMorningSession = 1 AND StartTime = '2026-06-01 07:30:00');
SET @SessA1_Layout = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamA1 AND IsMorningSession = 1 AND StartTime = '2026-06-01 09:30:00');

DECLARE @SecLT     INT = (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết');
DECLARE @SecLayout INT = (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình');
DECLARE @SecRoad   INT = (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường');

DECLARE @AreaLT1     INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1');
DECLARE @AreaLT2     INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2');
DECLARE @AreaAuto    INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1');
DECLARE @AreaRoad    INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường thi thực hành');
DECLARE @AreaMoto    INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A1');

-- ============================================
-- 10. CA ↔ PHẦN THI
-- ============================================
INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
(@SessB_LT, @SecLT),
(@SessB_Layout, @SecLayout),
(@SessB_Road, @SecRoad),
(@SessB2_LT, @SecLT),
(@SessA1_LT, @SecLT),
(@SessA1_Layout, @SecLayout);

-- ============================================
-- 11. CA ↔ KHU VỰC THI
-- ============================================
INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
(@SessB_LT, @AreaLT1),
(@SessB_Layout, @AreaAuto),
(@SessB_Road, @AreaRoad),
(@SessB2_LT, @AreaLT1),
(@SessA1_LT, @AreaLT2),
(@SessA1_Layout, @AreaMoto);

-- ============================================
-- 12. PHÂN CÔNG SÁT HẠCH VIÊN
-- ============================================
INSERT INTO ExaminerSchedule (SessionId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt) VALUES
(@SessB_LT, @SecLT, @AreaLT1,
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:00:00'),
(@SessB_Layout, @SecLayout, @AreaAuto,
 (SELECT UserId FROM [User] WHERE Username = N'shv_lan'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:05:00'),
(@SessB_Road, @SecRoad, @AreaRoad,
 (SELECT UserId FROM [User] WHERE Username = N'shv_dung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:10:00'),
(@SessB2_LT, @SecLT, @AreaLT1,
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_minh'), '2026-06-10 08:00:00'),
(@SessA1_LT, @SecLT, @AreaLT2,
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:15:00'),
(@SessA1_Layout, @SecLayout, @AreaMoto,
 (SELECT UserId FROM [User] WHERE Username = N'shv_lan'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:20:00');
GO

-- ============================================
-- 13. THIẾT BỊ THI
-- ============================================
INSERT INTO ExamDevice (DeviceName, DeviceType, IsActive, ExamAreaId) VALUES
(N'MT-LT-01', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-02', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-03', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-04', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-05', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-06', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-07', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-08', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-09', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-10', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-11', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2')),
(N'MT-LT-12', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2')),
(N'MT-LT-13', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2')),
(N'MT-LT-14', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2')),
(N'MT-LT-15', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2')),
(N'XM-A1-01', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A1')),
(N'XM-A1-02', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A1')),
(N'XM-A1-03', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A1')),
(N'XM-A1-DP', N'Mô tô', 0, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A1')),
(N'OTO-B-01', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1')),
(N'OTO-B-02', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1')),
(N'OTO-B-03', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1')),
(N'OTO-B-DP', N'Xe con', 0, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1')),
(N'OTO-DT-01', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường thi thực hành')),
(N'OTO-DT-02', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường thi thực hành')),
(N'OTO-DT-03', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường thi thực hành'));
GO

-- ============================================
-- 14. DANH MỤC LOẠI PHÍ
-- ============================================
INSERT INTO Fee (FeeName, FeeType, IsActive) VALUES
(N'Học phí lý thuyết',                    N'Học phí',       1),
(N'Học phí thực hành',                    N'Học phí',       1),
(N'Lệ phí thi lý thuyết',                N'Lệ phí thi',    1),
(N'Lệ phí thi thực hành trong hình',     N'Lệ phí thi',    1),
(N'Lệ phí thi thực hành trên đường',     N'Lệ phí thi',    1),
(N'Lệ phí cấp GPLX (phôi PET)',           N'Phí cấp bằng',  1),
(N'Phí xét hồ sơ và in ấn biểu mẫu',      N'Phí hành chính', 1),
(N'Phí dịch vụ hỗ trợ đăng ký trực tuyến', N'Phí hành chính', 1);
GO

-- ============================================
-- 15. BIỂU PHÍ THEO HẠNG
-- ============================================
INSERT INTO Licence_Fee (LicenceId, FeeId, Amount) VALUES
(NULL, (SELECT FeeId FROM Fee WHERE FeeName = N'Phí xét hồ sơ và in ấn biểu mẫu'), 50000.00),
(NULL, (SELECT FeeId FROM Fee WHERE FeeName = N'Phí dịch vụ hỗ trợ đăng ký trực tuyến'), 30000.00);

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
SELECT l.LicenceId, f.FeeId, v.Amount
FROM Licence l
CROSS JOIN (VALUES
    (N'Học phí lý thuyết', 450000.00),
    (N'Học phí thực hành', 1050000.00),
    (N'Lệ phí thi lý thuyết', 65000.00),
    (N'Lệ phí thi thực hành trong hình', 350000.00),
    (N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
) v(FeeName, Amount)
JOIN Fee f ON f.FeeName = v.FeeName
WHERE l.LicenceClass = N'A1';

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
SELECT l.LicenceId, f.FeeId, v.Amount
FROM Licence l
CROSS JOIN (VALUES
    (N'Học phí lý thuyết', 500000.00),
    (N'Học phí thực hành', 1200000.00),
    (N'Lệ phí thi lý thuyết', 65000.00),
    (N'Lệ phí thi thực hành trong hình', 400000.00),
    (N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
) v(FeeName, Amount)
JOIN Fee f ON f.FeeName = v.FeeName
WHERE l.LicenceClass = N'A';

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
SELECT l.LicenceId, f.FeeId, v.Amount
FROM Licence l
CROSS JOIN (VALUES
    (N'Học phí lý thuyết', 1800000.00),
    (N'Học phí thực hành', 7700000.00),
    (N'Lệ phí thi lý thuyết', 100000.00),
    (N'Lệ phí thi thực hành trong hình', 250000.00),
    (N'Lệ phí thi thực hành trên đường', 80000.00),
    (N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
) v(FeeName, Amount)
JOIN Fee f ON f.FeeName = v.FeeName
WHERE l.LicenceClass = N'B1';

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
SELECT l.LicenceId, f.FeeId, v.Amount
FROM Licence l
CROSS JOIN (VALUES
    (N'Học phí lý thuyết', 2200000.00),
    (N'Học phí thực hành', 9300000.00),
    (N'Lệ phí thi lý thuyết', 100000.00),
    (N'Lệ phí thi thực hành trong hình', 250000.00),
    (N'Lệ phí thi thực hành trên đường', 80000.00),
    (N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
) v(FeeName, Amount)
JOIN Fee f ON f.FeeName = v.FeeName
WHERE l.LicenceClass = N'B';

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
SELECT l.LicenceId, f.FeeId, v.Amount
FROM Licence l
CROSS JOIN (VALUES
    (N'Học phí lý thuyết', 2500000.00),
    (N'Học phí thực hành', 10500000.00),
    (N'Lệ phí thi lý thuyết', 100000.00),
    (N'Lệ phí thi thực hành trong hình', 250000.00),
    (N'Lệ phí thi thực hành trên đường', 80000.00),
    (N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
) v(FeeName, Amount)
JOIN Fee f ON f.FeeName = v.FeeName
WHERE l.LicenceClass = N'C1';

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
SELECT l.LicenceId, f.FeeId, v.Amount
FROM Licence l
CROSS JOIN (VALUES
    (N'Học phí lý thuyết', 2800000.00),
    (N'Học phí thực hành', 12000000.00),
    (N'Lệ phí thi lý thuyết', 100000.00),
    (N'Lệ phí thi thực hành trong hình', 250000.00),
    (N'Lệ phí thi thực hành trên đường', 80000.00),
    (N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
) v(FeeName, Amount)
JOIN Fee f ON f.FeeName = v.FeeName
WHERE l.LicenceClass = N'C';

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
SELECT l.LicenceId, f.FeeId, v.Amount
FROM Licence l
CROSS JOIN (VALUES
    (N'Học phí lý thuyết', 1500000.00),
    (N'Học phí thực hành', 4500000.00),
    (N'Lệ phí thi lý thuyết', 100000.00),
    (N'Lệ phí thi thực hành trong hình', 250000.00),
    (N'Lệ phí thi thực hành trên đường', 80000.00),
    (N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
) v(FeeName, Amount)
JOIN Fee f ON f.FeeName = v.FeeName
WHERE l.LicenceClass = N'D1';
GO

-- ============================================
-- 16. BẢNG LỖI TRỪ ĐIỂM (TT 12/2025/TT-BCA)
-- ============================================

INSERT INTO ScoreDeduction ([Reason], Points, IsCritical, LicenceId, ExamSectionId) VALUES
(N'Bánh xe đè vào vạch cản của hình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Chạm chân xuống đất trong quá trình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Hai bánh xe của xe sát hạch ra ngoài hình sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không hoàn thành bài sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Thời gian thực hiện các bài sát hạch quá 10 phút, cứ quá 01 phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe sát hạch bị đổ trong quá trình sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng trình tự bài sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch cản của hình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Chạm chân xuống đất trong quá trình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Hai bánh xe của xe sát hạch ra ngoài hình sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không hoàn thành bài sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Thời gian thực hiện các bài sát hạch quá 10 phút, cứ quá 01 phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe sát hạch bị đổ trong quá trình sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng trình tự bài sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đi ra ngoài vạch giới hạn hình sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không hoàn thành bài sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Thời gian thực hiện bài sát hạch quá 10 phút, cứ quá 01 phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị chết máy', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng trình tự bài sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Điều khiển xe bị rung giật mạnh', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe trước và bánh xe sau bên lái phụ không qua vùng giới hạn của hình vệt bánh xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch, cứ quá 05 giây', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Chưa ghép được xe vào nơi đỗ (khi kết thúc bài sát hạch, còn một phần thân xe nằm ngoài khu vực ghép xe)', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe chưa đến vạch dừng quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe quá vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Ghép xe không đúng vị trí quy định (toàn bộ thân xe nằm trong khu vực ghép xe nhưng không có tín hiệu báo kết thúc)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật và tắt đèn xi nhan trái kịp thời', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan khi rẽ trái hoặc rẽ phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không dừng xe ở vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không qua vạch kết thúc', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi số theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi tốc độ theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi đúng số và đúng tốc độ quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt đèn xi nhan trái ở khoảng cách 05 mét sau vạch xuất phát (đèn xanh trên xe tắt)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe lên vỉa hè', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe quá tốc độ quy định, cứ 3 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe vi phạm vạch kẻ đường để thiết bị báo không thực hiện đúng trình tự bài thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá thời gian 30 giây kể từ khi dừng xe không khởi hành xe qua vạch dừng', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Thời gian thực hiện bài sát hạch, cứ quá 02 phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tổng thời gian đến bài sát hạch đang thực hiện quá quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Vi phạm tín hiệu đèn điều khiển giao thông (đi qua ngã tư khi đèn tín hiệu màu đỏ)', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị tụt dốc quá 500 mm kể từ khi dừng xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe quá tốc độ quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Điểm sát hạch dưới 80 điểm', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Khi tăng hoặc giảm số, xe bị choạng lái quá làn đường quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không kéo phanh tay khi xe dừng hẳn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không nhả hết phanh tay khi khởi hành', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thực hiện theo hiệu lệnh của sát hạch viên', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát, chưa khởi hành xe qua vị trí xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không giảm được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không tăng được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 15 m không tăng từ số 1 lên số 3', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Vi phạm quy tắc giao thông đường bộ', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xe bị rung giật mạnh', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Bánh xe trước và bánh xe sau bên lái phụ không qua vùng giới hạn của hình vệt bánh xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch, cứ quá 05 giây', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Chưa ghép được xe vào nơi đỗ (khi kết thúc bài sát hạch, còn một phần thân xe nằm ngoài khu vực ghép xe)', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe chưa đến vạch dừng quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe quá vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Ghép xe không đúng vị trí quy định (toàn bộ thân xe nằm trong khu vực ghép xe nhưng không có tín hiệu báo kết thúc)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật và tắt đèn xi nhan trái kịp thời', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan khi rẽ trái hoặc rẽ phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không dừng xe ở vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không qua vạch kết thúc', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi số theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi tốc độ theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi đúng số và đúng tốc độ quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt đèn xi nhan trái ở khoảng cách 05 mét sau vạch xuất phát (đèn xanh trên xe tắt)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe lên vỉa hè', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe quá tốc độ quy định, cứ 3 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe vi phạm vạch kẻ đường để thiết bị báo không thực hiện đúng trình tự bài thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá thời gian 30 giây kể từ khi dừng xe không khởi hành xe qua vạch dừng', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Thời gian thực hiện bài sát hạch, cứ quá 02 phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tổng thời gian đến bài sát hạch đang thực hiện quá quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Vi phạm tín hiệu đèn điều khiển giao thông (đi qua ngã tư khi đèn tín hiệu màu đỏ)', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị tụt dốc quá 500 mm kể từ khi dừng xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe quá tốc độ quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Điểm sát hạch dưới 80 điểm', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Khi tăng hoặc giảm số, xe bị choạng lái quá làn đường quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không kéo phanh tay khi xe dừng hẳn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không nhả hết phanh tay khi khởi hành', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thực hiện theo hiệu lệnh của sát hạch viên', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát, chưa khởi hành xe qua vị trí xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không giảm được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không tăng được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 15 m không tăng từ số 1 lên số 3', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Vi phạm quy tắc giao thông đường bộ', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xe bị rung giật mạnh', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Bánh xe trước và bánh xe sau bên lái phụ không qua vùng giới hạn của hình vệt bánh xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch, cứ quá 05 giây', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Chưa ghép được xe vào nơi đỗ (khi kết thúc bài sát hạch, còn một phần thân xe nằm ngoài khu vực ghép xe)', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe chưa đến vạch dừng quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe quá vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Ghép xe không đúng vị trí quy định (toàn bộ thân xe nằm trong khu vực ghép xe nhưng không có tín hiệu báo kết thúc)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật và tắt đèn xi nhan trái kịp thời', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan khi rẽ trái hoặc rẽ phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không dừng xe ở vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không qua vạch kết thúc', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi số theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi tốc độ theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi đúng số và đúng tốc độ quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt đèn xi nhan trái ở khoảng cách 05 mét sau vạch xuất phát (đèn xanh trên xe tắt)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe lên vỉa hè', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe quá tốc độ quy định, cứ 3 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe vi phạm vạch kẻ đường để thiết bị báo không thực hiện đúng trình tự bài thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá thời gian 30 giây kể từ khi dừng xe không khởi hành xe qua vạch dừng', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Thời gian thực hiện bài sát hạch, cứ quá 02 phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tổng thời gian đến bài sát hạch đang thực hiện quá quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Vi phạm tín hiệu đèn điều khiển giao thông (đi qua ngã tư khi đèn tín hiệu màu đỏ)', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị tụt dốc quá 500 mm kể từ khi dừng xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe quá tốc độ quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Điểm sát hạch dưới 80 điểm', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Khi tăng hoặc giảm số, xe bị choạng lái quá làn đường quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không kéo phanh tay khi xe dừng hẳn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không nhả hết phanh tay khi khởi hành', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thực hiện theo hiệu lệnh của sát hạch viên', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát, chưa khởi hành xe qua vị trí xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không giảm được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không tăng được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 15 m không tăng từ số 1 lên số 3', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Vi phạm quy tắc giao thông đường bộ', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xe bị rung giật mạnh', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Bánh xe trước và bánh xe sau bên lái phụ không qua vùng giới hạn của hình vệt bánh xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch, cứ quá 05 giây', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Chưa ghép được xe vào nơi đỗ (khi kết thúc bài sát hạch, còn một phần thân xe nằm ngoài khu vực ghép xe)', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe chưa đến vạch dừng quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe quá vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Ghép xe không đúng vị trí quy định (toàn bộ thân xe nằm trong khu vực ghép xe nhưng không có tín hiệu báo kết thúc)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật và tắt đèn xi nhan trái kịp thời', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan khi rẽ trái hoặc rẽ phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không dừng xe ở vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không qua vạch kết thúc', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi số theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi tốc độ theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi đúng số và đúng tốc độ quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt đèn xi nhan trái ở khoảng cách 05 mét sau vạch xuất phát (đèn xanh trên xe tắt)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe lên vỉa hè', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe quá tốc độ quy định, cứ 3 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe vi phạm vạch kẻ đường để thiết bị báo không thực hiện đúng trình tự bài thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá thời gian 30 giây kể từ khi dừng xe không khởi hành xe qua vạch dừng', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Thời gian thực hiện bài sát hạch, cứ quá 02 phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tổng thời gian đến bài sát hạch đang thực hiện quá quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Vi phạm tín hiệu đèn điều khiển giao thông (đi qua ngã tư khi đèn tín hiệu màu đỏ)', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị tụt dốc quá 500 mm kể từ khi dừng xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe quá tốc độ quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Điểm sát hạch dưới 80 điểm', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Khi tăng hoặc giảm số, xe bị choạng lái quá làn đường quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không kéo phanh tay khi xe dừng hẳn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không nhả hết phanh tay khi khởi hành', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thực hiện theo hiệu lệnh của sát hạch viên', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát, chưa khởi hành xe qua vị trí xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không giảm được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không tăng được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 15 m không tăng từ số 1 lên số 3', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Vi phạm quy tắc giao thông đường bộ', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xe bị rung giật mạnh', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Khi tăng hoặc giảm số, xe bị choạng lái quá làn đường quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không kéo phanh tay khi xe dừng hẳn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không nhả hết phanh tay khi khởi hành', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không thực hiện theo hiệu lệnh của sát hạch viên', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát, chưa khởi hành xe qua vị trí xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không giảm được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 100 m không tăng được số, tốc độ', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Trong khoảng 15 m không tăng từ số 1 lên số 3', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Vi phạm quy tắc giao thông đường bộ', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xe bị rung giật mạnh', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường')),
(N'Không phanh dừng xe trong thời gian 3 giây khi có tín hiệu tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật tín hiệu nguy hiểm trên xe trong thời gian 5 giây', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt tín hiệu nguy hiểm trên xe trước khi đi tiếp sau tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thực hiện đúng các thao tác xử lý tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không phanh dừng xe trong thời gian 3 giây khi có tín hiệu tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật tín hiệu nguy hiểm trên xe trong thời gian 5 giây', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt tín hiệu nguy hiểm trên xe trước khi đi tiếp sau tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thực hiện đúng các thao tác xử lý tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không phanh dừng xe trong thời gian 3 giây khi có tín hiệu tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật tín hiệu nguy hiểm trên xe trong thời gian 5 giây', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt tín hiệu nguy hiểm trên xe trước khi đi tiếp sau tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thực hiện đúng các thao tác xử lý tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không phanh dừng xe trong thời gian 3 giây khi có tín hiệu tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật tín hiệu nguy hiểm trên xe trong thời gian 5 giây', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt tín hiệu nguy hiểm trên xe trước khi đi tiếp sau tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thực hiện đúng các thao tác xử lý tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không phanh dừng xe trong thời gian 3 giây khi có tín hiệu tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật tín hiệu nguy hiểm trên xe trong thời gian 5 giây', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt tín hiệu nguy hiểm trên xe trước khi đi tiếp sau tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thực hiện đúng các thao tác xử lý tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'));
GO
PRINT N'SEED OK – hạ tầng sẵn sàng (0 thí sinh).';
PRINT N'  Kỳ thi: B-20260601 (đủ 3 ca), B-20260615 (chỉ LT), A1-20260601 (LT + sa hình).';
PRINT N'  Login CB kỳ thi: exam_hoa / login123';
GO