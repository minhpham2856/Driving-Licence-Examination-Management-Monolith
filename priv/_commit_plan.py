#!/usr/bin/env python3
"""Generate backdated commit timestamps and execute staged commits for minhpn branch."""
import os
import random
import subprocess
import sys
from datetime import date, datetime, timedelta

AUTHOR = "minh <minhphamnhat2852006@gmail.com>"
START = date(2026, 7, 6)
END = date(2026, 7, 19)
SEED = 2856

# (message, paths/globs relative to repo root)
COMMITS = [
    ("Update shared Attributes session keys", ["src/java/shared/Attributes.java"]),
    ("Update shared ConfigManager env resolution", ["src/java/shared/ConfigManager.java"]),
    ("Remove duplicate shared util ConfigManager", ["src/java/shared/util/ConfigManager.java"]),
    ("Add shared PasswordUtil", ["src/java/shared/util/PasswordUtil.java"]),
    ("Add shared SectionStatusUtil", ["src/java/shared/util/SectionStatusUtil.java"]),
    ("Move FormatUtil to shared util", ["src/java/shared/util/FormatUtil.java", "src/java/auth/util/FormatUtil.java"]),
    ("Update shared DBContext", ["src/java/shared/dbconnection/DBContext.java"]),
    ("Update shared AuditAction enum", ["src/java/shared/enums/AuditAction.java"]),
    ("Update shared Candidate model", ["src/java/shared/model/Candidate.java"]),
    ("Update shared ExamEnrollment model", ["src/java/shared/model/ExamEnrollment.java"]),
    ("Update shared ExamEnrollmentSection model", ["src/java/shared/model/ExamEnrollmentSection.java"]),
    ("Update shared TheoryPaper model", ["src/java/shared/model/TheoryPaper.java"]),
    ("Add shared exam room queue registry", ["src/java/shared/queue/"]),
    ("Remove AppConfigListener", ["src/java/listener/AppConfigListener.java"]),
    ("Update DDL schema for enrollment sections", ["web/WEB-INF/others/sql/DDL_DLEM_DB.sql"]),
    ("Update DML seed data", ["web/WEB-INF/others/sql/DML_DLEM_DB.sql"]),
    ("Add migration for result printed flag", ["web/WEB-INF/others/sql/migration_exam_enrollment_section_result_printed.sql"]),
    ("Add seed SQL for A1 exam", ["web/WEB-INF/others/sql/seed_a1_exam_500.sql"]),
    ("Add seed SQL for candidates", ["web/WEB-INF/others/sql/seed_candidate.sql"]),
    ("Update web.xml servlet mappings", ["web/WEB-INF/web.xml"]),
    ("Add auth AccountDTO", ["src/java/auth/dto/AccountDTO.java"]),
    ("Update auth UpdateProfileDTO", ["src/java/auth/dto/UpdateProfileDTO.java"]),
    ("Add auth RoleRoute enum", ["src/java/auth/enums/RoleRoute.java"]),
    ("Add auth AccountFilter", ["src/java/auth/filter/AccountFilter.java"]),
    ("Update auth ProfileDAO", ["src/java/auth/dao/ProfileDAO.java", "src/java/auth/dao/impl/ProfileDAOImpl.java"]),
    ("Update auth UserDAO", ["src/java/auth/dao/UserDAO.java", "src/java/auth/dao/impl/UserDAOImpl.java"]),
    ("Update auth AuthService", ["src/java/auth/service/AuthService.java", "src/java/auth/service/impl/AuthServiceImpl.java"]),
    ("Update auth ProfileService", ["src/java/auth/service/ProfileService.java", "src/java/auth/service/impl/ProfileServiceImpl.java"]),
    ("Update auth EmailServiceImpl", ["src/java/auth/service/impl/EmailServiceImpl.java"]),
    ("Remove auth PasswordUtil duplicate", ["src/java/auth/util/PasswordUtil.java"]),
    ("Update auth ValidationUtil", ["src/java/auth/util/ValidationUtil.java"]),
    ("Update auth ChangePasswordServlet", ["src/java/auth/controller/general/ChangePasswordServlet.java"]),
    ("Update auth LoginServlet general", ["src/java/auth/controller/general/LoginServlet.java"]),
    ("Update auth ProfileServlet", ["src/java/auth/controller/general/ProfileServlet.java"]),
    ("Update auth RegisterServlet", ["src/java/auth/controller/general/RegisterServlet.java"]),
    ("Update auth internal LoginServlet", ["src/java/auth/controller/internal/LoginServlet.java"]),
    ("Update auth general change-password view", ["web/views/auth/general/change-password.jsp"]),
    ("Update auth general forgot-password view", ["web/views/auth/general/forgot-password.jsp"]),
    ("Update auth general login view", ["web/views/auth/general/login.jsp"]),
    ("Update auth general profile view", ["web/views/auth/general/profile.jsp"]),
    ("Update auth general register view", ["web/views/auth/general/register.jsp"]),
    ("Update auth internal login view", ["web/views/auth/internal/login.jsp"]),
    ("Add landing error pages CSS", ["web/assets/css/landing/error-pages.css"]),
    ("Add internal login CSS", ["web/assets/css/landing/internal-login.css"]),
    ("Update landing forgot-password CSS", ["web/assets/css/landing/forgot-password.css"]),
    ("Update landing page CSS", ["web/assets/css/landing/landing.css"]),
    ("Update license categories CSS", ["web/assets/css/landing/license-categories.css"]),
    ("Update landing login CSS", ["web/assets/css/landing/login.css"]),
    ("Update process page CSS", ["web/assets/css/landing/process.css"]),
    ("Update register page CSS", ["web/assets/css/landing/register.css"]),
    ("Update layout CSS", ["web/assets/css/layout.css"]),
    ("Add general error 403 view", ["web/views/general/error-403.jsp"]),
    ("Add general error 404 view", ["web/views/general/error-404.jsp"]),
    ("Update general home view", ["web/views/general/home.jsp"]),
    ("Update license categories view", ["web/views/general/license-categories.jsp"]),
    ("Update process view", ["web/views/general/process.jsp"]),
    ("Update layout footer", ["web/views/layout/footer.jsp"]),
    ("Update examstaff sidebar layout", ["web/views/layout/sidebar-examstaff.jsp"]),
    ("Update examstaff account CSS", ["web/assets/css/examstaff/account.css"]),
    ("Update SePay config", ["src/java/payment/util/sepay/SePayConfig.java"]),
    ("Update registrant profile service", ["src/java/registrant/service/impl/RegistrantProfileServiceImpl.java"]),
    ("Update registrant settings service", ["src/java/registrant/service/impl/RegistrantSettingsServiceImpl.java"]),
    ("Update registrant Cloudinary storage", ["src/java/registrant/util/CloudinaryDocumentStorage.java"]),
    ("Rename examiner servlets to short names batch 1", [
        "src/java/examiner/controller/ExaminerCandidateCallServlet.java",
        "src/java/examiner/controller/ActionServlet.java",
        "src/java/examiner/controller/ExaminerCandidateDetailsServlet.java",
        "src/java/examiner/controller/CandidateServlet.java",
        "src/java/examiner/controller/ExaminerDashboardServlet.java",
        "src/java/examiner/controller/DashboardServlet.java",
    ]),
    ("Rename examiner servlets to short names batch 2", [
        "src/java/examiner/controller/ExaminerDevicesServlet.java",
        "src/java/examiner/controller/DevicesServlet.java",
        "src/java/examiner/controller/ExaminerExamServlet.java",
        "src/java/examiner/controller/ExamServlet.java",
        "src/java/examiner/controller/ExaminerMiscServlet.java",
        "src/java/examiner/controller/MiscServlet.java",
    ]),
    ("Rename examiner servlets to short names batch 3", [
        "src/java/examiner/controller/ExaminerResultDetailsServlet.java",
        "src/java/examiner/controller/ResultServlet.java",
        "src/java/examiner/controller/ExaminerScoreEntryServlet.java",
        "src/java/examiner/controller/ScoreServlet.java",
        "src/java/examiner/controller/ExaminerViolationsServlet.java",
        "src/java/examiner/controller/ViolationsServlet.java",
    ]),
    ("Update examiner ExportServlet", ["src/java/examiner/controller/ExportServlet.java"]),
    ("Update examiner PrintServlet", ["src/java/examiner/controller/PrintServlet.java"]),
    ("Update examiner ExaminerFilter", ["src/java/examiner/filter/ExaminerFilter.java"]),
    ("Refactor examiner DAO interfaces naming batch 1", [
        "src/java/examiner/dao/AuditDAO.java",
        "src/java/examiner/dao/CandidateAnswerDAO.java",
        "src/java/examiner/dao/CandidateDAO.java",
        "src/java/examiner/dao/DeductionRecordDAO.java",
        "src/java/examiner/dao/DeductionRecordViewDAO.java",
    ]),
    ("Refactor examiner DAO interfaces naming batch 2", [
        "src/java/examiner/dao/ExamAreaDAO.java",
        "src/java/examiner/dao/ExamDAO.java",
        "src/java/examiner/dao/ExamDeviceDAO.java",
        "src/java/examiner/dao/ExamEnrollmentDAO.java",
        "src/java/examiner/dao/ExamResultDAO.java",
    ]),
    ("Refactor examiner DAO interfaces naming batch 3", [
        "src/java/examiner/dao/ExamScoreDAO.java",
        "src/java/examiner/dao/ExamSectionDAO.java",
        "src/java/examiner/dao/ExaminerScheduleDAO.java",
        "src/java/examiner/dao/ExaminerViewDAO.java",
        "src/java/examiner/dao/LicenceDAO.java",
    ]),
    ("Refactor examiner DAO interfaces naming batch 4", [
        "src/java/examiner/dao/PaymentDAO.java",
        "src/java/examiner/dao/ProfileDAO.java",
        "src/java/examiner/dao/QuestionDAO.java",
        "src/java/examiner/dao/RoleDAO.java",
        "src/java/examiner/dao/ScoreDeductionDAO.java",
        "src/java/examiner/dao/TheoryPaperDAO.java",
        "src/java/examiner/dao/UserDAO.java",
    ]),
    ("Add ExamEnrollmentSectionDAO", [
        "src/java/examiner/dao/ExamEnrollmentSectionDAO.java",
        "src/java/examiner/dao/impl/ExamEnrollmentSectionDAOImpl.java",
    ]),
    ("Refactor examiner DAO impl batch 1", [
        "src/java/examiner/dao/impl/AuditDAOImpl.java",
        "src/java/examiner/dao/impl/CandidateAnswerDAOImpl.java",
        "src/java/examiner/dao/impl/CandidateDAOImpl.java",
        "src/java/examiner/dao/impl/DeductionRecordDAOImpl.java",
        "src/java/examiner/dao/impl/DeductionRecordViewDAOImpl.java",
    ]),
    ("Refactor examiner DAO impl batch 2", [
        "src/java/examiner/dao/impl/ExamAreaDAOImpl.java",
        "src/java/examiner/dao/impl/ExamDAOImpl.java",
        "src/java/examiner/dao/impl/ExamDeviceDAOImpl.java",
        "src/java/examiner/dao/impl/ExamEnrollmentDAOImpl.java",
        "src/java/examiner/dao/impl/ExamResultDAOImpl.java",
    ]),
    ("Refactor examiner DAO impl batch 3", [
        "src/java/examiner/dao/impl/ExamScoreDAOImpl.java",
        "src/java/examiner/dao/impl/ExamSectionDAOImpl.java",
        "src/java/examiner/dao/impl/ExaminerScheduleDAOImpl.java",
        "src/java/examiner/dao/impl/ExaminerViewDAOImpl.java",
        "src/java/examiner/dao/impl/LicenceDAOImpl.java",
    ]),
    ("Refactor examiner DAO impl batch 4", [
        "src/java/examiner/dao/impl/PaymentDAOImpl.java",
        "src/java/examiner/dao/impl/ProfileDAOImpl.java",
        "src/java/examiner/dao/impl/QuestionDAOImpl.java",
        "src/java/examiner/dao/impl/RoleDAOImpl.java",
        "src/java/examiner/dao/impl/ScoreDeductionDAOImpl.java",
        "src/java/examiner/dao/impl/TheoryPaperDAOImpl.java",
        "src/java/examiner/dao/impl/UserDAOImpl.java",
    ]),
    ("Remove unused examiner DTOs", [
        "src/java/examiner/dto/CandidateProfileDTO.java",
        "src/java/examiner/dto/ExamReportDTO.java",
        "src/java/examiner/dto/InfractionDTO.java",
        "src/java/examiner/dto/RegisterResultDTO.java",
        "src/java/examiner/dto/UploadRowDTO.java",
    ]),
    ("Refactor examiner ServiceResult to record", ["src/java/examiner/dto/ServiceResult.java"]),
    ("Refactor CandidateRowDTO plain fields", ["src/java/examiner/dto/CandidateRowDTO.java"]),
    ("Flatten EnrollmentDTO fields", ["src/java/examiner/dto/EnrollmentDTO.java"]),
    ("Update examiner export DTOs", [
        "src/java/examiner/dto/ExportContextDTO.java",
        "src/java/examiner/dto/ExportPayloadDTO.java",
        "src/java/examiner/dto/XmlExportTable.java",
    ]),
    ("Update ExamStatsDTO and SaveResultDTO", [
        "src/java/examiner/dto/ExamStatsDTO.java",
        "src/java/examiner/dto/SaveResultDTO.java",
    ]),
    ("Add ExamDispatchResult DTO", ["src/java/examiner/dto/ExamDispatchResult.java"]),
    ("Add PrintPreviewDTO", ["src/java/examiner/dto/PrintPreviewDTO.java"]),
    ("Remove duplicate examiner services", [
        "src/java/examiner/service/AuthService.java",
        "src/java/examiner/service/CallService.java",
        "src/java/examiner/service/EmailService.java",
        "src/java/examiner/service/ExamAreaService.java",
        "src/java/examiner/service/ExamSectionService.java",
        "src/java/examiner/service/LicenceService.java",
        "src/java/examiner/service/ScheduleService.java",
    ]),
    ("Remove duplicate examiner service impls", [
        "src/java/examiner/service/impl/AuthServiceImpl.java",
        "src/java/examiner/service/impl/CallServiceImpl.java",
        "src/java/examiner/service/impl/EmailServiceImpl.java",
        "src/java/examiner/service/impl/ExamAreaServiceImpl.java",
        "src/java/examiner/service/impl/ExamSectionServiceImpl.java",
        "src/java/examiner/service/impl/LicenceServiceImpl.java",
        "src/java/examiner/service/impl/ScheduleServiceImpl.java",
    ]),
    ("Rename RegistrationService to EnrollmentService", [
        "src/java/examiner/service/RegistrationService.java",
        "src/java/examiner/service/impl/RegistrationServiceImpl.java",
        "src/java/examiner/service/EnrollmentService.java",
        "src/java/examiner/service/impl/EnrollmentServiceImpl.java",
    ]),
    ("Rename DocumentService to FileService", [
        "src/java/examiner/service/DocumentService.java",
        "src/java/examiner/service/FileService.java",
    ]),
    ("Rename ExamScoreService to ScoreService", [
        "src/java/examiner/service/ExamScoreService.java",
        "src/java/examiner/service/impl/ExamScoreServiceImpl.java",
        "src/java/examiner/service/ScoreService.java",
        "src/java/examiner/service/impl/ScoreServiceImpl.java",
    ]),
    ("Add examiner ActionService", [
        "src/java/examiner/service/ActionService.java",
        "src/java/examiner/service/impl/ActionServiceImpl.java",
    ]),
    ("Add examiner DispatchService", [
        "src/java/examiner/service/DispatchService.java",
        "src/java/examiner/service/impl/DispatchServiceImpl.java",
    ]),
    ("Add examiner ProgressService", [
        "src/java/examiner/service/ProgressService.java",
        "src/java/examiner/service/impl/ProgressServiceImpl.java",
    ]),
    ("Update examiner ExamService", [
        "src/java/examiner/service/ExamService.java",
        "src/java/examiner/service/impl/ExamServiceImpl.java",
    ]),
    ("Update examiner ExamViewService", [
        "src/java/examiner/service/ExamViewService.java",
        "src/java/examiner/service/impl/ExamViewServiceImpl.java",
    ]),
    ("Update examiner RoleService", [
        "src/java/examiner/service/RoleService.java",
        "src/java/examiner/service/impl/RoleServiceImpl.java",
    ]),
    ("Update examiner AuditService", [
        "src/java/examiner/service/AuditService.java",
        "src/java/examiner/service/impl/AuditServiceImpl.java",
    ]),
    ("Update examiner Docx export service", ["src/java/examiner/service/impl/DocxServiceImpl.java"]),
    ("Update examiner Excel export service", ["src/java/examiner/service/impl/ExcelServiceImpl.java"]),
    ("Remove examiner util duplicates", [
        "src/java/examiner/util/ConfigManager.java",
        "src/java/examiner/util/ExamQueue.java",
        "src/java/examiner/util/UsernameGenerator.java",
    ]),
    ("Rename ExaminerCandidateSort to SortUtil", [
        "src/java/examiner/util/ExaminerCandidateSort.java",
        "src/java/examiner/util/SortUtil.java",
    ]),
    ("Add examiner FormatUtil and ListUtil", [
        "src/java/examiner/util/FormatUtil.java",
        "src/java/examiner/util/ListUtil.java",
    ]),
    ("Remove old examiner docx templates", [
        "resources/docx-template/examiner/BB1(B-C1-C-D1-D2-D).docx",
        "resources/docx-template/examiner/BB2(B-C1-C-D1-D2-D).docx",
        "resources/docx-template/examiner/BB3(B-C1-C-D1-D2-D).docx",
    ]),
    ("Update examiner base CSS", ["web/assets/css/examiner/base.css"]),
    ("Update examiner dashboard CSS", ["web/assets/css/examiner/dashboard.css"]),
    ("Update examiner devices CSS", ["web/assets/css/examiner/devices.css"]),
    ("Update examiner score-entry CSS", ["web/assets/css/examiner/score-entry.css"]),
    ("Update examiner sidebar CSS", ["web/assets/css/examiner/sidebar.css"]),
    ("Add examiner print document CSS", ["web/assets/css/examiner/print-document.css"]),
    ("Remove examiner confirmation CSS", ["web/assets/css/examiner/confirmation.css"]),
    ("Add examiner print viewer JS", ["web/assets/js/examiner-print-viewer.js"]),
    ("Update candidate call JS", ["web/assets/js/candidatecall.js"]),
    ("Add examiner action view", ["web/views/examiner/action.jsp"]),
    ("Add examiner candidates list view", ["web/views/examiner/candidates.jsp"]),
    ("Update examiner audit view", ["web/views/examiner/audit.jsp"]),
    ("Update examiner candidate details view", ["web/views/examiner/candidate-details.jsp"]),
    ("Update examiner candidate paper view", ["web/views/examiner/candidate-paper.jsp"]),
    ("Update examiner dashboard view", ["web/views/examiner/dashboard.jsp"]),
    ("Update examiner devices view", ["web/views/examiner/devices.jsp"]),
    ("Update examiner exam select view", ["web/views/examiner/exam-select.jsp"]),
    ("Update examiner export view", ["web/views/examiner/export.jsp"]),
    ("Update examiner print documents view", ["web/views/examiner/print-documents.jsp"]),
    ("Update examiner result details views", [
        "web/views/examiner/result-details.jsp",
        "web/views/examiner/result-details-edit.jsp",
    ]),
    ("Update examiner score entry view", ["web/views/examiner/score-entry.jsp"]),
    ("Update examiner violations view", ["web/views/examiner/violations.jsp"]),
    ("Remove legacy examiner views batch 1", [
        "web/views/examiner/candidate-call.jsp",
        "web/views/examiner/candidate-details-edit.jsp",
        "web/views/examiner/confirmation.jsp",
    ]),
    ("Remove legacy examiner views batch 2", [
        "web/views/examiner/violation-confirm.jsp",
        "web/views/examiner/violation-detail.jsp",
        "web/views/examiner/violation-undo.jsp",
    ]),
    ("Update examiner candidate list component", ["web/views/examiner/components/candidate-list.jsp"]),
    ("Update examiner device grid component", ["web/views/examiner/components/device-grid.jsp"]),
    ("Update examiner document rows component", ["web/views/examiner/components/document-rows.jsp"]),
    ("Update examiner messages component", ["web/views/examiner/components/examiner-messages.jsp"]),
    ("Update examiner export row component", ["web/views/examiner/components/export-row.jsp"]),
    ("Update examiner faults component", ["web/views/examiner/components/faults.jsp"]),
    ("Update examiner sidebar component", ["web/views/examiner/components/sidebar.jsp"]),
    ("Update examiner sort header component", ["web/views/examiner/components/sort-th.jsp"]),
    ("Update examiner toolbar component", ["web/views/examiner/components/toolbar.jsp"]),
    ("Update examiner layout sidebar", ["web/views/layout/sidebar-examiner.jsp"]),
    ("Add examiner print views", ["web/views/examiner/print/"]),
]


