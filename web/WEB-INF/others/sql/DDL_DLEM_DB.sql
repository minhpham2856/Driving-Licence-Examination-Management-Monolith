-- ============================================
-- Database Sschema
-- Driving License Examination Management System
-- ============================================

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

-- User table
-- User ở đây là người dùng hệ thống TRỪ actor CANDIDATE sẽ thực hiện thi bằng cách nhập số báo danh thay vì login
CREATE TABLE [User] (
    UserId INT PRIMARY KEY IDENTITY(1,1),
    Username NVARCHAR(100) NOT NULL,
    Email NVARCHAR(255) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    RoleId INT NOT NULL REFERENCES [Role](RoleId),
    IsActive BIT NOT NULL DEFAULT 1
);
GO

-- Profile table
-- Profile đại diện cho tất cả mọi thông tin, tài liệu của USER không liên quan đến CANDIDATE
CREATE TABLE Profile (
    ProfileId INT PRIMARY KEY IDENTITY(1,1),
    FullName NVARCHAR(255) NOT NULL,
    DateOfBirth DATETIME NOT NULL,
    PhoneNumber NVARCHAR(20) NOT NULL,
    Sex BIT NOT NULL,
    GovernmentIdNumber NVARCHAR(100) NOT NULL UNIQUE,
    Address NVARCHAR(500),
    UserId INT NOT NULL REFERENCES [User](UserId)
);
GO

-- Document table
-- Mỗi 1 profile có nhiều documents
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
    UpgradeFromLicenceId INT NULL REFERENCES Licence(LicenceId)	-- tt này dùng để xác định xem hạng bằng nào là thi lên từ hạng bằng nào (PRECONDITION): không đăng ký lẻ được
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
-- Exam ở đây thể hiện cho kỳ thi
CREATE TABLE Exam (
    ExamId INT PRIMARY KEY IDENTITY(1,1),
    ExamCode NVARCHAR(50) NOT NULL UNIQUE,	-- tt này thể hiện cho "khoá thi" (là thuật ngữ chuẩn) chứ không phải mã kỳ thi
    ExamDate DATETIME NOT NULL,
    CentreName NVARCHAR(255) NOT NULL,	-- 1 kỳ thi có thể diễn ra tại trung tâm khác nên cần để thông tin cho người đăng ký
    [Status] NVARCHAR(50) NOT NULL,
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId)
);
GO

-- Session table
-- Session là ca thi
CREATE TABLE [Session] (
    SessionId INT PRIMARY KEY IDENTITY(1,1),
    SessionName NVARCHAR(100) NOT NULL, -- chỉ có ca sáng hoặc ca chiều (vào ngày thi chỉ thi ca sáng hay thi cả 2 ca, và thời gian cho từng ca mới được quyết định)
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NOT NULL,
    [Status] NVARCHAR(50) NOT NULL,
    ExamId INT NOT NULL REFERENCES Exam(ExamId),
    CHECK (EndTime > StartTime)
);
GO

