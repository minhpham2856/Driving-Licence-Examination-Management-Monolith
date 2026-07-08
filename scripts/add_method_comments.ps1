$ErrorActionPreference = "Stop"
$Root = "d:\study\SWPProject\Driving-Licence-Examination-Management-Monolith"

$MethodLabels = @{
    "getById" = "Lay dang ky theo id"
    "findById" = "Lay model dang ky theo id"
    "getBySessionAndSbd" = "Lay thi sinh theo ca va SBD"
    "getCandidatesBySession" = "Danh sach thi sinh theo ca"
    "getCandidatesByExam" = "Danh sach thi sinh theo ngay thi"
    "getByExamAndSbd" = "Lay thi sinh theo ngay thi va SBD"
    "updatePresent" = "Cap nhat co mat / vang"
    "updatePayment" = "Cap nhat trang thai thanh toan"
    "updateComputer" = "Gan may tinh cho thi sinh"
    "updateAllocatedRoom" = "Cap nhat phong da phan bo"
    "updateDevice" = "Gan thiet bi / xe cho thi sinh"
    "updateScores" = "Cap nhat diem ly thuyet va thuc hanh"
    "updateTheoryCorrectCount" = "Cap nhat so cau dung ly thuyet"
    "updateRoadScore" = "Cap nhat diem duong truong"
    "updateProfile" = "Cap nhat ho so co ban"
    "updateExaminerProfile" = "Cap nhat ho so day du (giam khao)"
    "updatePhoto" = "Cap nhat duong dan anh"
    "clearCompletedPayments" = "Xoa giao dich thanh toan da hoan tat"
    "insert" = "Them dang ky thi (qua Profile)"
    "insertFromDstsImport" = "Import DSTS vao Candidate + ExamEnrollment"
    "getAllCandidates" = "Lay tat ca dang ky"
    "markAbsent" = "Danh dau vang mat"
    "clearAbsentMarking" = "Huy danh dau vang"
    "findCandidateIdByProfileAndSession" = "Tim CandidateId theo Profile va ca"
    "findCandidateIdByGovIdAndSession" = "Tim CandidateId theo CCCD va ca"
    "applyScoreDeductions" = "Ap dung khoan tru diem"
    "adjustScoreDeductionOccurrence" = "Dieu chinh so lan tru diem"
    "finalizeScoreEntry" = "Chot diem va trang thai phan thi"
    "findAppliedScoreDeductions" = "Lay khoan tru diem da ap dung"
    "markSuspended" = "Danh dau dinh chi thi"
    "undoSuspension" = "Huy dinh chi thi"
    "syncSectionStatusesForSession" = "Dong bo trang thai phan thi theo ca"
    "markSignaturePrinted" = "Danh dau da in chu ky"
    "completeSection" = "Hoan tat phan thi / thu tuc"
    "doGet" = "Xu ly yeu cau GET"
    "doPost" = "Xu ly yeu cau POST"
}

function Remove-FileHeaderDescription([string]$text, [string]$ext) {
    if ($ext -eq ".java") {
        return [regex]::Replace($text, '^\s*//[^\r\n]*\r?\n\r?\n(?=package )', '')
    }
    if ($ext -eq ".jsp") {
        return [regex]::Replace($text, '^\s*<%--[^%]*--%>\s*\r?\n', '')
    }
    if ($ext -eq ".js") {
        return [regex]::Replace($text, '^\s*//[^\r\n]*\r?\n', '')
    }
    return $text
}

function Remove-AllLineComments([string]$text) {
    $lines = $text -split "`r?`n"
    $out = New-Object System.Collections.Generic.List[string]
    foreach ($line in $lines) {
        if ($line -match '^\s*//') { continue }
        [void]$out.Add($line)
    }
    return ($out -join "`n")
}

function Split-CamelCase([string]$name) {
    return [regex]::Replace($name, '([a-z])([A-Z])', '$1 $2').ToLower()
}

