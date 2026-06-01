-- ============================================
-- DATABASE: DLEM_DB
-- ============================================

USE master;
GO

IF EXISTS (SELECT 1 FROM sys.databases WHERE name = 'DLEM_DB')
BEGIN
    ALTER DATABASE DLEM_DB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE DLEM_DB;
END
GO

CREATE DATABASE DLEM_DB;
GO

USE DLEM_DB;
GO

-- ============================================
-- PERSON (Handles both registrants and walk-in candidates)
-- ============================================

CREATE TABLE Person (
    id INT IDENTITY(1,1) PRIMARY KEY,
    govIdNo NVARCHAR(50) NULL UNIQUE,
    fullName NVARCHAR(200) NOT NULL,
    dateOfBirth DATE NOT NULL,
    gender BIT NOT NULL DEFAULT 0, -- 0 = Male, 1 = Female
    phoneNo NVARCHAR(20) NOT NULL,
    email NVARCHAR(255) NULL,
    address NVARCHAR(500) NULL,
    photoUrl NVARCHAR(500) NULL,
    isWalkIn BIT NOT NULL DEFAULT 0,
    createdAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    updatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
	approvalStatus NVARCHAR(20) NOT NULL DEFAULT 'Pending' CHECK (approvalStatus IN ('Pending', 'Approved', 'Rejected')),
    rejectionReason NVARCHAR(500) NULL,
    INDEX IX_Person_govIdNo (govIdNo),
    INDEX IX_Person_phoneNo (phoneNo),
    INDEX IX_Person_fullName (fullName)
);

CREATE TABLE CandidateDocument (
    id INT IDENTITY(1,1) PRIMARY KEY,
    personId INT NOT NULL REFERENCES Person(id),
    documentType NVARCHAR(50) NOT NULL, -- 'ID_Card', 'Health_Cert', 'Residence_Proof', 'Photo', 'License_Copy'
    documentUrl NVARCHAR(500) NOT NULL,
    expiryDate DATE NULL,
    createdAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    INDEX IX_CandidateDocument_personId (personId),
);

-- ============================================
-- USER TABLE (System login accounts)
-- ============================================

CREATE TABLE Role (
    id INT IDENTITY(1,1) PRIMARY KEY,
    roleName NVARCHAR(50) NOT NULL UNIQUE CHECK (roleName IN ('Admin', 'Examiner', 'ManagingStaff', 'ExamStaff', 'Candidate', 'Registrant'))
);

CREATE TABLE [User] (
    id INT IDENTITY(1,1) PRIMARY KEY,
    personId INT NULL UNIQUE REFERENCES Person(id),
    username NVARCHAR(100) NOT NULL UNIQUE,
    email NVARCHAR(255) NULL UNIQUE,
    passwordHash NVARCHAR(255) NOT NULL,
    roleId INT NOT NULL REFERENCES Role(id),
    isActive BIT NOT NULL DEFAULT 1,
    lastLoginAt DATETIME2 NULL,
    createdAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    INDEX IX_User_username (username),
    INDEX IX_User_email (email),
    INDEX IX_User_roleId (roleId)
);

-- ============================================
-- LICENSE & EXAM STRUCTURE
-- ============================================

CREATE TABLE LicenseType (
    id INT IDENTITY(1,1) PRIMARY KEY,
    licenseCode NVARCHAR(10) NOT NULL UNIQUE,
    minAge INT NOT NULL CHECK (minAge >= 18),
    hasTheory BIT NOT NULL DEFAULT 1,
    hasPractical BIT NOT NULL DEFAULT 1,
    hasRoadLayout BIT NOT NULL DEFAULT 0,
    hasOnRoad BIT NOT NULL DEFAULT 0,
    durationYears INT NOT NULL DEFAULT 5,
    criticalQuestionCount INT NOT NULL DEFAULT 1
);

CREATE TABLE ExamType (
    id INT IDENTITY(1,1) PRIMARY KEY,
    typeName NVARCHAR(50) NOT NULL UNIQUE CHECK (typeName IN ('Theory', 'Practical', 'RoadLayout', 'OnRoad'))
);

