-- Tùy chọn Gmail trên trang Cài đặt thí sinh (chạy một lần trên DLEM_DB_2)
IF COL_LENGTH(N'[User]', N'NotifyExamResultsGmail') IS NULL
BEGIN
    ALTER TABLE [User] ADD NotifyExamResultsGmail BIT NOT NULL
        CONSTRAINT DF_User_NotifyExamResultsGmail DEFAULT 1;
END
GO

IF COL_LENGTH(N'[User]', N'NotifyPasswordChangeGmail') IS NULL
BEGIN
    ALTER TABLE [User] ADD NotifyPasswordChangeGmail BIT NOT NULL
        CONSTRAINT DF_User_NotifyPasswordChangeGmail DEFAULT 1;
END
GO
