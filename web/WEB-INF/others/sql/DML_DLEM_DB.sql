-- ============================================
-- ============================================
-- DML – DLEM_DB_2
-- Hệ thống quản lý sát hạch GPLX
-- Mật khẩu mặc định mọi tài khoản: login123
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
-- 2. NGƯỜI DÙNG HỆ THỐNG
-- ============================================
INSERT INTO [User] (Username, Email, PasswordHash, RoleId, IsActive) VALUES
(N'admin',          N'admin@trungtamsathach.vn',        N'login123', 1, 1),
(N'shv_tung', N'tung.nguyen@sathach.vn',          N'login123', 2, 1),
(N'shv_lan',  N'lan.tran@sathach.vn',             N'login123', 2, 1),
(N'shv_dung', N'dung.hoang@sathach.vn',           N'login123', 2, 1),
(N'qly123',   N'quanly.hoso@trungtamsathach.vn',  N'login123', 3, 1),
(N'exam_hoa',      N'hoa.le@trungtamsathach.vn',       N'login123', 4, 1),
(N'exam_minh',     N'minh.vu@trungtamsathach.vn',      N'login123', 4, 1),
(N'user_an',     N'an.nguyen@gmail.com',             N'login123', 6, 1),
(N'user_binh',   N'binh.tran@gmail.com',             N'login123', 6, 1),
(N'user_chinh',  N'chinh.le@gmail.com',              N'login123', 6, 1),
(N'user_dung',   N'dung.pham@gmail.com',             N'login123', 6, 1),
(N'user_em',     N'em.hoang@gmail.com',              N'login123', 6, 1),
(N'user_phuong', N'phuong.vu@gmail.com',             N'login123', 6, 1),
(N'user_hai',    N'hai.do@gmail.com',                N'login123', 6, 1),
(N'user_kim',    N'kim.ngo@gmail.com',               N'login123', 6, 1),
(N'user_long',   N'long.bui@gmail.com',              N'login123', 6, 0),
(N'user_hoa',    N'hoa.thi@gmail.com',               N'login123', 6, 1),
(N'user_khoa',   N'khoa.tran@gmail.com',             N'login123', 6, 1);
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
(N'Trần Văn Khoa',     '1996-09-03', N'0911004901', 1, N'001196090301', N'72 Cầu Giấy, Cầu Giấy, Hà Nội', 18);
GO

-- ============================================
-- 4. TÀI LIỆU HỒ SƠ ĐĂNG KÝ
-- ============================================
INSERT INTO Document (DocumentType, DocumentUrl, Notes, ProfileId) VALUES
(N'Căn cước công dân (mặt trước)', N'/uploads/dossiers/8/cccd_mat_truoc.jpg', NULL, 8),
(N'Căn cước công dân (mặt sau)',  N'/uploads/dossiers/8/cccd_mat_sau.jpg', NULL, 8),
(N'Giấy khám sức khỏe',           N'/uploads/dossiers/8/giay_kham_suc_khoe.pdf', N'Đủ điều kiện sức khỏe lái xe', 8),
(N'Căn cước công dân (mặt trước)', N'/uploads/dossiers/9/cccd_mat_truoc.jpg', NULL, 9),
(N'Giấy khám sức khỏe',           N'/uploads/dossiers/9/giay_kham_suc_khoe.pdf', NULL, 9),
(N'Căn cước công dân (mặt trước)', N'/uploads/dossiers/10/cccd_mat_truoc.jpg', NULL, 10),
(N'Giấy chứng nhận tốt nghiệp',   N'/uploads/dossiers/11/bang_tot_nghiep.pdf', N'Hạng A1', 11),
(N'Căn cước công dân (mặt trước)', N'/uploads/dossiers/11/cccd_mat_truoc.jpg', NULL, 11),
(N'Giấy phép lái xe hiện có',     N'/uploads/dossiers/12/gplx_hang_b1.jpg', N'Nâng hạng lên B', 12);
GO

-- ============================================
-- 5. HẠNG GPLX — Trung tâm loại 3: chỉ A1, A, B1
-- ============================================
INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId) VALUES
(N'A1', N'Xe mô tô hai bánh có dung tích xi-lanh đến 125 cm³', 18, 0, NULL),
(N'A',  N'Xe mô tô hai bánh có dung tích xi-lanh trên 125 cm³', 18, 0, NULL),
(N'B1', N'Ô tô số tự động tải trọng dưới 3.500 kg', 18, 0, NULL);
GO

