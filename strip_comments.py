import os
import re

def remove_comments(text):
    pattern = r'(".*?"|\'.*?\')|(/\*.*?\*/|//[^\r\n]*$)'
    regex = re.compile(pattern, re.MULTILINE | re.DOTALL)
    def _replacer(match):
        if match.group(2) is not None:
            return ''
        else:
            return match.group(1)
    return regex.sub(_replacer, text)

dirs = ['src/java/dao', 'src/java/service']
count = 0
for d in dirs:
    for root, _, files in os.walk(d):
        for f in files:
            if f.endswith('.java'):
                path = os.path.join(root, f)
                with open(path, 'r', encoding='utf-8') as file:
                    content = file.read()
                new_content = remove_comments(content)
                if new_content != content:
                    with open(path, 'w', encoding='utf-8') as file:
                        file.write(new_content)
                    count += 1
print(f'Stripped comments from {count} files.')
