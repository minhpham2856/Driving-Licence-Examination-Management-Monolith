USE DLEM_DB_2;
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

DECLARE @A1 INT=(SELECT LicenceId FROM Licence WHERE LicenceClass=N'A1');
DECLARE @A  INT=(SELECT LicenceId FROM Licence WHERE LicenceClass=N'A');
DECLARE @B1 INT=(SELECT LicenceId FROM Licence WHERE LicenceClass=N'B1');

-- Future tentative dates. These are not official Exam rows.
IF NOT EXISTS(SELECT 1 FROM ExamDates WHERE ExamDate='2026-08-15' AND LicenceId=@A1)
    INSERT ExamDates(ExamDate,LicenceId) VALUES('2026-08-15',@A1);
IF NOT EXISTS(SELECT 1 FROM ExamDates WHERE ExamDate='2026-08-22' AND LicenceId=@B1)
    INSERT ExamDates(ExamDate,LicenceId) VALUES('2026-08-22',@B1);
IF NOT EXISTS(SELECT 1 FROM ExamDates WHERE ExamDate='2026-09-05' AND LicenceId=@A)
    INSERT ExamDates(ExamDate,LicenceId) VALUES('2026-09-05',@A);
IF NOT EXISTS(SELECT 1 FROM ExamDates WHERE ExamDate='2026-09-12' AND LicenceId=@B1)
    INSERT ExamDates(ExamDate,LicenceId) VALUES('2026-09-12',@B1);

-- Put approved applications into tentative dates and mark them WaitingExam.
DECLARE @Waiting TABLE(RegistrationId INT, ExamDateId INT);
INSERT @Waiting
SELECT TOP (2) er.ExamRegistrationId,
       (SELECT ExamDateId FROM ExamDates WHERE ExamDate='2026-08-22' AND LicenceId=@B1)
FROM ExamRegistration er
WHERE er.LicenceId=@B1 AND er.RegistrationStatus IN(N'Duyệt',N'Approved',N'WaitingExam')
ORDER BY er.ExamRegistrationId;
INSERT @Waiting
SELECT TOP (1) er.ExamRegistrationId,
       (SELECT ExamDateId FROM ExamDates WHERE ExamDate='2026-08-15' AND LicenceId=@A1)
FROM ExamRegistration er
WHERE er.LicenceId=@A1 AND er.RegistrationStatus IN(N'Duyệt',N'Approved',N'WaitingExam')
ORDER BY er.ExamRegistrationId;
INSERT @Waiting
SELECT TOP (1) er.ExamRegistrationId,
       (SELECT ExamDateId FROM ExamDates WHERE ExamDate='2026-09-05' AND LicenceId=@A)
FROM ExamRegistration er
WHERE er.LicenceId=@A AND er.RegistrationStatus IN(N'Duyệt',N'Approved',N'WaitingExam')
ORDER BY er.ExamRegistrationId;

UPDATE rd SET IsActive=0
FROM RegistrationDates rd JOIN @Waiting w ON w.RegistrationId=rd.ExamRegistrationId
WHERE rd.ExamDateId<>w.ExamDateId AND rd.IsActive=1;
MERGE RegistrationDates WITH(HOLDLOCK) t
USING @Waiting s ON s.RegistrationId=t.ExamRegistrationId AND s.ExamDateId=t.ExamDateId
WHEN MATCHED THEN UPDATE SET IsActive=1
WHEN NOT MATCHED THEN INSERT(ExamRegistrationId,ExamDateId,IsActive)
VALUES(s.RegistrationId,s.ExamDateId,1);
UPDATE er SET RegistrationStatus=N'WaitingExam'
FROM ExamRegistration er JOIN @Waiting w ON w.RegistrationId=er.ExamRegistrationId;