CREATE TABLE ExamSection (
    id INT IDENTITY(1,1) PRIMARY KEY,
    examTypeId INT NOT NULL REFERENCES ExamType(id),
    licenseTypeId INT NOT NULL REFERENCES LicenseType(id),
    timeLimitMinutes INT NULL,
    examFee DECIMAL(18,2) NOT NULL DEFAULT 0,
    isActive BIT NOT NULL DEFAULT 1,
    INDEX IX_ExamSection_licenseTypeId (licenseTypeId)
);

-- ============================================
-- EXAM SESSIONS & REGISTRATION
-- ============================================

CREATE TABLE ExamArea (
    id INT IDENTITY(1,1) PRIMARY KEY,
    areaName NVARCHAR(100) NOT NULL,
    areaType NVARCHAR(50) NOT NULL CHECK (areaType IN ('Room', 'Ground', 'Road')),
    capacity INT NOT NULL CHECK (capacity > 0),
    location NVARCHAR(255) NULL,
    isActive BIT NOT NULL DEFAULT 1
);

CREATE TABLE ExamSession (
    id INT IDENTITY(1,1) PRIMARY KEY,
    sessionName NVARCHAR(100) NOT NULL,
    licenseTypeId INT NOT NULL REFERENCES LicenseType(id),
    examTypeId INT NOT NULL REFERENCES ExamType(id),
    examDate DATE NOT NULL,
    shiftStartTime TIME NOT NULL,
    shiftEndTime TIME NOT NULL,
    areaId INT NOT NULL REFERENCES ExamArea(id),
    status NVARCHAR(20) NOT NULL DEFAULT 'Scheduled' CHECK (status IN ('Scheduled', 'Open', 'InProgress', 'Completed', 'Cancelled')),
    maxCandidates INT NOT NULL CHECK (maxCandidates > 0),
    registeredCount INT NOT NULL DEFAULT 0,
    createdAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    INDEX IX_ExamSession_examDate (examDate),
    INDEX IX_ExamSession_status (status)
);




CREATE TABLE ExamRegistration (
    id INT IDENTITY(1,1) PRIMARY KEY,
    examSessionId INT NOT NULL REFERENCES ExamSession(id),
    personId INT NOT NULL REFERENCES Person(id),
    candidateNo INT NOT NULL,
    registrationType NVARCHAR(20) NOT NULL DEFAULT 'PreRegistered' CHECK (registrationType IN ('PreRegistered', 'WalkIn')),
    isPaymentCompleted BIT NOT NULL DEFAULT 0,
    isPresent BIT NOT NULL DEFAULT 0,
    presentMarkedAt DATETIME2 NULL,
    notes NVARCHAR(500) NULL,
    CONSTRAINT UQ_ExamRegistration_session_candidate UNIQUE (examSessionId, candidateNo),
    CONSTRAINT UQ_ExamRegistration_session_person UNIQUE (examSessionId, personId),
    INDEX IX_ExamRegistration_examSessionId (examSessionId),
    INDEX IX_ExamRegistration_personId (personId),
    INDEX IX_ExamRegistration_registrationType (registrationType)
);

-- ============================================
-- EXAMINATION EQUIPMENT
-- ============================================

CREATE TABLE ExamComputer (
    id INT IDENTITY(1,1) PRIMARY KEY,
    computerCode NVARCHAR(50) NOT NULL UNIQUE,
    areaId INT NOT NULL REFERENCES ExamArea(id),
    status NVARCHAR(20) NOT NULL DEFAULT 'Available' CHECK (status IN ('Available', 'InUse', 'Broken', 'Maintenance')),
    lastUsedAt DATETIME2 NULL
);

CREATE TABLE ExamDevice (
    id INT IDENTITY(1,1) PRIMARY KEY,
    areaId INT NOT NULL REFERENCES ExamArea(id),
    deviceType NVARCHAR(50) NOT NULL,
    deviceName NVARCHAR(100) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'Operational'
);

