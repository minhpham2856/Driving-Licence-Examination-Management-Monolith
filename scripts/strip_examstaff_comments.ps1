$ErrorActionPreference = "Stop"
$Root = "d:\study\SWPProject\Driving-Licence-Examination-Management-Monolith"

$Descriptions = @{
    "ExamRegistrationDAO.java" = "// DAO thao tac dang ky thi va thi sinh theo ca."
    "ExamRegistrationDAOImpl.java" = "// JDBC implementation cho ExamRegistrationDAO."
    "ExamSessionDAO.java" = "// DAO ca thi (Session)."
    "PaymentDAO.java" = "// DAO thanh toan le phi thi."
    "Db2CandidateSql.java" = "// SQL doc thi sinh (Candidate + ExamEnrollment)."
    "ExamEnrollmentMergeUtil.java" = "// Gop danh sach thi sinh theo CandidateId."
    "ExamStaffSidebarFilter.java" = "// Filter sidebar exam staff + dong bo sessionId."
    "ExamStaffViewHelper.java" = "// Helper chung: session, hang doi, sidebar exam staff."
    "UploadServlet.java" = "// Import DSTS va preview thi sinh."
    "AllocationServlet.java" = "// Phan bo thi sinh theo vong (ly thuyet / TH / duong)."
    "AllocationStageHelper.java" = "// Logic loc, sap xep, phan trang allocation."
    "AllocationPassRules.java" = "// Quy tac dat/truot va diem mau allocation."
    "DashboardServlet.java" = "// Dashboard tong quan exam staff."
    "ProcedureServlet.java" = "// Thu tuc thi: thu phi, ky ten."
    "CandidateCallServlet.java" = "// Goi thi sinh len ban thu tuc."
    "CandidateCallBoard.java" = "// Trang thai bang goi thi sinh (session)."
    "CandidateDossierServlet.java" = "// Ho so thi sinh chi tiet."
    "CandidatePhotoServlet.java" = "// Upload/chup anh thi sinh."
    "CandidatePhotoHelper.java" = "// Helper duong dan anh thi sinh."
    "SessionSelectServlet.java" = "// Chon ca thi tu sidebar."
    "SessionControlServlet.java" = "// Bat/dung ca thi."
    "PublicCallServlet.java" = "// Man hinh TV goi thi sinh (staff route)."
    "AuditServlet.java" = "// Nhat ky thao tac exam staff."
    "AuditExportServlet.java" = "// Xuat Excel nhat ky."
    "AuditExportLabels.java" = "// Nhan tieng Viet cho audit log."
    "AuditExcelExporter.java" = "// Ghi file Excel audit."
    "ReportServlet.java" = "// Bao cao ket qua thi."
    "ReportStatsHelper.java" = "// Thong ke bao cao."
    "ReportExportLabels.java" = "// Nhan xuat bao cao."
    "ReportExportStats.java" = "// Thong ke xuat bao cao."
    "ReportExcelExporter.java" = "// Ghi Excel bao cao."
    "ExaminerAllocationServlet.java" = "// Phan cong giam khao."
    "DossierFormHelper.java" = "// Helper form ho so in."
    "examstaff-sidebar.js" = "// Sidebar exam staff: chon ca thi."
    "allocation.js" = "// JS trang phan bo."
    "dashboard.js" = "// JS dashboard exam staff."
    "procedure.js" = "// JS trang thu tuc."
    "audit.js" = "// JS trang nhat ky."
    "candidatecall.js" = "// JS goi thi sinh."
    "examiner-allocation.js" = "// JS phan cong giam khao."
    "resolve-candidate-queue.jsp" = "<%-- Include hang doi thi sinh tu request. --%>"
    "sidebar-examstaff.jsp" = "<%-- Sidebar dieu huong exam staff. --%>"
    "header-examstaff.jsp" = "<%-- Header exam staff. --%>"
}

function Strip-CStyleComments([string]$text) {
    $sb = New-Object System.Text.StringBuilder
    $i = 0
    $n = $text.Length
    $inString = $null
    while ($i -lt $n) {
        $ch = $text[$i]
        if ($null -ne $inString) {
            [void]$sb.Append($ch)
            if ($ch -eq '\' -and ($i + 1) -lt $n) {
                [void]$sb.Append($text[$i + 1])
                $i += 2
                continue
            }
            if ($ch -eq $inString) { $inString = $null }
            $i++
            continue
        }
        if (($i + 1) -lt $n -and $text.Substring($i, 2) -eq '//') {
            $i += 2
            while ($i -lt $n -and $text[$i] -ne "`n") { $i++ }
            continue
        }
        if (($i + 1) -lt $n -and $text.Substring($i, 2) -eq '/*') {
            $i += 2
            while (($i + 1) -lt $n -and $text.Substring($i, 2) -ne '*/') { $i++ }
            $i = [Math]::Min($i + 2, $n)
            continue
        }
        if ($ch -eq '"' -or $ch -eq "'") {
            $inString = $ch
            [void]$sb.Append($ch)
            $i++
            continue
        }
        [void]$sb.Append($ch)
        $i++
    }
    return $sb.ToString()
}

