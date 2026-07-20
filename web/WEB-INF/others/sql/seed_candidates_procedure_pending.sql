-- ============================================
-- Seed thí sinh chờ thủ tục (chưa ảnh, chưa thanh toán)
-- Prerequisite: DML_DLEM_DB.sql đã chạy
-- Kỳ thi mặc định: A1-20260601-1000
--
-- Nhóm:
--   601–605  Bảo lưu lý thuyết  (TakeTheory=0, TakeLayout=1, TakeNo=2 → Retake)
--            → sau khi làm thủ tục có thể vào allocation practical
--   606–610  Thi cả 2           (TakeTheory=1, TakeLayout=1, TakeNo=1)
--   611–615  Bảo lưu thực hành  (TakeTheory=1, TakeLayout=0, TakeNo=1)
--
-- Chạy lại được: xóa theo CCCD prefix 0799006 trước khi insert.
-- ============================================

USE DLEM_DB_2;
GO

DECLARE @ExamId INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601-1000');
IF @ExamId IS NULL
BEGIN
    RAISERROR(N'Không tìm thấy kỳ A1-20260601-1000. Chạy DML_DLEM_DB.sql trước.', 16, 1);
    RETURN;
END;

DECLARE @TheorySectionId INT = (
    SELECT ExamSectionId FROM ExamSection
    WHERE ExamId = @ExamId AND SectionType = N'Lý thuyết'
);
DECLARE @PracticalSectionId INT = (
    SELECT ExamSectionId FROM ExamSection
    WHERE ExamId = @ExamId AND SectionType = N'Thực hành trong hình'
);