-- ============================================
-- QUESTION BANK (Theory)
-- ============================================

CREATE TABLE QuestionCategory (
    id INT IDENTITY(1,1) PRIMARY KEY,
    categoryName NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500) NULL
);

CREATE TABLE Question (
    id INT IDENTITY(1,1) PRIMARY KEY,
    questionNo NVARCHAR(50) NOT NULL UNIQUE,
    categoryId INT NOT NULL REFERENCES QuestionCategory(id),
    imageUrl NVARCHAR(500) NULL,
    correctAnswer NCHAR(1) NOT NULL CHECK (correctAnswer IN ('A', 'B', 'C', 'D')),
    isCritical BIT NOT NULL DEFAULT 0
);

CREATE TABLE LicenseQuestion (
    id INT IDENTITY(1,1) PRIMARY KEY,
    licenseTypeId INT NOT NULL REFERENCES LicenseType(id),
    questionId INT NOT NULL REFERENCES Question(id),
    CONSTRAINT UQ_LicenseQuestion UNIQUE (licenseTypeId, questionId),
    INDEX IX_LicenseQuestion_licenseTypeId (licenseTypeId),
    INDEX IX_LicenseQuestion_questionId (questionId)
);

-- ============================================
-- EXAM PAPERS & ANSWERS (Theory)
-- ============================================

CREATE TABLE ExamPaper (
    id INT IDENTITY(1,1) PRIMARY KEY,
    examRegistrationId INT NOT NULL REFERENCES ExamRegistration(id),
    examComputerId INT NULL REFERENCES ExamComputer(id),
    startedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    submittedAt DATETIME2 NULL,
    isSubmitted BIT NOT NULL DEFAULT 0,
    CONSTRAINT UQ_ExamPaper_examRegistrationId UNIQUE (examRegistrationId)
);

CREATE TABLE CandidateAnswer (
    id INT IDENTITY(1,1) PRIMARY KEY,
    examPaperId INT NOT NULL REFERENCES ExamPaper(id),
    questionId INT NOT NULL REFERENCES Question(id),
    selectedAnswer NCHAR(1) NULL CHECK (selectedAnswer IN ('A', 'B', 'C', 'D')),
    CONSTRAINT UQ_CandidateAnswer_examPaperId_questionId UNIQUE (examPaperId, questionId)
);

-- ============================================
-- SCORING (Theory)
-- ============================================

CREATE TABLE TheoryScore (
    id INT IDENTITY(1,1) PRIMARY KEY,
    examPaperId INT NOT NULL UNIQUE REFERENCES ExamPaper(id),
    totalRawScore INT NOT NULL DEFAULT 0,
    finalScore INT NOT NULL DEFAULT 0,
    calculatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE()
);

-- ============================================
-- SCORING (Practical, RoadLayout, OnRoad)
-- ============================================

CREATE TABLE PracticalScore (
    id INT IDENTITY(1,1) PRIMARY KEY,
    examRegistrationId INT NOT NULL REFERENCES ExamRegistration(id),
    examSectionId INT NOT NULL REFERENCES ExamSection(id),
    baseScore INT NOT NULL DEFAULT 100,
    totalDeductions INT NOT NULL DEFAULT 0,
    finalScore INT NOT NULL DEFAULT 0,
    evaluatedBy INT NOT NULL REFERENCES [User](id),
    evaluatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    CONSTRAINT UQ_PracticalScore_registration_section UNIQUE (examRegistrationId, examSectionId),
    INDEX IX_PracticalScore_examRegistrationId (examRegistrationId),
    INDEX IX_PracticalScore_examSectionId (examSectionId)
);

CREATE TABLE ScoreDeduction (
    id INT IDENTITY(1,1) PRIMARY KEY,
    practicalScoreId INT NOT NULL REFERENCES PracticalScore(id),
    deductionReason NVARCHAR(500) NOT NULL,
    deductionPoints INT NOT NULL CHECK (deductionPoints > 0 AND deductionPoints <= 100),
    note NVARCHAR(1000) NULL,
    INDEX IX_ScoreDeduction_practicalScoreId (practicalScoreId)
);