function Strip-JspComments([string]$text) {
    $text = [regex]::Replace($text, '<%--.*?--%>', '', 'Singleline')
    $text = [regex]::Replace($text, '<!--.*?-->', '', 'Singleline')
    return $text
}

function Cleanup-BlankLines([string]$text) {
    $lines = $text -split "`r?`n"
    $cleaned = New-Object System.Collections.Generic.List[string]
    $blankRun = 0
    foreach ($line in $lines) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            $blankRun++
            if ($blankRun -le 2) { [void]$cleaned.Add('') }
        } else {
            $blankRun = 0
            [void]$cleaned.Add($line.TrimEnd())
        }
    }
    return (($cleaned -join "`n").TrimEnd() + "`n")
}

function Get-Description([System.IO.FileInfo]$file) {
    if ($Descriptions.ContainsKey($file.Name)) {
        return $Descriptions[$file.Name]
    }
    $stem = $file.BaseName
    switch ($file.Extension.ToLower()) {
        '.jsp' { return "<%-- Trang $stem exam staff. --%>" }
        '.js' { return "// JS $stem." }
        default { return "// $stem." }
    }
}

function Prepend-Description([string]$text, [System.IO.FileInfo]$file) {
    $desc = Get-Description $file
    $text = $text.TrimStart("`n")
    if ($file.Extension -eq '.java' -and $text.StartsWith('package ')) {
        return "$desc`n`n$text"
    }
    return "$desc`n$text"
}

$files = New-Object System.Collections.Generic.List[string]

$dirs = @(
    "$Root\src\java\controller\staff\exam",
    "$Root\web\views\staff\examstaff"
)
foreach ($dir in $dirs) {
    if (Test-Path $dir) {
        Get-ChildItem -Path $dir -Recurse -Include *.java,*.jsp,*.js | ForEach-Object { [void]$files.Add($_.FullName) }
    }
}

$singles = @(
    "$Root\src\java\filter\ExamStaffSidebarFilter.java",
    "$Root\src\java\dao\ExamRegistrationDAO.java",
    "$Root\src\java\dao\impl\ExamRegistrationDAOImpl.java",
    "$Root\src\java\dao\ExamSessionDAO.java",
    "$Root\src\java\dao\PaymentDAO.java",
    "$Root\src\java\dao\Db2CandidateSql.java",
    "$Root\src\java\util\ExamEnrollmentMergeUtil.java",
    "$Root\web\views\layout\sidebar-examstaff.jsp",
    "$Root\web\views\layout\header-examstaff.jsp",
    "$Root\web\assets\js\examstaff-sidebar.js",
    "$Root\web\assets\js\allocation.js",
    "$Root\web\assets\js\dashboard.js",
    "$Root\web\assets\js\procedure.js",
    "$Root\web\assets\js\audit.js",
    "$Root\web\assets\js\candidatecall.js",
    "$Root\web\assets\js\examiner-allocation.js"
)
foreach ($p in $singles) {
    if (Test-Path $p) { [void]$files.Add($p) }
}

$unique = $files | Select-Object -Unique | Sort-Object
$changed = 0
foreach ($path in $unique) {
    $file = Get-Item $path
    $original = [System.IO.File]::ReadAllText($path)
    $text = $original
    $ext = $file.Extension.ToLower()
    if ($ext -in '.java', '.js') {
        $text = Strip-CStyleComments $text
    }
    if ($ext -eq '.jsp') {
        $text = Strip-JspComments $text
        $text = Strip-CStyleComments $text
    }
    $text = Cleanup-BlankLines $text
    $text = Prepend-Description $text $file
    if ($text -ne $original) {
        [System.IO.File]::WriteAllText($path, $text, [System.Text.UTF8Encoding]::new($false))
        $changed++
        Write-Output $file.FullName.Substring($Root.Length + 1)
    }
}
Write-Output "Updated $changed files"