-- ExamSection table
-- Phần thi: CHUẨN HOÁ GỒM 3 PHẦN THEO THUẬT NGỮ CHUẨN:
-- 1. Lý thuyết
-- 2. (Thực hành) trong hình
-- 3. (Thực hành) trên đường
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
    DurationMinutes INT NULL, -- 0 -> không có thời gian (phần thi trong hình của A, A1 không tính thời gian)
    UNIQUE (LicenceId, ExamSectionId)
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
-- Thể hiện cho địa điểm thi cụ thể, không phải KHU VỰC THI
CREATE TABLE ExamArea (
    ExamAreaId INT PRIMARY KEY IDENTITY(1,1),
    AreaName NVARCHAR(100) NOT NULL, -- Đặt tên theo hạng gplx trừ phòng thử tục -> ví dụ: phòng 102 (phòng thủ tục), phòng thi LT 102, sân thi số 1, sân thi số 2, RIÊNG đường thi thì không cố định
    AreaType NVARCHAR(50) NOT NULL, -- (phòng thủ tực, phòng thi, sân thi, đường thi)
    Capacity INT NULL,
    [Location] NVARCHAR(255) NOT NULL, -- ví dụ: tầng 2, toà A, khu sân thi thực hành,...
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

-- ExaminerSchedule: phân công shv theo ca (Session), kỳ thi (Exam), phần thi (ExamSection), địa điểm (ExamArea).
CREATE TABLE ExaminerSchedule (
    ExaminerScheduleId INT PRIMARY KEY IDENTITY(1,1),
    SessionId INT NOT NULL REFERENCES [Session](SessionId),
    ExaminerId INT NOT NULL REFERENCES [User](UserId),
    ExamSectionId INT NULL REFERENCES ExamSection(ExamSectionId),
    ExamAreaId INT NULL REFERENCES ExamArea(ExamAreaId),
    AssignedBy INT NULL REFERENCES [User](UserId),
    AssignedAt DATETIME NULL DEFAULT GETDATE(),
    UNIQUE (SessionId, ExaminerId),
    UNIQUE (ExaminerId),
    FOREIGN KEY (SessionId, ExamSectionId) REFERENCES Session_ExamSection(SessionId, ExamSectionId),
    FOREIGN KEY (SessionId, ExamAreaId) REFERENCES Session_ExamArea(SessionId, ExamAreaId)
);
GO

-- ExamDevice table
-- Thiết bị thi: bao gồm cả máy thi, và xe thi
CREATE TABLE ExamDevice (
    ExamDeviceId INT PRIMARY KEY IDENTITY(1,1),
    DeviceName NVARCHAR(100) NOT NULL, -- ví dụ: A1-01, B-02, B-03, PC-101, PC-211,
    DeviceType NVARCHAR(50) NOT NULL, -- máy tính, xe máy, oto con, oto tải,... 
    IsActive BIT NOT NULL,
    ExamAreaId INT NOT NULL REFERENCES ExamArea(ExamAreaId)
);
GO

-- Candidate table
-- Candidate thể hiện cho các thí sinh được import excel mọi thao tác đều không liên quan tới các tt như
-- User, ExamRegistration, Profile,....
CREATE TABLE Candidate (
    CandidateId INT PRIMARY KEY IDENTITY(1,1),
    CandidateNumber NVARCHAR(50) NOT NULL, -- số báo danh
	-- sbd ko unique vì sẽ mỗi Exam sẽ bị trùng sbd: ví dụ khoá thi OTO-123 có sbd 001 -> 240, A1-123 có sbd 001 -> 675 là vẫn được
	-- sbd theo format số: ví dụ 500 thí sinh sẽ có sbd từ 001 -> 500

	-- personal info
    FullName NVARCHAR(255) NOT NULL,
    DateOfBirth DATETIME NOT NULL,
    PhoneNumber NVARCHAR(20),
    Sex BIT NOT NUll,
    GovernmentIdNumber NVARCHAR(100), -- số căn cước cũng không unique vì 1 thí sinh có thể thi nhiều lần/ nhiều kỳ thi
    Address NVARCHAR(500),

	-- tracking
    TakeTheory BIT,
    TakeLayout BIT,
    TakeRoad BIT,
	TakeNo INT NOT NULL, -- lần thi thứ ?
    ReasonForTaking NVARCHAR(355),
    PhotoImageUrl NVARCHAR(500),
    IsAbsent BIT NOT NULL DEFAULT 0,
    IsSuspended BIT NOT NULL DEFAULT 0,
);
GO

-- ExamEnrollment table
-- Thể hiện mqh giữa candidate và exam
CREATE TABLE ExamEnrollment (
	ExamEnrollmentId INT PRIMARY KEY IDENTITY(1,1),
    CandidateId INT NOT NULL REFERENCES Candidate(CandidateId),
    SessionId INT NOT NULL REFERENCES [Session](SessionId),
    SectionStatus NVARCHAR(50) NOT NULL,
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
    IsActive BIT NOT NULL DEFAULT 1,
);
GO

-- Payment table
CREATE TABLE Payment (
    PaymentId INT PRIMARY KEY IDENTITY(1,1),
    PaymentStatus NVARCHAR(50) NOT NULL,    
    PaymentMethod NVARCHAR(50) NOT NULL,     
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

-- Licence_Fee junction table
CREATE TABLE Licence_Fee (
    LicenceFeeId INT PRIMARY KEY IDENTITY(1,1),
    LicenceId INT NULL REFERENCES Licence(LicenceId),  -- NULL = phí chung, không theo hạng
    FeeId INT NOT NULL REFERENCES Fee(FeeId),
    Amount DECIMAL(18,2), 
	CHECK (Amount >= 0),
    UNIQUE (LicenceId, FeeId)
);

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
    ImageUrl NVARCHAR(500), -- url của ảnh câu hỏi đã chứa câu hỏi và lựa chọn
    CorrectAnswer NVARCHAR(10) NOT NULL,
    IsCritical BIT NOT NULL DEFAULT 0, -- câu điểm liệt
    QuestionCategoryId INT NOT NULL REFERENCES QuestionCategory(QuestionCategoryId)
);
GO

-- Licence_Question junction table
-- 1 câu hỏi có thể sd cho nhiều hạng và 1 hạng có thể có nhiều câu hỏi
CREATE TABLE Licence_Question (
    LicenceQuestionId INT PRIMARY KEY IDENTITY(1,1),
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    QuestionId INT NOT NULL REFERENCES Question(QuestionId),
    UNIQUE (LicenceId, QuestionId)
);
GO

-- ============================== EXAM COMPONENTS ==============================
-- TheoryPaper table
-- đề thi lý thuyết của thí sinh
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
-- bảng lỗi để trừ điểm (theo hạng gplx)
CREATE TABLE ScoreDeduction (
    ScoreDeductionId INT PRIMARY KEY IDENTITY(1,1),
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    [Reason] NVARCHAR(500) NOT NULL,
    Points DECIMAL(5,2) NOT NULL,
    IsCritical BIT NOT NULL DEFAULT 0,
    ExamSectionId INT NOT NULL REFERENCES ExamSection(ExamSectionId),
    CHECK (Points > 0)
);
GO

-- DeductionRecord (số lần lỗi + thời điểm nhập gần nhất)
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