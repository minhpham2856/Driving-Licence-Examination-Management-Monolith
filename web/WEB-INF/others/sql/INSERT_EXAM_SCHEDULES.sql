/*
================================================================================
  TẠO LỊCH THI (ĐỢT THI + CA THI) — CHẠY RIÊNG, KHÔNG GẮN VÀO CHƯƠNG TRÌNH
================================================================================
  Database : DLEM_DB_2
  Mục đích : Thêm các đợt thi mới để thí sinh đăng ký trên cổng registrant.

  Điều kiện hiển thị trên cổng thí sinh (tham khảo RegistrantDAOImpl):
    - Exam.[Status]   IN (N'Open', N'Scheduled')
    - Session.[Status] IN (N'Open', N'Scheduled', N'InProgress')
    - Exam.ExamDate   >= ngày hiện tại

  Yêu cầu trước khi chạy:
    - Đã có bảng Licence, ExamSection, ExamArea (chạy DDL + DML gốc trước).
    - Script dùng IF NOT EXISTS theo ExamCode → chạy lại an toàn, không trùng mã.

  Cách chạy (SSMS / sqlcmd):
    USE DLEM_DB_2;
    GO
    -- mở và Execute toàn bộ file này
================================================================================
*/

USE DLEM_DB_2;
GO

SET NOCOUNT ON;
GO

/* --------------------------------------------------------------------------
   1. ĐỢT THI HẠNG B (B2 trên UI) — 01/07/2026
   Ca: Lý thuyết → Sa hình → Đường trường
   -------------------------------------------------------------------------- */
IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamCode = N'EX-B-20260701')
BEGIN
    INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId)
    VALUES (
        N'EX-B-20260701',
        '2026-07-01 07:00:00',
        N'Trung tâm Sát hạch Lái Vui - Hà Nội',
        N'Open',
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM [Session] WHERE SessionName = N'Ca 01/07 - Lý thuyết B2')
BEGIN
    INSERT INTO [Session] (SessionName, StartTime, EndTime, [Status], ExamId)
    VALUES
    (N'Ca 01/07 - Lý thuyết B2',   '2026-07-01 07:30:00', '2026-07-01 09:00:00', N'Open',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260701')),
    (N'Ca 01/07 - Sa hình B2',     '2026-07-01 09:30:00', '2026-07-01 11:30:00', N'Scheduled',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260701')),
    (N'Ca 01/07 - Đường trường B2','2026-07-01 13:00:00', '2026-07-01 16:00:00', N'Scheduled',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260701'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamSection ses
    INNER JOIN [Session] s ON s.SessionId = ses.SessionId
    WHERE s.SessionName = N'Ca 01/07 - Lý thuyết B2'
)
BEGIN
    INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 01/07 - Lý thuyết B2'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 01/07 - Sa hình B2'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 01/07 - Đường trường B2'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamArea sea
    INNER JOIN [Session] s ON s.SessionId = sea.SessionId
    WHERE s.SessionName = N'Ca 01/07 - Lý thuyết B2'
)
BEGIN
    INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 01/07 - Lý thuyết B2'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 01/07 - Sa hình B2'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 01/07 - Đường trường B2'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1'));
END;
GO

/* --------------------------------------------------------------------------
   2. ĐỢT THI HẠNG B (B2) — 15/07/2026
   -------------------------------------------------------------------------- */
IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamCode = N'EX-B-20260715')
BEGIN
    INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId)
    VALUES (
        N'EX-B-20260715',
        '2026-07-15 07:00:00',
        N'Trung tâm Sát hạch Lái Vui - Hà Nội',
        N'Scheduled',
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B')
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM [Session] WHERE SessionName = N'Ca 15/07 - Lý thuyết B2')
BEGIN
    INSERT INTO [Session] (SessionName, StartTime, EndTime, [Status], ExamId)
    VALUES
    (N'Ca 15/07 - Lý thuyết B2',   '2026-07-15 07:30:00', '2026-07-15 09:00:00', N'Open',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260715')),
    (N'Ca 15/07 - Sa hình B2',     '2026-07-15 09:30:00', '2026-07-15 11:30:00', N'Scheduled',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260715')),
    (N'Ca 15/07 - Đường trường B2','2026-07-15 13:00:00', '2026-07-15 16:00:00', N'Scheduled',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B-20260715'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamSection ses
    INNER JOIN [Session] s ON s.SessionId = ses.SessionId
    WHERE s.SessionName = N'Ca 15/07 - Lý thuyết B2'
)
BEGIN
    INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 15/07 - Lý thuyết B2'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 15/07 - Sa hình B2'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 15/07 - Đường trường B2'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamArea sea
    INNER JOIN [Session] s ON s.SessionId = sea.SessionId
    WHERE s.SessionName = N'Ca 15/07 - Lý thuyết B2'
)
BEGIN
    INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 15/07 - Lý thuyết B2'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 15/07 - Sa hình B2'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 15/07 - Đường trường B2'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1'));
END;
GO

/* --------------------------------------------------------------------------
   3. ĐỢT THI HẠNG A1 — 05/07/2026
   Ca: Lý thuyết + Thực hành
   -------------------------------------------------------------------------- */
IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamCode = N'EX-A1-20260705')
BEGIN
    INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId)
    VALUES (
        N'EX-A1-20260705',
        '2026-07-05 07:00:00',
        N'Trung tâm Sát hạch Lái Vui - Hà Nội',
        N'Open',
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'A1')
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM [Session] WHERE SessionName = N'Ca 05/07 - Lý thuyết A1')
BEGIN
    INSERT INTO [Session] (SessionName, StartTime, EndTime, [Status], ExamId)
    VALUES
    (N'Ca 05/07 - Lý thuyết A1', '2026-07-05 07:30:00', '2026-07-05 09:00:00', N'Open',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-A1-20260705')),
    (N'Ca 05/07 - Thực hành A1', '2026-07-05 09:30:00', '2026-07-05 11:30:00', N'Scheduled',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-A1-20260705'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamSection ses
    INNER JOIN [Session] s ON s.SessionId = ses.SessionId
    WHERE s.SessionName = N'Ca 05/07 - Lý thuyết A1'
)
BEGIN
    INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 05/07 - Lý thuyết A1'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 05/07 - Thực hành A1'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamArea sea
    INNER JOIN [Session] s ON s.SessionId = sea.SessionId
    WHERE s.SessionName = N'Ca 05/07 - Lý thuyết A1'
)
BEGIN
    INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 05/07 - Lý thuyết A1'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 05/07 - Thực hành A1'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi A1'));
END;
GO

/* --------------------------------------------------------------------------
   4. ĐỢT THI HẠNG B1 — 12/07/2026
   -------------------------------------------------------------------------- */
IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamCode = N'EX-B1-20260712')
BEGIN
    INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId)
    VALUES (
        N'EX-B1-20260712',
        '2026-07-12 07:00:00',
        N'Trung tâm Sát hạch Lái Vui - Đà Nẵng',
        N'Open',
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'B1')
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM [Session] WHERE SessionName = N'Ca 12/07 - Lý thuyết B1')
BEGIN
    INSERT INTO [Session] (SessionName, StartTime, EndTime, [Status], ExamId)
    VALUES
    (N'Ca 12/07 - Lý thuyết B1', '2026-07-12 07:30:00', '2026-07-12 09:00:00', N'Open',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B1-20260712')),
    (N'Ca 12/07 - Thực hành B1', '2026-07-12 09:30:00', '2026-07-12 11:30:00', N'Scheduled',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-B1-20260712'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamSection ses
    INNER JOIN [Session] s ON s.SessionId = ses.SessionId
    WHERE s.SessionName = N'Ca 12/07 - Lý thuyết B1'
)
BEGIN
    INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 12/07 - Lý thuyết B1'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 12/07 - Thực hành B1'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Thực hành'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamArea sea
    INNER JOIN [Session] s ON s.SessionId = sea.SessionId
    WHERE s.SessionName = N'Ca 12/07 - Lý thuyết B1'
)
BEGIN
    INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 12/07 - Lý thuyết B1'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 1')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 12/07 - Thực hành B1'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1'));
