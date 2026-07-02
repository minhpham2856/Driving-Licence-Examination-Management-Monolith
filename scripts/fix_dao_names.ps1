$ErrorActionPreference = "Stop"
$Src = Join-Path (Split-Path -Parent $PSScriptRoot) "src\java"

$replacements = @(
    'AuditLogdaoImpl','AuditLogDAOImpl',
    'CandidateCalldaoImpl','CandidateCallDAOImpl',
    'DocumentdaoImpl','DocumentDAOImpl',
    'ExamAreadaoImpl','ExamAreaDAOImpl',
    'ExamComputerdaoImpl','ExamComputerDAOImpl',
    'ExamDevicedaoImpl','ExamDeviceDAOImpl',
    'ExaminerAssignmentdaoImpl','ExaminerAssignmentDAOImpl',
    'ExaminerSessionDatadaoImpl','ExaminerSessionDataDAOImpl',
    'ExamRegistrationdaoImpl','ExamRegistrationDAOImpl',
    'ExamSessiondaoImpl','ExamSessionDAOImpl',
    'PaymentdaoImpl','PaymentDAOImpl',
    'ProfiledaoImpl','ProfileDAOImpl',
    'RegistrantdaoImpl','RegistrantDAOImpl',
    'RoledaoImpl','RoleDAOImpl',
    'TheoryPaperdaoImpl','TheoryPaperDAOImpl',
    'UserdaoImpl','UserDAOImpl',
    'AuditLogdao','AuditLogDAO',
    'CandidateCalldao','CandidateCallDAO',
    'Documentdao','DocumentDAO',
    'ExamAreadao','ExamAreaDAO',
    'ExamComputerdao','ExamComputerDAO',
    'ExamDevicedao','ExamDeviceDAO',
    'ExaminerAssignmentdao','ExaminerAssignmentDAO',
    'ExaminerSessionDatadao','ExaminerSessionDataDAO',
    'ExamRegistrationdao','ExamRegistrationDAO',
    'ExamSessiondao','ExamSessionDAO',
    'Paymentdao','PaymentDAO',
    'Profiledao','ProfileDAO',
    'Registrantdao','RegistrantDAO',
    'Roledao','RoleDAO',
    'TheoryPaperdao','TheoryPaperDAO',
    'Userdao','UserDAO'
)

$fixed = 0
$targets = @(
    (Join-Path (Split-Path -Parent $PSScriptRoot) "src\java"),
    (Join-Path (Split-Path -Parent $PSScriptRoot) "web")
)
foreach ($root in $targets) {
if (-not (Test-Path $root)) { continue }
$files = Get-ChildItem -Path $root -Include *.java,*.jsp -Recurse -File
foreach ($file in $files) {
    $path = $file.FullName
    $lines = [System.IO.File]::ReadAllLines($path)
    $changed = $false
    $newLines = New-Object System.Collections.Generic.List[string]
    foreach ($line in $lines) {
        $newLine = $line
        for ($i = 0; $i -lt $replacements.Length; $i += 2) {
            $newLine = $newLine.Replace($replacements[$i], $replacements[$i + 1])
        }
        if ($newLine -cne $line) { $changed = $true }
        [void]$newLines.Add($newLine)
    }
    if ($changed) {
        [System.IO.File]::WriteAllLines($path, $newLines)
        $fixed++
    }
}
}
Write-Host "Fixed $fixed files"
