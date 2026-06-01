-- ============================================
-- DML SAMPLE DATA – DLEM_DB
-- Driving License Examination Management System
-- ============================================

USE DLEM_DB;
GO

-- ============================================
-- 1. ROLES
-- ============================================
INSERT INTO Role (roleName) VALUES
('Admin'),
('Examiner'),
('ManagingStaff'),
('ExamStaff'),
('Candidate'),
('Registrant');

-- ============================================
-- 2. PERSON (Registrants & Walk‑in Candidates)
-- ============================================
INSERT INTO Person (govIdNo, fullName, dateOfBirth, gender, phoneNo, email, address, isWalkIn, approvalStatus) VALUES
('001203012345', N'Nguyễn Văn An',   '2000-03-15', 0, '0989123456', 'an.nguyen@email.com',   N'123 Lê Duẩn, Hà Nội',         0, 'Approved'),
('001203012346', N'Trần Thị Bình',   '1995-08-22', 1, '0912345678', 'binh.tran@email.com',   N'45 Nguyễn Huệ, TP.HCM',       0, 'Approved'),
('001203012347', N'Lê Văn Chính',    '1988-11-10', 0, '0978563412', 'chinh.le@email.com',    N'78 Trần Phú, Đà Nẵng',        0, 'Pending'),
('001203012348', N'Phạm Thị Dung',   '2002-01-28', 1, '0934567890', 'dung.pham@email.com',   N'56 Hai Bà Trưng, Hà Nội',     0, 'Approved'),
('001203012349', N'Hoàng Văn Em',    '1990-06-05', 0, '0901234567', 'em.hoang@email.com',    N'12 Lý Thường Kiệt, Huế',      0, 'Approved'),
('001203012350', N'Vũ Thị Phương',   '1998-12-12', 1, '0967890123', 'phuong.vu@email.com',   N'34 Nguyễn Trãi, Hà Nội',      1, 'Pending'),
('001203012351', N'Đỗ Văn Hải',      '2001-04-20', 0, '0945678901', 'hai.do@email.com',      N'90 Lê Lợi, TP.HCM',           1, 'Pending'),
('001203012352', N'Ngô Thị Kim',     '1999-09-09', 1, '0923456780', 'kim.ngo@email.com',     N'23 Bạch Đằng, Đà Nẵng',       0, 'Pending'),
('001203012353', N'Bùi Văn Long',    '1985-03-30', 0, '0888123456', 'long.bui@email.com',    N'67 Điện Biên Phủ, Hà Nội',    0, 'Pending');

-- ============================================
-- 3. USERS (System Accounts)
-- (Default password for all seeded accounts: admin123)
-- ============================================
INSERT INTO [User] (personId, username, passwordHash, roleId) VALUES
(1, 'admin123',       '$2b$12$LJ3m4ys3uI0FXm0eVMHhOeUfeOtVXoV.GMfLmZR4qK7pHj3hHLp5y', 1),
(2, 'shv_abc',   '$2b$12$LJ3m4ys3uI0FXm0eVMHhOeUfeOtVXoV.GMfLmZR4qK7pHj3hHLp5y', 2),
(3, 'shv_123',   '$2b$12$LJ3m4ys3uI0FXm0eVMHhOeUfeOtVXoV.GMfLmZR4qK7pHj3hHLp5y', 2),
(4, 'cbql_123','$2b$12$LJ3m4ys3uI0FXm0eVMHhOeUfeOtVXoV.GMfLmZR4qK7pHj3hHLp5y', 3),
(5, 'cbkt_111',  '$2b$12$LJ3m4ys3uI0FXm0eVMHhOeUfeOtVXoV.GMfLmZR4qK7pHj3hHLp5y', 4),
(6, 'cbkt_222',  '$2b$12$LJ3m4ys3uI0FXm0eVMHhOeUfeOtVXoV.GMfLmZR4qK7pHj3hHLp5y', 4),
(7,  'user123', '$2b$12$LJ3m4ys3uI0FXm0eVMHhOeUfeOtVXoV.GMfLmZR4qK7pHj3hHLp5y', 6),
(8,  'nguyenVan_An12', '$2b$12$LJ3m4ys3uI0FXm0eVMHhOeUfeOtVXoV.GMfLmZR4qK7pHj3hHLp5y', 6),
(9,  'us_hello', '$2b$12$LJ3m4ys3uI0FXm0eVMHhOeUfeOtVXoV.GMfLmZR4qK7pHj3hHLp5y', 6);

