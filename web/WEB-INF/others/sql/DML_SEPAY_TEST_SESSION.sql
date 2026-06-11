-- Đợt thi test SEPay (A1 + B). Chạy lại được.
USE DLEM_DB;
GO

DECLARE @TestSessions TABLE (
    sessionName NVARCHAR(100) NOT NULL,
    licenseCode NVARCHAR(10) NOT NULL,
    examTypeName NVARCHAR(20) NOT NULL,
    examDate DATE NOT NULL,
    shiftStart TIME NOT NULL,
    shiftEnd TIME NOT NULL
);

INSERT INTO @TestSessions (sessionName, licenseCode, examTypeName, examDate, shiftStart, shiftEnd)
VALUES
    (N'A1-LT-SEPAY-QR-TEST', 'A1', 'Theory', DATEADD(DAY, 14, CAST(GETUTCDATE() AS DATE)), '08:00', '09:30'),
    (N'B-LT-SEPAY-QR-TEST',  'B',  'Theory', DATEADD(DAY, 21, CAST(GETUTCDATE() AS DATE)), '13:30', '15:00');

DECLARE @sessionName NVARCHAR(100);
DECLARE @licenseCode NVARCHAR(10);
DECLARE @examTypeName NVARCHAR(20);
DECLARE @examDate DATE;
DECLARE @shiftStart TIME;
DECLARE @shiftEnd TIME;
DECLARE @licenseTypeId INT;
DECLARE @examTypeId INT;
DECLARE @areaId INT;
DECLARE @sessionId INT;

DECLARE c CURSOR LOCAL FAST_FORWARD FOR
    SELECT sessionName, licenseCode, examTypeName, examDate, shiftStart, shiftEnd FROM @TestSessions;
OPEN c;
FETCH NEXT FROM c INTO @sessionName, @licenseCode, @examTypeName, @examDate, @shiftStart, @shiftEnd;
WHILE @@FETCH_STATUS = 0
BEGIN
    SELECT @licenseTypeId = id FROM LicenseType WHERE licenseCode = @licenseCode;
    SELECT @examTypeId = id FROM ExamType WHERE typeName = @examTypeName;
    SELECT TOP 1 @areaId = id FROM ExamArea WHERE areaType = 'Room' ORDER BY id;

    IF NOT EXISTS (SELECT 1 FROM ExamSession WHERE sessionName = @sessionName)
        INSERT INTO ExamSession (sessionName, licenseTypeId, examTypeId, examDate, shiftStartTime, shiftEndTime, areaId, status, maxCandidates, registeredCount)
        VALUES (@sessionName, @licenseTypeId, @examTypeId, @examDate, @shiftStart, @shiftEnd, @areaId, 'Open', 50, 0);
    ELSE
        UPDATE ExamSession SET licenseTypeId=@licenseTypeId, examTypeId=@examTypeId, examDate=@examDate,
            shiftStartTime=@shiftStart, shiftEndTime=@shiftEnd, areaId=@areaId, status='Open', maxCandidates=50
        WHERE sessionName=@sessionName;

    SELECT @sessionId = id FROM ExamSession WHERE sessionName = @sessionName;

    DELETE p FROM Payment p JOIN ExamRegistration er ON p.examRegistrationId = er.id
    WHERE er.examSessionId = @sessionId AND er.isPaymentCompleted = 0;
    DELETE er FROM ExamRegistration er WHERE er.examSessionId = @sessionId AND er.isPaymentCompleted = 0;
    UPDATE ExamSession SET registeredCount = (
        SELECT COUNT(*) FROM ExamRegistration er WHERE er.examSessionId = @sessionId AND er.isPaymentCompleted = 1
    ) WHERE id = @sessionId;

    FETCH NEXT FROM c INTO @sessionName, @licenseCode, @examTypeName, @examDate, @shiftStart, @shiftEnd;
END
CLOSE c; DEALLOCATE c;

UPDATE Person SET approvalStatus = 'Approved'
WHERE id = (SELECT personId FROM [User] WHERE username = 'user123');
GO
