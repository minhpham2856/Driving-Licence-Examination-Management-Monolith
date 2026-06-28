import sys
filepath = 'src/java/controller/examiner/ExaminerViolationsServlet.java'
with open(filepath, 'rb') as f:
    content = f.read().decode('utf-8')
content = content.replace(
    'sessionId, sbd, reasonCode, reasonDetail, evidencePath, deductionIds, session);',
    'sessionId, sbd, reasonCode, reasonDetail, evidencePath, deductionIds, ((model.user.User) session.getAttribute("user")).getUserId(), resolveSectionType(session), resolveSectionName(session));'
)
if content.startswith('\ufeff'):
    content = content[1:]
with open(filepath, 'wb') as f:
    f.write(content.encode('utf-8'))
