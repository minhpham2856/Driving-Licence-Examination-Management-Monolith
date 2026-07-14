-- ============================================================================
-- Du lieu demo DAY DU cho luong nghiep vu A1 / A / B1
-- Target schema: DLEM_DB_2 dang duoc ung dung su dung (User.Role,
-- Candidate.ExamRegistrationId, Exam_Candidate, Session_Examiner).
--
-- Tai khoan demo dung chung mat khau: Demo@123
-- Script idempotent: co the chay lai ma khong nhan doi bo du lieu demo.
-- Phai chay MIGRATION_SCOPE_A1_A_B1.sql truoc script nay.
-- ============================================================================

USE DLEM_DB_2;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.Exam_Candidate', N'U') IS NULL
        THROW 51000, N'Schema hien tai khong co Exam_Candidate. Hay dung dung DLEM_DB_2 cua ung dung.', 1;

    IF (SELECT COUNT(*) FROM dbo.Licence WHERE LicenceClass IN (N'A1', N'A', N'B1')) <> 3
       OR EXISTS (SELECT 1 FROM dbo.Licence WHERE LicenceClass NOT IN (N'A1', N'A', N'B1'))
        THROW 51001, N'Hay chay MIGRATION_SCOPE_A1_A_B1.sql truoc khi seed du lieu day du.', 1;

    -- AuthService hien tai so sanh mat khau dang plain text.
    DECLARE @PasswordHash NVARCHAR(255) = N'Demo@123';

    -- Sua du lieu cu bi mo coi: Profile van con nhung UserId da bi xoa.
    -- Giu nguyen UserId de khong lam mat lien ket ho so/tai lieu da nop.
    IF EXISTS (
        SELECT 1 FROM dbo.Profile p
        LEFT JOIN dbo.[User] u ON u.UserId = p.UserId
        WHERE u.UserId IS NULL
    )
    BEGIN
        SET IDENTITY_INSERT dbo.[User] ON;
        INSERT INTO dbo.[User] (UserId, Username, Email, PasswordHash, [Role], [Status])
        SELECT p.UserId,
               CONCAT(N'recovered.', p.UserId),
               CONCAT(N'recovered.', p.UserId, N'@laivui.local'),
               @PasswordHash, N'Registrant', 1
        FROM dbo.Profile p
        LEFT JOIN dbo.[User] u ON u.UserId = p.UserId
        WHERE u.UserId IS NULL;
        SET IDENTITY_INSERT dbo.[User] OFF;
    END;

    -- ------------------------------------------------------------------------
    -- 1. Tai khoan nhan su de dang nhap va thao tac tung buoc trong quy trinh.
    -- ------------------------------------------------------------------------
    DECLARE @Staff TABLE (
        Username NVARCHAR(100), Email NVARCHAR(255), [Role] NVARCHAR(50),
        FullName NVARCHAR(255), DateOfBirth DATE, PhoneNumber NVARCHAR(20),
        Sex NVARCHAR(10), GovernmentIdNumber NVARCHAR(100), [Address] NVARCHAR(500)
    );

    INSERT INTO @Staff VALUES
        (N'demo.manager',   N'demo.manager@laivui.local',   N'ManagingStaff', N'Nguyễn Minh Quản', '1990-02-15', N'0909900001', N'Nam', N'079900000001', N'Trung tâm sát hạch Lái Vui'),
        (N'demo.examstaff', N'demo.examstaff@laivui.local', N'ExamStaff',     N'Trần Thu Điều',    '1992-06-20', N'0909900002', N'Nữ',  N'079900000002', N'Trung tâm sát hạch Lái Vui'),
        (N'demo.examiner1', N'demo.examiner1@laivui.local', N'Examiner',      N'Lê Văn Sát',       '1988-03-12', N'0909900003', N'Nam', N'079900000003', N'Phòng Cảnh sát giao thông'),
        (N'demo.examiner2', N'demo.examiner2@laivui.local', N'Examiner',      N'Phạm Mai Hạch',    '1989-09-22', N'0909900004', N'Nữ', N'079900000004', N'Phòng Cảnh sát giao thông');

    INSERT INTO dbo.[User] (Username, Email, PasswordHash, [Role], [Status])
    SELECT s.Username, s.Email, @PasswordHash, s.[Role], 1
    FROM @Staff s
    WHERE NOT EXISTS (SELECT 1 FROM dbo.[User] u WHERE u.Email = s.Email);

    UPDATE dbo.[User]
    SET PasswordHash = @PasswordHash
    WHERE Email LIKE N'demo.%@laivui.local';

    INSERT INTO dbo.Profile
        (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, [Address], UserId)
    SELECT s.FullName, s.DateOfBirth, s.PhoneNumber, s.Sex,
           s.GovernmentIdNumber, s.[Address], u.UserId
    FROM @Staff s
    JOIN dbo.[User] u ON u.Email = s.Email
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Profile p WHERE p.UserId = u.UserId);

    DECLARE @ManagerId INT = (SELECT UserId FROM dbo.[User] WHERE Email = N'demo.manager@laivui.local');
    DECLARE @Examiner1Id INT = (SELECT UserId FROM dbo.[User] WHERE Email = N'demo.examiner1@laivui.local');
    DECLARE @Examiner2Id INT = (SELECT UserId FROM dbo.[User] WHERE Email = N'demo.examiner2@laivui.local');

    -- ------------------------------------------------------------------------
    -- 2. Moi hang co ho so Cho duyet / Can bo sung / Da duyet / Dang thi.
    --    Ba ho so Dang thi se di tiep qua Candidate, thanh toan va cham thi.
    -- ------------------------------------------------------------------------
    DECLARE @Applicants TABLE (
        Username NVARCHAR(100), Email NVARCHAR(255), FullName NVARCHAR(255),
        DateOfBirth DATE, PhoneNumber NVARCHAR(20), Sex NVARCHAR(10),
        GovernmentIdNumber NVARCHAR(100), [Address] NVARCHAR(500),
        LicenceClass NVARCHAR(10), RegistrationStatus NVARCHAR(50),
        Notes NVARCHAR(500), DocumentSet NVARCHAR(20),
        CandidateNumber NVARCHAR(50) NULL, CandidateState NVARCHAR(30) NULL
    );

    INSERT INTO @Applicants VALUES
        (N'demo.a1.pending',    N'demo.a1.pending@laivui.local',    N'Nguyễn An A1',       '2001-01-11', N'0909000101', N'Nam', N'079900000101', N'Hà Nội', N'A1', N'Pending',        N'SOURCE=PORTAL;APPLICANT_TYPE=INDEPENDENT; Hồ sơ chờ Managing Staff duyệt', N'FULL', NULL, NULL),
        (N'demo.a1.supplement', N'demo.a1.supplement@laivui.local', N'Trần Bình A1',        '2000-02-12', N'0909000102', N'Nam', N'079900000102', N'Hà Nội', N'A1', N'NeedSupplement', N'SOURCE=PORTAL; Thiếu giấy khám sức khỏe', N'MISSING_HEALTH', NULL, NULL),
        (N'demo.a1.approved',   N'demo.a1.approved@laivui.local',   N'Lê Chi A1',           '1999-03-13', N'0909000103', N'Nữ',  N'079900000103', N'Hà Nội', N'A1', N'Approved',       N'SOURCE=PORTAL; Hồ sơ đã duyệt, chờ xếp kỳ thi', N'FULL', NULL, NULL),
        (N'demo.a1.present',    N'demo.a1.present@laivui.local',    N'Phạm Dũng A1',        '1998-04-14', N'0909000104', N'Nam', N'079900000104', N'Hà Nội', N'A1', N'Present',        N'SOURCE=PORTAL; Đã xếp kỳ thi và điểm danh', N'FULL', N'901', N'PARTIAL'),

        (N'demo.a.pending',     N'demo.a.pending@laivui.local',     N'Hoàng Anh Hạng A',    '2001-05-15', N'0909000105', N'Nam', N'079900000105', N'Hải Phòng', N'A', N'Pending',        N'SOURCE=PORTAL;APPLICANT_TYPE=INDEPENDENT; Hồ sơ chờ Managing Staff duyệt', N'FULL', NULL, NULL),
        (N'demo.a.supplement',  N'demo.a.supplement@laivui.local',  N'Vũ Bích Hạng A',      '2000-06-16', N'0909000106', N'Nữ',  N'079900000106', N'Hải Phòng', N'A', N'NeedSupplement', N'SOURCE=PORTAL; Thiếu giấy khám sức khỏe', N'MISSING_HEALTH', NULL, NULL),
        (N'demo.a.approved',    N'demo.a.approved@laivui.local',    N'Đặng Cường Hạng A',   '1999-07-17', N'0909000107', N'Nam', N'079900000107', N'Hải Phòng', N'A', N'Approved',       N'SOURCE=PORTAL; Hồ sơ đã duyệt, chờ xếp kỳ thi', N'FULL', NULL, NULL),
        (N'demo.a.present',     N'demo.a.present@laivui.local',     N'Đỗ Diệp Hạng A',      '1998-08-18', N'0909000108', N'Nữ',  N'079900000108', N'Hải Phòng', N'A', N'Present',        N'SOURCE=PORTAL; Đang làm bài lý thuyết', N'FULL', N'902', N'TESTING'),

        (N'demo.b1.pending',    N'demo.b1.pending@laivui.local',    N'Bùi An Hạng B1',      '2001-09-19', N'0909000109', N'Nam', N'079900000109', N'Bắc Ninh', N'B1', N'Pending',        N'SOURCE=PORTAL;APPLICANT_TYPE=INDEPENDENT; Hồ sơ chờ Managing Staff duyệt', N'FULL', NULL, NULL),
        (N'demo.b1.supplement', N'demo.b1.supplement@laivui.local', N'Ngô Bích Hạng B1',    '2000-10-20', N'0909000110', N'Nữ',  N'079900000110', N'Bắc Ninh', N'B1', N'NeedSupplement', N'SOURCE=PORTAL; Thiếu giấy khám sức khỏe', N'MISSING_HEALTH', NULL, NULL),
        (N'demo.b1.approved',   N'demo.b1.approved@laivui.local',   N'Dương Cường Hạng B1', '1999-11-21', N'0909000111', N'Nam', N'079900000111', N'Bắc Ninh', N'B1', N'Approved',       N'SOURCE=PORTAL; Hồ sơ đã duyệt, chờ xếp kỳ thi', N'FULL', NULL, NULL),
        (N'demo.b1.present',    N'demo.b1.present@laivui.local',    N'Lý Diễm Hạng B1',     '1998-12-22', N'0909000112', N'Nữ',  N'079900000112', N'Bắc Ninh', N'B1', N'Present',        N'SOURCE=PORTAL; Thực hành đã chấm, chờ ký xác nhận', N'FULL', N'903', N'AWAITING_SIGNATURE');

    INSERT INTO dbo.[User] (Username, Email, PasswordHash, [Role], [Status])
    SELECT a.Username, a.Email, @PasswordHash, N'Registrant', 1
    FROM @Applicants a
    WHERE NOT EXISTS (SELECT 1 FROM dbo.[User] u WHERE u.Email = a.Email);

    INSERT INTO dbo.Profile
        (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, [Address], UserId)
    SELECT a.FullName, a.DateOfBirth, a.PhoneNumber, a.Sex,
           a.GovernmentIdNumber, a.[Address], u.UserId
    FROM @Applicants a
    JOIN dbo.[User] u ON u.Email = a.Email
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Profile p WHERE p.UserId = u.UserId);

    INSERT INTO dbo.ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId)
    SELECT a.RegistrationStatus, a.Notes, p.ProfileId, l.LicenceId
    FROM @Applicants a
    JOIN dbo.[User] u ON u.Email = a.Email
    JOIN dbo.Profile p ON p.UserId = u.UserId
    JOIN dbo.Licence l ON l.LicenceClass = a.LicenceClass
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.ExamRegistration er
        WHERE er.ProfileId = p.ProfileId AND er.LicenceId = l.LicenceId
    );

    DECLARE @PortraitUrl NVARCHAR(500) = N'/assets/images/demo-documents/portrait.svg';
    DECLARE @IdFrontUrl NVARCHAR(500) = N'/assets/images/demo-documents/id-front.svg';
    DECLARE @IdBackUrl NVARCHAR(500) = N'/assets/images/demo-documents/id-back.svg';
    DECLARE @HealthUrl NVARCHAR(500) = N'/assets/images/demo-documents/health-certificate.svg';

    INSERT INTO dbo.Document (DocumentType, DocumentUrl, Notes, ProfileId)
    SELECT d.DocumentType, d.DocumentUrl, N'Dữ liệu demo đã tải lên', p.ProfileId
    FROM @Applicants a
    JOIN dbo.[User] u ON u.Email = a.Email
    JOIN dbo.Profile p ON p.UserId = u.UserId
    CROSS APPLY (VALUES
        (N'PORTRAIT', @PortraitUrl),
        (N'ID_FRONT', @IdFrontUrl),
        (N'ID_BACK', @IdBackUrl),
        (N'HEALTH_CERTIFICATE', @HealthUrl)
    ) d(DocumentType, DocumentUrl)
    WHERE (a.DocumentSet <> N'MISSING_HEALTH' OR d.DocumentType <> N'HEALTH_CERTIFICATE')
      AND NOT EXISTS (
          SELECT 1 FROM dbo.Document oldDoc
          WHERE oldDoc.ProfileId = p.ProfileId AND oldDoc.DocumentType = d.DocumentType
      );

    -- ------------------------------------------------------------------------
    -- 3. Tai nguyen thi: thiet bi, le phi va trang thai cac ca demo.
    -- ------------------------------------------------------------------------
    DECLARE @TheoryAreaId INT = (SELECT TOP 1 ea.ExamAreaId
        FROM dbo.ExamArea ea WHERE ea.AreaName = N'Phòng lý thuyết mô tô 1');
    IF @TheoryAreaId IS NULL THROW 51002, N'Khong tim thay khu vuc ly thuyet demo.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.ExamDevice WHERE DeviceName = N'DEMO-PC-01')
        INSERT INTO dbo.ExamDevice (DeviceName, DeviceType, [Status], ExamAreaId)
        VALUES (N'DEMO-PC-01', N'Computer', N'Available', @TheoryAreaId);
    IF NOT EXISTS (SELECT 1 FROM dbo.ExamDevice WHERE DeviceName = N'DEMO-PC-02')
        INSERT INTO dbo.ExamDevice (DeviceName, DeviceType, [Status], ExamAreaId)
        VALUES (N'DEMO-PC-02', N'Computer', N'InUse', @TheoryAreaId);
    IF NOT EXISTS (SELECT 1 FROM dbo.ExamDevice WHERE DeviceName = N'DEMO-PC-03')
        INSERT INTO dbo.ExamDevice (DeviceName, DeviceType, [Status], ExamAreaId)
        VALUES (N'DEMO-PC-03', N'Computer', N'Available', @TheoryAreaId);

    IF NOT EXISTS (SELECT 1 FROM dbo.Fee WHERE FeeName = N'Lệ phí sát hạch lý thuyết A1/A/B1')
        INSERT INTO dbo.Fee (FeeName, FeeType, Amount, IsActive)
        VALUES (N'Lệ phí sát hạch lý thuyết A1/A/B1', N'Exam', 100000, 1);
    IF NOT EXISTS (SELECT 1 FROM dbo.Fee WHERE FeeName = N'Lệ phí sát hạch thực hành A1/A/B1')
        INSERT INTO dbo.Fee (FeeName, FeeType, Amount, IsActive)
        VALUES (N'Lệ phí sát hạch thực hành A1/A/B1', N'Exam', 250000, 1);

    UPDATE dbo.Exam
    SET [Status] = N'InProgress'
    WHERE ExamCode IN (N'EX-A1-20260815', N'EX-A-20260816', N'EX-B1-20260822');

    UPDATE dbo.[Session]
    SET [Status] = CASE SessionName
        WHEN N'Ca lý thuyết A1' THEN N'Completed'
        WHEN N'Ca thực hành A1' THEN N'InProgress'
        WHEN N'Ca lý thuyết A' THEN N'InProgress'
        WHEN N'Ca thực hành A' THEN N'Scheduled'
        WHEN N'Ca lý thuyết B1' THEN N'Completed'
        WHEN N'Ca thực hành B1' THEN N'InProgress'
        ELSE [Status]
    END
    WHERE ExamId IN (SELECT ExamId FROM dbo.Exam WHERE ExamCode IN
        (N'EX-A1-20260815', N'EX-A-20260816', N'EX-B1-20260822'));

    -- ------------------------------------------------------------------------
    -- 4. Chuyen ba ho so Present thanh Candidate va dua vao ca thi.
    -- ------------------------------------------------------------------------
    INSERT INTO dbo.Candidate
        (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex,
         GovernmentIdNumber, [Address], TakeTheory, TakePractical,
         TakeRoadLayout, TakeOnRoad, ReasonForTaking, PhotoImageUrl,
         IsAbsent, IsSuspended, UserId, ExamRegistrationId)
    SELECT a.CandidateNumber, p.FullName, p.DateOfBirth, p.PhoneNumber, p.Sex,
           p.GovernmentIdNumber, p.[Address], 1, 1, 0, 0,
           N'Thi cấp giấy phép lái xe lần đầu', @PortraitUrl,
           0, 0, u.UserId, er.ExamRegistrationId
    FROM @Applicants a
    JOIN dbo.[User] u ON u.Email = a.Email
    JOIN dbo.Profile p ON p.UserId = u.UserId
    JOIN dbo.Licence l ON l.LicenceClass = a.LicenceClass
    JOIN dbo.ExamRegistration er ON er.ProfileId = p.ProfileId AND er.LicenceId = l.LicenceId
    WHERE a.CandidateNumber IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM dbo.Candidate c WHERE c.GovernmentIdNumber = p.GovernmentIdNumber);

    INSERT INTO dbo.Exam_Candidate
        (ExamId, CandidateId, SessionId, SectionStatus, SignaturePrinted)
    SELECT e.ExamId, c.CandidateId, s.SessionId,
           CASE
             WHEN a.CandidateState = N'TESTING' AND sec.SectionName = N'Lý thuyết' THEN N'Testing'
             WHEN a.CandidateState = N'PARTIAL' AND sec.SectionName = N'Lý thuyết' THEN N'Done'
             WHEN a.CandidateState = N'AWAITING_SIGNATURE' AND sec.SectionName = N'Lý thuyết' THEN N'Done'
             WHEN a.CandidateState = N'AWAITING_SIGNATURE' AND sec.SectionName = N'Thực hành' THEN N'AwaitingSignature'
             ELSE N'Pending'
           END,
           CASE WHEN sec.SectionName = N'Lý thuyết'
                     AND a.CandidateState IN (N'PARTIAL', N'AWAITING_SIGNATURE') THEN 1 ELSE 0 END
    FROM @Applicants a
    JOIN dbo.[User] u ON u.Email = a.Email
    JOIN dbo.Candidate c ON c.UserId = u.UserId
    JOIN dbo.Licence l ON l.LicenceClass = a.LicenceClass
    JOIN dbo.Exam e ON e.LicenceId = l.LicenceId
       AND e.ExamCode IN (N'EX-A1-20260815', N'EX-A-20260816', N'EX-B1-20260822')
    JOIN dbo.[Session] s ON s.ExamId = e.ExamId
    JOIN dbo.Session_ExamSection ses ON ses.SessionId = s.SessionId
    JOIN dbo.ExamSection sec ON sec.ExamSectionId = ses.ExamSectionId
    WHERE a.CandidateNumber IS NOT NULL
      AND sec.SectionName IN (N'Lý thuyết', N'Thực hành')
      AND NOT EXISTS (
          SELECT 1 FROM dbo.Exam_Candidate ec
          WHERE ec.ExamId = e.ExamId AND ec.CandidateId = c.CandidateId AND ec.SessionId = s.SessionId
      );

    -- ------------------------------------------------------------------------
    -- 5. Phan cong sat hach vien vao tat ca ca thi va tao mapping phong thi.
    -- ------------------------------------------------------------------------
    ;WITH DemoSessions AS (
        SELECT s.SessionId, s.ExamId, ses.ExamSectionId, sea.ExamAreaId,
               ROW_NUMBER() OVER (ORDER BY s.SessionId) AS rn
        FROM dbo.[Session] s
        JOIN dbo.Exam e ON e.ExamId = s.ExamId
        JOIN dbo.Session_ExamSection ses ON ses.SessionId = s.SessionId
        JOIN dbo.Session_ExamArea sea ON sea.SessionId = s.SessionId
        WHERE e.ExamCode IN (N'EX-A1-20260815', N'EX-A-20260816', N'EX-B1-20260822')
    )
    INSERT INTO dbo.Session_Examiner
        (SessionId, ExaminerId, ExamId, ExamSectionId, ExamAreaId, AssignedBy, AssignedAt)
    SELECT ds.SessionId,
           CASE WHEN ds.rn % 2 = 1 THEN @Examiner1Id ELSE @Examiner2Id END,
           ds.ExamId, ds.ExamSectionId, ds.ExamAreaId, @ManagerId, GETDATE()
    FROM DemoSessions ds
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.Session_Examiner se WHERE se.SessionId = ds.SessionId
    );

    INSERT INTO dbo.Audit
        (UserId, [Action], [Reason], EntityName, EntityId, OldValue, NewValue, Details, CreatedAt)
    SELECT @ManagerId, N'ASSIGN', N'Phân công dữ liệu demo', N'Session_ExaminerArea',
           CONCAT(se.SessionId, N':', se.ExamAreaId, N':', se.ExaminerId),
           NULL, N'Assigned', N'Phân công sát hạch viên và khu vực thi cho luồng demo', GETDATE()
    FROM dbo.Session_Examiner se
    JOIN dbo.Exam e ON e.ExamId = se.ExamId
    WHERE e.ExamCode IN (N'EX-A1-20260815', N'EX-A-20260816', N'EX-B1-20260822')
      AND NOT EXISTS (
          SELECT 1 FROM dbo.Audit au
          WHERE au.EntityName = N'Session_ExaminerArea'
            AND au.EntityId = CONCAT(se.SessionId, N':', se.ExamAreaId, N':', se.ExaminerId)
      );

    -- ------------------------------------------------------------------------
    -- 6. Thanh toan va chi tiet le phi cho ba Candidate demo.
    -- ------------------------------------------------------------------------
    INSERT INTO dbo.Payment
        (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, CandidateId, ExamId)
    SELECT CASE a.CandidateState WHEN N'TESTING' THEN N'Pending' ELSE N'Completed' END,
           CASE a.CandidateState WHEN N'TESTING' THEN N'BankTransfer' ELSE N'Cash' END,
           CONCAT(N'DEMO-PAY-', a.CandidateNumber), 350000,
           CASE a.CandidateState WHEN N'TESTING' THEN NULL ELSE GETDATE() END,
           c.CandidateId, e.ExamId
    FROM @Applicants a
    JOIN dbo.[User] u ON u.Email = a.Email
    JOIN dbo.Candidate c ON c.UserId = u.UserId
    JOIN dbo.Licence l ON l.LicenceClass = a.LicenceClass
    JOIN dbo.Exam e ON e.LicenceId = l.LicenceId
       AND e.ExamCode IN (N'EX-A1-20260815', N'EX-A-20260816', N'EX-B1-20260822')
    WHERE a.CandidateNumber IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM dbo.Payment p WHERE p.TransactionReference = CONCAT(N'DEMO-PAY-', a.CandidateNumber));

    INSERT INTO dbo.Payment_Fee (PaymentId, FeeId)
    SELECT p.PaymentId, f.FeeId
    FROM dbo.Payment p
    CROSS JOIN dbo.Fee f
    WHERE p.TransactionReference IN (N'DEMO-PAY-901', N'DEMO-PAY-902', N'DEMO-PAY-903')
      AND f.FeeName IN (N'Lệ phí sát hạch lý thuyết A1/A/B1', N'Lệ phí sát hạch thực hành A1/A/B1')
      AND NOT EXISTS (
          SELECT 1 FROM dbo.Payment_Fee pf WHERE pf.PaymentId = p.PaymentId AND pf.FeeId = f.FeeId
      );

    -- ------------------------------------------------------------------------
    -- 7. Bai thi ly thuyet, dap an, ket qua va diem thuc hanh mau.
    -- ------------------------------------------------------------------------
    INSERT INTO dbo.TheoryPaper (ExamCandidateId, ExamDeviceId, StartedAt, SubmittedAt)
    SELECT ec.ExamCandidateId,
           CASE l.LicenceClass
             WHEN N'A1' THEN (SELECT ExamDeviceId FROM dbo.ExamDevice WHERE DeviceName = N'DEMO-PC-01')
             WHEN N'A'  THEN (SELECT ExamDeviceId FROM dbo.ExamDevice WHERE DeviceName = N'DEMO-PC-02')
             ELSE            (SELECT ExamDeviceId FROM dbo.ExamDevice WHERE DeviceName = N'DEMO-PC-03')
           END,
           DATEADD(MINUTE, -20, GETDATE()),
           CASE a.CandidateState WHEN N'TESTING' THEN NULL ELSE GETDATE() END
    FROM @Applicants a
    JOIN dbo.[User] u ON u.Email = a.Email
    JOIN dbo.Candidate c ON c.UserId = u.UserId
    JOIN dbo.ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
    JOIN dbo.Licence l ON l.LicenceId = er.LicenceId
    JOIN dbo.Exam_Candidate ec ON ec.CandidateId = c.CandidateId
    JOIN dbo.Session_ExamSection ses ON ses.SessionId = ec.SessionId
    JOIN dbo.ExamSection sec ON sec.ExamSectionId = ses.ExamSectionId AND sec.SectionName = N'Lý thuyết'
    WHERE a.CandidateNumber IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM dbo.TheoryPaper tp WHERE tp.ExamCandidateId = ec.ExamCandidateId);

    ;WITH PaperScope AS (
        SELECT tp.TheoryPaperId, a.CandidateState, l.LicenceId
        FROM @Applicants a
        JOIN dbo.[User] u ON u.Email = a.Email
        JOIN dbo.Candidate c ON c.UserId = u.UserId
        JOIN dbo.ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
        JOIN dbo.Licence l ON l.LicenceId = er.LicenceId
        JOIN dbo.Exam_Candidate ec ON ec.CandidateId = c.CandidateId
        JOIN dbo.TheoryPaper tp ON tp.ExamCandidateId = ec.ExamCandidateId
    ), RankedQuestions AS (
        SELECT ps.TheoryPaperId, ps.CandidateState, q.QuestionId, q.CorrectAnswer,
               ROW_NUMBER() OVER (PARTITION BY ps.TheoryPaperId ORDER BY q.QuestionNumber, q.QuestionId) AS rn
        FROM PaperScope ps
        JOIN dbo.Licence_Question lq ON lq.LicenceId = ps.LicenceId
        JOIN dbo.Question q ON q.QuestionId = lq.QuestionId
    )
    INSERT INTO dbo.CandidateAnswer (TheoryPaperId, QuestionId, Answer)
    SELECT rq.TheoryPaperId, rq.QuestionId,
           CASE WHEN rq.CandidateState <> N'TESTING' AND rq.rn IN (4, 17)
                THEN CASE rq.CorrectAnswer WHEN N'A' THEN N'B' WHEN N'B' THEN N'C' WHEN N'C' THEN N'D' ELSE N'A' END
                ELSE rq.CorrectAnswer END
    FROM RankedQuestions rq
    WHERE rq.rn <= CASE WHEN rq.CandidateState = N'TESTING' THEN 10 ELSE 25 END
      AND NOT EXISTS (
          SELECT 1 FROM dbo.CandidateAnswer ca
          WHERE ca.TheoryPaperId = rq.TheoryPaperId AND ca.QuestionId = rq.QuestionId
      );

    -- Ket qua ly thuyet cho A1 va B1 (23/25); A dang thi nen chua co ket qua.
    INSERT INTO dbo.ExamResult (ExamCandidateId, IsPassed, ResultDate)
    SELECT ec.ExamCandidateId, 1, GETDATE()
    FROM @Applicants a
    JOIN dbo.[User] u ON u.Email = a.Email
    JOIN dbo.Candidate c ON c.UserId = u.UserId
    JOIN dbo.Exam_Candidate ec ON ec.CandidateId = c.CandidateId
    JOIN dbo.Session_ExamSection ses ON ses.SessionId = ec.SessionId
    JOIN dbo.ExamSection sec ON sec.ExamSectionId = ses.ExamSectionId AND sec.SectionName = N'Lý thuyết'
    WHERE a.CandidateState IN (N'PARTIAL', N'AWAITING_SIGNATURE')
      AND NOT EXISTS (SELECT 1 FROM dbo.ExamResult er WHERE er.ExamCandidateId = ec.ExamCandidateId);

    INSERT INTO dbo.ExamScore (ExamResultId, ExamSectionId, Score)
    SELECT er.ExamResultId, sec.ExamSectionId, 23
    FROM dbo.ExamResult er
    JOIN dbo.Exam_Candidate ec ON ec.ExamCandidateId = er.ExamCandidateId
    JOIN dbo.Candidate c ON c.CandidateId = ec.CandidateId AND c.CandidateNumber IN (N'901', N'903')
    JOIN dbo.Session_ExamSection ses ON ses.SessionId = ec.SessionId
    JOIN dbo.ExamSection sec ON sec.ExamSectionId = ses.ExamSectionId AND sec.SectionName = N'Lý thuyết'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.ExamScore oldScore
        WHERE oldScore.ExamResultId = er.ExamResultId AND oldScore.ExamSectionId = sec.ExamSectionId);

    -- B1 da thi thuc hanh, dat 85 diem va dang cho ky xac nhan.
    INSERT INTO dbo.ExamResult (ExamCandidateId, IsPassed, ResultDate)
    SELECT ec.ExamCandidateId, 1, GETDATE()
    FROM dbo.Candidate c
    JOIN dbo.Exam_Candidate ec ON ec.CandidateId = c.CandidateId
    JOIN dbo.Session_ExamSection ses ON ses.SessionId = ec.SessionId
    JOIN dbo.ExamSection sec ON sec.ExamSectionId = ses.ExamSectionId AND sec.SectionName = N'Thực hành'
    WHERE c.CandidateNumber = N'903'
      AND NOT EXISTS (SELECT 1 FROM dbo.ExamResult er WHERE er.ExamCandidateId = ec.ExamCandidateId);

    INSERT INTO dbo.ExamScore (ExamResultId, ExamSectionId, Score)
    SELECT er.ExamResultId, sec.ExamSectionId, 85
    FROM dbo.ExamResult er
    JOIN dbo.Exam_Candidate ec ON ec.ExamCandidateId = er.ExamCandidateId
    JOIN dbo.Candidate c ON c.CandidateId = ec.CandidateId AND c.CandidateNumber = N'903'
    JOIN dbo.Session_ExamSection ses ON ses.SessionId = ec.SessionId
    JOIN dbo.ExamSection sec ON sec.ExamSectionId = ses.ExamSectionId AND sec.SectionName = N'Thực hành'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.ExamScore oldScore
        WHERE oldScore.ExamResultId = er.ExamResultId AND oldScore.ExamSectionId = sec.ExamSectionId);

    DECLARE @DemoDeductionId INT = (SELECT TOP 1 ScoreDeductionId FROM dbo.ScoreDeduction ORDER BY ScoreDeductionId);
    IF @DemoDeductionId IS NOT NULL
    BEGIN
        INSERT INTO dbo.Score_Deduction (ExamScoreId, ScoreDeductionId)
        SELECT es.ExamScoreId, @DemoDeductionId
        FROM dbo.ExamScore es
        JOIN dbo.ExamResult er ON er.ExamResultId = es.ExamResultId
        JOIN dbo.Exam_Candidate ec ON ec.ExamCandidateId = er.ExamCandidateId
        JOIN dbo.Candidate c ON c.CandidateId = ec.CandidateId AND c.CandidateNumber = N'903'
        JOIN dbo.ExamSection sec ON sec.ExamSectionId = es.ExamSectionId AND sec.SectionName = N'Thực hành'
        WHERE NOT EXISTS (SELECT 1 FROM dbo.Score_Deduction sd
            WHERE sd.ExamScoreId = es.ExamScoreId AND sd.ScoreDeductionId = @DemoDeductionId);
    END;

    -- Xac thuc lai toan bo khoa ngoai. Neu con du lieu mo coi, transaction se
    -- rollback thay vi de DB o trang thai nua dung nua sai.
    DECLARE @TrustForeignKeysSql NVARCHAR(MAX);
    SELECT @TrustForeignKeysSql = STRING_AGG(CAST(
        N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + N'.'
        + QUOTENAME(OBJECT_NAME(parent_object_id)) + N' WITH CHECK CHECK CONSTRAINT '
        + QUOTENAME([name]) + N';' AS NVARCHAR(MAX)), CHAR(10))
    FROM sys.foreign_keys
    WHERE is_disabled = 0;
    IF @TrustForeignKeysSql IS NOT NULL EXEC sys.sp_executesql @TrustForeignKeysSql;

    COMMIT TRANSACTION;

    -- Bao cao nhanh sau khi seed.
    SELECT [Role], COUNT(*) AS AccountCount
    FROM dbo.[User]
    WHERE Email LIKE N'demo.%@laivui.local'
    GROUP BY [Role]
    ORDER BY [Role];

    SELECT l.LicenceClass, er.RegistrationStatus, COUNT(*) AS RegistrationCount
    FROM dbo.ExamRegistration er
    JOIN dbo.Profile p ON p.ProfileId = er.ProfileId
    JOIN dbo.[User] u ON u.UserId = p.UserId AND u.Email LIKE N'demo.%@laivui.local'
    JOIN dbo.Licence l ON l.LicenceId = er.LicenceId
    GROUP BY l.LicenceClass, er.RegistrationStatus
    ORDER BY l.LicenceClass, er.RegistrationStatus;

    SELECT
        (SELECT COUNT(*) FROM dbo.Candidate WHERE CandidateNumber IN (N'901', N'902', N'903')) AS DemoCandidates,
        (SELECT COUNT(*) FROM dbo.Exam_Candidate ec JOIN dbo.Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber IN (N'901', N'902', N'903')) AS DemoExamCandidates,
        (SELECT COUNT(*) FROM dbo.Payment WHERE TransactionReference LIKE N'DEMO-PAY-%') AS DemoPayments,
        (SELECT COUNT(*) FROM dbo.TheoryPaper tp JOIN dbo.Exam_Candidate ec ON ec.ExamCandidateId = tp.ExamCandidateId JOIN dbo.Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber IN (N'901', N'902', N'903')) AS DemoTheoryPapers,
        (SELECT COUNT(*) FROM dbo.ExamResult er JOIN dbo.Exam_Candidate ec ON ec.ExamCandidateId = er.ExamCandidateId JOIN dbo.Candidate c ON c.CandidateId = ec.CandidateId WHERE c.CandidateNumber IN (N'901', N'902', N'903')) AS DemoResults;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
