import os
import re

def fix_mojibake(text):
    # Regex for a word that contains typical CP1252 mapped bytes for Vietnamese UTF-8
    # Vietnamese UTF-8 bytes range: 0xC3-0xC6 and 0xE1
    # We match words that contain Ã, Ä, Æ, á, áº, á» followed by other CP1252 chars
    # Wait, simple: just try to decode EVERY word. If it works and is different, and looks like Viet, replace it.
    
    # We will split text into non-whitespace blocks
    words = re.split(r'(\s+)', text)
    changed = False
    
    for i, word in enumerate(words):
        # Quick check: does the word contain typical CP1252 chars mapped from UTF-8?
        if any(c in word for c in 'ÃÄÆá'):
            try:
                # Encode to Windows-1252 bytes
                b = word.encode('cp1252')
                # Decode to UTF-8 string
                decoded = b.decode('utf-8')
                # Check if it doesn't contain replacement character and it changed
                if '\ufffd' not in decoded and decoded != word and decoded.strip():
                    words[i] = decoded
                    changed = True
            except (UnicodeEncodeError, UnicodeDecodeError):
                # Cannot encode to cp1252 (e.g., contains real Viet char like 'ả') or not valid utf-8 bytes
                pass
                
    return "".join(words) if changed else None

count = 0
for root, dirs, files in os.walk('src/java'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as file:
                content = file.read()
            
            new_content = fix_mojibake(content)
            if new_content:
                # Write it back
                with open(path, 'w', encoding='utf-8', newline='') as file:
                    file.write(new_content)
                count += 1
                
print(f"Fixed {count} files.")
