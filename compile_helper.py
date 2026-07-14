import os
files = []
for root, dirs, filenames in os.walk('src/java'):
    for f in filenames:
        if f.endswith('.java'):
            files.append(os.path.join(root, f))
            
with open('files.txt', 'w', encoding='utf-8') as f:
    f.write('\n'.join(files))
