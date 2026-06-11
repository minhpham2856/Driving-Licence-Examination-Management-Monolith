-- Thời hạn hoàn tất thanh toán SEPay (chờ thanh toán → hủy sau 15 phút).
USE DLEM_DB;
GO

IF COL_LENGTH('ExamRegistration', 'isCancelled') IS NULL
BEGIN
    ALTER TABLE ExamRegistration ADD isCancelled BIT NOT NULL CONSTRAINT DF_ExamRegistration_isCancelled DEFAULT 0;
END
GO

IF COL_LENGTH('Payment', 'paymentExpiresAt') IS NULL
BEGIN
    ALTER TABLE Payment ADD paymentExpiresAt DATETIME2 NULL;
END
GO

DECLARE @constraintName NVARCHAR(200);
SELECT @constraintName = cc.name
FROM sys.check_constraints cc
JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id
WHERE cc.parent_object_id = OBJECT_ID('Payment')
  AND c.name = 'paymentStatus';

IF @constraintName IS NOT NULL
BEGIN
    DECLARE @dropSql NVARCHAR(400) = N'ALTER TABLE Payment DROP CONSTRAINT ' + QUOTENAME(@constraintName);
    EXEC sp_executesql @dropSql;
END

ALTER TABLE Payment ADD CONSTRAINT CK_Payment_paymentStatus
    CHECK (paymentStatus IN ('Pending', 'Completed', 'Failed', 'Refunded', 'Cancelled'));
GO
