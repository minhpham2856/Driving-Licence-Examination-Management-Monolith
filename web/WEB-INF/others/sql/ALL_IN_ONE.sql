-- Create Database
USE master;
GO

IF EXISTS (SELECT 1 FROM sys.databases WHERE name = 'DLEM_DB_2')
BEGIN
    ALTER DATABASE DLEM_DB_2 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE DLEM_DB_2;
END
GO

CREATE DATABASE DLEM_DB_2;
GO

USE DLEM_DB_2;
GO

-- ============================== USER ==============================
-- Role table
CREATE TABLE [Role] (
    RoleId INT PRIMARY KEY IDENTITY(1,1),
    RoleName NVARCHAR(50) NOT NULL UNIQUE
);
GO

-- Users table
CREATE TABLE [User] (
    UserId INT PRIMARY KEY IDENTITY(1,1),
    Username NVARCHAR(100) NOT NULL,
    Email NVARCHAR(255) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    RoleId INT NOT NULL REFERENCES [Role](RoleId),
    [Status] BIT NOT NULL DEFAULT 1
);
GO

-- Profile table
CREATE TABLE Profile (
    ProfileId INT PRIMARY KEY IDENTITY(1,1),
    FullName NVARCHAR(255) NOT NULL,
    DateOfBirth DATETIME NOT NULL,
    PhoneNumber NVARCHAR(20) NOT NULL,
    Sex NVARCHAR(10) NOT NULL,
    GovernmentIdNumber NVARCHAR(100) NOT NULL UNIQUE,
    Address NVARCHAR(500),
    UserId INT NOT NULL REFERENCES [User](UserId)
);
GO

-- Document table
CREATE TABLE Document (
    DocumentId INT PRIMARY KEY IDENTITY(1,1),
    DocumentType NVARCHAR(50) NOT NULL,
    DocumentUrl NVARCHAR(500) NOT NULL,
    Notes NVARCHAR(255),
    ProfileId INT NOT NULL REFERENCES Profile(ProfileId)
);
GO

-- Licence table
CREATE TABLE Licence (
    LicenceId INT PRIMARY KEY IDENTITY(1,1),
    LicenceClass NVARCHAR(50) NOT NULL UNIQUE,
    Description NVARCHAR(500),
    MinimumAge INT NOT NULL,
    ValidForYears INT NOT NULL,
    UpgradeFromLicenceId INT NULL REFERENCES Licence(LicenceId)
);
GO

-- ExamRegistration table (NOTE: chỉ sử dụng trong đăng ký thi qua trung tâm, không sử dụng trong kỳ thi)
CREATE TABLE ExamRegistration (
    ExamRegistrationId INT PRIMARY KEY IDENTITY(1,1),
    RegistrationStatus NVARCHAR(50) NOT NULL,
    Notes NVARCHAR(MAX),
    ProfileId INT NOT NULL REFERENCES Profile(ProfileId),
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId)
);
GO

-- ============================== EXAM ==============================
-- Exam table
CREATE TABLE Exam (
    ExamId INT PRIMARY KEY IDENTITY(1,1),
    ExamCode NVARCHAR(50) NOT NULL UNIQUE,
    ExamDate DATETIME NOT NULL,
    CentreName NVARCHAR(255) NOT NULL,
    [Status] NVARCHAR(50) NOT NULL,
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId)
);
GO

-- Session table
CREATE TABLE [Session] (
    SessionId INT PRIMARY KEY IDENTITY(1,1),
    SessionName NVARCHAR(100) NOT NULL,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NOT NULL,
    [Status] NVARCHAR(50) NOT NULL,
    ExamId INT NOT NULL REFERENCES Exam(ExamId),
    CHECK (EndTime > StartTime)
);
GO

