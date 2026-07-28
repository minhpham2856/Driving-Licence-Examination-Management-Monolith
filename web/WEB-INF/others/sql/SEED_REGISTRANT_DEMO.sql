-- =============================================================================
-- SEED REGISTRANT DEMO + 2 KỲ THI CHƯA BẮT ĐẦU
-- Chạy sau: DDL_DLEM_DB.sql -> DML_DLEM_DB.sql
--
-- Tất cả tài khoản demo dùng mật khẩu: login123
-- PasswordHash bên dưới là BCrypt, cùng giá trị DML_DLEM_DB.sql đang dùng.
--
-- Quy ước đúng theo code registrant hiện tại:
--   * ExamRegistration workflow hồ sơ: Draft / Pending / Approved / Rejected.
--   * ExamRegistration vòng đời kỳ thi: PreRegistered, CheckedIn, ...
--   * Document KHÔNG có cột trạng thái.
--   * Document.Notes dùng #PENDING#, #APPROVED# hoặc nội dung "Từ chối".
--   * Hồ sơ chính có marker #PROFILE_DOC# trong ExamRegistration.Notes.
--   * Kỳ thi chính thức được liên kết bằng #EXAM_ID#<id>#.
--
-- Ma trận 12 tài khoản:
--   1 empty + 1 pending đủ + 8 approved + 1 pending thiếu + 1 pending để test reject.
--   10 profile đủ 4/4 tài liệu; 1 profile có 2/4; empty không có tài liệu.
--   12 ExamRegistration workflow: 1 Pending đủ, 9 Approved (một profile có 2 hạng),
--   1 Pending thiếu, 1 Pending sẵn sàng test từ chối.
--   9 RegistrationDates cho 9 dòng Approved, cùng một ExamDate A1.
--
-- Hai kỳ thi chính thức:
--   DEMO-A1-20260728-1200: 28/07/2026 12:00, 20 thí sinh.
--   DEMO-A1-20260730-0800: 30/07/2026 08:00, 20 thí sinh khác.
--   Cả hai Chưa diễn ra, chưa ảnh, chưa payment, chưa check-in, chưa phân phòng,
--   đủ bảng nối khu vực và không có ExaminerSchedule.
-- =============================================================================

