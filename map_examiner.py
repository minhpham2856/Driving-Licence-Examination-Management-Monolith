import os
import re

DIR = r"src/java/controller/examiner"

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Imports
    content = content.replace("import service.ExaminerDataService;", "import service.ExamViewService;\nimport dto.CandidateRowDTO;")
    content = content.replace("import service.impl.ExaminerDataServiceImpl;", "import service.impl.ExamViewServiceImpl;")
    content = content.replace("import service.ExaminerActionsService;", "import service.CallService;\nimport service.ExamScoreService;")
    content = content.replace("import service.impl.ExaminerActionsServiceImpl;", "import service.impl.CallServiceImpl;\nimport service.impl.ExamScoreServiceImpl;")
    content = content.replace("import filter.ExaminerPortalFilter;", "")
    content = content.replace("import filter.ExaminerFilter;", "")
    content = content.replace("import service.ExaminerSessionContextService;", "")
    content = content.replace("import dto.ExaminerSlotDTO;", "import dto.SessionDTO;")
    
    # Export imports
    content = content.replace("import service.ExaminerExportService;", "")
    content = content.replace("import service.impl.ExaminerExportServiceImpl;", "")
    
    # Declarations
    content = content.replace("ExaminerDataService viewDataService = new ExaminerDataServiceImpl();", "ExamViewService viewDataService = new ExamViewServiceImpl();")
    content = content.replace("ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();", "CallService callService = new CallServiceImpl();")
    
    # Method calls
    content = content.replace("examinerService.", "callService.")
    
    # Filter constants
    content = content.replace("ExaminerPortalFilter.ATTR_ACTIVE_SESSION_ID", '"activeSessionId"')
    content = content.replace("ExaminerPortalFilter.isTheorySession(session)", 'Boolean.TRUE.equals(session.getAttribute("isTheory"))')
    content = content.replace("ExaminerPortalFilter.ATTR_SLOT", '"examinerSlot"')
    content = content.replace("ExaminerPortalFilter.ATTR_EXAM_SECTION_NAME", '"examSectionName"')

    # Replace Map<String, Object> candidates with List<CandidateRowDTO> where possible
    content = re.sub(r'List<Map<String, Object>>\s+candidates', 'List<CandidateRowDTO> candidates', content)
    content = re.sub(r'List<Map<String, Object>>\s+filtered', 'List<CandidateRowDTO> filtered', content)
    content = re.sub(r'for\s*\(\s*Map<String, Object>\s+row\s*:', 'for (CandidateRowDTO row :', content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for filename in os.listdir(DIR):
    if filename.endswith(".java"):
        process_file(os.path.join(DIR, filename))

print("Bulk replacement complete.")
