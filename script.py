import os
import glob
import re

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    lines = content.split('\n')
    new_lines = []
    packages_to_import = set()
    
    for line in lines:
        if line.strip().startswith('import ') or line.strip().startswith('package '):
            new_lines.append(line)
            continue
            
        upper_line = line.upper()
        if 'SELECT ' in upper_line or 'JOIN ' in upper_line or 'FROM ' in upper_line or 'WHERE ' in upper_line:
            # Still do findBy replacements even in SQL lines just in case, though unlikely
            pass
        else:
            def replacer(m):
                if m.group(1): 
                    return m.group(1)
                pkg = m.group(3)
                cls = m.group(4)
                
                if pkg.startswith('model'):
                    import_pkg = 'model'
                elif pkg.startswith('dto'):
                    import_pkg = 'dto'
                else:
                    import_pkg = pkg
                    
                packages_to_import.add(import_pkg)
                
                if cls == 'ScoreDeduction':
                    if 'candidate' in pkg:
                        cls = 'AppliedDeduction'
                    else:
                        cls = 'DeductionRule'
                        
                return cls

            pattern = re.compile(r'(\"([^\"\\]|\\.)*\")|\b(java\.[a-z0-9_\.]+|model(?:\.[a-z0-9_]+)?|dto(?:\.[a-z0-9_]+)?|dao(?:\.[a-z0-9_]+)?|service(?:\.[a-z0-9_]+)?|util|enums|controller(?:\.[a-z0-9_]+)?)\.([A-Z][A-Za-z0-9_]*)\b')
            line = pattern.sub(replacer, line)
        
        line = re.sub(r'\bfindByIds\b', 'getAllByIds', line)
        line = re.sub(r'\bfindByExamEnrollmentIds\b', 'getAllByExamEnrollmentIds', line)
        line = re.sub(r'\bfindByExamEnrollmentId\b', 'getByExamEnrollmentId', line)
        line = re.sub(r'\bfindBySessionAndCandidate\b', 'getBySessionAndCandidate', line)
        line = re.sub(r'\bfindByAreaIds\b', 'getAllByAreaIds', line)
        line = re.sub(r'\bfindByNumber\b', 'getByNumber', line)
        line = re.sub(r'\bfindByTheoryPaperIds\b', 'getAllByTheoryPaperIds', line)
        line = re.sub(r'\bfindByTheoryPaperId\b', 'getAllByTheoryPaperId', line)
        line = re.sub(r'\bfindBySessionId\b', 'getAllBySessionId', line)
        line = re.sub(r'\bfindByUserIds\b', 'getAllByUserIds', line)
        line = re.sub(r'\bfindById\b', 'getById', line)
        
        new_lines.append(line)
        
    if packages_to_import:
        final_lines = []
        imports_inserted = False
        java_pkgs = sorted([p for p in packages_to_import if p.startswith('java.') or p.startswith('javax.') or p.startswith('jakarta.')])
        other_pkgs = sorted([p for p in packages_to_import if not (p.startswith('java.') or p.startswith('javax.') or p.startswith('jakarta.'))])
        
        import_block = []
        for p in java_pkgs:
            import_block.append(f'import {p}.*;')
        if java_pkgs and other_pkgs:
            import_block.append('')
        for p in other_pkgs:
            import_block.append(f'import {p}.*;')
            
        for line in new_lines:
            final_lines.append(line)
            if not imports_inserted and line.strip().startswith('package '):
                final_lines.append('')
                final_lines.extend(import_block)
                imports_inserted = True
                
        if not imports_inserted:
            final_lines = import_block + [''] + final_lines
            
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write('\n'.join(final_lines))

java_files = []
for root, dirs, files in os.walk('src/java'):
    for file in files:
        if file.endswith('.java'):
            java_files.append(os.path.join(root, file))

for file in java_files:
    process_file(file)
