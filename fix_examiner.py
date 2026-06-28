import os
import re

with open('ExaminerActionsServiceImpl.java.backup', 'r', encoding='utf-16') as f:
    text = f.read()

# 1. Remove imports
text = text.replace('import jakarta.servlet.http.HttpSession;\n', '')
text = text.replace('import service.ExaminerSessionContextService;\n', '')

# 2. Fix method signatures
text = text.replace('public boolean callCandidate(int sessionId, String sbd, User user, Integer actionUserId) {', 'public boolean callCandidate(int sessionId, String sbd, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {')
text = text.replace('public String callNextCandidate(int sessionId, User user, Integer actionUserId) {', 'public String callNextCandidate(int sessionId, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {')
text = text.replace('public int callSelectedCandidates(int sessionId, String[] sbds, User user, Integer actionUserId) {', 'public int callSelectedCandidates(int sessionId, String[] sbds, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {')
text = text.replace('public boolean callScoreEntryCandidate(int sessionId, String sbd, User user, Integer actionUserId) {', 'public boolean callScoreEntryCandidate(int sessionId, String sbd, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {')
text = text.replace('public boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,\n            String evidencePath, int[] deductionIds, Integer actionUserId) {', 'public boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,\n            String evidencePath, int[] deductionIds, Integer actionUserId, enums.SectionType sectionType, String sectionName) {')
text = text.replace('public boolean finalizeScoreEntry(int sessionId, String sbd, Integer actionUserId) {', 'public boolean finalizeScoreEntry(int sessionId, String sbd, Integer actionUserId, String sectionKeyword) {')

# Fix unresolved ones because they used HttpSession session instead of actionUserId from the start!
text = text.replace('public boolean callCandidate(int sessionId, String sbd, User user, HttpSession session) {', 'public boolean callCandidate(int sessionId, String sbd, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {')
text = text.replace('public String callNextCandidate(int sessionId, User user, HttpSession session) {', 'public String callNextCandidate(int sessionId, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {')
text = text.replace('public int callSelectedCandidates(int sessionId, String[] sbds, User user, HttpSession session) {', 'public int callSelectedCandidates(int sessionId, String[] sbds, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {')
text = text.replace('public boolean callScoreEntryCandidate(int sessionId, String sbd, User user, HttpSession session) {', 'public boolean callScoreEntryCandidate(int sessionId, String sbd, User user, Integer actionUserId, enums.SectionType sectionType, String sectionName, String callDestination) {')
text = text.replace('public boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,\n            String evidencePath, int[] deductionIds, HttpSession session) {', 'public boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,\n            String evidencePath, int[] deductionIds, Integer actionUserId, enums.SectionType sectionType, String sectionName) {')
text = text.replace('public boolean finalizeScoreEntry(int sessionId, String sbd, HttpSession session) {', 'public boolean finalizeScoreEntry(int sessionId, String sbd, Integer actionUserId, String sectionKeyword) {')

# Bulk replace HttpSession to actionUserId
text = text.replace('HttpSession session', 'Integer actionUserId')
text = text.replace('session != null', 'actionUserId != null')
text = text.replace('session == null', 'actionUserId == null')

# Replace exact method calls
text = text.replace('auditLogService.persist(session,', 'auditLogService.persist(actionUserId,')
text = text.replace('auditLogService.persistFieldChanges(session,', 'auditLogService.persistFieldChanges(actionUserId,')
text = text.replace('auditLogService.persistWarning(session,', 'auditLogService.persistWarning(actionUserId,')

text = text.replace('SectionType sectionType = resolveSectionType(session);\n', '')
text = text.replace('String sectionName = resolveSectionName(session);\n', '')
text = text.replace('SectionType sectionType = resolveSectionType(actionUserId);\n', '')
text = text.replace('String sectionName = resolveSectionName(actionUserId);\n', '')
text = text.replace('String sectionKeyword = resolveSectionName(actionUserId);\n', '')
text = text.replace('String sectionKeyword = resolveSectionName(session);\n', '')

text = text.replace('return insertCall(sessionId, reg, user, session);', 'return insertCall(sessionId, reg, user, actionUserId, callDestination);')
text = text.replace('if (insertCall(sessionId, reg, user, session)) {', 'if (insertCall(sessionId, reg, user, actionUserId, callDestination)) {')
text = text.replace('return insertScoreEntryCall(sessionId, reg, user, session);', 'return insertScoreEntryCall(sessionId, reg, user, actionUserId, callDestination);')
text = text.replace('return insertCall(sessionId, reg, user, actionUserId);', 'return insertCall(sessionId, reg, user, actionUserId, callDestination);')
text = text.replace('if (insertCall(sessionId, reg, user, actionUserId)) {', 'if (insertCall(sessionId, reg, user, actionUserId, callDestination)) {')
text = text.replace('return insertScoreEntryCall(sessionId, reg, user, actionUserId);', 'return insertScoreEntryCall(sessionId, reg, user, actionUserId, callDestination);')

text = text.replace('private boolean insertCall(int sessionId, CandidateEnrollmentDTO reg, User user, Integer actionUserId) {', 'private boolean insertCall(int sessionId, CandidateEnrollmentDTO reg, User user, Integer actionUserId, String callDestination) {')
text = text.replace('private boolean insertScoreEntryCall(int sessionId, CandidateEnrollmentDTO reg, User user, Integer actionUserId) {', 'private boolean insertScoreEntryCall(int sessionId, CandidateEnrollmentDTO reg, User user, Integer actionUserId, String callDestination) {')
text = text.replace('String detail = \"calledTo=\" + resolveScoreEntryCallDestination(session) + \";result=Calling\";', 'String detail = \"calledTo=\" + callDestination + \";result=Calling\";')
text = text.replace('String detail = \"calledTo=\" + resolveScoreEntryCallDestination(actionUserId) + \";result=Calling\";', 'String detail = \"calledTo=\" + callDestination + \";result=Calling\";')
text = text.replace('if (callCandidate(sessionId, sbd.trim(), user, session)) {', 'if (callCandidate(sessionId, sbd.trim(), user, actionUserId, sectionType, sectionName, callDestination)) {')

# Regex remove unwanted methods completely
text = re.sub(r'(?s)\s*@Override\s*public String autoCallScoreEntryIfNeeded.*?\s*return\s+null;\s*\}', '', text)
text = re.sub(r'(?s)\s*@Override\s*public String deferScoreEntryAbsent.*?\s*return\s+nextSbd;\s*\}', '', text)
text = re.sub(r'(?s)\s*private static SectionType resolveSectionType.*?\}', '', text)
text = re.sub(r'(?s)\s*private static String resolveSectionName.*?\}', '', text)
text = re.sub(r'(?s)\s*private static String resolveScoreEntryCallDestination.*?\}', '', text)

text = text.replace('ExaminerScoreEntryQueue.setCalledSbd(session, sessionId, reg.getSbd());', '')
text = text.replace('ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, reg.getSbd());', '')
text = text.replace('ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, null);', '')
text = text.replace('ExaminerScoreEntryQueue.setCalledSbd(actionUserId, sessionId, reg.getSbd());', '')
text = text.replace('ExaminerScoreEntryQueue.setActiveSbd(actionUserId, sessionId, reg.getSbd());', '')
text = text.replace('ExaminerScoreEntryQueue.setActiveSbd(actionUserId, sessionId, null);', '')

with open('src/java/service/impl/ExaminerActionsServiceImpl.java', 'w', encoding='utf-8') as f:
    f.write(text)

