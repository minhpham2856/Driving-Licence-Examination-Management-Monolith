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

-- ExamRegistration table
CREATE TABLE ExamRegistration (
    ExamRegistrationId INT PRIMARY KEY IDENTITY(1,1),
    RegistrationStatus NVARCHAR(50) NOT NULL,
    Notes NVARCHAR(MAX),
    ProfileId INT NOT NULL REFERENCES Profile(ProfileId),
    LicenceId INT NOT NULL REFERENCES Licence(LicenceId)
);
GO

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
    AreaType NVARCHAR(50) NOT NULL,
    Capacity INT NOT NULL,
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

-- Session_Examiner: phân công giám khảo theo ca (Session), kỳ thi (Exam), phần thi (ExamSection), phòng (ExamArea).
-- ExamId / ExamSectionId / ExamAreaId nullable để tương thích INSERT legacy (SessionId, ExaminerId) từ code hiện tại.
CREATE TABLE Session_Examiner (
    SessionExaminerId INT PRIMARY KEY IDENTITY(1,1),
    SessionId INT NOT NULL REFERENCES [Session](SessionId),
    ExaminerId INT NOT NULL REFERENCES [User](UserId),
    ExamId INT NULL REFERENCES Exam(ExamId),
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

-- Payment table (cần tạo sau Candidate)
CREATE TABLE Payment (
    PaymentId INT PRIMARY KEY IDENTITY(1,1),
    PaymentStatus NVARCHAR(50) NOT NULL,
    PaymentMethod NVARCHAR(50) NOT NULL,
    TransactionReference NVARCHAR(255) UNIQUE,
    TotalAmount DECIMAL(18,2) NOT NULL,
    PaidAt DATETIME,
    CandidateId INT NOT NULL, -- FK sẽ thêm sau khi tạo Candidate
    ExamId INT NOT NULL REFERENCES Exam(ExamId),
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

-- Candidate table
CREATE TABLE Candidate (
    CandidateId INT PRIMARY KEY IDENTITY(1,1),
    CandidateNumber NVARCHAR(50) NOT NULL UNIQUE,
    FullName NVARCHAR(255) NOT NULL,
    DateOfBirth DATETIME NOT NULL,
    PhoneNumber NVARCHAR(20),
    Sex NVARCHAR(10),
    GovernmentIdNumber NVARCHAR(100) UNIQUE,
    Address NVARCHAR(500),
    TakeTheory BIT,
    TakePractical BIT,
    TakeRoadLayout BIT,
    TakeOnRoad BIT,
    ReasonForTaking NVARCHAR(355),
    PhotoImageUrl NVARCHAR(500),
    IsAbsent BIT NOT NULL DEFAULT 0,
    IsSuspended BIT NOT NULL DEFAULT 0,
    UserId INT NULL REFERENCES [User](UserId),
    ExamRegistrationId INT NOT NULL REFERENCES ExamRegistration(ExamRegistrationId)
);
GO

-- Thêm FK cho Payment.CandidateId sau khi có Candidate
ALTER TABLE Payment ADD FOREIGN KEY (CandidateId) REFERENCES Candidate(CandidateId);
GO

-- Exam_Candidate junction table
CREATE TABLE Exam_Candidate (
    ExamCandidateId INT PRIMARY KEY IDENTITY(1,1),
    ExamId INT NOT NULL REFERENCES Exam(ExamId),
    CandidateId INT NOT NULL REFERENCES Candidate(CandidateId),
    SessionId INT NOT NULL REFERENCES Session(SessionId),
    SectionStatus NVARCHAR(50) NOT NULL DEFAULT N'Pending',
    SignaturePrinted BIT NOT NULL DEFAULT 0,
    ExamDeviceId INT NULL REFERENCES ExamDevice(ExamDeviceId),
    UNIQUE (ExamId, CandidateId, SessionId)
);
GO

-- TheoryPaper table
CREATE TABLE TheoryPaper (
    TheoryPaperId INT PRIMARY KEY IDENTITY(1,1),
    ExamCandidateId INT NOT NULL REFERENCES Exam_Candidate(ExamCandidateId),
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
    ExamCandidateId INT NOT NULL REFERENCES Exam_Candidate(ExamCandidateId),
    IsPassed BIT NOT NULL,
    ResultDate DATETIME NOT NULL DEFAULT GETDATE(),
    UNIQUE (ExamCandidateId)
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

-- Score_Deduction junction table (số lần lỗi + thời điểm nhập gần nhất)
CREATE TABLE Score_Deduction (
    ScoreDeductionDetailId INT PRIMARY KEY IDENTITY(1,1),
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
CREATE INDEX IX_Candidate_ExamRegistrationId ON Candidate(ExamRegistrationId);
CREATE INDEX IX_Candidate_CandidateNumber ON Candidate(CandidateNumber);
CREATE INDEX IX_Payment_CandidateId ON Payment(CandidateId);
CREATE INDEX IX_Payment_ExamId ON Payment(ExamId);
CREATE INDEX IX_TheoryPaper_ExamCandidateId ON TheoryPaper(ExamCandidateId);
CREATE INDEX IX_CandidateAnswer_TheoryPaperId ON CandidateAnswer(TheoryPaperId);
CREATE INDEX IX_ExamResult_ExamCandidateId ON ExamResult(ExamCandidateId);
CREATE INDEX IX_Audit_CreatedAt ON Audit(CreatedAt);
CREATE INDEX IX_Audit_Entity ON Audit(EntityName, EntityId);
CREATE INDEX IX_Session_Examiner_SessionId ON Session_Examiner(SessionId);
CREATE INDEX IX_Session_Examiner_ExaminerId ON Session_Examiner(ExaminerId);
CREATE INDEX IX_Session_Examiner_ExamId ON Session_Examiner(ExamId);
CREATE INDEX IX_Session_Examiner_ExamSectionId ON Session_Examiner(ExamSectionId);
CREATE INDEX IX_Session_Examiner_ExamAreaId ON Session_Examiner(ExamAreaId);
GO