-- ============================================
-- SCORE CHANGE LOG
-- ============================================

CREATE TABLE ScoreChangeLog (
    id INT IDENTITY(1,1) PRIMARY KEY,
    scoreType NVARCHAR(20) NOT NULL CHECK (scoreType IN ('Theory', 'Practical', 'RoadLayout', 'OnRoad')),
    scoreId INT NOT NULL,
    oldScore INT NOT NULL,
    newScore INT NOT NULL,
    changedBy INT NOT NULL REFERENCES [User](id),
    changeReason NVARCHAR(500) NOT NULL,
    changedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    INDEX IX_ScoreChangeLog_scoreType_scoreId (scoreType, scoreId),
    INDEX IX_ScoreChangeLog_changedBy (changedBy)
);

-- ============================================
-- EXAM RESULTS
-- ============================================

CREATE TABLE ExamResult (
    id INT IDENTITY(1,1) PRIMARY KEY,
    examRegistrationId INT NOT NULL REFERENCES ExamRegistration(id),
    examSectionId INT NOT NULL REFERENCES ExamSection(id),
    theoryScoreId INT NULL REFERENCES TheoryScore(id),
    practicalScoreId INT NULL REFERENCES PracticalScore(id),
    startTime DATETIME2 NOT NULL,
    endTime DATETIME2 NOT NULL,
    answersCount INT NULL,
    correctAnswersCount INT NULL,
    isCancelled BIT NOT NULL DEFAULT 0,
    cancelReason NVARCHAR(500) NULL,
    cancelledBy INT NULL REFERENCES [User](id),
    CONSTRAINT UQ_ExamResult_registration_section UNIQUE (examRegistrationId, examSectionId),
    INDEX IX_ExamResult_examRegistrationId (examRegistrationId),
    INDEX IX_ExamResult_examSectionId (examSectionId)
);

-- ============================================
-- PAYMENTS
-- ============================================

CREATE TABLE Payment (
    id INT IDENTITY(1,1) PRIMARY KEY,
    examRegistrationId INT NOT NULL REFERENCES ExamRegistration(id),
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    paymentStatus NVARCHAR(20) NOT NULL DEFAULT 'Pending' CHECK (paymentStatus IN ('Pending', 'Completed', 'Failed', 'Refunded')),
    paymentMethod NVARCHAR(20) NOT NULL DEFAULT 'Cash' CHECK (paymentMethod IN ('Cash', 'BankTransfer')),
    paymentDate DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    transactionReference NVARCHAR(100) NULL,
    notes NVARCHAR(500) NULL,
    INDEX IX_Payment_examRegistrationId (examRegistrationId),
    INDEX IX_Payment_paymentStatus (paymentStatus)
);

-- ============================================
-- CANDIDATE CALLS
-- ============================================

CREATE TABLE CandidateCall (
    id INT IDENTITY(1,1) PRIMARY KEY,
    examSessionId INT NOT NULL REFERENCES ExamSession(id),
    candidateNo INT NOT NULL,
    calledTo NVARCHAR(200) NOT NULL,
    calledBy INT NOT NULL REFERENCES [User](id),
    calledAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    result NVARCHAR(100) NULL
);

-- ============================================
-- AUDIT LOG
-- ============================================

CREATE TABLE AuditLog (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    tableName NVARCHAR(128) NOT NULL,
    recordId INT NOT NULL,
    action NVARCHAR(10) NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE', 'EXPORT')),
    oldValue NVARCHAR(MAX) NULL,
    newValue NVARCHAR(MAX) NULL,
    changedBy INT NOT NULL REFERENCES [User](id),
    changedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    ipAddress NVARCHAR(45) NULL,
    sessionId NVARCHAR(100) NULL,
    INDEX IX_AuditLog_tableName_recordId (tableName, recordId),
    INDEX IX_AuditLog_changedBy (changedBy),
    INDEX IX_AuditLog_changedAt (changedAt)
);
