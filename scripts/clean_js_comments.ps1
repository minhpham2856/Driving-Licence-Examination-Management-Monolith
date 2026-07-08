$root = "d:\study\SWPProject\Driving-Licence-Examination-Management-Monolith\web\assets\js"
$files = @('allocation.js','audit.js','candidatecall.js','dashboard.js','examiner-allocation.js','examstaff-sidebar.js','procedure.js')
foreach ($name in $files) {
    $path = Join-Path $root $name
    if (-not (Test-Path $path)) { continue }
    $lines = Get-Content $path
    $clean = $lines | Where-Object { $_ -notmatch '^\s*//' }
    Set-Content -Path $path -Value $clean -Encoding utf8
    Write-Output $name
}