USE DLEM_DB_2;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @PasswordHash NVARCHAR(255) =
        N'$2a$10$E8ocGIv4gRp6xZurl5egNuxir.0zn/5BUJMO5kIjdz38csrH3s7Cm';
    DECLARE @RegistrantRoleId INT =
        (SELECT RoleId FROM [Role] WHERE RoleName = N'Người đăng ký thi');
    DECLARE @A1LicenceId INT =
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1');
    DECLARE @ALicenceId INT =
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A');

    IF @RegistrantRoleId IS NULL OR @A1LicenceId IS NULL OR @ALicenceId IS NULL
        THROW 51000, N'Thiếu Role Người đăng ký thi hoặc Licence A1/A. Hãy chạy DML_DLEM_DB.sql trước.', 1;

    IF NOT EXISTS (SELECT 1 FROM [User] u JOIN [Role] r ON r.RoleId = u.RoleId
                   WHERE r.RoleName = N'Sát hạch viên' AND u.IsActive = 1)
        THROW 51001, N'Không có sát hạch viên hoạt động. Hãy chạy DML_DLEM_DB.sql trước.', 1;

    DECLARE @RequiredDocumentTypes TABLE (
        UiType NVARCHAR(50) NOT NULL,
        DbType NVARCHAR(100) NOT NULL,
        DocumentUrl NVARCHAR(500) NOT NULL
    );

    INSERT INTO @RequiredDocumentTypes (UiType, DbType, DocumentUrl) VALUES
    (N'Portrait', N'Ảnh chân dung 3x4',
     N'https://res.cloudinary.com/dv0xxzkyy/image/upload/v1785210082/8733aafb-5242-43c6-8784-7c8d35ef12b1_iugaii.jpg'),
    (N'IdFront', N'Căn cước công dân (mặt trước)',
     N'https://res.cloudinary.com/dv0xxzkyy/image/upload/v1785210082/254c917d-17c3-459c-ba5d-9761fbd43330_qpwbzm.jpg'),
    (N'IdBack', N'Căn cước công dân (mặt sau)',
     N'https://res.cloudinary.com/dv0xxzkyy/image/upload/v1785210082/d4c5e5c9-4db7-4a5d-8263-78a58a7afaac_ujcgla.jpg'),
    (N'HealthCertificate', N'Giấy khám sức khỏe',
     N'https://res.cloudinary.com/dv0xxzkyy/image/upload/v1785210082/cf6bcd14-1cc3-40d1-9557-0ee7210fa204_cevlwr.jpg');

    IF EXISTS (
        SELECT 1
        FROM @RequiredDocumentTypes r
        WHERE NOT EXISTS (SELECT 1 FROM DocumentType dt WHERE dt.[Type] = r.DbType)
    )
        THROW 51002, N'Thiếu một trong bốn DocumentType bắt buộc của cổng Registrant.', 1;

    DECLARE @Accounts TABLE (
        SortNo INT PRIMARY KEY,
        Username NVARCHAR(100) NOT NULL,
        Email NVARCHAR(255) NOT NULL,
        FullName NVARCHAR(255) NOT NULL,
        DateOfBirth DATE NOT NULL,
        PhoneNumber NVARCHAR(20) NOT NULL,
        Sex BIT NOT NULL,
        GovernmentIdNumber NVARCHAR(100) NOT NULL,
        [Address] NVARCHAR(500) NOT NULL,
        DemoState NVARCHAR(30) NOT NULL
    );

    INSERT INTO @Accounts VALUES
    (1,  N'demo_reg_empty',       N'demo.reg.empty@example.test',       N'Demo Chưa Có Hồ Sơ',       '1998-01-11', N'0908100001', 1, N'079098100001', N'Quận 1, Thành phố Hồ Chí Minh', N'EMPTY'),
    (2,  N'demo_reg_pending',     N'demo.reg.pending@example.test',     N'Demo Hồ Sơ Chờ Duyệt',     '1998-02-12', N'0908100002', 0, N'079098100002', N'Quận 3, Thành phố Hồ Chí Minh', N'PENDING_FULL'),
    (3,  N'demo_reg_approved_01', N'demo.reg.approved01@example.test',  N'Demo Đã Duyệt 01',         '1998-03-13', N'0908100003', 1, N'079098100003', N'Ba Đình, Hà Nội', N'APPROVED'),
    (4,  N'demo_reg_approved_02', N'demo.reg.approved02@example.test',  N'Demo Đã Duyệt 02',         '1998-04-14', N'0908100004', 0, N'079098100004', N'Hoàn Kiếm, Hà Nội', N'APPROVED'),
    (5,  N'demo_reg_approved_03', N'demo.reg.approved03@example.test',  N'Demo Đã Duyệt 03',         '1998-05-15', N'0908100005', 1, N'079098100005', N'Hải Châu, Đà Nẵng', N'APPROVED'),
    (6,  N'demo_reg_approved_04', N'demo.reg.approved04@example.test',  N'Demo Đã Duyệt 04',         '1998-06-16', N'0908100006', 0, N'079098100006', N'Thanh Khê, Đà Nẵng', N'APPROVED'),
    (7,  N'demo_reg_approved_05', N'demo.reg.approved05@example.test',  N'Demo Đã Duyệt 05',         '1998-07-17', N'0908100007', 1, N'079098100007', N'Ninh Kiều, Cần Thơ', N'APPROVED'),
    (8,  N'demo_reg_approved_06', N'demo.reg.approved06@example.test',  N'Demo Đã Duyệt 06',         '1998-08-18', N'0908100008', 0, N'079098100008', N'Bình Thủy, Cần Thơ', N'APPROVED'),
    (9,  N'demo_reg_approved_07', N'demo.reg.approved07@example.test',  N'Demo Thi Lại 07',           '1998-09-19', N'0908100009', 1, N'079098100009', N'Thành phố Huế', N'APPROVED'),
    (10, N'demo_reg_approved_08', N'demo.reg.approved08@example.test',  N'Demo Thi Lại 08',           '1998-10-20', N'0908100010', 0, N'079098100010', N'Thành phố Huế', N'APPROVED'),
    (11, N'demo_reg_missing',     N'demo.reg.missing@example.test',     N'Demo Thiếu Tài Liệu',       '1998-11-21', N'0908100011', 1, N'079098100011', N'Thành phố Hải Phòng', N'PENDING_MISSING'),
    (12, N'demo_reg_reject_ready',N'demo.reg.reject@example.test',      N'Demo Sẵn Sàng Từ Chối',     '1998-12-22', N'0908100012', 0, N'079098100012', N'Thành phố Hải Phòng', N'PENDING_REJECT');

    -- Upsert User/Profile để seed có thể chạy lại.
    UPDATE u
    SET u.Email = a.Email,
        u.PasswordHash = @PasswordHash,
        u.RoleId = @RegistrantRoleId,
        u.IsActive = 1
    FROM [User] u
    JOIN @Accounts a ON a.Username = u.Username;

    INSERT INTO [User] (Username, Email, PasswordHash, RoleId, IsActive)
    SELECT a.Username, a.Email, @PasswordHash, @RegistrantRoleId, 1
    FROM @Accounts a
    WHERE NOT EXISTS (SELECT 1 FROM [User] u WHERE u.Username = a.Username);

    UPDATE p
    SET p.FullName = a.FullName,
        p.DateOfBirth = a.DateOfBirth,
        p.PhoneNumber = a.PhoneNumber,
        p.Sex = a.Sex,
        p.GovernmentIdNumber = a.GovernmentIdNumber,
        p.[Address] = a.[Address]
    FROM Profile p
    JOIN [User] u ON u.UserId = p.UserId
    JOIN @Accounts a ON a.Username = u.Username;

    INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, [Address], UserId)
    SELECT a.FullName, a.DateOfBirth, a.PhoneNumber, a.Sex, a.GovernmentIdNumber,
           a.[Address], u.UserId
    FROM @Accounts a
    JOIN [User] u ON u.Username = a.Username
    WHERE NOT EXISTS (SELECT 1 FROM Profile p WHERE p.UserId = u.UserId);

    -- Xóa riêng dữ liệu hai kỳ thi demo nếu seed được chạy lại.
    DECLARE @DemoExamIds TABLE (ExamId INT PRIMARY KEY);
    INSERT INTO @DemoExamIds (ExamId)
    SELECT ExamId FROM Exam
    WHERE ExamCode IN (N'DEMO-A1-20260728-1200', N'DEMO-A1-20260730-0800');

    DELETE ca
    FROM CandidateAnswer ca
    JOIN TheoryPaper tp ON tp.TheoryPaperId = ca.TheoryPaperId
    JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE tp
    FROM TheoryPaper tp
    JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE dr
    FROM DeductionRecord dr
    JOIN ExamScore esc ON esc.ExamScoreId = dr.ExamScoreId
    JOIN ExamResult er ON er.ExamResultId = esc.ExamResultId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE esc
    FROM ExamScore esc
    JOIN ExamResult er ON er.ExamResultId = esc.ExamResultId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE er
    FROM ExamResult er
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE p
    FROM Payment p
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE cv
    FROM CandidateViolation cv
    JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = cv.ExamEnrollmentSectionId
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE ees
    FROM ExamEnrollmentSection ees
    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE ee
    FROM ExamEnrollment ee
    JOIN @DemoExamIds d ON d.ExamId = ee.ExamId;

    DELETE sd
    FROM ScoreDeduction sd
    JOIN ExamSection es ON es.ExamSectionId = sd.ExamSectionId
    JOIN @DemoExamIds d ON d.ExamId = es.ExamId;

    DELETE esch
    FROM ExaminerSchedule esch
    JOIN @DemoExamIds d ON d.ExamId = esch.ExamId;

    DELETE x
    FROM Exam_ExamArea x
    JOIN @DemoExamIds d ON d.ExamId = x.ExamId;

    DELETE es
    FROM ExamSection es
    JOIN @DemoExamIds d ON d.ExamId = es.ExamId;

    DELETE e
    FROM Exam e
    JOIN @DemoExamIds d ON d.ExamId = e.ExamId;

    DELETE c
    FROM Candidate c
    WHERE (
            c.CandidateNumber LIKE N'D28-%'
         OR c.CandidateNumber LIKE N'D30-%'
         OR (c.FullName LIKE N'Thí Sinh Demo %'
             AND TRY_CAST(c.CandidateNumber AS INT) BETWEEN 1 AND 40)
          )
      AND NOT EXISTS (SELECT 1 FROM ExamEnrollment ee WHERE ee.CandidateId = c.CandidateId);

    -- Reset dữ liệu workflow chỉ của 12 profile demo.
    DECLARE @DemoProfileIds TABLE (ProfileId INT PRIMARY KEY);
    INSERT INTO @DemoProfileIds
    SELECT p.ProfileId
    FROM Profile p
    JOIN [User] u ON u.UserId = p.UserId
    JOIN @Accounts a ON a.Username = u.Username;

    DELETE rd
    FROM RegistrationDates rd
    JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
    JOIN @DemoProfileIds p ON p.ProfileId = er.ProfileId;

    DELETE er
    FROM ExamRegistration er
    JOIN @DemoProfileIds p ON p.ProfileId = er.ProfileId;

    DELETE d
    FROM Document d
    JOIN @DemoProfileIds p ON p.ProfileId = d.ProfileId;

    -- Document: empty = 0; missing = Portrait + IdFront; các profile còn lại đủ 4/4.
    INSERT INTO Document (DocumentTypeId, DocumentUrl, Notes, ProfileId)
    SELECT dt.DocumentTypeId,
           r.DocumentUrl,
           CASE
               WHEN a.DemoState = N'APPROVED'
                   THEN N'#APPROVED# Ban quản lý đã duyệt.'
               ELSE N'#PENDING# Gửi yêu cầu duyệt hồ sơ.'
           END,
           p.ProfileId
    FROM @Accounts a
    JOIN [User] u ON u.Username = a.Username
    JOIN Profile p ON p.UserId = u.UserId
    CROSS JOIN @RequiredDocumentTypes r
    JOIN DocumentType dt ON dt.[Type] = r.DbType
    WHERE a.DemoState <> N'EMPTY'
      AND (
            a.DemoState <> N'PENDING_MISSING'
            OR r.UiType IN (N'Portrait', N'IdFront')
          );

    -- 11 hồ sơ gốc: 1 Pending đủ, 8 Approved, 1 Pending thiếu, 1 Pending để test reject.
    INSERT INTO ExamRegistration
        (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
    SELECT CASE WHEN a.DemoState = N'APPROVED' THEN N'Approved' ELSE N'Pending' END,
           CASE
               WHEN a.DemoState = N'APPROVED'
                   THEN N'#PROFILE_DOC# Hồ sơ demo đã được ban quản lý phê duyệt.'
               WHEN a.DemoState = N'PENDING_MISSING'
                   THEN N'#PROFILE_DOC# Hồ sơ demo còn thiếu CCCD mặt sau và giấy khám sức khỏe.'
               WHEN a.DemoState = N'PENDING_REJECT'
                   THEN N'#PROFILE_DOC# Hồ sơ đủ tài liệu, sẵn sàng để cán bộ test thao tác từ chối.'
               ELSE N'#PROFILE_DOC# Hồ sơ đủ tài liệu đang chờ ban quản lý duyệt.'
           END,
           p.ProfileId,
           @A1LicenceId,
           CASE WHEN a.Username IN (N'demo_reg_approved_07', N'demo_reg_approved_08')
                THEN 1 ELSE 0 END
    FROM @Accounts a
    JOIN [User] u ON u.Username = a.Username
    JOIN Profile p ON p.UserId = u.UserId
    WHERE a.DemoState <> N'EMPTY';

    -- Dòng Approved thứ 9: profile 01 được duyệt thêm hạng A.
    INSERT INTO ExamRegistration
        (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
    SELECT N'Approved',
           N'#LICENCE_DOC# Xin duyệt hạng với hồ sơ đã có.',
           p.ProfileId,
           @ALicenceId,
           0
    FROM Profile p
    JOIN [User] u ON u.UserId = p.UserId
    WHERE u.Username = N'demo_reg_approved_01';

    -- Một ngày thi dự kiến A1, cách ngày seed hơn 7 ngày làm việc.
    DECLARE @PreferredExamDate DATE = '2026-08-10';
    DECLARE @ExamDateId INT;

    IF EXISTS (SELECT 1 FROM ExamDates WHERE ExamDate = @PreferredExamDate AND LicenceId <> @A1LicenceId)
        THROW 51003, N'Ngày 10/08/2026 đã được dùng cho hạng khác; không thể tạo ExamDates demo A1.', 1;

    IF NOT EXISTS (SELECT 1 FROM ExamDates WHERE ExamDate = @PreferredExamDate)
        INSERT INTO ExamDates (ExamDate, LicenceId, [Status], PoliceStatus)
        VALUES (@PreferredExamDate, @A1LicenceId, N'Open', N'NOT_SENT');
    ELSE
        UPDATE ExamDates
        SET [Status] = N'Open',
            PoliceStatus = N'NOT_SENT',
            CancelReason = NULL,
            CancelledAt = NULL,
            CancelledBy = NULL,
            CancelledRegistrationCount = NULL
        WHERE ExamDate = @PreferredExamDate;

    SELECT @ExamDateId = ExamDateId FROM ExamDates WHERE ExamDate = @PreferredExamDate;

    -- 6 RegistrationDates A1 (approved_03..08); bỏ approved_01/02 vì sẽ gắn kỳ chính thức;
    -- bỏ ER hạng A của approved_01 (không thuộc ngày dự kiến A1).
    INSERT INTO RegistrationDates
        (ExamRegistrationId, ExamDateId, IsActive, PoliceStatus, PoliceReason, OfficialCandidateNumber)
    SELECT er.ExamRegistrationId, @ExamDateId, 1, N'NOT_SENT', NULL, NULL
    FROM ExamRegistration er
    JOIN @DemoProfileIds p ON p.ProfileId = er.ProfileId
    JOIN Profile pr ON pr.ProfileId = er.ProfileId
    JOIN [User] u ON u.UserId = pr.UserId
    WHERE er.RegistrationStatus = N'Approved'
      AND er.LicenceId = @A1LicenceId
      AND u.Username NOT IN (N'demo_reg_approved_01', N'demo_reg_approved_02');

    -- Tạo hai kỳ thi chính thức.
    INSERT INTO Exam
        (ExamCode, ExamDate, StartTime, EndTime, [Status], ExamPassword,
         CentreName, LicenceId, SourceExamDateId)
    VALUES
    (N'DEMO-A1-20260728-1200', '2026-07-28', '2026-07-28T12:00:00', NULL,
     N'Chưa diễn ra', NULL, N'Trung tâm Sát hạch Lái Vui – Demo 28/07', @A1LicenceId, NULL),
    (N'DEMO-A1-20260730-0800', '2026-07-30', '2026-07-30T08:00:00', NULL,
     N'Chưa diễn ra', NULL, N'Trung tâm Sát hạch Lái Vui – Demo 30/07', @A1LicenceId, NULL);

    DECLARE @Exam28Id INT =
        (SELECT ExamId FROM Exam WHERE ExamCode = N'DEMO-A1-20260728-1200');
    DECLARE @Exam30Id INT =
        (SELECT ExamId FROM Exam WHERE ExamCode = N'DEMO-A1-20260730-0800');

    INSERT INTO ExamSection (SectionType, LicenceId, DurationMinutes, ExamId) VALUES
    (N'Lý thuyết', @A1LicenceId, 19, @Exam28Id),
    (N'Thực hành trong hình', @A1LicenceId, NULL, @Exam28Id),
    (N'Lý thuyết', @A1LicenceId, 19, @Exam30Id),
    (N'Thực hành trong hình', @A1LicenceId, NULL, @Exam30Id);

    -- Ghép đầy đủ các khu vực phù hợp; tuyệt đối chưa tạo ExaminerSchedule.
    INSERT INTO Exam_ExamArea (ExamId, ExamAreaId)
    SELECT e.ExamId, ea.ExamAreaId
    FROM Exam e
    CROSS JOIN ExamArea ea
    WHERE e.ExamId IN (@Exam28Id, @Exam30Id)
      AND ea.AreaType IN (N'Phòng thủ tục', N'Phòng thi', N'Sân thi');

    IF NOT EXISTS (
        SELECT 1 FROM Exam_ExamArea x
        JOIN ExamArea ea ON ea.ExamAreaId = x.ExamAreaId
        WHERE x.ExamId = @Exam28Id AND ea.AreaType = N'Phòng thủ tục'
    ) OR NOT EXISTS (
        SELECT 1 FROM Exam_ExamArea x
        JOIN ExamArea ea ON ea.ExamAreaId = x.ExamAreaId
        WHERE x.ExamId = @Exam28Id AND ea.AreaType = N'Phòng thi'
    ) OR NOT EXISTS (
        SELECT 1 FROM Exam_ExamArea x
        JOIN ExamArea ea ON ea.ExamAreaId = x.ExamAreaId
        WHERE x.ExamId = @Exam28Id AND ea.AreaType = N'Sân thi'
    )
        THROW 51004, N'Thiếu khu vực Phòng thủ tục/Phòng thi/Sân thi trong master data.', 1;

    DECLARE @CandidateSeed TABLE (
        ExamDay INT NOT NULL,
        SeqNo INT NOT NULL,
        CandidateNumber NVARCHAR(50) NOT NULL,
        FullName NVARCHAR(255) NOT NULL,
        GovernmentIdNumber NVARCHAR(100) NOT NULL,
        TakeTheory BIT NOT NULL,
        TakeLayout BIT NOT NULL,
        ReasonForTaking NVARCHAR(355) NOT NULL,
        PRIMARY KEY (ExamDay, SeqNo)
    );

    ;WITH n AS (
        SELECT 1 AS SeqNo
        UNION ALL
        SELECT SeqNo + 1 FROM n WHERE SeqNo < 20
    )
    INSERT INTO @CandidateSeed
        (ExamDay, SeqNo, CandidateNumber, FullName, GovernmentIdNumber,
         TakeTheory, TakeLayout, ReasonForTaking)
    SELECT d.ExamDay,
           n.SeqNo,
           -- SBD số (001–020 ngày 28, 021–040 ngày 30) để examiner/examstaff parse INT được.
           CASE d.ExamDay
               WHEN 28 THEN RIGHT(N'000' + CAST(n.SeqNo AS NVARCHAR(3)), 3)
               ELSE RIGHT(N'000' + CAST(20 + n.SeqNo AS NVARCHAR(3)), 3)
           END,
           CASE d.ExamDay
               WHEN 28 THEN N'Thí Sinh Demo 28-' + RIGHT(N'00' + CAST(n.SeqNo AS NVARCHAR(2)), 2)
               ELSE N'Thí Sinh Demo 30-' + RIGHT(N'00' + CAST(n.SeqNo AS NVARCHAR(2)), 2)
           END,
           CASE
               WHEN d.ExamDay = 28 AND n.SeqNo = 1 THEN N'079098100003'
               WHEN d.ExamDay = 30 AND n.SeqNo = 1 THEN N'079098100004'
               WHEN d.ExamDay = 28 THEN N'028726' + RIGHT(N'0000' + CAST(n.SeqNo AS NVARCHAR(4)), 4)
               ELSE N'030726' + RIGHT(N'0000' + CAST(n.SeqNo AS NVARCHAR(4)), 4)
           END,
           CASE WHEN n.SeqNo BETWEEN 8 AND 14 THEN 0 ELSE 1 END,
           CASE WHEN n.SeqNo BETWEEN 15 AND 20 THEN 0 ELSE 1 END,
           CASE
               WHEN n.SeqNo BETWEEN 8 AND 14
                   THEN N'Bảo lưu lý thuyết - chỉ thi thực hành trong hình'
               WHEN n.SeqNo BETWEEN 15 AND 20
                   THEN N'Bảo lưu thực hành - chỉ thi lý thuyết'
               ELSE N'Thi cả lý thuyết và thực hành trong hình'
           END
    FROM n
    CROSS JOIN (VALUES (28), (30)) d(ExamDay)
    OPTION (MAXRECURSION 20);

    INSERT INTO Candidate
        (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex,
         GovernmentIdNumber, [Address], TakeTheory, TakeLayout, TakeNo,
         ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended)
    SELECT cs.CandidateNumber,
           cs.FullName,
           DATEADD(DAY, cs.SeqNo, CAST('1996-01-01' AS DATE)),
           CASE cs.ExamDay
               WHEN 28 THEN N'09128' + RIGHT(N'00000' + CAST(cs.SeqNo AS NVARCHAR(5)), 5)
               ELSE N'09130' + RIGHT(N'00000' + CAST(cs.SeqNo AS NVARCHAR(5)), 5)
           END,
           NULL,
           CASE WHEN cs.SeqNo % 2 = 0 THEN 0 ELSE 1 END,
           cs.GovernmentIdNumber,
           N'Địa chỉ demo kỳ thi ngày ' + CAST(cs.ExamDay AS NVARCHAR(2)) + N'/07/2026',
           cs.TakeTheory,
           cs.TakeLayout,
           CASE WHEN cs.SeqNo <= 7 THEN 1 ELSE 2 END,
           cs.ReasonForTaking,
           NULL, -- Chưa có ảnh => chưa hoàn tất thủ tục.
           0,
           0
    FROM @CandidateSeed cs;

    -- Hai dòng lifecycle tách khỏi hồ sơ tài liệu để dashboard đọc đúng kỳ chính thức.
    DECLARE @Lifecycle28Id INT;
    DECLARE @Lifecycle30Id INT;

    INSERT INTO ExamRegistration
        (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
    SELECT N'PreRegistered',
           N'#EXAM_ID#' + CAST(@Exam28Id AS NVARCHAR(20)) + N'# Đã liên kết kỳ thi chính thức.',
           p.ProfileId, @A1LicenceId, 0
    FROM Profile p JOIN [User] u ON u.UserId = p.UserId
    WHERE u.Username = N'demo_reg_approved_01';
    SET @Lifecycle28Id = SCOPE_IDENTITY();

    INSERT INTO ExamRegistration
        (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
    SELECT N'PreRegistered',
           N'#EXAM_ID#' + CAST(@Exam30Id AS NVARCHAR(20)) + N'# Đã liên kết kỳ thi chính thức.',
           p.ProfileId, @A1LicenceId, 0
    FROM Profile p JOIN [User] u ON u.UserId = p.UserId
    WHERE u.Username = N'demo_reg_approved_02';
    SET @Lifecycle30Id = SCOPE_IDENTITY();

    -- Vô hiệu hóa RegistrationDates cũ (nếu còn) của 2 profile đã gắn kỳ chính thức.
    UPDATE rd
    SET rd.IsActive = 0
    FROM RegistrationDates rd
    JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
    JOIN Profile p ON p.ProfileId = er.ProfileId
    JOIN [User] u ON u.UserId = p.UserId
    WHERE rd.ExamDateId = @ExamDateId
      AND u.Username IN (N'demo_reg_approved_01', N'demo_reg_approved_02')
      AND er.RegistrationStatus <> N'PreRegistered';

    -- Vô hiệu hóa dòng RegistrationDates lệch hạng với ngày dự kiến.
    UPDATE rd
    SET rd.IsActive = 0
    FROM RegistrationDates rd
    JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
    JOIN ExamDates ed ON ed.ExamDateId = rd.ExamDateId
    WHERE rd.ExamDateId = @ExamDateId
      AND rd.IsActive = 1
      AND er.LicenceId <> ed.LicenceId;

    INSERT INTO ExamEnrollment
        (CandidateId, ExamId, ExamRegistrationId, AllocatedExamAreaId, ExamDeviceId)
    SELECT c.CandidateId,
           CASE WHEN cs.ExamDay = 28 THEN @Exam28Id ELSE @Exam30Id END,
           CASE
               WHEN cs.ExamDay = 28 AND cs.SeqNo = 1 THEN @Lifecycle28Id
               WHEN cs.ExamDay = 30 AND cs.SeqNo = 1 THEN @Lifecycle30Id
               ELSE NULL
           END,
           NULL,
           NULL
    FROM @CandidateSeed cs
    JOIN Candidate c ON c.CandidateNumber = cs.CandidateNumber;

    -- Chỉ tạo phần thi mà thí sinh thực sự phải thi; chưa phân phòng/thiết bị/check-in.
    INSERT INTO ExamEnrollmentSection
        (ExamEnrollmentId, ExamSectionId, ExamAreaId, ExamDeviceId, [Status],
         AllocatedAt, AllocatedBy, CheckedInAt, CheckedInBy,
         StartedAt, CompletedAt, ResultPrintedAt)
    SELECT ee.ExamEnrollmentId,
           es.ExamSectionId,
           NULL,
           NULL,
           N'Chưa thi',
           NULL, NULL, NULL, NULL, NULL, NULL, NULL
    FROM ExamEnrollment ee
    JOIN Candidate c ON c.CandidateId = ee.CandidateId
    JOIN ExamSection es ON es.ExamId = ee.ExamId
    WHERE ee.ExamId IN (@Exam28Id, @Exam30Id)
      AND (
            (es.SectionType = N'Lý thuyết' AND c.TakeTheory = 1)
         OR (es.SectionType = N'Thực hành trong hình' AND c.TakeLayout = 1)
          );

    -- Bảo đảm yêu cầu "chưa phân sát hạch viên" kể cả khi script được chỉnh/chạy lại.
    DELETE FROM ExaminerSchedule WHERE ExamId IN (@Exam28Id, @Exam30Id);

    COMMIT TRANSACTION;

    PRINT N'SEED_REGISTRANT_DEMO.sql hoàn tất.';
    PRINT N'12 tài khoản Registrant - mật khẩu chung: login123';
    PRINT N'2 kỳ thi, mỗi kỳ 20 thí sinh; chưa ảnh, chưa payment, chưa check-in, chưa phân SHV.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Chuẩn hóa SBD demo cũ dạng D28-/D30- sang số (001–040) nếu còn trong DB.
UPDATE Candidate
SET CandidateNumber = CASE
    WHEN CandidateNumber LIKE N'D28-%'
        THEN RIGHT(N'000' + CAST(TRY_CAST(RIGHT(CandidateNumber, 2) AS INT) AS NVARCHAR(3)), 3)
    WHEN CandidateNumber LIKE N'D30-%'
        THEN RIGHT(N'000' + CAST(20 + TRY_CAST(RIGHT(CandidateNumber, 2) AS INT) AS NVARCHAR(3)), 3)
    ELSE CandidateNumber
END
WHERE CandidateNumber LIKE N'D28-%' OR CandidateNumber LIKE N'D30-%';
GO

-- =============================================================================
-- KIỂM CHỨNG NHANH
-- =============================================================================
SELECT u.Username, u.Email, u.IsActive, p.ProfileId, p.GovernmentIdNumber
FROM [User] u
JOIN [Role] r ON r.RoleId = u.RoleId
LEFT JOIN Profile p ON p.UserId = u.UserId
WHERE u.Username LIKE N'demo_reg_%'
ORDER BY u.Username;

SELECT
    COUNT(*) AS DemoRegistrantCount,
    SUM(CASE WHEN u.Username = N'demo_reg_empty' THEN 1 ELSE 0 END) AS EmptyAccountCount
FROM [User] u
WHERE u.Username LIKE N'demo_reg_%';

SELECT
    SUM(CASE WHEN d.ProfileId IS NOT NULL THEN 1 ELSE 0 END) AS DocumentCount,
    COUNT(DISTINCT CASE WHEN d.ProfileId IS NOT NULL THEN p.ProfileId END) AS ProfilesWithDocuments
FROM Profile p
JOIN [User] u ON u.UserId = p.UserId
LEFT JOIN Document d ON d.ProfileId = p.ProfileId
WHERE u.Username LIKE N'demo_reg_%';

SELECT er.RegistrationStatus, COUNT(*) AS Total
FROM ExamRegistration er
JOIN Profile p ON p.ProfileId = er.ProfileId
JOIN [User] u ON u.UserId = p.UserId
WHERE u.Username LIKE N'demo_reg_%'
GROUP BY er.RegistrationStatus
ORDER BY er.RegistrationStatus;

SELECT e.ExamCode, e.StartTime, e.[Status],
       COUNT(DISTINCT ee.ExamEnrollmentId) AS CandidateCount,
       SUM(CASE WHEN c.TakeTheory = 1 AND c.TakeLayout = 1 THEN 1 ELSE 0 END) AS BothSections,
       SUM(CASE WHEN c.TakeTheory = 0 AND c.TakeLayout = 1 THEN 1 ELSE 0 END) AS TheoryExempt,
       SUM(CASE WHEN c.TakeTheory = 1 AND c.TakeLayout = 0 THEN 1 ELSE 0 END) AS LayoutExempt,
       SUM(CASE WHEN c.PhotoImageUrl IS NOT NULL THEN 1 ELSE 0 END) AS HasPhoto,
       SUM(CASE WHEN pay.PaymentId IS NOT NULL THEN 1 ELSE 0 END) AS HasPayment
FROM Exam e
JOIN ExamEnrollment ee ON ee.ExamId = e.ExamId
JOIN Candidate c ON c.CandidateId = ee.CandidateId
LEFT JOIN Payment pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
WHERE e.ExamCode IN (N'DEMO-A1-20260728-1200', N'DEMO-A1-20260730-0800')
GROUP BY e.ExamCode, e.StartTime, e.[Status]
ORDER BY e.StartTime;

SELECT e.ExamCode,
       COUNT(DISTINCT x.ExamAreaId) AS LinkedAreaCount,
       COUNT(DISTINCT esch.ExaminerScheduleId) AS ExaminerAssignmentCount
FROM Exam e
LEFT JOIN Exam_ExamArea x ON x.ExamId = e.ExamId
LEFT JOIN ExaminerSchedule esch ON esch.ExamId = e.ExamId
WHERE e.ExamCode IN (N'DEMO-A1-20260728-1200', N'DEMO-A1-20260730-0800')
GROUP BY e.ExamCode
ORDER BY e.ExamCode;
GO
