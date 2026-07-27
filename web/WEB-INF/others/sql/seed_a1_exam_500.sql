-- ============================================
-- Seed kỳ A1-20260601-1000: 20 thí sinh + phân công SHV
-- Chạy SAU: DDL → DML → seed_candidate.sql
-- shv_tung → Lý thuyết | shv_lan → Thực hành trong hình
-- ============================================

USE DLEM_DB_2;
GO

SET NOCOUNT ON;
GO

DECLARE @A1ExamId INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601-1000');
DECLARE @TheorySectionId INT;
DECLARE @LayoutSectionId INT;
DECLARE @TheoryAreaId INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2');
DECLARE @LayoutAreaId INT = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô');
DECLARE @TungId INT = (SELECT UserId FROM [User] WHERE Username = N'shv_tung');
DECLARE @LanId INT = (SELECT UserId FROM [User] WHERE Username = N'shv_lan');
DECLARE @AssignedBy INT = (SELECT UserId FROM [User] WHERE Username = N'exam_minh');

IF @A1ExamId IS NULL
BEGIN
    RAISERROR(N'Không tìm thấy exam A1-20260601-1000. Chạy DML_DLEM_DB.sql trước.', 16, 1);
    RETURN;
END;

SELECT @TheorySectionId = ExamSectionId
FROM ExamSection
WHERE ExamId = @A1ExamId AND SectionType = N'Lý thuyết';

SELECT @LayoutSectionId = ExamSectionId
FROM ExamSection
WHERE ExamId = @A1ExamId AND SectionType = N'Thực hành trong hình';

-- 1. Kích hoạt kỳ thi
UPDATE Exam
SET [Status] = N'Đang diễn ra'
   ,ExamPassword = NULL
WHERE ExamId = @A1ExamId;
GO

-- 2. Khu vực thi cho kỳ A1-1000
INSERT INTO Exam_ExamArea (ExamId, ExamAreaId)
SELECT e.ExamId, ea.ExamAreaId
FROM Exam e
CROSS JOIN ExamArea ea
WHERE e.ExamCode = N'A1-20260601-1000'
  AND ea.AreaName IN (N'Phòng thi LT 2', N'Sân thi mô tô')
  AND NOT EXISTS (
      SELECT 1 FROM Exam_ExamArea x
      WHERE x.ExamId = e.ExamId AND x.ExamAreaId = ea.ExamAreaId
  );
GO

-- 3. Phân công sát hạch viên (xóa cũ nếu có, gán mới)
DELETE FROM ExaminerSchedule
WHERE ExamId = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601-1000');

INSERT INTO ExaminerSchedule (ExamId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt)
SELECT e.ExamId, es.ExamSectionId, ea.ExamAreaId, u.UserId, ab.UserId, '2026-06-01 06:30:00'
FROM Exam e
JOIN ExamSection es ON es.ExamId = e.ExamId AND es.SectionType = N'Lý thuyết'
JOIN ExamArea ea ON ea.AreaName = N'Phòng thi LT 2'
JOIN [User] u ON u.Username = N'shv_tung'
JOIN [User] ab ON ab.Username = N'exam_minh'
WHERE e.ExamCode = N'A1-20260601-1000';

INSERT INTO ExaminerSchedule (ExamId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt)
SELECT e.ExamId, es.ExamSectionId, ea.ExamAreaId, u.UserId, ab.UserId, '2026-06-01 06:35:00'
FROM Exam e
JOIN ExamSection es ON es.ExamId = e.ExamId AND es.SectionType = N'Thực hành trong hình'
JOIN ExamArea ea ON ea.AreaName = N'Sân thi mô tô'
JOIN [User] u ON u.Username = N'shv_lan'
JOIN [User] ab ON ab.Username = N'exam_minh'
WHERE e.ExamCode = N'A1-20260601-1000';
GO

-- 4. Reset kỳ seed: chỉ giữ SBD 1-20 và chưa thi gì
DECLARE @ResetExamId INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601-1000');

DELETE ca
FROM CandidateAnswer ca
JOIN TheoryPaper tp ON tp.TheoryPaperId = ca.TheoryPaperId
JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
WHERE ee.ExamId = @ResetExamId;

DELETE tp
FROM TheoryPaper tp
JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
WHERE ee.ExamId = @ResetExamId;

DELETE dr
FROM DeductionRecord dr
JOIN ExamScore esc ON esc.ExamScoreId = dr.ExamScoreId
JOIN ExamResult er ON er.ExamResultId = esc.ExamResultId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
WHERE ee.ExamId = @ResetExamId;

DELETE esc
FROM ExamScore esc
JOIN ExamResult er ON er.ExamResultId = esc.ExamResultId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
WHERE ee.ExamId = @ResetExamId;

DELETE er
FROM ExamResult er
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
WHERE ee.ExamId = @ResetExamId;

DELETE p
FROM Payment p
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE ee.ExamId = @ResetExamId
  AND (TRY_CAST(c.CandidateNumber AS INT) IS NULL OR TRY_CAST(c.CandidateNumber AS INT) NOT BETWEEN 1 AND 20);

DELETE cv
FROM CandidateViolation cv
JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = cv.ExamEnrollmentSectionId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
WHERE ee.ExamId = @ResetExamId;

DELETE ees
FROM ExamEnrollmentSection ees
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE ee.ExamId = @ResetExamId
  AND (TRY_CAST(c.CandidateNumber AS INT) IS NULL OR TRY_CAST(c.CandidateNumber AS INT) NOT BETWEEN 1 AND 20);

