import os
import re

FILES = [
    r"src/java/controller/examiner/ExaminerResultDetailsServlet.java",
    r"src/java/controller/examiner/ExaminerViolationsServlet.java",
    r"src/java/controller/examiner/ExaminerDevicesServlet.java",
    r"src/java/controller/examiner/ExaminerMiscServlet.java"
]

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Imports
    content = content.replace("import controller.examiner.BaseExaminerServlet;", "import jakarta.servlet.http.HttpServlet;")
    content = content.replace("import service.ExaminerDataService;", "import service.ExamViewService;")
    content = content.replace("import service.impl.ExaminerDataServiceImpl;", "import service.impl.ExamViewServiceImpl;")
    content = content.replace("import service.ExaminerActionsService;", "import service.CallService;\nimport service.ExamScoreService;")
    content = content.replace("import service.impl.ExaminerActionsServiceImpl;", "import service.impl.CallServiceImpl;\nimport service.impl.ExamScoreServiceImpl;")
    content = content.replace("import filter.ExaminerPortalFilter;", "")
    content = content.replace("import dto.ExaminerSlotDTO;", "import dto.SessionDTO;")
    
    # Class signature
    content = content.replace("extends BaseExaminerServlet", "extends HttpServlet")

    # Fields
    content = content.replace("ExaminerDataService viewDataService = new ExaminerDataServiceImpl();", "ExamViewService viewDataService = new ExamViewServiceImpl();")
    content = content.replace("ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();", "CallService callService = new CallServiceImpl();")
    
    # Methods
    content = content.replace("examinerService.", "callService.")
    
    # Session handling
    content = re.sub(r'HttpSession\s+session\s*=\s*requireSession\([^)]+\);', 'HttpSession session = request.getSession(false);\n        if (session == null) { response.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }', content)
    content = re.sub(r'activeSessionId\s*\(\s*session\s*\)', '(Integer) session.getAttribute("activeSessionId")', content)
    content = re.sub(r'isTheorySection\s*\(\s*request\s*\)', 'Boolean.TRUE.equals(session.getAttribute("isTheory"))', content)
    
    # Parse helpers (inline them)
    content = re.sub(r'parseSbdParam\s*\(\s*request\.getParameter\(\s*"sbd"\s*\)\s*\)', 'parseSbd(request.getParameter("sbd"))', content)
    content = re.sub(r'parseCandidateNo\s*\(\s*request\.getParameter\(\s*"sbd"\s*\)\s*\)', 'parseSbd(request.getParameter("sbd"))', content)
    content = re.sub(r'parseCandidateNo\s*\(\s*request\.getParameter\(\s*"candidateNo"\s*\)\s*\)', 'parseSbd(request.getParameter("candidateNo"))', content)

    # We will inject the parseSbd method at the end of the class instead of trying to delete Wuan's ones which caused syntax errors.
    # Actually, Wuan's servlets didn't inherit parseSbdParam from BaseExaminerServlet? 
    # Yes, they did! BaseExaminerServlet had parseSbdParam, so the children didn't have it defined locally except in Dashboard/CandidateCall.
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for filepath in FILES:
    process_file(filepath)

print("Fixed remaining servlets.")
