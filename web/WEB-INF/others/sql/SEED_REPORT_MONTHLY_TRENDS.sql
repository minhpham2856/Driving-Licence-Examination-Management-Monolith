USE DLEM_DB_2;
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

/*
  Du lieu bao cao theo thang:
  - Nam 2024: du 12 thang, A1/A/B1
  - Nam 2025: du 12 thang, A1/A/B1
  - Nam 2026: thang 1-6, A1/A/B1
  - Moi ky co 8 thi sinh, ty le dat/truot thay doi theo thang va hang
  - Script idempotent: chay lai khong tao trung Exam/Candidate
*/

DECLARE @Classes TABLE(ClassNo INT, LicenceClass NVARCHAR(10), LicenceId INT);
INSERT @Classes(ClassNo,LicenceClass,LicenceId)
SELECT ROW_NUMBER() OVER(ORDER BY CASE LicenceClass WHEN N'A1' THEN 1 WHEN N'A' THEN 2 ELSE 3 END),
       LicenceClass,LicenceId
FROM Licence WHERE LicenceClass IN(N'A1',N'A',N'B1');

IF (SELECT COUNT(*) FROM @Classes)<>3
BEGIN
    RAISERROR (N'Can co du ba hang A1, A, B1 trong bang Licence.',16,1);
    ROLLBACK TRANSACTION;
    RETURN;
END;

DECLARE @Year INT=2024,@Month INT,@MaxMonth INT,@ClassNo INT;
DECLARE @LicenceClass NVARCHAR(10),@LicenceId INT,@ExamCode NVARCHAR(50);
DECLARE @ExamDate DATE,@StartTime DATETIME,@EndTime DATETIME,@ExamId INT;
DECLARE @CandidateIndex INT,@CandidateId INT,@EnrollmentId INT,@ResultId INT;
DECLARE @Passed BIT,@Absent BIT,@RateSeed INT;

