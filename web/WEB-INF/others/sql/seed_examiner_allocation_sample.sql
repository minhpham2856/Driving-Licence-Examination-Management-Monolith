-- Bổ sung dữ liệu mẫu cho Phân bổ giám khảo (idempotent — chạy nhiều lần an toàn)
USE DLEM_DB_2;
GO

-- 1. Giám khảo mẫu (nếu DB trống)
IF NOT EXISTS (SELECT 1 FROM [Role] WHERE RoleName = N'Examiner')
    INSERT INTO [Role] (RoleName) VALUES (N'Examiner');
GO

IF NOT EXISTS (SELECT 1 FROM [User] WHERE Username = N'examiner_tung')
BEGIN
    INSERT INTO [User] (Username, Email, PasswordHash, RoleId, [Status])
    VALUES (N'examiner_tung', N'tung.nguyen@pc08a.com', N'login123',
            (SELECT RoleId FROM [Role] WHERE RoleName = N'Examiner'), 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM [User] WHERE Username = N'examiner_lan')
BEGIN
    INSERT INTO [User] (Username, Email, PasswordHash, RoleId, [Status])
    VALUES (N'examiner_lan', N'lan.tran@pc08a.com', N'login123',
            (SELECT RoleId FROM [Role] WHERE RoleName = N'Examiner'), 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM Profile p JOIN [User] u ON u.UserId = p.UserId WHERE u.Username = N'examiner_tung')
BEGIN
    INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId)
    SELECT N'Nguyễn Văn Tùng', '1985-03-15', N'0901234567', N'Male', N'001085012345', N'Hà Nội', UserId
    FROM [User] WHERE Username = N'examiner_tung';
END
GO

IF NOT EXISTS (SELECT 1 FROM Profile p JOIN [User] u ON u.UserId = p.UserId WHERE u.Username = N'examiner_lan')
BEGIN
    INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId)
    SELECT N'Trần Thị Lan', '1988-07-22', N'0912345678', N'Female', N'001088076543', N'Hà Nội', UserId
    FROM [User] WHERE Username = N'examiner_lan';
END
GO

-- 2. Phòng thi mẫu
IF NOT EXISTS (SELECT 1 FROM ExamArea WHERE AreaName = N'Phòng LT 1')
    INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location])
    VALUES (N'Phòng LT 1', N'Room', 10, N'Tầng 2, Toà B');
GO

IF NOT EXISTS (SELECT 1 FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')
    INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location])
    VALUES (N'Sân thi Ô tô 1', N'Ground', 10, N'Sân thi 2');
GO

IF NOT EXISTS (SELECT 1 FROM ExamArea WHERE AreaName = N'Đường trường 1')
    INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location])
    VALUES (N'Đường trường 1', N'Route', 8, N'Khu vực thi đường trường');
GO

-- 3. Gắn phòng cho ca chưa có Session_ExamArea (theo tên ca / loại thi)
INSERT INTO Session_ExamArea (SessionId, ExamAreaId)
SELECT s.SessionId, ea.ExamAreaId
FROM [Session] s
CROSS APPLY (
    SELECT TOP 1 a.ExamAreaId
    FROM ExamArea a
    WHERE (
        (s.SessionName LIKE N'%Lý thuyết%' OR s.SessionName LIKE N'%Ly thuyet%')
        AND a.AreaName = N'Phòng LT 1'
    ) OR (
        (s.SessionName LIKE N'%Sa hình%' OR s.SessionName LIKE N'%Sa hinh%' OR s.SessionName LIKE N'%Thực hành%')
        AND a.AreaName = N'Sân thi Ô tô 1'
    ) OR (
        (s.SessionName LIKE N'%Đường%' OR s.SessionName LIKE N'%Duong%')
        AND a.AreaName = N'Đường trường 1'
    )
    ORDER BY a.ExamAreaId
) ea
WHERE NOT EXISTS (
    SELECT 1 FROM Session_ExamArea sea WHERE sea.SessionId = s.SessionId
);
GO

PRINT N'Đã đồng bộ giám khảo + Session_ExamArea mẫu.';
GO
