-- Migration: tách phân phòng (AllocatedExamAreaId) khỏi gán máy thi (ExamDeviceId)
-- Chạy trên DB đã deploy (ví dụ DLEM_DB_2) sau khi cập nhật code.

USE DLEM_DB_2;
GO

IF COL_LENGTH('ExamEnrollment', 'AllocatedExamAreaId') IS NULL
BEGIN
    ALTER TABLE ExamEnrollment
        ADD AllocatedExamAreaId INT NULL REFERENCES ExamArea(ExamAreaId);
END
GO

-- Backfill phòng từ máy đã gán trước đây (nếu có)
UPDATE ee
SET ee.AllocatedExamAreaId = ed.ExamAreaId
FROM ExamEnrollment ee
INNER JOIN ExamDevice ed ON ed.ExamDeviceId = ee.ExamDeviceId
WHERE ee.AllocatedExamAreaId IS NULL;
GO
