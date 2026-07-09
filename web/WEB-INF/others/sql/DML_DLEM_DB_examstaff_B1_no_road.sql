-- ============================================
-- DML mới cho luồng examstaff/public-call
-- Kịch bản: chỉ còn A1, A, B1 và KHÔNG có đường trường
-- Không ghi đè file DML cũ. Chạy file này thay cho DML cũ khi cần test.
-- Mục tiêu: dữ liệu dễ test cho examstaff/public-call, không seed hạng B / không seed road test.
-- ============================================

USE DLEM_DB_2;
GO

DELETE FROM Audit;
DELETE FROM DeductionRecord;
DELETE FROM ScoreDeduction;
DELETE FROM ExamScore;
DELETE FROM ExamResult;
DELETE FROM CandidateAnswer;
DELETE FROM TheoryPaper;
DELETE FROM Payment_Fee;
DELETE FROM Payment;
DELETE FROM Licence_Fee;
DELETE FROM ExamEnrollment;
DELETE FROM Candidate;
DELETE FROM ExamRegistration;
DELETE FROM Document;
DELETE FROM Profile;
DELETE FROM ExaminerSchedule;
DELETE FROM Session_ExamArea;
DELETE FROM Session_ExamSection;
DELETE FROM Licence_ExamSection;
DELETE FROM Licence_Question;
DELETE FROM Question;
DELETE FROM QuestionCategory;
DELETE FROM ExamDevice;
DELETE FROM [Session];
DELETE FROM Exam;
DELETE FROM ExamArea;
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
-- 2. NGƯỜI DÙNG
-- ============================================
INSERT INTO [User] (Username, Email, PasswordHash, RoleId, IsActive) VALUES
(N'admin',       N'admin@trungtamsathach.vn',   N'login123', 1, 1),
(N'shv_tung',    N'tung.nguyen@sathach.vn',     N'login123', 2, 1),
(N'shv_lan',     N'lan.tran@sathach.vn',        N'login123', 2, 1),
(N'shv_dung',    N'dung.hoang@sathach.vn',      N'login123', 2, 1),
(N'qly123',      N'quanly.hoso@trungtamsathach.vn', N'login123', 3, 1),
(N'exam_hoa',    N'hoa.le@trungtamsathach.vn',  N'login123', 4, 1),
(N'exam_minh',   N'minh.vu@trungtamsathach.vn', N'login123', 4, 1),
(N'user_an',     N'an.nguyen@gmail.com',        N'login123', 6, 1),
(N'user_binh',   N'binh.tran@gmail.com',        N'login123', 6, 1),
(N'user_phuong', N'phuong.vu@gmail.com',        N'login123', 6, 1),
(N'user_hai',    N'hai.do@gmail.com',           N'login123', 6, 1);
GO

-- ============================================
-- 3. HỒ SƠ CÁ NHÂN
-- ============================================
INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId) VALUES
(N'Phạm Văn Minh',   '1985-01-10', N'0901000001', 1, N'001085000001', N'Trung tâm Sát hạch Lái Vui, Hà Nội', 1),
(N'Nguyễn Văn Tùng', '1988-06-15', N'0911223344', 1, N'001088061501', N'12 Phạm Hùng, Nam Từ Liêm, Hà Nội', 2),
(N'Trần Thị Lan',    '1990-03-22', N'0922334455', 0, N'001090032201', N'45 Lê Văn Lương, Thanh Xuân, Hà Nội', 3),
(N'Hoàng Văn Dũng',  '1991-07-19', N'0933112233', 1, N'001091071901', N'88 Nguyễn Xiển, Thanh Trì, Hà Nội', 4),
(N'Lê Thị Quỳnh',    '1992-08-08', N'0933445566', 0, N'001092080801', N'56 Hai Bà Trưng, Hoàn Kiếm, Hà Nội', 5),
(N'Lê Văn Hòa',      '1991-11-11', N'0944556677', 1, N'001091111101', N'78 Trần Phú, Hải Châu, Đà Nẵng', 6),
(N'Vũ Minh Khang',   '1993-04-04', N'0955667788', 1, N'001093040401', N'34 Nguyễn Trãi, Hà Đông, Hà Nội', 7),
(N'Nguyễn Văn An',   '2000-03-15', N'0989123456', 1, N'001200031501', N'123 Lê Duẩn, Đống Đa, Hà Nội', 8),
(N'Trần Thị Bình',   '1995-08-22', N'0912345678', 0, N'001095082201', N'45 Nguyễn Huệ, Quận 1, TP.HCM', 9),
(N'Vũ Thị Phương',   '1998-12-12', N'0967890123', 0, N'001198121201', N'34 Nguyễn Trãi, Hà Đông, Hà Nội', 10),
(N'Đỗ Văn Hải',      '2001-04-20', N'0945678901', 1, N'001201042001', N'90 Lê Lợi, Quận 1, TP.HCM', 11);
GO