-- Demo official candidates/results for management reports.
IF NOT EXISTS(SELECT 1 FROM Candidate WHERE GovernmentIdNumber LIKE N'099TEST%')
BEGIN
    DECLARE @i INT=1,@CandidateId INT,@EnrollmentId INT,@ExamId INT,@Passed BIT,@Absent BIT;
    WHILE @i<=30
    BEGIN
        SELECT @ExamId=ExamId FROM (
            SELECT ExamId,ROW_NUMBER() OVER(ORDER BY ExamId) rn
            FROM Exam WHERE ExamCode IN(N'A1-20260601-0730',N'A1-20260601-1000',N'B1-20260601-0730',N'B1-20260601-0930',N'A-20260610')
        ) x WHERE rn=((@i-1)%5)+1;
        SET @Absent=CASE WHEN @i%10=0 THEN 1 ELSE 0 END;
        SET @Passed=CASE WHEN @Absent=0 AND @i%4<>0 THEN 1 ELSE 0 END;
        INSERT Candidate(CandidateNumber,FullName,DateOfBirth,PhoneNumber,Email,Sex,
            GovernmentIdNumber,Address,TakeTheory,TakeLayout,TakeNo,ReasonForTaking,
            PhotoImageUrl,IsAbsent,IsSuspended)
        VALUES(N'TST'+RIGHT(N'000'+CONVERT(nvarchar(3),@i),3),N'Thi sinh bao cao '+CONVERT(nvarchar(3),@i),
            DATEADD(DAY,-(@i*120),'2002-01-01'),N'0909'+RIGHT(N'000000'+CONVERT(nvarchar(6),@i),6),
            N'report.test'+CONVERT(nvarchar(3),@i)+N'@example.com',@i%2,
            N'099TEST'+RIGHT(N'0000'+CONVERT(nvarchar(4),@i),4),N'Dia chi du lieu thong ke',1,1,1,
            N'Du lieu test bao cao ManagingStaff',NULL,@Absent,0);
        SET @CandidateId=SCOPE_IDENTITY();
        INSERT ExamEnrollment(CandidateId,ExamId) VALUES(@CandidateId,@ExamId);
        SET @EnrollmentId=SCOPE_IDENTITY();
        INSERT ExamEnrollmentSection(ExamEnrollmentId,ExamSectionId,Status,StartedAt,CompletedAt)
        SELECT @EnrollmentId,es.ExamSectionId,
               CASE WHEN @Absent=1 THEN N'Absent' WHEN @Passed=1 THEN N'Passed' ELSE N'Failed' END,
               CASE WHEN @Absent=1 THEN NULL ELSE DATEADD(MINUTE,5,e.StartTime) END,
               CASE WHEN @Absent=1 THEN NULL ELSE DATEADD(MINUTE,25,e.StartTime) END
        FROM ExamSection es JOIN Exam e ON e.ExamId=es.ExamId WHERE es.ExamId=@ExamId;
        INSERT ExamResult(ExamEnrollmentId,IsPassed,ResultDate)
        SELECT @EnrollmentId,@Passed,DATEADD(HOUR,3,StartTime) FROM Exam WHERE ExamId=@ExamId;
        DECLARE @ResultId INT=SCOPE_IDENTITY();
        INSERT ExamScore(ExamResultId,ExamSectionId,Score)
        SELECT @ResultId,ExamSectionId,
               CASE WHEN @Absent=1 THEN 0 WHEN @Passed=1 THEN 85+(@i%10) ELSE 45+(@i%20) END
        FROM ExamSection WHERE ExamId=@ExamId;
        SET @i+=1;
    END
END

-- Historical official exams so year/month report filters have meaningful data.
IF NOT EXISTS(SELECT 1 FROM Exam WHERE ExamCode=N'DEMO-A1-2024')
    INSERT Exam(ExamCode,ExamDate,StartTime,EndTime,Status,CentreName,LicenceId)
    VALUES(N'DEMO-A1-2024','2024-05-18','2024-05-18 07:30','2024-05-18 11:30',N'Completed',N'Trung tam sat hach Lai Vui',@A1);
IF NOT EXISTS(SELECT 1 FROM Exam WHERE ExamCode=N'DEMO-B1-2024')
    INSERT Exam(ExamCode,ExamDate,StartTime,EndTime,Status,CentreName,LicenceId)
    VALUES(N'DEMO-B1-2024','2024-10-12','2024-10-12 07:30','2024-10-12 11:30',N'Completed',N'Trung tam sat hach Lai Vui',@B1);
