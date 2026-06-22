-- ==============================================================================
-- SCRIPT: SEED 50 THÍ SINH VÀO KỲ THI B-1292 ĐANG MỞ
-- CHỈ TẠO CANDIDATE VÀ EXAMENROLLMENT, KHÔNG TẠO USER/PROFILE
-- ==============================================================================
USE DLEM_DB_2;
GO

-- Xóa dữ liệu cũ nếu đã từng chạy script trước đó (Dựa vào GovernmentIdNumber LIKE '0791293%')
DELETE FROM Payment_Fee WHERE PaymentId IN (SELECT PaymentId FROM Payment WHERE TransactionReference LIKE N'TXN-B1292-EX-%');
DELETE FROM Payment WHERE TransactionReference LIKE N'TXN-B1292-EX-%';
DELETE FROM ExamEnrollment WHERE CandidateId IN (SELECT CandidateId FROM Candidate WHERE GovernmentIdNumber LIKE N'0791293%');
DELETE FROM Candidate WHERE GovernmentIdNumber LIKE N'0791293%';
DELETE FROM ExamRegistration WHERE ProfileId IN (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber LIKE N'0791293%');
DELETE FROM Profile WHERE GovernmentIdNumber LIKE N'0791293%';
DELETE FROM [User] WHERE Username LIKE N'b1292_extra_%';
GO

-- ==============================================================================
-- 1. TẠO 50 CANDIDATE (THÍ SINH)
-- (SBD nối tiếp từ 021 lên 070)
-- ==============================================================================
;WITH nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 50
)
INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, TakeTheory, TakePractical, TakeRoadLayout, TakeOnRoad, ReasonForTaking, PhotoImageUrl, TakeNo)
SELECT 
    RIGHT(N'000' + CAST(n + 20 AS NVARCHAR(3)), 3),
    N'Thí sinh ' + CAST(n AS NVARCHAR(2)),
    DATEADD(YEAR, - (20 + (n % 10)), CAST('2000-01-01' AS DATETIME)),
    N'09' + RIGHT(N'00000000' + CAST(912930000 + n AS NVARCHAR(10)), 8),
    CASE WHEN n % 2 = 0 THEN N'Nữ' ELSE N'Nam' END,
    N'0791293' + RIGHT(N'00000' + CAST(n AS NVARCHAR(5)), 5),
    N'Địa chỉ thí sinh số ' + CAST(n AS NVARCHAR(2)) + N', Hà Nội',
    1, NULL, 1, 1,
    N'Thi lần đầu - khoá B-1292 (bổ sung)',
    N'/docs/photos/b1292_extra_' + RIGHT(N'0' + CAST(n AS NVARCHAR(2)), 2) + N'.jpg',
    1
FROM nums
OPTION (MAXRECURSION 0);
GO

-- ==============================================================================
-- 2. TẠO EXAM ENROLLMENT VÀO 3 CA THI CỦA B-1292
-- ==============================================================================
INSERT INTO ExamEnrollment (CandidateId, SessionId)
SELECT 
    c.CandidateId, 
    s.SessionId
FROM Candidate c
CROSS JOIN [Session] s
WHERE c.GovernmentIdNumber LIKE N'0791293%'
  AND s.SessionName IN (N'Ca B-1292 - Lý thuyết', N'Ca B-1292 - Sa hình', N'Ca B-1292 - Đường trường');
GO

PRINT 'SEEDED 50 CANDIDATES AND ENROLLED INTO EXAM SUCCESSFULLY!';