-- ============================================
-- 4. HẠNG GPLX
-- Chỉ seed A1, A, B1, C1, C, D1. Không seed B.
-- ============================================
INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId) VALUES
(N'A1', N'Xe mô tô hai bánh có dung tích xi-lanh đến 125 cm³', 18, 0, NULL),
(N'A',  N'Xe mô tô hai bánh có dung tích xi-lanh trên 125 cm³', 18, 0, NULL),
(N'B1', N'Ô tô số tự động tải trọng dưới 3.500 kg', 18, 0, NULL),
(N'C1', N'Ô tô tải từ 3.500 kg đến 7.500 kg', 21, 10, NULL),
(N'C',  N'Ô tô tải trên 7.500 kg', 21, 5, NULL),
(N'D1', N'Xe khách từ 10 đến 16 chỗ (không kể ghế lái)', 24, 5, NULL);
GO

UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')
WHERE LicenceClass = N'A';
UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')
WHERE LicenceClass = N'C1';
UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1')
WHERE LicenceClass = N'C';
UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')
WHERE LicenceClass = N'D1';
GO

-- ============================================
-- 5. HỒ SƠ ĐĂNG KÝ THI
-- ============================================
INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId) VALUES
(N'Duyệt', N'Đủ hồ sơ, đủ điều kiện sức khỏe', 8,  (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'Duyệt', N'Đã xác minh căn cước và giấy khám sức khỏe', 9,  (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'Duyệt', N'Đăng ký thi cấp mới hạng A1', 10, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')),
(N'Duyệt', N'Đăng ký thi cấp mới hạng B1', 11, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'));
GO

-- ============================================
-- 6. KỲ THI
-- Chỉ dùng A1, A, B1.
-- ============================================
INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId) VALUES
(N'B1-20260601', '2026-06-01 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Mở',
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'B1-20260615', '2026-06-15 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Chưa diễn ra',
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'A1-20260601', '2026-06-01 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Mở',
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')),
(N'A-20260610', '2026-06-10 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Chưa diễn ra',
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'));
GO

-- ============================================
-- 7. PHẦN THI
-- Không có "Thực hành trên đường"
-- ============================================
INSERT INTO ExamSection (SectionName) VALUES
(N'Lý thuyết'),
(N'Thực hành trong hình');
GO

-- ============================================
-- 8. HẠNG ↔ PHẦN THI
-- B1 chỉ còn lý thuyết + thực hành trong hình
-- ============================================
INSERT INTO Licence_ExamSection (LicenceId, ExamSectionId, DurationMinutes) VALUES
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 19),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), NULL),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 19),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), NULL),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 20),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), 18),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 20),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), 15),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 20),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'C'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), 15),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'), 20),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'D1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'), 15);
GO

-- ============================================
-- 9. KHU VỰC / PHÒNG THI
-- Không seed phòng đường trường
-- ============================================
INSERT INTO ExamZone (ZoneName, [Location], IsActive) VALUES
(N'Khu Hà Nội chính', N'Trung tâm Sát hạch Lái Vui – Hà Nội', 1);
GO

DECLARE @ZoneId INT = (SELECT TOP 1 ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu Hà Nội chính');

INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location], ExamZoneId) VALUES
(N'Phòng thủ tục 102',      N'Hỗn hợp',   30, N'Tầng 1, Tòa A – Trung tâm Sát hạch Lái Vui', @ZoneId),
(N'Phòng thi lý thuyết 1',  N'Lý thuyết', 30, N'Tầng 2, Tòa B', @ZoneId),
(N'Phòng thi lý thuyết 2',  N'Lý thuyết', 30, N'Tầng 2, Tòa B', @ZoneId),
(N'Sân thi mô tô A1',       N'Thực hành', 20, N'Khu sân thi thực hành số 1', @ZoneId),
(N'Sân thi mô tô A',        N'Thực hành', 20, N'Khu sân thi mô tô nâng cao', @ZoneId),
(N'Sân thi ô tô số 1',      N'Thực hành', 12, N'Khu sân thi thực hành số 2', @ZoneId);
GO

