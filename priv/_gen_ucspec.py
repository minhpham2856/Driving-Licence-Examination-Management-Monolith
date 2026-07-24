#!/usr/bin/env python3
"""Generate priv/UCspec.md from priv/UC.md with servlet-aware use case specifications."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
UCSPEC_PATH = ROOT / "UCspec.md"
UC_PATH = ROOT / "UC.md"
EXAMPLE_LINE_COUNT = 14

SECTIONS = [
    ("2.1", "Information", [1, 2, 3, 4, 9, 16, 17, 25, 26, 56]),
    ("2.2", "Authentication", [5, 6, 7, 8, 23, 24]),
    ("2.3", "Profile Management", [10, 11, 30]),
    ("2.4", "Exam Registration", [12, 13, 14, 15, 18, 28]),
    ("2.5", "Examination", [19, 21, 22, 31, 36, 37, 38, 39, 40, 42, 43, 44, 45, 46]),
    ("2.6", "Payment Processing", [20]),
    ("2.7", "User Management", [27, 29]),
    ("2.8", "Exam Management", [32, 33, 35]),
    ("2.9", "Communication", [34]),
    ("2.10", "File Processing", [41, 47]),
    ("2.11", "Administration", [48, 49, 50, 51, 52, 53, 54, 55]),
]

TABLE_FIELDS = [
    "ID and Name",
    "Created By",
    "Primary Actor",
    "Secondary Actors",
    "Description",
    "Trigger",
    "Preconditions",
    "Postconditions",
    "Normal Flow",
    "Alternative Flows",
    "Exceptions",
]


def parse_uc_table(path: Path) -> dict[int, dict[str, str]]:
    text = path.read_text(encoding="utf-8")
    rows: dict[int, dict[str, str]] = {}
    for line in text.splitlines():
        line = line.strip()
        if not line.startswith("| UC-"):
            continue
        cells = [c.strip() for c in line.strip("|").split("|")]
        if len(cells) < 5:
            continue
        uc_id = int(cells[0].replace("UC-", ""))
        rows[uc_id] = {
            "id": cells[0],
            "name": cells[1],
            "actors": cells[2],
            "feature": cells[3],
            "description": cells[4],
        }
    return rows


def split_actors(raw: str) -> tuple[str, str]:
    parts = [p.strip() for p in raw.split(",")]
    if len(parts) == 1:
        return parts[0], "None"
    return parts[0], ", ".join(parts[1:])


def br(items: list[str]) -> str:
    return "<br>".join(items)


def flow_block(title: str, steps: list[str]) -> str:
    lines = [f"{title}<br>"]
    for idx, step in enumerate(steps, start=1):
        lines.append(f"{idx}. {step}<br>")
    return "".join(lines).rstrip("<br>")


def alt_block(entries: list[tuple[str, list[str]]]) -> str:
    chunks: list[str] = []
    for code, steps in entries:
        chunks.append(f"{code}<br>")
        for step in steps:
            chunks.append(f"{step}<br>")
        chunks.append("<br>")
    return "".join(chunks).rstrip("<br>")


# Servlet-aware specification templates keyed by UC number.
UC_TEMPLATES: dict[int, dict[str, object]] = {
    1: {
        "trigger": "Guest navigates to /home.",
        "pre": [
            "PRE-1: Guest is not required to be logged in.",
            "PRE-2: Home page is accessible at /home.",
            "PRE-3: Database connection to DLEM_DB is available.",
        ],
        "post": [
            "POST-1: System displays the landing page overview.",
            "POST-2: Navigation links to public information pages are available.",
        ],
        "normal": [
            "1.0 View home page",
            "Guest opens /home (HomeServlet).",
            "System loads public content via general.controller.HomeServlet.",
            "System forwards to web/views/general/home.jsp.",
            "Guest views system overview, licence highlights, and navigation links.",
        ],
        "alt": [
            (
                "A1 Guest selects another public page",
                [
                    "A1.1. At step 5, guest clicks a navigation link (e.g. /license-categories, /process).",
                    "A1.2. System navigates to the selected public page.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Database unavailable",
                [
                    "E1.1. At step 2, database connection fails.",
                    "E1.2. System displays: \"Không thể tải trang. Vui lòng thử lại.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    2: {
        "trigger": "Guest selects a licence type on /license-categories.",
        "pre": [
            "PRE-1: Guest is not required to be logged in.",
            "PRE-2: Licence categories page is accessible at /license-categories.",
            "PRE-3: Licence records exist in the Licence table.",
        ],
        "post": [
            "POST-1: Detailed licence information is displayed for the selected category.",
            "POST-2: Guest remains on the public information area.",
        ],
        "normal": [
            "1.0 View licence details",
            "Guest opens /license-categories (LicenceCategoriesServlet).",
            "System loads licence categories from LicenceService.",
            "Guest selects a licence type to view details.",
            "System displays fees, requirements, and description for the selected licence.",
        ],
        "alt": [
            (
                "A1 Guest returns to home",
                [
                    "A1.1. At step 5, guest clicks Home.",
                    "A1.2. System navigates to /home.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Licence not found",
                [
                    "E1.1. At step 4, selected licence id is invalid.",
                    "E1.2. System displays: \"Không tìm thấy hạng giấy phép.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    3: {
        "trigger": "Guest navigates to /license-categories.",
        "pre": [
            "PRE-1: Guest is not required to be logged in.",
            "PRE-2: Licence categories page is accessible at /license-categories.",
        ],
        "post": [
            "POST-1: All active licence categories are listed.",
            "POST-2: Guest can navigate to licence details.",
        ],
        "normal": [
            "1.0 View licence categories",
            "Guest opens /license-categories (LicenceCategoriesServlet).",
            "System queries LicenceDAO for all licence categories.",
            "System forwards to web/views/general/license-categories.jsp with the category list.",
            "Guest views available categories (A, B, C, etc.).",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Empty catalogue",
                [
                    "E1.1. At step 3, no licence categories are returned.",
                    "E1.2. System displays: \"Chưa có hạng giấy phép nào.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    4: {
        "trigger": "Guest navigates to /process.",
        "pre": [
            "PRE-1: Guest is not required to be logged in.",
            "PRE-2: Process page is accessible at /process.",
        ],
        "post": [
            "POST-1: Step-by-step licence acquisition process is displayed.",
        ],
        "normal": [
            "1.0 View process page",
            "Guest opens /process (ProcessServlet).",
            "System forwards to web/views/general/process.jsp.",
            "Guest reads the step-by-step process for obtaining a driving licence.",
        ],
        "alt": [],
        "exc": [],
    },
    5: {
        "trigger": "Guest submits the registration form at /register.",
        "pre": [
            "PRE-1: Guest is not required to be logged in.",
            "PRE-2: Registration page is accessible at /register.",
            "PRE-3: Database connection to DLEM_DB is available.",
            "PRE-4: Role Registrant exists in the Role table.",
        ],
        "post": [
            "POST-1: A new active User record is created with Registrant role.",
            "POST-2: Username and email are unique in the system.",
            "POST-3: Guest is redirected to /login with a success flash message.",
        ],
        "normal": [
            "1.0 Register registrant account",
            "Guest opens /register (RegisterServlet).",
            "System displays the registration form.",
            "Guest submits username, email, password, confirm password, and accepts terms.",
            "System validates required fields and password confirmation.",
            "System checks username and email uniqueness via AuthService.",
            "System creates User with Registrant role and isActive = true.",
            "System stores success message in session and redirects to /login.",
        ],
        "alt": [
            (
                "A1 Duplicate username",
                [
                    "A1.1. At step 6, username already exists.",
                    "A1.2. System displays: \"Tên đăng nhập đã tồn tại.\"",
                    "A1.3. Guest remains on /register.",
                ],
            ),
            (
                "A2 Duplicate email",
                [
                    "A2.1. At step 6, email already exists.",
                    "A2.2. System displays: \"Email đã được sử dụng.\"",
                    "A2.3. Guest remains on /register.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Missing required fields",
                [
                    "E1.1. At step 5, required fields are empty.",
                    "E1.2. System displays: \"Vui lòng điền vào ô trống.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
            (
                "E2 Password mismatch",
                [
                    "E2.1. At step 5, password and confirm password do not match.",
                    "E2.2. System displays: \"Mật khẩu nhập lại không khớp.\"",
                    "E2.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    6: {
        "trigger": "Registrant submits credentials at /login.",
        "pre": [
            "PRE-1: Registrant account exists and is active.",
            "PRE-2: Login page is accessible at /login.",
        ],
        "post": [
            "POST-1: Registrant session is created with user profile.",
            "POST-2: Registrant is redirected to /views/registrant/dashboard.jsp.",
        ],
        "normal": [
            "1.0 Login as registrant",
            "Registrant opens /login (auth.controller.general.LoginServlet).",
            "Registrant enters username and password and submits.",
            "System validates credentials via AuthService.",
            "System stores user in session and redirects to registrant dashboard.",
        ],
        "alt": [
            (
                "A1 Invalid credentials",
                [
                    "A1.1. At step 4, username or password is incorrect.",
                    "A1.2. System displays: \"Tên đăng nhập hoặc mật khẩu không đúng.\"",
                    "A1.3. Registrant remains on /login.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Account inactive",
                [
                    "E1.1. At step 4, account is locked or inactive.",
                    "E1.2. System displays: \"Tài khoản đã bị khóa.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    7: {
        "trigger": "Registrant selects logout at /logout.",
        "pre": [
            "PRE-1: Registrant has an active session.",
        ],
        "post": [
            "POST-1: Session is invalidated.",
            "POST-2: Registrant is redirected to /home or /login.",
        ],
        "normal": [
            "1.0 Logout registrant",
            "Registrant clicks logout.",
            "System invokes /logout (LogoutServlet).",
            "System invalidates the HTTP session.",
            "System redirects to public landing page.",
        ],
        "alt": [],
        "exc": [],
    },
    8: {
        "trigger": "Registrant submits the change-password form at /change-password.",
        "pre": [
            "PRE-1: Registrant is logged in.",
            "PRE-2: Change password page is accessible at /change-password.",
        ],
        "post": [
            "POST-1: Password hash is updated in the User record.",
            "POST-2: Success message is shown to the registrant.",
        ],
        "normal": [
            "1.0 Change password",
            "Registrant opens /change-password (ChangePasswordServlet).",
            "Registrant enters current password, new password, and confirmation.",
            "System validates current password and new password rules.",
            "System updates password via AuthService.",
            "System displays success and keeps registrant logged in.",
        ],
        "alt": [
            (
                "A1 Wrong current password",
                [
                    "A1.1. At step 4, current password is incorrect.",
                    "A1.2. System displays: \"Mật khẩu hiện tại không đúng.\"",
                    "A1.3. Use case resumes at step 2.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Password mismatch",
                [
                    "E1.1. At step 4, new password and confirmation differ.",
                    "E1.2. System displays: \"Mật khẩu nhập lại không khớp.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    9: {
        "trigger": "Registrant navigates to /views/registrant/dashboard.jsp.",
        "pre": [
            "PRE-1: Registrant is logged in.",
        ],
        "post": [
            "POST-1: Dashboard summary of registrations, exams, and notifications is displayed.",
        ],
        "normal": [
            "1.0 View registrant dashboard",
            "Registrant opens registrant dashboard after login.",
            "System loads profile summary, upcoming exams, and registration status.",
            "Registrant reviews personal status and quick links.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Session expired",
                [
                    "E1.1. At step 1, session is missing or expired.",
                    "E1.2. System redirects to /login.",
                    "E1.3. System displays: \"Phiên đăng nhập đã hết hạn.\"",
                ],
            ),
        ],
    },
    10: {
        "trigger": "Registrant submits personal information at /views/registrant/profile.jsp.",
        "pre": [
            "PRE-1: Registrant is logged in.",
            "PRE-2: Profile page is accessible.",
        ],
        "post": [
            "POST-1: Person/Profile records are updated.",
            "POST-2: Updated information is shown on the profile page.",
        ],
        "normal": [
            "1.0 Update personal information",
            "Registrant opens /views/registrant/profile.jsp.",
            "System displays current personal and contact details.",
            "Registrant edits fields and submits the form.",
            "System validates input and persists changes via ProfileService.",
            "System confirms update to the registrant.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Validation failure",
                [
                    "E1.1. At step 4, required fields are invalid.",
                    "E1.2. System displays: \"Thông tin cá nhân không hợp lệ.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    11: {
        "trigger": "Registrant uploads or updates documents at /views/registrant/upload-documents.jsp.",
        "pre": [
            "PRE-1: Registrant is logged in.",
            "PRE-2: Required document types are configured.",
        ],
        "post": [
            "POST-1: Document metadata and files are stored.",
            "POST-2: Document list reflects the latest uploads.",
        ],
        "normal": [
            "1.0 Manage documents",
            "Registrant opens /views/registrant/upload-documents.jsp.",
            "System lists required documents and current upload status.",
            "Registrant selects file(s) and submits upload.",
            "System validates file type/size and stores document records.",
            "System refreshes document status on the page.",
        ],
        "alt": [
            (
                "A1 Replace existing document",
                [
                    "A1.1. At step 3, registrant chooses to replace an uploaded document.",
                    "A1.2. System overwrites prior file reference after validation.",
                    "A1.3. Use case continues at step 5.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Invalid file",
                [
                    "E1.1. At step 4, file type or size is not allowed.",
                    "E1.2. System displays: \"Tệp tải lên không hợp lệ.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    12: {
        "trigger": "Registrant submits exam registration at /views/registrant/register-exam.jsp.",
        "pre": [
            "PRE-1: Registrant is logged in with complete profile.",
            "PRE-2: An open exam session exists for the selected licence category.",
        ],
        "post": [
            "POST-1: ExamEnrollment record is created with pending status.",
            "POST-2: Registrant sees confirmation of registration request.",
        ],
        "normal": [
            "1.0 Register for exam",
            "Registrant opens /views/registrant/register-exam.jsp.",
            "System lists available exam sessions and licence categories.",
            "Registrant selects exam date, category, and submits registration.",
            "System validates eligibility and capacity via RegistrationService.",
            "System creates ExamEnrollment with pending status.",
            "System shows confirmation and redirects to registration list.",
        ],
        "alt": [
            (
                "A1 Exam full",
                [
                    "A1.1. At step 5, selected exam has no remaining capacity.",
                    "A1.2. System displays: \"Kỳ thi đã đủ số lượng đăng ký.\"",
                    "A1.3. Registrant selects another exam.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Incomplete profile",
                [
                    "E1.1. At step 5, required profile/documents are missing.",
                    "E1.2. System displays: \"Vui lòng hoàn thiện hồ sơ trước khi đăng ký.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    13: {
        "trigger": "Registrant opens /views/registrant/my-exams.jsp.",
        "pre": ["PRE-1: Registrant is logged in."],
        "post": ["POST-1: List of registrant exam enrollments is displayed."],
        "normal": [
            "1.0 View exam registrations",
            "Registrant navigates to /views/registrant/my-exams.jsp.",
            "System loads all ExamEnrollment records for the registrant.",
            "Registrant reviews registered exams, dates, and statuses.",
        ],
        "alt": [],
        "exc": [],
    },
    14: {
        "trigger": "Registrant views schedule on /views/registrant/my-exams.jsp or dashboard.",
        "pre": ["PRE-1: Registrant is logged in.", "PRE-2: Registrant has at least one approved enrollment."],
        "post": ["POST-1: Exam date, time, and location are displayed."],
        "normal": [
            "1.0 View exam schedule",
            "Registrant opens exam registration or dashboard page.",
            "System loads linked Exam and ExamArea details.",
            "Registrant views scheduled date, time, and location.",
        ],
        "alt": [],
        "exc": [],
    },
    15: {
        "trigger": "Registrant opens /views/registrant/track-profile.jsp.",
        "pre": ["PRE-1: Registrant is logged in."],
        "post": ["POST-1: Current registration processing status is displayed."],
        "normal": [
            "1.0 Track registration status",
            "Registrant opens /views/registrant/track-profile.jsp.",
            "System loads enrollment and review status history.",
            "Registrant views pending, approved, rejected, or on-hold status.",
        ],
        "alt": [],
        "exc": [],
    },
    16: {
        "trigger": "Registrant views scores on /views/registrant/my-exams.jsp.",
        "pre": ["PRE-1: Registrant is logged in.", "PRE-2: Completed exam results exist."],
        "post": ["POST-1: Theory and practical scores are displayed."],
        "normal": [
            "1.0 View exam scores",
            "Registrant opens my-exams or score detail view.",
            "System loads ExamResult and ExamScore records.",
            "Registrant reviews past exam scores and pass/fail outcome.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 No results yet",
                [
                    "E1.1. At step 2, no finalized results exist.",
                    "E1.2. System displays: \"Chưa có kết quả thi.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    17: {
        "trigger": "Registrant selects a specific exam from /views/registrant/my-exams.jsp.",
        "pre": ["PRE-1: Registrant is logged in.", "PRE-2: Selected enrollment exists."],
        "post": ["POST-1: Detailed exam session information is displayed."],
        "normal": [
            "1.0 View exam details",
            "Registrant selects an exam from the registration list.",
            "System loads exam session, licence category, area, and status.",
            "Registrant reviews detailed exam information.",
        ],
        "alt": [],
        "exc": [],
    },
    18: {
        "trigger": "Registrant submits cancellation request from exam detail or my-exams page.",
        "pre": [
            "PRE-1: Registrant is logged in.",
            "PRE-2: Enrollment is in a cancellable state.",
        ],
        "post": [
            "POST-1: Cancellation request is recorded.",
            "POST-2: Enrollment status reflects cancellation pending or cancelled.",
        ],
        "normal": [
            "1.0 Request exam cancellation",
            "Registrant opens exam detail from /views/registrant/my-exams.jsp.",
            "Registrant submits cancellation request with reason.",
            "System validates cancellation window and updates enrollment status.",
            "System notifies registrant of submitted cancellation request.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Cancellation not allowed",
                [
                    "E1.1. At step 3, exam date is too close or exam already started.",
                    "E1.2. System displays: \"Không thể hủy đăng ký trong giai đoạn này.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    19: {
        "trigger": "Exam Staff captures candidate photo at /views/staff/examstaff/candidate-photo.",
        "pre": [
            "PRE-1: Exam Staff is logged in via /staff/login.",
            "PRE-2: Active exam session is selected in examstaff context.",
            "PRE-3: Candidate is registered for the session.",
        ],
        "post": [
            "POST-1: Candidate identity photo is stored.",
            "POST-2: Candidate is marked ready for check-in/procedure.",
        ],
        "normal": [
            "1.0 Authenticate candidate identity",
            "Exam Staff opens /views/staff/examstaff/candidate-photo (CandidatePhotoServlet).",
            "Exam Staff selects candidate and captures/uploads identity photo.",
            "System validates candidate enrollment for active exam.",
            "System stores photo and updates candidate verification status.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Candidate not found",
                [
                    "E1.1. At step 3, candidate id is invalid for active exam.",
                    "E1.2. System displays: \"Không tìm thấy thí sinh.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    20: {
        "trigger": "Candidate initiates payment from exam registration or check-in flow.",
        "pre": [
            "PRE-1: Candidate registration is approved.",
            "PRE-2: Exam fee amount is configured for licence category.",
        ],
        "post": [
            "POST-1: Payment record is created with paid or pending status.",
            "POST-2: Candidate may proceed to examination when payment confirmed.",
        ],
        "normal": [
            "1.0 Pay exam fee",
            "Candidate selects payment method (QR code or cash).",
            "System displays payable amount from Licence fee configuration.",
            "Candidate completes payment or staff confirms cash receipt.",
            "System records Payment with status Paid.",
            "System updates enrollment payment status.",
        ],
        "alt": [
            (
                "A1 QR payment pending confirmation",
                [
                    "A1.1. At step 4, online payment awaits gateway confirmation.",
                    "A1.2. System stores Payment as Pending.",
                    "A1.3. Candidate is notified when payment is confirmed.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Payment failed",
                [
                    "E1.1. At step 4, payment cannot be processed.",
                    "E1.2. System displays: \"Thanh toán thất bại. Vui lòng thử lại.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    21: {
        "trigger": "Candidate begins exam after call from /views/staff/examstaff/candidatecall.",
        "pre": [
            "PRE-1: Candidate is checked in and payment confirmed.",
            "PRE-2: Exam session status is In Progress.",
        ],
        "post": [
            "POST-1: Candidate completes theory and/or practical sections.",
            "POST-2: ExamResult and scores are stored.",
        ],
        "normal": [
            "1.0 Take exam",
            "Exam Staff calls candidate via /views/staff/examstaff/candidatecall.",
            "Candidate proceeds to theory station or practical exam area.",
            "Examiner manages scoring at /views/examiner/score-entry and /views/examiner/exam.",
            "System records answers, deductions, and final result.",
        ],
        "alt": [
            (
                "A1 Theory only session",
                [
                    "A1.1. At step 3, exam section is Theory.",
                    "A1.2. System generates paper via /views/examiner/exam.",
                    "A1.3. Candidate completes theory and result is finalized.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Candidate not called",
                [
                    "E1.1. At step 2, candidate is not in active queue.",
                    "E1.2. System displays: \"Thí sinh chưa được gọi thi.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    22: {
        "trigger": "Candidate submits or auto-saves answers during active exam session.",
        "pre": [
            "PRE-1: Candidate exam session is in progress.",
            "PRE-2: Theory paper or practical score entry is active.",
        ],
        "post": [
            "POST-1: Answers or interim scores are persisted.",
            "POST-2: Candidate can resume from saved state.",
        ],
        "normal": [
            "1.0 Save answers during exam",
            "Candidate answers question or examiner records interim score.",
            "System saves CandidateAnswer or ExamScore via examiner/examstaff services.",
            "System confirms save without ending the session.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Save failure",
                [
                    "E1.1. At step 2, database write fails.",
                    "E1.2. System displays: \"Không thể lưu câu trả lời. Vui lòng thử lại.\"",
                    "E1.3. Use case resumes at step 1.",
                ],
            ),
        ],
    },
    23: {
        "trigger": "Staff member uses /staff/login, /staff/logout, or /change-password.",
        "pre": [
            "PRE-1: Staff account exists with appropriate role.",
            "PRE-2: Staff login page is accessible at /staff/login.",
        ],
        "post": [
            "POST-1: Staff session is created, ended, or password updated.",
            "POST-2: Staff is redirected to role dashboard (examstaff/examiner/admin).",
        ],
        "normal": [
            "1.0 Staff authentication actions",
            "Staff opens /staff/login (auth.controller.internal.LoginServlet).",
            "Staff submits credentials; system validates role (Managing Staff, Exam Staff, Examiner, Admin).",
            "System creates session and redirects to role dashboard.",
            "For logout, staff invokes /staff/logout.",
            "For password change, staff uses /change-password while logged in.",
        ],
        "alt": [
            (
                "A1 Logout",
                [
                    "A1.1. Staff selects logout.",
                    "A1.2. System invalidates session via /staff/logout.",
                    "A1.3. Staff is redirected to /staff/login.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Unauthorized role",
                [
                    "E1.1. At step 3, account lacks staff role.",
                    "E1.2. System displays: \"Bạn không có quyền truy cập.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    24: {
        "trigger": "Staff submits forgot-password form at /forgot-password.",
        "pre": [
            "PRE-1: Staff account email exists in the system.",
            "PRE-2: Mail SMTP configuration is available.",
        ],
        "post": [
            "POST-1: Password reset token or temporary password is sent by email.",
            "POST-2: Staff can log in with new credentials at /staff/login.",
        ],
        "normal": [
            "1.0 Recover password",
            "Staff opens /forgot-password (ForgotPasswordServlet).",
            "Staff enters registered email and submits.",
            "System validates email and generates reset credentials.",
            "System sends recovery email via EmailService.",
            "Staff receives email and resets password.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Email not found",
                [
                    "E1.1. At step 3, email is not registered.",
                    "E1.2. System displays: \"Email không tồn tại trong hệ thống.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
            (
                "E2 Email send failure",
                [
                    "E2.1. At step 4, SMTP send fails.",
                    "E2.2. System displays: \"Không thể gửi email. Vui lòng thử lại.\"",
                    "E2.3. Use case ends.",
                ],
            ),
        ],
    },
    25: {
        "trigger": "Authenticated user opens role-specific dashboard.",
        "pre": ["PRE-1: User is logged in with a valid role."],
        "post": ["POST-1: Role-appropriate summary dashboard is displayed."],
        "normal": [
            "1.0 View role dashboard",
            "User logs in via /login or /staff/login.",
            "System routes to dashboard by role:",
            "Registrant -> /views/registrant/dashboard.jsp;",
            "Exam Staff -> /views/staff/examstaff/dashboard (DashboardServlet);",
            "Examiner -> /views/examiner/dashboard (ExaminerDashboardServlet);",
            "Managing Staff -> /views/staff/managing/dashboard.jsp;",
            "Admin -> /admin/dashboard.",
            "User reviews summary metrics and navigation.",
        ],
        "alt": [],
        "exc": [],
    },
    26: {
        "trigger": "Managing Staff opens /views/staff/examstaff/audit.",
        "pre": [
            "PRE-1: Managing Staff is logged in via /staff/login.",
            "PRE-2: Audit records exist in Audit table.",
        ],
        "post": ["POST-1: Filtered audit log entries are displayed."],
        "normal": [
            "1.0 View audit log (Managing Staff)",
            "Managing Staff navigates to /views/staff/examstaff/audit (AuditServlet).",
            "System loads audit entries via AuditService.",
            "Managing Staff filters/searches actions by user, date, or action type.",
            "System displays audit log table.",
        ],
        "alt": [
            (
                "A1 Export audit log",
                [
                    "A1.1. At step 4, staff clicks export.",
                    "A1.2. System generates file via /views/staff/examstaff/audit-export.",
                ],
            ),
        ],
        "exc": [],
    },
    27: {
        "trigger": "Managing Staff opens registrant management at /views/staff/managing/registrants.jsp.",
        "pre": ["PRE-1: Managing Staff is logged in.", "PRE-2: Registrant records exist."],
        "post": ["POST-1: Searchable registrant list is displayed."],
        "normal": [
            "1.0 Manage registrants",
            "Managing Staff opens registrant list page.",
            "System loads registrants with pagination, search, and sort.",
            "Managing Staff searches or sorts the list.",
            "System refreshes results according to criteria.",
        ],
        "alt": [],
        "exc": [],
    },
    28: {
        "trigger": "Managing Staff updates enrollment status from registration review screen.",
        "pre": [
            "PRE-1: Managing Staff is logged in.",
            "PRE-2: Pending ExamEnrollment exists.",
        ],
        "post": [
            "POST-1: Enrollment status is updated (approved, rejected, on hold).",
            "POST-2: Registrant can track new status.",
        ],
        "normal": [
            "1.0 Update registration status",
            "Managing Staff opens pending registration review.",
            "Staff selects approve, reject, or hold and enters reason.",
            "System validates status transition via RegistrationService.",
            "System updates ExamEnrollment and writes audit log.",
            "System notifies registrant if configured.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Missing reason",
                [
                    "E1.1. At step 3, reject/hold requires reason but none provided.",
                    "E1.2. System displays: \"Vui lòng nhập lý do.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    29: {
        "trigger": "Managing Staff submits new user form from staff user management.",
        "pre": [
            "PRE-1: Managing Staff is logged in.",
            "PRE-2: Target role exists in Role table.",
        ],
        "post": [
            "POST-1: New User and Profile records are created.",
            "POST-2: Credentials are communicated to the new user.",
        ],
        "normal": [
            "1.0 Register new user (staff)",
            "Managing Staff opens user creation form.",
            "Staff enters personal details, role, and contact information.",
            "System validates uniqueness and required fields.",
            "System creates account via AuthService/RegistrationService.",
            "System displays confirmation with generated credentials.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Duplicate username",
                [
                    "E1.1. At step 4, username exists.",
                    "E1.2. System displays: \"Tên đăng nhập đã tồn tại.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    30: {
        "trigger": "Managing Staff updates registrant documents from staff document review.",
        "pre": [
            "PRE-1: Managing Staff is logged in.",
            "PRE-2: Registrant profile and documents exist.",
        ],
        "post": [
            "POST-1: Document records are corrected or replaced.",
            "POST-2: Registrant document status is updated.",
        ],
        "normal": [
            "1.0 Update documents (staff)",
            "Managing Staff opens registrant document review.",
            "Staff uploads corrected document or updates metadata.",
            "System validates and persists document changes.",
            "System records audit entry and updates registrant status.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Invalid document",
                [
                    "E1.1. At step 3, file fails validation.",
                    "E1.2. System displays: \"Tệp tài liệu không hợp lệ.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    31: {
        "trigger": "Managing Staff uploads candidate file at /staff/examstaff/upload.",
        "pre": [
            "PRE-1: Managing Staff or Exam Staff is logged in.",
            "PRE-2: Target exam session is selected.",
            "PRE-3: Upload template format is valid.",
        ],
        "post": [
            "POST-1: Candidate rows are imported into the exam roster.",
            "POST-2: Import summary with success/error counts is displayed.",
        ],
        "normal": [
            "1.0 Import candidate list",
            "Staff opens /staff/examstaff/upload (ExamStaffUploadServlet).",
            "Staff selects exam and uploads Excel file.",
            "System parses rows and validates required fields.",
            "System bulk-inserts Candidate and ExamEnrollment records.",
            "System displays import result summary.",
        ],
        "alt": [
            (
                "A1 Partial import",
                [
                    "A1.1. At step 4, some rows fail validation.",
                    "A1.2. System imports valid rows and lists errors per row.",
                    "A1.3. Staff corrects file and re-uploads.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Invalid file format",
                [
                    "E1.1. At step 3, file is not a supported Excel template.",
                    "E1.2. System displays: \"Định dạng tệp không hợp lệ.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    32: {
        "trigger": "Managing Staff submits create-exam form from exam management.",
        "pre": [
            "PRE-1: Managing Staff is logged in.",
            "PRE-2: Licence category and exam area exist.",
        ],
        "post": [
            "POST-1: New Exam record is created with schedule and capacity.",
            "POST-2: Exam appears in management and registration lists.",
        ],
        "normal": [
            "1.0 Create new exam",
            "Managing Staff opens exam creation form.",
            "Staff enters date, time, licence category, area, and capacity.",
            "System validates schedule conflicts and capacity.",
            "System creates Exam via ExamService.",
            "System confirms creation and shows new exam in list.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Schedule conflict",
                [
                    "E1.1. At step 4, exam area/time conflicts with existing exam.",
                    "E1.2. System displays: \"Lịch thi bị trùng. Vui lòng chọn thời gian khác.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    33: {
        "trigger": "Managing Staff opens exam management list.",
        "pre": ["PRE-1: Managing Staff is logged in.", "PRE-2: Exam records exist."],
        "post": ["POST-1: Exam list reflects view, edit, or cancel actions."],
        "normal": [
            "1.0 Manage exams",
            "Managing Staff opens exam management page.",
            "System lists exams with status, date, and capacity.",
            "Staff views, edits, or cancels an exam session.",
            "System persists changes via ExamService and audit log.",
        ],
        "alt": [
            (
                "A1 Cancel exam",
                [
                    "A1.1. At step 4, staff cancels exam.",
                    "A1.2. System sets Exam status to Cancelled.",
                    "A1.3. Enrolled registrants are notified.",
                ],
            ),
        ],
        "exc": [],
    },
    34: {
        "trigger": "Managing Staff sends notification from communication module.",
        "pre": [
            "PRE-1: Managing Staff is logged in.",
            "PRE-2: Mail SMTP configuration is available.",
            "PRE-3: Target registrants are selected.",
        ],
        "post": [
            "POST-1: Notification emails are queued or sent.",
            "POST-2: Send status is recorded.",
        ],
        "normal": [
            "1.0 Notify registrants",
            "Managing Staff composes notification about upcoming exam.",
            "Staff selects recipient registrants or exam roster.",
            "System sends email via EmailService.",
            "System records notification outcome.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Send failure",
                [
                    "E1.1. At step 3, email delivery fails.",
                    "E1.2. System displays: \"Không thể gửi thông báo. Vui lòng thử lại.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    35: {
        "trigger": "Exam Staff opens /views/staff/examstaff/allocation.",
        "pre": [
            "PRE-1: Exam Staff is logged in via /staff/login.",
            "PRE-2: Active exam is selected at /views/staff/examstaff/select-exam.",
        ],
        "post": ["POST-1: Exam roster with candidate statuses is displayed."],
        "normal": [
            "1.0 Manage exam roster",
            "Exam Staff selects exam at /views/staff/examstaff/select-exam.",
            "Exam Staff opens /views/staff/examstaff/allocation (AllocationServlet).",
            "System loads candidate queue and stage views.",
            "Exam Staff reviews, filters, and manages roster assignments.",
        ],
        "alt": [
            (
                "A1 View stage-specific roster",
                [
                    "A1.1. Staff navigates to allocation-theory, allocation-practical, or allocation-waiting.",
                    "A1.2. System filters roster by section stage.",
                ],
            ),
        ],
        "exc": [],
    },
    36: {
        "trigger": "Exam Staff checks in candidate at /views/staff/examstaff/candidatecall.",
        "pre": [
            "PRE-1: Exam Staff is logged in.",
            "PRE-2: Active exam session is in progress or ready.",
            "PRE-3: Candidate is on the exam roster.",
        ],
        "post": [
            "POST-1: Candidate check-in status is updated.",
            "POST-2: Candidate appears in call queue.",
        ],
        "normal": [
            "1.0 Check in candidates",
            "Exam Staff opens /views/staff/examstaff/candidatecall (CandidateCallServlet).",
            "Staff confirms candidate presence and identity.",
            "System updates candidate status to checked-in.",
            "System adds candidate to active call queue.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Candidate absent",
                [
                    "E1.1. At step 3, candidate did not arrive.",
                    "E1.2. System marks absent via /views/staff/examstaff/procedure.",
                    "E1.3. Use case branches to UC-40.",
                ],
            ),
        ],
    },
    37: {
        "trigger": "Exam Staff opens /views/staff/examstaff/candidate-dossier.",
        "pre": [
            "PRE-1: Exam Staff is logged in.",
            "PRE-2: Candidate is registered for active exam.",
        ],
        "post": [
            "POST-1: Candidate dossier is reviewed and preparation status updated.",
        ],
        "normal": [
            "1.0 Process candidate profile",
            "Exam Staff opens /views/staff/examstaff/candidate-dossier (CandidateDossierServlet).",
            "System displays candidate profile, documents, and enrollment data.",
            "Exam Staff verifies completeness and confirms dossier ready.",
            "System updates candidate preparation status.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Incomplete dossier",
                [
                    "E1.1. At step 3, required documents are missing.",
                    "E1.2. System displays: \"Hồ sơ thí sinh chưa đầy đủ.\"",
                    "E1.3. Use case ends until documents are provided.",
                ],
            ),
        ],
    },
    38: {
        "trigger": "Exam Staff changes exam status at /views/staff/examstaff/exam-control.",
        "pre": [
            "PRE-1: Exam Staff is logged in.",
            "PRE-2: Exam session exists with valid status transition.",
        ],
        "post": [
            "POST-1: Exam status is updated (start, pause, stop).",
            "POST-2: Examiner and call-board views reflect new status.",
        ],
        "normal": [
            "1.0 Manage exam status",
            "Exam Staff opens /views/staff/examstaff/exam-control (ExamControlServlet).",
            "Staff selects start, pause, or stop action.",
            "System validates transition via ExamService.",
            "System updates Exam status and notifies dependent views.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Invalid transition",
                [
                    "E1.1. At step 3, status change is not allowed.",
                    "E1.2. System displays: \"Không thể thay đổi trạng thái kỳ thi.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    39: {
        "trigger": "Exam Staff ends exam at /views/staff/examstaff/exam-control.",
        "pre": [
            "PRE-1: Exam session is in progress.",
            "PRE-2: All active candidates are finalized or marked absent.",
        ],
        "post": [
            "POST-1: Exam status is set to Completed.",
            "POST-2: Results are locked for export.",
        ],
        "normal": [
            "1.0 End exam",
            "Exam Staff opens /views/staff/examstaff/exam-control.",
            "Staff confirms end-of-exam action.",
            "System finalizes open records and sets Exam status to Completed.",
            "System redirects staff to report/export options.",
        ],
        "alt": [],
        "exc": [],
    },
    40: {
        "trigger": "Exam Staff marks absent candidate at /views/staff/examstaff/procedure.",
        "pre": [
            "PRE-1: Exam Staff is logged in.",
            "PRE-2: Candidate did not check in before cutoff.",
        ],
        "post": [
            "POST-1: Candidate is marked absent.",
            "POST-2: Exam result reflects absence.",
        ],
        "normal": [
            "1.0 Handle absent candidate",
            "Exam Staff opens /views/staff/examstaff/procedure (ProcedureServlet).",
            "Staff selects candidate and marks as absent.",
            "System updates candidate and result status to Absent.",
            "System removes candidate from active call queue.",
        ],
        "alt": [],
        "exc": [],
    },
    41: {
        "trigger": "Exam Staff requests export at /views/staff/examstaff/report.",
        "pre": [
            "PRE-1: Exam Staff is logged in.",
            "PRE-2: Exam session has reportable data.",
        ],
        "post": [
            "POST-1: Excel or printable report file is generated.",
            "POST-2: File is downloaded or opened for printing.",
        ],
        "normal": [
            "1.0 Export files (Exam Staff)",
            "Exam Staff opens /views/staff/examstaff/report (ReportServlet).",
            "Staff selects report type and export format.",
            "System generates Excel/report via ExcelService.",
            "System returns file download or print view.",
        ],
        "alt": [
            (
                "A1 Print view",
                [
                    "A1.1. At step 3, staff chooses print.",
                    "A1.2. System opens report-print view.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Export failure",
                [
                    "E1.1. At step 3, report generation fails.",
                    "E1.2. System displays: \"Không thể xuất báo cáo. Vui lòng thử lại.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    42: {
        "trigger": "Examiner allocates candidates at /views/examiner/candidate-call.",
        "pre": [
            "PRE-1: Examiner is logged in via /staff/login.",
            "PRE-2: Active exam schedule is selected in examiner session.",
        ],
        "post": [
            "POST-1: Candidates are assigned to stations/queues.",
            "POST-2: Call board reflects allocation order.",
        ],
        "normal": [
            "1.0 Allocate candidates",
            "Examiner opens /views/examiner/candidate-call (ExaminerCandidateCallServlet).",
            "Examiner selects candidate and target station/section.",
            "System updates queue order via CallService.",
            "Public call board at /views/public/public-call reflects allocation.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 No active exam",
                [
                    "E1.1. At step 1, examiner session has no selected exam.",
                    "E1.2. System redirects to /views/examiner/exam-select.",
                    "E1.3. System displays: \"Vui lòng chọn kỳ thi.\"",
                ],
            ),
        ],
    },
    43: {
        "trigger": "Examiner enters scores at /views/examiner/score-entry.",
        "pre": [
            "PRE-1: Examiner is logged in.",
            "PRE-2: Candidate is called and exam section is active.",
        ],
        "post": [
            "POST-1: Scores and deductions are saved.",
            "POST-2: Result detail view shows updated outcome.",
        ],
        "normal": [
            "1.0 Manage exam results",
            "Examiner opens /views/examiner/score-entry (ExaminerScoreEntryServlet).",
            "Examiner records theory answers or practical deductions.",
            "System persists ExamScore and DeductionRecord via ExamScoreService.",
            "Examiner reviews result at /views/examiner/result-details.",
            "System calculates pass/fail and stores ExamResult.",
        ],
        "alt": [
            (
                "A1 Edit result",
                [
                    "A1.1. Examiner opens /views/examiner/result-details-edit.",
                    "A1.2. System allows authorized score correction before finalize.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Save failure",
                [
                    "E1.1. At step 3, score save fails.",
                    "E1.2. System displays: \"Không thể lưu điểm. Vui lòng thử lại.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    44: {
        "trigger": "Examiner cancels result at /views/examiner/result-details-edit.",
        "pre": [
            "PRE-1: Examiner is logged in.",
            "PRE-2: Candidate result exists and is cancellable.",
        ],
        "post": [
            "POST-1: Result is voided or marked cancelled.",
            "POST-2: Audit log records cancellation.",
        ],
        "normal": [
            "1.0 Cancel candidate result",
            "Examiner opens /views/examiner/result-details-edit.",
            "Examiner selects cancel/void result and confirms reason.",
            "System voids ExamResult via ExamScoreService.",
            "System writes audit entry.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Result finalized",
                [
                    "E1.1. At step 3, result is locked after exam closure.",
                    "E1.2. System displays: \"Không thể hủy kết quả đã chốt.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    45: {
        "trigger": "Examiner records violation at /views/examiner/violations.",
        "pre": [
            "PRE-1: Examiner is logged in.",
            "PRE-2: Candidate is in active practical exam.",
        ],
        "post": [
            "POST-1: DeductionRecord/violation is stored.",
            "POST-2: Candidate score reflects violation penalty.",
        ],
        "normal": [
            "1.0 Create violation",
            "Examiner opens /views/examiner/violations (ExaminerViolationsServlet).",
            "Examiner selects violation type and candidate.",
            "System records deduction via ScoreDeductionDAO.",
            "Examiner confirms at /views/examiner/violation-confirm.",
            "System updates running score total.",
        ],
        "alt": [
            (
                "A1 Undo violation",
                [
                    "A1.1. Examiner opens violation undo flow.",
                    "A1.2. System reverses deduction if allowed.",
                ],
            ),
        ],
        "exc": [],
    },
    46: {
        "trigger": "Examiner generates theory paper at /views/examiner/exam.",
        "pre": [
            "PRE-1: Examiner is logged in.",
            "PRE-2: Theory section is active for candidate.",
            "PRE-3: Question bank contains sufficient questions.",
        ],
        "post": [
            "POST-1: Randomized theory paper is generated.",
            "POST-2: Candidate paper view is available.",
        ],
        "normal": [
            "1.0 Generate theory paper",
            "Examiner opens /views/examiner/exam (ExaminerExamServlet).",
            "Examiner selects candidate for theory section.",
            "System randomizes questions via TheoryPaperDAO/QuestionDAO.",
            "System displays paper at /views/examiner/candidate-paper.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Insufficient questions",
                [
                    "E1.1. At step 3, question pool is too small.",
                    "E1.2. System displays: \"Không đủ câu hỏi để tạo đề thi.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    47: {
        "trigger": "Examiner exports or prints results via /examiner/export/result or /examiner/print.",
        "pre": [
            "PRE-1: Examiner is logged in.",
            "PRE-2: Exam session has finalized or draft results.",
        ],
        "post": [
            "POST-1: Results file is exported or sent to printer.",
        ],
        "normal": [
            "1.0 Print/export results files",
            "Examiner opens /views/examiner/export (ExaminerMiscServlet) or export toolbar.",
            "Examiner selects export type (Excel/DOCX) or print.",
            "System generates file via /examiner/export/result (ExportServlet) or /examiner/print (PrintServlet).",
            "Examiner downloads or prints official results.",
        ],
        "alt": [
            (
                "A1 DOCX export",
                [
                    "A1.1. Examiner selects DOCX template export.",
                    "A1.2. System generates document via /examiner/export/docx.",
                ],
            ),
        ],
        "exc": [
            (
                "E1 Export failure",
                [
                    "E1.1. At step 3, export service fails.",
                    "E1.2. System displays: \"Không thể xuất kết quả. Vui lòng thử lại.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    48: {
        "trigger": "Admin opens account management at /admin/accounts.",
        "pre": ["PRE-1: Admin is logged in via /staff/login.", "PRE-2: User records exist."],
        "post": ["POST-1: Account create, lock/unlock, or reset action is applied."],
        "normal": [
            "1.0 Manage accounts",
            "Admin navigates to /admin/accounts.",
            "Admin searches user and selects lock, unlock, or reset password.",
            "System validates admin authorization.",
            "System updates User record and writes audit log.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Unauthorized",
                [
                    "E1.1. At step 3, actor is not Admin.",
                    "E1.2. System displays: \"Bạn không có quyền quản trị.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
    },
    49: {
        "trigger": "Admin submits create-staff form at /admin/staff/create.",
        "pre": ["PRE-1: Admin is logged in.", "PRE-2: Staff roles exist."],
        "post": ["POST-1: Staff User account is created.", "POST-2: Credentials are issued."],
        "normal": [
            "1.0 Create staff accounts",
            "Admin opens staff account creation page.",
            "Admin enters staff details and assigns role (Exam Staff, Examiner, Managing Staff).",
            "System validates and creates account.",
            "System displays generated credentials.",
        ],
        "alt": [],
        "exc": [],
    },
    50: {
        "trigger": "Admin opens licence management at /admin/licences.",
        "pre": ["PRE-1: Admin is logged in."],
        "post": ["POST-1: Licence catalogue reflects add/modify/remove actions."],
        "normal": [
            "1.0 Manage driving licenses",
            "Admin opens licence management page.",
            "Admin adds, edits, or deactivates licence categories.",
            "System persists Licence records.",
        ],
        "alt": [],
        "exc": [],
    },
    51: {
        "trigger": "Admin edits licence category attributes at /admin/licences/edit.",
        "pre": ["PRE-1: Admin is logged in.", "PRE-2: Licence category exists."],
        "post": ["POST-1: Licence category attributes and requirements are updated."],
        "normal": [
            "1.0 Modify license categories",
            "Admin selects licence category to edit.",
            "Admin updates name, requirements, and metadata.",
            "System validates and saves Licence record.",
        ],
        "alt": [],
        "exc": [],
    },
    52: {
        "trigger": "Admin updates fees at /admin/licences/fees.",
        "pre": ["PRE-1: Admin is logged in.", "PRE-2: Licence fee records exist."],
        "post": ["POST-1: Exam fees are updated for selected categories."],
        "normal": [
            "1.0 Update license fees",
            "Admin opens fee management for licence categories.",
            "Admin enters new exam fee amounts.",
            "System validates numeric input and saves fees.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Invalid fee",
                [
                    "E1.1. At step 3, fee is negative or non-numeric.",
                    "E1.2. System displays: \"Mức phí không hợp lệ.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    53: {
        "trigger": "Admin manages exam areas at /admin/exam-areas.",
        "pre": ["PRE-1: Admin is logged in."],
        "post": ["POST-1: ExamArea records are created, updated, or removed."],
        "normal": [
            "1.0 Manage exam areas",
            "Admin opens exam area management.",
            "Admin defines location name, address, and capacity details.",
            "System persists ExamArea via admin service.",
        ],
        "alt": [],
        "exc": [],
    },
    54: {
        "trigger": "Admin manages devices at /admin/devices.",
        "pre": ["PRE-1: Admin is logged in."],
        "post": ["POST-1: ExamDevice records reflect add/update/remove actions."],
        "normal": [
            "1.0 Manage exam devices",
            "Admin opens device management page.",
            "Admin registers new device or updates device metadata.",
            "System persists ExamDevice records.",
        ],
        "alt": [],
        "exc": [],
    },
    55: {
        "trigger": "Admin or Examiner updates device status at /views/examiner/devices.",
        "pre": [
            "PRE-1: Actor is Admin or Examiner with device permissions.",
            "PRE-2: ExamDevice record exists.",
        ],
        "post": ["POST-1: Device operational status is updated."],
        "normal": [
            "1.0 Update device status",
            "Actor opens /views/examiner/devices (ExaminerDevicesServlet) or /admin/devices.",
            "Actor selects device and new status (available, in use, maintenance).",
            "System updates ExamDevice status.",
            "Device grid reflects new status.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Device in use",
                [
                    "E1.1. At step 3, device cannot change status while assigned.",
                    "E1.2. System displays: \"Thiết bị đang được sử dụng.\"",
                    "E1.3. Use case resumes at step 2.",
                ],
            ),
        ],
    },
    56: {
        "trigger": "Admin opens /views/examiner/audit.",
        "pre": [
            "PRE-1: Admin is logged in via /staff/login.",
            "PRE-2: Audit records exist.",
        ],
        "post": ["POST-1: Complete system audit log overview is displayed."],
        "normal": [
            "1.0 View audit log (Admin)",
            "Admin navigates to /views/examiner/audit (ExaminerMiscServlet).",
            "System loads full audit history via AuditService.",
            "Admin filters by user, action, and date range.",
            "System displays security audit overview.",
        ],
        "alt": [
            (
                "A1 Export audit",
                [
                    "A1.1. Admin exports audit data.",
                    "A1.2. System generates export via /examiner/export/audit.",
                ],
            ),
        ],
        "exc": [],
    },
}


def default_template(uc: dict[str, str]) -> dict[str, object]:
    primary, secondary = split_actors(uc["actors"])
    name_lower = uc["name"].lower()
    return {
        "trigger": f"{primary} initiates {name_lower}.",
        "pre": [f"PRE-1: {primary} is authenticated if required."],
        "post": [f"POST-1: System completes {name_lower} successfully."],
        "normal": [
            f"1.0 {uc['name']}",
            f"{primary} initiates the use case.",
            f"System processes request according to: {uc['description']}",
            f"{primary} receives outcome feedback.",
        ],
        "alt": [],
        "exc": [
            (
                "E1 Operation failed",
                [
                    "E1.1. System cannot complete the request.",
                    "E1.2. System displays: \"Đã xảy ra lỗi. Vui lòng thử lại.\"",
                    "E1.3. Use case ends.",
                ],
            ),
        ],
        "_primary": primary,
        "_secondary": secondary,
    }


def build_spec_table(uc_id: int, uc: dict[str, str], section_num: str, index: int) -> str:
    template = UC_TEMPLATES.get(uc_id, default_template(uc))
    primary = template.get("_primary") or split_actors(uc["actors"])[0]
    secondary = template.get("_secondary") or split_actors(uc["actors"])[1]

    heading = f"#### {section_num}.{index} {uc['name']}"
    rows = {
        "ID and Name": f"{uc['id']} {uc['name']}",
        "Created By": "TBD",
        "Primary Actor": primary,
        "Secondary Actors": secondary,
        "Description": uc["description"],
        "Trigger": str(template["trigger"]),
        "Preconditions": br(template["pre"]),  # type: ignore[arg-type]
        "Postconditions": br(template["post"]),  # type: ignore[arg-type]
        "Normal Flow": flow_block(str(template["normal"][0]), list(template["normal"][1:])),  # type: ignore[index]
        "Alternative Flows": alt_block(template["alt"]) if template["alt"] else "None",  # type: ignore[arg-type]
        "Exceptions": alt_block(template["exc"]) if template["exc"] else "None",  # type: ignore[arg-type]
    }

    lines = [heading, ""]
    for i, field in enumerate(TABLE_FIELDS):
        value = rows[field].replace("\n", "<br>")
        lines.append(f"| {field} | {value} |")
        if i == 0:
            lines.append(f"| {'-' * len(field)} | {'-' * 20} |")
    lines.append("")
    return "\n".join(lines)


def read_example_section(path: Path) -> str:
    lines = path.read_text(encoding="utf-8").splitlines()
    example_lines = lines[:EXAMPLE_LINE_COUNT]
    if len(example_lines) < EXAMPLE_LINE_COUNT:
        raise ValueError(f"Expected at least {EXAMPLE_LINE_COUNT} lines in {path}")
    return "\n".join(example_lines)


def generate_real_section(uc_rows: dict[int, dict[str, str]]) -> tuple[str, int]:
    parts = ["# real", "", "## 2. Use Case Specifications", ""]
    spec_count = 0

    for section_num, section_name, uc_ids in SECTIONS:
        parts.append(f"### {section_num} {section_name}")
        parts.append("")
        for idx, uc_id in enumerate(uc_ids, start=1):
            if uc_id not in uc_rows:
                raise KeyError(f"UC-{uc_id:02d} not found in UC.md")
            parts.append(build_spec_table(uc_id, uc_rows[uc_id], section_num, idx))
            spec_count += 1

    return "\n".join(parts).rstrip() + "\n", spec_count


def main() -> None:
    uc_rows = parse_uc_table(UC_PATH)
    example = read_example_section(UCSPEC_PATH)
    real_section, spec_count = generate_real_section(uc_rows)

    output = example + "\n\n" + real_section
    UCSPEC_PATH.write_text(output, encoding="utf-8")

    line_count = output.count("\n") + (0 if output.endswith("\n") else 1)
    print(f"Wrote {spec_count} UC specifications to {UCSPEC_PATH}")
    print(f"Total lines: {line_count}")


if __name__ == "__main__":
    main()
