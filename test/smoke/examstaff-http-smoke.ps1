# Smoke test examstaff + public call via HTTP (requires Tomcat at $BaseUrl).
param(
    [string]$BaseUrl = "http://localhost:8080/Driving-Licence-Examination-Management-Monolith",
    [string]$Username = "exam_minh",
    [string]$Password = "login123",
    [int]$ExamId = 2
)

$ErrorActionPreference = "Continue"
$passed = 0
$failed = 0
$skipped = 0
$results = @()

function Write-Result {
    param([string]$Name, [string]$Status, [string]$Detail = "")
    $script:results += [PSCustomObject]@{ Name = $Name; Status = $Status; Detail = $Detail }
    switch ($Status) {
        "PASS" { $script:passed++; Write-Host "[PASS] $Name" -ForegroundColor Green }
        "FAIL" { $script:failed++; Write-Host "[FAIL] $Name - $Detail" -ForegroundColor Red }
        "SKIP" { $script:skipped++; Write-Host "[SKIP] $Name - $Detail" -ForegroundColor Yellow }
    }
}

function Invoke-Http {
    param(
        [string]$Path,
        [string]$Method = "GET",
        [hashtable]$Body = $null,
        [Microsoft.PowerShell.Commands.WebRequestSession]$Session = $null,
        [switch]$AllowRedirect
    )
    $uri = if ($Path.StartsWith("http")) { $Path } else { "$BaseUrl$Path" }
    $params = @{
        Uri             = $uri
        Method          = $Method
        UseBasicParsing = $true
        TimeoutSec      = 30
    }
    if ($Session) { $params.WebSession = $Session }
    if ($Body) { $params.Body = $Body; $params.ContentType = "application/x-www-form-urlencoded" }
    if (-not $AllowRedirect) {
        $params.MaximumRedirection = 0
    }
    try {
        return Invoke-WebRequest @params
    } catch {
        if ($_.Exception.Response) {
            return $_.Exception.Response
        }
        throw
    }
}

function Get-StatusCode {
    param($Response)
    if ($Response -is [System.Net.HttpWebResponse]) {
        return [int]$Response.StatusCode
    }
    return [int]$Response.StatusCode
}

function Get-ResponseBody {
    param($Response)
    if ($Response -is [System.Net.HttpWebResponse]) {
        $reader = New-Object System.IO.StreamReader($Response.GetResponseStream())
        return $reader.ReadToEnd()
    }
    return $Response.Content
}

Write-Host "=== Examstaff + Public Call HTTP Smoke ===" -ForegroundColor Cyan
Write-Host "Base: $BaseUrl | User: $Username | ExamId: $ExamId"

# 0) Tomcat reachable
try {
    $root = Invoke-WebRequest -Uri $BaseUrl -UseBasicParsing -TimeoutSec 30
    Write-Result "App root reachable" "PASS" "HTTP $($root.StatusCode)"
} catch {
    Write-Result "App root reachable" "FAIL" $_.Exception.Message
    Write-Host "`nSummary: cannot continue without Tomcat." -ForegroundColor Red
    exit 1
}

# 1) Staff login page (no auth)
try {
    $loginPage = Invoke-WebRequest -Uri "$BaseUrl/staff/login" -UseBasicParsing -TimeoutSec 30
    if ($loginPage.Content -match 'id="identifier"' -and $loginPage.Content -match 'id="password"') {
        Write-Result "Staff login page" "PASS"
    } else {
        Write-Result "Staff login page" "FAIL" "Missing form fields"
    }
} catch {
    Write-Result "Staff login page" "FAIL" $_.Exception.Message
}

# 2) Login as EXAM_STAFF
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $loginResp = Invoke-Http -Path "/staff/login" -Method POST -Body @{
        identifier = $Username
        password   = $Password
    } -Session $session
    $code = Get-StatusCode $loginResp
    if ($code -eq 302 -or $code -eq 303) {
        Write-Result "Staff login POST" "PASS" "Redirect $code"
    } elseif ($code -eq 200) {
        $body = Get-ResponseBody $loginResp
        if ($body -match "không chính xác|not correct") {
            Write-Result "Staff login POST" "FAIL" "Invalid credentials"
        } else {
            Write-Result "Staff login POST" "PASS" "HTTP $code"
        }
    } else {
        Write-Result "Staff login POST" "FAIL" "HTTP $code"
    }
} catch {
    Write-Result "Staff login POST" "FAIL" $_.Exception.Message
}

# 3) Protected page without auth should redirect
$anon = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $anonResp = Invoke-Http -Path "/examstaff/dashboard" -Session $anon
    $code = Get-StatusCode $anonResp
    if ($code -eq 302 -or $code -eq 303) {
        Write-Result "Auth guard (dashboard)" "PASS" "HTTP $code -> login"
    } else {
        Write-Result "Auth guard (dashboard)" "FAIL" "Expected redirect, got $code"
    }
} catch {
    Write-Result "Auth guard (dashboard)" "FAIL" $_.Exception.Message
}