-- ============================================
-- 10. CA THI
-- Schema mới không có SessionName; nhãn ca được suy ra ở view layer.
-- ============================================
DECLARE @ExamB1 INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260601');
DECLARE @ExamB1K2 INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260615');
DECLARE @ExamA1 INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601');
DECLARE @ExamA INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A-20260610');

DECLARE @SessB1_LT INT, @SessB1_Layout INT, @SessB1K2_LT INT, @SessA1_LT INT, @SessA1_Layout INT, @SessA_LT INT, @SessA_Layout INT;

INSERT INTO [Session] (IsMorningSession, StartTime, EndTime, [Status], ExamId) VALUES
(1, '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'Đang diễn ra', @ExamB1),
(1, '2026-06-01 09:30:00', '2026-06-01 11:30:00', N'Chưa diễn ra', @ExamB1),
(1, '2026-06-15 07:30:00', '2026-06-15 09:00:00', N'Chưa diễn ra', @ExamB1K2),
(1, '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'Đang diễn ra', @ExamA1),
(1, '2026-06-01 09:30:00', '2026-06-01 11:00:00', N'Chưa diễn ra', @ExamA1),
(1, '2026-06-10 07:30:00', '2026-06-10 09:00:00', N'Chưa diễn ra', @ExamA),
(1, '2026-06-10 09:30:00', '2026-06-10 11:00:00', N'Chưa diễn ra', @ExamA);

SET @SessB1_LT     = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamB1   AND IsMorningSession = 1 AND StartTime = '2026-06-01 07:30:00');
SET @SessB1_Layout = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamB1   AND IsMorningSession = 1 AND StartTime = '2026-06-01 09:30:00');
SET @SessB1K2_LT   = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamB1K2 AND IsMorningSession = 1 AND StartTime = '2026-06-15 07:30:00');
SET @SessA1_LT     = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamA1   AND IsMorningSession = 1 AND StartTime = '2026-06-01 07:30:00');
SET @SessA1_Layout = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamA1   AND IsMorningSession = 1 AND StartTime = '2026-06-01 09:30:00');
SET @SessA_LT      = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamA    AND IsMorningSession = 1 AND StartTime = '2026-06-10 07:30:00');
SET @SessA_Layout  = (SELECT SessionId FROM [Session] WHERE ExamId = @ExamA    AND IsMorningSession = 1 AND StartTime = '2026-06-10 09:30:00');