-- ExamSection table
CREATE TABLE ExamSection (
    ExamSectionId INT PRIMARY KEY IDENTITY(1,1),
    SectionName NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- Licence_ExamSection junction table
CREATE TABLE Licence_ExamSection (
    LicenceExamSectionId INT PRIMARY KEY IDENTITY(1,1),
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    ExamSectionId INT NOT NULL REFERENCES ExamSection(ExamSectionId),
    DurationMinutes INT NULL,
    UNIQUE (LicenceId, ExamSectionId),
    CHECK (DurationMinutes IS NULL OR DurationMinutes >= 0) -- 0 -> không có thời gian 
);
GO

-- Session_ExamSection junction table
CREATE TABLE Session_ExamSection (
    SessionExamSectionId INT PRIMARY KEY IDENTITY(1,1),
    SessionId INT NOT NULL REFERENCES Session(SessionId),
    ExamSectionId INT NOT NULL REFERENCES ExamSection(ExamSectionId),
    UNIQUE (SessionId, ExamSectionId)
);
GO

-- ExamArea table
CREATE TABLE ExamArea (
    ExamAreaId INT PRIMARY KEY IDENTITY(1,1),
    AreaName NVARCHAR(100) NOT NULL,
    AreaType NVARCHAR(50) NOT NULL, -- (phòng thi, sân thi, đường thi - đường trường)
    Capacity INT NULL,
    [Location] NVARCHAR(255) NOT NULL,
    CHECK (Capacity > 0)
);
GO

-- Session_ExamArea junction table
CREATE TABLE Session_ExamArea (
    SessionExamAreaId INT PRIMARY KEY IDENTITY(1,1),
    SessionId INT NOT NULL REFERENCES Session(SessionId),
    ExamAreaId INT NOT NULL REFERENCES ExamArea(ExamAreaId),
    UNIQUE (SessionId, ExamAreaId)
);
GO

-- Session_Examiner --> ExaminerSchedule: phân công shv theo ca (Session), kỳ thi (Exam), phần thi (ExamSection), phòng (ExamArea).
CREATE TABLE ExaminerSchedule (
    ExaminerScheduleId INT PRIMARY KEY IDENTITY(1,1),
    SessionId INT NOT NULL REFERENCES [Session](SessionId),
    ExaminerId INT NOT NULL REFERENCES [User](UserId),
    ExamSectionId INT NULL REFERENCES ExamSection(ExamSectionId),
    ExamAreaId INT NULL REFERENCES ExamArea(ExamAreaId),
    AssignedBy INT NULL REFERENCES [User](UserId),
    AssignedAt DATETIME NULL DEFAULT GETDATE(),
    UNIQUE (SessionId, ExaminerId),
    FOREIGN KEY (SessionId, ExamSectionId) REFERENCES Session_ExamSection(SessionId, ExamSectionId),
    FOREIGN KEY (SessionId, ExamAreaId) REFERENCES Session_ExamArea(SessionId, ExamAreaId)
);
GO

-- ExamDevice table
CREATE TABLE ExamDevice (
    ExamDeviceId INT PRIMARY KEY IDENTITY(1,1),
    DeviceName NVARCHAR(100) NOT NULL,
    DeviceType NVARCHAR(50) NOT NULL,
    [Status] NVARCHAR(50) NOT NULL,
    ExamAreaId INT NOT NULL REFERENCES ExamArea(ExamAreaId)
);
GO

-- Candidate table
CREATE TABLE Candidate (
    CandidateId INT PRIMARY KEY IDENTITY(1,1),
    CandidateNumber NVARCHAR(50) NOT NULL UNIQUE,

	-- personal info
    FullName NVARCHAR(255) NOT NULL,
    DateOfBirth DATETIME NOT NULL,
    PhoneNumber NVARCHAR(20),
    Sex NVARCHAR(10),
    GovernmentIdNumber NVARCHAR(100) UNIQUE,
    Address NVARCHAR(500),

	-- tracking
    TakeTheory BIT,
    TakePractical BIT,
    TakeRoadLayout BIT,
    TakeOnRoad BIT,
	TakeNo INT NOT NULL,
    ReasonForTaking NVARCHAR(355),
    PhotoImageUrl NVARCHAR(500),
    IsAbsent BIT NOT NULL DEFAULT 0,
    IsSuspended BIT NOT NULL DEFAULT 0,
    UserId INT NULL REFERENCES [User](UserId)
);
GO

-- ExamEnrollment junction table --> ExamEnrollment
CREATE TABLE ExamEnrollment (
	ExamEnrollmentId INT PRIMARY KEY IDENTITY(1,1),
    CandidateId INT NOT NULL REFERENCES Candidate(CandidateId),
    SessionId INT NOT NULL REFERENCES [Session](SessionId),
    SectionStatus NVARCHAR(50) NOT NULL DEFAULT N'Pending',
    SignaturePrinted BIT NOT NULL DEFAULT 0,
    ExamDeviceId INT NULL REFERENCES ExamDevice(ExamDeviceId),
    UNIQUE (CandidateId, SessionId)
);
GO

-- ============================== PAYMENT ==============================
-- Fee table
CREATE TABLE Fee (
    FeeId INT PRIMARY KEY IDENTITY(1,1),
    FeeName NVARCHAR(100) NOT NULL,
    FeeType NVARCHAR(50) NOT NULL,
    Amount DECIMAL(18,2) NOT NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    CHECK (Amount >= 0)
);
GO

-- Payment table
CREATE TABLE Payment (
    PaymentId INT PRIMARY KEY IDENTITY(1,1),
    PaymentStatus NVARCHAR(50) NOT NULL,      -- Pending / Paid / Failed / Refunded
    PaymentMethod NVARCHAR(50) NOT NULL,      -- Cash / QR
    TransactionReference NVARCHAR(255) UNIQUE,
    TotalAmount DECIMAL(18,2) NOT NULL,
    PaidAt DATETIME NULL,
    ExamEnrollmentId INT NOT NULL REFERENCES ExamEnrollment(ExamEnrollmentId),
    CHECK (TotalAmount >= 0)
);
GO

-- Payment_Fee junction table
CREATE TABLE Payment_Fee (
    PaymentFeeId INT PRIMARY KEY IDENTITY(1,1),
    PaymentId INT NOT NULL REFERENCES Payment(PaymentId),
    FeeId INT NOT NULL REFERENCES Fee(FeeId),
    UNIQUE (PaymentId, FeeId)
);
GO

-- ============================== QUESTION ==============================
-- QuestionCategory table
CREATE TABLE QuestionCategory (
    QuestionCategoryId INT PRIMARY KEY IDENTITY(1,1),
    CategoryName NVARCHAR(100) NOT NULL UNIQUE,
    Description NVARCHAR(500)
);
GO

-- Question table
CREATE TABLE Question (
    QuestionId INT PRIMARY KEY IDENTITY(1,1),
    QuestionNumber INT NOT NULL,
    ImageUrl NVARCHAR(500),
    CorrectAnswer NVARCHAR(10) NOT NULL,
    IsCritical BIT NOT NULL DEFAULT 0,
    QuestionCategoryId INT NOT NULL REFERENCES QuestionCategory(QuestionCategoryId)
);
GO

-- Licence_Question junction table
CREATE TABLE Licence_Question (
    LicenceQuestionId INT PRIMARY KEY IDENTITY(1,1),
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    QuestionId INT NOT NULL REFERENCES Question(QuestionId),
    UNIQUE (LicenceId, QuestionId)
);
GO

-- ============================== EXAM COMPONENTS ==============================
-- TheoryPaper table
CREATE TABLE TheoryPaper (
    TheoryPaperId INT PRIMARY KEY IDENTITY(1,1),
   ExamEnrollmentId INT NOT NULL REFERENCES ExamEnrollment(ExamEnrollmentId),
    ExamDeviceId INT NOT NULL REFERENCES ExamDevice(ExamDeviceId),
    StartedAt DATETIME,
    SubmittedAt DATETIME,
    CHECK (SubmittedAt IS NULL OR SubmittedAt >= StartedAt)
);
GO

-- CandidateAnswer table
CREATE TABLE CandidateAnswer (
    CandidateAnswerId INT PRIMARY KEY IDENTITY(1,1),
    TheoryPaperId INT NOT NULL REFERENCES TheoryPaper(TheoryPaperId),
    QuestionId INT NOT NULL REFERENCES Question(QuestionId),
    Answer NVARCHAR(10),
    UNIQUE (TheoryPaperId, QuestionId)
);
GO

-- ExamResult table
CREATE TABLE ExamResult (
    ExamResultId INT PRIMARY KEY IDENTITY(1,1),
    ExamEnrollmentId INT NOT NULL REFERENCES ExamEnrollment(ExamEnrollmentId),
    IsPassed BIT NOT NULL,
    ResultDate DATETIME NOT NULL DEFAULT GETDATE(),
    UNIQUE (ExamEnrollmentId)
);
GO

-- ExamScore table
CREATE TABLE ExamScore (
    ExamScoreId INT PRIMARY KEY IDENTITY(1,1),
    ExamResultId INT NOT NULL REFERENCES ExamResult(ExamResultId),
    ExamSectionId INT NOT NULL REFERENCES ExamSection(ExamSectionId),
    Score DECIMAL(5,2) NOT NULL,
    CHECK (Score >= 0 AND Score <= 100),
    UNIQUE (ExamResultId, ExamSectionId)
);
GO

-- ScoreDeduction table
CREATE TABLE ScoreDeduction (
    ScoreDeductionId INT PRIMARY KEY IDENTITY(1,1),
    [Reason] NVARCHAR(500) NOT NULL,
    Points DECIMAL(5,2) NOT NULL,
    IsCritical BIT NOT NULL DEFAULT 0,
    ExamSectionId INT NULL REFERENCES ExamSection(ExamSectionId),
    SortOrder INT NOT NULL DEFAULT 0,
    CHECK (Points > 0)
);
GO

-- Score_Deduction --> DeductionRecord (số lần lỗi + thời điểm nhập gần nhất)
CREATE TABLE DeductionRecord (
    DeductionRecordId INT PRIMARY KEY IDENTITY(1,1),
    ExamScoreId INT NOT NULL REFERENCES ExamScore(ExamScoreId),
    ScoreDeductionId INT NOT NULL REFERENCES ScoreDeduction(ScoreDeductionId),
    OccurrenceCount INT NOT NULL DEFAULT 1,
    RecordedAt DATETIME NOT NULL DEFAULT GETDATE(),
    UNIQUE (ExamScoreId, ScoreDeductionId),
    CHECK (OccurrenceCount > 0)
);
GO

-- Audit table
CREATE TABLE Audit (
    AuditId BIGINT PRIMARY KEY IDENTITY(1,1),
    UserId INT NULL REFERENCES [User](UserId),
    Action NVARCHAR(50) NOT NULL,
    [Reason] NVARCHAR(MAX),
    EntityName NVARCHAR(255) NOT NULL,
    EntityId NVARCHAR(255) NOT NULL,
    OldValue NVARCHAR(MAX),
    NewValue NVARCHAR(MAX),
	Details NVARCHAR(MAX),
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- Indices
CREATE INDEX IX_User_Username ON [User](Username);
CREATE INDEX IX_User_Email ON [User](Email);
CREATE INDEX IX_Profile_UserId ON Profile(UserId);
CREATE INDEX IX_Profile_GovernmentId ON Profile(GovernmentIdNumber);
CREATE INDEX IX_Document_ProfileId ON Document(ProfileId);
CREATE INDEX IX_ExamRegistration_ProfileId ON ExamRegistration(ProfileId);
CREATE INDEX IX_ExamRegistration_LicenceId ON ExamRegistration(LicenceId);
CREATE INDEX IX_Exam_LicenceId ON Exam(LicenceId);
CREATE INDEX IX_Exam_ExamCode ON Exam(ExamCode);
CREATE INDEX IX_Session_ExamId ON Session(ExamId);
CREATE INDEX IX_Licence_ExamSection_LicenceId ON Licence_ExamSection(LicenceId);
CREATE INDEX IX_Licence_ExamSection_ExamSectionId ON Licence_ExamSection(ExamSectionId);
CREATE INDEX IX_ExamDevice_ExamAreaId ON ExamDevice(ExamAreaId);
CREATE INDEX IX_Candidate_UserId ON Candidate(UserId);
CREATE INDEX IX_Candidate_CandidateNumber ON Candidate(CandidateNumber);
CREATE INDEX IX_Payment_ExamEnrollmentId ON Payment(ExamEnrollmentId);
CREATE INDEX IX_TheoryPaper_ExamEnrollmentId ON TheoryPaper(ExamEnrollmentId);
CREATE INDEX IX_CandidateAnswer_TheoryPaperId ON CandidateAnswer(TheoryPaperId);
CREATE INDEX IX_ExamResult_ExamEnrollmentId ON ExamResult(ExamEnrollmentId);
CREATE INDEX IX_Audit_CreatedAt ON Audit(CreatedAt);
CREATE INDEX IX_Audit_Entity ON Audit(EntityName, EntityId);
CREATE INDEX IX_ExaminerSchedule_SessionId ON ExaminerSchedule(SessionId);
CREATE INDEX IX_ExaminerSchedule_ExaminerId ON ExaminerSchedule(ExaminerId);
CREATE INDEX IX_ExaminerSchedule_ExamSectionId ON ExaminerSchedule(ExamSectionId);
CREATE INDEX IX_ExaminerSchedule_ExamAreaId ON ExaminerSchedule(ExamAreaId);
GO

-- ============================================
-- DML SAMPLE DATA – DLEM_DB_2
-- Driving License Examination Management System
-- Default password for seeded accounts: login123
-- ============================================

USE DLEM_DB_2;
GO

-- Clear existing seed data
DELETE FROM DeductionRecord;
DELETE FROM ScoreDeduction;
DELETE FROM ExamScore;
DELETE FROM ExamResult;
DELETE FROM CandidateAnswer;
DELETE FROM TheoryPaper;
DELETE FROM ExamEnrollment;
DELETE FROM Payment_Fee;
DELETE FROM Payment;
DELETE FROM Candidate;
DELETE FROM ExamRegistration;
DELETE FROM Document;
DELETE FROM Profile;
DELETE FROM ExaminerSchedule;
DELETE FROM Session_ExamArea;
DELETE FROM Session_ExamSection;
DELETE FROM Licence_ExamSection;
DELETE FROM ExamDevice;
DELETE FROM [Session];
DELETE FROM Exam;
DELETE FROM ExamArea;
DELETE FROM ExamSection;
DELETE FROM Fee;
DELETE FROM Licence;
DELETE FROM Audit;
DELETE FROM [User];
DELETE FROM [Role];
GO

-- ============================================
-- 0. ROLES
-- ============================================
INSERT INTO [Role] (RoleName) VALUES
(N'Admin'),
(N'Examiner'),
(N'ManagingStaff'),
(N'ExamStaff'),
(N'Candidate'),
(N'Registrant');
GO

-- ============================================
-- 1. USERS
-- ============================================
INSERT INTO [User] (Username, Email, PasswordHash, RoleId, [Status]) VALUES
(N'admin',           N'admin@laivui.vn',           N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Admin'),          1),
(N'examiner_tung',   N'tung.nguyen@pc08a.com',   N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Examiner'),       1),
(N'examiner_lan',    N'lan.tran@pc08a.com',      N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Examiner'),       1),
(N'manager_dung',   N'dung.pham@laivui.vn',       N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'ManagingStaff'),  1),
(N'examstaff_hoa',  N'hoa.le@laivui.vn',          N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'ExamStaff'),      1),
(N'examstaff_minh', N'minh.vu@laivui.vn',         N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'ExamStaff'),      1),
(N'an.nguyen',       N'an.nguyen@gmail.com',       N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1),
(N'binh.tran',       N'binh.tran@gmail.com',       N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1),
(N'chinh.le',        N'chinh.le@gmail.com',        N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1),
(N'dung.pham',       N'dung.pham@gmail.com',       N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1),
(N'em.hoang',        N'em.hoang@gmail.com',        N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1),
(N'phuong.vu',       N'phuong.vu@gmail.com',       N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1),
(N'hai.do',          N'hai.do@gmail.com',          N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1),
(N'kim.ngo',         N'kim.ngo@gmail.com',         N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1),
(N'long.bui',        N'long.bui@gmail.com',        N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     0),
(N'thi048',          N'thi048@gmail.com',          N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1),
(N'thi049',          N'thi049@gmail.com',          N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),     1);
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
(N'Bùi Văn Long',             '1985-03-30', N'0888123456', N'Nam', N'001203012353', N'67 Điện Biên Phủ, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'long.bui')),
(N'Nguyễn Thị Hoa',          '1997-05-14', N'0911004801', N'Nữ', N'001203012354', N'18 Hoàng Hoa Thám, Hà Nội', (SELECT UserId FROM [User] WHERE Username = N'thi048')),
(N'Trần Văn Khoa',           '1996-09-03', N'0911004901', N'Nam', N'001203012355', N'72 Cầu Giấy, Hà Nội',      (SELECT UserId FROM [User] WHERE Username = N'thi049'));
GO

-- ============================================
-- 3. DOCUMENTS
-- ============================================
INSERT INTO Document (DocumentType, DocumentUrl, Notes, ProfileId) VALUES
(N'CCCD',         N'/docs/id/front_001.jpg', NULL, (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012345')),
(N'CCCD',         N'/docs/id/back_001.jpg',  NULL, (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012345')),
(N'Giấy khám SK', N'/docs/health/health_001.pdf', NULL, (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012345')),
(N'Khác',         N'/docs/other/commitment_001.pdf', N'Giấy cam kết bổ sung hồ sơ', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012345')),
(N'CCCD',         N'/docs/id/front_002.jpg', NULL, (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012346')),
(N'CCCD',         N'/docs/id/back_002.jpg',  NULL, (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012346')),
(N'Giấy khám SK', N'/docs/health/health_002.pdf', NULL, (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012346')),
(N'CCCD',         N'/docs/id/front_003.jpg', NULL, (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012347')),
(N'GPLX',         N'/docs/license/license_005.jpg', NULL, (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012349'));
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
(N'Rejected', N'Không đủ yêu cầu sức khoẻ',         (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012353'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'Approved', N'', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012354'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')),
(N'Approved', N'', (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = N'001203012355'), (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'));
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
(N'Ca sáng - Lý thuyết B',  '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'InProgress', (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Ca sáng - Sa hình B',    '2026-06-01 09:30:00', '2026-06-01 11:30:00', N'Scheduled',  (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Ca chiều - Đường trường B','2026-06-01 13:00:00','2026-06-01 16:00:00', N'Scheduled', (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260601')),
(N'Ca sáng - Lý thuyết A1', '2026-06-01 07:30:00', '2026-06-01 09:00:00', N'Open',       (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-A1-20260601')),
(N'Ca sáng - Lý thuyết B2', '2026-06-15 07:30:00', '2026-06-15 09:00:00', N'Scheduled',  (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260615'));
GO

-- ============================================
-- 9. SESSION EXAM SECTIONS
-- ============================================
INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B'),   (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Sa hình B'),     (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca chiều - Đường trường B'),(SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết A1'),  (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B2'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'));
GO

-- ============================================
-- 10. EXAM AREAS
-- ============================================
INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location]) VALUES
(N'Phòng LT 1',      N'Room',   10, N'Tầng 2, Toà B'),
(N'Phòng LT 2',      N'Room',   10, N'Tầng 2, Toà B'),
(N'Sân thi A1',      N'Ground', 15, N'Sân thi 1'),
(N'Sân thi Ô tô 1',  N'Ground', 10, N'Sân thi 2'),
(N'Đường trường 1',  N'Route',   8, N'Khu vực thi đường trường');
GO

-- ============================================
-- 11. SESSION EXAM AREAS
-- ============================================
INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B'),    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết A1'),   (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Sa hình B'),      (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca chiều - Đường trường B'),(SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B2'),   (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1'));
GO

-- ============================================
-- 12. SESSION EXAMINERS (Exam + ExamSection + ExamArea + Session + Examiner)
-- ============================================
INSERT INTO ExaminerSchedule (SessionId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt) VALUES
(
    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B'),
    (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1'),
    (SELECT UserId FROM [User] WHERE Username = N'examiner_tung'),
    (SELECT UserId FROM [User] WHERE Username = N'examstaff_hoa'),
    '2026-05-25 08:00:00'
),
(
    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Sa hình B'),
    (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'),
    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1'),
    (SELECT UserId FROM [User] WHERE Username = N'examiner_tung'),
    (SELECT UserId FROM [User] WHERE Username = N'examstaff_hoa'),
    '2026-05-25 08:05:00'
),
(
    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca chiều - Đường trường B'),
    (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'),
    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1'),
    (SELECT UserId FROM [User] WHERE Username = N'examiner_lan'),
    (SELECT UserId FROM [User] WHERE Username = N'examstaff_hoa'),
    '2026-05-25 08:10:00'
),
(
    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết A1'),
    (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2'),
    (SELECT UserId FROM [User] WHERE Username = N'examiner_lan'),
    (SELECT UserId FROM [User] WHERE Username = N'examstaff_minh'),
    '2026-05-25 08:15:00'
),
(
    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B2'),
    (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1'),
    (SELECT UserId FROM [User] WHERE Username = N'examiner_tung'),
    (SELECT UserId FROM [User] WHERE Username = N'examstaff_minh'),
    '2026-05-25 08:20:00'
);
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
(N'PC-LT2-02', N'Computer', N'Maintenance',  (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2')),
(N'Xe SH 01', N'Car', N'Available', (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')),
(N'Xe SH 02', N'Car', N'Available', (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')),
(N'Xe SH 03', N'Car', N'Available', (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')),
(N'Xe DT 01', N'Car', N'Available', (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1')),
(N'Xe DT 02', N'Car', N'Available', (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1')),
(N'Xe DT 03', N'Car', N'Available', (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1')),
(N'Xe máy 01', N'Motorcycle', N'Available', (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi A1')),
(N'Xe máy 02', N'Motorcycle', N'Available', (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi A1')),
(N'Xe máy 03', N'Motorcycle', N'Available', (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi A1'));
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
INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, TakeTheory, TakePractical, TakeRoadLayout, TakeOnRoad, TakeNo, ReasonForTaking, PhotoImageUrl, UserId) VALUES
-- B: lý thuyết bảo lưu, chỉ thi sa hình + đường trường
(N'021',    N'Nguyễn Văn A',     '1995-08-15', N'0989123456', N'Nam', N'079012345678', N'123 Nguyễn Văn Linh, P. Tân Phong, Q.7, TP.HCM', 0, NULL, 1, 1, 1, N'Thi lại vì trượt sa hình', NULL, (SELECT UserId FROM [User] WHERE Username = N'an.nguyen')),
-- B: thi lần đầu (đủ 3 phần)
(N'022',    N'Trần Thị Bình',    '1995-08-22', N'0912345678', N'Nữ',  N'079012345679', N'45 Nguyễn Huệ, Q.1, TP.HCM',                      1, NULL, 1, 1, 1, N'Thi lần đầu',              NULL, (SELECT UserId FROM [User] WHERE Username = N'binh.tran')),
-- C1: thi lần đầu
(N'023',    N'Lê Văn Chính',     '1988-11-10', N'0978563412', N'Nam', N'079012345680', N'78 Trần Phú, Đà Nẵng',                           1, NULL, 1, 1, 1, N'Thi lần đầu',              NULL, (SELECT UserId FROM [User] WHERE Username = N'chinh.le')),
-- B: trượt lý thuyết → phải thi lại hết
(N'024',N'Nguyễn Văn Quyết', '1992-04-12', N'0909111222', N'Nam', N'031092004581', N'88 Lê Lợi, TP.HCM',                               1, NULL, 1, 1, 1, N'Thi lại vì trượt lý thuyết', NULL, (SELECT UserId FROM [User] WHERE Username = N'em.hoang')),
-- B1: trừ hết điểm → chỉ thi lại lý thuyết
(N'025',N'Nguyễn Văn B',     '1998-02-18', N'0933445566', N'Nam', N'079012345681', N'12 Lý Thường Kiệt, Huế',                          1, 0,    NULL, NULL, 1, N'Thi lại vì trừ hết điểm',  NULL, (SELECT UserId FROM [User] WHERE Username = N'phuong.vu')),
-- B: thi lần đầu
(N'026',N'Phạm Văn Cường',   '1990-07-07', N'0944556677', N'Nam', N'079012345682', N'56 Hai Bà Trưng, Hà Nội',                         1, NULL, 1, 1, 1, N'Thi lần đầu',              NULL, (SELECT UserId FROM [User] WHERE Username = N'hai.do')),
-- A1: thi lần đầu (lý thuyết + thực hành)
(N'027',N'Hoàng Thị Mai',    '1999-11-30', N'0955667788', N'Nữ',  N'079012345683', N'34 Nguyễn Trãi, Hà Nội',                          1, 1,    NULL, NULL, 1, N'Thi lần đầu',              NULL, (SELECT UserId FROM [User] WHERE Username = N'kim.ngo')),
-- B: ca lý thuyết đang thi (InProgress) - thêm cho examiner test
(N'028', N'Nguyễn Thị Hoa', '1997-05-14', N'0911004801', N'Nữ', N'079012345684', N'18 Hoàng Hoa Thám, Hà Nội',                     1, NULL, 1, 1, 1, N'Thi lần đầu',              N'/docs/photos/thi028.jpg', (SELECT UserId FROM [User] WHERE Username = N'thi048')),
(N'029', N'Trần Văn Khoa',  '1996-09-03', N'0911004901', N'Nam', N'079012345685', N'72 Cầu Giấy, Hà Nội',                           1, NULL, 1, 1, 1, N'Thi lần đầu',              N'/docs/photos/thi029.jpg', (SELECT UserId FROM [User] WHERE Username = N'thi049'));
GO

-- ============================================
-- 16. EXAM_CANDIDATE (assign candidates to exam sessions)
-- ============================================
INSERT INTO ExamEnrollment (CandidateId, SessionId) VALUES
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'022'),    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'024'), (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'026'),(SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'021'),    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Sa hình B')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'025'),(SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết A1')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'028'), (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B')),
((SELECT CandidateId FROM Candidate WHERE CandidateNumber = N'029'), (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca sáng - Lý thuyết B'));
GO

-- ============================================
-- 16b. THỦ TỤC + PHÒNG THI (ca Lý thuyết B - InProgress)
-- ============================================
UPDATE er
SET RegistrationStatus = N'Present',
    Notes = N'AllocatedRoom:'
        + CAST(ea.ExamAreaId AS NVARCHAR(10)) + N':'
        + ea.AreaName
FROM ExamRegistration er
JOIN Profile p ON p.ProfileId = er.ProfileId
JOIN Candidate c ON c.GovernmentIdNumber = p.GovernmentIdNumber
JOIN ExamEnrollment ec ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
CROSS JOIN ExamArea ea
WHERE s.SessionName = N'Ca sáng - Lý thuyết B'
  AND ea.AreaName = N'Phòng LT 1'
  AND c.CandidateNumber IN (N'022', N'024', N'026', N'028', N'029');
GO

UPDATE c
SET PhotoImageUrl = N'/docs/photos/' + c.CandidateNumber + N'.jpg'
FROM Candidate c
JOIN ExamEnrollment ec ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE s.SessionName = N'Ca sáng - Lý thuyết B'
  AND (c.PhotoImageUrl IS NULL OR c.PhotoImageUrl = N'');
GO

-- ============================================
-- 17. PAYMENTS
-- ============================================
INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
SELECT N'Completed', N'BankTransfer', N'TXN-20260520-001', 430000.00, '2026-05-20 10:15:00', ec.ExamEnrollmentId
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'021' AND s.SessionName = N'Ca sáng - Sa hình B'
UNION ALL SELECT N'Completed', N'BankTransfer', N'TXN-20260520-002', 430000.00, '2026-05-20 11:00:00', ec.ExamEnrollmentId
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'022' AND s.SessionName = N'Ca sáng - Lý thuyết B'
UNION ALL SELECT N'Completed', N'Cash', N'CASH-20260521-001', 430000.00, '2026-05-21 08:30:00', ec.ExamEnrollmentId
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'024' AND s.SessionName = N'Ca sáng - Lý thuyết B'
UNION ALL SELECT N'Pending', N'Cash', N'PENDING-20260522-001', 430000.00, NULL, ec.ExamEnrollmentId
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'026' AND s.SessionName = N'Ca sáng - Lý thuyết B'
UNION ALL SELECT N'Completed', N'BankTransfer', N'TXN-20260522-001', 130000.00, '2026-05-22 14:20:00', ec.ExamEnrollmentId
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'025' AND s.SessionName = N'Ca sáng - Lý thuyết A1'
UNION ALL SELECT N'Completed', N'BankTransfer', N'TXN-20260528-048', 430000.00, '2026-05-28 09:00:00', ec.ExamEnrollmentId
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'028' AND s.SessionName = N'Ca sáng - Lý thuyết B'
UNION ALL SELECT N'Completed', N'Cash', N'CASH-20260528-049', 430000.00, '2026-05-28 09:30:00', ec.ExamEnrollmentId
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'029' AND s.SessionName = N'Ca sáng - Lý thuyết B';
GO

UPDATE Payment
SET PaymentStatus = N'Completed',
    PaymentMethod = N'BankTransfer',
    TransactionReference = N'TXN-20260522-456',
    PaidAt = '2026-05-22 15:00:00'
WHERE TransactionReference = N'PENDING-20260522-001';
GO

INSERT INTO Payment_Fee (PaymentId, FeeId)
SELECT p.PaymentId, f.FeeId
FROM Payment p
CROSS JOIN Fee f
WHERE p.TransactionReference = N'TXN-20260520-001'
  AND f.FeeName IN (N'Lệ phí thi lý thuyết', N'Phí hồ sơ', N'Lệ phí thi sa hình', N'Lệ phí thi đường trường');
GO

-- ============================================
-- 18. THEORY PAPER (ca Lý thuyết B - InProgress)
-- ============================================
INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
SELECT ec.ExamEnrollmentId,
       (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'PC-LT1-04'),
       '2026-06-01 07:35:00',
       '2026-06-01 07:52:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'022'
  AND s.SessionName = N'Ca sáng - Lý thuyết B';
GO

INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
SELECT ec.ExamEnrollmentId,
       (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'PC-LT1-01'),
       '2026-06-01 07:40:00',
       '2026-06-01 07:58:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'024'
  AND s.SessionName = N'Ca sáng - Lý thuyết B';
GO

-- 456: đang thi (đã bắt đầu, chưa nộp bài)
INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
SELECT ec.ExamEnrollmentId,
       (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'PC-LT1-02'),
       '2026-06-01 08:05:00',
       NULL
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'026'
  AND s.SessionName = N'Ca sáng - Lý thuyết B';
GO

-- ============================================
-- 19. EXAM RESULTS & SCORES (ca Lý thuyết B)
-- ============================================
INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 1, '2026-06-01 07:55:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'022'
  AND s.SessionName = N'Ca sáng - Lý thuyết B';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
       33.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
WHERE c.CandidateNumber = N'022';
GO

INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 0, '2026-06-01 09:05:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'024'
  AND s.SessionName = N'Ca sáng - Lý thuyết B';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
       25.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
WHERE c.CandidateNumber = N'024';
GO

INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 0, '2026-06-01 11:20:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'021'
  AND s.SessionName = N'Ca sáng - Sa hình B';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'),
       72.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
WHERE c.CandidateNumber = N'021';
GO

-- ============================================
-- 20. SCORE DEDUCTIONS (TT 12/2025/BCA - trung tâm loại 2, A1→D1)
-- ============================================
INSERT INTO ScoreDeduction ([Reason], Points, IsCritical, ExamSectionId, SortOrder) VALUES
-- Thi thực hành trong hình (Sa hình - B/C1/C/D1)
(N'Không thực hiện đúng hiệu lệnh của sát hạch viên thi', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 1),
(N'Không bám sát vạch d tốc độ cho phép', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 2),
(N'Không xi-nhan đúng quy định (chuyển hướng, chuyển làn, vượt, trước khi dừng)', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 3),
(N'Không quan sát, sử dụng gương chiếu hậu', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 4),
(N'Không nhường đường cho người đi bộ', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 5),
(N'Dừng, đỗ hoặc để xe không đúng quy định', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 6),
(N'Tăng ga, giảm ga hoặc sử dụng ly hợp không đúng', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 7),
(N'Không thắt dây an toàn khi lái xe', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 8),
(N'Không đảm bảo khoảng cách an toàn', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 9),
(N'Lái xe vào vị trí cấm dừng, cấm đỗ', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 10),
(N'Bánh xe ra ngoài vạch giới hạn', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 11),
(N'Sai trình tự thực hiện bài thi', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 12),
(N'Gây tai nạn, sự cố trong quá trình thi', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 13),
(N'Không hoàn thành bài thi trong thời gian quy định', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 14),
(N'Lái xe lên vỉa hè, lề đường', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'), 15),
-- Thi thực hành trên đường (Đường trường)
(N'Không thực hiện đúng hiệu lệnh của sát hạch viên thi', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 1),
(N'Không tuân thủ tín hiệu đèn giao thông', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 2),
(N'Không xi-nhan đúng quy định', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 3),
(N'Không quan sát, sử dụng gương chiếu hậu', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 4),
(N'Không nhường đường cho người đi bộ, xe ưu tiên', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 5),
(N'Vượt xe, chuyển làn không đúng quy định', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 6),
(N'Không giữ khoảng cách an toàn', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 7),
(N'Dừng, đỗ xe không đúng quy định', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 8),
(N'Không thắt dây an toàn khi lái xe', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 9),
(N'Gây tai nạn, sự cố trong quá trình thi', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 10),
(N'Không hoàn thành bài thi trong thời gian quy định', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 11),
(N'Lái xe lên vỉa hè, lề đường', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'), 12),
-- Thi thực hành xe máy (A1 - Phụ lục II)
(N'Không thực hiện đúng hiệu lệnh của sát hạch viên thi', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 1),
(N'Không bám sát vạch d tốc độ cho phép', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 2),
(N'Không xi-nhan bằng tay đúng quy định', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 3),
(N'Không quan sát trước khi đi, trước khi chuyển hướng', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 4),
(N'Không đội mũ bảo hiểm đúng quy cách', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 5),
(N'Dừng, đỗ xe không đúng quy định', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 6),
(N'Tăng ga, giảm ga không đúng', 5.00, 0, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 7),
(N'Bánh xe ra ngoài vạch giới hạn', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 8),
(N'Sai trình tự thực hiện bài thi', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 9),
(N'Gây tai nạn, sự cố trong quá trình thi', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 10),
(N'Không hoàn thành bài thi trong thời gian quy định', 5.00, 1, (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'), 11);
GO

INSERT INTO DeductionRecord (ExamScoreId, ScoreDeductionId, OccurrenceCount, RecordedAt)
SELECT es.ExamScoreId, sd.ScoreDeductionId, 1, GETDATE()
FROM ExamScore es
JOIN ExamSection sec ON es.ExamSectionId = sec.ExamSectionId
JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
JOIN ExamEnrollment ec ON ec.ExamEnrollmentId = er.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN ScoreDeduction sd ON sd.ExamSectionId = sec.ExamSectionId AND sd.SortOrder = 3
WHERE sec.SectionName = N'Sa hình'
  AND c.CandidateNumber = N'021';
GO

-- ============================================
-- 21. AUDIT LOG (matches examiner audit.jsp mock)
-- ============================================
INSERT INTO Audit (UserId, Action, [Reason], EntityName, EntityId, OldValue, NewValue, CreatedAt) VALUES
((SELECT UserId FROM [User] WHERE Username = N'admin'),          N'UPDATE',  N'Phúc khảo',           N'Thí sinh', N'024', N'28/30', N'30/30', '2023-10-25 09:15:22'),
(NULL,                                                           N'SYSTEM',  N'Theo lịch trình',     N'Phòng thi', N'-',        N'Khóa',  N'Mở',    '2023-10-25 07:00:00'),
((SELECT UserId FROM [User] WHERE Username = N'examiner_tung'),  N'WARNING', N'Mang điện thoại',     N'Thí sinh', N'026', N'Bình thường', N'Vi phạm', '2023-10-24 10:45:11'),
((SELECT UserId FROM [User] WHERE Username = N'admin'),          N'DELETE',  N'Trùng CMND',          N'Thí sinh', N'027', N'Tồn tại', N'Đã xóa', '2023-10-24 14:20:05'),
((SELECT UserId FROM [User] WHERE Username = N'admin'),          N'UPDATE',  N'Yêu cầu từ Cục',      N'Thí sinh', N'025', N'Nguyễn Văn A', N'Nguyễn Văn B', '2023-10-23 08:10:00'),
((SELECT UserId FROM [User] WHERE Username = N'examiner_tung'),  N'UPDATE',  N'Chấm sai',            N'Kết quả thi', N'024', N'25/35', N'27/35', '2026-06-01 09:20:00'),
((SELECT UserId FROM [User] WHERE Username = N'manager_dung'),   N'APPROVE', N'Duyệt hồ sơ',         N'ExamRegistration', N'9', N'Pending', N'Approved', '2026-05-18 15:30:00');

-- Phân công phòng sát hạch viên (legacy mapping cho code đọc qua Audit EntityName = Session_ExaminerArea)
INSERT INTO Audit (UserId, Action, EntityName, EntityId, NewValue, CreatedAt)
SELECT se.AssignedBy,
       N'ASSIGN',
       N'Session_ExaminerArea',
       CAST(se.SessionId AS NVARCHAR(20)) + N':' + CAST(se.ExamAreaId AS NVARCHAR(20)) + N':' + CAST(se.ExaminerId AS NVARCHAR(20)),
       ea.AreaName,
       ISNULL(se.AssignedAt, GETDATE())
FROM ExaminerSchedule se
JOIN ExamArea ea ON ea.ExamAreaId = se.ExamAreaId
WHERE se.ExamAreaId IS NOT NULL;
GO

-- ============================================
-- 22. KHOÁ THI B-1292 - 3 sát hạch viên, 20 thí sinh (5 đã thi LT, 2 đã thi SH), ca InProgress
-- ============================================
INSERT INTO [User] (Username, Email, PasswordHash, RoleId, [Status]) VALUES
(N'examiner_b1292_lt', N'lt.b1292@pc08a.com',  N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Examiner'), 1),
(N'examiner_b1292_sh', N'sh.b1292@pc08a.com',  N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Examiner'), 1),
(N'examiner_b1292_dt', N'dt.b1292@pc08a.com',  N'login123', (SELECT RoleId FROM [Role] WHERE RoleName = 'Examiner'), 1);
GO

INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId) VALUES
(N'Hoàng Văn Lý Thuyết',  '1987-02-14', N'0911292001', N'Nam', N'001087021401', N'PC08A - Phòng lý thuyết',  (SELECT UserId FROM [User] WHERE Username = N'examiner_b1292_lt')),
(N'Trần Thị Sa Hình',      '1989-07-20', N'0911292002', N'Nữ', N'001089072001', N'PC08A - Sân sa hình',      (SELECT UserId FROM [User] WHERE Username = N'examiner_b1292_sh')),
(N'Nguyễn Văn Đường Trường','1991-11-05', N'0911292003', N'Nam', N'001091110501', N'PC08A - Đường trường',   (SELECT UserId FROM [User] WHERE Username = N'examiner_b1292_dt'));
GO

INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId) VALUES
(N'B-1292', '2026-06-10 07:00:00', N'Trung tâm Sát hạch Lái Vui - Hà Nội', N'Open', (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B'));
GO

INSERT INTO [Session] (SessionName, StartTime, EndTime, [Status], ExamId) VALUES
(N'Ca B-1292 - Lý thuyết',    '2026-06-10 07:30:00', '2026-06-10 09:00:00', N'InProgress', (SELECT ExamId FROM Exam WHERE ExamCode = N'B-1292')),
(N'Ca B-1292 - Sa hình',      '2026-06-10 09:30:00', '2026-06-10 11:30:00', N'InProgress', (SELECT ExamId FROM Exam WHERE ExamCode = N'B-1292')),
(N'Ca B-1292 - Đường trường', '2026-06-10 13:00:00', '2026-06-10 16:00:00', N'InProgress', (SELECT ExamId FROM Exam WHERE ExamCode = N'B-1292'));
GO

INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca B-1292 - Lý thuyết'),    (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca B-1292 - Sa hình'),      (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca B-1292 - Đường trường'), (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'));
GO

INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca B-1292 - Lý thuyết'),    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca B-1292 - Sa hình'),      (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')),
((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca B-1292 - Đường trường'), (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1'));
GO

INSERT INTO ExaminerSchedule (SessionId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt) VALUES
(
    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca B-1292 - Lý thuyết'),
    (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2'),
    (SELECT UserId FROM [User] WHERE Username = N'examiner_b1292_lt'),
    (SELECT UserId FROM [User] WHERE Username = N'examstaff_hoa'),
    '2026-06-09 08:00:00'
),
(
    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca B-1292 - Sa hình'),
    (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'),
    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1'),
    (SELECT UserId FROM [User] WHERE Username = N'examiner_b1292_sh'),
    (SELECT UserId FROM [User] WHERE Username = N'examstaff_hoa'),
    '2026-06-09 08:05:00'
),
(
    (SELECT SessionId FROM [Session] WHERE SessionName = N'Ca B-1292 - Đường trường'),
    (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'),
    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1'),
    (SELECT UserId FROM [User] WHERE Username = N'examiner_b1292_dt'),
    (SELECT UserId FROM [User] WHERE Username = N'examstaff_hoa'),
    '2026-06-09 08:10:00'
);
GO

;WITH nums AS (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
)
INSERT INTO [User] (Username, Email, PasswordHash, RoleId, [Status])
SELECT
    N'b1292_ts' + RIGHT(N'0' + CAST(n AS NVARCHAR(2)), 2),
    N'b1292.ts' + RIGHT(N'0' + CAST(n AS NVARCHAR(2)), 2) + N'@test.vn',
    N'login123',
    (SELECT RoleId FROM [Role] WHERE RoleName = 'Registrant'),
    1
FROM nums
OPTION (MAXRECURSION 0);
GO

;WITH nums AS (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
)
INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId)
SELECT
    CASE n
        WHEN 1  THEN N'Nguyễn Văn Minh'   WHEN 2  THEN N'Trần Thị Lan'      WHEN 3  THEN N'Lê Hoàng Nam'
        WHEN 4  THEN N'Phạm Thu Hà'       WHEN 5  THEN N'Hoàng Văn Đức'     WHEN 6  THEN N'Võ Thị Mai'
        WHEN 7  THEN N'Đặng Quốc Huy'     WHEN 8  THEN N'Bùi Ngọc Anh'      WHEN 9  THEN N'Ngô Văn Tài'
        WHEN 10 THEN N'Dương Thị Hương'   WHEN 11 THEN N'Lý Văn Phong'      WHEN 12 THEN N'Mai Thị Linh'
        WHEN 13 THEN N'Trịnh Văn Bảo'     WHEN 14 THEN N'Cao Thị Ngọc'     WHEN 15 THEN N'Phan Văn Khánh'
        WHEN 16 THEN N'Hồ Thị Yến'        WHEN 17 THEN N'Đinh Quang Hải'   WHEN 18 THEN N'Lưu Thị Trang'
        WHEN 19 THEN N'Chu Văn Long'     WHEN 20 THEN N'Vũ Thị Hồng'
    END,
    DATEADD(YEAR, - (20 + n), CAST('2000-01-01' AS DATETIME)),
    N'09' + RIGHT(N'00000000' + CAST(912920000 + n AS NVARCHAR(10)), 8),
    CASE WHEN n % 2 = 0 THEN N'Nữ' ELSE N'Nam' END,
    N'0791292' + RIGHT(N'00000' + CAST(n AS NVARCHAR(5)), 5),
    CASE n
        WHEN 1  THEN N'Số 12, ngõ 45 Phố Huế, P. Bạch Mai, Hà Nội'
        WHEN 2  THEN N'Tổ 8, thôn Đông, xã Kim An, huyện Thanh Oai, Hà Nội'
        WHEN 3  THEN N'Ngách 3, ngõ 18 Phố Láng, Đống Đa, Hà Nội'
        WHEN 4  THEN N'Số 56, đường Nguyễn Trãi, Thanh Xuân, Hà Nội'
        WHEN 5  THEN N'Ấp 2, xã Tân Hiệp, huyện Hóc Môn, TP.HCM'
        WHEN 6  THEN N'Số 9, đường Lê Lợi, Q.1, TP.HCM'
        WHEN 7  THEN N'Tổ 15, phường An Hải Bắc, Sơn Trà, Đà Nẵng'
        WHEN 8  THEN N'Số 102, Phan Chu Trinh, Hải Châu, Đà Nẵng'
        WHEN 9  THEN N'Khu phố 4, phường Trần Hưng Đạo, TP. Huế'
        WHEN 10 THEN N'Số 27, đường Bà Triệu, TP. Huế'
        WHEN 11 THEN N'Ngõ 7, phố Minh Khai, Hai Bà Trưng, Hà Nội'
        WHEN 12 THEN N'Số 33, đường Hoàng Văn Thụ, Q. Tân Bình, TP.HCM'
        WHEN 13 THEN N'Tổ 6, thôn Vân, xã Vân Canh, Hoài Đức, Hà Nội'
        WHEN 14 THEN N'Số 88, Phố Vọng, Hai Bà Trưng, Hà Nội'
        WHEN 15 THEN N'Ấp Bình Thọ, phường Trường Thọ, TP. Thủ Đức, TP.HCM'
        WHEN 16 THEN N'Số 41, đường Cầu Giấy, Cầu Giấy, Hà Nội'
        WHEN 17 THEN N'Khu 5, phường Quyết Thắng, TP. Thái Nguyên'
        WHEN 18 THEN N'Số 19, Phố Huế, Hai Bà Trưng, Hà Nội'
        WHEN 19 THEN N'Tổ 12, phường Lê Hồng Phong, TP. Vinh, Nghệ An'
        WHEN 20 THEN N'Số 64, đường Trần Phú, Nha Trang, Khánh Hòa'
    END,
    (SELECT UserId FROM [User] WHERE Username = N'b1292_ts' + RIGHT(N'0' + CAST(n AS NVARCHAR(2)), 2))
FROM nums
OPTION (MAXRECURSION 0);
GO

;WITH nums AS (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
)
INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId)
SELECT
    N'Approved',
    N'',
    p.ProfileId,
    (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')
FROM nums
JOIN Profile p ON p.GovernmentIdNumber = N'0791292' + RIGHT(N'00000' + CAST(nums.n AS NVARCHAR(5)), 5);
GO

;WITH nums AS (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
)
INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, TakeTheory, TakePractical, TakeRoadLayout, TakeOnRoad, ReasonForTaking, PhotoImageUrl, UserId, TakeNo)
SELECT
    RIGHT(N'000' + CAST(n AS NVARCHAR(3)), 3),
    p.FullName,
    p.DateOfBirth,
    p.PhoneNumber,
    p.Sex,
    p.GovernmentIdNumber,
    p.Address,
    1, NULL, 1, 1,
    N'Thi lần đầu - khoá B-1292',
    N'/docs/photos/b1292_' + RIGHT(N'0' + CAST(n AS NVARCHAR(2)), 2) + N'.jpg',
    p.UserId,
    1
FROM nums
JOIN Profile p ON p.GovernmentIdNumber = N'0791292' + RIGHT(N'00000' + CAST(nums.n AS NVARCHAR(5)), 5);
GO

INSERT INTO ExamEnrollment (CandidateId, SessionId)
SELECT
    c.CandidateId,
    s.SessionId
FROM Candidate c
JOIN Profile p ON p.GovernmentIdNumber = c.GovernmentIdNumber
CROSS JOIN [Session] s
WHERE p.GovernmentIdNumber LIKE N'0791292%'
  AND s.SessionName IN (N'Ca B-1292 - Lý thuyết', N'Ca B-1292 - Sa hình', N'Ca B-1292 - Đường trường');
GO

UPDATE er
SET RegistrationStatus = N'Present',
    Notes = N'AllocatedRoom:'
        + CAST(ea.ExamAreaId AS NVARCHAR(10)) + N':'
        + ea.AreaName
FROM ExamRegistration er
JOIN Profile p ON p.ProfileId = er.ProfileId
JOIN Candidate c ON c.GovernmentIdNumber = p.GovernmentIdNumber
JOIN ExamEnrollment ec ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
JOIN Session_ExamArea sea ON sea.SessionId = s.SessionId
JOIN ExamArea ea ON ea.ExamAreaId = sea.ExamAreaId
WHERE s.ExamId = (SELECT ExamId FROM Exam WHERE ExamCode = N'B-1292')
  AND p.GovernmentIdNumber LIKE N'0791292%';
GO

;WITH nums AS (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
)
INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
SELECT DISTINCT
    N'Completed',
    N'BankTransfer',
    N'TXN-B1292-' + RIGHT(N'0' + CAST(n AS NVARCHAR(2)), 2),
    430000.00,
    '2026-06-09 14:00:00',
    (SELECT MIN(ec2.ExamEnrollmentId) FROM ExamEnrollment ec2 WHERE ec2.CandidateId = c.CandidateId)
FROM nums
JOIN Candidate c ON c.CandidateNumber = RIGHT(N'000' + CAST(nums.n AS NVARCHAR(3)), 3)
JOIN Profile p ON p.GovernmentIdNumber = c.GovernmentIdNumber
WHERE p.GovernmentIdNumber LIKE N'0791292%';
GO

-- 5 thí sinh đã thi lý thuyết B-1292 (001–005): 3 đạt, 2 trượt
INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
SELECT ec.ExamEnrollmentId,
       (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'PC-LT2-01'),
       '2026-06-10 07:35:00',
       '2026-06-10 07:52:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'001'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
SELECT ec.ExamEnrollmentId,
       (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'PC-LT2-02'),
       '2026-06-10 07:40:00',
       '2026-06-10 07:58:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'002'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
SELECT ec.ExamEnrollmentId,
       (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'PC-LT2-01'),
       '2026-06-10 07:45:00',
       '2026-06-10 08:02:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'003'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
SELECT ec.ExamEnrollmentId,
       (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'PC-LT2-02'),
       '2026-06-10 07:50:00',
       '2026-06-10 08:08:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'004'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt, SubmittedAt)
SELECT ec.ExamEnrollmentId,
       (SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = N'PC-LT2-01'),
       '2026-06-10 07:55:00',
       '2026-06-10 08:12:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'005'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 1, '2026-06-10 07:55:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'001'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
       33.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'001'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 0, '2026-06-10 08:00:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'002'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
       25.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'002'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 1, '2026-06-10 08:05:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'003'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
       32.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'003'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 0, '2026-06-10 08:10:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'004'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
       28.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'004'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 1, '2026-06-10 08:15:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'005'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết'),
       33.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE c.CandidateNumber = N'005'
  AND s.SessionName = N'Ca B-1292 - Lý thuyết';
GO

-- 2 thí sinh đã thi sa hình B-1292: 001 (đạt), 003 (trượt) - đã đạt lý thuyết
INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 1, '2026-06-10 10:05:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'001'
  AND s.SessionName = N'Ca B-1292 - Sa hình';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'),
       85.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'001'
  AND s.SessionName = N'Ca B-1292 - Sa hình';
GO

INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate)
SELECT ec.ExamEnrollmentId, 0, '2026-06-10 10:35:00'
FROM ExamEnrollment ec
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'003'
  AND s.SessionName = N'Ca B-1292 - Sa hình';
GO

INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score)
SELECT er.ExamResultId,
       (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'),
       62.00
FROM ExamResult er
JOIN ExamEnrollment ec ON er.ExamEnrollmentId = ec.ExamEnrollmentId
JOIN Candidate c ON ec.CandidateId = c.CandidateId
JOIN [Session] s ON ec.SessionId = s.SessionId
WHERE c.CandidateNumber = N'003'
  AND s.SessionName = N'Ca B-1292 - Sa hình';
GO

INSERT INTO Audit (UserId, Action, EntityName, EntityId, NewValue, CreatedAt)
SELECT se.AssignedBy,
       N'ASSIGN',
       N'Session_ExaminerArea',
       CAST(se.SessionId AS NVARCHAR(20)) + N':' + CAST(se.ExamAreaId AS NVARCHAR(20)) + N':' + CAST(se.ExaminerId AS NVARCHAR(20)),
       ea.AreaName,
       ISNULL(se.AssignedAt, GETDATE())
FROM ExaminerSchedule se
JOIN ExamArea ea ON ea.ExamAreaId = se.ExamAreaId
JOIN [Session] s ON s.SessionId = se.SessionId
WHERE s.SessionName LIKE N'Ca B-1292%'
  AND se.ExamAreaId IS NOT NULL;
GO

-- ============================================
-- QUICK PATCH (chạy riêng nếu DB đã seed, không muốn chạy lại toàn bộ DML)
-- Bật ca B-1292 InProgress; tắt ca Lý thuyết B cũ
-- ============================================
UPDATE [Session]
SET [Status] = N'Scheduled'
WHERE SessionName = N'Ca sáng - Lý thuyết B';
GO

UPDATE [Session]
SET [Status] = N'InProgress'
WHERE SessionName LIKE N'Ca B-1292%';
GO

-- Migration: ExamEnrollment section workflow (chờ ký / đã thi)
IF COL_LENGTH('ExamEnrollment', 'SectionStatus') IS NULL
BEGIN
    ALTER TABLE ExamEnrollment ADD SectionStatus NVARCHAR(50) NOT NULL CONSTRAINT DF_ExamEnrollment_SectionStatus DEFAULT N'Pending';
END
GO
IF COL_LENGTH('ExamEnrollment', 'SignaturePrinted') IS NULL
BEGIN
    ALTER TABLE ExamEnrollment ADD SignaturePrinted BIT NOT NULL CONSTRAINT DF_ExamEnrollment_SignaturePrinted DEFAULT 0;
END
GO

IF COL_LENGTH('ScoreDeduction', 'ExamSectionId') IS NULL
BEGIN
    ALTER TABLE ScoreDeduction ADD ExamSectionId INT NULL REFERENCES ExamSection(ExamSectionId);
    ALTER TABLE ScoreDeduction ADD SortOrder INT NOT NULL CONSTRAINT DF_ScoreDeduction_SortOrder DEFAULT 0;
END
GO

IF COL_LENGTH('DeductionRecord', 'OccurrenceCount') IS NULL
BEGIN
    ALTER TABLE DeductionRecord ADD OccurrenceCount INT NOT NULL CONSTRAINT DF_DeductionRecord_OccurrenceCount DEFAULT 1;
END
GO

IF COL_LENGTH('DeductionRecord', 'RecordedAt') IS NULL
BEGIN
    ALTER TABLE DeductionRecord ADD RecordedAt DATETIME NOT NULL CONSTRAINT DF_DeductionRecord_RecordedAt DEFAULT GETDATE();
END
GO

-- Chuẩn hóa SBD: chỉ chữ số (001, 281…), bỏ prefix hạng kiểu B-0001
UPDATE c
SET CandidateNumber = CASE
    WHEN x.num < 1000 THEN RIGHT(N'000' + CAST(x.num AS NVARCHAR(10)), 3)
    ELSE CAST(x.num AS NVARCHAR(10))
END
FROM Candidate c
CROSS APPLY (
    SELECT COALESCE(
        TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT),
        TRY_CAST(c.CandidateNumber AS INT)
    ) AS num
) x
WHERE c.CandidateNumber LIKE N'%-%'
  AND x.num IS NOT NULL;
GO

IF COL_LENGTH('ExamEnrollment', 'ExamDeviceId') IS NULL
BEGIN
    ALTER TABLE ExamEnrollment ADD ExamDeviceId INT NULL REFERENCES ExamDevice(ExamDeviceId);
END
GO

UPDATE ec
SET SectionStatus = N'AwaitingSignature'
FROM ExamEnrollment ec
JOIN TheoryPaper tp ON tp.ExamEnrollmentId = ec.ExamEnrollmentId
WHERE tp.SubmittedAt IS NOT NULL
  AND ec.SectionStatus = N'Pending';
GO

UPDATE ec
SET SectionStatus = N'Done',
    SignaturePrinted = 1
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE s.SessionName = N'Ca B-1292 - Lý thuyết'
  AND c.CandidateNumber IN (N'001', N'002', N'003', N'004', N'005');
GO

UPDATE ec
SET SectionStatus = N'Done',
    SignaturePrinted = 1
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN [Session] s ON s.SessionId = ec.SessionId
WHERE s.SessionName = N'Ca B-1292 - Sa hình'
  AND c.CandidateNumber IN (N'001', N'003');
GO

 -- ============================================
-- DML: 600 QUESTIONS – DLEM_DB_2
-- Based on CSGT-P5 No.2262 (07/05/2025)
-- Run after DDL_DLEM_DB.sql and DML_DLEM_DB.sql (Licence rows required)
-- ============================================

USE DLEM_DB_2;
GO
-- ============================================
-- 1. QUESTION CATEGORIES (5 Chapters)
-- ============================================
INSERT INTO QuestionCategory (CategoryName, Description) VALUES
(N'I', N'Quy định chung và quy tắc giao thông đường bộ'),
(N'II', N'Văn hóa giao thông, đạo đức người lái xe, kỹ năng phòng cháy, chữa cháy và cứu hộ, cứu nạn'),
(N'III', N'Kỹ thuật lái xe'),
(N'IV', N'Cấu tạo và sửa chữa'),
(N'V', N'Báo hiệu đường bộ'),
(N'VI', N'Giải thế sa hình và kỹ năng xử lý tình huống giao thông');

-- ============================================
-- 2. INSERT ALL 600 QUESTIONS
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
-- 3. LICENCE-QUESTION MAPPING (N-N)
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

-- ============================================
-- 4. CANDIDATE ANSWERS - mọi thí sinh lý thuyết đã nộp bài (SubmittedAt NOT NULL)
-- 35 câu (QuestionNumber 1–35), khớp ExamResult.IsPassed (đạt ≥32/35, trượt <32)
-- ============================================
;WITH AnswerPlan AS (
    SELECT tp.TheoryPaperId,
           q.QuestionId,
           q.CorrectAnswer,
           CASE
               WHEN c.CandidateNumber IN (N'022', N'001', N'005')
                    AND q.QuestionNumber IN (3, 7) THEN 1
               WHEN c.CandidateNumber = N'003'
                    AND q.QuestionNumber IN (3, 7, 12) THEN 1
               WHEN c.CandidateNumber = N'004'
                    AND q.QuestionNumber IN (3, 7, 12, 18, 22, 29, 33) THEN 1
               WHEN c.CandidateNumber IN (N'024', N'002')
                    AND q.QuestionNumber IN (3, 7, 12, 18, 22, 25, 29, 33, 34, 35) THEN 1
               ELSE 0
           END AS isWrong
    FROM TheoryPaper tp
    JOIN ExamEnrollment ec ON tp.ExamEnrollmentId = ec.ExamEnrollmentId
    JOIN Candidate c ON ec.CandidateId = c.CandidateId
    JOIN Question q ON q.QuestionNumber BETWEEN 1 AND 35
    WHERE tp.SubmittedAt IS NOT NULL
)
INSERT INTO CandidateAnswer (TheoryPaperId, QuestionId, Answer)
SELECT TheoryPaperId,
       QuestionId,
       CASE
           WHEN isWrong = 1 THEN
               CASE CorrectAnswer
                   WHEN N'A' THEN N'B'
                   WHEN N'B' THEN N'C'
                   WHEN N'C' THEN N'D'
                   ELSE N'A'
               END
           ELSE CorrectAnswer
       END
FROM AnswerPlan;
GO

-- Đồng bộ điểm lý thuyết = số câu đúng / 35 (khớp CandidateAnswer)
UPDATE es
SET Score = v.correctCnt
FROM ExamScore es
JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
JOIN ExamEnrollment ec ON ec.ExamEnrollmentId = er.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
JOIN (
    VALUES
        (N'022',  33),
        (N'024',  25),
        (N'001', 33),
        (N'002', 25),
        (N'003', 32),
        (N'004', 28),
        (N'005', 33)
) AS v(CandidateNumber, correctCnt) ON v.CandidateNumber = c.CandidateNumber
WHERE sec.SectionName = N'Lý thuyết';
GO
