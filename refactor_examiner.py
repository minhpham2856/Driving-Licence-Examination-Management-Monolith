import os
import re

DIR = r"src/java/controller/examiner"

def refactor_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Remove extends BaseExaminer...
    content = re.sub(r'extends\s+BaseExaminer(?:Export)?Servlet', 'extends HttpServlet', content)
    
    # Remove import of BaseExaminerServlet
    content = re.sub(r'import\s+controller\.examiner\.BaseExaminer(?:Export)?Servlet;\s*', '', content)

    # Add HttpServlet import if missing
    if 'extends HttpServlet' in content and 'import jakarta.servlet.http.HttpServlet;' not in content:
        content = content.replace('import jakarta.servlet.http.HttpServletRequest;', 'import jakarta.servlet.http.HttpServletRequest;\nimport jakarta.servlet.http.HttpServlet;')

    # Add ExaminerPortalFilter import if missing
    if 'ExaminerPortalFilter.ATTR_ACTIVE_SESSION_ID' not in content and 'import filter.ExaminerPortalFilter;' not in content:
        if 'jakarta.servlet.http.HttpServletRequest;' in content:
            content = content.replace('import jakarta.servlet.http.HttpServletRequest;', 'import filter.ExaminerPortalFilter;\nimport jakarta.servlet.http.HttpServletRequest;')

    # 2. requireSession -> request.getSession(false)
    content = re.sub(r'HttpSession\s+session\s*=\s*requireSession\([^)]+\);', 'HttpSession session = request.getSession(false);', content)
    
    # Remove if (session == null) return; blocks immediately after getting session
    content = re.sub(r'HttpSession\s+session\s*=\s*request\.getSession\(false\);\s*if\s*\(\s*session\s*==\s*null\s*\)\s*\{\s*return\s*;\s*\}', r'HttpSession session = request.getSession(false);', content)

    # 3. activeSessionId(session) -> session.getAttribute(...)
    content = re.sub(r'activeSessionId\s*\(\s*session\s*\)', r'(Integer) session.getAttribute(ExaminerPortalFilter.ATTR_ACTIVE_SESSION_ID)', content)

    # 4. isTheorySection(request) -> ExaminerPortalFilter.isTheorySession(session)
    content = re.sub(r'isTheorySection\s*\(\s*request\s*\)', r'ExaminerPortalFilter.isTheorySession(session)', content)

    # 5. sbd parsing logic removals
    content = re.sub(r'parseSbdParam\s*\(\s*request\.getParameter\(\s*"sbd"\s*\)\s*\)', r'Integer.parseInt(request.getParameter("sbd"))', content)
    content = re.sub(r'parseCandidateNo\s*\(\s*request\.getParameter\(\s*"sbd"\s*\)\s*\)', r'Integer.parseInt(request.getParameter("sbd"))', content)
    content = re.sub(r'parseDeductionIds\s*\(\s*request\.getParameterValues\(\s*"deductions"\s*\)\s*\)', r'parseDeductionIds(request.getParameterValues("deductions"))', content) # Might still need it, or we replace usage completely

    # 6. Remove redundant local methods (some servlets copied them)
    content = re.sub(r'private\s+HttpSession\s+requireSession\s*\([^{]+\s*\{[^}]*response\.sendError\([^}]*\}\s*return\s*session;\s*\}', '', content)
    content = re.sub(r'private\s+Integer\s+activeSessionId\s*\([^{]+\s*\{[^}]*return\s*\(Integer\)[^}]*\}', '', content)
    content = re.sub(r'private\s+Integer\s+parseSbdParam\s*\([^{]+\s*\{[^}]+catch\s*\(NumberFormatException[^{]+\{[^}]+\}\s*\}', '', content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for filename in os.listdir(DIR):
    if filename.endswith(".java") and filename not in ("BaseExaminerServlet.java", "BaseExaminerExportServlet.java"):
        refactor_file(os.path.join(DIR, filename))

print("Refactored examiner servlets.")
