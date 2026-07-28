-- ============================================
-- Database Schema (unified)
-- Driving License Examination Management System
-- DB: DLEM_DB_2
-- Ghi chú schema hợp nhất (đăng ký + thi):
--   - ExamDates / RegistrationDates / OfficialExamCandidate: luồng đăng ký + police
--   - ExamRegistration.IsRetake, OfficialExamCandidate.ExamParticipationType
--   - Exam.SourceExamDateId, ExamEnrollment.ExamRegistrationId
--   - Exam.ExamPassword, CandidateViolation, CheckedIn*, ResultPrintedAt: luồng thi examiner/candidate
-- Chỉ chạy file này (+ DML). Không chạy DDL_DLEM_DB_2_POLICE.sql.
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

-- User: người dùng hệ thống
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

CREATE TABLE DocumentType (
    DocumentTypeId INT PRIMARY KEY IDENTITY(1,1),
    [Type] NVARCHAR(100) NOT NULL UNIQUE
);
GO

CREATE TABLE Document (
    DocumentId INT PRIMARY KEY IDENTITY(1,1),
    DocumentTypeId INT NOT NULL REFERENCES DocumentType(DocumentTypeId),
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
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    IsRetake BIT NOT NULL DEFAULT 0
);
GO

-- Ngày thi dự kiến do managing staff tạo theo hạng GPLX (LicenceId -> Licence.LicenceId)
CREATE TABLE ExamDates (
    ExamDateId INT PRIMARY KEY IDENTITY(1,1),
    ExamDate DATE NOT NULL,
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    [Status] NVARCHAR(20) NOT NULL
        CONSTRAINT DF_ExamDates_Status DEFAULT N'Open',
    PoliceStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_ExamDates_PoliceStatus DEFAULT N'NOT_SENT',
    CancelReason NVARCHAR(500) NULL,
    CancelledAt DATETIME2 NULL,
    CancelledBy INT NULL REFERENCES [User](UserId),
    CancelledRegistrationCount INT NULL,
    CONSTRAINT CK_ExamDates_Status
        CHECK ([Status] IN (N'Open', N'Locked', N'Cancelled')),
    CONSTRAINT CK_ExamDates_PoliceStatus
        CHECK (PoliceStatus IN (N'NOT_SENT', N'PENDING', N'COMPLETED')),
    CONSTRAINT CK_ExamDates_CancelledRegistrationCount
        CHECK (CancelledRegistrationCount IS NULL OR CancelledRegistrationCount >= 0)
);
GO

-- Theo nghiệp vụ hiện tại: một ngày chỉ mở một ngày thi dự kiến, không phụ thuộc hạng GPLX
CREATE UNIQUE INDEX UX_ExamDates_ExamDate
    ON ExamDates(ExamDate);
GO

-- N–N ExamRegistration - ExamDates: thí sinh chọn ngày dự kiến; IsActive đánh dấu lựa chọn còn hiệu lực
-- Police duyệt/từ chối trực tiếp trên từng dòng của bảng nối này
CREATE TABLE RegistrationDates (
    RegistrationDateId INT PRIMARY KEY IDENTITY(1,1),
    ExamRegistrationId INT NOT NULL REFERENCES ExamRegistration(ExamRegistrationId),
    ExamDateId INT NOT NULL REFERENCES ExamDates(ExamDateId),
    IsActive BIT NOT NULL DEFAULT 1,
    PoliceStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_RegistrationDates_PoliceStatus DEFAULT N'NOT_SENT',
    PoliceReason NVARCHAR(500) NULL,
    OfficialCandidateNumber NVARCHAR(50) NULL,
    CONSTRAINT UQ_RegistrationDates_Registration_Date
        UNIQUE (ExamRegistrationId, ExamDateId),
    CONSTRAINT CK_RegistrationDates_PoliceStatus
        CHECK (PoliceStatus IN (N'NOT_SENT', N'PENDING', N'APPROVED', N'REJECTED'))
);
GO

CREATE INDEX IX_RegistrationDates_ExamDate_PoliceStatus
    ON RegistrationDates(ExamDateId, PoliceStatus, IsActive);
GO

-- Số báo danh chỉ cần duy nhất trong cùng danh sách ngày dự kiến
CREATE UNIQUE INDEX UX_RegistrationDates_Date_CandidateNumber
    ON RegistrationDates(ExamDateId, OfficialCandidateNumber)
    WHERE OfficialCandidateNumber IS NOT NULL;
GO

