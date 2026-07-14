import os
import re

files_to_fix = [
    'src/java/examstaff/dao/Db2CandidateSql.java',
    'src/java/examstaff/dao/impl/AuditLogDAOImpl.java',
    'src/java/examstaff/dao/impl/ExamAreaDAOImpl.java',
    'src/java/examstaff/dao/impl/ExaminerAssignmentDAOImpl.java',
    'src/java/examstaff/dao/impl/PaymentDAOImpl.java',
    'src/java/examstaff/service/impl/AllocationActionServiceImpl.java',
    'src/java/examstaff/service/impl/ExaminerAllocationDeskServiceImpl.java',
    'src/java/examstaff/service/impl/ExaminerAllocationServiceImpl.java',
    'src/java/examstaff/service/impl/ExamSelectServiceImpl.java',
    'src/java/examstaff/service/impl/ExamSessionControlServiceImpl.java',
    'src/java/examstaff/service/impl/ProcedurePaymentServiceImpl.java',
    'src/java/examstaff/service/impl/ReportFeeQueryServiceImpl.java',
    'src/java/examstaff/service/impl/StaffAuditLogServiceImpl.java',
    'src/java/examstaff/util/ExamAreaTypeResolver.java',
    'src/java/examstaff/util/ExaminerAssignmentRules.java',
    'src/java/examstaff/util/ExamSessionSummaryMapper.java'
]

for filepath in files_to_fix:
    if not os.path.exists(filepath): continue
    with open(filepath, 'rb') as f:
        content_bytes = f.read()
    
    if content_bytes.startswith(b'\xef\xbb\xbf'):
        content_bytes = content_bytes[3:]
    
    content = content_bytes.decode('utf-8')
    
    # Fix the broken replacements
    content = content.replace('shared.enums."N\'Ho?n t?t\', N\'Paid\'"', '"N\'Ho?n t?t\', N\'Paid\'"')
    content = content.replace('ExamStaffMessage.EXAM_NOT_FOUND_PREFIX.getValue() + )', 'ExamStaffMessage.EXAM_NOT_FOUND_PREFIX.getValue() + param)')
    content = content.replace('shared.enums.ExamSessionStatus.CHO_THI.getValue().equals()))', 'shared.enums.ExamSessionStatus.CHO_THI.getValue().equals(examSession.getStatus()))')
    content = content.replace('shared.enums.ExamSessionStatus.DANG_DIEN_RA.getValue().equals()))', 'shared.enums.ExamSessionStatus.DANG_DIEN_RA.getValue().equals(examSession.getStatus()))')
    content = content.replace('(shared.enums.PaymentStatus.COMPLETED.getValue().equalsIgnoreCase() || "Paid".equalsIgnoreCase()))', '(shared.enums.PaymentStatus.COMPLETED.getValue().equalsIgnoreCase(payment.getPaymentStatus()) || "Paid".equalsIgnoreCase(payment.getPaymentStatus())))')
    content = content.replace('log.setEntityName());', 'log.setEntityName(resolveEntityName(action, details));')
    content = content.replace('String resolved = ( + " " + );', 'String resolved = (action + " " + details);')
    content = content.replace('// Forced recompilation trigger\n', '')
    
    with open(filepath, 'wb') as f:
        f.write(content.encode('utf-8'))

print("Fixed syntax errors and stripped BOMs.")
