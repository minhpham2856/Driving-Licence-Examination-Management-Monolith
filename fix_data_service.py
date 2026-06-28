import sys, re

filepath = 'src/java/service/impl/ExaminerDataServiceImpl.java'
with open(filepath, 'rb') as f:
    text = f.read().decode('utf-8')

# Remove import
text = text.replace('import jakarta.servlet.http.HttpServletRequest;\n', '')

# Replace method signatures
replacements = [
    (r'public void attachToRequest\(HttpServletRequest request, int sessionId, String sbdParam\)', r'public Map<String, Object> getCandidateCallData(int sessionId, String sbdParam)'),
    (r'public void attachToRequest\(HttpServletRequest request, int sessionId, String sbdParam, String searchQuery\)', r'public Map<String, Object> getCandidateCallData(int sessionId, String sbdParam, String searchQuery)'),
    (r'public void attachAuditLogs\(HttpServletRequest request, int sessionId, String pageParam\)', r'public Map<String, Object> getAuditLogsData(int sessionId, String pageParam)'),
    (r'public void attachAuditLogs\(HttpServletRequest request, int sessionId, String pageParam, String searchQuery\)', r'public Map<String, Object> getAuditLogsData(int sessionId, String pageParam, String searchQuery)'),
    (r'public void attachPaperAnswers\(HttpServletRequest request, int sessionId, String sbd, String contextPath\)', r'public Map<String, Object> getPaperAnswersData(int sessionId, String sbd, String contextPath)'),
    (r'public void attachScoreEntry\(HttpServletRequest request, int sessionId, String sbdParam\)', r'public Map<String, Object> getScoreEntryData(int sessionId, String sbdParam)'),
    (r'public void attachResultDetailsEdit\(HttpServletRequest request, int sessionId, String sbdParam\)', r'public Map<String, Object> getResultDetailsEditData(int sessionId, String sbdParam)'),
    (r'public void attachViolation\(HttpServletRequest request, int sessionId, String sbdParam\)', r'public Map<String, Object> getViolationData(int sessionId, String sbdParam)'),
    (r'public void attachDevices\(HttpServletRequest request, int sessionId, String searchQuery\)', r'public Map<String, Object> getDevicesData(int sessionId, String searchQuery)')
]

for old, new_val in replacements:
    pattern = re.compile(old + r'\s*\{')
    def repl(m):
        return new_val + ' {\n        Map<String, Object> __model = new java.util.HashMap<>();'
    text = pattern.sub(repl, text)

# Replace request.setAttribute("...", ...) with __model.put("...", ...)
text = re.sub(r'request\.setAttribute\((.*?),\s*(.*?)\);', r'__model.put(\1, \2);', text)

def replace_returns_in_methods(text):
    methods_to_track = ['getCandidateCallData', 'getAuditLogsData', 'getPaperAnswersData', 'getScoreEntryData', 'getResultDetailsEditData', 'getViolationData', 'getDevicesData']
    
    lines = text.split('\n')
    in_target_method = False
    brace_level = 0
    
    for i, line in enumerate(lines):
        if any(f'public Map<String, Object> {m}' in line for m in methods_to_track):
            in_target_method = True
            brace_level = 0
            
        if in_target_method:
            brace_level += line.count('{')
            brace_level -= line.count('}')
            
            if re.search(r'\breturn\s*;', line):
                lines[i] = re.sub(r'\breturn\s*;', 'return __model;', line)
                
            if brace_level == 0 and '}' in line: 
                idx = line.rfind('}')
                lines[i] = line[:idx] + 'return __model;\n    }' + line[idx+1:]
                in_target_method = False
                
    return '\n'.join(lines)

text = replace_returns_in_methods(text)

with open(filepath, 'wb') as f:
    f.write(text.encode('utf-8'))
