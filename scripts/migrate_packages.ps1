# Migrate Java packages to match origin/main lowercase convention.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not (Test-Path "$Root\src\java\DAO")) {
    $Root = "d:\Code\Driving-Licence-Examination-Management-Monolith"
}
$Src = Join-Path $Root "src\java"
$Staging = Join-Path $Root "src\java_migrated"

$ModelPkg = @{
    "User" = "model.user"
    "Profile" = "model.user"
    "Role" = "model.user"
    "AuditLog" = "model.user"
    "ExamRegistration" = "model.exam"
    "ExamResult" = "model.exam"
    "ExamArea" = "model.exam"
    "ExamDevice" = "model.exam"
    "ExamSession" = "model.exam"
    "ExamComputer" = "model.exam"
    "SessionExamSectionInfo" = "model.exam"
    "SessionScheduleInfo" = "model.exam"
    "ExaminerPaperState" = "model.exam"
    "ExaminerAnswerStats" = "model.exam"
    "TheoryPaperAnswer" = "model.exam"
    "TheoryScore" = "model.exam"
    "PracticalScore" = "model.exam"
    "Payment" = "model.payment"
    "ScoreDeduction" = "model.candidate"
    "CandidateCall" = "model.candidate"
    "ManagingStaffApprovalView" = "model.staff"
    "StaffProcedureKpi" = "model.staff"
    "RegisterResult" = "model.common"
    "ProfileRegistrationSyncResult" = "model.registrant"
    "RegistrantDocumentView" = "model.registrant"
    "RegistrantDocumentSummary" = "model.registrant"
    "RegistrantMyExamRow" = "model.registrant"
    "RegistrantRegisteredExamRow" = "model.registrant"
    "RegistrantExamSessionOption" = "model.registrant"
    "RegistrantLicenceOption" = "model.registrant"
    "RegistrantProfileContext" = "model.registrant"
    "RegistrantProfileProgressStep" = "model.registrant"
    "RegistrantDashboardActivity" = "model.registrant"
    "RegistrantDashboardActionItem" = "model.registrant"
    "RegistrantFilterOption" = "model.registrant"
    "RegistrantTrackingLog" = "model.registrant"
    "RegistrantSectionRegistrationBlock" = "model.registrant"
    "RegistrantExamResultEmailData" = "model.registrant"
}

$SePayModels = @(
    "SePayCheckoutRequest", "SePayCheckoutSession", "SePayIpnEvent",
    "SePayIpnResult", "SePayPaymentException"
)

$LandingServlets = @("HomeServlet", "LicenseCategoriesServlet", "ProcessServlet")

$OldTopDirs = @("DAO", "Services", "Utils", "Models", "Controllers", "Filters", "Constants", "DBConnection", "Listeners")

function Get-TargetInfo([string]$OldRel) {
    $parts = $OldRel -split '/'
    switch ($parts[0]) {
        "DAO" {
            if ($parts.Count -eq 3 -and $parts[1] -eq "Impl") {
                return @{ Rel = "dao/impl/$($parts[2])"; Package = "dao.impl" }
            }
            return @{ Rel = "dao/$($parts[1])"; Package = "dao" }
        }
        "Services" {
            if ($parts.Count -eq 3 -and $parts[1] -eq "Impl") {
                return @{ Rel = "service/impl/$($parts[2])"; Package = "service.impl" }
            }
            return @{ Rel = "service/$($parts[1])"; Package = "service" }
        }
        "Utils" {
            if ($parts.Count -ge 4 -and $parts[1] -eq "payment" -and $parts[2] -eq "sepay") {
                return @{ Rel = "util/payment/sepay/$($parts[3])"; Package = "util.payment.sepay" }
            }
            $rel = "util/" + ($parts[1..($parts.Count - 1)] -join '/')
            $pkg = if ($parts.Count -eq 2) { "util" } else { "util." + ($parts[2..($parts.Count - 1)] -join '.').Replace('.java','') }
            if ($pkg -match '\.java$') { $pkg = $pkg -replace '\.java$','' }
            return @{ Rel = $rel; Package = "util" }
        }
        "Filters" {
            return @{ Rel = "filter/$($parts[1])"; Package = "filter" }
        }
        "DBConnection" {
            return @{ Rel = "dbconnection/$($parts[1])"; Package = "dbconnection" }
        }
        "Listeners" {
            return @{ Rel = "listener/$($parts[1])"; Package = "listener" }
        }
        "Constants" {
            return @{ Rel = "constant/$($parts[1])"; Package = "constant" }
        }
        "Models" {
            if ($parts.Count -ge 4 -and $parts[1] -eq "payment" -and $parts[2] -eq "sepay") {
                return @{ Rel = "model/payment/sepay/$($parts[3])"; Package = "model.payment.sepay" }
            }
            $cls = [System.IO.Path]::GetFileNameWithoutExtension($parts[1])
            if (-not $ModelPkg.ContainsKey($cls)) { throw "No model mapping for $OldRel" }
            $pkg = $ModelPkg[$cls]
            $pkgPath = $pkg -replace '\.', '/'
            return @{ Rel = "$pkgPath/$($parts[1])"; Package = $pkg }
        }
        "Controllers" {
            $name = [System.IO.Path]::GetFileNameWithoutExtension($parts[-1])
            if ($parts.Count -ge 4 -and $parts[1] -eq "Auth" -and $parts[2] -eq "Public" -and $LandingServlets -contains $name) {
                return @{ Rel = "controller/landing/$name"; Package = "controller.landing" }
            }
            if ($parts.Count -ge 3 -and $parts[1] -eq "Auth" -and $parts[2] -eq "Public") {
                return @{ Rel = "controller/auth/landing/$name"; Package = "controller.auth.landing" }
            }
            if ($parts[1] -eq "Registrant") {
                return @{ Rel = "controller/registrant/$name"; Package = "controller.registrant" }
            }
            if ($parts[1] -eq "ManagingStaff") {
                return @{ Rel = "controller/staff/managing/$name"; Package = "controller.staff.managing" }
            }
            if ($parts.Count -ge 3 -and $parts[1] -eq "Staff" -and $parts[2] -eq "ExamStaff") {
                return @{ Rel = "controller/staff/exam/$name"; Package = "controller.staff.exam" }
            }
            if ($parts[1] -eq "Examiner") {
                return @{ Rel = "controller/examiner/$name"; Package = "controller.examiner" }
            }
            if ($parts[1] -eq "Payment") {
                return @{ Rel = "controller/payment/$name"; Package = "controller.payment" }
            }
            if ($parts[1] -eq "Public") {
                return @{ Rel = "controller/staff/exam/$name"; Package = "controller.staff.exam" }
            }
            throw "Unknown controller path: $OldRel"
        }
        default { throw "Unknown source: $OldRel" }
    }
}

