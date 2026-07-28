-- ============================================
-- DML – DLEM_DB_2 (khớp DDL_DLEM_DB.sql hợp nhất)
-- Mật khẩu mặc định mọi tài khoản: login123
-- ExamDates / RegistrationDates / OfficialExamCandidate: không seed —
-- managing staff và police tạo qua UI.
-- ============================================

USE DLEM_DB_2;
GO

SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;
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
DELETE FROM ExamEnrollmentSection;
DELETE FROM ExamEnrollment;
DELETE FROM Candidate;
DELETE FROM OfficialExamCandidate;
DELETE FROM RegistrationDates;
DELETE FROM ExamRegistration;
DELETE FROM Document;
DELETE FROM DocumentType;
DELETE FROM Profile;
DELETE FROM ExaminerSchedule;
DELETE FROM Exam_ExamArea;
DELETE FROM Licence_Question;
DELETE FROM Question;
DELETE FROM QuestionCategory;
DELETE FROM ExamDevice;
DELETE FROM ExamSection;
DELETE FROM Exam;
DELETE FROM ExamDates;
DELETE FROM ExamArea;
DELETE FROM ExamZone;
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
(N'Người đăng ký thi'),
(N'Cán bộ CSGT');
GO

-- ============================================
-- 2. NGƯỜI DÙNG HỆ THỐNG
-- PasswordHash = BCrypt of login123
-- ============================================
DECLARE @Pw NVARCHAR(255) = N'$2a$10$E8ocGIv4gRp6xZurl5egNuxir.0zn/5BUJMO5kIjdz38csrH3s7Cm';

INSERT INTO [User] (Username, Email, PasswordHash, RoleId, IsActive) VALUES
(N'admin',       N'admin@trungtamsathach.vn',       @Pw, 1, 1),
(N'shv_tung',    N'tung.nguyen@sathach.vn',         @Pw, 2, 1),
(N'shv_lan',     N'lan.tran@sathach.vn',            @Pw, 2, 1),
(N'shv_dung',    N'dung.hoang@sathach.vn',          @Pw, 2, 1),
(N'qly123',      N'quanly.hoso@trungtamsathach.vn', @Pw, 3, 1),
(N'exam_hoa',    N'hoa.le@trungtamsathach.vn',      @Pw, 4, 1),
(N'exam_minh',   N'minh.vu@trungtamsathach.vn',     @Pw, 4, 1),
(N'user_an',     N'an.nguyen@gmail.com',            @Pw, 6, 1),
(N'user_binh',   N'binh.tran@gmail.com',            @Pw, 6, 1),
(N'user_chinh',  N'chinh.le@gmail.com',             @Pw, 6, 1),
(N'user_dung',   N'dung.pham@gmail.com',            @Pw, 6, 1),
(N'user_em',     N'em.hoang@gmail.com',             @Pw, 6, 1),
(N'user_phuong', N'phuong.vu@gmail.com',            @Pw, 6, 1),
(N'user_hai',    N'hai.do@gmail.com',               @Pw, 6, 1),
(N'user_kim',    N'kim.ngo@gmail.com',              @Pw, 6, 1),
(N'user_long',   N'long.bui@gmail.com',             @Pw, 6, 0),
(N'user_hoa',    N'hoa.thi@gmail.com',              @Pw, 6, 1),
(N'user_khoa',   N'khoa.tran@gmail.com',            @Pw, 6, 1),
(N'police123',   N'police@csgt.gov.vn',             @Pw, 7, 1);
GO

-- ============================================
-- 3. HỒ SƠ CÁ NHÂN
-- ============================================
INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId) VALUES
(N'Phạm Văn Minh',     '1985-01-10', N'0901000001', 1, N'001085000001', N'Trung tâm Sát hạch Lái Vui, Hà Nội', 1),
(N'Nguyễn Văn Tùng',   '1988-06-15', N'0911223344', 1, N'001088061501', N'12 Phạm Hùng, Nam Từ Liêm, Hà Nội', 2),
(N'Trần Thị Lan',      '1990-03-22', N'0922334455', 0, N'001090032201', N'45 Lê Văn Lương, Thanh Xuân, Hà Nội', 3),
(N'Hoàng Văn Dũng',    '1991-07-19', N'0933112233', 1, N'001091071901', N'88 Nguyễn Xiển, Thanh Trì, Hà Nội', 4),
(N'Lê Thị Quỳnh',      '1992-08-08', N'0933445566', 0, N'001092080801', N'56 Hai Bà Trưng, Hoàn Kiếm, Hà Nội', 5),
(N'Lê Văn Hòa',        '1991-11-11', N'0944556677', 1, N'001091111101', N'78 Trần Phú, Hải Châu, Đà Nẵng', 6),
(N'Vũ Minh Khang',     '1993-04-04', N'0955667788', 1, N'001093040401', N'34 Nguyễn Trãi, Hà Đông, Hà Nội', 7),
(N'Nguyễn Văn An',     '2000-03-15', N'0989123456', 1, N'001200031501', N'123 Lê Duẩn, Đống Đa, Hà Nội', 8),
(N'Trần Thị Bình',     '1995-08-22', N'0912345678', 0, N'001095082201', N'45 Nguyễn Huệ, Quận 1, TP.HCM', 9),
(N'Lê Văn Chính',      '1988-11-10', N'0978563412', 1, N'001088111001', N'78 Trần Phú, Hải Châu, Đà Nẵng', 10),
(N'Phạm Thị Dung',     '2002-01-28', N'0934567890', 0, N'001202012801', N'56 Hai Bà Trưng, Hoàn Kiếm, Hà Nội', 11),
(N'Hoàng Văn Em',      '1990-06-05', N'0901234567', 1, N'001090060501', N'12 Lý Thường Kiệt, Huế, Thừa Thiên Huế', 12),
(N'Vũ Thị Phương',     '1998-12-12', N'0967890123', 0, N'001198121201', N'34 Nguyễn Trãi, Hà Đông, Hà Nội', 13),
(N'Đỗ Văn Hải',        '2001-04-20', N'0945678901', 1, N'001201042001', N'90 Lê Lợi, Quận 1, TP.HCM', 14),
(N'Ngô Thị Kim',       '1999-09-09', N'0923456780', 0, N'001199090901', N'23 Bạch Đằng, Hải Châu, Đà Nẵng', 15),
(N'Bùi Văn Long',      '1985-03-30', N'0888123456', 1, N'001085033001', N'67 Điện Biên Phủ, Ba Đình, Hà Nội', 16),
(N'Nguyễn Thị Hoa',    '1997-05-14', N'0911004801', 0, N'001197051401', N'18 Hoàng Hoa Thám, Ba Đình, Hà Nội', 17),
(N'Trần Văn Khoa',     '1996-09-03', N'0911004901', 1, N'001196090301', N'72 Cầu Giấy, Cầu Giấy, Hà Nội', 18),
(N'Lê Văn Cảnh',       '1987-02-18', N'0911005001', 1, N'001087021801', N'Cục CSGT, Hà Nội', 19);
GO

-- ============================================
-- 4. LOẠI TÀI LIỆU HỒ SƠ + TÀI LIỆU
-- ============================================
INSERT INTO DocumentType ([Type]) VALUES
(N'Ảnh chân dung 3x4'),
(N'Căn cước công dân (mặt trước)'),
(N'Căn cước công dân (mặt sau)'),
(N'Giấy khám sức khỏe'),
(N'Hồ sơ khác'),
(N'Giấy chứng nhận tốt nghiệp'),
(N'Giấy phép lái xe hiện có');
GO

