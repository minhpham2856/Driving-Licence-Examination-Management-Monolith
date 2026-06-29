import os, glob, re

def remove_comments(code):
    pattern = r'(\"(?:\\.|[^\\\"])*\"|\'(?:\\.|[^\\\'])*\')|(/\*.*?\*/|//[^\r\n]*)'
    def repl(m):
        if m.group(2): return ''
        return m.group(1)
    return re.sub(pattern, repl, code, flags=re.DOTALL)

def fix_send_error(code):
    return re.sub(r'(response|resp|httpResponse)\.sendError\(\s*([^,]+?)\s*,\s*\"(?:\\.|[^\\\"])*\"\s*\);', r'\1.sendError(\2);', code, flags=re.DOTALL)
    
all_java_files = glob.glob('src/java/**/*.java', recursive=True)
for file in all_java_files:
    with open(file, 'r', encoding='utf-8') as f:
        c = f.read()
    new_c = fix_send_error(c)
    if c != new_c:
        with open(file, 'w', encoding='utf-8') as f:
            f.write(new_c)

examiner_files = glob.glob('src/java/controller/examiner/*.java') + glob.glob('src/java/util/ExaminerUtil.java')
for file in examiner_files:
    with open(file, 'r', encoding='utf-8') as f:
        c = f.read()
    new_c = remove_comments(c)
    new_c = re.sub(r'\n\s*\n\s*\n', '\n\n', new_c)
    if c != new_c:
        with open(file, 'w', encoding='utf-8') as f:
            f.write(new_c)
