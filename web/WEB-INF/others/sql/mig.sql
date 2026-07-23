USE DLEM_DB_2;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('[User]') AND name = 'MustChangePassword')
    ALTER TABLE [User] ADD MustChangePassword BIT NOT NULL DEFAULT 0;
GO