DELETE ee
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE ee.ExamId = @ResetExamId
  AND (TRY_CAST(c.CandidateNumber AS INT) IS NULL OR TRY_CAST(c.CandidateNumber AS INT) NOT BETWEEN 1 AND 20);

UPDATE ees
SET ees.Status = N'Chưa thi',
    ees.CheckedInAt = NULL,
    ees.CheckedInBy = NULL,
    ees.StartedAt = NULL,
    ees.CompletedAt = NULL,
    ees.ResultPrintedAt = NULL,
    ees.ExamDeviceId = NULL,
    ees.ExamAreaId = CASE
        WHEN es.SectionType = N'Lý thuyết' THEN (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')
        WHEN es.SectionType = N'Thực hành trong hình' THEN (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô')
        ELSE ees.ExamAreaId
    END
FROM ExamEnrollmentSection ees
JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE ee.ExamId = @ResetExamId
  AND TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 20;

UPDATE c
SET c.IsAbsent = 0,
    c.IsSuspended = 0
FROM Candidate c
JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
WHERE ee.ExamId = @ResetExamId
  AND TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 20;
GO

INSERT INTO ExamEnrollment (CandidateId, ExamId, AllocatedExamAreaId, ExamDeviceId)
SELECT c.CandidateId,
       (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601-1000'),
       NULL,
       NULL
FROM Candidate c
WHERE TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 20
  AND NOT EXISTS (
      SELECT 1 FROM ExamEnrollment ec
      WHERE ec.CandidateId = c.CandidateId
        AND ec.ExamId = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601-1000')
  );

UPDATE c
SET c.IsAbsent = 0,
    c.IsSuspended = 0
FROM Candidate c
JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
JOIN Exam e ON e.ExamId = ee.ExamId AND e.ExamCode = N'A1-20260601-1000'
WHERE TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 20;
GO

-- 5a. Phần thi Lý thuyết (TakeTheory = 1)
INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, ExamAreaId, ExamDeviceId, Status)
SELECT ec.ExamEnrollmentId, es.ExamSectionId,
       (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2'),
       NULL, N'Chưa thi'
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN Exam e ON e.ExamId = ec.ExamId AND e.ExamCode = N'A1-20260601-1000'
JOIN ExamSection es ON es.ExamId = ec.ExamId AND es.SectionType = N'Lý thuyết'
WHERE c.TakeTheory = 1
  AND TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 20
  AND NOT EXISTS (
      SELECT 1 FROM ExamEnrollmentSection ees
      WHERE ees.ExamEnrollmentId = ec.ExamEnrollmentId
        AND ees.ExamSectionId = es.ExamSectionId
  );
GO

-- 5b. Phần thi Thực hành trong hình (TakeLayout = 1)
INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, ExamAreaId, ExamDeviceId, Status)
SELECT ec.ExamEnrollmentId, es.ExamSectionId,
       (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô'),
       NULL, N'Chưa thi'
FROM ExamEnrollment ec
JOIN Candidate c ON c.CandidateId = ec.CandidateId
JOIN Exam e ON e.ExamId = ec.ExamId AND e.ExamCode = N'A1-20260601-1000'
JOIN ExamSection es ON es.ExamId = ec.ExamId AND es.SectionType = N'Thực hành trong hình'
WHERE c.TakeLayout = 1
  AND TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 20
  AND NOT EXISTS (
      SELECT 1 FROM ExamEnrollmentSection ees
      WHERE ees.ExamEnrollmentId = ec.ExamEnrollmentId
        AND ees.ExamSectionId = es.ExamSectionId
  );
GO

-- 6. Thanh toán hoàn tất cho mọi ghi danh
INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
SELECT N'Hoàn tất',
       N'Chuyển khoản',
       N'A1-1000-PAY-' + RIGHT(N'000000' + CAST(ec.ExamEnrollmentId AS NVARCHAR(10)), 6),
       550000.00,
       '2026-05-28 10:00:00',
       ec.ExamEnrollmentId
FROM ExamEnrollment ec
JOIN Exam e ON e.ExamId = ec.ExamId AND e.ExamCode = N'A1-20260601-1000'
WHERE NOT EXISTS (
    SELECT 1 FROM Payment p WHERE p.ExamEnrollmentId = ec.ExamEnrollmentId
);
GO

-- 7. Quy tắc nghiệp vụ: chưa hoàn tất LT thì TH luôn ở trạng thái "Chưa thi"
DECLARE @SeedExamId INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601-1000');

UPDATE eesLayout
SET eesLayout.Status = N'Chưa thi',
    eesLayout.CheckedInAt = NULL,
    eesLayout.CheckedInBy = NULL,
    eesLayout.StartedAt = NULL,
    eesLayout.CompletedAt = NULL,
    eesLayout.ResultPrintedAt = NULL
FROM ExamEnrollmentSection eesLayout
JOIN ExamSection secLayout ON secLayout.ExamSectionId = eesLayout.ExamSectionId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = eesLayout.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
JOIN ExamSection secTheory ON secTheory.ExamId = ee.ExamId AND secTheory.SectionType = N'Lý thuyết'
JOIN ExamEnrollmentSection eesTheory
    ON eesTheory.ExamEnrollmentId = ee.ExamEnrollmentId
   AND eesTheory.ExamSectionId = secTheory.ExamSectionId
WHERE ee.ExamId = @SeedExamId
  AND secLayout.SectionType = N'Thực hành trong hình'
  AND c.TakeTheory = 1
  AND ISNULL(eesTheory.Status, N'Chưa thi') <> N'Đã thi';
GO

PRINT N'seed_a1_exam_500.sql hoàn tất với 20 thí sinh chưa thi.';
GO