def jitter_minute(rng: random.Random) -> int:
    while True:
        m = rng.randint(0, 59)
        if m % 5 != 0 or rng.random() < 0.35:
            return m


def random_time_in_window(rng: random.Random, window: str) -> tuple[int, int, int]:
    if window == "morning":
        hour = rng.randint(9, 11)
        if hour == 11:
            minute = rng.randint(3, 58)
        else:
            minute = jitter_minute(rng)
    elif window == "afternoon":
        hour = rng.randint(15, 17)
        if hour == 17:
            minute = rng.randint(0, 29)
        else:
            minute = jitter_minute(rng)
    elif window == "evening":
        hour = rng.randint(19, 22)
        if hour == 19:
            minute = rng.randint(25, 58)
        elif hour == 22:
            minute = rng.randint(0, 57)
        else:
            minute = jitter_minute(rng)
    else:  # late
        if rng.random() < 0.55:
            hour, minute = 23, rng.randint(8, 59)
        else:
            hour, minute = 0, rng.randint(3, 47)
    second = rng.randint(3, 59)
    return hour, minute, second


def generate_timestamps(count: int) -> list[str]:
    rng = random.Random(SEED)
    days = []
    cur = START
    while cur <= END:
        days.append(cur)
        cur += timedelta(days=1)

    weights = [0.42, 0.14, 0.34, 0.10]
    windows = ["morning", "afternoon", "evening", "late"]

    # assign commits to days with slight ramp toward end
    day_weights = [1.0 + (i / max(len(days) - 1, 1)) * 0.8 for i in range(len(days))]
    day_assign = []
    for _ in range(count):
        day_assign.append(rng.choices(days, weights=day_weights, k=1)[0])

    day_assign.sort()
    stamps: list[datetime] = []
    prev: datetime | None = None
    for day in day_assign:
        window = rng.choices(windows, weights=weights, k=1)[0]
        h, mi, s = random_time_in_window(rng, window)
        dt = datetime.combine(day, datetime.min.time().replace(hour=h, minute=mi, second=s))
        if prev is not None and dt <= prev:
            dt = prev + timedelta(minutes=rng.randint(8, 47), seconds=rng.randint(5, 50))
        stamps.append(dt)
        prev = dt

    return [dt.strftime("%Y-%m-%dT%H:%M:%S+0700") for dt in stamps]