UPDATE Licence SET UpgradeFromLicenceId = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1') WHERE LicenceClass = N'A';
GO

-- ============================================
-- 6. HỒ SƠ ĐĂNG KÝ THI (trung tâm)
-- ============================================
INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId) VALUES
(N'Duyệt',       N'Đủ hồ sơ, đủ điều kiện sức khỏe', 8,  (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'Duyệt',       N'Đã xác minh căn cước và giấy khám sức khỏe', 9, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'Chờ duyệt',   N'Chờ cán bộ quản lý duyệt hồ sơ', 10, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A')),
(N'Duyệt',       N'Đăng ký thi cấp mới hạng A1', 11, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')),
(N'Duyệt',       N'Nâng hạng từ A1 lên A', 12, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A')),
(N'Duyệt',       N'Đăng ký hạng B1 số tự động', 13, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'Duyệt',       N'Hồ sơ hoàn chỉnh', 14, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'Chờ duyệt',   N'Chờ bổ sung ảnh chân dung', 15, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')),
(N'Loại',        N'Không đủ điều kiện sức khỏe theo quy định', 16, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'Duyệt',       N'Đã thu học phí và lệ phí thi', 17, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'Loại',        N'Cần bổ sung giấy xác nhận cư trú', 18, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'));
GO

-- ============================================
-- 7. KỲ THI (khoá thi) — chỉ hạng A1, A, B1
-- ============================================
INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId) VALUES
(N'A1-20260601', '2026-06-01 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Mở',          (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')),
(N'A-20260610',  '2026-06-10 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Chưa diễn ra', (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A')),
(N'B1-20260601', '2026-06-01 07:00:00', N'Trung tâm Sát hạch Lái Vui – Hà Nội', N'Mở',          (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'B1-20260608', '2026-06-08 07:00:00', N'Trung tâm Sát hạch Lái Vui – Đà Nẵng', N'Chưa diễn ra', (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'));
GO

-- ============================================
-- 8. PHẦN THI
-- ============================================
INSERT INTO ExamSection (SectionName) VALUES
(N'Lý thuyết'),
(N'Thực hành trong hình'),
(N'Thực hành trên đường');
GO

-- ============================================
-- 9. HẠNG ↔ PHẦN THI (thời gian làm bài theo quy chế)
-- ============================================
INSERT INTO Licence_ExamSection (LicenceId, ExamSectionId, DurationMinutes) VALUES
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), 1, 19),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1'), 2, NULL),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'),  1, 19),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'A'),  2, NULL),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 1, 20),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 2, 18),
((SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), 3, 30);
GO

-- ============================================
-- 10. CA THI (IsMorningSession: 1 = Ca sáng, 0 = Ca chiều)
-- Phần thi gắn qua Session_ExamSection; UI chỉ hiển thị Ca sáng / Ca chiều.
-- ============================================
INSERT INTO [Session] (IsMorningSession, StartTime, EndTime, [Status], ExamId) VALUES
(1, '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'Đang diễn ra', (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260601')),
(1, '2026-06-01 09:30:00', '2026-06-01 11:30:00', N'Chưa diễn ra',  (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260601')),
(0, '2026-06-01 13:00:00', '2026-06-01 16:00:00', N'Chưa diễn ra',  (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260601')),
(1, '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'Đang diễn ra', (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601')),
(1, '2026-06-01 10:00:00', '2026-06-01 11:30:00', N'Chưa diễn ra',  (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601')),
(1, '2026-06-08 07:30:00', '2026-06-08 09:00:00', N'Chưa diễn ra',  (SELECT ExamId FROM Exam WHERE ExamCode = N'B1-20260608'));
GO

-- ============================================
-- 11. CA ↔ PHẦN THI
-- Tra cứu SessionId: ExamCode + IsMorningSession + StartTime (phân biệt nhiều ca cùng buổi)
-- ============================================
INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), 1),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 09:30:00'), 2),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 0 AND s.StartTime = '2026-06-01 13:00:00'), 3),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'A1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), 1),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'A1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 10:00:00'), 2),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260608' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-08 07:30:00'), 1);
GO

