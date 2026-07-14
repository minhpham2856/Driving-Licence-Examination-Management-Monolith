/*
  Cập nhật RegistrationStatus = Approved cho hồ sơ thí sinh (nguồn chính trên web).
  Không cần sửa marker #APPROVED# trong Document.Notes.

  Thay @Username trước khi chạy.
*/
USE DLEM_DB_2;
GO

DECLARE @Username NVARCHAR(100) = N'dung.pham';
DECLARE @ProfileId INT = (
    SELECT p.ProfileId
    FROM [User] u
    INNER JOIN Profile p ON p.UserId = u.UserId
    WHERE u.Username = @Username
);

IF @ProfileId IS NULL
BEGIN
    RAISERROR(N'Không tìm thấy ProfileId.', 16, 1);
    RETURN;
END

UPDATE ExamRegistration
SET RegistrationStatus = N'Approved',
    Notes = N'Ban quản lý đã duyệt hồ sơ tài liệu.'
WHERE ProfileId = @ProfileId
  AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected');

PRINT N'Đã đặt RegistrationStatus = Approved cho ProfileId = ' + CAST(@ProfileId AS NVARCHAR(20));
GO
