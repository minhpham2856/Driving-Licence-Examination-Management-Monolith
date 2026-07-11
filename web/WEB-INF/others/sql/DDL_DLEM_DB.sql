-- ============================================
-- Database Schema
-- Driving License Examination Management System
-- DB: DLEM_DB_2
-- Ghi chú examstaff / public-call (schema DLEM_DB_2):
--   - Một kỳ thi = một hàng Exam (StartTime/EndTime trên Exam)
--   - StartTime: managing staff gán khi tạo kỳ; EndTime: NULL cho đến khi examstaff kết thúc kỳ
--   - sessionId trên URL/UI = ExamId
--   - Trạng thái LT/TH trên ExamEnrollmentSection
-- ============================================

USE master;
GO

IF EXISTS (SELECT 1 FROM sys.databases WHERE name = N'DLEM_DB_2')
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
CREATE TABLE [Role] (
    RoleId INT PRIMARY KEY IDENTITY(1,1),
    RoleName NVARCHAR(50) NOT NULL UNIQUE
);
GO

-- User: người dùng hệ thống (không gồm thí sinh import ngày thi)
CREATE TABLE [User] (
    UserId INT PRIMARY KEY IDENTITY(1,1),
    Username NVARCHAR(100) NOT NULL,
    Email NVARCHAR(255) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    RoleId INT NOT NULL REFERENCES [Role](RoleId),
    IsActive BIT NOT NULL DEFAULT 1
);
GO

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

CREATE TABLE Document (
    DocumentId INT PRIMARY KEY IDENTITY(1,1),
    DocumentType NVARCHAR(50) NOT NULL,
    DocumentUrl NVARCHAR(500) NOT NULL,
    Notes NVARCHAR(255),
    ProfileId INT NOT NULL REFERENCES Profile(ProfileId)
);
GO

CREATE TABLE Licence (
    LicenceId INT PRIMARY KEY IDENTITY(1,1),
    LicenceClass NVARCHAR(50) NOT NULL UNIQUE,
    Description NVARCHAR(500),
    MinimumAge INT NOT NULL,
    ValidForYears INT NOT NULL,
    UpgradeFromLicenceId INT NULL REFERENCES Licence(LicenceId)
);
GO

-- Chỉ dùng đăng ký thi qua trung tâm (không dùng trong ngày thi import DSTS)
CREATE TABLE ExamRegistration (
    ExamRegistrationId INT PRIMARY KEY IDENTITY(1,1),
    RegistrationStatus NVARCHAR(50) NOT NULL,
    Notes NVARCHAR(MAX),
    ProfileId INT NOT NULL REFERENCES Profile(ProfileId),
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId)
);
GO

-- ============================== EXAM ==============================
CREATE TABLE Exam (
    ExamId INT PRIMARY KEY IDENTITY(1,1),
    ExamCode NVARCHAR(50) NOT NULL UNIQUE,
    ExamDate DATETIME NOT NULL,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NULL,
    [Status] NVARCHAR(50) NOT NULL,
    CentreName NVARCHAR(255) NOT NULL,
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    CHECK (EndTime IS NULL OR EndTime > StartTime)
);
GO

CREATE TABLE ExamSection (
    ExamSectionId INT PRIMARY KEY IDENTITY(1,1),
    SectionType NVARCHAR(100) NOT NULL,
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    DurationMinutes INT NULL,
    ExamId INT NOT NULL REFERENCES Exam(ExamId),
    UNIQUE (ExamId, SectionType)
);
GO

-- Khu vực thi (khuôn viên)
CREATE TABLE ExamZone (
    ExamZoneId INT PRIMARY KEY IDENTITY(1,1),
    ZoneName NVARCHAR(100) NOT NULL,
    [Location] NVARCHAR(255) NOT NULL,
    IsActive BIT NOT NULL DEFAULT 1
);
GO

-- Địa điểm cụ thể (phòng / sân / đường) thuộc một ExamZone
CREATE TABLE ExamArea (
    ExamAreaId INT PRIMARY KEY IDENTITY(1,1),
    AreaName NVARCHAR(100) NOT NULL,
    AreaType NVARCHAR(50) NOT NULL,
    Capacity INT NULL,
    [Location] NVARCHAR(255) NOT NULL,
    ExamZoneId INT NOT NULL REFERENCES ExamZone(ExamZoneId),
    CHECK (Capacity IS NULL OR Capacity > 0)
);
GO