-- Phòng / sân gắn kỳ A1 (idempotent) để allocation có chỗ chọn
IF NOT EXISTS (
    SELECT 1 FROM Exam_ExamArea
    WHERE ExamId = @ExamId
      AND ExamAreaId = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1')
)
INSERT INTO Exam_ExamArea (ExamId, ExamAreaId) VALUES
(@ExamId, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 1'));

IF NOT EXISTS (
    SELECT 1 FROM Exam_ExamArea
    WHERE ExamId = @ExamId
      AND ExamAreaId = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2')
)
INSERT INTO Exam_ExamArea (ExamId, ExamAreaId) VALUES
(@ExamId, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng thi LT 2'));

IF NOT EXISTS (
    SELECT 1 FROM Exam_ExamArea
    WHERE ExamId = @ExamId
      AND ExamAreaId = (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô')
)
INSERT INTO Exam_ExamArea (ExamId, ExamAreaId) VALUES
(@ExamId, (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô'));

-- Phân công SHV sân thực hành (nếu chưa có)
IF NOT EXISTS (
    SELECT 1 FROM ExaminerSchedule
    WHERE ExamId = @ExamId AND ExamSectionId = @PracticalSectionId
)
INSERT INTO ExaminerSchedule (ExamId, ExamSectionId, ExamAreaId, ExaminerId, AssignedBy, AssignedAt)
VALUES (
    @ExamId,
    @PracticalSectionId,
    (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi mô tô'),
    (SELECT UserId FROM [User] WHERE Username = N'shv_tung'),
    (SELECT UserId FROM [User] WHERE Username = N'exam_hoa'),
    '2026-05-28 09:00:00'
);
GO

-- Xóa seed cũ (nếu chạy lại)
DECLARE @ExamId INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601-1000');

DELETE pf
FROM Payment_Fee pf
JOIN Payment p ON p.PaymentId = pf.PaymentId
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE c.GovernmentIdNumber LIKE N'0799006%';

DELETE p
FROM Payment p
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE c.GovernmentIdNumber LIKE N'0799006%';

DELETE ees
FROM ExamEnrollmentSection ees
JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE c.GovernmentIdNumber LIKE N'0799006%';

DELETE ee
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE c.GovernmentIdNumber LIKE N'0799006%';

DELETE FROM Candidate WHERE GovernmentIdNumber LIKE N'0799006%';
GO

-- ============================================
-- 1. CANDIDATE — chưa ảnh (PhotoImageUrl NULL), chưa Payment
-- ============================================
INSERT INTO Candidate (
    CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex,
    GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeNo,
    ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended
) VALUES
-- Bảo lưu lý thuyết (chỉ thi thực hành; TakeNo=2 = Retake → đủ điều kiện stage TH sau thủ tục)
(N'601', N'Nguyễn Văn Bảo',   '1999-02-11', N'0911601001', N'bao.nguyen.seed@example.com',   1, N'079900600601', N'12 Trần Duy Hưng, Cầu Giấy, Hà Nội',     0, 1, 2, N'Bảo lưu lý thuyết – thi lại thực hành hạng A1', NULL, 0, 0),
(N'602', N'Trần Thị Lan Anh', '2001-07-22', N'0911601002', N'lananh.tran.seed@example.com', 0, N'079900600602', N'45 Nguyễn Chí Thanh, Đống Đa, Hà Nội',   0, 1, 2, N'Bảo lưu lý thuyết – thi lại thực hành hạng A1', NULL, 0, 0),
(N'603', N'Lê Minh Quân',     '1998-11-03', N'0911601003', N'quan.le.seed@example.com',      1, N'079900600603', N'88 Láng Hạ, Ba Đình, Hà Nội',            0, 1, 2, N'Bảo lưu lý thuyết – thi lại thực hành hạng A1', NULL, 0, 0),
(N'604', N'Phạm Thị Mai',     '2000-04-18', N'0911601004', N'mai.pham.seed@example.com',     0, N'079900600604', N'23 Tây Sơn, Đống Đa, Hà Nội',            0, 1, 2, N'Bảo lưu lý thuyết – thi lại thực hành hạng A1', NULL, 0, 0),
(N'605', N'Hoàng Văn Đức',    '1997-09-29', N'0911601005', N'duc.hoang.seed@example.com',    1, N'079900600605', N'67 Giải Phóng, Hai Bà Trưng, Hà Nội',    0, 1, 2, N'Bảo lưu lý thuyết – thi lại thực hành hạng A1', NULL, 0, 0),

-- Thi cả 2
(N'606', N'Vũ Thị Hằng',      '2002-01-15', N'0911601006', N'hang.vu.seed@example.com',      0, N'079900600606', N'19 Kim Mã, Ba Đình, Hà Nội',             1, 1, 1, N'Thi sát hạch cấp mới hạng A1 (lý thuyết + thực hành)', NULL, 0, 0),
(N'607', N'Đặng Văn Phong',   '1996-06-08', N'0911601007', N'phong.dang.seed@example.com',   1, N'079900600607', N'54 Hoàng Quốc Việt, Cầu Giấy, Hà Nội',  1, 1, 1, N'Thi sát hạch cấp mới hạng A1 (lý thuyết + thực hành)', NULL, 0, 0),
(N'608', N'Bùi Thị Ngọc',     '2001-12-25', N'0911601008', N'ngoc.bui.seed@example.com',     0, N'079900600608', N'31 Trần Phú, Hà Đông, Hà Nội',           1, 1, 1, N'Thi sát hạch cấp mới hạng A1 (lý thuyết + thực hành)', NULL, 0, 0),
(N'609', N'Đỗ Minh Tuấn',     '1999-08-14', N'0911601009', N'tuan.do.seed@example.com',      1, N'079900600609', N'102 Nguyễn Trãi, Thanh Xuân, Hà Nội',   1, 1, 1, N'Thi sát hạch cấp mới hạng A1 (lý thuyết + thực hành)', NULL, 0, 0),
(N'610', N'Ngô Thị Thu',      '2000-03-07', N'0911601010', N'thu.ngo.seed@example.com',      0, N'079900600610', N'76 Lê Văn Lương, Thanh Xuân, Hà Nội',   1, 1, 1, N'Thi sát hạch cấp mới hạng A1 (lý thuyết + thực hành)', NULL, 0, 0),

-- Bảo lưu thực hành (chỉ thi lý thuyết)
(N'611', N'Phan Văn Khoa',    '1995-05-20', N'0911601011', N'khoa.phan.seed@example.com',    1, N'079900600611', N'15 Đê La Thành, Đống Đa, Hà Nội',       1, 0, 1, N'Bảo lưu thực hành – chỉ thi lý thuyết hạng A1', NULL, 0, 0),
(N'612', N'Lý Thị Yến',       '1998-10-02', N'0911601012', N'yen.ly.seed@example.com',       0, N'079900600612', N'40 Xuân Thủy, Cầu Giấy, Hà Nội',        1, 0, 1, N'Bảo lưu thực hành – chỉ thi lý thuyết hạng A1', NULL, 0, 0),
(N'613', N'Trịnh Văn Hùng',   '2001-11-19', N'0911601013', N'hung.trinh.seed@example.com',   1, N'079900600613', N'28 Tôn Thất Tùng, Đống Đa, Hà Nội',     1, 0, 1, N'Bảo lưu thực hành – chỉ thi lý thuyết hạng A1', NULL, 0, 0),
(N'614', N'Cao Thị Hương',    '1997-01-30', N'0911601014', N'huong.cao.seed@example.com',    0, N'079900600614', N'91 Phạm Văn Đồng, Bắc Từ Liêm, Hà Nội', 1, 0, 1, N'Bảo lưu thực hành – chỉ thi lý thuyết hạng A1', NULL, 0, 0),
(N'615', N'Dương Văn Nam',    '1999-07-12', N'0911601015', N'nam.duong.seed@example.com',    1, N'079900600615', N'63 Minh Khai, Hai Bà Trưng, Hà Nội',    1, 0, 1, N'Bảo lưu thực hành – chỉ thi lý thuyết hạng A1', NULL, 0, 0);
GO

-- ============================================
-- 2. ENROLLMENT → kỳ A1-20260601-1000
-- ============================================
INSERT INTO ExamEnrollment (CandidateId, ExamId, AllocatedExamAreaId, ExamDeviceId)
SELECT c.CandidateId, e.ExamId, NULL, NULL
FROM Candidate c
CROSS JOIN Exam e
WHERE e.ExamCode = N'A1-20260601-1000'
  AND c.GovernmentIdNumber LIKE N'0799006%'
  AND NOT EXISTS (
      SELECT 1 FROM ExamEnrollment ee
      WHERE ee.CandidateId = c.CandidateId AND ee.ExamId = e.ExamId
  );
GO

-- ============================================
-- 3. ENROLLMENT SECTION theo bảo lưu
-- ============================================
DECLARE @ExamId INT = (SELECT ExamId FROM Exam WHERE ExamCode = N'A1-20260601-1000');
DECLARE @TheorySectionId INT = (
    SELECT ExamSectionId FROM ExamSection
    WHERE ExamId = @ExamId AND SectionType = N'Lý thuyết'
);
DECLARE @PracticalSectionId INT = (
    SELECT ExamSectionId FROM ExamSection
    WHERE ExamId = @ExamId AND SectionType = N'Thực hành trong hình'
);

-- Section lý thuyết: thi cả 2 + bảo lưu thực hành (TakeTheory = 1)
INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, ExamAreaId, ExamDeviceId, [Status])
SELECT ee.ExamEnrollmentId, @TheorySectionId, NULL, NULL, N'Pending'
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE ee.ExamId = @ExamId
  AND c.GovernmentIdNumber LIKE N'0799006%'
  AND ISNULL(c.TakeTheory, 1) = 1
  AND NOT EXISTS (
      SELECT 1 FROM ExamEnrollmentSection x
      WHERE x.ExamEnrollmentId = ee.ExamEnrollmentId AND x.ExamSectionId = @TheorySectionId
  );

-- Section thực hành: thi cả 2 + bảo lưu lý thuyết (TakeLayout = 1)
INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, ExamAreaId, ExamDeviceId, [Status])
SELECT ee.ExamEnrollmentId, @PracticalSectionId, NULL, NULL, N'Pending'
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
WHERE ee.ExamId = @ExamId
  AND c.GovernmentIdNumber LIKE N'0799006%'
  AND ISNULL(c.TakeLayout, 1) = 1
  AND NOT EXISTS (
      SELECT 1 FROM ExamEnrollmentSection x
      WHERE x.ExamEnrollmentId = ee.ExamEnrollmentId AND x.ExamSectionId = @PracticalSectionId
  );
GO

-- ============================================
-- 4. Kiểm tra nhanh
-- ============================================
SELECT
    c.CandidateNumber AS SBD,
    c.FullName,
    c.TakeTheory,
    c.TakeLayout,
    c.TakeNo,
    CASE
        WHEN c.TakeTheory = 0 AND c.TakeLayout = 1 THEN N'Bảo lưu LT'
        WHEN c.TakeTheory = 1 AND c.TakeLayout = 0 THEN N'Bảo lưu TH'
        ELSE N'Thi cả 2'
    END AS Nhom,
    c.PhotoImageUrl,
    CAST(CASE WHEN pay.PaymentId IS NOT NULL THEN 1 ELSE 0 END AS BIT) AS DaThanhToan,
    (SELECT COUNT(*) FROM ExamEnrollmentSection ees WHERE ees.ExamEnrollmentId = ee.ExamEnrollmentId) AS SoSection
FROM Candidate c
JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
JOIN Exam e ON e.ExamId = ee.ExamId AND e.ExamCode = N'A1-20260601-1000'
LEFT JOIN (
    SELECT ExamEnrollmentId, MIN(PaymentId) AS PaymentId
    FROM Payment
    WHERE PaymentStatus IN (N'Hoàn tất', N'Completed', N'Paid')
    GROUP BY ExamEnrollmentId
) pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
WHERE c.GovernmentIdNumber LIKE N'0799006%'
ORDER BY c.CandidateNumber;
GO