INSERT INTO Document (DocumentTypeId, DocumentUrl, Notes, ProfileId) VALUES
((SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Căn cước công dân (mặt trước)'), N'/uploads/dossiers/8/cccd_mat_truoc.jpg', NULL, 8),
((SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Căn cước công dân (mặt sau)'),  N'/uploads/dossiers/8/cccd_mat_sau.jpg', NULL, 8),
((SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Giấy khám sức khỏe'),           N'/uploads/dossiers/8/giay_kham_suc_khoe.pdf', N'Đủ điều kiện sức khỏe lái xe', 8),
((SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Căn cước công dân (mặt trước)'), N'/uploads/dossiers/9/cccd_mat_truoc.jpg', NULL, 9),
((SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Giấy khám sức khỏe'),           N'/uploads/dossiers/9/giay_kham_suc_khoe.pdf', NULL, 9),
((SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Căn cước công dân (mặt trước)'), N'/uploads/dossiers/10/cccd_mat_truoc.jpg', NULL, 10),
((SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Giấy chứng nhận tốt nghiệp'),   N'/uploads/dossiers/11/bang_tot_nghiep.pdf', N'Hạng A1', 11),
((SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Căn cước công dân (mặt trước)'), N'/uploads/dossiers/11/cccd_mat_truoc.jpg', NULL, 11),
((SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Giấy phép lái xe hiện có'),     N'/uploads/dossiers/12/gplx_hang_a1.jpg', N'Nâng hạng từ A1 lên A', 12);
GO

-- ============================================
-- 5. HẠNG GPLX - chỉ A1, A, B1
-- ============================================
INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId) VALUES
(N'A1', N'Xe mô tô hai bánh có dung tích xi-lanh đến 125 cm3', 18, 0, NULL),
(N'A',  N'Xe mô tô hai bánh có dung tích xi-lanh trên 125 cm3', 18, 0, NULL),
(N'B1', N'Xe mô tô ba bánh', 18, 0, NULL);
GO

UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1') WHERE LicenceClass = N'A';
GO

-- ============================================
-- 6. HỒ SƠ ĐĂNG KÝ THI
-- ============================================
INSERT INTO ExamRegistration
    (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
VALUES
(N'Duyệt',       N'Đủ hồ sơ, đủ điều kiện sức khỏe', 8,  (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 0),
(N'Duyệt',       N'Đã xác minh căn cước và giấy khám sức khỏe', 9, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 0),
(N'Chờ duyệt',   N'Chờ cán bộ quản lý duyệt hồ sơ', 10, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), 0),
(N'Duyệt',       N'Đăng ký thi cấp mới hạng A1', 11, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), 0),
(N'Duyệt',       N'Đăng ký thi lại; chờ CSGT quyết định nội dung thi', 12, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'), 1),
(N'Duyệt',       N'Đăng ký thi cấp mới hạng B1 (mô tô ba bánh)', 13, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 0),
(N'Duyệt',       N'Hồ sơ hoàn chỉnh', 14, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 0),
(N'Chờ duyệt',   N'Chờ bổ sung ảnh chân dung', 15, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), 0),
(N'Loại',        N'Không đủ điều kiện sức khỏe theo quy định', 16, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 0),
(N'Duyệt',       N'Đã thu học phí và lệ phí thi', 17, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 0),
(N'Loại',        N'Cần bổ sung giấy xác nhận cư trú', 18, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 0);
GO

-- ============================================
-- 9. KHU VỰC / PHÒNG / SÂN THI
-- ============================================
INSERT INTO ExamZone (ZoneName, [Location], IsActive) VALUES
(N'Khu nhà điều hành',           N'Tòa A – Trung tâm Sát hạch Lái Vui, Hà Nội', 1),
(N'Khu sân thi mô tô',           N'Khu sân thực hành số 1 – Trung tâm Sát hạch Lái Vui', 1),
(N'Khu sân thi mô tô ba bánh',   N'Khu sân thực hành số 2 – Trung tâm Sát hạch Lái Vui', 1);
GO

INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location], ExamZoneId) VALUES
(N'Phòng thủ tục 102',       N'Phòng thủ tục', 30, N'Tầng 1, Tòa A', (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu nhà điều hành')),
(N'Phòng thi LT 1',          N'Phòng thi',     30, N'Tầng 2, Tòa B', (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu nhà điều hành')),
(N'Phòng thi LT 2',          N'Phòng thi',     30, N'Tầng 2, Tòa B', (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu nhà điều hành')),
(N'Sân TH 1',                N'Sân thi',       20, N'Sân số 1',       (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu sân thi mô tô')),
(N'Sân TH 2',                N'Sân thi',       12, N'Sân số 2',       (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu sân thi mô tô ba bánh'));
GO

-- ============================================
-- 11. THIẾT BỊ THI
-- ============================================
INSERT INTO ExamDevice (DeviceName, DeviceType, IsActive, ExamAreaId) VALUES
(N'MT1',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT2',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT3',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT4',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT5',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT6',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT7',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT8',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT9',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT10', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT1',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT2',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT3',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT4',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT5',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT6',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT7',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT8',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT9',  N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT10', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'XM-A1-01', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân TH 1')),
(N'XM-A1-02', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân TH 1')),
(N'XM-A1-03', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân TH 1')),
(N'XM-A1-DP', N'Mô tô', 0, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân TH 1')),
(N'XM3-B1-01', N'Mô tô ba bánh', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân TH 2')),
(N'XM3-B1-02', N'Mô tô ba bánh', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân TH 2')),
(N'XM3-B1-03', N'Mô tô ba bánh', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân TH 2')),
(N'XM3-B1-DP', N'Mô tô ba bánh', 0, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân TH 2'));
GO

-- ============================================
-- 12. DANH MỤC PHÍ + BIỂU PHÍ THEO HẠNG
-- ============================================
INSERT INTO Fee (FeeName, FeeType, IsActive) VALUES
(N'Học phí lý thuyết',                    N'Học phí',       1),
(N'Học phí thực hành',                    N'Học phí',       1),
(N'Lệ phí thi lý thuyết',                N'Lệ phí thi',    1),
(N'Lệ phí thi thực hành trong hình',     N'Lệ phí thi',    1),
(N'Lệ phí cấp GPLX (phôi PET)',           N'Phí cấp bằng',  1),
(N'Phí xét hồ sơ và in ấn biểu mẫu',      N'Phí hành chính', 1),
(N'Phí dịch vụ hỗ trợ đăng ký trực tuyến', N'Phí hành chính', 1);
GO

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
    (N'Học phí thực hành', 2000000.00),
    (N'Lệ phí thi lý thuyết', 100000.00),
    (N'Lệ phí thi thực hành trong hình', 350000.00),
    (N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
) v(FeeName, Amount)
JOIN Fee f ON f.FeeName = v.FeeName
WHERE l.LicenceClass = N'B1';
GO

-- ============================================
-- 19. LỖI TRỪ ĐIỂM (gắn section thực hành của 1 kỳ đại diện / hạng)
-- ============================================
INSERT INTO ScoreDeduction ([Reason], Points, IsCritical, LicenceId, ExamSectionId)
SELECT v.Reason, v.Points, v.IsCritical, l.LicenceId, es.ExamSectionId
FROM (VALUES
    (N'A1', N'Bánh xe đè vào vạch cản của hình sát hạch', 5.00, 0),
    (N'A1', N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0),
    (N'A1', N'Chạm chân xuống đất trong quá trình sát hạch', 5.00, 0),
    (N'A1', N'Hai bánh xe của xe sát hạch ra ngoài hình sát hạch', 100.00, 1),
    (N'A1', N'Không hoàn thành bài sát hạch', 100.00, 1),
    (N'A1', N'Thời gian thực hiện các bài sát hạch quá 10 phút, trừ quá 01 phút', 5.00, 0),
    (N'A1', N'Xe bị chết máy', 5.00, 0),
    (N'A1', N'Xe sát hạch bị đổ trong quá trình sát hạch', 100.00, 1),
    (N'A1', N'Đi không đúng trình tự bài sát hạch', 100.00, 1),
    (N'A',  N'Bánh xe đè vào vạch cản của hình sát hạch', 5.00, 0),
    (N'A',  N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0),
    (N'A',  N'Chạm chân xuống đất trong quá trình sát hạch', 5.00, 0),
    (N'A',  N'Hai bánh xe của xe sát hạch ra ngoài hình sát hạch', 100.00, 1),
    (N'A',  N'Không hoàn thành bài sát hạch', 100.00, 1),
    (N'A',  N'Thời gian thực hiện các bài sát hạch quá 10 phút, trừ quá 01 phút', 5.00, 0),
    (N'A',  N'Xe bị chết máy', 5.00, 0),
    (N'A',  N'Xe sát hạch bị đổ trong quá trình sát hạch', 100.00, 1),
    (N'A',  N'Đi không đúng trình tự bài sát hạch', 100.00, 1),
    (N'B1', N'Bánh xe đè vào vạch cản của hình sát hạch', 5.00, 0),
    (N'B1', N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0),
    (N'B1', N'Chạm chân xuống đất trong quá trình sát hạch', 5.00, 0),
    (N'B1', N'Hai bánh xe của xe sát hạch ra ngoài hình sát hạch', 100.00, 1),
    (N'B1', N'Không hoàn thành bài sát hạch', 100.00, 1),
    (N'B1', N'Thời gian thực hiện các bài sát hạch quá 10 phút, trừ quá 01 phút', 5.00, 0),
    (N'B1', N'Xe bị chết máy', 5.00, 0),
    (N'B1', N'Xe sát hạch bị đổ trong quá trình sát hạch', 100.00, 1),
    (N'B1', N'Đi không đúng trình tự bài sát hạch', 100.00, 1)
) v(LicenceClass, Reason, Points, IsCritical)
JOIN Licence l ON l.LicenceClass = v.LicenceClass
JOIN Exam e ON e.LicenceId = l.LicenceId AND e.ExamCode = CASE v.LicenceClass
    WHEN N'A1' THEN N'A1-20260601-1000'
    WHEN N'A'  THEN N'A-20260610'
    ELSE N'B1-20260601-0730'
END
JOIN ExamSection es ON es.ExamId = e.ExamId AND es.SectionType = N'Thực hành trong hình';
GO

-- ============================================
-- 21–22. NGÂN HÀNG CÂU HỎI + LICENCE_QUESTION
-- ============================================
INSERT INTO QuestionCategory (CategoryName, Description) VALUES
(N'Chương I', N'Quy định chung và quy tắc giao thông đường bộ'),
(N'Chương II', N'Văn hóa giao thông, đạo đức người lái xe, kỹ năng PCCC và cứu nạn'),
(N'Chương III', N'Kỹ thuật lái xe'),
(N'Chương IV', N'Cấu tạo và sửa chữa thông thường'),
(N'Chương V', N'Báo hiệu đường bộ'),
(N'Chương VI', N'Giải thế sa hình và xử lý tình huống giao thông');
GO

-- ============================================
-- 26. NGÂN HÀNG 600 CÂU HỎI
-- ============================================

INSERT INTO Question (QuestionNumber, QuestionCategoryId, ImageUrl, CorrectAnswer, IsCritical) VALUES
(1, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/001_pb4uxc.png', 'A', 0),
(2, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/002_xfqch7.png', 'B', 0),
(3, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127983/003_f2kpqz.png', 'C', 0),
(4, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/004_ype2gx.png', 'A', 0),
(5, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/005_pnn5lk.png', 'B', 0),
(6, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/006_s70rei.png', 'D', 0),
(7, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127983/007_whxzz0.png', 'A', 0),
(8, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/008_mxsrqj.png', 'C', 0),
(9, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/009_abuu5g.png', 'B', 0),
(10, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/010_uyxezy.png', 'A', 0),
(11, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/011_ixho2u.png', 'C', 0),
(12, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127984/012_q8eac6.png', 'D', 0),
(13, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127985/013_m9ukph.png', 'A', 0),
(14, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127986/014_gr7xtc.png', 'B', 0),
(15, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127987/015_yd6vvp.png', 'C', 0),
(16, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127987/016_fhnvpg.png', 'A', 0),
(17, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127987/017_ed6f9x.png', 'B', 0),
(18, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127988/018_pnkk0a.png', 'D', 0),
(19, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127987/019_vi5sbd.png', 'C', 1),
(20, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127988/020_qowgmo.png', 'A', 1),
(21, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127988/021_wu5ldu.png', 'B', 1),
(22, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127988/022_cqaxks.png', 'D', 1),
(23, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127989/023_faqfex.png', 'C', 1),
(24, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127989/024_r2of7f.png', 'A', 1),
(25, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127990/025_u8hekl.png', 'B', 1),
(26, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127990/026_hjqqi6.png', 'D', 1),
(27, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127991/027_uoiwz5.png', 'A', 1),
(28, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127991/028_plm6ha.png', 'C', 1),
(29, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127991/029_du9wza.png', 'B', 0),
(30, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127992/030_ham25h.png', 'A', 1),
(31, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127992/031_m6czby.png', 'C', 0),
(32, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127992/032_t9kpuy.png', 'D', 1),
(33, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127992/033_nmi4s9.png', 'A', 0),
(34, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127993/034_guyjgw.png', 'B', 1),
(35, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127995/035_zgtght.png', 'C', 1),
(36, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127995/036_dcy6um.png', 'A', 0),
(37, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127995/037_tltbs7.png', 'B', 0),
(38, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127994/038_cxq1kr.png', 'D', 0),
(39, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127995/039_wyu86d.png', 'C', 0),
(40, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127996/040_kbe9gh.png', 'A', 0),
(41, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127995/041_oj5qmn.png', 'B', 0),
(42, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127997/042_wpamoj.png', 'C', 0),
(43, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127996/043_v7kqsh.png', 'A', 0),
(44, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127997/044_wls1rp.png', 'D', 0),
(45, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127997/045_axiuat.png', 'B', 0),
(46, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127998/046_bqwe6i.png', 'C', 0),
(47, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127998/047_slk3et.png', 'A', 1),
(48, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127998/048_mlbrlo.png', 'D', 1),
(49, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127999/049_wyruhz.png', 'B', 0),
(50, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127999/050_spneqa.png', 'C', 0),
(51, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128000/051_qdraol.png', 'A', 0),
(52, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128000/052_lwmzvd.png', 'D', 1),
(53, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128000/053_eushhv.png', 'B', 1),
(54, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128001/054_fhh0s6.png', 'C', 0),
(55, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128001/055_kyiuuw.png', 'A', 1),
(56, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128001/056_xgoprj.png', 'D', 0),
(57, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128002/057_fttibl.png', 'B', 0),
(58, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128002/058_g7j5xb.png', 'C', 1),
(59, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128003/059_iucavh.png', 'A', 0),
(60, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128029/060_n1c9oc.png', 'B', 0),
(61, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128061/061_wkqrmm.png', 'D', 0),
(62, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128062/062_kmnyyi.png', 'C', 0),
(63, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128062/063_owf6sw.png', 'A', 1),
(64, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128063/064_b5bayy.png', 'B', 1),
(65, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128063/065_x1vtaf.png', 'D', 1),
(66, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128063/066_z6uezi.png', 'C', 1),
(67, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128063/067_skxb5w.png', 'A', 1),
(68, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128064/068_u849de.png', 'B', 1),
(69, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128064/069_gcourd.png', 'D', 0),
(70, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128065/070_g48rdu.png', 'C', 1),
(71, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128065/071_nxghcs.png', 'A', 1),
(72, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128065/072_fbxr7u.png', 'B', 1),
(73, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128066/073_acsspf.png', 'C', 1),
(74, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128066/074_hilzhh.png', 'D', 1),
(75, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128067/075_jfj2h3.png', 'A', 0),
(76, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128067/076_gmiejc.png', 'B', 0),
(77, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128067/077_mxyztg.png', 'C', 0),
(78, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128068/078_mrzuw4.png', 'D', 0),
(79, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128068/079_zr1lit.png', 'A', 0),
(80, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128068/080_wqcmhb.png', 'B', 0),
(81, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128069/081_guzmoj.png', 'C', 0),
(82, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128069/082_niflaq.png', 'D', 0),
(83, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128070/083_v44mwh.png', 'A', 0),
(84, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128070/084_vlndg1.png', 'B', 0),
(85, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128070/085_fmf5bn.png', 'C', 1),
(86, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128071/086_ito2bd.png', 'D', 1),
(87, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128071/087_bss4kv.png', 'A', 1),
(88, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128072/088_gckzmf.png', 'B', 1),
(89, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128072/089_evsq87.png', 'C', 1),
(90, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128072/090_fr5jwd.png', 'D', 1),
(91, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128073/091_zo0yxq.png', 'A', 1),
(92, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128073/092_ccx4fx.png', 'B', 1),
(93, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128074/093_fg12sa.png', 'C', 1),
(94, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128075/094_sseojo.png', 'D', 0),
(95, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128075/095_xozpst.png', 'A', 0),
(96, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128075/096_ixo3b4.png', 'B', 0),
(97, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128075/097_p2xwxi.png', 'C', 1),
(98, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128076/098_llubtp.png', 'D', 1),
(99, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128077/099_ffijlu.png', 'A', 0),
(100, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128094/100_fsv2rt.png', 'B', 0),
(101, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128197/101_ykojxc.png', 'C', 0),
(102, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128197/102_fqevzv.png', 'D', 1),
(103, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128198/103_lpreax.png', 'A', 0),
(104, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128198/104_wu041c.png', 'B', 0),
(105, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128199/105_oalsco.png', 'C', 0),
(106, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128199/106_ppcr45.png', 'D', 0),
(107, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128199/107_wzbo9r.png', 'A', 0),
(108, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128199/108_umauqz.png', 'B', 0),
(109, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128211/109_im8pml.png', 'C', 0),
(110, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128977/110_dggyve.png', 'D', 0),
(111, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129465/111_tgvosq.png', 'A', 0),
(112, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129465/112_ovpqwz.png', 'B', 0),
(113, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129466/113_ylad5a.png', 'C', 0),
(114, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129466/114_bwrpii.png', 'D', 0),
(115, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129466/115_o44smc.png', 'A', 0),
(116, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129466/116_gj5dvx.png', 'B', 0),
(117, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129467/117_sq4az7.png', 'C', 1),
(118, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129467/118_gfxerc.png', 'D', 0),
(119, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129467/119_kt81yk.png', 'A', 0),
(120, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129468/120_dwtx2x.png', 'B', 0),
(121, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129468/121_ezwdwa.png', 'C', 0),
(122, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129469/122_or2yjc.png', 'D', 0),
(123, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129469/123_g63clo.png', 'A', 0),
(124, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129469/124_vcbfcj.png', 'B', 0),
(125, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129470/125_ltutyc.png', 'C', 0),
(126, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129470/126_fhwjf3.png', 'D', 0),
(127, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129470/127_c129oc.png', 'A', 0),
(128, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129472/128_vl7ljb.png', 'B', 0),
(129, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129472/129_fs1zbl.png', 'C', 0),
(130, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129473/130_o04wkd.png', 'D', 0),
(131, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129473/131_abboso.png', 'A', 0),
(132, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129473/132_po1eaz.png', 'B', 0),
(133, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129473/133_h7dnvu.png', 'C', 0),
(134, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129473/134_bdpmng.png', 'D', 0),
(135, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129473/135_bsw5io.png', 'A', 0),
(136, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129474/136_ngz48g.png', 'B', 0),
(137, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129474/137_wtzgda.png', 'C', 0),
(138, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129475/138_z1ufap.png', 'D', 0),
(139, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129476/139_gbbac4.png', 'A', 0),
(140, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129475/140_v7dmrt.png', 'B', 0),
(141, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129477/141_k36qx0.png', 'C', 0),
(142, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129476/142_bnpxk2.png', 'D', 0),
(143, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129476/143_imq1pu.png', 'A', 0),
(144, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129477/144_wqti3t.png', 'B', 0),
(145, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129477/145_b375it.png', 'C', 0),
(146, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129478/146_rrgbuo.png', 'D', 0),
(147, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129478/147_xpt91l.png', 'A', 0),
(148, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129478/148_xhomxs.png', 'B', 0),
(149, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129479/149_avkgz9.png', 'C', 0),
(150, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129480/150_cwz1qw.png', 'D', 0),
(151, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129482/151_a3m4ad.png', 'A', 0),
(152, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129480/152_kqkgn0.png', 'B', 0),
(153, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129481/153_agyman.png', 'C', 0),
(154, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129482/154_rjg1z7.png', 'D', 0),
(155, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129481/155_qlh4t8.png', 'A', 0),
(156, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129482/156_kj2akz.png', 'B', 0),
(157, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129482/157_qv3gwt.png', 'C', 0),
(158, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129482/158_npk7rw.png', 'D', 0),
(159, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129483/159_srh8zv.png', 'A', 0),
(160, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129484/160_c7dydd.png', 'B', 0),
(161, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129484/161_wgioez.png', 'C', 0),
(162, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129484/162_zv0l8s.png', 'D', 0),
(163, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129485/163_ts7zmn.png', 'A', 1),
(164, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129485/164_xd92hv.png', 'B', 0),
(165, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129486/165_us4j8c.png', 'C', 1),
(166, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129486/166_adkqpj.png', 'D', 0),
(167, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129486/167_dhuuxa.png', 'A', 1),
(168, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129486/168_d71r3l.png', 'B', 0),
(169, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129487/169_ih6yyw.png', 'C', 0),
(170, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129487/170_ivvrnq.png', 'D', 0),
(171, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129487/171_pb2d4w.png', 'A', 0),
(172, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129489/172_ajvpns.png', 'B', 0),
(173, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129488/173_ycplr2.png', 'C', 0),
(174, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129489/174_dshbia.png', 'D', 0),
(175, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129490/175_ielrnl.png', 'A', 0),
(176, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129490/176_ru92pb.png', 'B', 0),
(177, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129490/177_ck7kkz.png', 'C', 0),
(178, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129491/178_chxjbu.png', 'D', 0),
(179, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129491/179_o9rxrs.png', 'A', 0),
(180, 1, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129491/180_le5y4x.png', 'B', 0),
(181, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129492/181_d1iuft.png', 'C', 0),
(182, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129492/182_js0gly.png', 'D', 0),
(183, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129493/183_nv8b0d.png', 'A', 0),
(184, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129493/184_whax1i.png', 'B', 0),
(185, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129493/185_yfkynh.png', 'C', 0),
(186, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129493/186_mtposg.png', 'D', 0),
(187, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129493/187_osgklv.png', 'A', 0),
(188, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129494/188_wtcpgy.png', 'B', 0),
(189, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129494/189_wdbkt8.png', 'C', 0),
(190, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129495/190_f2ld1c.png', 'D', 0),
(191, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129496/191_vwx6m6.png', 'A', 0),
(192, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129496/192_weco1k.png', 'B', 0),
(193, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129496/193_csn4ks.png', 'C', 0),
(194, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129497/194_we6csi.png', 'D', 0),
(195, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129497/195_zgypge.png', 'A', 0),
(196, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129497/196_z2m09m.png', 'B', 0),
(197, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129497/197_jlsy1o.png', 'C', 1),
(198, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129498/198_uym25l.png', 'D', 1),
(199, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129498/199_nvwi9x.png', 'A', 0),
(200, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129499/200_d4gpdm.png', 'B', 0),
(201, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129499/201_uqv0ca.png', 'A', 0),
(202, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129500/202_cvwkxe.png', 'B', 0),
(203, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129500/203_gnnbly.png', 'C', 0),
(204, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129501/204_oschp8.png', 'D', 0),
(205, 2, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129501/205_gad7mq.png', 'A', 0),
(206, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129501/206_arf84o.png', 'B', 1),
(207, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129501/207_pz1nwf.png', 'C', 0),
(208, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129502/208_gwv69i.png', 'D', 0),
(209, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129503/209_knqyad.png', 'A', 0),
(210, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129503/210_nabkxk.png', 'B', 0),
(211, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129503/211_c8xgyx.png', 'C', 0),
(212, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129504/212_mvy8dn.png', 'D', 0),
(213, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129504/213_ykwhpt.png', 'A', 0),
(214, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129505/214_uxjq2x.png', 'B', 0),
(215, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129505/215_j2buxk.png', 'C', 1),
(216, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129505/216_gddzh6.png', 'D', 0),
(217, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129506/217_p2uwyz.png', 'A', 0),
(218, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129506/218_pph80c.png', 'B', 0),
(219, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129506/219_ygkyhz.png', 'C', 0),
(220, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129507/220_hmbrmh.png', 'D', 0),
(221, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129507/221_anmslw.png', 'A', 0),
(222, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129508/222_vjbgyi.png', 'B', 0),
(223, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129508/223_f07pbu.png', 'C', 0),
(224, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129509/224_pjjr5v.png', 'D', 0),
(225, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129509/225_t7ttqb.png', 'A', 0),
(226, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129509/226_blhaf1.png', 'B', 1),
(227, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129510/227_sicyjm.png', 'C', 0),
(228, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129511/228_efjozm.png', 'D', 0),
(229, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129511/229_vxhqn2.png', 'A', 0),
(230, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129511/230_g8anmp.png', 'B', 0),
(231, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129511/231_eayjxr.png', 'C', 0),
(232, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129511/232_yetdmn.png', 'D', 0),
(233, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129512/233_znkwce.png', 'A', 0),
(234, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129512/234_dxjmyf.png', 'B', 1),
(235, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129513/235_bwcbbm.png', 'C', 0),
(236, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129513/236_zt9t4b.png', 'D', 0),
(237, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129514/237_ng3rnz.png', 'A', 0),
(238, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129514/238_wophkw.png', 'B', 0),
(239, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129514/239_eym2sw.png', 'C', 0),
(240, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129515/240_dp1fea.png', 'D', 0),
(241, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129515/241_dwljna.png', 'A', 0),
(242, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129515/242_g1j29c.png', 'B', 0),
(243, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129516/243_brhmue.png', 'C', 0),
(244, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129517/244_jvws6k.png', 'D', 0),
(245, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129517/245_s0oiph.png', 'A', 1),
(246, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129517/246_bu41kl.png', 'B', 1),
(247, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129517/247_t9cacc.png', 'C', 0),
(248, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129518/248_zrupzh.png', 'D', 0),
(249, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129519/249_bl16ae.png', 'A', 0),
(250, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129518/250_oseknx.png', 'B', 0),
(251, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129519/251_cospbe.png', 'C', 0),
(252, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129519/252_ny4xb1.png', 'D', 1),
(253, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129520/253_n9k9cz.png', 'A', 1),
(254, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129520/254_nn4pun.png', 'B', 1),
(255, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129520/255_kz59ph.png', 'C', 1),
(256, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129521/256_jqd5cx.png', 'D', 0),
(257, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129521/257_c8ljqh.png', 'A', 0),
(258, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129522/258_uztxml.png', 'B', 0),
(259, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129522/259_obab1x.png', 'C', 0),
(260, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129557/260_mskp37.png', 'D', 1),
(261, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129557/261_qfzf5w.png', 'A', 0),
(262, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129558/262_uskyzm.png', 'B', 0),
(263, 3, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129559/263_k6urgf.png', 'C', 0),
(264, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129559/264_oijxrq.png', 'D', 0),
(265, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129560/265_fydz7e.png', 'A', 0),
(266, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129560/266_br9mnx.png', 'B', 0),
(267, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129560/267_k8tmgq.png', 'C', 0),
(268, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129561/268_gmaqmz.png', 'D', 0),
(269, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129561/269_mauydu.png', 'A', 0),
(270, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129562/270_xefsqo.png', 'B', 0),
(271, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129562/271_p7ohuj.png', 'C', 0),
(272, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129563/272_glgp2q.png', 'D', 0),
(273, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129563/273_karevb.png', 'A', 0),
(274, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129563/274_lruogz.png', 'B', 0),
(275, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129563/275_owhizr.png', 'C', 0),
(276, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129564/276_sxn8m9.png', 'D', 0),
(277, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129564/277_bvshbr.png', 'A', 0),
(278, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129565/278_xezt9z.png', 'B', 0),
(279, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129565/279_p2u2hv.png', 'C', 0),
(280, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129565/280_ibui8e.png', 'D', 0),
(281, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129566/281_h24r2r.png', 'A', 0),
(282, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129566/282_tt8txc.png', 'B', 0),
(283, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129567/283_kkvhek.png', 'C', 0),
(284, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129567/284_y1ccaj.png', 'D', 0),
(285, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129567/285_mj7zgq.png', 'A', 0),
(286, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129568/286_yshhcr.png', 'B', 0),
(287, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129569/287_p204w9.png', 'C', 0),
(288, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129569/288_jx4xz3.png', 'D', 0),
(289, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129575/289_igjb1p.png', 'A', 0),
(290, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129632/290_v8p2rr.png', 'B', 0),
(291, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129632/291_wxr9qr.png', 'C', 0),
(292, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129632/292_muqcgx.png', 'D', 0),
(293, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129632/293_g2uetk.png', 'A', 0),
(294, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129633/294_ccia8z.png', 'B', 0),
(295, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129633/295_ndn6x3.png', 'C', 0),
(296, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129634/296_gfrita.png', 'D', 0),
(297, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129634/297_x40ses.png', 'A', 0),
(298, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129635/298_ql2nnj.png', 'B', 0),
(299, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129636/299_nrfbws.png', 'C', 0),
(300, 4, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129635/300_ayxbet.png', 'D', 0),
(301, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129637/301_udhhqg.png', 'A', 0),
(302, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129638/302_re6rsk.png', 'B', 0),
(303, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129637/303_v3asog.png', 'C', 0),
(304, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129638/304_og747h.png', 'D', 0),
(305, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129638/305_rrp5z1.png', 'A', 0),
(306, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129640/306_rn8fie.png', 'B', 0),
(307, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129639/307_lnt0d6.png', 'C', 0),
(308, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129640/308_rirsnr.png', 'D', 0),
(309, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129641/309_hh2dr4.png', 'A', 0),
(310, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129641/310_mbo4o2.png', 'B', 0),
(311, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129640/311_mljaxg.png', 'C', 0),
(312, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129641/312_tfnj1l.png', 'D', 0),
(313, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129642/313_qzewn0.png', 'A', 0),
(314, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129642/314_lsb3zu.png', 'B', 0),
(315, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129642/315_llugra.png', 'C', 0),
(316, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129643/316_hbjru5.png', 'D', 0),
(317, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129643/317_qtruoe.png', 'A', 0),
(318, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129644/318_nga7ca.png', 'B', 0),
(319, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129645/319_ppij2x.png', 'C', 0),
(320, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129645/320_hyt2dw.png', 'D', 0),
(321, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129645/321_lthit8.png', 'A', 0),
(322, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129645/322_a1f2ia.png', 'B', 0),
(323, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129646/323_rytsos.png', 'C', 0),
(324, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129646/324_nek8qe.png', 'D', 0),
(325, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129647/325_ejqu4i.png', 'A', 0),
(326, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129647/326_razz73.png', 'B', 0),
(327, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129648/327_lyk0mt.png', 'C', 0),
(328, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129649/328_hxeomh.png', 'D', 0),
(329, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129648/329_zrktif.png', 'A', 0),
(330, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129649/330_wnl2im.png', 'B', 0),
(331, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129650/331_k30tbh.png', 'C', 0),
(332, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129650/332_qjfmib.png', 'D', 0),
(333, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129651/333_ytt2o0.png', 'A', 0),
(334, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129650/334_ku9x1g.png', 'B', 0),
(335, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129651/335_imodts.png', 'C', 0),
(336, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129652/336_a5zy2z.png', 'D', 0),
(337, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129652/337_lxxlqr.png', 'A', 0),
(338, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129653/338_t3rots.png', 'B', 0),
(339, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129653/339_drr5wo.png', 'C', 0),
(340, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129653/340_tlgxnw.png', 'D', 0),
(341, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129654/341_tbrovi.png', 'A', 0),
(342, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129655/342_cqjfhr.png', 'B', 0),
(343, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129656/343_vytzkw.png', 'C', 0),
(344, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129654/344_xmhpck.png', 'D', 0),
(345, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129655/345_lgtg4m.png', 'A', 0),
(346, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129655/346_gacy7p.png', 'B', 0),
(347, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129655/347_owtb7s.png', 'C', 0),
(348, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129656/348_if7yf9.png', 'D', 0),
(349, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129657/349_ihuuip.png', 'A', 0),
(350, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129657/350_orkjbe.png', 'B', 0),
(351, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129658/351_fnr9wu.png', 'C', 0),
(352, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129658/352_i8l4ds.png', 'D', 0),
(353, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129658/353_ulfznj.png', 'A', 0),
(354, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129658/354_bscsen.png', 'B', 0),
(355, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129659/355_hgrawd.png', 'C', 0),
(356, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129659/356_yyic8d.png', 'D', 0),
(357, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129659/357_gyhfm0.png', 'A', 0),
(358, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129660/358_tb6uye.png', 'B', 0),
(359, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129661/359_gmigxr.png', 'C', 0),
(360, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129661/360_wxqlek.png', 'D', 0),
(361, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129661/361_y0beq3.png', 'A', 0),
(362, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129662/362_emfcze.png', 'B', 0),
(363, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129662/363_eptskn.png', 'C', 0),
(364, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129662/364_l5zvlg.png', 'D', 0),
(365, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129663/365_f09pec.png', 'A', 0),
(366, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129663/366_qvdzgu.png', 'B', 0),
(367, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129664/367_gqwk0w.png', 'C', 0),
(368, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129665/368_hdlzkh.png', 'D', 0),
(369, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129666/369_b97yse.png', 'A', 0),
(370, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129666/370_jqk7xk.png', 'B', 0),
(371, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129666/371_znuoqu.png', 'C', 0),
(372, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129666/372_qejxlc.png', 'D', 0),
(373, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129666/373_rg6fis.png', 'A', 0),
(374, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129667/374_yp3mta.png', 'B', 0),
(375, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129668/375_pngxoz.png', 'C', 0),
(376, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129668/376_j0ddg8.png', 'D', 0),
(377, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129668/377_gtnuel.png', 'A', 0),
(378, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129669/378_giz8jv.png', 'B', 0),
(379, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129669/379_rgazrv.png', 'C', 0),
(380, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129670/380_b3kmfx.png', 'D', 0),
(381, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129670/381_a9uhrw.png', 'A', 0),
(382, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129670/382_sjbqfb.png', 'B', 0),
(383, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129671/383_xwpemh.png', 'C', 0),
(384, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129671/384_foxudi.png', 'D', 0),
(385, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129671/385_ap1lwt.png', 'A', 0),
(386, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129672/386_e1csbp.png', 'B', 0),
(387, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129672/387_pi3z0v.png', 'C', 0),
(388, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129674/388_x3nvwz.png', 'D', 0),
(389, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129673/389_c3eoag.png', 'A', 0),
(390, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129674/390_prwmdu.png', 'B', 0),
(391, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129674/391_livrbr.png', 'C', 0),
(392, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129674/392_wysgvl.png', 'D', 0),
(393, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129675/393_m5jspq.png', 'A', 0),
(394, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129675/394_b5exee.png', 'B', 0),
(395, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129676/395_pehaaa.png', 'C', 0),
(396, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129676/396_ncjddx.png', 'D', 0),
(397, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129677/397_tlxntv.png', 'A', 0),
(398, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129677/398_cc1qum.png', 'B', 0),
(399, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129678/399_xjhhts.png', 'C', 0),
(400, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129678/400_hsj1bo.png', 'D', 0),
(401, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129678/401_spw8lo.png', 'A', 0),
(402, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129679/402_ggg1gc.png', 'B', 0),
(403, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129679/403_rjleku.png', 'C', 0),
(404, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129680/404_y7tw9d.png', 'D', 0),
(405, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129680/405_hxep58.png', 'A', 0),
(406, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129680/406_d6bi0p.png', 'B', 0),
(407, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129681/407_qu81ki.png', 'C', 0),
(408, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129681/408_ugxwoj.png', 'D', 0),
(409, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129682/409_nvyxmd.png', 'A', 0),
(410, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129682/410_sirsxs.png', 'B', 0),
(411, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129682/411_um1nkh.png', 'C', 0),
(412, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129683/412_zrpnwp.png', 'D', 0),
(413, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129684/413_fgvagx.png', 'A', 0),
(414, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129684/414_ycomaf.png', 'B', 0),
(415, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129684/415_buwsjg.png', 'C', 0),
(416, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129684/416_vvx7at.png', 'D', 0),
(417, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129685/417_j0qwup.png', 'A', 0),
(418, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129685/418_fw5ppb.png', 'B', 0),
(419, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129685/419_lnn9jx.png', 'C', 0),
(420, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129686/420_hvzior.png', 'D', 0),
(421, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129686/421_ng9p9n.png', 'A', 0),
(422, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129687/422_eos2of.png', 'B', 0),
(423, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129687/423_e37o39.png', 'C', 0),
(424, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129688/424_fivmvt.png', 'D', 0),
(425, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129688/425_r7rjfw.png', 'A', 0),
(426, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129689/426_hoyljk.png', 'B', 0),
(427, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129690/427_j7okbu.png', 'C', 0),
(428, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129689/428_xyxzaj.png', 'D', 0),
(429, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129690/429_a9lqho.png', 'A', 0),
(430, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129690/430_mi1ayj.png', 'B', 0),
(431, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129690/431_hlxdc6.png', 'C', 0),
(432, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129691/432_yhu3pd.png', 'D', 0),
(433, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129691/433_k3m8nb.png', 'A', 0),
(434, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129692/434_fzziym.png', 'B', 0),
(435, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129693/435_cvqs3i.png', 'C', 0),
(436, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129693/436_twnwj0.png', 'D', 0),
(437, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129694/437_anowtz.png', 'A', 0),
(438, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129694/438_xowcoi.png', 'B', 0),
(439, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129695/439_hu1sec.png', 'C', 0),
(440, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129695/440_dgnmff.png', 'D', 0),
(441, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129695/441_qcgyfz.png', 'A', 0),
(442, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129696/442_c1dbp1.png', 'B', 0),
(443, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129695/443_jxjkis.png', 'C', 0),
(444, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129696/444_wtjbh8.png', 'D', 0),
(445, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129697/445_qakyti.png', 'A', 0),
(446, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129697/446_ik2o5d.png', 'B', 0),
(447, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129697/447_oxqas0.png', 'C', 0),
(448, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129698/448_ldx6x5.png', 'D', 0),
(449, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129698/449_o37k7p.png', 'A', 0),
(450, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129699/450_fe9luk.png', 'B', 0),
(451, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129699/451_plvhiy.png', 'C', 0),
(452, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129700/452_k26k9z.png', 'D', 0),
(453, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129699/453_hsjixm.png', 'A', 0),
(454, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129700/454_jcx6k9.png', 'B', 0),
(455, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129701/455_cco9x7.png', 'C', 0),
(456, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129702/456_zdxorq.png', 'D', 0),
(457, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129702/457_xuepzw.png', 'A', 0),
(458, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129702/458_f1sjon.png', 'B', 0),
(459, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129702/459_gcfagw.png', 'C', 0),
(460, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129718/460_gkerek.png', 'D', 0),
(461, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129721/461_wbhyzj.png', 'A', 0),
(462, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129720/462_ztp97d.png', 'B', 0),
(463, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129721/463_ab3pq3.png', 'C', 0),
(464, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129722/464_ac2f8o.png', 'D', 0),
(465, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129722/465_khvu4y.png', 'A', 0),
(466, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129723/466_r0yrnw.png', 'B', 0),
(467, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129723/467_mharlp.png', 'C', 0),
(468, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129724/468_uieza2.png', 'D', 0),
(469, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129724/469_gudlqv.png', 'A', 0),
(470, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129725/470_dz0srq.png', 'B', 0),
(471, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129725/471_hwjhic.png', 'C', 0),
(472, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129726/472_gpy8lb.png', 'D', 0),
(473, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129726/473_mumdr1.png', 'A', 0),
(474, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129726/474_suvcbn.png', 'B', 0),
(475, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129726/475_ekiwcv.png', 'C', 0),
(476, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129727/476_npwkwb.png', 'D', 0),
(477, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129727/477_m3xdfk.png', 'A', 0),
(478, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129728/478_bjknat.png', 'B', 0),
(479, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129728/479_cmyxnd.png', 'C', 0),
(480, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129729/480_vsq0w5.png', 'D', 0),
(481, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129729/481_dwt8da.png', 'A', 0),
(482, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129730/482_hdkxyc.png', 'B', 0),
(483, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129731/483_ufbzb9.png', 'C', 0),
(484, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129731/484_yketkd.png', 'D', 0),
(485, 5, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129732/485_hdv7xo.png', 'A', 0),
(486, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129734/486_wkgued.png', 'B', 0),
(487, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129734/487_wwowcp.png', 'C', 0),
(488, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129735/488_icexaj.png', 'D', 0),
(489, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129735/489_udvmbj.png', 'A', 0),
(490, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129735/490_gmhim1.png', 'B', 0),
(491, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129736/491_uuf5or.png', 'C', 0),
(492, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129737/492_rysq8i.png', 'D', 0),
(493, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129738/493_pcmrkl.png', 'A', 0),
(494, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129737/494_sxfxke.png', 'B', 0),
(495, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129739/495_sm9a5f.png', 'C', 0),
(496, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129739/496_yjkwhi.png', 'D', 0),
(497, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129739/497_gdeen3.png', 'A', 0),
(498, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129740/498_lahpye.png', 'B', 0),
(499, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129741/499_hvzojm.png', 'C', 0),
(500, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129741/500_qbh2fv.png', 'D', 0),
(501, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129741/501_sm3qmb.png', 'A', 0),
(502, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129744/502_ye8ken.png', 'B', 0),
(503, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129744/503_gkf3rn.png', 'C', 0),
(504, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129743/504_qvm7yb.png', 'D', 0),
(505, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129745/505_qf007m.png', 'A', 0),
(506, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129744/506_lrq5km.png', 'B', 0),
(507, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129746/507_kofg00.png', 'C', 0),
(508, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129745/508_gjvw7m.png', 'D', 0),
(509, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129746/509_hsjl77.png', 'A', 0),
(510, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129747/510_hd3gjl.png', 'B', 0),
(511, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129748/511_cs0agz.png', 'C', 0),
(512, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129747/512_ke5e6q.png', 'D', 0),
(513, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129748/513_xfbx3n.png', 'A', 0),
(514, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129748/514_wvigvu.png', 'B', 0),
(515, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129749/515_linbjn.png', 'C', 0),
(516, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129750/516_jokw46.png', 'D', 0),
(517, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129751/517_kbc4qk.png', 'A', 0),
(518, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129751/518_i8xqzn.png', 'B', 0),
(519, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129751/519_bh8o81.png', 'C', 0),
(520, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129751/520_ftdvwf.png', 'D', 0),
(521, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129751/521_ey2lwj.png', 'A', 0),
(522, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129754/522_fdmkpv.png', 'B', 0),
(523, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129754/523_rtdqlz.png', 'C', 0),
(524, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129754/524_frtax4.png', 'D', 0),
(525, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129755/525_awgwxt.png', 'A', 0),
(526, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129754/526_n3fwq9.png', 'B', 0),
(527, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129754/527_bg4w84.png', 'C', 0),
(528, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129757/528_scifjt.png', 'D', 0),
(529, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129757/529_zhigwl.png', 'A', 0),
(530, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129758/530_hindnc.png', 'B', 0),
(531, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129759/531_jnmzej.png', 'C', 0),
(532, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129758/532_srmhy6.png', 'D', 0),
(533, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129759/533_ulwabd.png', 'A', 0),
(534, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129760/534_peknol.png', 'B', 0),
(535, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129760/535_o2txwn.png', 'C', 0),
(536, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129762/536_op1sra.png', 'D', 0),
(537, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129761/537_vskann.png', 'A', 0),
(538, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129762/538_ainacx.png', 'B', 0),
(539, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129763/539_fwfcn5.png', 'C', 0),
(540, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129764/540_u9xtnk.png', 'D', 0),
(541, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129763/541_a0lohy.png', 'A', 0),
(542, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129763/542_rbdeg9.png', 'B', 0),
(543, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129765/543_s0rlug.png', 'C', 0),
(544, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129766/544_gnixpe.png', 'D', 0),
(545, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129769/545_rjwor4.png', 'A', 0),
(546, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129767/546_ma7qzs.png', 'B', 0),
(547, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129767/547_rtlepl.png', 'C', 0),
(548, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129766/548_gc6wbs.png', 'D', 0),
(549, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129771/549_vi9s0k.png', 'A', 0),
(550, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129769/550_suu6re.png', 'B', 0),
(551, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129770/551_qrpw2b.png', 'C', 0),
(552, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129769/552_uwjvdw.png', 'D', 0),
(553, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129769/553_z8qwy5.png', 'A', 0),
(554, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129773/554_psfcei.png', 'B', 0),
(555, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129772/555_trkgly.png', 'C', 0),
(556, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129773/556_dr2f2s.png', 'D', 0),
(557, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129774/557_b7nifc.png', 'A', 0),
(558, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129775/558_bkiwg1.png', 'B', 0),
(559, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129775/559_y7z4kb.png', 'C', 0),
(560, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129775/560_i7imkd.png', 'D', 0),
(561, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129776/561_vmzrma.png', 'A', 0),
(562, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129777/562_njglo1.png', 'B', 0),
(563, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129777/563_upqscb.png', 'C', 0),
(564, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129778/564_o3wzhq.png', 'D', 0),
(565, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129778/565_vupjq5.png', 'A', 0),
(566, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129778/566_i8escc.png', 'B', 0),
(567, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129779/567_aovbg6.png', 'C', 0),
(568, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129780/568_m3z5mp.png', 'D', 0),
(569, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129781/569_fkrfrn.png', 'A', 0),
(570, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129783/570_ngppvm.png', 'B', 0),
(571, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129782/571_e3dtwb.png', 'C', 0),
(572, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129782/572_eygmfj.png', 'D', 0),
(573, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129783/573_zkgwi9.png', 'A', 0),
(574, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129784/574_azmbgv.png', 'B', 0),
(575, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129785/575_el2pzq.png', 'C', 0),
(576, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129786/576_l0ov81.png', 'D', 0),
(577, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129788/577_bzrkm8.png', 'A', 0),
(578, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129788/578_iieoxu.png', 'B', 0),
(579, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129787/579_gmdk85.png', 'C', 0),
(580, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129788/580_mz6huy.png', 'D', 0),
(581, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129788/581_ahzzg1.png', 'A', 0),
(582, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129790/582_o774qq.png', 'B', 0),
(583, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129792/583_cg1h2l.png', 'C', 0),
(584, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129792/584_q9svjy.png', 'D', 0),
(585, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129794/585_alk5bi.png', 'A', 0),
(586, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129792/586_by45sp.png', 'B', 0),
(587, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129793/587_ivcdgz.png', 'C', 0),
(588, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129794/588_oqfkce.png', 'D', 0),
(589, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129796/589_yqtmyk.png', 'A', 0),
(590, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129795/590_e60bxp.png', 'B', 0),
(591, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129797/591_bmzstk.png', 'C', 0),
(592, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129797/592_tqzqme.png', 'D', 0),
(593, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129797/593_uzxnfm.png', 'A', 0),
(594, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129798/594_ctzhwd.png', 'B', 0),
(595, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129798/595_odqobt.png', 'C', 0),
(596, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129800/596_zbbv8d.png', 'D', 0),
(597, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129801/597_mja4fm.png', 'A', 0),
(598, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129801/598_rgvqlz.png', 'B', 0),
(599, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129801/599_hlhngm.png', 'C', 0),
(600, 6, 'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780129801/600_w6w1am.png', 'D', 0);

-- ============================================
-- 27. ÁNH XẠ CÂU HỎI THEO HẠNG GPLX
-- ============================================

-- A1 and A
INSERT INTO Licence_Question (LicenceId, QuestionId)
SELECT l.LicenceId, q.QuestionId
FROM Question q
CROSS JOIN Licence l
WHERE l.LicenceClass IN (N'A1', N'A')
  AND q.QuestionNumber IN (
    1,2,3,4,5,6,7,8,9,10,
    11,12,13,19,20,21,22,24,26,27,
    28,29,30,31,32,33,34,35,36,37,
    38,39,40,41,43,44,45,46,47,48,
    49,51,52,53,54,56,57,59,63,64,
    65,66,67,68,69,70,71,72,73,74,
    75,76,77,80,81,87,88,90,91,92,
    93,94,96,97,98,99,100,102,103,107,
    109,110,111,119,123,124,125,126,137,138,
    140,141,142,145,146,151,155,163,167,178,
    182,185,187,189,191,192,193,194,195,200,
    206,215,219,232,233,240,241,242,254,255,
    257,258,259,260,261,
    303,304,305,306,307,313,314,315,317,318,
    322,323,324,325,326,329,330,335,345,346,
    347,348,349,350,351,354,360,362,364,366,
    367,368,369,370,371,372,373,374,375,376,
    377,380,381,382,386,387,389,390,391,393,
    394,395,397,398,400,401,411,412,413,415,
    419,422,427,430,431,432,433,434,435,437,
    438,439,440,441,442,445,450,451,452,454,
    455,457,458,459,460,461,474,475,476,478,
    486,487,490,492,495,499,500,503,504,505,
    507,508,509,517,520,525,527,528,529,538,
    539,540,543,548,553,556,559,560,562,565,
    567,568,583,592,600
);

-- B1
INSERT INTO Licence_Question (LicenceId, QuestionId)
SELECT l.LicenceId, q.QuestionId
FROM Question q
CROSS JOIN Licence l
WHERE l.LicenceClass = N'B1'
  AND q.QuestionNumber IN (
    1,2,3,4,5,6,7,8,9,10,
    11,12,13,19,20,21,22,24,26,27,
    28,29,30,31,32,33,34,35,36,37,
    38,39,40,41,43,44,45,46,47,48,
    49,51,52,53,54,55,56,57,59,63,
    64,65,66,67,68,69,70,71,72,73,
    74,75,76,77,78,80,81,82,87,88,
    89,90,91,92,93,94,96,97,98,99,
    100,102,103,107,108,109,110,111,119,123,
    124,125,126,137,138,139,140,141,142,145,
    146,151,155,157,162,163,165,166,167,178,
    182,185,187,189,191,192,193,194,195,200,
    206,215,219,232,233,240,241,242,254,255,
    257,258,259,260,261,266,285,
    303,304,305,306,307,313,314,315,317,318,
    322,323,324,325,326,329,330,332,333,334,
    335,344,345,346,347,348,349,350,351,354,
    355,360,361,362,364,366,367,368,369,370,
    371,372,373,374,375,376,377,380,381,382,
    383,384,385,386,387,388,389,390,391,392,
    393,394,395,396,397,398,400,401,402,405,
    406,407,408,409,410,411,412,413,415,416,
    418,419,420,421,422,423,424,425,426,427,
    430,431,432,433,434,435,436,437,438,439,
    440,441,442,443,445,446,450,451,452,454,
    455,456,457,458,459,460,461,474,475,476,
    477,478,479,480,481,482,483,485,
    486,487,490,492,495,499,500,503,504,505,
    507,508,509,517,520,525,527,528,529,538,
    539,540,543,548,553,556,559,560,562,565,
    567,568,583,592,600
);
GO

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
WHERE secLayout.SectionType = N'Thực hành trong hình'
  AND c.TakeTheory = 1
  AND ISNULL(eesTheory.Status, N'Chưa thi') <> N'Đã thi';
GO


USE DLEM_DB_2;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @PasswordHash NVARCHAR(255) =
        N'$2a$10$E8ocGIv4gRp6xZurl5egNuxir.0zn/5BUJMO5kIjdz38csrH3s7Cm';
    DECLARE @RegistrantRoleId INT =
        (SELECT RoleId FROM [Role] WHERE RoleName = N'Người đăng ký thi');
    DECLARE @A1LicenceId INT =
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1');
    DECLARE @ALicenceId INT =
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A');

    IF @RegistrantRoleId IS NULL OR @A1LicenceId IS NULL OR @ALicenceId IS NULL
        THROW 51000, N'Thiếu Role Người đăng ký thi hoặc Licence A1/A. Hãy chạy DML_DLEM_DB.sql trước.', 1;

    IF NOT EXISTS (SELECT 1 FROM [User] u JOIN [Role] r ON r.RoleId = u.RoleId
                   WHERE r.RoleName = N'Sát hạch viên' AND u.IsActive = 1)
        THROW 51001, N'Không có sát hạch viên hoạt động. Hãy chạy DML_DLEM_DB.sql trước.', 1;

    DECLARE @RequiredDocumentTypes TABLE (
        UiType NVARCHAR(50) NOT NULL,
        DbType NVARCHAR(100) NOT NULL,
        DocumentUrl NVARCHAR(500) NOT NULL
    );

    INSERT INTO @RequiredDocumentTypes (UiType, DbType, DocumentUrl) VALUES
    (N'Portrait', N'Ảnh chân dung 3x4',
     N'https://res.cloudinary.com/dv0xxzkyy/image/upload/v1785210082/8733aafb-5242-43c6-8784-7c8d35ef12b1_iugaii.jpg'),
    (N'IdFront', N'Căn cước công dân (mặt trước)',
     N'https://res.cloudinary.com/dv0xxzkyy/image/upload/v1785210082/254c917d-17c3-459c-ba5d-9761fbd43330_qpwbzm.jpg'),
    (N'IdBack', N'Căn cước công dân (mặt sau)',
     N'https://res.cloudinary.com/dv0xxzkyy/image/upload/v1785210082/d4c5e5c9-4db7-4a5d-8263-78a58a7afaac_ujcgla.jpg'),
    (N'HealthCertificate', N'Giấy khám sức khỏe',
     N'https://res.cloudinary.com/dv0xxzkyy/image/upload/v1785210082/cf6bcd14-1cc3-40d1-9557-0ee7210fa204_cevlwr.jpg');

    IF EXISTS (
        SELECT 1
        FROM @RequiredDocumentTypes r
        WHERE NOT EXISTS (SELECT 1 FROM DocumentType dt WHERE dt.[Type] = r.DbType)
    )
        THROW 51002, N'Thiếu một trong bốn DocumentType bắt buộc của cổng Registrant.', 1;

    DECLARE @Accounts TABLE (
        SortNo INT PRIMARY KEY,
        Username NVARCHAR(100) NOT NULL,
        Email NVARCHAR(255) NOT NULL,
        FullName NVARCHAR(255) NOT NULL,
        DateOfBirth DATE NOT NULL,
        PhoneNumber NVARCHAR(20) NOT NULL,
        Sex BIT NOT NULL,
        GovernmentIdNumber NVARCHAR(100) NOT NULL,
        [Address] NVARCHAR(500) NOT NULL,
        DemoState NVARCHAR(30) NOT NULL
    );

    INSERT INTO @Accounts VALUES
    (1,  N'demo_reg_empty',       N'demo.reg.empty@example.test',       N'Demo Chưa Có Hồ Sơ',       '1998-01-11', N'0908100001', 1, N'079098100001', N'Quận 1, Thành phố Hồ Chí Minh', N'EMPTY'),
    (2,  N'demo_reg_pending',     N'demo.reg.pending@example.test',     N'Demo Hồ Sơ Chờ Duyệt',     '1998-02-12', N'0908100002', 0, N'079098100002', N'Quận 3, Thành phố Hồ Chí Minh', N'PENDING_FULL'),
    (3,  N'demo_reg_approved_01', N'demo.reg.approved01@example.test',  N'Demo Đã Duyệt 01',         '1998-03-13', N'0908100003', 1, N'079098100003', N'Ba Đình, Hà Nội', N'APPROVED'),
    (4,  N'demo_reg_approved_02', N'demo.reg.approved02@example.test',  N'Demo Đã Duyệt 02',         '1998-04-14', N'0908100004', 0, N'079098100004', N'Hoàn Kiếm, Hà Nội', N'APPROVED'),
    (5,  N'demo_reg_approved_03', N'demo.reg.approved03@example.test',  N'Demo Đã Duyệt 03',         '1998-05-15', N'0908100005', 1, N'079098100005', N'Hải Châu, Đà Nẵng', N'APPROVED'),
    (6,  N'demo_reg_approved_04', N'demo.reg.approved04@example.test',  N'Demo Đã Duyệt 04',         '1998-06-16', N'0908100006', 0, N'079098100006', N'Thanh Khê, Đà Nẵng', N'APPROVED'),
    (7,  N'demo_reg_approved_05', N'demo.reg.approved05@example.test',  N'Demo Đã Duyệt 05',         '1998-07-17', N'0908100007', 1, N'079098100007', N'Ninh Kiều, Cần Thơ', N'APPROVED'),
    (8,  N'demo_reg_approved_06', N'demo.reg.approved06@example.test',  N'Demo Đã Duyệt 06',         '1998-08-18', N'0908100008', 0, N'079098100008', N'Bình Thủy, Cần Thơ', N'APPROVED'),
    (9,  N'demo_reg_approved_07', N'demo.reg.approved07@example.test',  N'Demo Thi Lại 07',           '1998-09-19', N'0908100009', 1, N'079098100009', N'Thành phố Huế', N'APPROVED'),
    (10, N'demo_reg_approved_08', N'demo.reg.approved08@example.test',  N'Demo Thi Lại 08',           '1998-10-20', N'0908100010', 0, N'079098100010', N'Thành phố Huế', N'APPROVED'),
    (11, N'demo_reg_missing',     N'demo.reg.missing@example.test',     N'Demo Thiếu Tài Liệu',       '1998-11-21', N'0908100011', 1, N'079098100011', N'Thành phố Hải Phòng', N'PENDING_MISSING'),
    (12, N'demo_reg_reject_ready',N'demo.reg.reject@example.test',      N'Demo Sẵn Sàng Từ Chối',     '1998-12-22', N'0908100012', 0, N'079098100012', N'Thành phố Hải Phòng', N'PENDING_REJECT');

    -- Upsert User/Profile để seed có thể chạy lại.
    UPDATE u
    SET u.Email = a.Email,
        u.PasswordHash = @PasswordHash,
        u.RoleId = @RegistrantRoleId,
        u.IsActive = 1
    FROM [User] u
    JOIN @Accounts a ON a.Username = u.Username;

    INSERT INTO [User] (Username, Email, PasswordHash, RoleId, IsActive)
    SELECT a.Username, a.Email, @PasswordHash, @RegistrantRoleId, 1
    FROM @Accounts a
    WHERE NOT EXISTS (SELECT 1 FROM [User] u WHERE u.Username = a.Username);

    UPDATE p
    SET p.FullName = a.FullName,
        p.DateOfBirth = a.DateOfBirth,
        p.PhoneNumber = a.PhoneNumber,
        p.Sex = a.Sex,
        p.GovernmentIdNumber = a.GovernmentIdNumber,
        p.[Address] = a.[Address]
    FROM Profile p
    JOIN [User] u ON u.UserId = p.UserId
    JOIN @Accounts a ON a.Username = u.Username;

    INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, [Address], UserId)
    SELECT a.FullName, a.DateOfBirth, a.PhoneNumber, a.Sex, a.GovernmentIdNumber,
           a.[Address], u.UserId
    FROM @Accounts a
    JOIN [User] u ON u.Username = a.Username
    WHERE NOT EXISTS (SELECT 1 FROM Profile p WHERE p.UserId = u.UserId);

    -- Xóa riêng dữ liệu hai kỳ thi demo nếu seed được chạy lại.
    DECLARE @DemoExamIds TABLE (ExamId INT PRIMARY KEY);
    INSERT INTO @DemoExamIds (ExamId)
    SELECT ExamId FROM Exam
    WHERE ExamCode IN (N'DEMO-A1-20260728-1200', N'DEMO-A1-20260730-0800');

    DELETE ca
    FROM CandidateAnswer ca
    JOIN TheoryPaper tp ON tp.TheoryPaperId = ca.TheoryPaperId
    JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE tp
    FROM TheoryPaper tp
    JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE dr
    FROM DeductionRecord dr
    JOIN ExamScore esc ON esc.ExamScoreId = dr.ExamScoreId
    JOIN ExamResult er ON er.ExamResultId = esc.ExamResultId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE esc
    FROM ExamScore esc
    JOIN ExamResult er ON er.ExamResultId = esc.ExamResultId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE er
    FROM ExamResult er
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE p
    FROM Payment p
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE cv
    FROM CandidateViolation cv
    JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = cv.ExamEnrollmentSectionId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE ees
    FROM ExamEnrollmentSection ees
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE ee
    FROM ExamEnrollment ee
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE sd
    FROM ScoreDeduction sd
    JOIN ExamSection es ON es.ExamSectionId = sd.ExamSectionId
    JOIN @DemoExamIds d ON d.ExamId = es.ExamId;

    DELETE esch
    FROM ExaminerSchedule esch
    JOIN @DemoExamIds d ON d.ExamId = esch.ExamId;

    DELETE x
    FROM Exam_ExamArea x
    JOIN @DemoExamIds d ON d.ExamId = x.ExamId;

    DELETE es
    FROM ExamSection es
    JOIN @DemoExamIds d ON d.ExamId = es.ExamId;

    DELETE e
    FROM Exam e
    JOIN @DemoExamIds d ON d.ExamId = e.ExamId;

    DELETE c
    FROM Candidate c
    WHERE (
            c.CandidateNumber LIKE N'D28-%'
         OR c.CandidateNumber LIKE N'D30-%'
         OR (c.FullName LIKE N'Thí Sinh Demo %'
             AND TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 40)
          )
      AND NOT EXISTS (SELECT 1 FROM ExamEnrollment ee WHERE ee.CandidateId = c.CandidateId);

    -- Reset dữ liệu workflow chỉ của 12 profile demo.
    DECLARE @DemoProfileIds TABLE (ProfileId INT PRIMARY KEY);
    INSERT INTO @DemoProfileIds
    SELECT p.ProfileId
    FROM Profile p
    JOIN [User] u ON u.UserId = p.UserId
    JOIN @Accounts a ON a.Username = u.Username;

    DELETE rd
    FROM RegistrationDates rd
    JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
    JOIN @DemoProfileIds p ON p.ProfileId = er.ProfileId;

    DELETE er
    FROM ExamRegistration er
    JOIN @DemoProfileIds p ON p.ProfileId = er.ProfileId;

    DELETE d
    FROM Document d
    JOIN @DemoProfileIds p ON p.ProfileId = d.ProfileId;

    -- Document: empty = 0; missing = Portrait + IdFront; các profile còn lại đủ 4/4.
    INSERT INTO Document (DocumentTypeId, DocumentUrl, Notes, ProfileId)
    SELECT dt.DocumentTypeId,
           r.DocumentUrl,
           CASE
               WHEN a.DemoState = N'APPROVED'
                   THEN N'#APPROVED# Ban quản lý đã duyệt.'
               ELSE N'#PENDING# Gửi yêu cầu duyệt hồ sơ.'
           END,
           p.ProfileId
    FROM @Accounts a
    JOIN [User] u ON u.Username = a.Username
    JOIN Profile p ON p.UserId = u.UserId
    CROSS JOIN @RequiredDocumentTypes r
    JOIN DocumentType dt ON dt.[Type] = r.DbType
    WHERE a.DemoState <> N'EMPTY'
      AND (
            a.DemoState <> N'PENDING_MISSING'
            OR r.UiType IN (N'Portrait', N'IdFront')
          );

    -- 11 hồ sơ gốc: 1 Pending đủ, 8 Approved, 1 Pending thiếu, 1 Pending để test reject.
    INSERT INTO ExamRegistration
        (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
    SELECT CASE WHEN a.DemoState = N'APPROVED' THEN N'Approved' ELSE N'Pending' END,
           CASE
               WHEN a.DemoState = N'APPROVED'
                   THEN N'#PROFILE_DOC# Hồ sơ demo đã được ban quản lý phê duyệt.'
               WHEN a.DemoState = N'PENDING_MISSING'
                   THEN N'#PROFILE_DOC# Hồ sơ demo còn thiếu CCCD mặt sau và giấy khám sức khỏe.'
               WHEN a.DemoState = N'PENDING_REJECT'
                   THEN N'#PROFILE_DOC# Hồ sơ đủ tài liệu, sẵn sàng để cán bộ test thao tác từ chối.'
               ELSE N'#PROFILE_DOC# Hồ sơ đủ tài liệu đang chờ ban quản lý duyệt.'
           END,
           p.ProfileId,
           @A1LicenceId,
           CASE WHEN a.Username IN (N'demo_reg_approved_07', N'demo_reg_approved_08')
                THEN 1 ELSE 0 END
    FROM @Accounts a
    JOIN [User] u ON u.Username = a.Username
    JOIN Profile p ON p.UserId = u.UserId
    WHERE a.DemoState <> N'EMPTY';

    -- Dòng Approved thứ 9: profile 01 được duyệt thêm hạng A.
    INSERT INTO ExamRegistration
        (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
    SELECT N'Approved',
           N'#LICENCE_DOC# Xin duyệt hạng với hồ sơ đã có.',
           p.ProfileId,
           @ALicenceId,
           0
    FROM Profile p
    JOIN [User] u ON u.UserId = p.UserId
    WHERE u.Username = N'demo_reg_approved_01';

    -- Một ngày thi dự kiến A1, cách ngày seed hơn 7 ngày làm việc.
    DECLARE @PreferredExamDate DATE = '2026-08-10';
    DECLARE @ExamDateId INT;

    IF EXISTS (SELECT 1 FROM ExamDates WHERE ExamDate = @PreferredExamDate AND LicenceId <> @A1LicenceId)
        THROW 51003, N'Ngày 10/08/2026 đã được dùng cho hạng khác; không thể tạo ExamDates demo A1.', 1;

    IF NOT EXISTS (SELECT 1 FROM ExamDates WHERE ExamDate = @PreferredExamDate)
        INSERT INTO ExamDates (ExamDate, LicenceId, [Status], PoliceStatus)
        VALUES (@PreferredExamDate, @A1LicenceId, N'Open', N'NOT_SENT');
    ELSE
        UPDATE ExamDates
        SET [Status] = N'Open',
            PoliceStatus = N'NOT_SENT',
            CancelReason = NULL,
            CancelledAt = NULL,
            CancelledBy = NULL,
            CancelledRegistrationCount = NULL
        WHERE ExamDate = @PreferredExamDate;

    SELECT @ExamDateId = ExamDateId FROM ExamDates WHERE ExamDate = @PreferredExamDate;

    -- 6 RegistrationDates A1 (approved_03..08); bỏ approved_01/02 vì sẽ gắn kỳ chính thức;
    -- bỏ ER hạng A của approved_01 (không thuộc ngày dự kiến A1).
    INSERT INTO RegistrationDates
        (ExamRegistrationId, ExamDateId, IsActive, PoliceStatus, PoliceReason, OfficialCandidateNumber)
    SELECT er.ExamRegistrationId, @ExamDateId, 1, N'NOT_SENT', NULL, NULL
    FROM ExamRegistration er
    JOIN @DemoProfileIds p ON p.ProfileId = er.ProfileId
    JOIN Profile pr ON pr.ProfileId = er.ProfileId
    JOIN [User] u ON u.UserId = pr.UserId
    WHERE er.RegistrationStatus = N'Approved'
      AND er.LicenceId = @A1LicenceId
      AND u.Username NOT IN (N'demo_reg_approved_01', N'demo_reg_approved_02');

    -- Tạo hai kỳ thi chính thức.
    INSERT INTO Exam
        (ExamCode, ExamDate, StartTime, EndTime, [Status], ExamPassword,
         CentreName, LicenceId, SourceExamDateId)
    VALUES
    (N'DEMO-A1-20260728-1200', '2026-07-28', '2026-07-28T12:00:00', NULL,
     N'Chưa diễn ra', NULL, N'Trung tâm Sát hạch Lái Vui – Demo 28/07', @A1LicenceId, NULL),
    (N'DEMO-A1-20260730-0800', '2026-07-30', '2026-07-30T08:00:00', NULL,
     N'Chưa diễn ra', NULL, N'Trung tâm Sát hạch Lái Vui – Demo 30/07', @A1LicenceId, NULL);

    DECLARE @Exam28Id INT =
        (SELECT ExamId FROM Exam WHERE ExamCode = N'DEMO-A1-20260728-1200');
    DECLARE @Exam30Id INT =
        (SELECT ExamId FROM Exam WHERE ExamCode = N'DEMO-A1-20260730-0800');

    INSERT INTO ExamSection (SectionType, LicenceId, DurationMinutes, ExamId) VALUES
    (N'Lý thuyết', @A1LicenceId, 19, @Exam28Id),
    (N'Thực hành trong hình', @A1LicenceId, NULL, @Exam28Id),
    (N'Lý thuyết', @A1LicenceId, 19, @Exam30Id),
    (N'Thực hành trong hình', @A1LicenceId, NULL, @Exam30Id);

    -- Ghép đầy đủ các khu vực phù hợp; tuyệt đối chưa tạo ExaminerSchedule.
    INSERT INTO Exam_ExamArea (ExamId, ExamAreaId)
    SELECT e.ExamId, ea.ExamAreaId
    FROM Exam e
    CROSS JOIN ExamArea ea
    WHERE e.ExamId IN (@Exam28Id, @Exam30Id)
      AND ea.AreaType IN (N'Phòng thủ tục', N'Phòng thi', N'Sân thi');

    IF NOT EXISTS (
        SELECT 1 FROM Exam_ExamArea x
        JOIN ExamArea ea ON ea.ExamAreaId = x.ExamAreaId
        WHERE x.ExamId = @Exam28Id AND ea.AreaType = N'Phòng thủ tục'
    ) OR NOT EXISTS (
        SELECT 1 FROM Exam_ExamArea x
        JOIN ExamArea ea ON ea.ExamAreaId = x.ExamAreaId
        WHERE x.ExamId = @Exam28Id AND ea.AreaType = N'Phòng thi'
    ) OR NOT EXISTS (
        SELECT 1 FROM Exam_ExamArea x
        JOIN ExamArea ea ON ea.ExamAreaId = x.ExamAreaId
        WHERE x.ExamId = @Exam28Id AND ea.AreaType = N'Sân thi'
    )
        THROW 51004, N'Thiếu khu vực Phòng thủ tục/Phòng thi/Sân thi trong master data.', 1;

    DECLARE @CandidateSeed TABLE (
        ExamDay INT NOT NULL,
        SeqNo INT NOT NULL,
        CandidateNumber NVARCHAR(50) NOT NULL,
        FullName NVARCHAR(255) NOT NULL,
        GovernmentIdNumber NVARCHAR(100) NOT NULL,
        TakeTheory BIT NOT NULL,
        TakeLayout BIT NOT NULL,
        ReasonForTaking NVARCHAR(355) NOT NULL,
        PRIMARY KEY (ExamDay, SeqNo)
    );

    ;WITH n AS (
        SELECT 1 AS SeqNo
        UNION ALL
        SELECT SeqNo + 1 FROM n WHERE SeqNo < 20
    )
    INSERT INTO @CandidateSeed
        (ExamDay, SeqNo, CandidateNumber, FullName, GovernmentIdNumber,
         TakeTheory, TakeLayout, ReasonForTaking)
    SELECT d.ExamDay,
           n.SeqNo,
           -- SBD số (001–020 ngày 28, 021–040 ngày 30) để examiner/examstaff parse INT được.
           CASE d.ExamDay
               WHEN 28 THEN RIGHT(N'000' + CAST(n.SeqNo AS NVARCHAR(3)), 3)
               ELSE RIGHT(N'000' + CAST(20 + n.SeqNo AS NVARCHAR(3)), 3)
           END,
           CASE d.ExamDay
               WHEN 28 THEN N'Thí Sinh Demo 28-' + RIGHT(N'00' + CAST(n.SeqNo AS NVARCHAR(2)), 2)
               ELSE N'Thí Sinh Demo 30-' + RIGHT(N'00' + CAST(n.SeqNo AS NVARCHAR(2)), 2)
           END,
           CASE
               WHEN d.ExamDay = 28 AND n.SeqNo = 1 THEN N'079098100003'
               WHEN d.ExamDay = 30 AND n.SeqNo = 1 THEN N'079098100004'
               WHEN d.ExamDay = 28 THEN N'028726' + RIGHT(N'0000' + CAST(n.SeqNo AS NVARCHAR(4)), 4)
               ELSE N'030726' + RIGHT(N'0000' + CAST(n.SeqNo AS NVARCHAR(4)), 4)
           END,
           CASE WHEN n.SeqNo BETWEEN 8 AND 14 THEN 0 ELSE 1 END,
           CASE WHEN n.SeqNo BETWEEN 15 AND 20 THEN 0 ELSE 1 END,
           CASE
               WHEN n.SeqNo BETWEEN 8 AND 14
                   THEN N'Bảo lưu lý thuyết - chỉ thi thực hành trong hình'
               WHEN n.SeqNo BETWEEN 15 AND 20
                   THEN N'Bảo lưu thực hành - chỉ thi lý thuyết'
               ELSE N'Thi cả lý thuyết và thực hành trong hình'
           END
    FROM n
    CROSS JOIN (VALUES (28), (30)) d(ExamDay)
    OPTION (MAXRECURSION 20);

    INSERT INTO Candidate
        (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex,
         GovernmentIdNumber, [Address], TakeTheory, TakeLayout, TakeNo,
         ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended)
    SELECT cs.CandidateNumber,
           cs.FullName,
           DATEADD(DAY, cs.SeqNo, CAST('1996-01-01' AS DATE)),
           CASE cs.ExamDay
               WHEN 28 THEN N'09128' + RIGHT(N'00000' + CAST(cs.SeqNo AS NVARCHAR(5)), 5)
               ELSE N'09130' + RIGHT(N'00000' + CAST(cs.SeqNo AS NVARCHAR(5)), 5)
           END,
           NULL,
           CASE WHEN cs.SeqNo % 2 = 0 THEN 0 ELSE 1 END,
           cs.GovernmentIdNumber,
           N'Địa chỉ demo kỳ thi ngày ' + CAST(cs.ExamDay AS NVARCHAR(2)) + N'/07/2026',
           cs.TakeTheory,
           cs.TakeLayout,
           CASE WHEN cs.SeqNo <= 7 THEN 1 ELSE 2 END,
           cs.ReasonForTaking,
           NULL, -- Chưa có ảnh => chưa hoàn tất thủ tục.
           0,
           0
    FROM @CandidateSeed cs;

    -- Hai dòng lifecycle tách khỏi hồ sơ tài liệu để dashboard đọc đúng kỳ chính thức.
    DECLARE @Lifecycle28Id INT;
    DECLARE @Lifecycle30Id INT;

    INSERT INTO ExamRegistration
        (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
    SELECT N'PreRegistered',
           N'#EXAM_ID#' + CAST(@Exam28Id AS NVARCHAR(20)) + N'# Đã liên kết kỳ thi chính thức.',
           p.ProfileId, @A1LicenceId, 0
    FROM Profile p JOIN [User] u ON u.UserId = p.UserId
    WHERE u.Username = N'demo_reg_approved_01';
    SET @Lifecycle28Id = SCOPE_IDENTITY();

    INSERT INTO ExamRegistration
        (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
    SELECT N'PreRegistered',
           N'#EXAM_ID#' + CAST(@Exam30Id AS NVARCHAR(20)) + N'# Đã liên kết kỳ thi chính thức.',
           p.ProfileId, @A1LicenceId, 0
    FROM Profile p JOIN [User] u ON u.UserId = p.UserId
    WHERE u.Username = N'demo_reg_approved_02';
    SET @Lifecycle30Id = SCOPE_IDENTITY();

    -- Vô hiệu hóa RegistrationDates cũ (nếu còn) của 2 profile đã gắn kỳ chính thức.
    UPDATE rd
    SET rd.IsActive = 0
    FROM RegistrationDates rd
    JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
    JOIN Profile p ON p.ProfileId = er.ProfileId
    JOIN [User] u ON u.UserId = p.UserId
    WHERE rd.ExamDateId = @ExamDateId
      AND u.Username IN (N'demo_reg_approved_01', N'demo_reg_approved_02')
      AND er.RegistrationStatus <> N'PreRegistered';

    -- Vô hiệu hóa dòng RegistrationDates lệch hạng với ngày dự kiến.
    UPDATE rd
    SET rd.IsActive = 0
    FROM RegistrationDates rd
    JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
    JOIN ExamDates ed ON ed.ExamDateId = rd.ExamDateId
    WHERE rd.ExamDateId = @ExamDateId
      AND rd.IsActive = 1
      AND er.LicenceId <> ed.LicenceId;

    INSERT INTO ExamEnrollment
        (CandidateId, ExamId, ExamRegistrationId, AllocatedExamAreaId, ExamDeviceId)
    SELECT c.CandidateId,
           CASE WHEN cs.ExamDay = 28 THEN @Exam28Id ELSE @Exam30Id END,
           CASE
               WHEN cs.ExamDay = 28 AND cs.SeqNo = 1 THEN @Lifecycle28Id
               WHEN cs.ExamDay = 30 AND cs.SeqNo = 1 THEN @Lifecycle30Id
               ELSE NULL
           END,
           NULL,
           NULL
    FROM @CandidateSeed cs
    JOIN Candidate c ON c.CandidateNumber = cs.CandidateNumber;

    -- Chỉ tạo phần thi mà thí sinh thực sự phải thi; chưa phân phòng/thiết bị/check-in.
    INSERT INTO ExamEnrollmentSection
        (ExamEnrollmentId, ExamSectionId, ExamAreaId, ExamDeviceId, [Status],
         AllocatedAt, AllocatedBy, CheckedInAt, CheckedInBy,
         StartedAt, CompletedAt, ResultPrintedAt)
    SELECT ee.ExamEnrollmentId,
           es.ExamSectionId,
           NULL,
           NULL,
           N'Chưa thi',
           NULL, NULL, NULL, NULL, NULL, NULL, NULL
    FROM ExamEnrollment ee
    JOIN Candidate c ON c.CandidateId = ee.CandidateId
    JOIN ExamSection es ON es.ExamId = ee.ExamId
    WHERE ee.ExamId IN (@Exam28Id, @Exam30Id)
      AND (
            (es.SectionType = N'Lý thuyết' AND c.TakeTheory = 1)
         OR (es.SectionType = N'Thực hành trong hình' AND c.TakeLayout = 1)
          );

    -- Bảo đảm yêu cầu "chưa phân sát hạch viên" kể cả khi script được chỉnh/chạy lại.
    DELETE FROM ExaminerSchedule WHERE ExamId IN (@Exam28Id, @Exam30Id);

    COMMIT TRANSACTION;

    PRINT N'SEED_REGISTRANT_DEMO.sql hoàn tất.';
    PRINT N'12 tài khoản Registrant - mật khẩu chung: login123';
    PRINT N'2 kỳ thi, mỗi kỳ 20 thí sinh; chưa ảnh, chưa payment, chưa check-in, chưa phân SHV.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Chuẩn hóa SBD demo cũ dạng D28-/D30- sang số (001–040) nếu còn trong DB.
UPDATE Candidate
SET CandidateNumber = CASE
    WHEN CandidateNumber LIKE N'D28-%'
        THEN RIGHT(N'000' + CAST(TRY_CAST(RIGHT(CandidateNumber, 2) AS INT) AS NVARCHAR(3)), 3)
    WHEN CandidateNumber LIKE N'D30-%'
        THEN RIGHT(N'000' + CAST(20 + TRY_CAST(RIGHT(CandidateNumber, 2) AS INT) AS NVARCHAR(3)), 3)
    ELSE CandidateNumber
END
WHERE CandidateNumber LIKE N'D28-%' OR CandidateNumber LIKE N'D30-%';
GO

-- =============================================================================
-- KIỂM CHỨNG NHANH
-- =============================================================================
SELECT u.Username, u.Email, u.IsActive, p.ProfileId, p.GovernmentIdNumber
FROM [User] u
JOIN [Role] r ON r.RoleId = u.RoleId
LEFT JOIN Profile p ON p.UserId = u.UserId
WHERE u.Username LIKE N'demo_reg_%'
ORDER BY u.Username;

SELECT
    COUNT(*) AS DemoRegistrantCount,
    SUM(CASE WHEN u.Username = N'demo_reg_empty' THEN 1 ELSE 0 END) AS EmptyAccountCount
FROM [User] u
WHERE u.Username LIKE N'demo_reg_%';

SELECT
    SUM(CASE WHEN d.ProfileId IS NOT NULL THEN 1 ELSE 0 END) AS DocumentCount,
    COUNT(DISTINCT CASE WHEN d.ProfileId IS NOT NULL THEN p.ProfileId END) AS ProfilesWithDocuments
FROM Profile p
JOIN [User] u ON u.UserId = p.UserId
LEFT JOIN Document d ON d.ProfileId = p.ProfileId
WHERE u.Username LIKE N'demo_reg_%';

SELECT er.RegistrationStatus, COUNT(*) AS Total
FROM ExamRegistration er
JOIN Profile p ON p.ProfileId = er.ProfileId
JOIN [User] u ON u.UserId = p.UserId
WHERE u.Username LIKE N'demo_reg_%'
GROUP BY er.RegistrationStatus
ORDER BY er.RegistrationStatus;

SELECT e.ExamCode, e.StartTime, e.[Status],
       COUNT(DISTINCT ee.ExamEnrollmentId) AS CandidateCount,
       SUM(CASE WHEN c.TakeTheory = 1 AND c.TakeLayout = 1 THEN 1 ELSE 0 END) AS BothSections,
       SUM(CASE WHEN c.TakeTheory = 0 AND c.TakeLayout = 1 THEN 1 ELSE 0 END) AS TheoryExempt,
       SUM(CASE WHEN c.TakeTheory = 1 AND c.TakeLayout = 0 THEN 1 ELSE 0 END) AS LayoutExempt,
       SUM(CASE WHEN c.PhotoImageUrl IS NOT NULL THEN 1 ELSE 0 END) AS HasPhoto,
       SUM(CASE WHEN pay.PaymentId IS NOT NULL THEN 1 ELSE 0 END) AS HasPayment
FROM Exam e
JOIN ExamEnrollment ee ON ee.ExamId = e.ExamId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
LEFT JOIN Payment pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
WHERE e.ExamCode IN (N'DEMO-A1-20260728-1200', N'DEMO-A1-20260730-0800')
GROUP BY e.ExamCode, e.StartTime, e.[Status]
ORDER BY e.StartTime;

SELECT e.ExamCode,
       COUNT(DISTINCT x.ExamAreaId) AS LinkedAreaCount,
       COUNT(DISTINCT esch.ExaminerScheduleId) AS ExaminerAssignmentCount
FROM Exam e
LEFT JOIN Exam_ExamArea x ON x.ExamId = e.ExamId
LEFT JOIN ExaminerSchedule esch ON esch.ExamId = e.ExamId
WHERE e.ExamCode IN (N'DEMO-A1-20260728-1200', N'DEMO-A1-20260730-0800')
GROUP BY e.ExamCode
ORDER BY e.ExamCode;
GO
