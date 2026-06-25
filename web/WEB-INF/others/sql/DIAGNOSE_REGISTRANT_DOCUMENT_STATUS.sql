/*
  Chẩn đoán trạng thái hồ sơ tài liệu thí sinh.
  UI đọc ExamRegistration.RegistrationStatus (Draft / Pending / Approved / Rejected).

  Chạy với user dung.pham (CCCD 001203012348).
*/
USE DLEM_DB_2;
GO

DECLARE @Username NVARCHAR(100) = N'dung.pham';

SELECT
    u.Username,
    p.ProfileId,
    p.FullName,
    p.GovernmentIdNumber AS CCCD
FROM [User] u
INNER JOIN Profile p ON p.UserId = u.UserId
WHERE u.Username = @Username;

DECLARE @ProfileId INT = (
    SELECT p.ProfileId
    FROM [User] u
    INNER JOIN Profile p ON p.UserId = u.UserId
    WHERE u.Username = @Username
);

PRINT N'--- ExamRegistration (NGUỒN CHÍNH cho trạng thái hồ sơ trên web) ---';
SELECT ExamRegistrationId, RegistrationStatus, Notes, LicenceId
FROM ExamRegistration
WHERE ProfileId = @ProfileId
  AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected');

PRINT N'--- Document (file upload — Notes chỉ metadata / lý do từ chối) ---';
SELECT
    DocumentId,
    DocumentType,
    LEFT(DocumentUrl, 80) AS DocumentUrl,
    LEFT(Notes, 80) AS Notes
FROM Document
WHERE ProfileId = @ProfileId
ORDER BY DocumentId;

GO