IF NOT EXISTS(SELECT 1 FROM Exam WHERE ExamCode=N'DEMO-A-2025')
    INSERT Exam(ExamCode,ExamDate,StartTime,EndTime,Status,CentreName,LicenceId)
    VALUES(N'DEMO-A-2025','2025-04-19','2025-04-19 07:30','2025-04-19 11:30',N'Completed',N'Trung tam sat hach Lai Vui',@A);
IF NOT EXISTS(SELECT 1 FROM Exam WHERE ExamCode=N'DEMO-B1-2025')
    INSERT Exam(ExamCode,ExamDate,StartTime,EndTime,Status,CentreName,LicenceId)
    VALUES(N'DEMO-B1-2025','2025-11-15','2025-11-15 07:30','2025-11-15 11:30',N'Completed',N'Trung tam sat hach Lai Vui',@B1);

INSERT ExamSection(SectionType,LicenceId,DurationMinutes,ExamId)
SELECT N'Ly thuyet',e.LicenceId,20,e.ExamId FROM Exam e
WHERE e.ExamCode LIKE N'DEMO-%' AND NOT EXISTS(
    SELECT 1 FROM ExamSection s WHERE s.ExamId=e.ExamId AND s.SectionType=N'Ly thuyet');
INSERT ExamSection(SectionType,LicenceId,DurationMinutes,ExamId)
SELECT N'Thuc hanh',e.LicenceId,30,e.ExamId FROM Exam e
WHERE e.ExamCode LIKE N'DEMO-%' AND NOT EXISTS(
    SELECT 1 FROM ExamSection s WHERE s.ExamId=e.ExamId AND s.SectionType=N'Thuc hanh');

IF NOT EXISTS(SELECT 1 FROM Candidate WHERE GovernmentIdNumber LIKE N'098YEAR%')
BEGIN
    DECLARE @y INT=1,@YearCandidateId INT,@YearEnrollmentId INT,@YearExamId INT,@YearPassed BIT;
    WHILE @y<=24
    BEGIN
        SELECT @YearExamId=ExamId FROM (
            SELECT ExamId,ROW_NUMBER() OVER(ORDER BY ExamDate,ExamId) rn
            FROM Exam WHERE ExamCode IN(N'DEMO-A1-2024',N'DEMO-B1-2024',N'DEMO-A-2025',N'DEMO-B1-2025')
        ) x WHERE rn=((@y-1)%4)+1;
        SET @YearPassed=CASE WHEN @y%3=0 THEN 0 ELSE 1 END;
        INSERT Candidate(CandidateNumber,FullName,DateOfBirth,PhoneNumber,Email,Sex,
            GovernmentIdNumber,Address,TakeTheory,TakeLayout,TakeNo,ReasonForTaking,
            PhotoImageUrl,IsAbsent,IsSuspended)
        VALUES(N'YR'+RIGHT(N'000'+CONVERT(nvarchar(3),@y),3),N'Thi sinh lich su '+CONVERT(nvarchar(3),@y),
            DATEADD(DAY,-(@y*90),'2001-01-01'),N'0918'+RIGHT(N'000000'+CONVERT(nvarchar(6),@y),6),
            N'history.test'+CONVERT(nvarchar(3),@y)+N'@example.com',@y%2,
            N'098YEAR'+RIGHT(N'0000'+CONVERT(nvarchar(4),@y),4),N'Du lieu lich su bao cao',1,1,1,
            N'Du lieu test loc theo nam',NULL,CASE WHEN @y%11=0 THEN 1 ELSE 0 END,0);
        SET @YearCandidateId=SCOPE_IDENTITY();
        INSERT ExamEnrollment(CandidateId,ExamId) VALUES(@YearCandidateId,@YearExamId);
        SET @YearEnrollmentId=SCOPE_IDENTITY();
        INSERT ExamEnrollmentSection(ExamEnrollmentId,ExamSectionId,Status,StartedAt,CompletedAt)
        SELECT @YearEnrollmentId,s.ExamSectionId,CASE WHEN @YearPassed=1 THEN N'Passed' ELSE N'Failed' END,
               DATEADD(MINUTE,5,e.StartTime),DATEADD(MINUTE,25,e.StartTime)
        FROM ExamSection s JOIN Exam e ON e.ExamId=s.ExamId WHERE s.ExamId=@YearExamId;
        INSERT ExamResult(ExamEnrollmentId,IsPassed,ResultDate)
        SELECT @YearEnrollmentId,@YearPassed,DATEADD(HOUR,3,StartTime) FROM Exam WHERE ExamId=@YearExamId;
        DECLARE @YearResultId INT=SCOPE_IDENTITY();
        INSERT ExamScore(ExamResultId,ExamSectionId,Score)
        SELECT @YearResultId,ExamSectionId,CASE WHEN @YearPassed=1 THEN 88 ELSE 55 END
        FROM ExamSection WHERE ExamId=@YearExamId;
        SET @y+=1;
    END
