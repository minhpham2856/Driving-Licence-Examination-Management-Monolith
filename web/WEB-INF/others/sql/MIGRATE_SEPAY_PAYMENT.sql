-- Run on existing DLEM_DB before using SEPay payment method.
-- Adds SEPay to Payment.paymentMethod check constraint.

DECLARE @constraintName NVARCHAR(200);
SELECT @constraintName = cc.name
FROM sys.check_constraints cc
JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id
WHERE cc.parent_object_id = OBJECT_ID('Payment')
  AND c.name = 'paymentMethod';

IF @constraintName IS NOT NULL
BEGIN
    DECLARE @dropSql NVARCHAR(400) = N'ALTER TABLE Payment DROP CONSTRAINT ' + QUOTENAME(@constraintName);
    EXEC sp_executesql @dropSql;
END

ALTER TABLE Payment ADD CONSTRAINT CK_Payment_paymentMethod
    CHECK (paymentMethod IN ('Cash', 'BankTransfer', 'SEPay'));
