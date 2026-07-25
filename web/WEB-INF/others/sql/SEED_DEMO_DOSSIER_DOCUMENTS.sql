USE DLEM_DB_2;
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRANSACTION;

DECLARE @DemoDocuments TABLE (
    DocumentType NVARCHAR(100) NOT NULL,
    DocumentUrl NVARCHAR(500) NOT NULL
);

INSERT INTO @DemoDocuments (DocumentType, DocumentUrl)
VALUES
    (N'Ảnh chân dung 3x4',                  N'/uploads/demo-dossier/portrait.jpg'),
    (N'Căn cước công dân (mặt trước)',      N'/uploads/demo-dossier/id-front.jpg'),
    (N'Căn cước công dân (mặt sau)',        N'/uploads/demo-dossier/id-back.jpg'),
    (N'Giấy khám sức khỏe',                 N'/uploads/demo-dossier/health-certificate.jpg');

-- Chỉ áp dụng cho tài khoản Registrant demo của hệ thống.
UPDATE d
SET d.DocumentUrl = source.DocumentUrl,
    d.Notes = N'Tài liệu mô phỏng phục vụ kiểm thử'
FROM Document d
JOIN DocumentType dt ON dt.DocumentTypeId = d.DocumentTypeId
JOIN @DemoDocuments source ON source.DocumentType = dt.[Type]
JOIN Profile p ON p.ProfileId = d.ProfileId
JOIN [User] u ON u.UserId = p.UserId
JOIN [Role] r ON r.RoleId = u.RoleId
WHERE r.RoleName = N'Người đăng ký thi'
  AND u.Email LIKE N'%@laivui.local';

INSERT INTO Document (DocumentTypeId, DocumentUrl, Notes, ProfileId)
SELECT dt.DocumentTypeId,
       source.DocumentUrl,
       N'Tài liệu mô phỏng phục vụ kiểm thử',
       p.ProfileId
FROM Profile p
JOIN [User] u ON u.UserId = p.UserId
JOIN [Role] r ON r.RoleId = u.RoleId
CROSS JOIN @DemoDocuments source
JOIN DocumentType dt ON dt.[Type] = source.DocumentType
WHERE r.RoleName = N'Người đăng ký thi'
  AND u.Email LIKE N'%@laivui.local'
  AND EXISTS (
      SELECT 1
      FROM ExamRegistration er
      WHERE er.ProfileId = p.ProfileId
  )
  AND NOT EXISTS (
      SELECT 1
      FROM Document existing
      WHERE existing.ProfileId = p.ProfileId
        AND existing.DocumentTypeId = dt.DocumentTypeId
  );

COMMIT TRANSACTION;

SELECT COUNT(DISTINCT p.ProfileId) AS DemoProfiles,
       COUNT(d.DocumentId) AS DemoDocuments
FROM Profile p
JOIN [User] u ON u.UserId = p.UserId
JOIN [Role] r ON r.RoleId = u.RoleId
LEFT JOIN Document d ON d.ProfileId = p.ProfileId
WHERE r.RoleName = N'Người đăng ký thi'
  AND u.Email LIKE N'%@laivui.local';