END

-- Audit history for the ManagingStaff account.
DECLARE @ManagerId INT=(SELECT TOP 1 u.UserId FROM [User] u JOIN [Role] r ON r.RoleId=u.RoleId
                         WHERE r.RoleName IN(N'Cán bộ quản lý',N'CÃ¡n bá»™ quáº£n lÃ½') ORDER BY u.UserId);
IF @ManagerId IS NULL SET @ManagerId=(SELECT TOP 1 UserId FROM [User] WHERE Username=N'qly123');
;WITH DemoAuditDuplicates AS (
    SELECT AuditId,ROW_NUMBER() OVER(PARTITION BY Details ORDER BY AuditId) rn
    FROM Audit WHERE Details LIKE N'#MANAGINGSTAFF_DEMO#%'
)
DELETE FROM DemoAuditDuplicates WHERE rn>1;
IF @ManagerId IS NOT NULL AND NOT EXISTS(SELECT 1 FROM Audit WHERE Details LIKE N'#MANAGINGSTAFF_DEMO#%')
BEGIN
    INSERT Audit(UserId,Action,Reason,EntityName,EntityId,OldValue,NewValue,Details,CreatedAt) VALUES
    (@ManagerId,N'APPROVE',N'Hồ sơ hợp lệ',N'ExamRegistration',N'8',N'Pending',N'Approved',N'#MANAGINGSTAFF_DEMO# Duyệt hồ sơ',DATEADD(DAY,-18,GETDATE())),
    (@ManagerId,N'APPROVE',N'Hồ sơ hợp lệ',N'ExamRegistration',N'9',N'Pending',N'Approved',N'#MANAGINGSTAFF_DEMO# Duyệt hồ sơ',DATEADD(DAY,-16,GETDATE())),
    (@ManagerId,N'REJECT',N'Thiếu giấy khám sức khỏe',N'ExamRegistration',N'10',N'Pending',N'Rejected',N'#MANAGINGSTAFF_DEMO# Từ chối hồ sơ',DATEADD(DAY,-14,GETDATE())),
    (@ManagerId,N'CREATE',N'Tạo ngày dự kiến',N'ExamDates',N'2026-08-15',NULL,N'Open',N'#MANAGINGSTAFF_DEMO# Ngày dự kiến',DATEADD(DAY,-10,GETDATE())),
    (@ManagerId,N'EXPORT',N'Gửi danh sách đối soát',N'RegistrationDates',N'2026-08-22',NULL,N'Excel',N'#MANAGINGSTAFF_DEMO# Xuất Excel',DATEADD(DAY,-7,GETDATE())),
    (@ManagerId,N'IMPORT',N'Nhận danh sách chính thức',N'Exam',N'B1-20260601-0730',NULL,N'30 candidates',N'#MANAGINGSTAFF_DEMO# Import danh sách',DATEADD(DAY,-3,GETDATE()));
END

COMMIT TRANSACTION;

SELECT 'ExamDates' AS Item,COUNT(*) Total FROM ExamDates
UNION ALL SELECT 'WaitingExam',COUNT(*) FROM ExamRegistration WHERE RegistrationStatus=N'WaitingExam'
UNION ALL SELECT 'Candidates',COUNT(*) FROM Candidate
UNION ALL SELECT 'ExamResults',COUNT(*) FROM ExamResult
UNION ALL SELECT 'Audit',COUNT(*) FROM Audit;
