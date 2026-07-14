-- ================================================================
-- Thu hep pham vi he thong con 3 hang GPLX hien hanh: A1, A, B1
-- Target: DLEM_DB_2 (schema dang duoc ung dung hien tai su dung)
--
-- Nghiep vu:
--   A1: mo to hai banh den 125 cm3 hoac dong co dien den 11 kW
--   A : mo to hai banh tren 125 cm3 hoac dong co dien tren 11 kW
--   B1: mo to ba banh va cac xe thuoc hang A1
--
-- Chu y: script lam sach du lieu VAN HANH KY THI cu (Exam/Session/
-- Candidate/Score/Payment) de khong bien du lieu thi o to B2/C thanh
-- du lieu B1 sai nghiep vu. User, Profile, Document va ExamRegistration
-- duoc giu lai; dang ky cu duoc quy ve 3 hang trong pham vi demo.
-- ================================================================

USE DLEM_DB_2;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    -- Constraint co the da duoc tao khi chay lai migration.
    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = N'CK_Licence_Class_Scope_A1_A_B1'
          AND parent_object_id = OBJECT_ID(N'dbo.Licence')
    )
        ALTER TABLE dbo.Licence DROP CONSTRAINT CK_Licence_Class_Scope_A1_A_B1;

    -- Luat moi doi ten hang A2 thanh A. Neu DB da co A thi giu ban ghi A.
    IF NOT EXISTS (SELECT 1 FROM dbo.Licence WHERE LicenceClass = N'A')
       AND EXISTS (SELECT 1 FROM dbo.Licence WHERE LicenceClass = N'A2')
    BEGIN
        UPDATE dbo.Licence
        SET LicenceClass = N'A'
        WHERE LicenceClass = N'A2';
    END;

    -- Dam bao du dung 3 hang, khong phu thuoc seed cu.
    IF NOT EXISTS (SELECT 1 FROM dbo.Licence WHERE LicenceClass = N'A1')
        INSERT INTO dbo.Licence
            (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId)
        VALUES
            (N'A1', N'Xe mô tô hai bánh đến 125 cm³ hoặc động cơ điện đến 11 kW', 18, 0, NULL);

    IF NOT EXISTS (SELECT 1 FROM dbo.Licence WHERE LicenceClass = N'A')
        INSERT INTO dbo.Licence
            (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId)
        VALUES
            (N'A', N'Xe mô tô hai bánh trên 125 cm³ hoặc động cơ điện trên 11 kW; bao gồm xe hạng A1', 18, 0, NULL);

    IF NOT EXISTS (SELECT 1 FROM dbo.Licence WHERE LicenceClass = N'B1')
        INSERT INTO dbo.Licence
            (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId)
        VALUES
            (N'B1', N'Xe mô tô ba bánh và các loại xe thuộc hạng A1', 18, 0, NULL);

    UPDATE dbo.Licence
    SET Description = CASE LicenceClass
            WHEN N'A1' THEN N'Xe mô tô hai bánh đến 125 cm³ hoặc động cơ điện đến 11 kW'
            WHEN N'A'  THEN N'Xe mô tô hai bánh trên 125 cm³ hoặc động cơ điện trên 11 kW; bao gồm xe hạng A1'
            WHEN N'B1' THEN N'Xe mô tô ba bánh và các loại xe thuộc hạng A1'
        END,
        MinimumAge = 18,
        ValidForYears = 0,
        UpgradeFromLicenceId = NULL
    WHERE LicenceClass IN (N'A1', N'A', N'B1');

    DECLARE @A1Id INT = (SELECT LicenceId FROM dbo.Licence WHERE LicenceClass = N'A1');
    DECLARE @AId  INT = (SELECT LicenceId FROM dbo.Licence WHERE LicenceClass = N'A');
    DECLARE @B1Id INT = (SELECT LicenceId FROM dbo.Licence WHERE LicenceClass = N'B1');

    -- Giu ho so dang ky de Managing Staff con du lieu duyet/demo.
    -- B/B2 quy ve B1; cac hang ngoai pham vi con lai quy ve A.
    UPDATE er
    SET LicenceId = CASE
        WHEN l.LicenceClass IN (N'B', N'B2') THEN @B1Id
        ELSE @AId
    END
    FROM dbo.ExamRegistration er
    JOIN dbo.Licence l ON l.LicenceId = er.LicenceId
    WHERE l.LicenceClass NOT IN (N'A1', N'A', N'B1');

    -- Chuan hoa loai tai lieu cu sang bo ma ma code Managing Staff dang dung.
    UPDATE dbo.Document
    SET DocumentType = N'ID_FRONT'
    WHERE DocumentType = N'CCCD'
      AND (DocumentUrl LIKE N'%front%' OR DocumentUrl LIKE N'%mat-truoc%');

    UPDATE dbo.Document
    SET DocumentType = N'ID_BACK'
    WHERE DocumentType = N'CCCD'
      AND (DocumentUrl LIKE N'%back%' OR DocumentUrl LIKE N'%mat-sau%');

    UPDATE dbo.Document
    SET DocumentType = N'HEALTH_CERTIFICATE'
    WHERE DocumentType IN (N'Giấy khám SK', N'Giấy khám sức khỏe');

    -- ----------------------------------------------------------------
    -- Xoa du lieu van hanh cu theo thu tu khoa ngoai.
    -- Khong xoa User/Profile/Document/ExamRegistration.
    -- ----------------------------------------------------------------
    DELETE FROM dbo.CandidateAnswer;
    DELETE FROM dbo.Score_Deduction;
    DELETE FROM dbo.ExamScore;
    DELETE FROM dbo.ExamResult;
    DELETE FROM dbo.TheoryPaper;
    DELETE FROM dbo.Payment_Fee;
    DELETE FROM dbo.Payment;
    DELETE FROM dbo.Exam_Candidate;
    DELETE FROM dbo.Candidate;
    DELETE FROM dbo.Session_Examiner;
    DELETE FROM dbo.Session_ExamArea;
    DELETE FROM dbo.Session_ExamSection;
    DELETE FROM dbo.[Session];
    DELETE FROM dbo.Exam;

    -- Chi giu cau hinh cau hoi/phan thi cua 3 hang trong pham vi.
    DELETE lq
    FROM dbo.Licence_Question lq
    JOIN dbo.Licence l ON l.LicenceId = lq.LicenceId
    WHERE l.LicenceClass NOT IN (N'A1', N'A', N'B1');

    DELETE les
    FROM dbo.Licence_ExamSection les
    JOIN dbo.Licence l ON l.LicenceId = les.LicenceId;

    UPDATE dbo.Licence SET UpgradeFromLicenceId = NULL;
    DELETE FROM dbo.Licence
    WHERE LicenceClass NOT IN (N'A1', N'A', N'B1');

    -- Chan viec them lai cac hang ngoai scope tu Admin/DAO cu.
    ALTER TABLE dbo.Licence WITH CHECK
    ADD CONSTRAINT CK_Licence_Class_Scope_A1_A_B1
        CHECK (LicenceClass IN (N'A1', N'A', N'B1'));
    ALTER TABLE dbo.Licence
        CHECK CONSTRAINT CK_Licence_Class_Scope_A1_A_B1;

    -- Moi hang trong scope chi gom Ly thuyet va Thuc hanh.
    IF NOT EXISTS (SELECT 1 FROM dbo.ExamSection WHERE SectionName = N'Lý thuyết')
        INSERT INTO dbo.ExamSection (SectionName) VALUES (N'Lý thuyết');
    IF NOT EXISTS (SELECT 1 FROM dbo.ExamSection WHERE SectionName = N'Thực hành')
        INSERT INTO dbo.ExamSection (SectionName) VALUES (N'Thực hành');

    DECLARE @TheoryId INT = (SELECT ExamSectionId FROM dbo.ExamSection WHERE SectionName = N'Lý thuyết');
    DECLARE @PracticeId INT = (SELECT ExamSectionId FROM dbo.ExamSection WHERE SectionName = N'Thực hành');

    INSERT INTO dbo.Licence_ExamSection (LicenceId, ExamSectionId, DurationMinutes)
    VALUES
        (@A1Id, @TheoryId, 19), (@A1Id, @PracticeId, NULL),
        (@AId,  @TheoryId, 19), (@AId,  @PracticeId, NULL),
        (@B1Id, @TheoryId, 19), (@B1Id, @PracticeId, NULL);

    -- Khu vuc mau dung chung cho A1/A/B1.
    IF NOT EXISTS (SELECT 1 FROM dbo.ExamArea WHERE AreaName = N'Phòng lý thuyết mô tô 1')
        INSERT INTO dbo.ExamArea (AreaName, AreaType, Capacity, [Location])
        VALUES (N'Phòng lý thuyết mô tô 1', N'Room', 30, N'Tầng 1 - Trung tâm sát hạch');

    IF NOT EXISTS (SELECT 1 FROM dbo.ExamArea WHERE AreaName = N'Sân thực hành mô tô 1')
        INSERT INTO dbo.ExamArea (AreaName, AreaType, Capacity, [Location])
        VALUES (N'Sân thực hành mô tô 1', N'Ground', 30, N'Khu sân sát hạch mô tô');

    DECLARE @TheoryAreaId INT = (SELECT ExamAreaId FROM dbo.ExamArea WHERE AreaName = N'Phòng lý thuyết mô tô 1');
    DECLARE @PracticeAreaId INT = (SELECT ExamAreaId FROM dbo.ExamArea WHERE AreaName = N'Sân thực hành mô tô 1');

    -- Du lieu ky thi/ca thi mau: moi Exam la mot ky, moi ky co 2 Session.
    INSERT INTO dbo.Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId)
    VALUES
        (N'EX-A1-20260815', '2026-08-15T07:00:00', N'Trung tâm sát hạch Lái Vui', N'Scheduled', @A1Id),
        (N'EX-A-20260816',  '2026-08-16T07:00:00', N'Trung tâm sát hạch Lái Vui', N'Scheduled', @AId),
        (N'EX-B1-20260822', '2026-08-22T07:00:00', N'Trung tâm sát hạch Lái Vui', N'Scheduled', @B1Id);

    DECLARE @ExamA1Id INT = (SELECT ExamId FROM dbo.Exam WHERE ExamCode = N'EX-A1-20260815');
    DECLARE @ExamAId  INT = (SELECT ExamId FROM dbo.Exam WHERE ExamCode = N'EX-A-20260816');
    DECLARE @ExamB1Id INT = (SELECT ExamId FROM dbo.Exam WHERE ExamCode = N'EX-B1-20260822');

    INSERT INTO dbo.[Session] (SessionName, StartTime, EndTime, [Status], ExamId)
    VALUES
        (N'Ca lý thuyết A1',  '2026-08-15T07:30:00', '2026-08-15T08:00:00', N'Scheduled', @ExamA1Id),
        (N'Ca thực hành A1',  '2026-08-15T08:15:00', '2026-08-15T11:30:00', N'Scheduled', @ExamA1Id),
        (N'Ca lý thuyết A',   '2026-08-16T07:30:00', '2026-08-16T08:00:00', N'Scheduled', @ExamAId),
        (N'Ca thực hành A',   '2026-08-16T08:15:00', '2026-08-16T11:30:00', N'Scheduled', @ExamAId),
        (N'Ca lý thuyết B1',  '2026-08-22T07:30:00', '2026-08-22T08:00:00', N'Scheduled', @ExamB1Id),
        (N'Ca thực hành B1',  '2026-08-22T08:15:00', '2026-08-22T11:30:00', N'Scheduled', @ExamB1Id);

    INSERT INTO dbo.Session_ExamSection (SessionId, ExamSectionId)
    SELECT SessionId,
           CASE WHEN SessionName LIKE N'%lý thuyết%' THEN @TheoryId ELSE @PracticeId END
    FROM dbo.[Session]
    WHERE ExamId IN (@ExamA1Id, @ExamAId, @ExamB1Id);

    INSERT INTO dbo.Session_ExamArea (SessionId, ExamAreaId)
    SELECT SessionId,
           CASE WHEN SessionName LIKE N'%lý thuyết%' THEN @TheoryAreaId ELSE @PracticeAreaId END
    FROM dbo.[Session]
    WHERE ExamId IN (@ExamA1Id, @ExamAId, @ExamB1Id);

    COMMIT TRANSACTION;

    SELECT LicenceId, LicenceClass, Description, MinimumAge, ValidForYears
    FROM dbo.Licence
    ORDER BY CASE LicenceClass WHEN N'A1' THEN 1 WHEN N'A' THEN 2 WHEN N'B1' THEN 3 END;

    SELECT e.ExamCode, l.LicenceClass, s.SessionName, s.StartTime, s.EndTime, s.[Status]
    FROM dbo.Exam e
    JOIN dbo.Licence l ON l.LicenceId = e.LicenceId
    JOIN dbo.[Session] s ON s.ExamId = e.ExamId
    ORDER BY s.StartTime;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