END;
GO

/* --------------------------------------------------------------------------
   5. ĐỢT THI HẠNG C1 — 20/07/2026
   -------------------------------------------------------------------------- */
IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamCode = N'EX-C1-20260720')
BEGIN
    INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId)
    VALUES (
        N'EX-C1-20260720',
        '2026-07-20 07:00:00',
        N'Trung tâm Sát hạch Lái Vui - Hưng Yên',
        N'Scheduled',
        (SELECT LicenceId FROM Licence WHERE LicenceClass = N'C1')
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM [Session] WHERE SessionName = N'Ca 20/07 - Lý thuyết C1')
BEGIN
    INSERT INTO [Session] (SessionName, StartTime, EndTime, [Status], ExamId)
    VALUES
    (N'Ca 20/07 - Lý thuyết C1',   '2026-07-20 07:30:00', '2026-07-20 09:00:00', N'Open',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-C1-20260720')),
    (N'Ca 20/07 - Sa hình C1',     '2026-07-20 09:30:00', '2026-07-20 11:30:00', N'Scheduled',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-C1-20260720')),
    (N'Ca 20/07 - Đường trường C1','2026-07-20 13:00:00', '2026-07-20 16:00:00', N'Scheduled',
        (SELECT ExamId FROM Exam WHERE ExamCode = N'EX-C1-20260720'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamSection ses
    INNER JOIN [Session] s ON s.SessionId = ses.SessionId
    WHERE s.SessionName = N'Ca 20/07 - Lý thuyết C1'
)
BEGIN
    INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 20/07 - Lý thuyết C1'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Lý thuyết')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 20/07 - Sa hình C1'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 20/07 - Đường trường C1'),
        (SELECT ExamSectionId FROM ExamSection WHERE SectionName = N'Đường trường'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM Session_ExamArea sea
    INNER JOIN [Session] s ON s.SessionId = sea.SessionId
    WHERE s.SessionName = N'Ca 20/07 - Lý thuyết C1'
)
BEGIN
    INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 20/07 - Lý thuyết C1'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Phòng LT 2')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 20/07 - Sa hình C1'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Sân thi Ô tô 1')),
    ((SELECT SessionId FROM [Session] WHERE SessionName = N'Ca 20/07 - Đường trường C1'),
        (SELECT ExamAreaId FROM ExamArea WHERE AreaName = N'Đường trường 1'));
END;
GO

/* --------------------------------------------------------------------------
   6. KIỂM TRA KẾT QUẢ
   -------------------------------------------------------------------------- */
SELECT
    e.ExamCode,
    e.ExamDate,
    e.CentreName,
    e.[Status]       AS examStatus,
    l.LicenceClass,
    s.SessionName,
    s.StartTime,
    s.EndTime,
    s.[Status]       AS sessionStatus,
    es.SectionName,
    ea.AreaName
FROM Exam e
INNER JOIN Licence l ON l.LicenceId = e.LicenceId
INNER JOIN [Session] s ON s.ExamId = e.ExamId
LEFT JOIN Session_ExamSection ses ON ses.SessionId = s.SessionId
LEFT JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
LEFT JOIN (
    SELECT sea2.SessionId, MIN(sea2.ExamAreaId) AS ExamAreaId
    FROM Session_ExamArea sea2
    GROUP BY sea2.SessionId
) sea ON sea.SessionId = s.SessionId
LEFT JOIN ExamArea ea ON ea.ExamAreaId = sea.ExamAreaId
WHERE e.ExamCode IN (
    N'EX-B-20260701',
    N'EX-B-20260715',
    N'EX-A1-20260705',
    N'EX-B1-20260712',
    N'EX-C1-20260720'
)
ORDER BY e.ExamDate, s.StartTime, es.SectionName;
GO

PRINT N'Hoàn tất: đã tạo (hoặc bỏ qua nếu đã tồn tại) 5 đợt thi mới.';
GO