def git(args: list[str], env: dict | None = None) -> None:
    cmd = ["git"] + args
    subprocess.run(cmd, check=True, env=env)


def main() -> int:
    if len(COMMITS) == 0:
        print("No commits defined")
        return 1

    stamps = generate_timestamps(len(COMMITS))
    print(f"Planned {len(COMMITS)} commits from {stamps[0]} to {stamps[-1]}")

    for i, (msg, paths) in enumerate(COMMITS):
        git(["add", "-A", "--"] + paths)
        staged = subprocess.run(
            ["git", "diff", "--cached", "--quiet"],
            capture_output=True,
        )
        if staged.returncode == 0:
            print(f"SKIP (empty): {msg}")
            continue
        env = os.environ.copy()
        env["GIT_AUTHOR_DATE"] = stamps[i]
        env["GIT_COMMITTER_DATE"] = stamps[i]
        git(["commit", "--author", AUTHOR, "-m", msg], env=env)
        print(f"[{i+1}/{len(COMMITS)}] {stamps[i]} {msg}")

    # Commit any remaining tracked changes not covered above.
    git(["add", "-A", ":!priv"])
    staged = subprocess.run(["git", "diff", "--cached", "--quiet"], capture_output=True)
    if staged.returncode != 0:
        idx = min(len(COMMITS), len(stamps) - 1)
        env = os.environ.copy()
        env["GIT_AUTHOR_DATE"] = stamps[idx]
        env["GIT_COMMITTER_DATE"] = stamps[idx]
        git(["commit", "--author", AUTHOR, "-m", "Update remaining examiner integration files"], env=env)
        print(f"[leftover] {stamps[idx]} Update remaining examiner integration files")

    return 0


if __name__ == "__main__":
    sys.exit(main())