# 4) Select exam (sets session context)
try {
    $selResp = Invoke-Http -Path "/examstaff/select-exam?examId=$ExamId" -Session $session -AllowRedirect
    $code = Get-StatusCode $selResp
    if ($code -ge 200 -and $code -lt 400) {
        Write-Result "Select exam ($ExamId)" "PASS" "HTTP $code"
    } else {
        Write-Result "Select exam ($ExamId)" "FAIL" "HTTP $code"
    }
} catch {
    Write-Result "Select exam ($ExamId)" "FAIL" $_.Exception.Message
}

$q = "?examId=$ExamId"
$staffPages = @(
    @{ Name = "Dashboard"; Path = "/examstaff/dashboard$q" },
    @{ Name = "Allocation overview"; Path = "/examstaff/allocation$q" },
    @{ Name = "Allocation waiting"; Path = "/examstaff/allocation-waiting$q" },
    @{ Name = "Allocation theory"; Path = "/examstaff/allocation-theory$q" },
    @{ Name = "Allocation practical"; Path = "/examstaff/allocation-practical$q" },
    @{ Name = "Allocation pass"; Path = "/examstaff/allocation-results-pass$q" },
    @{ Name = "Allocation fail"; Path = "/examstaff/allocation-results-fail$q" },
    @{ Name = "Allocation suspended"; Path = "/examstaff/allocation-results-suspended$q" },
    @{ Name = "Candidate call"; Path = "/examstaff/candidatecall$q" },
    @{ Name = "Procedure desk"; Path = "/examstaff/procedure$q" },
    @{ Name = "Exam control"; Path = "/examstaff/exam-control$q" },
    @{ Name = "Examiner allocation"; Path = "/examstaff/examiner-allocation$q" },
    @{ Name = "Report"; Path = "/examstaff/report$q" },
    @{ Name = "Audit"; Path = "/examstaff/audit$q" },
    @{ Name = "Public call screen"; Path = "/examstaff/public-call$q" }
)

foreach ($page in $staffPages) {
    try {
        $resp = Invoke-WebRequest -Uri "$BaseUrl$($page.Path)" -WebSession $session -UseBasicParsing -TimeoutSec 45 -MaximumRedirection 10
        $html = $resp.Content
        if ($resp.StatusCode -eq 200 -and $html -notmatch "HTTP Status 500" -and $html -notmatch "error-page") {
            Write-Result $page.Name "PASS" "HTTP $($resp.StatusCode)"
        } elseif ($resp.StatusCode -eq 200) {
            Write-Result $page.Name "FAIL" "Page may contain error content"
        } else {
            Write-Result $page.Name "FAIL" "HTTP $($resp.StatusCode)"
        }
    } catch {
        Write-Result $page.Name "FAIL" $_.Exception.Message
    }
}

# 5) Public call JSON API (no session required)
try {
    $api = Invoke-WebRequest -Uri "$BaseUrl/api/public-call/state?examId=$ExamId" -UseBasicParsing -TimeoutSec 30
    $json = $api.Content
    if ($api.StatusCode -eq 200 -and $json -match '"examId"' -and $json -match '"waitingQueue"') {
        Write-Result "Public call state API" "PASS" "JSON OK"
    } else {
        Write-Result "Public call state API" "FAIL" "Bad response: $($json.Substring(0, [Math]::Min(120, $json.Length)))"
    }
} catch {
    Write-Result "Public call state API" "FAIL" $_.Exception.Message
}

# 6) Call board in-memory: resume shift + verify API reflects examId
try {
    $resumeResp = Invoke-Http -Path "/examstaff/candidatecall$q&action=startShift" -Session $session -AllowRedirect
    $resumeCode = Get-StatusCode $resumeResp
    if ($resumeCode -ge 200 -and $resumeCode -lt 400) {
        Write-Result "Resume call shift" "PASS" "HTTP $resumeCode"
    } else {
        Write-Result "Resume call shift" "FAIL" "HTTP $resumeCode"
    }
    Start-Sleep -Seconds 1
    $api2 = Invoke-WebRequest -Uri "$BaseUrl/api/public-call/state?examId=$ExamId" -UseBasicParsing -TimeoutSec 30
    if ($api2.Content -match """examId"":$ExamId") {
        Write-Result "In-memory call board (examId in API)" "PASS"
    } else {
        Write-Result "In-memory call board (examId in API)" "FAIL" $api2.Content.Substring(0, [Math]::Min(200, $api2.Content.Length))
    }
} catch {
    Write-Result "Call board flow" "FAIL" $_.Exception.Message
}

# 7) Audit export (GET should respond, may be empty file)
try {
    $export = Invoke-WebRequest -Uri "$BaseUrl/examstaff/audit-export$q" -WebSession $session -UseBasicParsing -TimeoutSec 45 -MaximumRedirection 10
    if ($export.StatusCode -eq 200) {
        Write-Result "Audit export" "PASS" "HTTP 200"
    } else {
        Write-Result "Audit export" "FAIL" "HTTP $($export.StatusCode)"
    }
} catch {
    Write-Result "Audit export" "FAIL" $_.Exception.Message
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "PASS: $passed | FAIL: $failed | SKIP: $skipped"
$results | Format-Table -AutoSize
if ($failed -gt 0) { exit 1 }
exit 0
