-- ============================================
-- DML SAMPLE DATA – DLEM_DB_2
-- Driving License Examination Management System
-- Default password for seeded accounts: login123
-- ============================================

USE DLEM_DB_2;
GO

-- Clear existing seed data
DELETE FROM Score_Deduction;
DELETE FROM ScoreDeduction;
DELETE FROM ExamScore;
DELETE FROM ExamResult;
DELETE FROM CandidateAnswer;
DELETE FROM TheoryPaper;
DELETE FROM Exam_Candidate;
DELETE FROM Payment_Fee;
DELETE FROM Payment;
DELETE FROM Candidate;
DELETE FROM ExamRegistration;
DELETE FROM Document;
DELETE FROM Profile;
DELETE FROM Session_ExamArea;
DELETE FROM Session_ExamSection;
DELETE FROM Licence_ExamSection;
DELETE FROM Session_Examiner;
DELETE FROM ExamDevice;
DELETE FROM [Session];
DELETE FROM Exam;
DELETE FROM ExamArea;
DELETE FROM ExamSection;
DELETE FROM Fee;
DELETE FROM Licence;
DELETE FROM Audit;
DELETE FROM [User];
GO

-- ============================================
-- 1. USERS
-- ============================================
INSERT INTO [User] (Username, Email, PasswordHash, [Role], [Status]) VALUES
(N'admin',           N'admin@laivui.vn',           N'login123', N'Admin',          1),
(N'examiner_tung',   N'tung.nguyen@pc08a.com',   N'login123', N'Examiner',       1),
(N'examiner_lan',    N'lan.tran@pc08a.com',      N'login123', N'Examiner',       1),
(N'manager_dung',   N'dung.pham@laivui.vn',       N'login123', N'ManagingStaff',  1),
(N'examstaff_hoa',  N'hoa.le@laivui.vn',          N'login123', N'ExamStaff',      1),
(N'examstaff_minh', N'minh.vu@laivui.vn',         N'login123', N'ExamStaff',      1),
(N'an.nguyen',       N'an.nguyen@gmail.com',       N'login123', N'Registrant',     1),
(N'binh.tran',       N'binh.tran@gmail.com',       N'login123', N'Registrant',     1),
(N'chinh.le',        N'chinh.le@gmail.com',        N'login123', N'Registrant',     1),
(N'dung.pham',       N'dung.pham@gmail.com',       N'login123', N'Registrant',     1),
(N'em.hoang',        N'em.hoang@gmail.com',        N'login123', N'Registrant',     1),
(N'phuong.vu',       N'phuong.vu@gmail.com',       N'login123', N'Registrant',     1),
(N'hai.do',          N'hai.do@gmail.com',          N'login123', N'Registrant',     1),
(N'kim.ngo',         N'kim.ngo@gmail.com',         N'login123', N'Registrant',     1),
(N'long.bui',        N'long.bui@gmail.com',        N'login123', N'Registrant',     0);
GO

