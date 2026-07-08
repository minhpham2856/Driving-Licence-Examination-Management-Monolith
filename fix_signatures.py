import os
import re

DIR = r"src/java/controller/examiner"

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Add missing import if needed
    if 'import enums.ExamSection;' not in content and 'ExamSection' in content:
        content = content.replace("import enums.", "import enums.ExamSection;\nimport enums.")
    elif 'import enums.ExamSection;' not in content:
        content = content.replace("import dto.SessionDTO;", "import dto.SessionDTO;\nimport enums.ExamSection;")

    # Fix method signatures for CallService
    # callNextCandidate(sessionId, user, userId, isTheory, sectionName, destination) -> callNextCandidate(sessionId, user, userId, ExamSection.fromName(sectionName), isTheory, sectionName, destination)
    content = re.sub(r'callService\.callNextCandidate\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\)', 
                     r'callService.callNextCandidate(\1, \2, \3, enums.ExamSection.fromString(\5), \4, \5, \6)', content)

    # callCandidate(sessionId, sbd, user, userId, isTheory, sectionName, destination)
    content = re.sub(r'callService\.callCandidate\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\)', 
                     r'callService.callCandidate(\1, \2, \3, \4, enums.ExamSection.fromString(\6), \5, \6, \7)', content)
                     
    # callSelectedCandidates(sessionId, sbds, user, userId, isTheory, sectionName, destination)
    # minhpn uses: callSelectedCandidates(int sessionId, User user, Integer actionUserId, ExamSection examSection, boolean isTheory, String sectionName, String callDestination, int[] sbds)
    # Wuan used: callSelectedCandidates(sessionId, sbds, user, userId, isTheory, sectionName, destination)
    content = re.sub(r'callService\.callSelectedCandidates\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\)', 
                     r'callService.callSelectedCandidates(\1, \3, \4, enums.ExamSection.fromString(\6), \5, \6, \7, \2)', content)

    # completeCandidateSection(sessionId, sbd, userId) -> completeCandidateSection(sessionId, sbd, userId, null)
    content = re.sub(r'callService\.completeCandidateSection\(([^,]+),\s*([^,]+),\s*([^)]+)\)', 
                     r'callService.completeCandidateSection(\1, \2, \3, null)', content)
                     
    # undoAbsent -> undoPresent, markAbsent -> markPresent
    content = content.replace("callService.undoAbsent", "callService.undoPresent")
    content = content.replace("callService.markAbsent", "callService.markPresent")

    # getCandidateCallData returning Map -> returning CandidateRowDTO or we already handle it in dashboard
    # Wait, other servlets still use getCandidateCallData. Let's fix them manually.
    
    # activeSessionId(session) is removed, but just in case:
    content = re.sub(r'private Integer activeSessionId\([^}]+\}\s*}?', '', content)
    content = re.sub(r'private Integer parseSbdParam\([^}]+\}\s*catch\s*\(NumberFormatException\s*[a-z]\)\s*\{\s*return null;\s*\}\s*}?', '', content)
    content = re.sub(r'private int\[\] parseSbdParams\([^}]+\}[^}]+return result;\s*}?', '', content)
    content = re.sub(r'private String resolveSectionName\([^}]+\}[^}]+return[^;]+;\s*}?', '', content)
    content = re.sub(r'private String resolveCallDestination\([^}]+\}[^}]+return[^;]+;\s*}?', '', content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for filename in os.listdir(DIR):
    if filename.endswith(".java") and filename != "ExaminerDashboardServlet.java":
        process_file(os.path.join(DIR, filename))

print("Fixed CallService signatures.")