CREATE TABLE Exam_ExamArea (
    ExamExamAreaId INT PRIMARY KEY IDENTITY(1,1),
    ExamId INT NOT NULL REFERENCES Exam(ExamId),
    ExamAreaId INT NOT NULL REFERENCES ExamArea(ExamAreaId),
    UNIQUE (ExamId, ExamAreaId)
);
GO

CREATE TABLE ExaminerSchedule (
    ExaminerScheduleId INT PRIMARY KEY IDENTITY(1,1),
    ExamId INT NOT NULL REFERENCES Exam(ExamId),
    ExaminerId INT NOT NULL REFERENCES [User](UserId),
    ExamSectionId INT NULL REFERENCES ExamSection(ExamSectionId),
    ExamAreaId INT NULL REFERENCES ExamArea(ExamAreaId),
    AssignedBy INT NULL REFERENCES [User](UserId),
    AssignedAt DATETIME NULL DEFAULT GETDATE(),
    UNIQUE (ExamId, ExaminerId)
);
GO

CREATE TABLE ExamDevice (
    ExamDeviceId INT PRIMARY KEY IDENTITY(1,1),
    DeviceName NVARCHAR(100) NOT NULL,
    DeviceType NVARCHAR(50) NOT NULL, -- Máy tính | Mô tô | Xe con
    IsActive BIT NOT NULL,
    ExamAreaId INT NOT NULL REFERENCES ExamArea(ExamAreaId)
);
GO

-- Thí sinh ngày thi (tách biệt User/Profile)
CREATE TABLE Candidate (
    CandidateId INT PRIMARY KEY IDENTITY(1,1),
    CandidateNumber NVARCHAR(50) NOT NULL,
    FullName NVARCHAR(255) NOT NULL,
    DateOfBirth DATETIME NOT NULL,
    PhoneNumber NVARCHAR(20),
    Email NVARCHAR(255),
    Sex BIT NOT NULL,
    GovernmentIdNumber NVARCHAR(100),
    Address NVARCHAR(500),
    TakeTheory BIT,
    TakeLayout BIT,
    TakeNo INT NOT NULL,
    ReasonForTaking NVARCHAR(355),
    PhotoImageUrl NVARCHAR(500),
    IsAbsent BIT NOT NULL DEFAULT 0,
    IsSuspended BIT NOT NULL DEFAULT 0
);
GO

CREATE TABLE ExamEnrollment (
    ExamEnrollmentId INT PRIMARY KEY IDENTITY(1,1),
    CandidateId INT NOT NULL REFERENCES Candidate(CandidateId),
    ExamId INT NOT NULL REFERENCES Exam(ExamId),
    AllocatedExamAreaId INT NULL REFERENCES ExamArea(ExamAreaId),
    ExamDeviceId INT NULL REFERENCES ExamDevice(ExamDeviceId),
    UNIQUE (CandidateId, ExamId)
);
GO

CREATE TABLE ExamEnrollmentSection (
    ExamEnrollmentSectionId INT PRIMARY KEY IDENTITY(1,1),
    ExamEnrollmentId INT NOT NULL REFERENCES ExamEnrollment(ExamEnrollmentId),
    ExamSectionId INT NOT NULL REFERENCES ExamSection(ExamSectionId),
    ExamAreaId INT NULL REFERENCES ExamArea(ExamAreaId),
    ExamDeviceId INT NULL REFERENCES ExamDevice(ExamDeviceId),
    Status NVARCHAR(50) NOT NULL DEFAULT N'Pending',
    AllocatedAt DATETIME NULL,
    AllocatedBy INT NULL REFERENCES [User](UserId),
    StartedAt DATETIME NULL,
    CompletedAt DATETIME NULL,
    UNIQUE (ExamEnrollmentId, ExamSectionId)
);
GO

