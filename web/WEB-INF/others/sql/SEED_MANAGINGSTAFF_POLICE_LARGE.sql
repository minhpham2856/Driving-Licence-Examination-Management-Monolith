-- ============================================================
-- DỮ LIỆU MỞ RỘNG CHO MANAGING STAFF + POLICE STAFF
-- Có thể chạy trực tiếp SAU DDL_DLEM_DB_2_POLICE.sql.
-- Nếu cần toàn bộ câu hỏi/thiết bị của hệ thống, chạy DML gốc trước rồi
-- mới chạy file này. File tự bổ sung dữ liệu nền còn thiếu cho Managing.
--
-- Mật khẩu các tài khoản tạo trong file này: login123
-- File có thể chạy lại; dữ liệu demo dùng prefix ms_demo_/RPT-/POLICE-.
-- ============================================================

USE DLEM_DB_2;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.ExamDates', 'PoliceStatus') IS NULL
        THROW 50001, N'DB chưa có ExamDates.PoliceStatus. Hãy chạy DDL mới trước.', 1;

    IF COL_LENGTH('dbo.RegistrationDates', 'PoliceStatus') IS NULL
        THROW 50002, N'DB chưa có RegistrationDates.PoliceStatus. Hãy chạy DDL mới trước.', 1;

    IF COL_LENGTH('dbo.Exam', 'SourceExamDateId') IS NULL
        THROW 50003, N'DB chưa có Exam.SourceExamDateId. Hãy chạy DDL mới trước.', 1;

    IF COL_LENGTH('dbo.ExamEnrollment', 'ExamRegistrationId') IS NULL
        THROW 50004, N'DB chưa có ExamEnrollment.ExamRegistrationId. Hãy chạy DDL mới trước.', 1;

    IF COL_LENGTH('dbo.ExamRegistration', 'IsRetake') IS NULL
        THROW 50006, N'DB chưa có ExamRegistration.IsRetake. Hãy chạy DDL mới trước.', 1;

    IF COL_LENGTH('dbo.OfficialExamCandidate', 'ExamParticipationType') IS NULL
        THROW 50007, N'DB chưa có OfficialExamCandidate.ExamParticipationType. Hãy chạy DDL mới trước.', 1;

    DECLARE @PasswordHash NVARCHAR(255) =
        N'$2a$10$E8ocGIv4gRp6xZurl5egNuxir.0zn/5BUJMO5kIjdz38csrH3s7Cm';

    -- ========================================================
    -- 1. ROLE + TÀI KHOẢN MANAGING/POLICE + HẠNG GPLX NỀN
    -- ========================================================

    IF NOT EXISTS (SELECT 1 FROM [Role] WHERE RoleName = N'Quản trị viên')
        INSERT INTO [Role](RoleName) VALUES (N'Quản trị viên');
    IF NOT EXISTS (SELECT 1 FROM [Role] WHERE RoleName = N'Sát hạch viên')
        INSERT INTO [Role](RoleName) VALUES (N'Sát hạch viên');
    IF NOT EXISTS (SELECT 1 FROM [Role] WHERE RoleName = N'Cán bộ quản lý')
        INSERT INTO [Role](RoleName) VALUES (N'Cán bộ quản lý');
    IF NOT EXISTS (SELECT 1 FROM [Role] WHERE RoleName = N'Cán bộ kỳ thi')
        INSERT INTO [Role](RoleName) VALUES (N'Cán bộ kỳ thi');
    IF NOT EXISTS (SELECT 1 FROM [Role] WHERE RoleName = N'Thí sinh')
        INSERT INTO [Role](RoleName) VALUES (N'Thí sinh');
    IF NOT EXISTS (SELECT 1 FROM [Role] WHERE RoleName = N'Người đăng ký thi')
        INSERT INTO [Role](RoleName) VALUES (N'Người đăng ký thi');
    IF NOT EXISTS (SELECT 1 FROM [Role] WHERE RoleName = N'Cán bộ CSGT')
        INSERT INTO [Role](RoleName) VALUES (N'Cán bộ CSGT');

    DECLARE @ManagerRoleId INT =
        (SELECT RoleId FROM [Role] WHERE RoleName = N'Cán bộ quản lý');
    DECLARE @PoliceRoleId INT =
        (SELECT RoleId FROM [Role] WHERE RoleName = N'Cán bộ CSGT');

    IF NOT EXISTS (SELECT 1 FROM [User] WHERE Username = N'qly123')
    BEGIN
        INSERT INTO [User](Username, Email, PasswordHash, RoleId, IsActive)
        VALUES (
            N'qly123',
            N'quanly.hoso@trungtamsathach.vn',
            @PasswordHash,
            @ManagerRoleId,
            1
        );
    END;

    IF NOT EXISTS (SELECT 1 FROM [User] WHERE Username = N'police123')
    BEGIN
        INSERT INTO [User](Username, Email, PasswordHash, RoleId, IsActive)
        VALUES (
            N'police123',
            N'police@csgt.gov.vn',
            @PasswordHash,
            @PoliceRoleId,
            1
        );
    END;

    DECLARE @PoliceUserId INT =
        (SELECT UserId FROM [User] WHERE Username = N'police123');

    IF NOT EXISTS (
        SELECT 1 FROM Profile
        WHERE UserId = (SELECT UserId FROM [User] WHERE Username = N'qly123')
    )
    BEGIN
        INSERT INTO Profile
            (FullName, DateOfBirth, PhoneNumber, Sex,
             GovernmentIdNumber, Address, UserId)
        VALUES
            (N'Nguyễn Thị Quản Lý', '1986-01-10', N'0908000003', 0,
             N'001086011099', N'Trung tâm sát hạch Lái Vui, Hà Nội',
             (SELECT UserId FROM [User] WHERE Username = N'qly123'));
    END;

    IF NOT EXISTS (SELECT 1 FROM Profile WHERE UserId = @PoliceUserId)
    BEGIN
        INSERT INTO Profile
            (FullName, DateOfBirth, PhoneNumber, Sex,
             GovernmentIdNumber, Address, UserId)
        VALUES
            (N'Nguyễn Văn Cảnh Sát', '1987-02-20', N'0908000007', 1,
             N'001087022099', N'Phòng Cảnh sát giao thông, Hà Nội',
             @PoliceUserId);
    END;

    DECLARE @RegistrantRoleId INT =
        (SELECT RoleId FROM [Role] WHERE RoleName = N'Người đăng ký thi');

    IF @RegistrantRoleId IS NULL
        THROW 50005, N'Không tìm thấy role Người đăng ký thi.', 1;

    IF NOT EXISTS (SELECT 1 FROM Licence WHERE LicenceClass = N'A1')
        INSERT INTO Licence
            (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId)
        VALUES
            (N'A1', N'Xe mô tô hai bánh đến 125 cm³', 18, 0, NULL);

    DECLARE @BaseA1Id INT =
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1');

    IF NOT EXISTS (SELECT 1 FROM Licence WHERE LicenceClass = N'A')
        INSERT INTO Licence
            (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId)
        VALUES
            (N'A', N'Xe mô tô hai bánh trên 125 cm³', 18, 0, @BaseA1Id);

    IF NOT EXISTS (SELECT 1 FROM Licence WHERE LicenceClass = N'B1')
        INSERT INTO Licence
            (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId)
        VALUES
            (N'B1', N'Xe mô tô ba bánh', 18, 0, NULL);

    UPDATE Licence
    SET UpgradeFromLicenceId = @BaseA1Id
    WHERE LicenceClass = N'A' AND UpgradeFromLicenceId IS NULL;

    DECLARE @A1 INT = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1');
    DECLARE @A  INT = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A');
    DECLARE @B1 INT = (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1');

    IF @A1 IS NULL OR @A IS NULL OR @B1 IS NULL
        THROW 50006, N'Cần đủ ba hạng A1, A và B1 trong bảng Licence.', 1;

    -- Đảm bảo bốn loại tài liệu trong scope tồn tại.
    IF NOT EXISTS (SELECT 1 FROM DocumentType WHERE [Type] = N'Ảnh chân dung 3x4')
        INSERT DocumentType([Type]) VALUES (N'Ảnh chân dung 3x4');
    IF NOT EXISTS (SELECT 1 FROM DocumentType WHERE [Type] = N'Căn cước công dân (mặt trước)')
        INSERT DocumentType([Type]) VALUES (N'Căn cước công dân (mặt trước)');
    IF NOT EXISTS (SELECT 1 FROM DocumentType WHERE [Type] = N'Căn cước công dân (mặt sau)')
        INSERT DocumentType([Type]) VALUES (N'Căn cước công dân (mặt sau)');
    IF NOT EXISTS (SELECT 1 FROM DocumentType WHERE [Type] = N'Giấy khám sức khỏe')
        INSERT DocumentType([Type]) VALUES (N'Giấy khám sức khỏe');

    DECLARE @PortraitType INT =
        (SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Ảnh chân dung 3x4');
    DECLARE @FrontType INT =
        (SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Căn cước công dân (mặt trước)');
    DECLARE @BackType INT =
        (SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Căn cước công dân (mặt sau)');
    DECLARE @HealthType INT =
        (SELECT DocumentTypeId FROM DocumentType WHERE [Type] = N'Giấy khám sức khỏe');

    -- ========================================================
    -- 2. 120 REGISTRANT/HỒ SƠ DEMO
    -- ========================================================

    DECLARE @Demo TABLE (
        Seq INT PRIMARY KEY,
        UserId INT NOT NULL,
        ProfileId INT NOT NULL,
        ExamRegistrationId INT NOT NULL,
        LicenceId INT NOT NULL
    );

    DECLARE @i INT = 1;
    WHILE @i <= 120
    BEGIN
        DECLARE @Suffix NVARCHAR(3) = RIGHT(N'000' + CONVERT(NVARCHAR(3), @i), 3);
        DECLARE @Username NVARCHAR(100) = N'ms_demo_' + @Suffix;
        DECLARE @Email NVARCHAR(255) = N'ms.demo.' + @Suffix + N'@laivui.local';
        DECLARE @CitizenId NVARCHAR(12) =
            N'079' + RIGHT(N'000000000' + CONVERT(NVARCHAR(9), @i), 9);
        DECLARE @Phone NVARCHAR(20) =
            N'0907' + RIGHT(N'000000' + CONVERT(NVARCHAR(6), @i), 6);
        DECLARE @FullName NVARCHAR(255) =
            CASE @i % 8
                WHEN 0 THEN N'Nguyễn Minh Anh '
                WHEN 1 THEN N'Trần Hoàng Nam '
                WHEN 2 THEN N'Lê Thu Hà '
                WHEN 3 THEN N'Phạm Quốc Bảo '
                WHEN 4 THEN N'Vũ Ngọc Mai '
                WHEN 5 THEN N'Đỗ Đức Long '
                WHEN 6 THEN N'Bùi Khánh Linh '
                ELSE N'Ngô Thành Công '
            END + @Suffix;

        IF NOT EXISTS (SELECT 1 FROM [User] WHERE Username = @Username)
        BEGIN
            INSERT INTO [User](Username, Email, PasswordHash, RoleId, IsActive)
            VALUES (@Username, @Email, @PasswordHash, @RegistrantRoleId,
                    CASE WHEN @i IN (17, 58, 99) THEN 0 ELSE 1 END);
        END;

        DECLARE @DemoUserId INT =
            (SELECT UserId FROM [User] WHERE Username = @Username);

        IF NOT EXISTS (SELECT 1 FROM Profile WHERE GovernmentIdNumber = @CitizenId)
        BEGIN
            INSERT INTO Profile
                (FullName, DateOfBirth, PhoneNumber, Sex,
                 GovernmentIdNumber, Address, UserId)
            VALUES
                (@FullName,
                 DATEADD(DAY, -(@i * 17), CONVERT(DATETIME, '2002-12-31')),
                 @Phone,
                 CASE WHEN @i % 2 = 0 THEN 0 ELSE 1 END,
                 @CitizenId,
                 N'Số ' + CONVERT(NVARCHAR(10), @i)
                    + N', đường Nguyễn Trãi, Hà Nội',
                 @DemoUserId);
        END;

        DECLARE @ProfileId INT =
            (SELECT ProfileId FROM Profile WHERE GovernmentIdNumber = @CitizenId);
        DECLARE @LicenceId INT =
            CASE @i % 3 WHEN 1 THEN @A1 WHEN 2 THEN @A ELSE @B1 END;
        DECLARE @RegistrationStatus NVARCHAR(50) =
            CASE
                WHEN @i <= 12 THEN N'Chờ duyệt'
                WHEN @i <= 18 THEN N'Loại'
                ELSE N'Duyệt'
            END;

        IF NOT EXISTS (
            SELECT 1 FROM ExamRegistration
            WHERE ProfileId = @ProfileId AND LicenceId = @LicenceId
        )
        BEGIN
            INSERT INTO ExamRegistration
                (RegistrationStatus, Notes, ProfileId, LicenceId, IsRetake)
            VALUES
                (@RegistrationStatus,
                 CASE
                    WHEN @RegistrationStatus = N'Chờ duyệt'
                        THEN N'#MS_LARGE# Hồ sơ đang chờ Managing Staff thẩm định'
                    WHEN @RegistrationStatus = N'Loại'
                        THEN N'#MS_LARGE# Hồ sơ demo bị từ chối'
                    ELSE N'#MS_LARGE# Hồ sơ đã được Managing Staff duyệt'
                 END,
                 @ProfileId,
                 @LicenceId,
                 CASE WHEN @RegistrationStatus = N'Duyệt' AND @i % 8 = 0
                      THEN 1 ELSE 0 END);
        END;

        DECLARE @RegistrationId INT = (
            SELECT TOP (1) ExamRegistrationId
            FROM ExamRegistration
            WHERE ProfileId = @ProfileId AND LicenceId = @LicenceId
            ORDER BY ExamRegistrationId DESC
        );

        INSERT INTO @Demo(Seq, UserId, ProfileId, ExamRegistrationId, LicenceId)
        VALUES (@i, @DemoUserId, @ProfileId, @RegistrationId, @LicenceId);

        -- Mười hai hồ sơ đầu cố ý thiếu tài liệu để có dữ liệu chờ duyệt.
        IF @i > 12
        BEGIN
            IF NOT EXISTS (
                SELECT 1 FROM Document
                WHERE ProfileId = @ProfileId AND DocumentTypeId = @PortraitType
            )
                INSERT Document(DocumentTypeId, DocumentUrl, Notes, ProfileId)
                VALUES (@PortraitType,
                        N'/uploads/dossiers/demo/' + @Suffix + N'/portrait.jpg',
                        N'#MS_LARGE# Ảnh chân dung demo', @ProfileId);

            IF NOT EXISTS (
                SELECT 1 FROM Document
                WHERE ProfileId = @ProfileId AND DocumentTypeId = @FrontType
            )
                INSERT Document(DocumentTypeId, DocumentUrl, Notes, ProfileId)
                VALUES (@FrontType,
                        N'/uploads/dossiers/demo/' + @Suffix + N'/cccd-front.jpg',
                        N'#MS_LARGE# CCCD mặt trước demo', @ProfileId);

            IF NOT EXISTS (
                SELECT 1 FROM Document
                WHERE ProfileId = @ProfileId AND DocumentTypeId = @BackType
            )
                INSERT Document(DocumentTypeId, DocumentUrl, Notes, ProfileId)
                VALUES (@BackType,
                        N'/uploads/dossiers/demo/' + @Suffix + N'/cccd-back.jpg',
                        N'#MS_LARGE# CCCD mặt sau demo', @ProfileId);

            IF NOT EXISTS (
                SELECT 1 FROM Document
                WHERE ProfileId = @ProfileId AND DocumentTypeId = @HealthType
            )
                INSERT Document(DocumentTypeId, DocumentUrl, Notes, ProfileId)
                VALUES (@HealthType,
                        N'/uploads/dossiers/demo/' + @Suffix + N'/health.pdf',
                        N'#MS_LARGE# Giấy khám sức khỏe demo', @ProfileId);
        END;

        SET @i += 1;
    END;

    -- ========================================================
    -- 3. NGÀY THI DỰ KIẾN Ở NHIỀU TRẠNG THÁI
    -- ========================================================

    DECLARE @Dates TABLE (
        ExamDate DATE PRIMARY KEY,
        LicenceId INT,
        [Status] NVARCHAR(20),
        PoliceStatus NVARCHAR(20),
        CancelReason NVARCHAR(500)
    );

    INSERT INTO @Dates(ExamDate, LicenceId, [Status], PoliceStatus, CancelReason)
    VALUES
        ('2026-06-17', @A,  N'Locked',    N'COMPLETED', NULL),
        ('2026-07-28', @A1, N'Locked',    N'PENDING',   NULL),
        ('2026-07-29', @A,  N'Locked',    N'PENDING',   NULL),
        ('2026-07-30', @B1, N'Locked',    N'COMPLETED', NULL),
        ('2026-08-12', @A1, N'Open',      N'NOT_SENT',  NULL),
        ('2026-08-19', @A1, N'Cancelled', N'NOT_SENT',  N'Không đủ số lượng thí sinh tối thiểu'),
        ('2026-08-26', @A,  N'Open',      N'NOT_SENT',  NULL),
        ('2026-09-09', @B1, N'Open',      N'NOT_SENT',  NULL),
        ('2026-09-23', @A1, N'Open',      N'NOT_SENT',  NULL),
        ('2026-10-07', @A,  N'Open',      N'NOT_SENT',  NULL),
        ('2026-10-21', @B1, N'Open',      N'NOT_SENT',  NULL);

    INSERT INTO ExamDates
        (ExamDate, LicenceId, [Status], PoliceStatus,
         CancelReason, CancelledAt, CancelledBy, CancelledRegistrationCount)
    SELECT d.ExamDate, d.LicenceId, d.[Status], d.PoliceStatus,
           d.CancelReason,
           CASE WHEN d.[Status] = N'Cancelled' THEN '2026-07-15 09:00:00' END,
           CASE WHEN d.[Status] = N'Cancelled'
                THEN (SELECT UserId FROM [User] WHERE Username = N'qly123') END,
           CASE WHEN d.[Status] = N'Cancelled' THEN 6 END
    FROM @Dates d
    WHERE NOT EXISTS (
        SELECT 1 FROM ExamDates existing WHERE existing.ExamDate = d.ExamDate
    );

    -- Gán mỗi hồ sơ đã duyệt vào một ngày phù hợp đúng hạng.
    DECLARE @Seq INT, @RegId INT, @RegLicenceId INT;
    DECLARE demo_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT Seq, ExamRegistrationId, LicenceId
        FROM @Demo
        WHERE Seq > 18
        ORDER BY Seq;

    OPEN demo_cursor;
    FETCH NEXT FROM demo_cursor INTO @Seq, @RegId, @RegLicenceId;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        DECLARE @SelectedDate DATE;

        IF @RegLicenceId = @A1
            SET @SelectedDate = CASE
                WHEN @Seq % 4 = 1 THEN '2026-07-28'
                WHEN @Seq % 2 = 0 THEN '2026-08-12'
                ELSE '2026-09-23'
            END;
        ELSE IF @RegLicenceId = @A
            SET @SelectedDate = CASE
                WHEN @Seq % 4 = 2 THEN '2026-07-29'
                WHEN @Seq % 2 = 0 THEN '2026-08-26'
                ELSE '2026-10-07'
            END;
        ELSE
            SET @SelectedDate = CASE
                WHEN @Seq % 4 = 3 THEN '2026-07-30'
                WHEN @Seq % 2 = 0 THEN '2026-09-09'
                ELSE '2026-10-21'
            END;

        DECLARE @SelectedExamDateId INT =
            (SELECT ExamDateId FROM ExamDates WHERE ExamDate = @SelectedDate);
        DECLARE @DatePoliceStatus NVARCHAR(20) =
            (SELECT PoliceStatus FROM ExamDates WHERE ExamDateId = @SelectedExamDateId);
        DECLARE @CandidatePoliceStatus NVARCHAR(20) =
            CASE
                WHEN @DatePoliceStatus = N'NOT_SENT' THEN N'NOT_SENT'
                WHEN @DatePoliceStatus = N'PENDING' AND @Seq % 7 = 0 THEN N'REJECTED'
                WHEN @DatePoliceStatus = N'PENDING' AND @Seq % 5 = 0 THEN N'APPROVED'
                WHEN @DatePoliceStatus = N'PENDING' THEN N'PENDING'
                WHEN @DatePoliceStatus = N'COMPLETED' AND @Seq % 5 = 0 THEN N'REJECTED'
                ELSE N'APPROVED'
            END;

        IF NOT EXISTS (
            SELECT 1 FROM RegistrationDates
            WHERE ExamRegistrationId = @RegId AND ExamDateId = @SelectedExamDateId
        )
        BEGIN
            INSERT INTO RegistrationDates
                (ExamRegistrationId, ExamDateId, IsActive,
                 PoliceStatus, PoliceReason, OfficialCandidateNumber)
            VALUES
                (@RegId, @SelectedExamDateId, 1,
                 @CandidatePoliceStatus,
                 CASE WHEN @CandidatePoliceStatus = N'REJECTED'
                      THEN N'Thông tin trong hồ sơ chưa khớp dữ liệu đối chiếu' END,
                 CASE WHEN @CandidatePoliceStatus = N'APPROVED'
                            AND @DatePoliceStatus = N'COMPLETED'
                      THEN RIGHT(N'000' + CONVERT(NVARCHAR(3), @Seq), 3) END);
        END;

        FETCH NEXT FROM demo_cursor INTO @Seq, @RegId, @RegLicenceId;
    END;

    CLOSE demo_cursor;
    DEALLOCATE demo_cursor;

    DECLARE @RetakePendingRegistrationId INT = (
        SELECT TOP (1) rd.ExamRegistrationId
        FROM RegistrationDates rd
        JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId
        WHERE rd.IsActive=1
          AND rd.PoliceStatus=N'PENDING'
          AND ed.PoliceStatus=N'PENDING'
        ORDER BY rd.RegistrationDateId
    );
    UPDATE ExamRegistration
    SET IsRetake=1,
        Notes=CONCAT(COALESCE(Notes + N'; ',N''),
                     N'#RETAKE_REQUEST# Thí sinh đề nghị thi lại')
    WHERE ExamRegistrationId=@RetakePendingRegistrationId
      AND IsRetake=0;

    DECLARE @RetakeOfficialRegistrationId INT = (
        SELECT TOP (1) rd.ExamRegistrationId
        FROM RegistrationDates rd
        JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId
        WHERE rd.IsActive=1
          AND rd.PoliceStatus=N'APPROVED'
          AND ed.PoliceStatus=N'COMPLETED'
          AND ed.ExamDate>CAST(GETDATE() AS DATE)
        ORDER BY rd.RegistrationDateId
    );
    UPDATE ExamRegistration
    SET IsRetake=1,
        Notes=CONCAT(COALESCE(Notes + N'; ',N''),
                     N'#RETAKE_REQUEST# CSGT cho phép chỉ thi lại thực hành')
    WHERE ExamRegistrationId=@RetakeOfficialRegistrationId
      AND IsRetake=0;

    ;WITH approved AS (
        SELECT rd.ExamDateId,er.ExamRegistrationId,ed.LicenceId,
               p.FullName,CAST(p.DateOfBirth AS DATE) DateOfBirth,
               p.GovernmentIdNumber,p.PhoneNumber,u.Email,er.IsRetake,
               ROW_NUMBER() OVER (
                   PARTITION BY rd.ExamDateId
                   ORDER BY rd.RegistrationDateId
               ) CandidateOrder
        FROM RegistrationDates rd
        JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId
        JOIN ExamRegistration er ON er.ExamRegistrationId=rd.ExamRegistrationId
        JOIN Profile p ON p.ProfileId=er.ProfileId
        JOIN [User] u ON u.UserId=p.UserId
        WHERE ed.PoliceStatus=N'COMPLETED'
          AND rd.IsActive=1
          AND rd.PoliceStatus=N'APPROVED'
    )
    INSERT INTO OfficialExamCandidate
        (ExamDateId,ExamRegistrationId,LicenceId,CandidateNumber,
         FullName,DateOfBirth,GovernmentIdNumber,PhoneNumber,Email,
         SourceUnitCode,SourceUnitName,ExamParticipationType)
    SELECT approved.ExamDateId,approved.ExamRegistrationId,approved.LicenceId,
           RIGHT(N'000' + CONVERT(NVARCHAR(10),approved.CandidateOrder),3),
           approved.FullName,approved.DateOfBirth,approved.GovernmentIdNumber,
           approved.PhoneNumber,approved.Email,N'LAIVUI',
           N'Trung tâm sát hạch Lái Vui',
           CASE WHEN approved.IsRetake=1
                THEN N'PRACTICAL_ONLY' ELSE N'FULL_EXAM' END
    FROM approved
    WHERE NOT EXISTS (
        SELECT 1 FROM OfficialExamCandidate official
        WHERE official.ExamDateId=approved.ExamDateId
          AND official.GovernmentIdNumber=approved.GovernmentIdNumber
    );

    UPDATE official
    SET ExamParticipationType=
        CASE WHEN registration.IsRetake=1
             THEN N'PRACTICAL_ONLY' ELSE N'FULL_EXAM' END
    FROM OfficialExamCandidate official
    JOIN ExamRegistration registration
      ON registration.ExamRegistrationId=official.ExamRegistrationId
    JOIN ExamDates date ON date.ExamDateId=official.ExamDateId
    WHERE date.PoliceStatus=N'COMPLETED';

    -- Sáu lựa chọn đã bị hủy để test lịch sử hủy và số lượng bị ảnh hưởng.
    DECLARE @CancelledDateId INT =
        (SELECT ExamDateId FROM ExamDates WHERE ExamDate = '2026-08-19');

    INSERT INTO RegistrationDates
        (ExamRegistrationId, ExamDateId, IsActive, PoliceStatus, PoliceReason)
    SELECT TOP (6) d.ExamRegistrationId, @CancelledDateId, 0, N'NOT_SENT',
           N'Ngày dự kiến đã bị trung tâm hủy'
    FROM @Demo d
    WHERE d.Seq > 18 AND d.LicenceId = @A1
      AND NOT EXISTS (
          SELECT 1 FROM RegistrationDates rd
          WHERE rd.ExamRegistrationId = d.ExamRegistrationId
            AND rd.ExamDateId = @CancelledDateId
      )
    ORDER BY d.Seq;

    -- ========================================================
    -- 4. PHIÊN CHÍNH THỨC TỪ DANH SÁCH POLICE ĐÃ DUYỆT
    -- ========================================================

    DECLARE @PastDateId INT =
        (SELECT ExamDateId FROM ExamDates WHERE ExamDate = '2026-06-17');

    -- Gắn 10 hồ sơ hạng A vào danh sách đã duyệt trong quá khứ.
    DECLARE @PastApproved TABLE(RowNo INT IDENTITY(1,1), ExamRegistrationId INT);
    INSERT INTO @PastApproved(ExamRegistrationId)
    SELECT TOP (10) d.ExamRegistrationId
    FROM @Demo d
    WHERE d.Seq > 18 AND d.LicenceId = @A
    ORDER BY d.Seq;

    INSERT INTO RegistrationDates
        (ExamRegistrationId, ExamDateId, IsActive,
         PoliceStatus, PoliceReason, OfficialCandidateNumber)
    SELECT p.ExamRegistrationId, @PastDateId, 0,
           N'APPROVED', NULL,
           RIGHT(N'000' + CONVERT(NVARCHAR(3), 700 + p.RowNo), 3)
    FROM @PastApproved p
    WHERE NOT EXISTS (
        SELECT 1 FROM RegistrationDates rd
        WHERE rd.ExamRegistrationId = p.ExamRegistrationId
          AND rd.ExamDateId = @PastDateId
    );

    INSERT INTO OfficialExamCandidate
        (ExamDateId, ExamRegistrationId, LicenceId, CandidateNumber,
         FullName, DateOfBirth, GovernmentIdNumber, PhoneNumber, Email,
         SourceUnitCode, SourceUnitName, ExamParticipationType)
    SELECT
        @PastDateId,
        p.ExamRegistrationId,
        @A,
        RIGHT(N'000' + CONVERT(NVARCHAR(3), 700 + p.RowNo), 3),
        profile.FullName,
        CAST(profile.DateOfBirth AS DATE),
        profile.GovernmentIdNumber,
        profile.PhoneNumber,
        account.Email,
        N'LAIVUI',
        N'Trung tâm sát hạch Lái Vui',
        CASE WHEN registration.IsRetake = 1
             THEN N'PRACTICAL_ONLY' ELSE N'FULL_EXAM' END
    FROM @PastApproved p
    JOIN ExamRegistration registration
      ON registration.ExamRegistrationId = p.ExamRegistrationId
    JOIN Profile profile ON profile.ProfileId = registration.ProfileId
    JOIN [User] account ON account.UserId = profile.UserId
    WHERE NOT EXISTS (
        SELECT 1
        FROM OfficialExamCandidate official
        WHERE official.ExamDateId = @PastDateId
          AND official.GovernmentIdNumber = profile.GovernmentIdNumber
    );

    IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamCode = N'POLICE-A-20260617')
    BEGIN
        INSERT INTO Exam
            (ExamCode, ExamDate, StartTime, EndTime,
             [Status], CentreName, LicenceId, SourceExamDateId)
        VALUES
            (N'POLICE-A-20260617', '2026-06-17 07:30:00',
             '2026-06-17 07:30:00', '2026-06-17 11:00:00',
             N'Completed', N'Trung tâm sát hạch Lái Vui', @A, @PastDateId);
    END;

    DECLARE @PoliceExamId INT =
        (SELECT ExamId FROM Exam WHERE ExamCode = N'POLICE-A-20260617');

    IF NOT EXISTS (
        SELECT 1 FROM ExamSection
        WHERE ExamId = @PoliceExamId AND SectionType = N'Lý thuyết'
    )
        INSERT ExamSection(SectionType, LicenceId, DurationMinutes, ExamId)
        VALUES (N'Lý thuyết', @A, 20, @PoliceExamId);

    IF NOT EXISTS (
        SELECT 1 FROM ExamSection
        WHERE ExamId = @PoliceExamId AND SectionType = N'Thực hành trong hình'
    )
        INSERT ExamSection(SectionType, LicenceId, DurationMinutes, ExamId)
        VALUES (N'Thực hành trong hình', @A, 30, @PoliceExamId);

    DECLARE @PastRow INT = 1;
    WHILE @PastRow <= 10
    BEGIN
        DECLARE @PastRegId INT =
            (SELECT ExamRegistrationId FROM @PastApproved WHERE RowNo = @PastRow);
        DECLARE @PastProfileId INT =
            (SELECT ProfileId FROM ExamRegistration WHERE ExamRegistrationId = @PastRegId);
        DECLARE @PastCandidateId INT;
        DECLARE @PastGovId NVARCHAR(100) =
            (SELECT GovernmentIdNumber FROM Profile WHERE ProfileId = @PastProfileId);
        DECLARE @PastSbd NVARCHAR(50) =
            RIGHT(N'000' + CONVERT(NVARCHAR(3), 700 + @PastRow), 3);

        SET @PastCandidateId = NULL;
        SELECT TOP (1) @PastCandidateId = c.CandidateId
        FROM Candidate c
        JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
        WHERE ee.ExamId = @PoliceExamId
          AND ee.ExamRegistrationId = @PastRegId;

        IF @PastCandidateId IS NULL
        BEGIN
            INSERT INTO Candidate
                (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email,
                 Sex, GovernmentIdNumber, Address, TakeTheory, TakeLayout,
                 TakeNo, ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended)
            SELECT
                @PastSbd, p.FullName, p.DateOfBirth, p.PhoneNumber, u.Email,
                p.Sex, p.GovernmentIdNumber, p.Address, 1, 1,
                1, N'Thi sát hạch hạng A', portrait.DocumentUrl, 0, 0
            FROM Profile p
            JOIN [User] u ON u.UserId = p.UserId
            OUTER APPLY (
                SELECT TOP (1) d.DocumentUrl
                FROM Document d
                WHERE d.ProfileId = p.ProfileId
                  AND d.DocumentTypeId = @PortraitType
                ORDER BY d.DocumentId DESC
            ) portrait
            WHERE p.ProfileId = @PastProfileId;

            SET @PastCandidateId = SCOPE_IDENTITY();
        END;

        DECLARE @PastEnrollmentId INT = (
            SELECT TOP (1) ExamEnrollmentId
            FROM ExamEnrollment
            WHERE ExamId = @PoliceExamId AND ExamRegistrationId = @PastRegId
        );

        IF @PastEnrollmentId IS NULL
        BEGIN
            INSERT INTO ExamEnrollment(CandidateId, ExamId, ExamRegistrationId)
            VALUES (@PastCandidateId, @PoliceExamId, @PastRegId);
            SET @PastEnrollmentId = SCOPE_IDENTITY();
        END;

        INSERT INTO ExamEnrollmentSection
            (ExamEnrollmentId, ExamSectionId, [Status], StartedAt, CompletedAt)
        SELECT @PastEnrollmentId, es.ExamSectionId, N'Completed',
               '2026-06-17 07:30:00', '2026-06-17 10:30:00'
        FROM ExamSection es
        WHERE es.ExamId = @PoliceExamId
          AND NOT EXISTS (
              SELECT 1 FROM ExamEnrollmentSection ees
              WHERE ees.ExamEnrollmentId = @PastEnrollmentId
                AND ees.ExamSectionId = es.ExamSectionId
          );

        DECLARE @PastResultId INT = (
            SELECT ExamResultId FROM ExamResult
            WHERE ExamEnrollmentId = @PastEnrollmentId
        );

        IF @PastResultId IS NULL
        BEGIN
            INSERT INTO ExamResult(ExamEnrollmentId, IsPassed, ResultDate)
            VALUES (@PastEnrollmentId,
                    CASE WHEN @PastRow IN (4, 9) THEN 0 ELSE 1 END,
                    '2026-06-17 11:15:00');
            SET @PastResultId = SCOPE_IDENTITY();
        END;

        INSERT INTO ExamScore(ExamResultId, ExamSectionId, Score)
        SELECT @PastResultId, es.ExamSectionId,
               CASE
                   WHEN @PastRow IN (4, 9) THEN 58 + @PastRow
                   WHEN es.SectionType = N'Lý thuyết' THEN 82 + (@PastRow % 15)
                   ELSE 80 + (@PastRow % 17)
               END
        FROM ExamSection es
        WHERE es.ExamId = @PoliceExamId
          AND NOT EXISTS (
              SELECT 1 FROM ExamScore score
              WHERE score.ExamResultId = @PastResultId
                AND score.ExamSectionId = es.ExamSectionId
          );

        UPDATE ExamRegistration
        SET RegistrationStatus = N'ExamCompleted'
        WHERE ExamRegistrationId = @PastRegId;

        SET @PastRow += 1;
    END;

    -- ========================================================
    -- 5. DỮ LIỆU BÁO CÁO THEO THÁNG/NĂM
    -- 2024: 12 tháng; 2025: 12 tháng; 2026: tháng 1-6.
    -- Mỗi tháng có A1, A, B1; mỗi kỳ có 10 thí sinh.
    -- ========================================================

    DECLARE @ReportClasses TABLE(
        ClassNo INT PRIMARY KEY,
        LicenceId INT,
        LicenceClass NVARCHAR(10),
        ExamDay INT
    );
    INSERT INTO @ReportClasses(ClassNo, LicenceId, LicenceClass, ExamDay)
    VALUES (1, @A1, N'A1', 10), (2, @A, N'A', 17), (3, @B1, N'B1', 24);

    DECLARE @Year INT = 2024;
    WHILE @Year <= 2026
    BEGIN
        DECLARE @MaxMonth INT = CASE WHEN @Year = 2026 THEN 6 ELSE 12 END;
        DECLARE @Month INT = 1;

        WHILE @Month <= @MaxMonth
        BEGIN
            DECLARE @ClassNo INT = 1;
            WHILE @ClassNo <= 3
            BEGIN
                DECLARE @ReportLicenceId INT;
                DECLARE @ReportLicenceClass NVARCHAR(10);
                DECLARE @ExamDay INT;
                SELECT @ReportLicenceId = LicenceId,
                       @ReportLicenceClass = LicenceClass,
                       @ExamDay = ExamDay
                FROM @ReportClasses WHERE ClassNo = @ClassNo;

                DECLARE @ReportDate DATE = DATEFROMPARTS(@Year, @Month, @ExamDay);
                DECLARE @ReportCode NVARCHAR(50) =
                    N'RPT-' + @ReportLicenceClass + N'-'
                    + CONVERT(NVARCHAR(4), @Year) + N'-'
                    + RIGHT(N'0' + CONVERT(NVARCHAR(2), @Month), 2);

                IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamCode = @ReportCode)
                BEGIN
                    INSERT INTO Exam
                        (ExamCode, ExamDate, StartTime, EndTime,
                         [Status], CentreName, LicenceId, SourceExamDateId)
                    VALUES
                        (@ReportCode,
                         DATEADD(MINUTE, 450, CONVERT(DATETIME, @ReportDate)),
                         DATEADD(MINUTE, 450, CONVERT(DATETIME, @ReportDate)),
                         DATEADD(MINUTE, 660, CONVERT(DATETIME, @ReportDate)),
                         N'Completed', N'Trung tâm sát hạch Lái Vui',
                         @ReportLicenceId, NULL);
                END;

                DECLARE @ReportExamId INT =
                    (SELECT ExamId FROM Exam WHERE ExamCode = @ReportCode);

                IF NOT EXISTS (
                    SELECT 1 FROM ExamSection
                    WHERE ExamId = @ReportExamId AND SectionType = N'Lý thuyết'
                )
                    INSERT ExamSection(SectionType, LicenceId, DurationMinutes, ExamId)
                    VALUES (N'Lý thuyết', @ReportLicenceId, 20, @ReportExamId);

                IF NOT EXISTS (
                    SELECT 1 FROM ExamSection
                    WHERE ExamId = @ReportExamId AND SectionType = N'Thực hành trong hình'
                )
                    INSERT ExamSection(SectionType, LicenceId, DurationMinutes, ExamId)
                    VALUES (N'Thực hành trong hình', @ReportLicenceId, 30, @ReportExamId);

                DECLARE @CandidateIndex INT = 1;
                WHILE @CandidateIndex <= 10
                BEGIN
                    DECLARE @ReportGovId NVARCHAR(100) =
                        RIGHT(
                            N'000000000000'
                            + CONVERT(NVARCHAR(20),
                                CONVERT(BIGINT, @Year) * 100000000
                                + @Month * 1000000
                                + @ClassNo * 10000
                                + @CandidateIndex),
                            12
                        );
                    DECLARE @ReportCandidateId INT;

                    SET @ReportCandidateId = NULL;
                    SELECT TOP (1) @ReportCandidateId = c.CandidateId
                    FROM Candidate c
                    JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                    WHERE ee.ExamId = @ReportExamId
                      AND c.GovernmentIdNumber = @ReportGovId;

                    IF @ReportCandidateId IS NULL
                    BEGIN
                        INSERT INTO Candidate
                            (CandidateNumber, FullName, DateOfBirth, PhoneNumber,
                             Email, Sex, GovernmentIdNumber, Address,
                             TakeTheory, TakeLayout, TakeNo, ReasonForTaking,
                             PhotoImageUrl, IsAbsent, IsSuspended)
                        VALUES
                            (RIGHT(N'000' + CONVERT(NVARCHAR(3), @CandidateIndex), 3),
                             N'Thí sinh báo cáo ' + @ReportLicenceClass + N' '
                                + CONVERT(NVARCHAR(4), @Year) + N'-'
                                + RIGHT(N'0' + CONVERT(NVARCHAR(2), @Month), 2)
                                + N'-' + RIGHT(N'00' + CONVERT(NVARCHAR(2), @CandidateIndex), 2),
                             DATEADD(DAY, -(@CandidateIndex * 123), '2001-12-31'),
                             N'0988' + RIGHT(N'000000' + CONVERT(NVARCHAR(6),
                                @Month * 1000 + @ClassNo * 100 + @CandidateIndex), 6),
                             N'report.' + CONVERT(NVARCHAR(4), @Year) + N'.'
                                + CONVERT(NVARCHAR(2), @Month) + N'.'
                                + CONVERT(NVARCHAR(2), @ClassNo) + N'.'
                                + CONVERT(NVARCHAR(2), @CandidateIndex) + N'@laivui.local',
                             CASE WHEN @CandidateIndex % 2 = 0 THEN 0 ELSE 1 END,
                             @ReportGovId,
                             N'Dữ liệu báo cáo Managing Staff',
                             1, 1, 1,
                             N'Thi sát hạch hạng ' + @ReportLicenceClass,
                             NULL,
                             CASE WHEN @CandidateIndex = 10 AND @Month % 4 = 0 THEN 1 ELSE 0 END,
                             0);
                        SET @ReportCandidateId = SCOPE_IDENTITY();
                    END;

                    DECLARE @ReportEnrollmentId INT = (
                        SELECT TOP (1) ExamEnrollmentId
                        FROM ExamEnrollment
                        WHERE CandidateId = @ReportCandidateId
                          AND ExamId = @ReportExamId
                    );

                    IF @ReportEnrollmentId IS NULL
                    BEGIN
                        INSERT ExamEnrollment(CandidateId, ExamId, ExamRegistrationId)
                        VALUES (@ReportCandidateId, @ReportExamId, NULL);
                        SET @ReportEnrollmentId = SCOPE_IDENTITY();
                    END;

                    INSERT INTO ExamEnrollmentSection
                        (ExamEnrollmentId, ExamSectionId, [Status], StartedAt, CompletedAt)
                    SELECT @ReportEnrollmentId, es.ExamSectionId,
                           CASE
                               WHEN @CandidateIndex = 10 AND @Month % 4 = 0
                                   THEN N'Absent'
                               ELSE N'Completed'
                           END,
                           DATEADD(MINUTE, 450, CONVERT(DATETIME, @ReportDate)),
                           DATEADD(MINUTE, 630, CONVERT(DATETIME, @ReportDate))
                    FROM ExamSection es
                    WHERE es.ExamId = @ReportExamId
                      AND NOT EXISTS (
                          SELECT 1 FROM ExamEnrollmentSection existing
                          WHERE existing.ExamEnrollmentId = @ReportEnrollmentId
                            AND existing.ExamSectionId = es.ExamSectionId
                      );

                    DECLARE @IsAbsent BIT =
                        CASE WHEN @CandidateIndex = 10 AND @Month % 4 = 0 THEN 1 ELSE 0 END;
                    DECLARE @IsPassed BIT =
                        CASE
                            WHEN @IsAbsent = 1 THEN 0
                            WHEN (@Month * 3 + @ClassNo * 5 + @CandidateIndex + @Year) % 10 < 7
                                THEN 1
                            ELSE 0
                        END;
                    DECLARE @ReportResultId INT = (
                        SELECT ExamResultId FROM ExamResult
                        WHERE ExamEnrollmentId = @ReportEnrollmentId
                    );

                    IF @ReportResultId IS NULL
                    BEGIN
                        INSERT ExamResult(ExamEnrollmentId, IsPassed, ResultDate)
                        VALUES (@ReportEnrollmentId, @IsPassed,
                                DATEADD(MINUTE, 670, CONVERT(DATETIME, @ReportDate)));
                        SET @ReportResultId = SCOPE_IDENTITY();
                    END;

                    INSERT INTO ExamScore(ExamResultId, ExamSectionId, Score)
                    SELECT @ReportResultId, es.ExamSectionId,
                           CASE
                               WHEN @IsAbsent = 1 THEN 0
                               WHEN @IsPassed = 1
                                   THEN 78 + ((@Month + @CandidateIndex + @ClassNo) % 20)
                               ELSE 45 + ((@Month + @CandidateIndex + @ClassNo) % 25)
                           END
                    FROM ExamSection es
                    WHERE es.ExamId = @ReportExamId
                      AND NOT EXISTS (
                          SELECT 1 FROM ExamScore existing
                          WHERE existing.ExamResultId = @ReportResultId
                            AND existing.ExamSectionId = es.ExamSectionId
                      );

                    SET @CandidateIndex += 1;
                END;

                SET @ClassNo += 1;
            END;

            SET @Month += 1;
        END;

        SET @Year += 1;
    END;

    -- ========================================================
    -- 6. PHIÊN SẮP TỚI/ĐANG THI/ĐÃ HỦY CHO CÁC TAB QUẢN LÝ
    -- ========================================================

    DECLARE @OperationalExams TABLE(
        ExamCode NVARCHAR(50),
        ExamDate DATETIME,
        StartTime DATETIME,
        EndTime DATETIME NULL,
        [Status] NVARCHAR(50),
        LicenceId INT
    );

    INSERT INTO @OperationalExams
        (ExamCode, ExamDate, StartTime, EndTime, [Status], LicenceId)
    VALUES
        (N'DEMO-A1-20260720', '2026-07-20 07:30:00', '2026-07-20 07:30:00', NULL, N'Đang diễn ra', @A1),
        (N'DEMO-A-20260805',  '2026-08-05 07:30:00', '2026-08-05 07:30:00', NULL, N'Chưa diễn ra', @A),
        (N'DEMO-B1-20260818', '2026-08-18 08:00:00', '2026-08-18 08:00:00', NULL, N'Chưa diễn ra', @B1),
        (N'DEMO-A1-20260912', '2026-09-12 07:30:00', '2026-09-12 07:30:00', NULL, N'Chưa diễn ra', @A1),
        (N'DEMO-A-20261015',  '2026-10-15 13:00:00', '2026-10-15 13:00:00', NULL, N'Chưa diễn ra', @A),
        (N'DEMO-B1-CANCEL',   '2026-08-02 07:30:00', '2026-08-02 07:30:00', NULL, N'Đã hủy', @B1);

    INSERT INTO Exam
        (ExamCode, ExamDate, StartTime, EndTime,
         [Status], CentreName, LicenceId, SourceExamDateId)
    SELECT o.ExamCode, o.ExamDate, o.StartTime, o.EndTime,
           o.[Status], N'Trung tâm sát hạch Lái Vui', o.LicenceId, NULL
    FROM @OperationalExams o
    WHERE NOT EXISTS (SELECT 1 FROM Exam e WHERE e.ExamCode = o.ExamCode);

    INSERT INTO ExamSection(SectionType, LicenceId, DurationMinutes, ExamId)
    SELECT N'Lý thuyết', e.LicenceId, 20, e.ExamId
    FROM Exam e
    WHERE e.ExamCode LIKE N'DEMO-%'
      AND NOT EXISTS (
          SELECT 1 FROM ExamSection es
          WHERE es.ExamId = e.ExamId AND es.SectionType = N'Lý thuyết'
      );

    INSERT INTO ExamSection(SectionType, LicenceId, DurationMinutes, ExamId)
    SELECT N'Thực hành trong hình', e.LicenceId, 30, e.ExamId
    FROM Exam e
    WHERE e.ExamCode LIKE N'DEMO-%'
      AND NOT EXISTS (
          SELECT 1 FROM ExamSection es
          WHERE es.ExamId = e.ExamId AND es.SectionType = N'Thực hành trong hình'
      );

    -- ========================================================
    -- 7. NHẬT KÝ THAO TÁC CHO MÀN AUDIT
    -- ========================================================

    DELETE FROM Audit WHERE Details LIKE N'#MS_LARGE#%';

    DECLARE @ManagerUserId INT =
        (SELECT UserId FROM [User] WHERE Username = N'qly123');
    DECLARE @AuditIndex INT = 1;

    WHILE @AuditIndex <= 60
    BEGIN
        INSERT INTO Audit
            (UserId, Action, [Reason], EntityName, EntityId,
             OldValue, NewValue, Details, CreatedAt)
        VALUES
            (CASE WHEN @AuditIndex % 3 = 0 THEN @PoliceUserId ELSE @ManagerUserId END,
             CASE @AuditIndex % 5
                WHEN 0 THEN N'APPROVE'
                WHEN 1 THEN N'SEND_POLICE'
                WHEN 2 THEN N'REJECT'
                WHEN 3 THEN N'IMPORT'
                ELSE N'CREATE_EXAM'
             END,
             CASE WHEN @AuditIndex % 5 = 2
                  THEN N'Hồ sơ chưa khớp dữ liệu đối chiếu'
                  ELSE N'Thao tác dữ liệu demo' END,
             CASE WHEN @AuditIndex % 2 = 0 THEN N'ExamRegistration' ELSE N'ExamDates' END,
             CONVERT(NVARCHAR(20), @AuditIndex),
             N'PENDING',
             CASE WHEN @AuditIndex % 5 = 2 THEN N'REJECTED' ELSE N'APPROVED' END,
             N'#MS_LARGE# Nhật ký demo số ' + CONVERT(NVARCHAR(10), @AuditIndex),
             DATEADD(HOUR, -(@AuditIndex * 9), SYSDATETIME()));

        SET @AuditIndex += 1;
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- ============================================================
-- KẾT QUẢ SAU KHI SEED
-- ============================================================

SELECT N'Tài khoản Police Staff' AS N'Nhóm', Username, Email, IsActive
FROM [User] WHERE Username = N'police123';

SELECT RegistrationStatus, COUNT(*) AS SoLuong
FROM ExamRegistration
GROUP BY RegistrationStatus
ORDER BY RegistrationStatus;

SELECT [Status], PoliceStatus, COUNT(*) AS SoNgayDuKien
FROM ExamDates
GROUP BY [Status], PoliceStatus
ORDER BY [Status], PoliceStatus;

SELECT PoliceStatus, COUNT(*) AS SoThiSinh
FROM RegistrationDates
GROUP BY PoliceStatus
ORDER BY PoliceStatus;

SELECT YEAR(ExamDate) AS Nam, MONTH(ExamDate) AS Thang,
       COUNT(*) AS SoKyThi
FROM Exam
GROUP BY YEAR(ExamDate), MONTH(ExamDate)
ORDER BY Nam, Thang;

SELECT COUNT(*) AS TongCandidate FROM Candidate;
SELECT COUNT(*) AS TongExamResult FROM ExamResult;
SELECT COUNT(*) AS TongAudit FROM Audit;
GO
