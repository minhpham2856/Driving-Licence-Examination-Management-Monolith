-- Cho phép một giám khảo phân công ở nhiều kỳ thi khác nhau.
-- Gỡ UNIQUE chỉ trên ExaminerId (giữ UNIQUE (SessionId, ExaminerId)).

USE DLEM_DB_2;
GO

DECLARE @sql NVARCHAR(MAX);

SELECT @sql = 'ALTER TABLE ExaminerSchedule DROP CONSTRAINT ' + QUOTENAME(kc.name)
FROM sys.key_constraints kc
INNER JOIN (
    SELECT ic.object_id, ic.index_id
    FROM sys.index_columns ic
    INNER JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
    WHERE ic.object_id = OBJECT_ID(N'dbo.ExaminerSchedule')
    GROUP BY ic.object_id, ic.index_id
    HAVING COUNT(*) = 1 AND MAX(c.name) = N'ExaminerId'
) single_col ON single_col.object_id = kc.parent_object_id
    AND single_col.index_id = kc.unique_index_id
WHERE kc.type = 'UQ';

IF @sql IS NOT NULL
BEGIN
    PRINT @sql;
    EXEC sp_executesql @sql;
END
GO
