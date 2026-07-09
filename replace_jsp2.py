import os
import glob

files = [
    r'web/views/examiner/violation-detail.jsp',
    r'web/views/examiner/violation-undo.jsp'
]

for path in files:
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        content = content.replace('', '')
        
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)

print("Done files")