-- ============================== PAYMENT ==============================
CREATE TABLE Fee (
    FeeId INT PRIMARY KEY IDENTITY(1,1),
    FeeName NVARCHAR(100) NOT NULL,
    FeeType NVARCHAR(50) NOT NULL,
    IsActive BIT NOT NULL DEFAULT 1
);
GO

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

CREATE TABLE Payment_Fee (
    PaymentFeeId INT PRIMARY KEY IDENTITY(1,1),
    PaymentId INT NOT NULL REFERENCES Payment(PaymentId),
    FeeId INT NOT NULL REFERENCES Fee(FeeId),
    UNIQUE (PaymentId, FeeId)
);
GO

CREATE TABLE Licence_Fee (
    LicenceFeeId INT PRIMARY KEY IDENTITY(1,1),
    LicenceId INT NULL REFERENCES Licence(LicenceId),
    FeeId INT NOT NULL REFERENCES Fee(FeeId),
    Amount DECIMAL(18,2),
    CHECK (Amount >= 0),
    UNIQUE (LicenceId, FeeId)
);
GO

-- ============================== QUESTION ==============================
CREATE TABLE QuestionCategory (
    QuestionCategoryId INT PRIMARY KEY IDENTITY(1,1),
    CategoryName NVARCHAR(100) NOT NULL UNIQUE,
    Description NVARCHAR(500)
);
GO

CREATE TABLE Question (
    QuestionId INT PRIMARY KEY IDENTITY(1,1),
    QuestionNumber INT NOT NULL,
    ImageUrl NVARCHAR(500),
    CorrectAnswer NVARCHAR(10) NOT NULL,
    IsCritical BIT NOT NULL DEFAULT 0,
    QuestionCategoryId INT NOT NULL REFERENCES QuestionCategory(QuestionCategoryId)
);
GO

CREATE TABLE Licence_Question (
    LicenceQuestionId INT PRIMARY KEY IDENTITY(1,1),
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    QuestionId INT NOT NULL REFERENCES Question(QuestionId),
    UNIQUE (LicenceId, QuestionId)
);
GO

-- ============================== EXAM COMPONENTS ==============================
CREATE TABLE TheoryPaper (
    TheoryPaperId INT PRIMARY KEY IDENTITY(1,1),
    ExamEnrollmentSectionId INT NOT NULL REFERENCES ExamEnrollmentSection(ExamEnrollmentSectionId),
    StartedAt DATETIME,
    SubmittedAt DATETIME,
    CHECK (SubmittedAt IS NULL OR SubmittedAt >= StartedAt)
);
GO

CREATE TABLE CandidateAnswer (
    CandidateAnswerId INT PRIMARY KEY IDENTITY(1,1),
    TheoryPaperId INT NOT NULL REFERENCES TheoryPaper(TheoryPaperId),
    QuestionId INT NOT NULL REFERENCES Question(QuestionId),
    Answer NVARCHAR(10),
    UNIQUE (TheoryPaperId, QuestionId)
);
GO

CREATE TABLE ExamResult (
    ExamResultId INT PRIMARY KEY IDENTITY(1,1),
    ExamEnrollmentId INT NOT NULL REFERENCES ExamEnrollment(ExamEnrollmentId),
    IsPassed BIT NOT NULL,
    ResultDate DATETIME NOT NULL DEFAULT GETDATE(),
    UNIQUE (ExamEnrollmentId)
);
GO

CREATE TABLE ExamScore (
    ExamScoreId INT PRIMARY KEY IDENTITY(1,1),
    ExamResultId INT NOT NULL REFERENCES ExamResult(ExamResultId),
    ExamSectionId INT NOT NULL REFERENCES ExamSection(ExamSectionId),
    Score DECIMAL(5,2) NOT NULL,
    CHECK (Score >= 0 AND Score <= 100),
    UNIQUE (ExamResultId, ExamSectionId)
);
GO

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

-- Migration (DB đã tạo trước khi EndTime nullable):
-- ALTER TABLE Exam ALTER COLUMN EndTime DATETIME NULL;
-- ALTER TABLE Exam DROP CONSTRAINT CK__Exam__*;  -- tên constraint tùy instance
-- ALTER TABLE Exam ADD CONSTRAINT CK_Exam_EndTimeAfterStart CHECK (EndTime IS NULL OR EndTime > StartTime);
GO