-- ============================================
-- 2. PROFILES
-- ============================================
INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId) VALUES
(N'Quản trị viên hệ thống', '1985-01-10', N'0900000001', N'Nam', N'001085000001', N'Trung tâm Lái Vui, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'admin')),
(N'Nguyễn Văn Tùng',         '1988-06-15', N'0911223344', N'Nam', N'001088061501', N'12 Phạm Hùng, Hà Nội',     (SELECT UserId FROM [User] WHERE Username = N'examiner_tung')),
(N'Trần Thị Lan',            '1990-03-22', N'0922334455', N'Nữ', N'001090032201', N'45 Lê Văn Lương, Hà Nội',  (SELECT UserId FROM [User] WHERE Username = N'examiner_lan')),
(N'Phạm Thị Dung',           '1992-08-08', N'0933445566', N'Nữ', N'001092080801', N'56 Hai Bà Trưng, Hà Nội',  (SELECT UserId FROM [User] WHERE Username = N'manager_dung')),
(N'Lê Văn Hòa',              '1991-11-11', N'0944556677', N'Nam', N'001091111101', N'78 Trần Phú, Đà Nẵng',     (SELECT UserId FROM [User] WHERE Username = N'examstaff_hoa')),
(N'Vũ Minh Khang',            '1993-04-04', N'0955667788', N'Nam', N'001093040401', N'34 Nguyễn Trãi, Hà Nội',   (SELECT UserId FROM [User] WHERE Username = N'examstaff_minh')),
(N'Nguyễn Văn An',           '2000-03-15', N'0989123456', N'Nam', N'001203012345', N'123 Lê Duẩn, Hà Nội',      (SELECT UserId FROM [User] WHERE Username = N'an.nguyen')),
(N'Trần Thị Bình',           '1995-08-22', N'0912345678', N'Nữ', N'001203012346', N'45 Nguyễn Huệ, TP.HCM',    (SELECT UserId FROM [User] WHERE Username = N'binh.tran')),
(N'Lê Văn Chính',            '1988-11-10', N'0978563412', N'Nam', N'001203012347', N'78 Trần Phú, Đà Nẵng',     (SELECT UserId FROM [User] WHERE Username = N'chinh.le')),
(N'Phạm Thị Dung',           '2002-01-28', N'0934567890', N'Nữ', N'001203012348', N'56 Hai Bà Trưng, Hà Nội',  (SELECT UserId FROM [User] WHERE Username = N'dung.pham')),
(N'Hoàng Văn Em',            '1990-06-05', N'0901234567', N'Nam', N'001203012349', N'12 Lý Thường Kiệt, Huế',   (SELECT UserId FROM [User] WHERE Username = N'em.hoang')),
(N'Vũ Thị Phương',           '1998-12-12', N'0967890123', N'Nữ', N'001203012350', N'34 Nguyễn Trãi, Hà Nội',   (SELECT UserId FROM [User] WHERE Username = N'phuong.vu')),
(N'Đỗ Văn Hải',              '2001-04-20', N'0945678901', N'Nam', N'001203012351', N'90 Lê Lợi, TP.HCM',        (SELECT UserId FROM [User] WHERE Username = N'hai.do')),
(N'Ngô Thị Kim',             '1999-09-09', N'0923456780', N'Nữ', N'001203012352', N'23 Bạch Đằng, Đà Nẵng',    (SELECT UserId FROM [User] WHERE Username = N'kim.ngo')),
(N'Bùi Văn Long',             '1985-03-30', N'0888123456', N'Nam', N'001203012353', N'67 Điện Biên Phủ, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'long.bui'));
GO

-- ============================================
-- 3. DOCUMENTS
-- ============================================
INSERT INTO Document (DocumentType, DocumentUrl, ProfileId) VALUES
(N'CCCD',         N'/docs/id/front_001.jpg', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012345')),
(N'CCCD',         N'/docs/id/back_001.jpg',  (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012345')),
(N'Giấy khám SK', N'/docs/health/health_001.pdf', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012345')),
(N'CCCD',         N'/docs/id/front_002.jpg', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012346')),
(N'CCCD',         N'/docs/id/back_002.jpg',  (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012346')),
(N'Giấy khám SK', N'/docs/health/health_002.pdf', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012346')),
(N'CCCD',         N'/docs/id/front_003.jpg', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012347')),
(N'GPLX',         N'/docs/license/license_005.jpg', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012349'));
GO

-- ============================================
-- 4. LICENCES
-- ============================================
INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId) VALUES
(N'A1',  N'Xe mô tô hai bánh có dung tích xi-lanh đến 125 cm³', 18, 0,  NULL),
(N'A',   N'Xe mô tô hai bánh có dung tích xi-lanh trên 125 cm³', 18, 0,  NULL),
(N'B1',  N'Ô tô số tự động tải trọng dưới 3.500 kg',             18, 0,  NULL),
(N'B',   N'Ô tô tải trọng dưới 3.500 kg, tối đa 9 chỗ',          18, 10, NULL),
(N'C1',  N'Ô tô tải trọng từ 3.500 kg đến 7.500 kg',            21, 10, NULL),
(N'C',   N'Ô tô tải trọng trên 7.500 kg',                        21, 5,  NULL),
(N'D1',  N'Xe khách từ 10 đến 16 chỗ',                           24, 5,  NULL),
(N'D2',  N'Xe khách từ 17 đến 29 chỗ',                           24, 5,  NULL),
(N'D',   N'Xe khách trên 30 chỗ',                                24, 5,  NULL);
GO

-- ============================================
-- 5. EXAM REGISTRATIONS
-- ============================================
INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId) VALUES
(N'Approved', N'', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012345'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'Approved', N'', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012346'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'Pending',  N'Chờ duyệt hồ sơ',    (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012347'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1')),
(N'Approved', N'', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012348'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')),
(N'Approved', N'',          (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012349'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'Approved', N'',   (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012350'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')),
(N'Approved', N'', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012351'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'Pending',  NULL,                  (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012352'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')),
(N'Rejected', N'Không đủ yêu cầu sức khoẻ',         (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012353'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'));
GO

-- ============================================
-- 6. EXAMS
-- ============================================
INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId) VALUES
(N'EX-B-20260601', '2026-06-01 07:00:00', N'Trung tâm Sát hạch Lái Vui - Hà Nội', N'Open',       (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'EX-B-20260615', '2026-06-15 07:00:00', N'Trung tâm Sát hạch ABC123 - Hà Nội', N'Scheduled',  (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'EX-A1-20260601','2026-06-01 07:00:00', N'Trung tâm Sát hạch ABC234 - Hà Nội', N'Open',       (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')),
(N'EX-C1-20260601','2026-06-01 13:00:00', N'Trung tâm Sát hạch ABC234 - Hưng Yên', N'Scheduled',  (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1')),
(N'EX-B1-20260608','2026-06-08 07:00:00', N'Trung tâm Sát hạch ABC345 - Đà Nẵng', N'Scheduled', (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1'));
GO

-- ============================================
-- 7. EXAM SECTIONS
-- ============================================
INSERT INTO ExamSection (SectionName) VALUES
(N'Lý thuyết'),
(N'Sa hình'),
(N'Đường trường'),
(N'Thực hành');
GO

-- ============================================
-- 7b. LICENCE ↔ EXAM SECTION (duration per licence class)
-- ============================================
INSERT INTO Licence_ExamSection (LicenceId, ExamSectionId, DurationMinutes)
SELECT l.LicenceId, es.ExamSectionId, v.DurationMinutes
FROM Licence l
JOIN (VALUES
    (N'A1',  N'Lý thuyết',    19),
    (N'A1',  N'Thực hành',    NULL),
    (N'A',   N'Lý thuyết',    19),
    (N'A',   N'Thực hành',    NULL),
    (N'B1',  N'Lý thuyết',    20),
    (N'B1',  N'Thực hành',    18),
    (N'B',   N'Lý thuyết',    20),
    (N'B',   N'Sa hình',      15),
    (N'B',   N'Đường trường', 30),
    (N'C1',  N'Lý thuyết',    20),
    (N'C1',  N'Sa hình',      15),
    (N'C1',  N'Đường trường', 30),
    (N'C',   N'Lý thuyết',    20),
    (N'C',   N'Sa hình',      15),
    (N'C',   N'Đường trường', 30)
) v(LicenceClass, SectionName, DurationMinutes) ON l.LicenceClass = v.LicenceClass
JOIN ExamSection es ON es.SectionName = v.SectionName;
GO

-- ============================================
-- 8. SESSIONS
-- ============================================
INSERT INTO [Session] (SessionName, StartTime, EndTime, [Status], ExamId) VALUES
(N'Ca sáng - Lý thuyết B',  '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'Open',       (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Ca sáng - Sa hình B',    '2026-06-01 09:30:00', '2026-06-01 11:30:00', N'Scheduled',  (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Ca chiều - Đường trường B','2026-06-01 13:00:00','2026-06-01 16:00:00', N'Scheduled', (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Ca sáng - Lý thuyết A1', '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'Open',       (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-A1-20260601')),
(N'Ca sáng - Lý thuyết B2', '2026-06-15 07:30:00', '2026-06-15 09:00:00', N'Scheduled',  (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260615'));
GO

-- ============================================
-- 9. SESSION EXAMINERS
-- ============================================
INSERT INTO Session_Examiner (SessionId, ExaminerId) VALUES
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B'),  (SELECT UserId FROM [User] WHERE Username = N'examiner_tung')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Sa hình B'),    (SELECT UserId FROM [User] WHERE Username = N'examiner_tung')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca chiều - Đường trường B'),(SELECT UserId FROM [User] WHERE Username = N'examiner_lan')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết A1'),  (SELECT UserId FROM [User] WHERE Username = N'examiner_lan')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B2'), (SELECT UserId FROM [User] WHERE Username = N'examiner_tung'));
GO

-- ============================================
-- 10. SESSION EXAM SECTIONS
-- ============================================
INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B'),   (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Sa hình B'),     (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca chiều - Đường trường B'),(SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết A1'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B2'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'));
GO

-- ============================================
-- 11. EXAM AREAS
-- ============================================
INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location]) VALUES
(N'Phòng LT 1',      N'Room',   10, N'Tầng 2, Toà B'),
(N'Phòng LT 2',      N'Room',   10, N'Tầng 2, Toà B'),
(N'Sân thi A1',      N'Ground', 15, N'Sân thi 1'),
(N'Sân thi Ô tô 1',  N'Ground', 10, N'Sân thi 2'),
GO

-- ============================================
-- 12. SESSION EXAM AREAS
-- ============================================
INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B'),    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết A1'),   (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Sa hình B'),      (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca chiều - Đường trường B'),(SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1'));
GO

-- ============================================
-- 13. EXAM DEVICES
-- ============================================
INSERT INTO ExamDevice (DeviceName, DeviceType, [Status], ExamAreaId) VALUES
(N'PC-LT1-01', N'Computer', N'Available',    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1')),
(N'PC-LT1-02', N'Computer', N'Available',    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1')),
(N'PC-LT1-03', N'Computer', N'Available',    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1')),
(N'PC-LT1-04', N'Computer', N'InUse',        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1')),
(N'PC-LT2-01', N'Computer', N'Available',    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2')),
(N'PC-LT2-02', N'Computer', N'Maintenance',  (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2'));
GO

-- ============================================
-- 14. FEES
-- ============================================
INSERT INTO Fee (FeeName, FeeType, Amount, IsActive) VALUES
(N'Lệ phí thi lý thuyết',  N'Exam',      100000.00, 1),
(N'Lệ phí thi sa hình',    N'Exam',      250000.00, 1),
(N'Lệ phí thi đường trường',N'Exam',      80000.00,  1),
(N'Phí hồ sơ',              N'Admin',      50000.00,  1),
(N'Phí cấp GPLX',           N'License',   135000.00,  1);
GO

-- ============================================
-- 15. CANDIDATES (exam-day records)
-- ============================================
INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, TakeTheory, TakePractical, TakeRoadLayout, TakeOnRoad, ReasonForTaking, PhotoImageUrl, UserId, ExamRegistrationId) VALUES
-- B: lý thuyết bảo lưu, chỉ thi sa hình + đường trường
(N'045',    N'Nguyễn Văn A',     '1995-08-15', N'0989123456', N'Nam', N'079012345678', N'123 Nguyễn Văn Linh, P. Tân Phong, Q.7, TP.HCM', 0, NULL, 1, 1, N'Thi lại vì trượt sa hình', NULL, (SELECT UserId FROM [User] WHERE Username = N'an.nguyen'),  (SELECT ExamRegistrationId FROM ExamRegistration er JOIN Profile p ON er.ProfileId = p.ProfileId WHERE p.GovernmentIdNumber = N'001203012345')),
-- B: thi lần đầu (đủ 3 phần)
(N'046',    N'Trần Thị Bình',    '1995-08-22', N'0912345678', N'Nữ',  N'079012345679', N'45 Nguyễn Huệ, Q.1, TP.HCM',                      1, NULL, 1, 1, N'Thi lần đầu',              NULL, (SELECT UserId FROM [User] WHERE Username = N'binh.tran'), (SELECT ExamRegistrationId FROM ExamRegistration er JOIN Profile p ON er.ProfileId = p.ProfileId WHERE p.GovernmentIdNumber = N'001203012346')),
-- C1: thi lần đầu
(N'047',    N'Lê Văn Chính',     '1988-11-10', N'0978563412', N'Nam', N'079012345680', N'78 Trần Phú, Đà Nẵng',                           1, NULL, 1, 1, N'Thi lần đầu',              NULL, (SELECT UserId FROM [User] WHERE Username = N'chinh.le'),  (SELECT ExamRegistrationId FROM ExamRegistration er JOIN Profile p ON er.ProfileId = p.ProfileId WHERE p.GovernmentIdNumber = N'001203012347')),
-- B: trượt lý thuyết → phải thi lại hết
(N'123',N'Nguyễn Văn Quyết', '1992-04-12', N'0909111222', N'Nam', N'031092004581', N'88 Lê Lợi, TP.HCM',                               1, NULL, 1, 1, N'Thi lại vì trượt lý thuyết', NULL, (SELECT UserId FROM [User] WHERE Username = N'em.hoang'), (SELECT ExamRegistrationId FROM ExamRegistration er JOIN Profile p ON er.ProfileId = p.ProfileId WHERE p.GovernmentIdNumber = N'001203012349')),
-- B1: trừ hết điểm → chỉ thi lại lý thuyết
(N'124',N'Nguyễn Văn B',     '1998-02-18', N'0933445566', N'Nam', N'079012345681', N'12 Lý Thường Kiệt, Huế',                          1, 0,    NULL, NULL, N'Thi lại vì trừ hết điểm',  NULL, (SELECT UserId FROM [User] WHERE Username = N'phuong.vu'), (SELECT ExamRegistrationId FROM ExamRegistration er JOIN Profile p ON er.ProfileId = p.ProfileId WHERE p.GovernmentIdNumber = N'001203012350')),
-- B: thi lần đầu
(N'456',N'Phạm Văn Cường',   '1990-07-07', N'0944556677', N'Nam', N'079012345682', N'56 Hai Bà Trưng, Hà Nội',                         1, NULL, 1, 1, N'Thi lần đầu',              NULL, (SELECT UserId FROM [User] WHERE Username = N'hai.do'),    (SELECT ExamRegistrationId FROM ExamRegistration er JOIN Profile p ON er.ProfileId = p.ProfileId WHERE p.GovernmentIdNumber = N'001203012351')),
-- A1: thi lần đầu (lý thuyết + thực hành)
(N'789',N'Hoàng Thị Mai',    '1999-11-30', N'0955667788', N'Nữ',  N'079012345683', N'34 Nguyễn Trãi, Hà Nội',                          1, 1,    NULL, NULL, N'Thi lần đầu',              NULL, (SELECT UserId FROM [User] WHERE Username = N'kim.ngo'),   (SELECT ExamRegistrationId FROM ExamRegistration er JOIN Profile p ON er.ProfileId = p.ProfileId WHERE p.GovernmentIdNumber = N'001203012352'));
GO

-- ============================================
-- 16. EXAM_CANDIDATE (assign candidates to exam sessions)
-- ============================================
INSERT INTO Exam_Candidate (ExamId, CandidateId, SessionId) VALUES
((SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601'), (SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'046'),    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B')),
((SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601'), (SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'123'), (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B')),
((SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601'), (SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'456'),(SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B')),
((SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601'), (SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'045'),    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Sa hình B')),
((SELECT ExamId FROM Exam WHERE ExamCode = N'EX-A1-20260601'),(SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'124'),(SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết A1'));
GO

-- ============================================
-- 17. PAYMENTS
-- ============================================
INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, CandidateId, ExamId) VALUES
(N'Completed', N'BankTransfer', N'TXN-20260520-001', 430000.00, '2026-05-20 10:15:00', (SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'045'),    (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Completed', N'BankTransfer', N'TXN-20260520-002', 430000.00, '2026-05-20 11:00:00', (SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'046'),    (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Completed', N'Cash',         NULL,                430000.00, '2026-05-21 08:30:00', (SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'123'), (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Pending',   N'Cash',         NULL,                430000.00, NULL,                  (SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'456'),(SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Completed', N'BankTransfer', N'TXN-20260522-001', 130000.00, '2026-05-22 14:20:00', (SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'124'),(SELECT ExamId FROM Exam WHERE ExamCode = N'EX-A1-20260601'));
GO

INSERT INTO Payment_Fee (PaymentId, FeeId)
SELECT p.PaymentId, f.FeeId
FROM Payment p
CROSS JOIN Fee f
WHERE p.TransactionReference = N'TXN-20260520-001'
  AND f.FeeName IN (N'Lệ phí thi lý thuyết', N'Phí hồ sơ', N'Lệ phí thi sa hình', N'Lệ phí thi đường trường');
GO

-- ============================================
-- 18. THEORY PAPER (answers seeded in 600_DML_DLEM_DB.sql after questions load)
-- ============================================
INSERT INTO TheoryPaper (ExamCandidateId, ExamDeviceId, StartedAt, SubmittedAt)
SELECT ec.ExamCandidateId,
       (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'PC-LT1-04'),
       '2026-06-01 07:35:00',
       '2026-06-01 07:52:00'
FROM Exam_Candidate ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'046'
  AND s.SessionName = N'Ca sáng - Lý thuyết B';
GO

-- ============================================
-- 19. EXAM RESULTS & SCORES
-- ============================================
INSERT INTO ExamResult (ExamCandidateId, IsPassed, ResultDate)
SELECT ec.ExamCandidateId, 0, '2026-06-01 09:05:00'
FROM Exam_Candidate ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'123'
  AND s.SessionName = N'Ca sáng - Lý thuyết B';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
       25.00
FROM ExamResult er
JOIN Exam_Candidate ec ON er.ExamCandidateId = ec.ExamCandidateId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
WHERE c.CandidateNumber = N'123';
GO

INSERT INTO ExamResult (ExamCandidateId, IsPassed, ResultDate)
SELECT ec.ExamCandidateId, 0, '2026-06-01 11:20:00'
FROM Exam_Candidate ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'045'
  AND s.SessionName = N'Ca sáng - Sa hình B';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'),
       72.00
FROM ExamResult er
JOIN Exam_Candidate ec ON er.ExamCandidateId = ec.ExamCandidateId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
WHERE c.CandidateNumber = N'045';
GO

-- ============================================
-- 20. SCORE DEDUCTIONS
-- ============================================
INSERT INTO ScoreDeduction ([Reason], Points, IsCritical) VALUES
(N'Không bật đèn xi-nhan khi chuyển làn', 1.00, 0),
(N'Vượt quá tốc độ cho phép trong sân thi', 2.00, 0),
(N'Không nhường đường cho người đi bộ', 5.00, 1);
GO

INSERT INTO Score_Deduction (ExamScoreId, ScoreDeductionId)
SELECT es.ExamScoreId, sd.ScoreDeductionId
FROM ExamScore es
JOIN ExamSection sec ON es.ExamSectionId = sec.ExamSectionId
CROSS JOIN ScoreDeduction sd
WHERE sec.SectionName = N'Sa hình'
  AND sd.[Reason] = N'Không bật đèn xi-nhan khi chuyển làn';
GO

-- ============================================
-- 21. AUDIT LOG (matches examiner audit.jsp mock)
-- ============================================
INSERT INTO Audit (UserId, Action, [Reason], EntityName, EntityId, OldValue, NewValue, CreatedAt) VALUES
((SELECT UserId FROM [User] WHERE Username = N'admin'),          N'UPDATE',  N'Phúc khảo',           N'Thí sinh', N'123', N'28/30', N'30/30', '2023-10-25 09:15:22'),
(NULL,                                                           N'SYSTEM',  N'Theo lịch trình',     N'Phòng thi', N'-',        N'Khóa',  N'Mở',    '2023-10-25 07:00:00'),
((SELECT UserId FROM [User] WHERE Username = N'examiner_tung'),  N'WARNING', N'Mang điện thoại',     N'Thí sinh', N'456', N'Bình thường', N'Vi phạm', '2023-10-24 10:45:11'),
((SELECT UserId FROM [User] WHERE Username = N'admin'),          N'DELETE',  N'Trùng CMND',          N'Thí sinh', N'789', N'Tồn tại', N'Đã xóa', '2023-10-24 14:20:05'),
((SELECT UserId FROM [User] WHERE Username = N'admin'),          N'UPDATE',  N'Yêu cầu từ Cục',      N'Thí sinh', N'124', N'Nguyễn Văn A', N'Nguyễn Văn B', '2023-10-23 08:10:00'),
((SELECT UserId FROM [User] WHERE Username = N'examiner_tung'),  N'UPDATE',  N'Chấm sai',            N'Kết quả thi', N'123', N'25/35', N'27/35', '2026-06-01 09:20:00'),
((SELECT UserId FROM [User] WHERE Username = N'manager_dung'),   N'APPROVE', N'Duyệt hồ sơ',         N'ExamRegistration', N'9', N'Pending', N'Approved', '2026-05-18 15:30:00');
GO