function Describe-Method([string]$name) {
    if ($MethodLabels.ContainsKey($name)) { return $MethodLabels[$name] }
    if ($name -match '^getBy(.+)$') { return "Lay theo $(Split-CamelCase $matches[1])" }
    if ($name -match '^getAll(.+)$') { return "Lay tat ca $(Split-CamelCase $matches[1])" }
    if ($name -match '^get(.+)$') { return "Lay $(Split-CamelCase $matches[1])" }
    if ($name -match '^find(.+)$') { return "Tim $(Split-CamelCase $matches[1])" }
    if ($name -match '^update(.+)$') { return "Cap nhat $(Split-CamelCase $matches[1])" }
    if ($name -match '^insert(.+)$') { return "Them $(Split-CamelCase $matches[1])" }
    if ($name -match '^mark(.+)$') { return "Danh dau $(Split-CamelCase $matches[1])" }
    if ($name -match '^clear(.+)$') { return "Xoa / reset $(Split-CamelCase $matches[1])" }
    if ($name -match '^bind(.+)$') { return "Gan thuoc tinh $(Split-CamelCase $matches[1])" }
    if ($name -match '^resolve(.+)$') { return "Xac dinh $(Split-CamelCase $matches[1])" }
    if ($name -match '^refresh(.+)$') { return "Lam moi $(Split-CamelCase $matches[1])" }
    if ($name -match '^load(.+)$') { return "Tai $(Split-CamelCase $matches[1])" }
    if ($name -match '^build(.+)$') { return "Tao $(Split-CamelCase $matches[1])" }
    if ($name -match '^is([A-Z].+)$') { return "Kiem tra $(Split-CamelCase $matches[1])" }
    if ($name -match '^has([A-Z].+)$') { return "Co $(Split-CamelCase $matches[1]) hay khong" }
    return (Split-CamelCase $name)
}

function Get-Indent([string]$line) {
    if ($line -match '^(\s*)') { return $matches[1] }
    return '    '
}

function Is-MethodDeclarationLine([string]$line) {
    if ($line -notmatch '\(') { return $false }
    if ($line -match '^\s*return\b') { return $false }
    if ($line -match '\.') { return $false }
    if ($line -match '\b(if|for|while|switch|catch|throw|new)\s*[\(;]') { return $false }
    if ($line -match '=\s*[^=]') { return $false }
    if ($line -match '^\s*@\w+') { return $false }

    if ($line -match '^\s+(?:(?:public|private|protected)\s+)(?:static\s+)?(?:final\s+)?(?:synchronized\s+)?[\w<>,\?\[\]\s]+\s+(\w+)\s*\(') {
        return $true
    }
    if ($line -match '^\s{4}(?:static\s+)?[\w<>,\?\[\]\s]+\s+(\w+)\s*\([^)]*\)\s*;\s*$') {
        return $true
    }
    return $false
}

function Extract-MethodName([string]$signature) {
    $m = [regex]::Match($signature, '\b(\w+)\s*\([^)]*\)\s*(?:;|\{|throws)')
    if ($m.Success) { return $m.Groups[1].Value }
    $m2 = [regex]::Match($signature, '\b(\w+)\s*\(')
    if ($m2.Success) { return $m2.Groups[1].Value }
    return $null
}

function Collect-MethodDeclarations([string[]]$lines) {
    $decls = New-Object System.Collections.Generic.List[object]
    $i = 0
    while ($i -lt $lines.Count) {
        $line = $lines[$i]
        if (-not (Is-MethodDeclarationLine $line)) {
            $i++
            continue
        }

        $sig = $line
        $j = $i
        while ($sig -notmatch '\)' -and ($j + 1) -lt $lines.Count) {
            $j++
            $sig += ' ' + $lines[$j].Trim()
        }
        while ($sig -notmatch '(;|\{)\s*$' -and ($j + 1) -lt $lines.Count) {
            $next = $lines[$j + 1].Trim()
            if ($next.StartsWith('@')) { break }
            if ($next -match '^(if|for|while|return|throw)') { break }
            $j++
            $sig += ' ' + $next
        }

        $name = Extract-MethodName $sig
        if ($null -ne $name -and $name -notin @('if', 'for', 'while', 'switch', 'catch', 'class', 'interface', 'enum')) {
            [void]$decls.Add([pscustomobject]@{ Index = $i; Name = $name; Indent = (Get-Indent $line) })
        }
        $i++
    }
    return $decls
}

