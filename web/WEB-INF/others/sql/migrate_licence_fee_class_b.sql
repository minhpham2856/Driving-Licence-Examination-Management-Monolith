-- Bổ sung biểu phí thủ tục hạng B nếu DB đã seed trước khi có block Licence_Fee cho B.
-- Chạy trên DLEM_DB_2 (hoặc DB đang dùng). An toàn khi chạy lại: chỉ INSERT khi chưa có.

IF NOT EXISTS (
    SELECT 1
    FROM Licence_Fee lf
    INNER JOIN Licence l ON l.LicenceId = lf.LicenceId
    WHERE l.LicenceClass = N'B'
)
BEGIN
    INSERT INTO Licence_Fee (LicenceId, FeeId, Amount)
    SELECT l.LicenceId, f.FeeId, v.Amount
    FROM Licence l
    CROSS JOIN (VALUES
        (N'Học phí lý thuyết', 2200000.00),
        (N'Học phí thực hành', 9300000.00),
        (N'Lệ phí thi lý thuyết', 100000.00),
        (N'Lệ phí thi thực hành trong hình', 250000.00),
        (N'Lệ phí thi thực hành trên đường', 80000.00),
        (N'Lệ phí cấp GPLX (phôi PET)', 135000.00)
    ) v(FeeName, Amount)
    JOIN Fee f ON f.FeeName = v.FeeName
    WHERE l.LicenceClass = N'B';
END
GO