-- ============================================
-- 12. KHU VỰC THI (ExamZone) — trung tâm loại 3
-- ============================================
INSERT INTO ExamZone (ZoneName, [Location], IsActive) VALUES
(N'Khu nhà điều hành',     N'Tòa A – Trung tâm Sát hạch Lái Vui, Hà Nội', 1),
(N'Khu sân thi mô tô',     N'Khu sân thực hành số 1 – Trung tâm Sát hạch Lái Vui', 1),
(N'Khu sân thi ô tô B1',   N'Khu sân thực hành số 2 – Trung tâm Sát hạch Lái Vui', 1);
GO

-- ============================================
-- 13. PHÒNG / SÂN / ĐƯỜNG THI (ExamArea)
-- AreaType: Phòng thủ tục | Phòng thi | Sân thi | Đường thi
-- ============================================
INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location], ExamZoneId) VALUES
(N'Phòng thủ tục 102',   N'Phòng thủ tục', 30, N'Tầng 1, Tòa A', (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu nhà điều hành')),
(N'Phòng thi LT 1',      N'Phòng thi',     30, N'Tầng 2, Tòa B', (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu nhà điều hành')),
(N'Phòng thi LT 2',      N'Phòng thi',     30, N'Tầng 2, Tòa B', (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu nhà điều hành')),
(N'Sân thi mô tô',       N'Sân thi',       20, N'Sân số 1',       (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu sân thi mô tô')),
(N'Sân thi ô tô B1',     N'Sân thi',       12, N'Sân số 2',       (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu sân thi ô tô B1')),
(N'Đường thi B1',        N'Đường thi',   NULL, N'Lộ trình ngoài khuôn viên', (SELECT ExamZoneId FROM ExamZone WHERE ZoneName = N'Khu sân thi ô tô B1'));
GO

-- ============================================
-- 14. CA ↔ ĐỊA ĐIỂM THI
-- ============================================
INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'A1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 09:30:00'), (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô B1')),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 0 AND s.StartTime = '2026-06-01 13:00:00'), (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường thi B1')),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'A1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 10:00:00'), (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô')),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260608' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-08 07:30:00'), (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1'));
GO

-- ============================================
-- 15. PHÂN CÔNG SÁT HẠCH VIÊN
-- ============================================
INSERT INTO ExaminerSchedule (SessionId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt) VALUES
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
 (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1'),
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:00:00'),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 09:30:00'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'),
 (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô B1'),
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:05:00'),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 0 AND s.StartTime = '2026-06-01 13:00:00'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trên đường'),
 (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường thi B1'),
 (SELECT UserId FROM [User] WHERE Username = N'shv_lan'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), '2026-05-25 08:10:00'),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'A1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
 (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2'),
 (SELECT UserId FROM [User] WHERE Username = N'shv_dung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_minh'), '2026-05-25 08:15:00'),
((SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260608' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-08 07:30:00'),
 (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
 (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1'),
 (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
 (SELECT UserId FROM [User] WHERE Username = N'exam_minh'), '2026-05-25 08:20:00');
GO

-- ============================================
-- 16. THIẾT BỊ THI (A1/A: Máy tính + Mô tô; B1: Máy tính + Xe con)
-- ============================================
INSERT INTO ExamDevice (DeviceName, DeviceType, IsActive, ExamAreaId) VALUES
(N'MT-LT-01', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-02', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-03', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-04', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-05', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-06', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-07', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-08', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-09', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-10', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')),
(N'MT-LT-11', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT-LT-12', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT-LT-13', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT-LT-14', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'MT-LT-15', N'Máy tính', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')),
(N'XM-A1-01', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô')),
(N'XM-A1-02', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô')),
(N'XM-A1-03', N'Mô tô', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô')),
(N'XM-A1-DP', N'Mô tô', 0, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô')),
(N'OTO-B1-01', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô B1')),
(N'OTO-B1-02', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô B1')),
(N'OTO-B1-03', N'Xe con', 1, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô B1')),
(N'OTO-B1-DP', N'Xe con', 0, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi ô tô B1'));
GO

-- ============================================
-- 16. DANH MỤC LOẠI PHÍ
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
-- 17. BIỂU PHÍ THEO HẠNG (Licence_Fee)
-- Mức thu tham chiếu biểu phí đào tạo/sát hạch phổ biến tại trung tâm
-- ============================================
-- Phí chung (LicenceId NULL)
INSERT INTO Licence_Fee (LicenceId, FeeId, Amount) VALUES
(NULL, (SELECT FeeId FROM Fee WHERE FeeName = N'Phí xét hồ sơ và in ấn biểu mẫu'), 50000.00),
(NULL, (SELECT FeeId FROM Fee WHERE FeeName = N'Phí dịch vụ hỗ trợ đăng ký trực tuyến'), 30000.00);

-- Hạng A1
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

-- Hạng A
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

-- Hạng B1
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
GO

-- ============================================
-- 18. THÍ SINH (dữ liệu ngày thi – tách biệt Profile)
-- ============================================
INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address,
    TakeTheory, TakeLayout, TakeRoad, TakeNo, ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended) VALUES
(N'001', N'Nguyễn Văn An',       '2000-03-15', N'0989123456', 1, N'001200031501', N'123 Lê Duẩn, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', N'/uploads/candidates/b/001.jpg', 0, 0),
(N'002', N'Trần Thị Bình',       '1995-08-22', N'0912345678', 0, N'001095082201', N'45 Nguyễn Huệ, TP.HCM', 1, 1, 1, 1, N'Thi cấp mới hạng B', N'/uploads/candidates/b/002.jpg', 0, 0),
(N'003', N'Lê Văn Chính',        '1988-11-10', N'0978563412', 1, N'001088111001', N'78 Trần Phú, Đà Nẵng', 1, 1, 1, 2, N'Thi lại lý thuyết', N'/uploads/candidates/b/003.jpg', 0, 0),
(N'046', N'Phạm Minh Đức',       '1999-02-14', N'0908460001', 1, N'001199021401', N'12 Giải Phóng, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', N'/uploads/candidates/b/046.jpg', 0, 0),
(N'048', N'Nguyễn Thị Hoa',      '1997-05-14', N'0911004801', 0, N'001197051401', N'18 Hoàng Hoa Thám, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', N'/uploads/candidates/b/048.jpg', 0, 0),
(N'049', N'Trần Văn Khoa',       '1996-09-03', N'0911004901', 1, N'001196090301', N'72 Cầu Giấy, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', N'/uploads/candidates/b/049.jpg', 0, 0),
(N'123', N'Hoàng Văn Em',        '1990-06-05', N'0901234567', 1, N'001090060501', N'12 Lý Thường Kiệt, Huế', 1, 1, 1, 1, N'Thi cấp mới hạng B', N'/uploads/candidates/b/123.jpg', 0, 0),
(N'456', N'Vũ Thị Phương',       '1998-12-12', N'0967890123', 0, N'001198121201', N'34 Nguyễn Trãi, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', N'/uploads/candidates/b/456.jpg', 0, 0),
(N'010', N'Phạm Thị Dung',       '2002-01-28', N'0934567890', 0, N'001202012801', N'56 Hai Bà Trưng, Hà Nội', 1, 1, 0, 1, N'Thi cấp mới hạng A1', N'/uploads/candidates/a1/010.jpg', 0, 0),
(N'011', N'Đỗ Văn Hải',          '2001-04-20', N'0945678901', 1, N'001201042001', N'90 Lê Lợi, TP.HCM', 1, 1, 0, 1, N'Thi cấp mới hạng A1', N'/uploads/candidates/a1/011.jpg', 0, 0),
(N'012', N'Ngô Thị Kim',          '1999-09-09', N'0923456780', 0, N'001199090901', N'23 Bạch Đằng, Đà Nẵng', 1, 1, 0, 1, N'Thi cấp mới hạng A1', NULL, 0, 0);
GO

-- ============================================
-- 19. GHI DANH CA THI
-- ============================================
INSERT INTO ExamEnrollment (CandidateId, SessionId, SectionStatus, SignaturePrinted, ExamDeviceId) VALUES
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'001' AND FullName = N'Nguyễn Văn An'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Chờ ký', 1, (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-04')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'002' AND FullName = N'Trần Thị Bình'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Chờ ký', 0, (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-01')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'003' AND FullName = N'Lê Văn Chính'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Đang thi', 0, (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-02')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'046' AND FullName = N'Phạm Minh Đức'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Chờ ký', 0, (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-05')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'048' AND FullName = N'Nguyễn Thị Hoa'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Chưa thi', 0, NULL),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'049' AND FullName = N'Trần Văn Khoa'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Chưa thi', 0, NULL),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'123' AND FullName = N'Hoàng Văn Em'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Chưa thi', 0, NULL),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'456' AND FullName = N'Vũ Thị Phương'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'B1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Chờ ký', 0, (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-06')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'010' AND FullName = N'Phạm Thị Dung'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'A1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Đã thi', 1, (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-11')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'011' AND FullName = N'Đỗ Văn Hải'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'A1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Đang thi', 0, (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-12')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'012' AND FullName = N'Ngô Thị Kim'),
 (SELECT s.SessionId FROM [Session] s JOIN Exam e ON e.ExamId = s.ExamId WHERE e.ExamCode = N'A1-20260601' AND s.IsMorningSession = 1 AND s.StartTime = '2026-06-01 07:30:00'), N'Chưa thi', 0, NULL);
GO

-- ============================================
-- 20. THANH TOÁN (lệ phí thi tại quầy thủ tục)
-- ============================================
INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId) VALUES
(N'Hoàn tất', N'Chuyển khoản', N'GD-20260520-001', 565000.00, '2026-05-20 10:15:00',
 (SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'001')),
(N'Hoàn tất', N'Chuyển khoản', N'GD-20260520-002', 565000.00, '2026-05-20 11:00:00',
 (SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'002')),
(N'Hoàn tất', N'Tiền mặt',      N'TM-20260521-001', 565000.00, '2026-05-21 08:30:00',
 (SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'046')),
(N'Chờ thanh toán',   N'Tiền mặt',      N'CHO-20260522-001', 565000.00, NULL,
 (SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'048')),
(N'Hoàn tất', N'Quét mã QR',    N'QR-20260522-001', 550000.00, '2026-05-22 14:20:00',
 (SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'010')),
(N'Hoàn tất', N'Chuyển khoản', N'GD-20260528-048', 565000.00, '2026-05-28 09:00:00',
 (SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'123')),
(N'Hoàn tất', N'Tiền mặt',      N'TM-20260528-049', 565000.00, '2026-05-28 09:30:00',
 (SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'456'));
GO

INSERT INTO Payment_Fee (PaymentId, FeeId) VALUES
(1, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí thi lý thuyết')),
(1, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí thi thực hành trong hình')),
(1, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí thi thực hành trên đường')),
(1, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí cấp GPLX (phôi PET)')),
(2, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí thi lý thuyết')),
(2, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí thi thực hành trong hình')),
(2, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí thi thực hành trên đường')),
(2, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí cấp GPLX (phôi PET)')),
(5, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí thi lý thuyết')),
(5, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí thi thực hành trong hình')),
(5, (SELECT FeeId FROM Fee WHERE FeeName = N'Lệ phí cấp GPLX (phôi PET)'));
GO

-- ============================================
-- 21. BÀI THI LÝ THUYẾT
-- ============================================
INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt) VALUES
((SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'002'),
 (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-01'), '2026-06-01 07:40:00', '2026-06-01 07:58:00'),
((SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'003'),
 (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-02'), '2026-06-01 08:05:00', NULL),
((SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'046'),
 (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-05'), '2026-06-01 07:45:00', '2026-06-01 08:00:00'),
((SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'456'),
 (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-06'), '2026-06-01 07:50:00', '2026-06-01 08:08:00'),
((SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'010'),
 (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-11'), '2026-06-01 07:35:00', '2026-06-01 07:50:00'),
((SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'011'),
 (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'MT-LT-12'), '2026-06-01 07:42:00', NULL);
GO

-- ============================================
-- 22. KẾT QUẢ THI & ĐIỂM
-- ============================================
INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate) VALUES
((SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'001'), 1, '2026-06-01 07:55:00'),
((SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'002'), 0, '2026-06-01 09:05:00'),
((SELECT ec.ExamEnrollmentId FROM ExamEnrollment ec JOIN Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber = N'010'), 1, '2026-06-01 07:55:00');
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES
(1, 1, 92.00),
(2, 1, 25.00),
(3, 1, 88.00);
GO

-- ============================================
-- 23. BẢNG LỖI TRỪ ĐIỂM (theo Phụ lục TT 12/2025/TT-BCA)
-- Mỗi hạng GPLX và mỗi phần thi có danh mục lỗi riêng.
-- IsCritical = 1: lỗi đình chỉ sát hạch.
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
(N'Bánh xe trước và bánh xe sau bên lái phụ không qua vùng giới hạn của hình vệt bánh xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Bánh xe đè vào vạch giới hạn hình sát hạch, cứ quá 05 giây', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Chưa ghép được xe vào nơi đỗ (khi kết thúc bài sát hạch, còn một phần thân xe nằm ngoài khu vực ghép xe)', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe chưa đến vạch dừng quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Dừng xe quá vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Ghép xe không đúng vị trí quy định (toàn bộ thân xe nằm trong khu vực ghép xe nhưng không có tín hiệu báo kết thúc)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật và tắt đèn xi nhan trái kịp thời', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan khi rẽ trái hoặc rẽ phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan phải', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật đèn xi nhan trái khi xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không dừng xe ở vạch dừng quy định', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không qua vạch kết thúc', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi số theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi tốc độ theo quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thay đổi đúng số và đúng tốc độ quy định', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thắt dây an toàn', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt đèn xi nhan trái ở khoảng cách 05 mét sau vạch xuất phát (đèn xanh trên xe tắt)', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe lên vỉa hè', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe quá tốc độ quy định, cứ 3 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Lái xe vi phạm vạch kẻ đường để thiết bị báo không thực hiện đúng trình tự bài thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 20 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây kể từ khi có lệnh xuất phát (đèn xanh trên xe bật sáng) không đi qua vạch xuất phát', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá 30 giây từ khi đèn tín hiệu màu xanh bật sáng không lái xe qua được vạch kết thúc ngã tư', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Quá thời gian 30 giây kể từ khi dừng xe không khởi hành xe qua vạch dừng', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Thời gian thực hiện bài sát hạch, cứ quá 02 phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Tổng thời gian đến bài sát hạch đang thực hiện quá quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Vi phạm tín hiệu đèn điều khiển giao thông (đi qua ngã tư khi đèn tín hiệu màu đỏ)', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị chết máy', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe bị tụt dốc quá 500 mm kể từ khi dừng xe', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xe quá tốc độ quy định, cứ 03 giây', 1.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Xử lý tình huống không hợp lý gây tai nạn', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe sát hạch', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Đi không đúng hình của hạng xe thi', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Điểm sát hạch dưới 80 điểm', 100.00, 1, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Để tốc độ động cơ quá 4000 vòng/phút', 5.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
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
(N'Không phanh dừng xe trong thời gian 3 giây khi có tín hiệu tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật tín hiệu nguy hiểm trên xe trong thời gian 5 giây', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt tín hiệu nguy hiểm trên xe trước khi đi tiếp sau tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thực hiện đúng các thao tác xử lý tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không phanh dừng xe trong thời gian 3 giây khi có tín hiệu tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không bật tín hiệu nguy hiểm trên xe trong thời gian 5 giây', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không tắt tín hiệu nguy hiểm trên xe trước khi đi tiếp sau tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình')),
(N'Không thực hiện đúng các thao tác xử lý tình huống nguy hiểm', 10.00, 0, (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành trong hình'));
GO

-- ============================================
-- 24. NHẬT KÝ HỆ THỐNG
-- ============================================
INSERT INTO Audit (UserId, Action, [Reason], EntityName, EntityId, OldValue, NewValue, Details, CreatedAt) VALUES
((SELECT UserId FROM [User] WHERE Username = N'Quản trị viên'), N'Cập nhật', N'Phúc khảo theo đơn của thí sinh',
 N'Kết quả thi', N'1', N'28/30', N'30/30', N'Điều chỉnh điểm lý thuyết sau phúc khảo', '2026-05-18 09:15:22'),
(NULL, N'Cập nhật', N'Theo lịch vận hành ca thi',
 N'Ca thi', N'1', N'Chưa diễn ra', N'Đang diễn ra', N'Tự động mở ca sáng - Lý thuyết B', '2026-06-01 07:25:00'),
((SELECT UserId FROM [User] WHERE Username = N'shv_tung'), N'Cập nhật', N'Vi phạm quy chế phòng thi',
 N'Thí sinh', N'456', N'Bình thường', N'Vi phạm', N'Thí sinh mang điện thoại vào phòng thi', '2026-06-01 08:12:11'),
((SELECT UserId FROM [User] WHERE Username = N'qly123'), N'Cập nhật', N'Duyệt hồ sơ đăng ký',
 N'Hồ sơ', N'8', N'Chờ duyệt', N'Duyệt', N'Hồ sơ đủ điều kiện sức khỏe hạng B', '2026-05-15 15:30:00'),
((SELECT UserId FROM [User] WHERE Username = N'shv_tung'), N'Cập nhật', N'Chấm lại theo biên bản',
 N'Kết quả thi', N'2', N'25/30', N'27/30', N'Rà soát lại đáp án trắc nghiệm', '2026-06-01 09:20:00'),
((SELECT UserId FROM [User] WHERE Username = N'exam_hoa'), N'Thêm', N'Phân công ca thi',
 N'Phân công sát hạch viên', N'1', NULL, N'shv_tung', N'Gán sát hạch viên ca lý thuyết B', '2026-05-25 08:00:00');
GO


INSERT INTO QuestionCategory (CategoryName, Description) VALUES
(N'Chương I', N'Quy định chung và quy tắc giao thông đường bộ'),
(N'Chương II', N'Văn hóa giao thông, đạo đức người lái xe, kỹ năng PCCC và cứu nạn'),
(N'Chương III', N'Kỹ thuật lái xe'),
(N'Chương IV', N'Cấu tạo và sửa chữa thông thường'),
(N'Chương V', N'Báo hiệu đường bộ'),
(N'Chương VI', N'Giải thế sa hình và xử lý tình huống giao thông');

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

-- ============================================
-- 27. SEED TEST – SBD 001 đề lý thuyết ngẫu nhiên (in đề + chuyển queue)
-- Chạy lại riêng: seed_sbd001_theory_test.sql
-- ============================================
DECLARE @Sbd001EnrollmentId INT;
DECLARE @Sbd001PaperId INT;
DECLARE @Sbd001DeviceId INT;

SELECT
    @Sbd001EnrollmentId = ec.ExamEnrollmentId,
    @Sbd001DeviceId = ec.ExamDeviceId
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'001'
  AND EXISTS (
    SELECT 1 FROM [Session] s
    JOIN Exam e ON e.ExamId = s.ExamId
    WHERE s.SessionId = ec.SessionId
      AND e.ExamCode = N'B1-20260601'
      AND s.IsMorningSession = 1
      AND s.StartTime = '2026-06-01 07:30:00'
  );

DELETE ca
FROM CandidateAnswer ca
JOIN TheoryPaper tp ON tp.TheoryPaperId = ca.TheoryPaperId
WHERE tp.ExamEnrollmentId = @Sbd001EnrollmentId;

DELETE FROM TheoryPaper WHERE ExamEnrollmentId = @Sbd001EnrollmentId;

INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
VALUES (@Sbd001EnrollmentId, @Sbd001DeviceId, '2026-06-01 07:35:00', '2026-06-01 07:52:00');

SET @Sbd001PaperId = SCOPE_IDENTITY();

INSERT INTO CandidateAnswer (TheoryPaperId, QuestionId, Answer)
SELECT
    @Sbd001PaperId,
    picked.QuestionId,
    CASE
        WHEN ABS(CHECKSUM(NEWID())) % 10 < 8 THEN picked.CorrectAnswer
        ELSE CASE ABS(CHECKSUM(NEWID())) % 4
            WHEN 0 THEN N'A' WHEN 1 THEN N'B' WHEN 2 THEN N'C' ELSE N'D'
        END
    END
FROM (
    SELECT TOP 35 q.QuestionId, q.CorrectAnswer
    FROM Question q
    INNER JOIN Licence_Question lq ON lq.QuestionId = q.QuestionId
    INNER JOIN Licence l ON l.LicenceId = lq.LicenceId AND l.LicenceClass = N'B1'
    ORDER BY NEWID()
) picked;
GO