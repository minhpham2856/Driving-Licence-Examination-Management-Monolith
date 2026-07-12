function Replace-InFile {
    param([string]$FilePath, [string]$Pattern, [string]$Replacement)
    $content = Get-Content $FilePath -Raw
    if ($content -match $Pattern) {
        $newContent = $content -replace $Pattern, $Replacement
        Set-Content -Path $FilePath -Value $newContent -Encoding UTF8
        Write-Output "Updated $FilePath"
    }
}

Get-ChildItem -Path src\java -Filter "*.java" -Recurse | ForEach-Object {
    $f = $_.FullName
    Replace-InFile $f '\.getDisplayName\(\)' '.getValue()'
    Replace-InFile $f '\.getCode\(\)' '.getValue()'
    Replace-InFile $f 'PaymentStatus\.sqlInClause\(\)' '"N''Ho?n t?t'', N''Paid''"'
    Replace-InFile $f 'PaymentStatus\.isCompleted\((.*?)\)' '(shared.enums.PaymentStatus.COMPLETED.getValue().equalsIgnoreCase() || "Paid".equalsIgnoreCase())'
    Replace-InFile $f 'ExamSessionStatus\.canStart\((.*?)\)' 'shared.enums.ExamSessionStatus.CHO_THI.getValue().equals()'
    Replace-InFile $f 'ExamSessionStatus\.isInProgress\((.*?)\)' 'shared.enums.ExamSessionStatus.DANG_DIEN_RA.getValue().equals()'
    Replace-InFile $f 'AuditEntity\.resolveLabel\((.*?)\)' ''
    Replace-InFile $f 'examstaff\.util\.AuditLogHelper\.resolveEntityName\((.*?), (.*?)\)' '( + " " + )'
    Replace-InFile $f 'examstaff\.util\.AuditLogHelper\.normalizeAction\((.*?)\)' ''
    Replace-InFile $f 'EXAM_NOT_FOUND_PREFIX\.formatExamNotFound\((.*?)\)' 'EXAM_NOT_FOUND_PREFIX.getValue() + '
    Replace-InFile $f '\.getExamTypeId\(\)' '.ordinal()'
    Replace-InFile $f 'shared\.enums\.ExamSection\.THUC_HANH_TREN_DUONG\.getValue\(\)' '"Th?c h?nh tr?n ???ng"'
    Replace-InFile $f 'ExamSection\.THUC_HANH_TREN_DUONG\.ordinal\(\)' '2'
}
