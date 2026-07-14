import os
files_to_fix = [
    'src/java/examstaff/service/impl/ExaminerAllocationDeskServiceImpl.java',
    'src/java/examstaff/util/ExamSessionSummaryMapper.java'
]

for filepath in files_to_fix:
    if not os.path.exists(filepath): continue
    with open(filepath, 'rb') as f:
        content_bytes = f.read()
    
    if content_bytes.startswith(b'\xef\xbb\xbf'):
        content_bytes = content_bytes[3:]
        with open(filepath, 'wb') as f:
            f.write(content_bytes)