function Get-ImportReplacements {
    $reps = [System.Collections.Generic.List[object]]::new()
    foreach ($kv in ($ModelPkg.GetEnumerator() | Sort-Object { $_.Key.Length } -Descending)) {
        $reps.Add([PSCustomObject]@{ Old = "Models.$($kv.Key)"; New = "$($kv.Value).$($kv.Key)" })
    }
    foreach ($cls in $SePayModels) {
        $reps.Add([PSCustomObject]@{ Old = "Models.payment.sepay.$cls"; New = "model.payment.sepay.$cls" })
    }
    foreach ($s in $LandingServlets) {
        $reps.Add([PSCustomObject]@{ Old = "Controllers.Auth.Public.$s"; New = "controller.landing.$s" })
    }
    @(
        @("Controllers.Staff.ExamStaff", "controller.staff.exam"),
        @("Controllers.ManagingStaff", "controller.staff.managing"),
        @("Controllers.Auth.Public", "controller.auth.landing"),
        @("Controllers.Registrant", "controller.registrant"),
        @("Controllers.Examiner", "controller.examiner"),
        @("Controllers.Payment", "controller.payment"),
        @("Controllers.Public", "controller.staff.exam"),
        @("DAO.Impl", "dao.impl"),
        @("Services.Impl", "service.impl"),
        @("Models.payment.sepay", "model.payment.sepay"),
        @("Utils.payment.sepay", "util.payment.sepay"),
        @("DBConnection", "dbconnection"),
        @("Listeners", "listener"),
        @("Filters", "filter"),
        @("Constants", "constant"),
        @("DAO", "dao"),
        @("Services", "service"),
        @("Utils", "util"),
        @("Models", "model"),
        @("Controllers", "controller")
    ) | ForEach-Object { $reps.Add([PSCustomObject]@{ Old = $_[0]; New = $_[1] }) }
    return $reps
}

$ImportReps = Get-ImportReplacements

function Transform-Content([string]$Content, [string]$Package) {
    $Content = $Content -replace '(?m)^package\s+[\w.]+;', "package $Package;"
    foreach ($r in $ImportReps) {
        $Content = $Content.Replace($r.Old, $r.New)
    }
    return $Content
}

if (Test-Path $Staging) { Remove-Item $Staging -Recurse -Force }
New-Item -ItemType Directory -Path $Staging | Out-Null

$files = @()
foreach ($dir in $OldTopDirs) {
    $base = Join-Path $Src $dir
    if (Test-Path $base) {
        $files += Get-ChildItem -Path $base -Filter "*.java" -Recurse -File
    }
}

Write-Host "Migrating $($files.Count) Java files..."

foreach ($f in $files) {
    $oldRel = $f.FullName.Substring($Src.Length + 1).Replace('\', '/')
    $info = Get-TargetInfo $oldRel
    $content = Get-Content -Path $f.FullName -Raw -Encoding UTF8
    $content = Transform-Content $content $info.Package
    $dest = Join-Path $Staging ($info.Rel -replace '/', '\')
    $destDir = Split-Path $dest -Parent
    if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
    [System.IO.File]::WriteAllText($dest, $content, [System.Text.UTF8Encoding]::new($false))
    Write-Host "  $oldRel -> $($info.Rel)"
}

foreach ($dir in $OldTopDirs) {
    $path = Join-Path $Src $dir
    if (Test-Path $path) {
        Remove-Item $path -Recurse -Force
        Write-Host "Removed $path"
    }
}

Get-ChildItem -Path $Staging -Recurse -File | ForEach-Object {
    $rel = $_.FullName.Substring($Staging.Length + 1)
    $dest = Join-Path $Src $rel
    $destDir = Split-Path $dest -Parent
    if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
    Move-Item -Path $_.FullName -Destination $dest -Force
}
Remove-Item $Staging -Recurse -Force -ErrorAction SilentlyContinue

$allJava = Get-ChildItem -Path $Src -Filter "*.java" -Recurse -File
foreach ($f in $allJava) {
    $text = Get-Content -Path $f.FullName -Raw -Encoding UTF8
    $newText = $text
    foreach ($r in $ImportReps) {
        $newText = $newText.Replace($r.Old, $r.New)
    }
    if ($newText -ne $text) {
        [System.IO.File]::WriteAllText($f.FullName, $newText, [System.Text.UTF8Encoding]::new($false))
    }
}

Write-Host "Done. $($allJava.Count) files under $Src"
