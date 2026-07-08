import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

DESCRIPTIONS = {
    "ExamRegistrationDAO.java": "// DAO thao tac dang ky thi va thi sinh theo ca.",
    "ExamRegistrationDAOImpl.java": "// JDBC implementation cho ExamRegistrationDAO.",
    "ExamSessionDAO.java": "// DAO ca thi (Session).",
    "PaymentDAO.java": "// DAO thanh toan le phi thi.",
    "Db2CandidateSql.java": "// SQL doc thi sinh (Candidate + ExamEnrollment).",
    "ExamEnrollmentMergeUtil.java": "// Gop danh sach thi sinh theo CandidateId.",
    "ExamStaffSidebarFilter.java": "// Filter sidebar exam staff + dong bo sessionId.",
    "ExamStaffViewHelper.java": "// Helper chung: session, hang doi, sidebar exam staff.",
    "UploadServlet.java": "// Import DSTS va preview thi sinh.",
    "AllocationServlet.java": "// Phan bo thi sinh theo vong (ly thuyet / TH / duong).",
    "AllocationStageHelper.java": "// Logic loc, sap xep, phan trang allocation.",
    "AllocationPassRules.java": "// Quy tac dat/truot va diem mau allocation.",
    "DashboardServlet.java": "// Dashboard tong quan exam staff.",
    "ProcedureServlet.java": "// Thu tuc thi: thu phi, ky ten.",
    "CandidateCallServlet.java": "// Goi thi sinh len ban thu tuc.",
    "CandidateCallBoard.java": "// Trang thai bang goi thi sinh (session).",
    "CandidateDossierServlet.java": "// Ho so thi sinh chi tiet.",
    "CandidatePhotoServlet.java": "// Upload/chup anh thi sinh.",
    "CandidatePhotoHelper.java": "// Helper duong dan anh thi sinh.",
    "SessionSelectServlet.java": "// Chon ca thi tu sidebar.",
    "SessionControlServlet.java": "// Bat/dung ca thi.",
    "PublicCallServlet.java": "// Man hinh TV goi thi sinh (staff route).",
    "AuditServlet.java": "// Nhat ky thao tac exam staff.",
    "AuditExportServlet.java": "// Xuat Excel nhat ky.",
    "AuditExportLabels.java": "// Nhan tieng Viet cho audit log.",
    "AuditExcelExporter.java": "// Ghi file Excel audit.",
    "ReportServlet.java": "// Bao cao ket qua thi.",
    "ReportStatsHelper.java": "// Thong ke bao cao.",
    "ReportExportLabels.java": "// Nhan xuat bao cao.",
    "ReportExportStats.java": "// Thong ke xuat bao cao.",
    "ReportExcelExporter.java": "// Ghi Excel bao cao.",
    "ExaminerAllocationServlet.java": "// Phan cong giam khao.",
    "DossierFormHelper.java": "// Helper form ho so in.",
    "examstaff-sidebar.js": "// Sidebar exam staff: chon ca thi.",
    "allocation.js": "// JS trang phan bo.",
    "dashboard.js": "// JS dashboard exam staff.",
    "procedure.js": "// JS trang thu tuc.",
    "audit.js": "// JS trang nhat ky.",
    "candidatecall.js": "// JS goi thi sinh.",
    "examiner-allocation.js": "// JS phan cong giam khao.",
    "resolve-candidate-queue.jsp": "<%-- Include hang doi thi sinh tu request. --%>",
    "sidebar-examstaff.jsp": "<%-- Sidebar dieu huong exam staff. --%>",
    "header-examstaff.jsp": "<%-- Header exam staff. --%>",
}


