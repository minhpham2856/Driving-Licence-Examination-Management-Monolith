import os
import re

def fix_mojibake(text):
    changed = False
    
    # Split text into chunks that are either pure mojibake words or anything else.
    # Mojibake words consist of CP1252 characters typically mapped from UTF-8 Vietnamese.
    # We will just split by space and punctuation, BUT keeping punctuation might be hard to restore exactly.
    # Let's use a regex to find all sequences of non-whitespace characters
    
    def replacer(match):
        word = match.group(0)
        if any(c in word for c in 'ÃÄÆá'):
            try:
                b = word.encode('cp1252')
                d = b.decode('utf-8')
                if '\ufffd' not in d and d != word:
                    nonlocal changed
                    changed = True
                    return d
            except:
                pass
        return word

    new_text = re.sub(r'[^\s]+', replacer, text)
    return new_text if changed else None

count = 0
for root, dirs, files in os.walk('src/java'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as file:
                content = file.read()
            
            new_content = fix_mojibake(content)
            if new_content:
                with open(path, 'w', encoding='utf-8', newline='') as file:
                    file.write(new_content)
                count += 1
                print(f"Fixed {path}")
                
print(f"Fixed total {count} files.")