WHILE @Year<=2026
BEGIN
    SET @Month=1;
    SET @MaxMonth=CASE WHEN @Year=2026 THEN 6 ELSE 12 END;
    WHILE @Month<=@MaxMonth
    BEGIN
        SET @ClassNo=1;
        WHILE @ClassNo<=3
        BEGIN
            SELECT @LicenceClass=LicenceClass,@LicenceId=LicenceId
            FROM @Classes WHERE ClassNo=@ClassNo;

            SET @ExamCode=N'STAT-'+@LicenceClass+N'-'+CONVERT(nvarchar(4),@Year)
                          +N'-'+RIGHT(N'0'+CONVERT(nvarchar(2),@Month),2);
            SET @ExamDate=DATEFROMPARTS(@Year,@Month,15);
            SET @StartTime=DATEADD(MINUTE,450,CAST(@ExamDate AS DATETIME));
            SET @EndTime=DATEADD(HOUR,4,@StartTime);

            IF NOT EXISTS(SELECT 1 FROM Exam WHERE ExamCode=@ExamCode)
            BEGIN
                INSERT Exam(ExamCode,ExamDate,StartTime,EndTime,[Status],CentreName,LicenceId)
                VALUES(@ExamCode,@ExamDate,@StartTime,@EndTime,N'Completed',
                       N'Trung tam sat hach Lai Vui - Du lieu thong ke',@LicenceId);
            END;
            SELECT @ExamId=ExamId FROM Exam WHERE ExamCode=@ExamCode;

            IF NOT EXISTS(SELECT 1 FROM ExamSection WHERE ExamId=@ExamId AND SectionType=N'Ly thuyet')
                INSERT ExamSection(SectionType,LicenceId,DurationMinutes,ExamId)
                VALUES(N'Ly thuyet',@LicenceId,20,@ExamId);
            IF NOT EXISTS(SELECT 1 FROM ExamSection WHERE ExamId=@ExamId AND SectionType=N'Thuc hanh')
                INSERT ExamSection(SectionType,LicenceId,DurationMinutes,ExamId)
                VALUES(N'Thuc hanh',@LicenceId,30,@ExamId);

            SET @CandidateIndex=1;
            WHILE @CandidateIndex<=8
            BEGIN
                -- Bien dong ty le dat theo thang/hang de duong xu huong khong bi phang.
                SET @RateSeed=(@Month*3+@ClassNo*5+@CandidateIndex+@Year)%10;
                SET @Absent=CASE WHEN @CandidateIndex=8 AND @Month%4=0 THEN 1 ELSE 0 END;
                SET @Passed=CASE WHEN @Absent=0 AND @RateSeed NOT IN(0,1,2) THEN 1 ELSE 0 END;

                DECLARE @GovId NVARCHAR(30)=N'097'+CONVERT(nvarchar(4),@Year)
                    +RIGHT(N'0'+CONVERT(nvarchar(2),@Month),2)
                    +CONVERT(nvarchar(1),@ClassNo)
                    +RIGHT(N'0'+CONVERT(nvarchar(2),@CandidateIndex),2);

                IF NOT EXISTS(SELECT 1 FROM Candidate WHERE GovernmentIdNumber=@GovId)
                BEGIN
                    INSERT Candidate(CandidateNumber,FullName,DateOfBirth,PhoneNumber,Email,Sex,
                        GovernmentIdNumber,Address,TakeTheory,TakeLayout,TakeNo,ReasonForTaking,
                        PhotoImageUrl,IsAbsent,IsSuspended)
                    VALUES(N'S'+RIGHT(CONVERT(nvarchar(4),@Year),2)
                              +RIGHT(N'0'+CONVERT(nvarchar(2),@Month),2)
                              +CONVERT(nvarchar(1),@ClassNo)
                              +RIGHT(N'0'+CONVERT(nvarchar(2),@CandidateIndex),2),
                           N'Thi sinh thong ke '+@LicenceClass+N' '
                              +RIGHT(N'0'+CONVERT(nvarchar(2),@Month),2)+N'/'
                              +CONVERT(nvarchar(4),@Year)+N' #'+CONVERT(nvarchar(2),@CandidateIndex),
                           DATEADD(DAY,-(@CandidateIndex*180),DATEFROMPARTS(2002,1,1)),
                           N'0907'+RIGHT(N'000000'+CONVERT(nvarchar(6),
                              (@Year-2020)*10000+@Month*100+@CandidateIndex),6),
                           N'stat.'+CONVERT(nvarchar(4),@Year)+N'.'
                              +RIGHT(N'0'+CONVERT(nvarchar(2),@Month),2)+N'.'
                              +@LicenceClass+N'.'+CONVERT(nvarchar(2),@CandidateIndex)+N'@example.com',
                           @CandidateIndex%2,@GovId,N'Du lieu test bao cao theo thang',1,1,1,
                           N'Du lieu demo bieu do ManagingStaff',NULL,@Absent,0);
                    SET @CandidateId=SCOPE_IDENTITY();

                    INSERT ExamEnrollment(CandidateId,ExamId,AllocatedExamAreaId,ExamDeviceId)
                    VALUES(@CandidateId,@ExamId,NULL,NULL);
                    SET @EnrollmentId=SCOPE_IDENTITY();

                    INSERT ExamEnrollmentSection(ExamEnrollmentId,ExamSectionId,ExamAreaId,
                        ExamDeviceId,[Status],AllocatedAt,AllocatedBy,StartedAt,CompletedAt)
                    SELECT @EnrollmentId,s.ExamSectionId,NULL,NULL,
                           CASE WHEN @Absent=1 THEN N'Absent'
                                WHEN @Passed=1 THEN N'Passed' ELSE N'Failed' END,
                           NULL,NULL,
                           CASE WHEN @Absent=1 THEN NULL ELSE DATEADD(MINUTE,5,@StartTime) END,
                           CASE WHEN @Absent=1 THEN NULL ELSE DATEADD(MINUTE,30,@StartTime) END
                    FROM ExamSection s WHERE s.ExamId=@ExamId;

                    INSERT ExamResult(ExamEnrollmentId,IsPassed,ResultDate)
                    VALUES(@EnrollmentId,@Passed,DATEADD(HOUR,4,@StartTime));
                    SET @ResultId=SCOPE_IDENTITY();

                    INSERT ExamScore(ExamResultId,ExamSectionId,Score)
                    SELECT @ResultId,s.ExamSectionId,
                           CASE WHEN @Absent=1 THEN 0
                                WHEN @Passed=1 THEN 82+((@Month+@CandidateIndex)%15)
                                ELSE 45+((@Month+@CandidateIndex)%25) END
                    FROM ExamSection s WHERE s.ExamId=@ExamId;
                END;
                SET @CandidateIndex+=1;
            END;
            SET @ClassNo+=1;
        END;
        SET @Month+=1;
    END;
    SET @Year+=1;
END;

COMMIT TRANSACTION;

-- Kiem tra ket qua sau khi chay.
SELECT YEAR(e.ExamDate) AS ExamYear,MONTH(e.ExamDate) AS ExamMonth,l.LicenceClass,
       COUNT(ee.ExamEnrollmentId) AS TotalCandidates,
       SUM(CASE WHEN r.IsPassed=1 AND c.IsAbsent=0 THEN 1 ELSE 0 END) AS Passed,
       SUM(CASE WHEN r.IsPassed=0 OR c.IsAbsent=1 THEN 1 ELSE 0 END) AS Failed,
       CAST(100.0*SUM(CASE WHEN r.IsPassed=1 AND c.IsAbsent=0 THEN 1 ELSE 0 END)
            /NULLIF(COUNT(ee.ExamEnrollmentId),0) AS DECIMAL(5,1)) AS PassRate
FROM Exam e
JOIN Licence l ON l.LicenceId=e.LicenceId
JOIN ExamEnrollment ee ON ee.ExamId=e.ExamId
JOIN Candidate c ON c.CandidateId=ee.CandidateId
JOIN ExamResult r ON r.ExamEnrollmentId=ee.ExamEnrollmentId
WHERE e.ExamCode LIKE N'STAT-%'
GROUP BY YEAR(e.ExamDate),MONTH(e.ExamDate),l.LicenceClass
ORDER BY ExamYear,ExamMonth,CASE l.LicenceClass WHEN N'A1' THEN 1 WHEN N'A' THEN 2 ELSE 3 END;