def strip_c_style_comments(text: str) -> str:
    result = []
    i = 0
    n = len(text)
    in_string = None
    while i < n:
        ch = text[i]
        if in_string:
            result.append(ch)
            if ch == "\\" and i + 1 < n:
                result.append(text[i + 1])
                i += 2
                continue
            if ch == in_string:
                in_string = None
            i += 1
            continue
        if i + 1 < n and text[i : i + 2] == "//":
            i += 2
            while i < n and text[i] != "\n":
                i += 1
            continue
        if i + 1 < n and text[i : i + 2] == "/*":
            i += 2
            while i + 1 < n and text[i : i + 2] != "*/":
                i += 1
            i = min(i + 2, n)
            continue
        if ch in ('"', "'"):
            in_string = ch
            result.append(ch)
            i += 1
            continue
        result.append(ch)
        i += 1
    return "".join(result)


def strip_jsp_comments(text: str) -> str:
    text = re.sub(r"<%--.*?--%>", "", text, flags=re.DOTALL)
    text = re.sub(r"<!--.*?-->", "", text, flags=re.DOTALL)
    return text


def cleanup_blank_lines(text: str) -> str:
    lines = text.splitlines()
    cleaned = []
    blank_run = 0
    for line in lines:
        if line.strip() == "":
            blank_run += 1
            if blank_run <= 2:
                cleaned.append("")
        else:
            blank_run = 0
            cleaned.append(line.rstrip())
    return "\n".join(cleaned).rstrip() + "\n"


def default_description(path: Path) -> str:
    stem = path.stem
    if path.suffix.lower() == ".jsp":
        return f"<%-- Trang {stem} exam staff. --%>"
    if path.suffix.lower() == ".js":
        return f"// JS {stem}."
    return f"// {stem}."


def prepend_description(text: str, path: Path) -> str:
    desc = DESCRIPTIONS.get(path.name, default_description(path))
    text = text.lstrip("\n")
    if path.suffix.lower() == ".java":
        if text.startswith("package "):
            return desc + "\n\n" + text
        return desc + "\n\n" + text
    return desc + "\n" + text


def collect_files():
    files = set()
    patterns = [
        ROOT / "src/java/controller/staff/exam",
        ROOT / "src/java/filter/ExamStaffSidebarFilter.java",
        ROOT / "src/java/dao/ExamRegistrationDAO.java",
        ROOT / "src/java/dao/impl/ExamRegistrationDAOImpl.java",
        ROOT / "src/java/dao/ExamSessionDAO.java",
        ROOT / "src/java/dao/PaymentDAO.java",
        ROOT / "src/java/dao/Db2CandidateSql.java",
        ROOT / "src/java/util/ExamEnrollmentMergeUtil.java",
        ROOT / "web/views/staff/examstaff",
        ROOT / "web/views/layout/sidebar-examstaff.jsp",
        ROOT / "web/views/layout/header-examstaff.jsp",
    ]
    js_names = [
        "examstaff-sidebar.js",
        "allocation.js",
        "dashboard.js",
        "procedure.js",
        "audit.js",
        "candidatecall.js",
        "examiner-allocation.js",
    ]
    for p in patterns:
        if p.is_dir():
            for f in p.rglob("*"):
                if f.suffix.lower() in {".java", ".jsp", ".js"}:
                    files.add(f)
        elif p.exists():
            files.add(p)
    for name in js_names:
        f = ROOT / "web/assets/js" / name
        if f.exists():
            files.add(f)
    return sorted(files)


def main():
    changed = []
    for path in collect_files():
        original = path.read_text(encoding="utf-8", errors="replace")
        text = original
        if path.suffix.lower() in {".java", ".js"}:
            text = strip_c_style_comments(text)
        if path.suffix.lower() == ".jsp":
            text = strip_jsp_comments(text)
            text = strip_c_style_comments(text)
        text = cleanup_blank_lines(text)
        text = prepend_description(text, path)
        if text != original:
            path.write_text(text, encoding="utf-8", newline="\n")
            changed.append(str(path.relative_to(ROOT)))
    print(f"Updated {len(changed)} files")
    for f in changed:
        print(f)


if __name__ == "__main__":
    main()