-- Danh sách thi chính thức do Police Staff lập. ExamRegistrationId NULL
-- đối với thí sinh đến từ đơn vị khác (không có tài khoản tại trung tâm)
CREATE TABLE OfficialExamCandidate (
    OfficialExamCandidateId INT PRIMARY KEY IDENTITY(1,1),
    ExamDateId INT NOT NULL REFERENCES ExamDates(ExamDateId),
    ExamRegistrationId INT NULL REFERENCES ExamRegistration(ExamRegistrationId),
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    CandidateNumber NVARCHAR(50) NULL,
    FullName NVARCHAR(255) NOT NULL,
    DateOfBirth DATE NOT NULL,
    GovernmentIdNumber NVARCHAR(100) NOT NULL,
    PhoneNumber NVARCHAR(20) NOT NULL,
    Email NVARCHAR(255) NOT NULL,
    SourceUnitCode NVARCHAR(50) NOT NULL,
    SourceUnitName NVARCHAR(255) NOT NULL,
    ExamParticipationType NVARCHAR(30) NOT NULL DEFAULT N'FULL_EXAM',
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UNIQUE (ExamDateId, GovernmentIdNumber)
);
GO

CREATE UNIQUE INDEX UX_OfficialExamCandidate_Date_Number
    ON OfficialExamCandidate(ExamDateId, CandidateNumber)
    WHERE CandidateNumber IS NOT NULL;
GO

CREATE INDEX IX_OfficialExamCandidate_Date
    ON OfficialExamCandidate(ExamDateId);
GO

-- ============================== EXAM ==============================
-- SourceExamDateId: phiên thi chính thức tạo từ ngày dự kiến nào
CREATE TABLE Exam (
    ExamId INT PRIMARY KEY IDENTITY(1,1),
    ExamCode NVARCHAR(50) NOT NULL UNIQUE,
    ExamDate DATETIME NOT NULL,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NULL,
    [Status] NVARCHAR(50) NOT NULL,
    ExamPassword NVARCHAR(255) NULL,
    CentreName NVARCHAR(255) NOT NULL,
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId),
    SourceExamDateId INT NULL REFERENCES ExamDates(ExamDateId),
    CONSTRAINT CK_Exam_EndTimeAfterStart
        CHECK (EndTime IS NULL OR EndTime > StartTime)
);
GO

-- Một ngày dự kiến chỉ sinh tối đa một phiên thi chính thức
CREATE UNIQUE INDEX UX_Exam_SourceExamDateId
    ON Exam(SourceExamDateId)
    WHERE SourceExamDateId IS NOT NULL;
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

-- Địa điểm cụ thể (phòng / sân) thuộc một ExamZone
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
    DeviceType NVARCHAR(50) NOT NULL, -- Máy tính | Mô tô | Mô tô ba bánh
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
    IsSuspended BIT NOT NULL DEFAULT 0,
    SourceUnitCode NVARCHAR(50) NULL,
    SourceUnitName NVARCHAR(255) NULL
);
GO

-- ExamRegistrationId nối thí sinh trong phiên chính thức về tài khoản Registrant
CREATE TABLE ExamEnrollment (
    ExamEnrollmentId INT PRIMARY KEY IDENTITY(1,1),
    CandidateId INT NOT NULL REFERENCES Candidate(CandidateId),
    ExamId INT NOT NULL REFERENCES Exam(ExamId),
    ExamRegistrationId INT NULL REFERENCES ExamRegistration(ExamRegistrationId),
    AllocatedExamAreaId INT NULL REFERENCES ExamArea(ExamAreaId),
    ExamDeviceId INT NULL REFERENCES ExamDevice(ExamDeviceId),
    UNIQUE (CandidateId, ExamId)
);
GO

-- Một hồ sơ đăng ký không được xuất hiện hai lần trong cùng một phiên
CREATE UNIQUE INDEX UX_ExamEnrollment_Exam_Registration
    ON ExamEnrollment(ExamId, ExamRegistrationId)
    WHERE ExamRegistrationId IS NOT NULL;
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
    CheckedInAt DATETIME NULL,
    CheckedInBy INT NULL REFERENCES [User](UserId),
    StartedAt DATETIME NULL,
    CompletedAt DATETIME NULL,
    ResultPrintedAt DATETIME NULL,
    UNIQUE (ExamEnrollmentId, ExamSectionId)
);
GO

CREATE TABLE CandidateViolation (
    CandidateViolationId INT PRIMARY KEY IDENTITY(1,1),
    ExamEnrollmentSectionId INT NOT NULL REFERENCES ExamEnrollmentSection(ExamEnrollmentSectionId),
    Reason NVARCHAR(100) NOT NULL,
    Details NVARCHAR(2000),
    EvidenceUrl NVARCHAR(500) NOT NULL,
    CreatedBy INT NOT NULL REFERENCES [User](UserId),
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
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

-- ============================== INDEXES ==============================

CREATE INDEX IX_ExamDates_Status_PoliceStatus_Date
    ON ExamDates([Status], PoliceStatus, ExamDate);
GO

CREATE INDEX IX_ExamRegistration_Profile_Status
    ON ExamRegistration(ProfileId, RegistrationStatus);
GO

CREATE INDEX IX_Document_Profile_Type
    ON Document(ProfileId, DocumentTypeId);
GO

CREATE INDEX IX_Exam_Date_Status
    ON Exam(ExamDate, [Status]);
GO

CREATE INDEX IX_ExamEnrollment_Exam
    ON ExamEnrollment(ExamId);
GO
