import os
import re

path = r'src/java/service/impl/ExamViewServiceImpl.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Define method comments
comments = {
    'filterCandidateRows': '// Iterates through candidate rows and filters them down to those matching the given search query string.',
    'loadCandidateRows(int sessionId)': '// Overloaded method to load candidates for a specific session, defaulting to the theory section.',
    'loadCandidateRows(int sessionId, boolean isTheory, String sectionName)': '// Core method to load all candidates in a session. It pulls enrollments, theory stats, section scores, passing flags, and device assignments, and merges them into a list of CandidateRowDTO objects.',
    'buildCandidateSummary': '// Calculates a high-level summary of exam statistics for the session, tallying candidates in different states like COMPLETED, IN_PROGRESS, NOT_STARTED, and checking pass/fail results.',
    'getAuditLogsData(int sessionId, String pageParam)': '// Overloaded method to fetch audit logs data defaulting to no search query.',
    'getAuditLogsData(int sessionId, String pageParam, String searchQuery)': '// Fetches paginated audit logs (with an optional search query) for a specific session. Parses the page number, calculates total pages, and transforms Audit entities into view-friendly map objects.',
    'getPaperAnswersData': '// Gathers detailed paper answers data for a candidate by parsing their submitted CandidateAnswer array, comparing it against the theoretical questions, and determining correct/wrong/unanswered counts.',
    'getScoreEntryQueueData': '// Constructs the "Score Entry Queue" view. It filters out candidates who haven''t finished or are suspended, and orders the list of candidates such that the ones currently in the active testing lanes are prioritized.',
    'getScoreEntryHistoryData': '// Gathers history of score entries for a session to show recent grading events.',
    'getDetailViewData': '// Pulls detailed information for a specific candidate (sbdParam) to be displayed in the candidate detail view. Includes score deductions and paper answers if available.',
    'getDetailEditData': '// Pulls detail data specifically formatted for the candidate edit view.',
    'getResultDetailsEditData': '// Fetches detailed results data for editing, computing the score summary and any deductions applied to this candidate.',
    'isScoreQueueEligible': '// Checks if a given candidate is eligible to be placed in the score entry queue. They must not be absent, suspended, completed, or awaiting signature.',
    'getViolationData': '// Prepares data needed for the violation handling views, including a list of candidates and a dropdown of available violation reasons.',
    'getDevicesData(int sessionId, String searchQuery)': '// Overloaded method to fetch device allocations without specifying a preferred area.',
    'getDevicesData(int sessionId, String searchQuery, Integer preferredAreaId)': '// Retrieves active and maintenance exam devices in the designated area for the session, parsing their status into a view model and filtering by search query if present.',
    'toDeviceRow': '// Transforms an ExamDevice entity into a simplified Map<String, Object> structure for JSP rendering, appending status labels, classes, and material design icons.',
    'loadAreaName': '// Looks up the display name for a given ExamArea ID from the database.',
    'buildSbdLookup': '// Creates a fast lookup mapping from Enrollment ID to Candidate Number (SBD) for efficient resolving during audit log parsing.',
    'isCallEligible': '// Checks if a candidate is eligible to be called into the exam room (e.g., they must not be suspended or already completed).',
    'orderCandidateRowsByQueue': '// Sorts the candidate list to prioritize those currently queued in active exam lanes based on the SectionType.',
    'examSectionFromName': '// Converts a string representation of an exam section into the robust SectionType enum, defaulting to THEORY if invalid.',
    'buildCandidateRow': '// Maps a generic EnrollmentDTO into a comprehensive CandidateRowDTO mapping section logic, scores, and status flags into a single presentation object.',
    'sectionStatusOf': '// Resolves the specific CandidateStatus enum safely from an enrollment record.',
    'statusCssKey': '// Translates a standard CandidateStatus into a legacy UI-friendly CSS key identifier like "done", "testing", or "pending".',
    'matchesSearch': '// Helper to evaluate if a candidate matches the provided search query string (checking against SBD, Name, and Government ID).',
    'contains': '// Null-safe utility to check if a string contains a search query.',
    'formatDate': '// Formats a Date object into a standardized short date string safely, defaulting to "-" if null.',
    'formatSessionDate': '// Retrieves and formats the start time of an exam session safely into a standardized string.',
    'loadLicenceClass': '// Fetches the associated license class string tied to an exam session.',
    'loadScoreDeductions': '// Queries and bundles penalty deduction logs for a specific candidate within a section, merging occurrence counts and recorded timestamps.',
    'applyScoreSummary': '// Retrieves the current accumulated score for a section and updates the model payload with the currentScore and scoreDisqualified flags.',
    'loadPrimarySessionAreaId': '// Determines the primary physical testing area assigned to an exam session.',
    'loadSessionVehicles': '// Loads device context mapped specifically to vehicles (cars, motorcycles) within the session area.',
    'orderRowsByQueue': '// Reorders candidate rows strictly adhering to the external Lane queue prioritization (ExamQueue), ensuring active lane candidates appear first.',
    'buildViolationReasonOptions': '// Generates a predefined dictionary of valid violation reasons for frontend dropdown selection.',
    'isComputerDevice': '// Utility check to confirm if a device is categorized as a computer.',
    'deviceIcon': '// Maps a device type enum to its corresponding material design icon string (e.g., "computer", "two_wheeler").'
}

# Process each comment insertion
lines = content.split('\n')
new_lines = []

def get_base_method(line):
    # Try to match the method name in a public/private method signature
    m = re.search(r'(public|private)(?: static)?(?: final)? [\w<>\[\]\?,\s]+ (\w+)\(', line)
    if m:
        return m.group(2)
    return None

for i, line in enumerate(lines):
    if line.strip().startswith('public ') or line.strip().startswith('private '):
        base = get_base_method(line)
        if base:
            # Match overloaded signatures if present in dictionary
            matched_key = None
            if '(' in line:
                sig_part = line[line.find(base):line.find(')')+1]
                # simplistic match
                for k in comments.keys():
                    if k.startswith(base + '(') and k.replace(' ', '') == sig_part.replace(' ', ''):
                        matched_key = k
                        break
            if not matched_key and base in comments:
                matched_key = base
            
            if matched_key:
                # Add the comment line right before the method
                # Make sure we don't duplicate existing comments
                if i > 0 and not lines[i-1].strip().startswith('//'):
                    new_lines.append('    ' + comments[matched_key])
                elif i > 0 and lines[i-1].strip().startswith('//'):
                    # if there is already a comment, we might replace or append. We just replace the previous comment
                    new_lines[-1] = '    ' + comments[matched_key]
    new_lines.append(line)

with open(path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(new_lines))

print("Done")
