$ErrorActionPreference = "Stop"
$Web = Join-Path (Split-Path -Parent $PSScriptRoot) "web"

$replacements = @(
    @('Controllers.Staff.ExamStaff', 'controller.staff.exam'),
    @('Controllers.ManagingStaff', 'controller.staff.managing'),
    @('Controllers.Auth.Public', 'controller.auth.landing'),
    @('Controllers.Registrant', 'controller.registrant'),
    @('Controllers.Examiner', 'controller.examiner'),
    @('Controllers.Payment', 'controller.payment'),
    @('Controllers.Public', 'controller.staff.exam'),
    @('DAO.Impl', 'dao.impl'),
    @('Services.Impl', 'service.impl'),
    @('Models.payment.sepay', 'model.payment.sepay'),
    @('Utils.payment.sepay', 'util.payment.sepay'),
    @('DBConnection.DBConfig', 'dbconnection.DBContext'),
    @('DBConnection', 'dbconnection'),
    @('Listeners', 'listener'),
    @('Filters', 'filter'),
    @('Constants', 'constant'),
    @('DAO', 'dao'),
    @('Services', 'service'),
    @('Utils', 'util'),
    @('Models', 'model'),
    @('Controllers', 'controller')
)

$modelMap = @{
    'User' = 'model.user.User'
    'Profile' = 'model.user.Profile'
    'Role' = 'model.user.Role'
    'AuditLog' = 'model.user.AuditLog'
    'ExamRegistration' = 'model.exam.ExamRegistration'
    'ExamResult' = 'model.exam.ExamResult'
    'ExamArea' = 'model.exam.ExamArea'
    'ExamDevice' = 'model.exam.ExamDevice'
    'ExamSession' = 'model.exam.ExamSession'
    'ExamComputer' = 'model.exam.ExamComputer'
    'StaffProcedureKpi' = 'model.staff.StaffProcedureKpi'
    'ManagingStaffApprovalView' = 'model.staff.ManagingStaffApprovalView'
}

$fixed = 0
Get-ChildItem -Path $Web -Include *.jsp -Recurse -File | ForEach-Object {
    $path = $_.FullName
    $text = [System.IO.File]::ReadAllText($path)
    $newText = $text
    foreach ($pair in $replacements) {
        $newText = $newText.Replace($pair[0], $pair[1])
    }
    foreach ($entry in $modelMap.GetEnumerator()) {
        $newText = $newText.Replace("model.$($entry.Key)", $entry.Value)
    }
    # JSP direct connection via DBContext instance
    $newText = $newText.Replace('dbconnection.DBContext.getConnection()', '(new dbconnection.DBContext()).getConnection()')
    if ($newText -cne $text) {
        [System.IO.File]::WriteAllText($path, $newText, [System.Text.UTF8Encoding]::new($false))
        $fixed++
        Write-Host "Updated $($_.FullName.Substring($Web.Length + 1))"
    }
}
Write-Host "Fixed $fixed JSP files"

# fix_jsp_packages replaces DAO->dao inside class names (ExamSessionDAO -> ExamSessiondao).
$fixDao = Join-Path $PSScriptRoot "fix_dao_names.ps1"
if (Test-Path $fixDao) {
    Write-Host "Running fix_dao_names.ps1 for JSP cleanup..."
    & $fixDao
}