DECLARE @SecLT INT = (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết');
DECLARE @SecLayout INT = (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình');
DECLARE @AreaLT1 INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1');
DECLARE @AreaLT2 INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2');
DECLARE @AreaMotoA1 INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A1');
DECLARE @AreaMotoA INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A');
DECLARE @AreaAuto INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1');

-- ============================================
-- 11. CA ↔ PHẦN THI
-- ============================================
INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
(@SessB1_LT, @SecLT),
(@SessB1_Layout, @SecLayout),
(@SessB1K2_LT, @SecLT),
(@SessA1_LT, @SecLT),
(@SessA1_Layout, @SecLayout),
(@SessA_LT, @SecLT),
(@SessA_Layout, @SecLayout);

-- ============================================
-- 12. CA ↔ KHU VỰC THI
-- ============================================
INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
(@SessB1_LT, @AreaLT1),
(@SessB1_Layout, @AreaAuto),
(@SessB1K2_LT, @AreaLT1),
(@SessA1_LT, @AreaLT2),
(@SessA1_Layout, @AreaMotoA1),
(@SessA_LT, @AreaLT2),
(@SessA_Layout, @AreaMotoA);

-- ============================================
-- 13. PHÂN CÔNG SÁT HẠCH VIÊN
-- ============================================
INSERT INTO ExaminerSchedule (SessionId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt) VALUES
(@SessB1_LT, @SecLT, @AreaLT1,
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:00:00'),
(@SessB1_Layout, @SecLayout, @AreaAuto,
 (SELECT UserId FROM [User] WHERE Username = N'shv_lan'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:05:00'),
(@SessA1_LT, @SecLT, @AreaLT2,
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:15:00');
GO

-- ============================================
-- 14. THIẾT BỊ THI
-- Không seed xe/phòng đường trường
-- ============================================
INSERT INTO ExamDevice (DeviceName, DeviceType, IsActive, ExamAreaId) VALUES
(N'MT-LT-01', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-02', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-03', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 1')),
(N'MT-LT-11', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2')),
(N'MT-LT-12', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi lý thuyết 2')),
(N'XM-A1-01', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A1')),
(N'XM-A1-02', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A1')),
(N'XM-A-01',  N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A')),
(N'XM-A-02',  N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô A')),
(N'OTO-B1-01', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1')),
(N'OTO-B1-02', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1')),
(N'OTO-B1-03', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô số 1'));
GO

-- ============================================
-- 15. DANH MỤC PHÍ
-- Không seed phí đường trường
-- ============================================
INSERT INTO Fee (FeeName, FeeType, IsActive) VALUES
(N'Học phí lý thuyết',                    N'Học phí',        1),
(N'Học phí thực hành',                    N'Học phí',        1),
(N'Lệ phí thi lý thuyết',                 N'Lệ phí thi',     1),
(N'Lệ phí thi thực hành trong hình',      N'Lệ phí thi',     1),
(N'Lệ phí cấp GPLX (phôi PET)',           N'Phí cấp bằng',   1),
(N'Phí xét hồ sơ và in ấn biểu mẫu',      N'Phí hành chính', 1),
(N'Phí dịch vụ hỗ trợ đăng ký trực tuyến',N'Phí hành chính', 1);
GO

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount) VALUES
(NULL, (SELECT FeeId FROM Fee WHERE FeeName = N'Phí xét hồ sơ và in ấn biểu mẫu'), 50000.00),
(NULL, (SELECT FeeId FROM Fee WHERE FeeName = N'Phí dịch vụ hỗ trợ đăng ký trực tuyến'), 30000.00);
GO

INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
SELECT l.LicenceId, f.FeeId, v.Amount
FROM Licence l
JOIN (VALUES
    (N'A1', N'Học phí lý thuyết', 450000.00),
    (N'A1', N'Học phí thực hành', 1050000.00),
    (N'A1', N'Lệ phí thi lý thuyết', 65000.00),
    (N'A1', N'Lệ phí thi thực hành trong hình', 350000.00),
    (N'A1', N'Lệ phí cấp GPLX (phôi PET)', 135000.00),
    (N'A',  N'Học phí lý thuyết', 500000.00),
    (N'A',  N'Học phí thực hành', 1200000.00),
    (N'A',  N'Lệ phí thi lý thuyết', 65000.00),
    (N'A',  N'Lệ phí thi thực hành trong hình', 400000.00),
    (N'A',  N'Lệ phí cấp GPLX (phôi PET)', 135000.00),
    (N'B1', N'Học phí lý thuyết', 1800000.00),
    (N'B1', N'Học phí thực hành', 7700000.00),
    (N'B1', N'Lệ phí thi lý thuyết', 100000.00),
    (N'B1', N'Lệ phí thi thực hành trong hình', 250000.00),
    (N'B1', N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
) v(LicenceClass, FeeName, Amount) ON v.LicenceClass = l.LicenceClass
JOIN Fee f ON f.FeeName = v.FeeName;
GO

-- ============================================
-- 20. BẢNG LỖI TRỪ ĐIỂM
-- Chỉ tạo cho phần "Thực hành trong hình"
-- ============================================
INSERT INTO ScoreDeduction ([Reason], Points, IsCritical, LicenceId, ExamSectionId) VALUES
(N'Bánh xe đi ra ngoài vạch giới hạn hình sát hạch', 100.00, 1,
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch', 10.00, 0,
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không hoàn thành bài sát hạch', 100.00, 1,
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị chết máy', 10.00, 0,
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch cản của hình sát hạch', 5.00, 0,
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không hoàn thành bài sát hạch', 100.00, 1,
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch cản của hình sát hạch', 5.00, 0,
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không hoàn thành bài sát hạch', 100.00, 1,
 (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'));
GO

-- ============================================
-- Kiểm tra nhanh sau khi chạy
-- ============================================
-- SELECT LicenceClass FROM Licence ORDER BY LicenceClass;
-- SELECT SectionName FROM ExamSection ORDER BY SectionName;
-- SELECT AreaName FROM ExamArea ORDER BY AreaName;
-- SELECT ExamCode FROM Exam ORDER BY ExamCode;
-- SELECT SessionId, ExamId, IsMorningSession, StartTime, EndTime FROM [Session] ORDER BY StartTime;