-- ============================================
-- 5. Candidate DOCUMENTS
-- ============================================
INSERT INTO CandidateDocument (personId, documentType, documentUrl, expiryDate) VALUES
(1, 'ID_Card', '/docs/id/front_001.jpg', '2030-01-15'),
(1, 'ID_Card', '/docs/id/back_001.jpg',  '2030-01-15'),
(2, 'ID_Card', '/docs/id/front_002.jpg', '2029-06-10'),
(2, 'ID_Card', '/docs/id/back_002.jpg',  '2029-06-10'),
(3, 'ID_Card', '/docs/id/front_003.jpg', '2028-03-20'),
(3, 'ID_Card', '/docs/id/back_003.jpg',  '2028-03-20'),
(4, 'ID_Card', '/docs/id/front_004.jpg', '2032-07-01'),
(1, 'Health_Cert', '/docs/health/health_001.pdf', NULL),
(2, 'Health_Cert', '/docs/health/health_002.pdf', NULL),
(3, 'Health_Cert', '/docs/health/health_003.pdf', NULL),
(5, 'Health_Cert', '/docs/health/health_005.pdf', NULL),
(2, 'License_Copy', '/docs/license/license_002.jpg', NULL),
(5, 'License_Copy', '/docs/license/license_005.jpg', NULL);

-- ============================================
-- 6. LICENSE TYPES
-- ============================================
INSERT INTO LicenseType (licenseCode, minAge, hasTheory, hasPractical, hasRoadLayout, hasOnRoad, durationYears) VALUES
('A1', 18, 1, 1, 0, 0, 0),
('A', 18, 1, 1, 0, 0, 0),
('B1',     18, 1, 1, 1, 1, 0),
('B',      18, 1, 1, 1, 1, 10),
('C1',     21, 1, 1, 1, 1, 10),
('C',      21, 1, 1, 1, 1, 5),
('D1',     24, 1, 1, 1, 1, 5),
('D2',     24, 1, 1, 1, 1, 5),
('D',      24, 1, 1, 1, 1, 5);

-- ============================================
-- 7. EXAM TYPES
-- ============================================
INSERT INTO ExamType (typeName) VALUES
('Theory'),
('Practical'),
('RoadLayout'),
('OnRoad');

-- ============================================
-- 8. EXAM SECTIONS (per license × exam type)
-- ============================================
INSERT INTO ExamSection (examTypeId, licenseTypeId, timeLimitMinutes, examFee) VALUES
-- A1
(1, 1, 19,  60000),
(2, 1, NULL, 70000),
-- A
(1, 2, 19,  60000),
(2, 2, NULL, 70000),
-- B1
(1, 3, 20,  60000),
(2, 3, 18,  70000),
-- B
(1, 4, 20,  100000),
(3, 4, 15,  250000),
(4, 4, 30,  80000),  -- OnRoad
-- C1
(1, 5, 20,  100000),
(3, 5, 15,  250000),
(4, 5, 30,  80000),  -- OnRoad
-- C
(1, 6, 20,  100000),
(3, 6, 15,  250000),
(4, 6, 30,  80000);  -- OnRoad

-- ============================================
-- 9. EXAM AREAS
-- ============================================
INSERT INTO ExamArea (areaName, areaType, capacity, location) VALUES
('Phòng LT 1',     'Room',    10, N'Tầng 2, Toà B'),
('Phòng LT 2',     'Room',    10, N'Tầng 2, Toà B'),
('Sân thi A1',    'Ground',  15, N'Sân thi 1'),
('Sân thi A',    'Ground',  15, N'Sân thi 1'),
('Sân thi Ô tô 1',  'Ground',   10, N'Sân thi 2');

-- ============================================
-- 10. EXAM COMPUTERS (for Theory rooms)
-- ============================================
INSERT INTO ExamComputer (computerCode, areaId, status) VALUES
('PC-LT1-01', 1, 'Available'),
('PC-LT1-02', 1, 'Available'),
('PC-LT1-03', 1, 'Available'),
('PC-LT1-04', 1, 'Available'),
('PC-LT1-05', 1, 'Available'),
('PC-LT1-06', 1, 'Available'),
('PC-LT1-07', 1, 'Available'),
('PC-LT1-08', 1, 'Available'),
('PC-LT1-09', 1, 'Available'),
('PC-LT1-10', 1, 'Available'),
('PC-LT2-01', 2, 'Available'),
('PC-LT2-02', 2, 'Available'),
('PC-LT2-03', 2, 'Maintenance'),
('PC-LT2-04', 2, 'Available'),
('PC-LT2-05', 2, 'Available');

-- ============================================
-- 11. EXAM SESSIONS
-- ============================================
INSERT INTO ExamSession (sessionName, licenseTypeId, examTypeId, examDate, shiftStartTime, shiftEndTime, areaId, status, maxCandidates, registeredCount) VALUES
-- A1 Theory - Morning
('A1-LT-SANG-01',    1, 1, '2026-06-01', '07:30', '09:00',  1, 'Scheduled', 10, 3),
-- A1 Practical - Morning
('A1-TH-SANG-01',    1, 2, '2026-06-01', '09:30', '11:30',  3, 'Scheduled', 10, 3),
-- B Theory - Morning
('B-LT-SANG-01',     4, 1, '2026-06-01', '07:30', '09:00',  2, 'Scheduled', 10, 2),
-- B Layout - Morning
('B-SH-SANG-01',     4, 3, '2026-06-01', '09:30', '11:30',  4, 'Scheduled',  5, 2),
-- B OnRoad - Afternoon
('B-DT-CHIEU-01',    4, 4, '2026-06-01', '13:00', '16:00',  5, 'Scheduled',  3, 2),
-- C1 Theory - Afternoon
('C1-LT-CHIEU-01',   5, 1, '2026-06-01', '13:00', '15:00',  1, 'Scheduled', 10, 1),
-- C1 Layout - Afternoon
('C1-SH-CHIEU-01',   5, 3, '2026-06-01', '15:30', '17:30',  4, 'Scheduled',  5, 1);

