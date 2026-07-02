# Realign packages to match origin/main: enums + dto layers.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Src = Join-Path $Root "src\java"

function Move-ClassFile {
    param(
        [string]$FromRel,
        [string]$ToRel,
        [string]$Package,
        [string]$ClassName = $null,
        [string]$NewClassName = $null
    )
    $from = Join-Path $Src ($FromRel -replace '/', '\')
    $to = Join-Path $Src ($ToRel -replace '/', '\')
    if (-not (Test-Path $from)) {
        Write-Host "SKIP missing: $FromRel"
        return
    }
    $dir = Split-Path $to -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    $content = [System.IO.File]::ReadAllText($from)
    $content = $content -replace '(?m)^package\s+[\w.]+;', "package $Package;"
    if ($ClassName -and $NewClassName -and $ClassName -cne $NewClassName) {
        $content = $content.Replace($ClassName, $NewClassName)
    }
    [System.IO.File]::WriteAllText($to, $content, [System.Text.UTF8Encoding]::new($false))
    if ($from -cne $to) { Remove-Item $from -Force }
    Write-Host "MOVED $FromRel -> $ToRel"
}

# --- constant -> enums (registrant-specific, keep class names) ---
$constantToEnums = @(
    'AuditEntityLabels.java',
    'CandidateSectionStatus.java',
    'Db2Mappings.java',
    'ExamRegistrationLifecycleStatus.java',
    'ExamSectionProfiles.java',
    'ExamTypes.java',
    'ProfileRegistrationStatus.java',
    'ViolationReasonCodes.java'
)
foreach ($f in $constantToEnums) {
    $from = Join-Path $Src "constant\$f"
    if (Test-Path $from) {
        $to = Join-Path $Src "enums\$f"
        $content = [System.IO.File]::ReadAllText($from) -replace '(?m)^package constant;', 'package enums;'
        [System.IO.File]::WriteAllText($to, $content, [System.Text.UTF8Encoding]::new($false))
        Remove-Item $from -Force
        Write-Host "ENUM $f"
    }
}
# Remove superseded by origin/main enums
@('ExamSessionStatus.java', 'ExamSectionType.java') | ForEach-Object {
    $p = Join-Path $Src "constant\$_"
    if (Test-Path $p) { Remove-Item $p -Force; Write-Host "DEL constant/$_" }
}
if ((Get-ChildItem (Join-Path $Src 'constant') -ErrorAction SilentlyContinue | Measure-Object).Count -eq 0) {
    Remove-Item (Join-Path $Src 'constant') -Force -ErrorAction SilentlyContinue
}

# --- model/service -> dto ---
$moves = @(
    @{ From='model/common/RegisterResult.java'; To='dto/registration/RegisterResult.java'; Pkg='dto.registration' },
    @{ From='model/staff/ManagingStaffApprovalView.java'; To='dto/staff/ManagingStaffApprovalView.java'; Pkg='dto.staff' },
    @{ From='model/staff/StaffProcedureKpi.java'; To='dto/staff/StaffProcedureKpiDTO.java'; Pkg='dto.staff'; Old='StaffProcedureKpi'; New='StaffProcedureKpiDTO' },
    @{ From='model/exam/SessionExamSectionInfo.java'; To='dto/exam/SessionExamSectionInfo.java'; Pkg='dto.exam' },
    @{ From='model/exam/SessionScheduleInfo.java'; To='dto/exam/SessionScheduleInfo.java'; Pkg='dto.exam' },
    @{ From='model/exam/ExaminerPaperState.java'; To='dto/examiner/ExaminerPaperState.java'; Pkg='dto.examiner' },
    @{ From='model/exam/ExaminerAnswerStats.java'; To='dto/examiner/ExaminerAnswerStats.java'; Pkg='dto.examiner' },
    @{ From='model/exam/TheoryPaperAnswer.java'; To='dto/score/TheoryPaperAnswer.java'; Pkg='dto.score' },
    @{ From='model/exam/TheoryScore.java'; To='dto/score/TheoryScore.java'; Pkg='dto.score' },
    @{ From='model/exam/PracticalScore.java'; To='dto/score/PracticalScore.java'; Pkg='dto.score' },
    @{ From='service/ExaminerExportContext.java'; To='dto/examiner/ExaminerExportContext.java'; Pkg='dto.examiner' },
    @{ From='service/ExaminerExportPayload.java'; To='dto/examiner/ExaminerExportPayload.java'; Pkg='dto.examiner' },
    @{ From='service/XmlExportDocument.java'; To='dto/xml/XmlExportDocument.java'; Pkg='dto.xml' },
    @{ From='service/XmlExportTable.java'; To='dto/xml/XmlExportTable.java'; Pkg='dto.xml' }
)
foreach ($m in $moves) {
    Move-ClassFile -FromRel $m.From -ToRel $m.To -Package $m.Pkg -ClassName $m.Old -NewClassName $m.New
}

# registrant view models -> dto.registrant
$regDir = Join-Path $Src 'model\registrant'
if (Test-Path $regDir) {
    Get-ChildItem $regDir -Filter '*.java' | ForEach-Object {
        $name = $_.Name
        Move-ClassFile -FromRel "model/registrant/$name" -ToRel "dto/registrant/$name" -Package 'dto.registrant'
    }
    if ((Get-ChildItem $regDir -ErrorAction SilentlyContinue | Measure-Object).Count -eq 0) {
        Remove-Item $regDir -Force -ErrorAction SilentlyContinue
    }
}
@('model/common', 'model/staff') | ForEach-Object {
    $d = Join-Path $Src ($_ -replace '/', '\')
    if ((Test-Path $d) -and ((Get-ChildItem $d -ErrorAction SilentlyContinue | Measure-Object).Count -eq 0)) {
        Remove-Item $d -Recurse -Force -ErrorAction SilentlyContinue
    }
}

# --- bulk import / symbol replacements (longest first) ---
$replacements = @(
    @('import model.registrant.', 'import dto.registrant.'),
    @('import model.common.RegisterResult', 'import dto.registration.RegisterResult'),
    @('import model.staff.StaffProcedureKpi', 'import dto.staff.StaffProcedureKpiDTO'),
    @('import model.staff.', 'import dto.staff.'),
    @('import model.exam.SessionExamSectionInfo', 'import dto.exam.SessionExamSectionInfo'),
    @('import model.exam.SessionScheduleInfo', 'import dto.exam.SessionScheduleInfo'),
    @('import model.exam.ExaminerPaperState', 'import dto.examiner.ExaminerPaperState'),
    @('import model.exam.ExaminerAnswerStats', 'import dto.examiner.ExaminerAnswerStats'),
    @('import model.exam.TheoryPaperAnswer', 'import dto.score.TheoryPaperAnswer'),
    @('import model.exam.TheoryScore', 'import dto.score.TheoryScore'),
    @('import model.exam.PracticalScore', 'import dto.score.PracticalScore'),
    @('import service.ExaminerExportContext', 'import dto.examiner.ExaminerExportContext'),
    @('import service.ExaminerExportPayload', 'import dto.examiner.ExaminerExportPayload'),
    @('import service.XmlExportDocument', 'import dto.xml.XmlExportDocument'),
    @('import service.XmlExportTable', 'import dto.xml.XmlExportTable'),
    @('import constant.', 'import enums.'),
    @('constant.', 'enums.'),
    @('ExamSectionType', 'SectionType'),
    @('StaffProcedureKpi', 'StaffProcedureKpiDTO'),
    @('ExamSessionStatus.canStart(', 'ExamSessionStatus.canStartSession('),
    @('ExamSessionStatus.isInProgress(', 'ExamSessionStatus.isSessionInProgress('),
    @('ExamSessionStatus.isEnded(', 'ExamSessionStatus.isSessionEnded(')
)

$files = Get-ChildItem $Src -Include *.java -Recurse -File
$fixed = 0
foreach ($file in $files) {
    $text = [System.IO.File]::ReadAllText($file.FullName)
    $newText = $text
    foreach ($pair in $replacements) {
        $newText = $newText.Replace($pair[0], $pair[1])
    }
    if ($newText -cne $text) {
        [System.IO.File]::WriteAllText($file.FullName, $newText, [System.Text.UTF8Encoding]::new($false))
        $fixed++
    }
}
Write-Host "Updated imports in $fixed files"

# Fix ExaminerExportContext to use enums.SectionType in record
$ctx = Join-Path $Src 'dto\examiner\ExaminerExportContext.java'
if (Test-Path $ctx) {
    $t = [System.IO.File]::ReadAllText($ctx)
    $t = $t -replace 'import constant\.SectionType;', 'import enums.SectionType;'
    $t = $t -replace 'import enums\.SectionType;', 'import enums.SectionType;'
    if ($t -notmatch 'import enums\.SectionType') {
        $t = $t -replace '(package dto\.examiner;)', "`$1`r`n`r`nimport enums.SectionType;"
    }
    [System.IO.File]::WriteAllText($ctx, $t, [System.Text.UTF8Encoding]::new($false))
}

Write-Host "Realign complete."
