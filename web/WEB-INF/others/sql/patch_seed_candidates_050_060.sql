-- ============================================
-- Patch: thí sinh SBD 050–060 + ExamEnrollment đủ 3 ca kỳ B-20260601
-- Chạy trên DB đã có DDL + DML cơ bản (không cần DELETE toàn bộ).
-- An toàn khi chạy lại: bỏ qua bản ghi đã tồn tại.
-- ============================================

USE DLEM_DB_2;
GO

-- 1. Thí sinh (chỉ thêm nếu chưa có SBD trong kỳ B-20260601)
INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address,
    TakeTheory, TakeLayout, TakeRoad, TakeNo, ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended)
SELECT v.CandidateNumber, v.FullName, v.DateOfBirth, v.PhoneNumber, v.Sex, v.GovernmentIdNumber, v.Address,
    v.TakeTheory, v.TakeLayout, v.TakeRoad, v.TakeNo, v.ReasonForTaking, v.PhotoImageUrl, 0, 0
FROM (VALUES
    (N'050', N'Lê Thanh Bình',   '1998-07-12', N'0905005001', 1, N'001198071201', N'10 Phạm Văn Đồng, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B',
        N'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127999/050_spneqa.png'),
    (N'051', N'Phạm Thu Hà',      '1999-03-25', N'0905005101', 0, N'001199032501', N'22 Nguyễn Chí Thanh, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B',
        N'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128000/051_qdraol.png'),
    (N'052', N'Hoàng Minh Tuấn',  '2000-11-08', N'0905005201', 1, N'001200110801', N'5 Láng Hạ, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B',
        N'https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780128000/052_lwmzvd.png'),
    (N'053', N'Võ Thị Lan',       '1997-01-19', N'0905005301', 0, N'001197011901', N'88 Kim Mã, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', NULL),
    (N'054', N'Đặng Văn Phúc',    '1996-06-30', N'0905005401', 1, N'001196063001', N'15 Xã Đàn, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', NULL),
    (N'055', N'Bùi Thị Ngọc',     '2001-12-02', N'0905005501', 0, N'001201120201', N'40 Giảng Võ, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', NULL),
    (N'056', N'Nguyễn Quốc Huy',  '1995-04-17', N'0905005601', 1, N'001195041701', N'72 Trần Duy Hưng, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', NULL),
    (N'057', N'Trần Thị Mai',     '1998-09-21', N'0905005701', 0, N'001198092101', N'9 Hoàng Quốc Việt, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', NULL),
    (N'058', N'Lý Văn Đạt',       '1999-08-14', N'0905005801', 1, N'001199081401', N'31 Đội Cấn, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', NULL),
    (N'059', N'Phan Thị Oanh',    '2000-02-27', N'0905005901', 0, N'001200022701', N'60 Thái Hà, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', NULL),
    (N'060', N'Mai Văn Sơn',      '1997-10-05', N'0905006001', 1, N'001197100501', N'18 Chùa Bộc, Hà Nội', 1, 1, 1, 1, N'Thi cấp mới hạng B', NULL)
) v(CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address,
    TakeTheory, TakeLayout, TakeRoad, TakeNo, ReasonForTaking, PhotoImageUrl)
WHERE NOT EXISTS (
    SELECT 1 FROM Candidate c WHERE c.CandidateNumber = v.CandidateNumber
);
GO

-- 2. Ghi danh đủ 3 ca (chỉ thêm cặp Candidate–Session chưa có)
INSERT INTO ExamEnrollment (CandidateId, SessionId, SectionStatus, SignaturePrinted, ExamDeviceId)
SELECT c.CandidateId, s.SessionId, N'Pending', 0, NULL
FROM Candidate c
CROSS JOIN [Session] s
WHERE c.CandidateNumber IN (
    N'050', N'051', N'052', N'053', N'054', N'055', N'056', N'057', N'058', N'059', N'060'
)
  AND s.SessionName IN (
    N'Ca sáng - Lý thuyết B',
    N'Ca sáng - Thực hành trong hình B',
    N'Ca chiều - Thực hành trên đường B'
  )
  AND NOT EXISTS (
    SELECT 1 FROM ExamEnrollment ee
    WHERE ee.CandidateId = c.CandidateId AND ee.SessionId = s.SessionId
  );
GO

-- 3. Ghi danh ca lý thuyết kỳ B-20260615 (chỉ ca lý thuyết – kỳ này chưa có ca thực hành trong seed)
INSERT INTO ExamEnrollment (CandidateId, SessionId, SectionStatus, SignaturePrinted, ExamDeviceId)
SELECT c.CandidateId, s.SessionId, N'Pending', 0, NULL
FROM Candidate c
CROSS JOIN [Session] s
WHERE c.CandidateNumber IN (
    N'050', N'051', N'052', N'053', N'054', N'055', N'056', N'057', N'058', N'059', N'060'
)
  AND s.SessionName = N'Ca sáng - Lý thuyết B (kỳ 2)'
  AND NOT EXISTS (
    SELECT 1 FROM ExamEnrollment ee
    WHERE ee.CandidateId = c.CandidateId AND ee.SessionId = s.SessionId
  );
GO