-- ============================================
-- 12. EXAM REGISTRATIONS
-- ============================================
INSERT INTO ExamRegistration (examSessionId, personId, candidateNo, registrationType, isPaymentCompleted, isPresent) VALUES
-- A1 Theory (session 1)
(1, 1, 1, 'PreRegistered', 1, 0),
(1, 4, 2, 'PreRegistered', 1, 0),
(1, 6, 3, 'WalkIn',        1, 0),
-- A1 Practical (session 2)
(2, 1, 1, 'PreRegistered', 1, 0),
(2, 4, 2, 'PreRegistered', 1, 0),
(2, 6, 3, 'WalkIn',        1, 0),
-- B Theory (session 3)
(3, 2, 1, 'PreRegistered', 1, 0),
(3, 5, 2, 'PreRegistered', 1, 0),
-- B Practical (session 4)
(4, 2, 1, 'PreRegistered', 1, 0),
(4, 5, 2, 'PreRegistered', 1, 0),
-- B OnRoad (session 5)
(5, 2, 1, 'PreRegistered', 1, 0),
(5, 5, 2, 'PreRegistered', 1, 0),
-- C1 Theory (session 6)
(6, 3, 1, 'PreRegistered', 1, 0),
-- C1 Practical (session 7)
(7, 3, 1, 'PreRegistered', 1, 0);

-- ============================================
-- 13. PAYMENTS (Fixed - Full payment per license type)
-- ============================================
INSERT INTO Payment (examRegistrationId, amount, paymentStatus, paymentMethod, transactionReference) VALUES
(1, 130000, 'Completed', 'BankTransfer', 'TXN-20260520-001'),
(2, 130000, 'Completed', 'BankTransfer', 'TXN-20260520-002'),
(3, 130000, 'Completed', 'Cash',         NULL),
(4, 130000, 'Completed', 'BankTransfer', 'TXN-20260520-003'),
(5, 130000, 'Completed', 'Cash',         NULL),
(6, 130000, 'Completed', 'Cash',         NULL),
(7, 430000, 'Completed', 'BankTransfer', 'TXN-20260521-001'),
(8, 430000, 'Completed', 'BankTransfer', 'TXN-20260521-002'),
(9, 430000, 'Completed', 'BankTransfer', 'TXN-20260521-003'),
(10, 430000, 'Completed', 'BankTransfer', 'TXN-20260521-004'),
(11, 430000, 'Completed', 'Cash',        NULL),
(12, 430000, 'Pending',   'Cash',        NULL),
(13, 430000, 'Completed', 'BankTransfer', 'TXN-20260522-001'),
(14, 430000, 'Pending',   'Cash',        NULL);

-- ============================================
-- 14. Candidate CALLS (Exam day)
-- ============================================
INSERT INTO CandidateCall (examSessionId, candidateNo, calledTo, calledBy, result) VALUES
(1, 1, N'Phòng làm thủ tục', 5, N'Có mặt'),
(1, 2, N'Phòng làm thủ tục', 5, N'Có mặt'),
(1, 3, N'Phòng làm thủ tục', 5, N'Có mặt'),
(3, 1, N'Phòng làm thủ tục', 6, N'Có mặt'),
(3, 2, N'Phòng làm thủ tục', 6, N'Vắng mặt - Chuyển cuối danh sách'),
(3, 2, N'Phòng làm thủ tục', 6, N'Có mặt (lần 2)');

-- ============================================
-- 15. AUDIT LOG (Sample entries)
-- ============================================
INSERT INTO AuditLog (tableName, recordId, action, oldValue, newValue, changedBy) VALUES
('CandidateDocument', 1,  'UPDATE', '{"status":"Pending"}', '{"status":"Approved"}', 4),
('CandidateDocument', 3,  'UPDATE', '{"status":"Pending"}', '{"status":"Approved"}', 4),
('CandidateDocument', 7,  'UPDATE', '{"status":"Pending"}', '{"status":"Approved"}', 4),
('ExamSession',       3,  'UPDATE', '{"status":"Scheduled"}', '{"status":"Open"}', 5),
('ExamSession',       1,  'UPDATE', '{"status":"Scheduled"}', '{"status":"Open"}', 5);