function Add-MethodComments([string]$text) {
    $lines = [string[]]($text -split "`r?`n")
    $decls = Collect-MethodDeclarations $lines
    if ($decls.Count -eq 0) { return $text }

    $out = New-Object System.Collections.Generic.List[string]
    $declQueue = [System.Collections.Queue]::new()
    foreach ($d in $decls) { [void]$declQueue.Enqueue($d) }

    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($declQueue.Count -gt 0 -and $declQueue.Peek().Index -eq $i) {
            $d = $declQueue.Dequeue()
            $insert = $i
            while ($insert -gt 0 -and $lines[$insert - 1].Trim().StartsWith('@')) { $insert-- }
            $comment = "$($d.Indent)// $(Describe-Method $d.Name)"
            if ($out.Count -le $insert) {
                [void]$out.Add($comment)
            } else {
                $out.Insert($insert, $comment)
            }
        }
        [void]$out.Add($lines[$i])
    }
    return ($out -join "`n")
}

function Cleanup-BlankLines([string]$text) {
    $lines = $text -split "`r?`n"
    $cleaned = New-Object System.Collections.Generic.List[string]
    $blankRun = 0
    foreach ($line in $lines) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            $blankRun++
            if ($blankRun -le 1) { [void]$cleaned.Add('') }
        } else {
            $blankRun = 0
            [void]$cleaned.Add($line.TrimEnd())
        }
    }
    return (($cleaned -join "`n").TrimEnd() + "`n")
}

function Collect-JavaFiles {
    $files = New-Object System.Collections.Generic.List[string]
    $items = @(
        "$Root\src\java\controller\staff\exam",
        "$Root\src\java\filter\ExamStaffSidebarFilter.java",
        "$Root\src\java\dao\ExamRegistrationDAO.java",
        "$Root\src\java\dao\impl\ExamRegistrationDAOImpl.java",
        "$Root\src\java\dao\ExamSessionDAO.java",
        "$Root\src\java\dao\PaymentDAO.java",
        "$Root\src\java\dao\Db2CandidateSql.java",
        "$Root\src\java\util\ExamEnrollmentMergeUtil.java"
    )
    foreach ($item in $items) {
        if (Test-Path $item -PathType Container) {
            Get-ChildItem $item -Filter *.java -Recurse | ForEach-Object { [void]$files.Add($_.FullName) }
        } elseif (Test-Path $item) {
            [void]$files.Add((Resolve-Path $item).Path)
        }
    }
    return $files | Select-Object -Unique | Sort-Object
}

$changed = 0
foreach ($path in (Collect-JavaFiles)) {
    $original = [System.IO.File]::ReadAllText($path)
    $text = Remove-FileHeaderDescription $original ".java"
    $text = Remove-AllLineComments $text
    $text = Add-MethodComments $text
    $text = Cleanup-BlankLines $text
    if ($text -ne $original) {
        [System.IO.File]::WriteAllText($path, $text, [System.Text.UTF8Encoding]::new($false))
        $changed++
        Write-Output $path.Substring($Root.Length + 1)
    }
}

$jspFiles = @(
    "$Root\web\views\staff\examstaff",
    "$Root\web\views\layout\sidebar-examstaff.jsp",
    "$Root\web\views\layout\header-examstaff.jsp"
)
foreach ($item in $jspFiles) {
    if (Test-Path $item -PathType Container) {
        Get-ChildItem $item -Filter *.jsp -Recurse | ForEach-Object {
            $original = [System.IO.File]::ReadAllText($_.FullName)
            $text = Remove-FileHeaderDescription $original ".jsp"
            $text = Cleanup-BlankLines $text
            if ($text -ne $original) {
                [System.IO.File]::WriteAllText($_.FullName, $text, [System.Text.UTF8Encoding]::new($false))
                $changed++
                Write-Output $_.FullName.Substring($Root.Length + 1)
            }
        }
    } elseif (Test-Path $item) {
        $original = [System.IO.File]::ReadAllText($item)
        $text = Remove-FileHeaderDescription $original ".jsp"
        $text = Cleanup-BlankLines $text
        if ($text -ne $original) {
            [System.IO.File]::WriteAllText($item, $text, [System.Text.UTF8Encoding]::new($false))
            $changed++
            Write-Output $item.Substring($Root.Length + 1)
        }
    }
}

Write-Output "Updated $changed files"
